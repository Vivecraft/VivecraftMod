package org.vivecraft.mixin.client.multiplayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.*;
import org.spongepowered.asm.mixin.Unique;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.api.CommonNetworkHelper;
import org.vivecraft.extensions.PlayerExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.api.ClientNetworkHelper;
import org.vivecraft.provider.ControllerType;
import org.vivecraft.provider.openvr_jna.control.VivecraftMovementInput;
import org.vivecraft.render.PlayerModelController;
import org.vivecraft.settings.VRSettings;

import java.util.UUID;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerVRMixin {
    @Final
    @Shadow private Minecraft minecraft;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void init(Minecraft minecraft, Screen screen, Connection connection, ServerData serverData, GameProfile gameProfile, WorldSessionTelemetryManager worldSessionTelemetryManager, CallbackInfo ci) {
        ClientDataHolder.getInstance().vrSettings.overrides.resetAll();
        ClientNetworkHelper.resetServerSettings();
        ClientNetworkHelper.displayedChatMessage = false;
    }

    @Inject(at = @At("TAIL"), method = "handleLogin(Lnet/minecraft/network/protocol/game/ClientboundLoginPacket;)V")
    public void login(ClientboundLoginPacket p_105030_, CallbackInfo callback) {
        CommonNetworkHelper.vivePlayers.clear();
        ClientNetworkHelper.sendVersionInfo();
        ClientDataHolder.getInstance().vrPlayer.teleportWarningTimer = 200;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;adjustPlayer(Lnet/minecraft/world/entity/player/Player;)V"), method = "handleLogin")
    public void readdInput(ClientboundLoginPacket clientboundLoginPacket, CallbackInfo ci) {
        this.minecraft.player.input = new VivecraftMovementInput(this.minecraft.options);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/telemetry/WorldSessionTelemetryManager;onPlayerInfoReceived(Lnet/minecraft/world/level/GameType;Z)V"), method = "handleLogin(Lnet/minecraft/network/protocol/game/ClientboundLoginPacket;)V")
    public void noTelemetry(WorldSessionTelemetryManager instance, GameType gameType, boolean bl) {
        return;
    }

    @Inject(at = @At("TAIL"), method = "onDisconnect")
    public void disconnect(Component component, CallbackInfo ci) {
        ClientDataHolder.getInstance().vrPlayer.setTeleportSupported(false);
        ClientDataHolder.getInstance().vrPlayer.setTeleportOverride(false);
        ClientDataHolder.getInstance().vrSettings.overrides.resetAll();
    }

    @Unique String lastMsg = null;
    @Inject(at = @At("TAIL"), method = "sendChat")
    public void chatMsg(String string, CallbackInfo ci) {
        this.lastMsg = string;
    }

    @Inject(at = @At("TAIL"), method = "sendCommand")
    public void commandMsg(String string, CallbackInfo ci) {
        this.lastMsg = string;
    }

    @Inject(at = @At("TAIL"), method = "handlePlayerChat")
    public void chat(ClientboundPlayerChatPacket clientboundPlayerChatPacket, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || lastMsg == null || clientboundPlayerChatPacket.sender() == minecraft.player.getUUID()) {
            ClientDataHolder dataholder = ClientDataHolder.getInstance();
            if (dataholder.vrSettings.chatNotifications != VRSettings.ChatNotifications.NONE) {
                if ((dataholder.vrSettings.chatNotifications == VRSettings.ChatNotifications.HAPTIC || dataholder.vrSettings.chatNotifications == VRSettings.ChatNotifications.BOTH) && !dataholder.vrSettings.seated) {
                    dataholder.vr.triggerHapticPulse(ControllerType.LEFT, 0.2F, 1000.0F, 1.0F);}

                if (dataholder.vrSettings.chatNotifications == VRSettings.ChatNotifications.SOUND || dataholder.vrSettings.chatNotifications == VRSettings.ChatNotifications.BOTH) {
                    Vec3 vec3 = dataholder.vrPlayer.vrdata_world_pre.getController(1).getPosition();
                    minecraft.level.playLocalSound(vec3.x(), vec3.y(), vec3.z(), BuiltInRegistries.SOUND_EVENT.get(new ResourceLocation(dataholder.vrSettings.chatNotificationSound)), SoundSource.NEUTRAL, 0.3F, 0.1F, false);
                }
            }
        }
        lastMsg = null;
    }

    @Inject(at = @At("TAIL"), method = "handleSystemChat")
    public void chatSystem(ClientboundSystemChatPacket clientboundSystemChatPacket, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || lastMsg == null || clientboundSystemChatPacket.content().getString().contains(lastMsg)) {
            ClientDataHolder dataholder = ClientDataHolder.getInstance();
            if (dataholder.vrSettings.chatNotifications != VRSettings.ChatNotifications.NONE) {
                if ((dataholder.vrSettings.chatNotifications == VRSettings.ChatNotifications.HAPTIC || dataholder.vrSettings.chatNotifications == VRSettings.ChatNotifications.BOTH) && !dataholder.vrSettings.seated) {
                    dataholder.vr.triggerHapticPulse(ControllerType.LEFT, 0.2F, 1000.0F, 1.0F);}

                if (dataholder.vrSettings.chatNotifications == VRSettings.ChatNotifications.SOUND || dataholder.vrSettings.chatNotifications == VRSettings.ChatNotifications.BOTH) {
                    Vec3 vec3 = dataholder.vrPlayer.vrdata_world_pre.getController(1).getPosition();
                    minecraft.level.playLocalSound(vec3.x(), vec3.y(), vec3.z(), BuiltInRegistries.SOUND_EVENT.get(new ResourceLocation(dataholder.vrSettings.chatNotificationSound)), SoundSource.NEUTRAL, 0.3F, 0.1F, false);
                }
            }
        }
        lastMsg = null;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetPos()V"), method = "handleRespawn")
    public void sync(LocalPlayer instance) {
        instance.resetPos();
        // ((PlayerExtension)instance).updateSyncFields(this.minecraft.player);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;adjustPlayer(Lnet/minecraft/world/entity/player/Player;)V", shift = At.Shift.BEFORE), method = "handleRespawn")
    public void readdInput2(ClientboundRespawnPacket clientboundRespawnPacket, CallbackInfo ci) {
        ClientNetworkHelper.resetServerSettings();
        ClientNetworkHelper.sendVersionInfo();
        ClientDataHolder.getInstance().vrPlayer.teleportWarningTimer = 200;
        this.minecraft.player.input = new VivecraftMovementInput(this.minecraft.options);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", ordinal = 0, shift = At.Shift.AFTER), method = "handleRespawn(Lnet/minecraft/network/protocol/game/ClientboundRespawnPacket;)V")
    public void respawn(ClientboundRespawnPacket packet, CallbackInfo callback) {
        ClientDataHolder.getInstance().vrSettings.overrides.resetAll();
    }

    @Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/network/protocol/game/ClientboundCustomPayloadPacket;getData()Lnet/minecraft/network/FriendlyByteBuf;"), method = "handleCustomPayload(Lnet/minecraft/network/protocol/game/ClientboundCustomPayloadPacket;)V", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	public void handlepacket(ClientboundCustomPayloadPacket p_105004_, CallbackInfo info, ResourceLocation resourcelocation, FriendlyByteBuf friendlybytebuf) {
        ClientDataHolder dataholder = ClientDataHolder.getInstance();
        if (resourcelocation.getNamespace().equalsIgnoreCase("vivecraft")) {
			if (resourcelocation.getPath().equalsIgnoreCase("data")) {
				byte b0 = friendlybytebuf.readByte();
                CommonNetworkHelper.PacketDiscriminators networkhelper$packetdiscriminators = CommonNetworkHelper.PacketDiscriminators.values()[b0];

				switch (networkhelper$packetdiscriminators) {
				case VERSION:
					String s11 = friendlybytebuf.readUtf(1024);
                    dataholder.vrPlayer.setTeleportSupported(true);
                    dataholder.vrPlayer.teleportWarningTimer = -1;

					if (!ClientNetworkHelper.displayedChatMessage) {
						ClientNetworkHelper.displayedChatMessage = true;
						this.minecraft.gui.getChat().addMessage(Component.translatable("vivecraft.messages.serverplugin", s11));
					}
                    if (dataholder.vrSettings.manualCalibration == -1.0F && !dataholder.vrSettings.seated) {
                        this.minecraft.gui.getChat().addMessage(Component.translatable("vivecraft.messages.calibrateheight"));
                    }

					break;

				case REQUESTDATA:
					ClientNetworkHelper.serverWantsData = true;
					break;

				case CLIMBING:
                    ClientNetworkHelper.serverAllowsClimbey = friendlybytebuf.readBoolean();

                    if (friendlybytebuf.readableBytes() > 0) {
                        dataholder.climbTracker.serverblockmode = friendlybytebuf.readByte();
                        dataholder.climbTracker.blocklist.clear();

                        while (friendlybytebuf.readableBytes() > 0) {
                            String s12 = friendlybytebuf.readUtf(16384);
                            Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(s12));

                            if (block != null) {
                                dataholder.climbTracker.blocklist.add(block);
                            }
                        }
                    }

					break;

				case TELEPORT:
					ClientNetworkHelper.serverSupportsDirectTeleport = true;
					break;

				case UBERPACKET:
					Long olong = friendlybytebuf.readLong();
					Long olong1 = friendlybytebuf.readLong();
					byte[] abyte = new byte[29];
					byte[] abyte1 = new byte[29];
					byte[] abyte2 = new byte[29];
					friendlybytebuf.readBytes(29).getBytes(0, abyte);
					friendlybytebuf.readBytes(29).getBytes(0, abyte1);
					friendlybytebuf.readBytes(29).getBytes(0, abyte2);
					UUID uuid2 = new UUID(olong, olong1);
					float f3 = 1.0F;
					float f4 = 1.0F;

					if (friendlybytebuf.isReadable()) {
						f3 = friendlybytebuf.readFloat();
					}

					if (friendlybytebuf.isReadable()) {
						f4 = friendlybytebuf.readFloat();
					}

					PlayerModelController.getInstance().Update(uuid2, abyte, abyte1, abyte2, f3, f4);
					break;

				case SETTING_OVERRIDE:
                    while (friendlybytebuf.readableBytes() > 0) {
                        String s13 = friendlybytebuf.readUtf(16384);
                        String s14 = friendlybytebuf.readUtf(16384);
                        String[] astring = s13.split("\\.", 2);

                        if (dataholder.vrSettings.overrides.hasSetting(astring[0])) {
                            VRSettings.ServerOverrides.Setting vrsettings$serveroverrides$setting = dataholder.vrSettings.overrides.getSetting(astring[0]);

                            try {
                                if (astring.length > 1) {
                                    String s15 = astring[1];
                                    switch (s15) {
                                        case "min":
                                            vrsettings$serveroverrides$setting.setValueMin(Float.parseFloat(s14));
                                            break;

                                        case "max":
                                            vrsettings$serveroverrides$setting.setValueMax(Float.parseFloat(s14));
                                    }
                                }
                                else {
                                    Object object = vrsettings$serveroverrides$setting.getOriginalValue();

                                    if (object instanceof Boolean) {
                                        vrsettings$serveroverrides$setting.setValue(s14.equals("true"));
                                    }
                                    else if (!(object instanceof Integer) && !(object instanceof Byte) && !(object instanceof Short)) {
                                        if (!(object instanceof Float) && !(object instanceof Double)) {
                                            vrsettings$serveroverrides$setting.setValue(s14);
                                        }
                                        else {
                                            vrsettings$serveroverrides$setting.setValue(Float.parseFloat(s14));
                                        }
                                    }
                                    else {
                                        vrsettings$serveroverrides$setting.setValue(Integer.parseInt(s14));
                                    }
                                }

                                System.out.println("Server setting override: " + s13 + " = " + s14);
                            }
                            catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        }
                    }

					break;

				case CRAWL:
					ClientNetworkHelper.serverAllowsCrawling = true;
				}
			}
			if (friendlybytebuf != null) {
				friendlybytebuf.release();
			}
			info.cancel();
		}
	}
}
