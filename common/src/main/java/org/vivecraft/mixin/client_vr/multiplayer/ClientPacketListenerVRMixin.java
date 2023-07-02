package org.vivecraft.mixin.client_vr.multiplayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.*;
import org.spongepowered.asm.mixin.Unique;
import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.common.network.CommonNetworkHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.common.VRServerPerms;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.settings.VRSettings;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerVRMixin {
    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void init(Minecraft minecraft, Screen screen, Connection connection, ServerData serverData, GameProfile gameProfile, WorldSessionTelemetryManager worldSessionTelemetryManager, CallbackInfo ci) {
        if (ClientNetworking.needsReset) {
            ClientDataHolderVR.getInstance().vrSettings.overrides.resetAll();
            ClientNetworking.resetServerSettings();
            ClientNetworking.displayedChatMessage = false;
            ClientNetworking.needsReset = false;
        }
    }

    @Inject(at = @At("TAIL"), method = "handleLogin(Lnet/minecraft/network/protocol/game/ClientboundLoginPacket;)V")
    public void login(ClientboundLoginPacket p_105030_, CallbackInfo callback) {
        VRPlayersClient.clear();
        ClientNetworking.sendVersionInfo();

        if (VRState.vrInitialized) {
            // set the timer, even if vr is currently not running
            ClientDataHolderVR.getInstance().vrPlayer.teleportWarningTimer = 200;
            ClientDataHolderVR.getInstance().vrPlayer.vrSwitchWarningTimer = 200;
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/telemetry/WorldSessionTelemetryManager;onPlayerInfoReceived(Lnet/minecraft/world/level/GameType;Z)V"), method = "handleLogin(Lnet/minecraft/network/protocol/game/ClientboundLoginPacket;)V")
    public void noTelemetry(WorldSessionTelemetryManager instance, GameType gameType, boolean bl) {
        // TODO, should we still cancel that in NONVR?
        return;
    }

    @Inject(at = @At("TAIL"), method = "onDisconnect")
    public void disconnect(Component component, CallbackInfo ci) {
        VRServerPerms.INSTANCE.setTeleportSupported(false);
        if (VRState.vrInitialized) {
            ClientDataHolderVR.getInstance().vrPlayer.setTeleportOverride(false);
        }
        ClientDataHolderVR.getInstance().vrSettings.overrides.resetAll();
    }

    @Inject(at = @At("TAIL"), method = "close")
    public void cleanup(CallbackInfo ci) {
        ClientNetworking.needsReset = true;
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
        if (VRState.vrRunning && (minecraft.player == null || lastMsg == null || clientboundPlayerChatPacket.sender() == minecraft.player.getUUID())) {
            triggerHapticSound();
        }
        lastMsg = null;
    }

    @Inject(at = @At("TAIL"), method = "handleSystemChat")
    public void chatSystem(ClientboundSystemChatPacket clientboundSystemChatPacket, CallbackInfo ci) {
        if (VRState.vrRunning && (minecraft.player == null || lastMsg == null || clientboundSystemChatPacket.content().getString().contains(lastMsg))) {
            triggerHapticSound();
        }
        lastMsg = null;
    }

    @Unique
    private void triggerHapticSound(){
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        if (dataholder.vrSettings.chatNotifications != VRSettings.ChatNotifications.NONE) {
            if ((dataholder.vrSettings.chatNotifications == VRSettings.ChatNotifications.HAPTIC || dataholder.vrSettings.chatNotifications == VRSettings.ChatNotifications.BOTH) && !dataholder.vrSettings.seated) {
                dataholder.vr.triggerHapticPulse(ControllerType.LEFT, 0.2F, 1000.0F, 1.0F);}

            if (dataholder.vrSettings.chatNotifications == VRSettings.ChatNotifications.SOUND || dataholder.vrSettings.chatNotifications == VRSettings.ChatNotifications.BOTH) {
                Vec3 vec3 = dataholder.vrPlayer.vrdata_world_pre.getController(1).getPosition();
                minecraft.level.playLocalSound(vec3.x(), vec3.y(), vec3.z(), BuiltInRegistries.SOUND_EVENT.get(new ResourceLocation(dataholder.vrSettings.chatNotificationSound)), SoundSource.NEUTRAL, 0.3F, 0.1F, false);
            }
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;adjustPlayer(Lnet/minecraft/world/entity/player/Player;)V", shift = At.Shift.BEFORE), method = "handleRespawn")
    public void readdInput2(ClientboundRespawnPacket clientboundRespawnPacket, CallbackInfo ci) {
        ClientNetworking.resetServerSettings();
        ClientNetworking.sendVersionInfo();
        if (VRState.vrInitialized) {
            // set the timer, even if vr is currently not running
            ClientDataHolderVR.getInstance().vrPlayer.teleportWarningTimer = 200;
            ClientDataHolderVR.getInstance().vrPlayer.vrSwitchWarningTimer = 200;
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", ordinal = 0, shift = At.Shift.AFTER), method = "handleRespawn(Lnet/minecraft/network/protocol/game/ClientboundRespawnPacket;)V")
    public void respawn(ClientboundRespawnPacket packet, CallbackInfo callback) {
        ClientDataHolderVR.getInstance().vrSettings.overrides.resetAll();
    }

    @Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/network/protocol/game/ClientboundCustomPayloadPacket;getData()Lnet/minecraft/network/FriendlyByteBuf;"), method = "handleCustomPayload(Lnet/minecraft/network/protocol/game/ClientboundCustomPayloadPacket;)V", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	public void handlepacket(ClientboundCustomPayloadPacket p_105004_, CallbackInfo info, ResourceLocation channelID, FriendlyByteBuf buffer) {
        if (channelID.equals(CommonNetworkHelper.CHANNEL)) {
            var packetID = CommonNetworkHelper.PacketDiscriminators.values()[buffer.readByte()];
            ClientNetworking.handlePacket(packetID, buffer);
            buffer.release();
            info.cancel();
        }
	}

    @Inject(at = @At("HEAD"), method = "handleOpenScreen")
    public void markScreenActive(ClientboundOpenScreenPacket clientboundOpenScreenPacket, CallbackInfo ci) {
        GuiHandler.guiAppearOverBlockActive = true;
    }
}
