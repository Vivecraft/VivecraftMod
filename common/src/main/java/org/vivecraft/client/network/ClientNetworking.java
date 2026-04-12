package org.vivecraft.client.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;
import org.vivecraft.Xplat;
import org.vivecraft.api.data.FBTMode;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.client.ClientVRPlayers;
import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.settings.AutoCalibration;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.CommonDataHolder;
import org.vivecraft.common.VRServerPerms;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.NetworkVersion;
import org.vivecraft.common.network.VrPlayerState;
import org.vivecraft.common.network.packet.c2s.*;
import org.vivecraft.common.network.packet.s2c.*;
import org.vivecraft.common.utils.MathUtils;
import org.vivecraft.mod_compat_vr.ReplayHelper;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Supplier;

public class ClientNetworking {

    public static boolean DISPLAYED_CHAT_MESSAGE = false;
    public static boolean DISPLAYED_CHAT_WARNING = false;
    public static boolean DISPLAYED_HEAD_AIM_WARNING = false;
    public static boolean DISPLAYED_VR_CHANGES = false;
    public static boolean ABLE_TO_DISPLAY_CHAT_WARNINGS = false;
    public static boolean SHOW_NO_TELEPORT_MESSAGE = false;

    public static int CHAT_WARNING_TIMER = -1;
    public static boolean TELEPORT_WARNING = false;
    public static boolean VR_SWITCHING_WARNING = false;
    public static boolean HEAD_AIM_WARNING = false;
    public static boolean REQUESTED_DAMAGE_DIRECTION = false;

    public static boolean SERVER_HAS_VIVECRAFT = false;

    public static boolean SERVER_WANTS_DATA = false;
    public static boolean SERVER_SUPPORTS_DIRECT_TELEPORT = false;
    public static boolean SERVER_ALLOWS_DIRECT_TELEPORT = true;
    public static boolean SERVER_ALLOWS_CLIMBEY = false;
    public static boolean SERVER_ALLOWS_CRAWLING = false;
    public static boolean SERVER_ALLOWS_VR_SWITCHING = false;
    public static boolean SERVER_ALLOWS_DUAL_WIELDING = false;
    public static boolean SERVER_ALLOWS_ATTACKING_WHILE_BLOCKING = false;

    public static Map<String, String> SERVER_VR_CHANGES_LIST;

    // assume a legacy server by default, to not send invalid packets
    public static NetworkVersion USED_NETWORK_VERSION = NetworkVersion.LEGACY;
    private static float WORLDSCALE_LAST = 0.0F;
    private static float HEIGHT_LAST = 0.0F;

    // these overrides are for vanilla servers
    public static Vector3fc AIM_DIR_OVERRIDE = null;
    public static Vec3 AIM_POS_OVERRIDE = null;

    private static VRBodyPart LAST_SENT_BODY_PART = VRBodyPart.MAIN_HAND;
    public static VRBodyPart BODY_PART_CLIENT_OVERRIDE = null;
    public static boolean IS_LAST_BODY_PART_AIM = false;

    public static boolean NEEDS_RESET = true;

    /**
     * server settings that should be reset on each dimension change/respawn
     */
    public static void resetServerSettings() {
        WORLDSCALE_LAST = 0.0F;
        HEIGHT_LAST = 0.0F;
        SERVER_HAS_VIVECRAFT = false;
        SERVER_WANTS_DATA = false;
        SERVER_SUPPORTS_DIRECT_TELEPORT = false;
        SERVER_ALLOWS_DIRECT_TELEPORT = true;
        SERVER_ALLOWS_CLIMBEY = false;
        SERVER_ALLOWS_CRAWLING = false;
        SERVER_ALLOWS_VR_SWITCHING = false;
        SERVER_ALLOWS_DUAL_WIELDING = false;
        SERVER_ALLOWS_ATTACKING_WHILE_BLOCKING = false;
        USED_NETWORK_VERSION = NetworkVersion.LEGACY;
        LAST_SENT_BODY_PART = VRBodyPart.MAIN_HAND;
        BODY_PART_CLIENT_OVERRIDE = null;
        IS_LAST_BODY_PART_AIM = false;

        // clear VR player data
        ClientVRPlayers.clear();
        // clear teleport
        VRServerPerms.INSTANCE.setTeleportSupported(false);
        if (VRState.VR_INITIALIZED) {
            ClientDataHolderVR.getInstance().vrPlayer.setTeleportOverride(false);
        }
        // clear server overrides
        ClientDataHolderVR.getInstance().vrSettings.overrides.resetAll();
    }

    /**
     * server settings that should be reset only when connecting to a new server
     */
    public static void resetOnceServerSettings() {
        DISPLAYED_CHAT_MESSAGE = false;
        DISPLAYED_CHAT_WARNING = false;
        DISPLAYED_HEAD_AIM_WARNING = false;
        DISPLAYED_VR_CHANGES = false;
        SERVER_VR_CHANGES_LIST = null;
        SHOW_NO_TELEPORT_MESSAGE = false;
    }

    public static void sendVersionInfo() {
        // send version string, with currently running
        if (!ClientDataHolderVR.getInstance().completelyDisabled &&
            Xplat.INSTANCE.serverAcceptsPacket(Minecraft.getInstance().getConnection(), CommonNetworkHelper.CHANNEL))
        {
            Minecraft.getInstance().getConnection().send(createServerPacket(
                new VersionPayloadC2S(
                    CommonDataHolder.getInstance().versionIdentifier,
                    VRState.VR_RUNNING,
                    CommonNetworkHelper.MAX_SUPPORTED_NETWORK_PROTOCOL,
                    CommonNetworkHelper.MIN_SUPPORTED_NETWORK_PROTOCOL)));
        }
    }

    public static void sendVRPlayerPositions(VRPlayer vrPlayer) {
        if (Minecraft.getInstance().getConnection() == null ||
            Minecraft.getInstance().getCameraEntity() != Minecraft.getInstance().player)
        {
            return;
        }
        if (ReplayHelper.isLoaded()) {
            // replay mod / flashback compat, on servers without the plugin
            ReplayHelper.storePlayerData(vrPlayer);
        }
        if (!SERVER_WANTS_DATA) {
            return;
        }

        float worldScale = ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_post.worldScale;

        if (worldScale != WORLDSCALE_LAST) {
            sendServerPacket(new WorldScalePayloadC2S(worldScale));

            WORLDSCALE_LAST = worldScale;
        }

        float userHeight = AutoCalibration.getPlayerHeight();

        if (userHeight != HEIGHT_LAST) {
            sendServerPacket(new HeightPayloadC2S(userHeight / AutoCalibration.DEFAULT_HEIGHT));

            HEIGHT_LAST = userHeight;
        }

        var vrPlayerState = VrPlayerState.create(vrPlayer);

        if (USED_NETWORK_VERSION != NetworkVersion.LEGACY) {
            sendServerPacket(new VRPlayerStatePayloadC2S(vrPlayerState));
        } else {
            sendLegacyPackets(vrPlayerState);
        }
        if (ClientDataHolderVR.getInstance().vrSettings.mainPlayerDataSource != VRSettings.DataSource.SERVER) {
            ClientVRPlayers.getInstance()
                .update(Minecraft.getInstance().player.getGameProfile().id(), vrPlayerState, worldScale,
                    userHeight / AutoCalibration.DEFAULT_HEIGHT, true);
        }
    }

    /**
     * Sends the given {@code payload} to the server, but only if the server sent that it has vivecraft
     *
     * @param payload Payload to send
     */
    public static void sendServerPacket(VivecraftPayloadC2S payload) {
        if (Minecraft.getInstance().getConnection() != null && SERVER_HAS_VIVECRAFT) {
            Minecraft.getInstance().getConnection().send(createServerPacket(payload));
        }
    }

    public static Packet<?> createServerPacket(VivecraftPayloadC2S payload) {
        return Xplat.INSTANCE.getC2SPacket(payload);
    }

    public static void sendLegacyPackets(VrPlayerState vrPlayerState) {
        // main controller packet
        sendServerPacket(new LegacyController0DataPayloadC2S(ClientDataHolderVR.getInstance().vrSettings.reverseHands,
            vrPlayerState.mainHand()));

        // offhand controller packet
        sendServerPacket(new LegacyController1DataPayloadC2S(ClientDataHolderVR.getInstance().vrSettings.reverseHands,
            vrPlayerState.offHand()));

        // hmd packet
        sendServerPacket(
            new LegacyHeadDataPayloadC2S(ClientDataHolderVR.getInstance().vrSettings.seated, vrPlayerState.hmd()));
    }

    // ServerSetting override checks

    public static boolean isThirdPersonItems() {
        return ClientDataHolderVR.getInstance().vrSettings.overrides.getSetting(
            VRSettings.VrOptions.THIRDPERSON_ITEMTRANSFORMS).getBoolean();
    }

    public static boolean isThirdPersonItemsCustom() {
        return ClientDataHolderVR.getInstance().vrSettings.overrides.getSetting(
            VRSettings.VrOptions.THIRDPERSON_ITEMTRANSFORMS_CUSTOM).getBoolean();
    }

    public static boolean isLimitedSurvivalTeleport() {
        return ClientDataHolderVR.getInstance().vrSettings.overrides.getSetting(VRSettings.VrOptions.LIMIT_TELEPORT)
            .getBoolean();
    }

    public static boolean supportsReversedBow() {
        // old plugins hardcode the hand order
        return NetworkVersion.DUAL_WIELDING.accepts(USED_NETWORK_VERSION) || !SERVER_HAS_VIVECRAFT;
    }

    public static int getTeleportUpLimit() {
        return ClientDataHolderVR.getInstance().vrSettings.overrides.getSetting(VRSettings.VrOptions.TELEPORT_UP_LIMIT)
            .getInt();
    }

    public static int getTeleportDownLimit() {
        return ClientDataHolderVR.getInstance().vrSettings.overrides.getSetting(
            VRSettings.VrOptions.TELEPORT_DOWN_LIMIT).getInt();
    }

    public static int getTeleportHorizLimit() {
        return ClientDataHolderVR.getInstance().vrSettings.overrides.getSetting(
            VRSettings.VrOptions.TELEPORT_HORIZ_LIMIT).getInt();
    }

    /**
     * resets the aim override
     */
    public static void resetAim(int ticks) {
        if (!SERVER_WANTS_DATA) {
            restoreLook();
        } else if (NetworkVersion.AIM_OVERRIDE.accepts(USED_NETWORK_VERSION)) {
            sendServerPacket(new AimOverrideResetPayloadC2S(ticks));
            if (ticks == 0) {
                AIM_DIR_OVERRIDE = null;
                AIM_POS_OVERRIDE = null;
            }
        }
    }

    /**
     * overrides the aim of the player to the given direction. Position is still based on active bodypart
     *
     * @param dirOverride direction to override the aim to
     */
    public static void overrideAimDir(Vector3fc dirOverride) {
        if (!SERVER_WANTS_DATA) {
            overrideLook(Minecraft.getInstance().player, () -> dirOverride);
        } else if (NetworkVersion.AIM_OVERRIDE.accepts(USED_NETWORK_VERSION)) {
            sendServerPacket(new AimDirOverridePayloadC2S(dirOverride));
            AIM_DIR_OVERRIDE = dirOverride;
        }
    }

    /**
     * @return the current aim direction
     */
    public static Vector3fc getActiveAimDir() {
        if (AIM_DIR_OVERRIDE != null) {
            return AIM_DIR_OVERRIDE;
        } else {
            VRBodyPart bp = IS_LAST_BODY_PART_AIM ? getActiveBodyPart() :
                ClientDataHolderVR.getInstance().vrSettings.aimDevice == VRSettings.AimDevice.HMD ? VRBodyPart.HEAD :
                VRBodyPart.MAIN_HAND;
            return ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getBodyPart(bp).getDirection();
        }
    }

    /**
     * overrides the aim position of the player to the given position.
     *
     * @param posOverride position to override the aim to
     */
    public static void overrideAimPos(Vec3 posOverride) {
        // this is only for modded servers, can't handle this on vanilla ones
        if (SERVER_WANTS_DATA && NetworkVersion.AIM_OVERRIDE.accepts(USED_NETWORK_VERSION)) {
            sendServerPacket(new AimPosOverridePayloadC2S(
                MathUtils.subtractToVector3f(posOverride, Minecraft.getInstance().player.position())));
            AIM_POS_OVERRIDE = posOverride;
        }
    }

    /**
     * @return the current aim position, {@code null} when not on a server with vivecraft
     */
    @Nullable
    public static Vec3 getActiveAimPos() {
        if (!SERVER_WANTS_DATA) {
            // no overrides on servers without the plugin
            return null;
        } else if (AIM_POS_OVERRIDE != null) {
            return AIM_POS_OVERRIDE;
        } else {
            VRBodyPart bp = IS_LAST_BODY_PART_AIM ? getActiveBodyPart() :
                ClientDataHolderVR.getInstance().vrSettings.aimDevice == VRSettings.AimDevice.HMD ? VRBodyPart.HEAD :
                VRBodyPart.MAIN_HAND;
            return ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getBodyPart(bp).getPosition();
        }
    }

    /**
     * resets the active hand to the main hand
     */
    public static void resetActiveBodyPart() {
        sendActiveBodyPart(VRBodyPart.MAIN_HAND, false);
    }

    /**
     * sets the active BodyPart to the given {@code hand}, accounts for head aim when the hand is not used as aim
     *
     * @param hand      Hand to set active
     * @param useForAim if this hand should be used to aim
     */
    public static void sendActiveHand(InteractionHand hand, boolean useForAim) {
        if (!useForAim && ClientDataHolderVR.getInstance().vrSettings.aimDevice == VRSettings.AimDevice.HMD) {
            sendActiveBodyPart(VRBodyPart.HEAD, true);
        } else {
            sendActiveBodyPart(VRBodyPart.fromInteractionHand(hand), useForAim);
        }
    }

    /**
     * sets the active BodyPart to the given {@code bodyPart}
     *
     * @param bodyPart  BodyPart to set active
     * @param useForAim if this bodyPart should be used to aim
     */
    public static void sendActiveBodyPart(VRBodyPart bodyPart, boolean useForAim) {
        if (SERVER_WANTS_DATA) {
            if ((!NetworkVersion.HEAD_AIM.accepts(USED_NETWORK_VERSION) && bodyPart == VRBodyPart.HEAD) ||
                (!NetworkVersion.DUAL_WIELDING.accepts(USED_NETWORK_VERSION) &&
                    !bodyPart.availableInMode(FBTMode.ARMS_ONLY)
                ))
            {
                // old plugins only support main and offhand
                bodyPart = VRBodyPart.MAIN_HAND;
            }
            // only send if the hand is different from last time, don't need to spam packets
            if (bodyPart != LAST_SENT_BODY_PART) {
                sendServerPacket(new ActiveBodyPartPayloadC2S(bodyPart, useForAim));
            }
        }
        LAST_SENT_BODY_PART = bodyPart;
        IS_LAST_BODY_PART_AIM = useForAim;
    }

    /**
     * @return the active VRBodyPart, accounting for a client only override
     */
    public static VRBodyPart getActiveBodyPart() {
        return BODY_PART_CLIENT_OVERRIDE != null ? BODY_PART_CLIENT_OVERRIDE : LAST_SENT_BODY_PART;
    }

    public static void overridePose(LocalPlayer player) {
        if (ClientDataHolderVR.getInstance().crawlTracker.crawling) {
            player.setPose(Pose.SWIMMING);
        }
    }

    public static void overrideLook(Player player, Supplier<Vector3fc> viewSupplier) {
        if (SERVER_WANTS_DATA) return; // shouldn't be needed, don't tease the anti-cheat.

        Vector3fc view = viewSupplier.get();
        float pitch = (float) Math.toDegrees(Math.asin(-view.y() / view.length()));
        float yaw = (float) Math.toDegrees(Math.atan2(-view.x(), view.z()));
        ((LocalPlayer) player).connection.send(
            new ServerboundMovePlayerPacket.Rot(yaw, pitch, player.onGround(),
                player.horizontalCollision));
        AIM_DIR_OVERRIDE = view;
    }

    public static void restoreLook() {
        AIM_DIR_OVERRIDE = null;
        AIM_POS_OVERRIDE = null;
    }

    public static void handlePacket(VivecraftPayloadS2C s2cPayload) {
        if (s2cPayload instanceof UnknownPayloadS2C) return;
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        switch (s2cPayload.payloadId()) {
            case VERSION -> {
                SERVER_HAS_VIVECRAFT = true;
                VRServerPerms.INSTANCE.setTeleportSupported(true);
                TELEPORT_WARNING = false;
                VR_SWITCHING_WARNING = true;
                HEAD_AIM_WARNING = true;

                if (!DISPLAYED_CHAT_MESSAGE && dataholder.vrSettings.showServerPluginMessage.getAsBoolean()) {
                    DISPLAYED_CHAT_MESSAGE = true;
                    ClientUtils.addChatMessage(Component.translatable("vivecraft.messages.serverplugin",
                        ((VersionPayloadS2C) s2cPayload).version()));
                }
                if (VRState.VR_INITIALIZED && dataholder.vrSettings.manualCalibration == -1.0F &&
                    !dataholder.vrSettings.seated)
                {
                    ClientUtils.addChatMessage(Component.translatable("vivecraft.messages.calibrateheight"));
                }
            }
            case IS_VR_ACTIVE -> {
                VRActivePayloadS2C packet = (VRActivePayloadS2C) s2cPayload;
                if (!packet.vr()) {
                    ClientVRPlayers.getInstance().disableVR(packet.playerID());
                }
            }
            case REQUESTDATA -> SERVER_WANTS_DATA = true;
            case CLIMBING -> {
                ClimbingPayloadS2C packet = (ClimbingPayloadS2C) s2cPayload;
                SERVER_ALLOWS_CLIMBEY = packet.allowed();
                dataholder.climbTracker.serverBlockmode = packet.blockmode();
                dataholder.climbTracker.blocklist.clear();

                if (packet.blocks() != null) {
                    for (String blockId : packet.blocks()) {
                        BuiltInRegistries.BLOCK.get(Identifier.parse(blockId)).ifPresent(block -> {
                            if (block.value() != Blocks.AIR) {
                                dataholder.climbTracker.blocklist.add(block.value());
                            }
                        });
                    }
                }
            }
            case TELEPORT -> {
                SERVER_SUPPORTS_DIRECT_TELEPORT = true;
                SERVER_ALLOWS_DIRECT_TELEPORT = ((TeleportPayloadS2C) s2cPayload).allowed();
                if (!SERVER_ALLOWS_DIRECT_TELEPORT && VRState.VR_INITIALIZED) {
                    dataholder.vrPlayer.setTeleportOverride(false);
                    SHOW_NO_TELEPORT_MESSAGE = true;
                }
            }
            case UBERPACKET -> {
                UberPacketPayloadS2C packet = (UberPacketPayloadS2C) s2cPayload;
                ClientVRPlayers.getInstance()
                    .update(packet.playerID(), packet.state(), packet.worldScale(), packet.heightScale());
            }
            case SETTING_OVERRIDE -> {
                SettingOverridePayloadS2C overridePayload = (SettingOverridePayloadS2C) s2cPayload;
                for (Map.Entry<String, String> override : overridePayload.overrides().entrySet()) {
                    String[] split = override.getKey().split("\\.", 2);

                    if (dataholder.vrSettings.overrides.hasSetting(split[0])) {
                        VRSettings.ServerOverrides.Setting setting = dataholder.vrSettings.overrides.getSetting(
                            split[0]);
                        if (overridePayload.clear()) {
                            setting.resetValue();
                            if (setting.isFloat()) {
                                setting.resetValueMin();
                                setting.resetValueMax();
                            }
                            VRSettings.LOGGER.info("Vivecraft: Server setting override cleared: {}", override.getKey());
                            continue;
                        }

                        try {
                            if (split.length > 1) {
                                switch (split[1]) {
                                    case "min" -> setting.setValueMin(Float.parseFloat(override.getValue()));
                                    case "max" -> setting.setValueMax(Float.parseFloat(override.getValue()));
                                }
                            } else {
                                Object origValue = setting.getOriginalValue();

                                if (origValue instanceof Boolean) {
                                    setting.setValue(override.getValue().equals("true"));
                                } else if (origValue instanceof Integer || origValue instanceof Byte ||
                                    origValue instanceof Short)
                                {
                                    setting.setValue(Integer.parseInt(override.getValue()));
                                } else if (origValue instanceof Float || origValue instanceof Double) {
                                    setting.setValue(Float.parseFloat(override.getValue()));
                                } else {
                                    setting.setValue(override.getValue());
                                }
                            }

                            VRSettings.LOGGER.info("Vivecraft: Server setting override: {}={}", override.getKey(),
                                override.getValue());
                        } catch (Exception exception) {
                            VRSettings.LOGGER.error("Vivecraft: error parsing server setting override: ", exception);
                        }
                    }
                }
                if (Minecraft.getInstance().screen != null) {
                    // reinit screen, since overrides affect some option availability
                    Minecraft.getInstance().screen.init(
                        Minecraft.getInstance().getWindow().getGuiScaledWidth(),
                        Minecraft.getInstance().getWindow().getGuiScaledHeight());
                }
            }
            case CRAWL -> SERVER_ALLOWS_CRAWLING = ((CrawlPayloadS2C) s2cPayload).allowed();
            case NETWORK_VERSION -> {
                USED_NETWORK_VERSION = ((NetworkVersionPayloadS2C) s2cPayload).version();

                if (NetworkVersion.HEAD_AIM.accepts(USED_NETWORK_VERSION)) {
                    HEAD_AIM_WARNING = false;
                }
            }
            case VR_SWITCHING -> {
                SERVER_ALLOWS_VR_SWITCHING = ((VRSwitchingPayloadS2C) s2cPayload).allowed();
                if (!SERVER_ALLOWS_VR_SWITCHING) {
                    ClientUtils.addChatMessage(Component.translatable("vivecraft.messages.novrhotswitching"));
                }
                VR_SWITCHING_WARNING = false;
            }
            case DUAL_WIELDING -> SERVER_ALLOWS_DUAL_WIELDING = ((DualWieldingPayloadS2C) s2cPayload).allowed();
            case SERVER_VR_CHANGES -> SERVER_VR_CHANGES_LIST = ((ServerVrChangesS2CPacket) s2cPayload).changes();
            case HAPTIC -> {
                if (VRState.VR_RUNNING) {
                    HapticPayloadS2C haptic = ((HapticPayloadS2C) s2cPayload);
                    ClientDataHolderVR.getInstance().vr.triggerHapticPulse(
                        haptic.bodyPart(),
                        haptic.duration(),
                        haptic.frequency(),
                        haptic.amplitude(),
                        haptic.delay()
                    );
                }
            }
            case DAMAGE_DIRECTION ->
                dataholder.hapticTracker.setLastHitDirection(((DamageDirectionPayloadS2C) s2cPayload).damageDir());
            case ATTACK_WHILE_BLOCKING ->
                SERVER_ALLOWS_ATTACKING_WHILE_BLOCKING = ((AttackWhileBlockingPayloadS2C) s2cPayload).allowed();
        }
    }
}
