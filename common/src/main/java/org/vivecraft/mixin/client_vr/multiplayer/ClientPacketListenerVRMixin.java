package org.vivecraft.mixin.client_vr.multiplayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.settings.VRSettings.ChatNotifications;
import org.vivecraft.common.VRServerPerms;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.CommonNetworkHelper.PacketDiscriminators;

import static org.vivecraft.client_vr.VRState.*;

@Mixin(net.minecraft.client.multiplayer.ClientPacketListener.class)
public class ClientPacketListenerVRMixin {

    @Inject(at = @At("TAIL"), method = "<init>")
    public void vivecraft$init(net.minecraft.client.Minecraft minecraft, Screen screen, Connection connection, ServerData serverData, GameProfile gameProfile, WorldSessionTelemetryManager worldSessionTelemetryManager, CallbackInfo ci) {
        if (ClientNetworking.needsReset) {
            dh.vrSettings.overrides.resetAll();
            ClientNetworking.resetServerSettings();
            ClientNetworking.displayedChatMessage = false;
            ClientNetworking.displayedChatWarning = false;
            ClientNetworking.needsReset = false;
        }
    }

    @Inject(at = @At("TAIL"), method = "handleLogin(Lnet/minecraft/network/protocol/game/ClientboundLoginPacket;)V")
    public void vivecraft$login(ClientboundLoginPacket p_105030_, CallbackInfo callback) {
        VRPlayersClient.clear();
        ClientNetworking.sendVersionInfo();

        if (vrInitialized) {
            // set the timer, even if vr is currently not running
            dh.vrPlayer.chatWarningTimer = 200;
            dh.vrPlayer.teleportWarning = true;
            dh.vrPlayer.vrSwitchWarning = false;
        }
    }

    @Inject(at = @At("TAIL"), method = "onDisconnect")
    public void vivecraft$disconnect(Component component, CallbackInfo ci) {
        VRServerPerms.setTeleportSupported(false);
        if (vrInitialized) {
            dh.vrPlayer.setTeleportOverride(false);
        }
        dh.vrSettings.overrides.resetAll();
    }

    @Inject(at = @At("TAIL"), method = "close")
    public void vivecraft$cleanup(CallbackInfo ci) {
        ClientNetworking.needsReset = true;
    }

    @Unique
    String vivecraft$lastMsg;

    @Inject(at = @At("TAIL"), method = "sendChat")
    public void vivecraft$chatMsg(String string, CallbackInfo ci) {
        this.vivecraft$lastMsg = string;
    }

    @Inject(at = @At("TAIL"), method = "sendCommand")
    public void vivecraft$commandMsg(String string, CallbackInfo ci) {
        this.vivecraft$lastMsg = string;
    }

    @Inject(at = @At("TAIL"), method = "handlePlayerChat")
    public void vivecraft$chat(ClientboundPlayerChatPacket clientboundPlayerChatPacket, CallbackInfo ci) {
        if (vrRunning && (mc.player == null || this.vivecraft$lastMsg == null || clientboundPlayerChatPacket.sender() == mc.player.getUUID())) {
            this.vivecraft$triggerHapticSound();
        }
        this.vivecraft$lastMsg = null;
    }

    @Inject(at = @At("TAIL"), method = "handleSystemChat")
    public void vivecraft$chatSystem(ClientboundSystemChatPacket clientboundSystemChatPacket, CallbackInfo ci) {
        if (vrRunning && (mc.player == null || this.vivecraft$lastMsg == null || clientboundSystemChatPacket.content().getString().contains(this.vivecraft$lastMsg))) {
            this.vivecraft$triggerHapticSound();
        }
        this.vivecraft$lastMsg = null;
    }

    @Unique
    private void vivecraft$triggerHapticSound() {
        if (dh.vrSettings.chatNotifications != ChatNotifications.NONE) {
            if ((dh.vrSettings.chatNotifications == ChatNotifications.HAPTIC || dh.vrSettings.chatNotifications == ChatNotifications.BOTH) && !dh.vrSettings.seated) {
                dh.vr.triggerHapticPulse(ControllerType.LEFT, 0.2F, 1000.0F, 1.0F);
            }

            if (dh.vrSettings.chatNotifications == ChatNotifications.SOUND || dh.vrSettings.chatNotifications == ChatNotifications.BOTH) {
                Vec3 vec3 = dh.vrPlayer.vrdata_world_pre.getController(1).getPosition();
                mc.level.playLocalSound(vec3.x(), vec3.y(), vec3.z(), BuiltInRegistries.SOUND_EVENT.get(new ResourceLocation(dh.vrSettings.chatNotificationSound)), SoundSource.NEUTRAL, 0.3F, 0.1F, false);
            }
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;adjustPlayer(Lnet/minecraft/world/entity/player/Player;)V", shift = Shift.BEFORE), method = "handleRespawn")
    public void vivecraft$readdInput2(ClientboundRespawnPacket clientboundRespawnPacket, CallbackInfo ci) {
        ClientNetworking.resetServerSettings();
        ClientNetworking.sendVersionInfo();
        if (vrInitialized) {
            // set the timer, even if vr is currently not running
            dh.vrPlayer.chatWarningTimer = 200;
            dh.vrPlayer.teleportWarning = true;
            dh.vrPlayer.vrSwitchWarning = false;
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", ordinal = 0, shift = Shift.AFTER), method = "handleRespawn(Lnet/minecraft/network/protocol/game/ClientboundRespawnPacket;)V")
    public void vivecraft$respawn(ClientboundRespawnPacket packet, CallbackInfo callback) {
        dh.vrSettings.overrides.resetAll();
    }

    @Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/network/protocol/game/ClientboundCustomPayloadPacket;getData()Lnet/minecraft/network/FriendlyByteBuf;"), method = "handleCustomPayload(Lnet/minecraft/network/protocol/game/ClientboundCustomPayloadPacket;)V", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void vivecraft$handlepacket(ClientboundCustomPayloadPacket packet, CallbackInfo info, ResourceLocation channelID, FriendlyByteBuf buffer) {
        if (channelID.equals(CommonNetworkHelper.CHANNEL)) {
            var packetID = PacketDiscriminators.values()[buffer.readByte()];
            ClientNetworking.handlePacket(packetID, buffer);
            buffer.release();
            info.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "handleOpenScreen")
    public void vivecraft$markScreenActive(ClientboundOpenScreenPacket clientboundOpenScreenPacket, CallbackInfo ci) {
        GuiHandler.guiAppearOverBlockActive = true;
    }
}
