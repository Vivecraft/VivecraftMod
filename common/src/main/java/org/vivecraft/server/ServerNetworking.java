package org.vivecraft.server;

import net.minecraft.IdentifierException;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivecraft.Xplat;
import org.vivecraft.api.data.FBTMode;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.common.CommonDataHolder;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.NetworkVersion;
import org.vivecraft.common.network.VrPlayerState;
import org.vivecraft.common.network.packet.PayloadIdentifier;
import org.vivecraft.common.network.packet.c2s.*;
import org.vivecraft.common.network.packet.s2c.*;
import org.vivecraft.data.ViveItems;
import org.vivecraft.mixin.server.ChunkMapAccessor;
import org.vivecraft.mixin.server.TrackedEntityAccessor;
import org.vivecraft.mod_compat_vr.ReplayHelper;
import org.vivecraft.server.config.ConfigBuilder;
import org.vivecraft.server.config.ServerConfig;
import org.vivecraft.server.config.enums.ClimbeyBlockmode;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ServerNetworking {

    // temporarily stores the packets from legacy clients to assemble a complete VrPlayerState
    private static final Map<UUID, Map<PayloadIdentifier, VivecraftPayloadC2S>> LEGACY_DATA_MAP = new HashMap<>();

    /**
     * logger for messages from the server
     */
    public static final Logger LOGGER = LoggerFactory.getLogger("VivecraftServer");

    /**
     * handles a {@link VivecraftPayloadC2S} sent to the server
     *
     * @param c2sPayload     payload that needs to be handled
     * @param player         ServerPlayer that sent the packet
     * @param packetConsumer consumer to send packets back with
     */
    public static void handlePacket(
        VivecraftPayloadC2S c2sPayload, ServerPlayer player, Consumer<VivecraftPayloadS2C> packetConsumer)
    {
        if (c2sPayload instanceof UnknownPayloadC2S) return;
        ServerVivePlayer vivePlayer = ServerVRPlayers.getVivePlayer(player);

        // clients are expected to send a VERSION packet first
        if (vivePlayer == null && c2sPayload.payloadId() != PayloadIdentifier.VERSION) {
            return;
        }

        // the player object changes in some circumstances, like respawning, so need to make sure it's up to date
        if (vivePlayer != null) {
            vivePlayer.player = player;
        }

        switch (c2sPayload.payloadId()) {
            case VERSION -> {
                // Vivecraft client connected, send server settings
                vivePlayer = new ServerVivePlayer(player);

                VersionPayloadC2S payload = (VersionPayloadC2S) c2sPayload;

                if (ServerConfig.DEBUG.get()) {
                    LOGGER.info("Vivecraft: player '{}' joined with {}", player.getName().getString(),
                        payload.version());
                }

                if (!payload.legacy()) {
                    // check if client supports a supported version
                    if (CommonNetworkHelper.MIN_SUPPORTED_NETWORK_PROTOCOL <= payload.maxVersion() &&
                        payload.minVersion() <= CommonNetworkHelper.MAX_SUPPORTED_NETWORK_PROTOCOL)
                    {
                        vivePlayer.networkVersion = NetworkVersion.fromProtocolVersion(
                            Math.min(payload.maxVersion(), CommonNetworkHelper.MAX_SUPPORTED_NETWORK_PROTOCOL));
                        if (ServerConfig.DEBUG.get()) {
                            LOGGER.info("Vivecraft: {} networking supported, using version {}",
                                player.getName().getString(), vivePlayer.networkVersion);
                        }
                    } else {
                        // unsupported version, send notification, and disregard
                        player.sendSystemMessage(
                            Component.literal("Unsupported vivecraft version, VR features will not work"));
                        if (ServerConfig.DEBUG.get()) {
                            LOGGER.info(
                                "Vivecraft: {} networking not supported. client range [{},{}], server range [{},{}]",
                                player.getScoreboardName(),
                                payload.minVersion(),
                                payload.maxVersion(),
                                CommonNetworkHelper.MIN_SUPPORTED_NETWORK_PROTOCOL,
                                CommonNetworkHelper.MAX_SUPPORTED_NETWORK_PROTOCOL);
                        }
                        return;
                    }
                } else {
                    // client didn't send a version, so it's a legacy client
                    vivePlayer.networkVersion = NetworkVersion.LEGACY;
                    if (ServerConfig.DEBUG.get()) {
                        LOGGER.info("Vivecraft: {} using legacy networking", player.getScoreboardName());
                    }
                }

                vivePlayer.setVR(payload.vr());

                ServerVRPlayers.getPlayersWithVivecraft(player.level().getServer()).put(player.getUUID(), vivePlayer);

                packetConsumer.accept(new VersionPayloadS2C(CommonDataHolder.getInstance().versionIdentifier));
                packetConsumer.accept(new RequestDataPayloadS2C());

                // send server settings
                if (ServerConfig.CLIMBEY_ENABLED.get()) {
                    packetConsumer.accept(getClimbeyServerPayload());
                }

                // always send in new versions to allow disabling of teleports
                if (ServerConfig.TELEPORT_ENABLED.get() ||
                    NetworkVersion.OPTION_TOGGLE.accepts(vivePlayer.networkVersion))
                {
                    packetConsumer.accept(
                        new TeleportPayloadS2C(ServerConfig.TELEPORT_ENABLED.get(), vivePlayer.networkVersion));
                }

                if (ServerConfig.TELEPORT_LIMITED_SURVIVAL.get()) {
                    packetConsumer.accept(getSurvivalTeleportOverridePayload());
                }

                if (ServerConfig.WORLDSCALE_LIMITED.get()) {
                    packetConsumer.accept(getWorldScaleOverridePayload());
                }

                if (ServerConfig.FORCE_THIRD_PERSON_ITEMS.get()) {
                    packetConsumer.accept(getThirdPersonItemsOverridePayload());
                }

                if (ServerConfig.FORCE_THIRD_PERSON_ITEMS_CUSTOM.get()) {
                    packetConsumer.accept(getThirdPersonItemsCustomOverridePayload());
                }

                if (ServerConfig.CRAWLING_ENABLED.get()) {
                    packetConsumer.accept(new CrawlPayloadS2C(true, vivePlayer.networkVersion));
                }

                // send if hotswitching is allowed
                packetConsumer.accept(getVRSwitchingPayload());

                if (NetworkVersion.DUAL_WIELDING.accepts(vivePlayer.networkVersion)) {
                    packetConsumer.accept(new DualWieldingPayloadS2C(ServerConfig.DUAL_WIELDING.get()));
                }

                // send vr changes settings, to inform the client what is non default
                if (NetworkVersion.SERVER_VR_CHANGES.accepts(vivePlayer.networkVersion)) {
                    Map<String, String> settings = new HashMap<>();
                    for (ConfigBuilder.ConfigValue<?> config : ServerConfig.getConfigValues()) {
                        if (config.getPath().startsWith("vrChanges") && !config.isDefault()) {
                            settings.put(config.getPath(), String.valueOf(config.get()));
                        }
                    }
                    if (!settings.isEmpty()) {
                        packetConsumer.accept(new ServerVrChangesS2CPacket(settings));
                    }
                }

                if (NetworkVersion.OPTION_TOGGLE.accepts(vivePlayer.networkVersion)) {
                    packetConsumer.accept(
                        new AttackWhileBlockingPayloadS2C(ServerConfig.ALLOW_ATTACKS_WHILE_BLOCKING.get()));
                }

                packetConsumer.accept(new NetworkVersionPayloadS2C(vivePlayer.networkVersion));
            }
            case IS_VR_ACTIVE -> {
                VRActivePayloadC2S payload = (VRActivePayloadC2S) c2sPayload;
                if (vivePlayer.isVR() == payload.vr()) {
                    break;
                }
                vivePlayer.setVR(!vivePlayer.isVR());
                if (!vivePlayer.isVR()) {
                    // send all nearby players that the state changed
                    // this is only needed for OFF, to delete the clientside vr player state
                    sendPacketToTrackingPlayers(vivePlayer, new VRActivePayloadS2C(false, player.getUUID()));
                }
            }
            case DRAW -> vivePlayer.draw = ((DrawPayloadC2S) c2sPayload).draw();
            case VR_PLAYER_STATE -> vivePlayer.setVrPlayerState(((VRPlayerStatePayloadC2S) c2sPayload).playerState());
            case WORLDSCALE -> vivePlayer.worldScale = ((WorldScalePayloadC2S) c2sPayload).worldScale();
            case HEIGHT -> vivePlayer.heightScale = ((HeightPayloadC2S) c2sPayload).heightScale();
            case TELEPORT -> {
                if (!ServerConfig.TELEPORT_ENABLED.get()) break;
                TeleportPayloadC2S payload = (TeleportPayloadC2S) c2sPayload;

                if (ServerConfig.TELEPORT_FOOD_EXHAUSTION.get() && payload.y() > player.getY()) {
                    // cause food exhaustion when tping up blocks, to mimic jumping up
                    player.causeFoodExhaustion(0.05F);
                }

                player.absSnapTo(payload.x(), payload.y(), payload.z(), player.getYRot(), player.getXRot());
            }
            case CLIMBING -> {
                if (!ServerConfig.CLIMBEY_ENABLED.get()) break;
                player.fallDistance = 0.0F;
                player.connection.aboveGroundTickCount = 0;
                if (ServerConfig.CLIMBEY_FOOD_EXHAUSTION.get() &&
                    (ViveItems.isClimbingClaws(player.getMainHandItem()) ||
                        ViveItems.isClimbingClaws(player.getOffhandItem())
                    ))
                {
                    player.causeFoodExhaustion(0.005F);
                }
            }
            case JUMPING -> {
                if (ServerConfig.CLIMBEY_FOOD_EXHAUSTION.get()) {
                    player.causeFoodExhaustion(0.3F);
                }
            }
            case ACTIVEHAND -> {
                ActiveBodyPartPayloadC2S activeBodypart = (ActiveBodyPartPayloadC2S) c2sPayload;
                VRBodyPart newBodyPart = activeBodypart.bodyPart();
                if (vivePlayer.isSeated() && newBodyPart != VRBodyPart.HEAD) {
                    newBodyPart = VRBodyPart.MAIN_HAND;
                }
                vivePlayer.useBodyPartForAim = activeBodypart.useForAim();
                if (vivePlayer.activeBodyPart != newBodyPart && !vivePlayer.isDrawing() &&
                    ServerConfig.DUAL_WIELDING.get() && NetworkVersion.DUAL_WIELDING.accepts(vivePlayer.networkVersion))
                {
                    // handle equipment changes
                    ItemStack oldItem = player.getItemBySlot(EquipmentSlot.MAINHAND);
                    vivePlayer.activeBodyPart = newBodyPart;
                    ItemStack newItem = player.getItemBySlot(EquipmentSlot.MAINHAND);

                    // in case the item broke
                    applyEquipmentChange(player, vivePlayer.activeItemOverride, oldItem);
                    // actual item change
                    applyEquipmentChange(player, oldItem, newItem);
                    // store in case it breaks
                    vivePlayer.activeItemOverride = newItem.copy();
                } else {
                    // still update it, since it ued for the bow
                    vivePlayer.activeBodyPart = newBodyPart;
                }
            }
            case CRAWL -> {
                if (!ServerConfig.CRAWLING_ENABLED.get()) break;
                vivePlayer.crawling = ((CrawlPayloadC2S) c2sPayload).crawling();
                if (vivePlayer.crawling) {
                    player.setPose(Pose.SWIMMING);
                }
            }
            case DAMAGE_DIRECTION -> vivePlayer.wantsDamageDirection = true;
            case AIM_OVERRIDE_RESET -> {
                AimOverrideResetPayloadC2S reset = (AimOverrideResetPayloadC2S) c2sPayload;
                if (reset.ticks() == 0) {
                    vivePlayer.aimDirOverride = null;
                    vivePlayer.aimPosOverride = null;
                }
                vivePlayer.aimReset = reset.ticks();
            }
            case AIM_DIRECTION_OVERRIDE ->
                vivePlayer.aimDirOverride = ((AimDirOverridePayloadC2S) c2sPayload).direction();
            case AIM_POSITION_OVERRIDE -> vivePlayer.aimPosOverride = player.position()
                .add(((AimPosOverridePayloadC2S) c2sPayload).position().x(),
                    ((AimPosOverridePayloadC2S) c2sPayload).position().y(),
                    ((AimPosOverridePayloadC2S) c2sPayload).position().z());
            // legacy support
            case CONTROLLER0DATA, CONTROLLER1DATA, HEADDATA -> {
                Map<PayloadIdentifier, VivecraftPayloadC2S> playerData;
                if ((playerData = LEGACY_DATA_MAP.get(player.getUUID())) == null) {
                    playerData = new HashMap<>();
                    LEGACY_DATA_MAP.put(player.getUUID(), playerData);
                }
                // keep the payload around
                playerData.put(c2sPayload.payloadId(), c2sPayload);

                if (playerData.size() == 3) {
                    // we have all data
                    LegacyController0DataPayloadC2S controller0Data = (LegacyController0DataPayloadC2S) playerData
                        .get(PayloadIdentifier.CONTROLLER0DATA);
                    LegacyController1DataPayloadC2S controller1Data = (LegacyController1DataPayloadC2S) playerData
                        .get(PayloadIdentifier.CONTROLLER1DATA);
                    LegacyHeadDataPayloadC2S headData = (LegacyHeadDataPayloadC2S) playerData
                        .get(PayloadIdentifier.HEADDATA);

                    vivePlayer.setVrPlayerState(new VrPlayerState(
                        headData.seated(), // isSeated
                        headData.hmdPose(), // head pose
                        controller0Data.leftHanded(), // leftHanded 0
                        controller0Data.mainHand(), // mainHand pose
                        controller1Data.leftHanded(), // leftHanded 1
                        controller1Data.offHand(), // offHand pose
                        FBTMode.ARMS_ONLY, null,
                        null, null,
                        null, null,
                        null, null));

                    LEGACY_DATA_MAP.remove(player.getUUID());
                }
            }
            default -> throw new IllegalStateException(
                "Vivecraft: got unexpected packet on server: " + c2sPayload.payloadId());
        }
    }

    /**
     * attribute modification, based on vanilla code: {@link net.minecraft.world.entity.LivingEntity#collectEquipmentChanges}
     *
     * @param player  player to modify attributes for
     * @param oldItem old item to remove the attributes for
     * @param newItem new item to add the attributes for
     */
    private static void applyEquipmentChange(ServerPlayer player, ItemStack oldItem, ItemStack newItem) {
        if (player.equipmentHasChanged(oldItem, newItem)) {
            AttributeMap attributeMap = player.getAttributes();
            if (!oldItem.isEmpty()) {
                oldItem.forEachModifier(EquipmentSlot.MAINHAND, (holder, attributeModifier) -> {
                    AttributeInstance attributeInstance = attributeMap.getInstance(holder);
                    if (attributeInstance != null) {
                        attributeInstance.removeModifier(attributeModifier);
                    }
                });
            }

            if (!newItem.isEmpty()) {
                newItem.forEachModifier(EquipmentSlot.MAINHAND, (holder, attributeModifier) -> {
                    AttributeInstance attributeInstance = attributeMap.getInstance(holder);
                    if (attributeInstance != null) {
                        attributeInstance.removeModifier(attributeModifier.id());
                        attributeInstance.addTransientModifier(attributeModifier);
                    }
                });
            }
        }
    }

    /**
     * @return CLIMBING payload holding blockmode and list of blocks
     */
    public static VivecraftPayloadS2C getClimbeyServerPayload() {
        List<String> blocks = null;
        if (ServerConfig.CLIMBEY_BLOCKMODE.get() != ClimbeyBlockmode.DISABLED) {
            blocks = new ArrayList<>();
            for (String block : ServerConfig.CLIMBEY_BLOCKLIST.get()) {
                try {
                    Holder.Reference<Block> b = BuiltInRegistries.BLOCK.get(Identifier.parse(block))
                        .orElseGet(() -> null);
                    // only send valid blocks
                    if (b != null && b.value() != Blocks.AIR) {
                        blocks.add(block);
                    }
                } catch (IdentifierException ignore) {}
            }
        }
        return new ClimbingPayloadS2C(ServerConfig.CLIMBEY_ENABLED.get(), ServerConfig.CLIMBEY_BLOCKMODE.get(), blocks);
    }

    /**
     * @return VR switching payload for the current settings
     */
    public static VivecraftPayloadS2C getVRSwitchingPayload() {
        return new VRSwitchingPayloadS2C(ServerConfig.VR_SWITCHING_ENABLED.get() && !ServerConfig.VR_ONLY.get());
    }

    /**
     * @return Survival TP override payload for the current settings
     */
    public static VivecraftPayloadS2C getSurvivalTeleportOverridePayload() {
        return new SettingOverridePayloadS2C(Map.of(
            "limitedTeleport", "true",
            "teleportLimitUp", String.valueOf(ServerConfig.TELEPORT_UP_LIMIT.get()),
            "teleportLimitDown", String.valueOf(ServerConfig.TELEPORT_DOWN_LIMIT.get()),
            "teleportLimitHoriz", String.valueOf(ServerConfig.TELEPORT_HORIZONTAL_LIMIT.get())
        ), !ServerConfig.TELEPORT_LIMITED_SURVIVAL.get());
    }

    /**
     * @return world scale override payload for the current settings
     */
    public static VivecraftPayloadS2C getWorldScaleOverridePayload() {
        return new SettingOverridePayloadS2C(Map.of(
            "worldScale.min", String.valueOf(ServerConfig.WORLDSCALE_MIN.get()),
            "worldScale.max", String.valueOf(ServerConfig.WORLDSCALE_MAX.get())
        ), !ServerConfig.WORLDSCALE_LIMITED.get());
    }

    /**
     * @return third person transforms override payload for the current settings
     */
    public static VivecraftPayloadS2C getThirdPersonItemsOverridePayload() {
        return new SettingOverridePayloadS2C(Map.of(
            "thirdPersonItems", "true"
        ), !ServerConfig.FORCE_THIRD_PERSON_ITEMS.get());
    }

    /**
     * @return custom third person transforms override payload for the current settings
     */
    public static VivecraftPayloadS2C getThirdPersonItemsCustomOverridePayload() {
        return new SettingOverridePayloadS2C(Map.of(
            "thirdPersonItemsCustom", "true"
        ), !ServerConfig.FORCE_THIRD_PERSON_ITEMS_CUSTOM.get());
    }

    /**
     * Sends an update packet for the given {@code config} to all ServerVivePlayer on the {@code server}
     *
     * @param server server to get the vive players from
     * @param config ConfigValue to send an update for
     */
    public static void sendUpdatePacketToAll(MinecraftServer server, ConfigBuilder.ConfigValue<?> config) {
        Function<ServerVivePlayer, VivecraftPayloadS2C> function = config.getPacketFunction();
        if (function != null) {
            for (ServerVivePlayer vivePlayer : ServerVRPlayers.getPlayersWithVivecraft(server).values()) {
                VivecraftPayloadS2C payload = function.apply(vivePlayer);
                // old clients cannot clear server overrides, crawl or tp
                if (!NetworkVersion.OPTION_TOGGLE.accepts(vivePlayer.networkVersion) &&
                    ((payload instanceof SettingOverridePayloadS2C override && override.clear()) ||
                        (payload instanceof CrawlPayloadS2C crawl && !crawl.allowed()) ||
                        (payload instanceof TeleportPayloadS2C tp && !tp.allowed())
                    ))
                {
                    continue;
                }
                vivePlayer.player.connection.send(Xplat.INSTANCE.getS2CPacket(payload));
            }
        }
    }

    /**
     * kicks any players that are not allowed based on the current vive/vr only settings, and sends if vr switching is allowed
     *
     * @param server server to get the vive players from
     */
    public static void updateViveVROnly(MinecraftServer server) {
        // get all players
        // need to make a copy, since kicking a player causes a concurrent modification exception
        for (ServerPlayer player : new ArrayList<>(server.getPlayerList().getPlayers())) {
            // this could technically cause a race condition, where a player didn't send the vivecraft packet yet and
            // gets kicked because of that, but that should be neglectable, since server settings don't change that often
            ServerUtil.kickIfNotAllowed(player);
        }

        // update if vr switching is allowed
        for (ServerVivePlayer vivePlayer : ServerVRPlayers.getPlayersWithVivecraft(server).values()) {
            vivePlayer.player.connection.send(Xplat.INSTANCE.getS2CPacket(getVRSwitchingPayload()));
        }
    }

    /**
     * removes the crawl state from every vive player, if crawling is disabled
     *
     * @param server server to get the vive players from
     */
    public static void updateCrawling(MinecraftServer server) {
        if (!ServerConfig.CRAWLING_ENABLED.get()) {
            // remove the current crawl state from every player
            for (ServerVivePlayer vivePlayer : ServerVRPlayers.getPlayersWithVivecraft(server).values()) {
                vivePlayer.crawling = false;
            }
        }
    }

    /**
     * sends a haptic event to the given player if they are in VR, to be processed on the client
     */
    public static void sendHapticToClient(
        ServerPlayer player, VRBodyPart bodyPart, float duration, float frequency, float amplitude, float delay)
    {
        ServerVivePlayer vivePlayer = ServerVRPlayers.getVivePlayer(player);
        if (vivePlayer != null && vivePlayer.isVR() &&
            NetworkVersion.HAPTIC_PACKET.accepts(vivePlayer.networkVersion))
        {
            vivePlayer.player.connection.send(
                Xplat.INSTANCE.getS2CPacket(new HapticPayloadS2C(bodyPart, duration, frequency, amplitude, delay)));
        }
    }

    /**
     * send the players VR data to all other players that can see them
     *
     * @param vivePlayer player to send the VR data for
     */
    public static void sendVrPlayerStateToClients(ServerVivePlayer vivePlayer) {
        // create the packets here, to try to avoid unnecessary memory copies when creating multiple packets
        Packet<?> legacyPacket = Xplat.INSTANCE.getS2CPacket(
            new UberPacketPayloadS2C(vivePlayer.player.getUUID(),
                new VrPlayerState(vivePlayer.vrPlayerState(), NetworkVersion.LEGACY),
                vivePlayer.worldScale, vivePlayer.heightScale));
        Packet<?> newPacket = Xplat.INSTANCE.getS2CPacket(
            new UberPacketPayloadS2C(vivePlayer.player.getUUID(), vivePlayer.vrPlayerState(), vivePlayer.worldScale,
                vivePlayer.heightScale));

        sendPacketToTrackingPlayers(vivePlayer,
            (version) -> version == NetworkVersion.LEGACY ? legacyPacket : newPacket);
    }

    /**
     * gets all players that can see {@code player}
     *
     * @param player ServerPlayer to check
     * @return unmodifiableSet set of all other players that can see {@code player}
     */
    public static Set<ServerPlayerConnection> getTrackingPlayers(ServerPlayer player) {
        ChunkMap chunkMap = player.level().getChunkSource().chunkMap;
        TrackedEntityAccessor playerTracker = ((ChunkMapAccessor) chunkMap).getTrackedEntities().get(player.getId());
        return playerTracker != null ? Collections.unmodifiableSet(playerTracker.getPlayersTracking()) :
            Collections.emptySet();
    }

    /**
     * sends a packet to all players that can see {@code vivePlayer}
     *
     * @param vivePlayer player that needs to be seen to get the packet
     * @param payload    payload to send
     */
    private static void sendPacketToTrackingPlayers(ServerVivePlayer vivePlayer, VivecraftPayloadS2C payload) {
        Packet<?> packet = Xplat.INSTANCE.getS2CPacket(payload);
        sendPacketToTrackingPlayers(vivePlayer, (v) -> packet);
    }

    /**
     * sends a packet to all players that can see {@code vivePlayer}
     *
     * @param vivePlayer     player that needs to be seen to get the packet
     * @param packetProvider provider for network packets, based on client network version
     */
    private static void sendPacketToTrackingPlayers(
        ServerVivePlayer vivePlayer, Function<NetworkVersion, Packet<?>> packetProvider)
    {
        Map<UUID, ServerVivePlayer> vivePlayers = ServerVRPlayers.getPlayersWithVivecraft(
            vivePlayer.player.level().getServer());
        for (var trackedPlayer : getTrackingPlayers(vivePlayer.player)) {
            if (!vivePlayers.containsKey(trackedPlayer.getPlayer().getUUID()) ||
                trackedPlayer.getPlayer() == vivePlayer.player)
            {
                continue;
            }
            trackedPlayer.send(packetProvider.apply(vivePlayer.networkVersion));
        }
        if (ServerConfig.SEND_DATA_TO_OWNER.get() || ReplayHelper.isLoaded()) {
            // force on when a replay mod is loaded
            vivePlayer.player.connection.send(packetProvider.apply(vivePlayer.networkVersion));
        }
    }
}
