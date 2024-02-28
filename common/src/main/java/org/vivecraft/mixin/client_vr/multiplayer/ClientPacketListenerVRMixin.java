package org.vivecraft.mixin.client_vr.multiplayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.ClientTelemetryManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Registry;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.network.CommonNetworkHelper;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerVRMixin {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void vivecraft$init(Minecraft minecraft, Screen screen, Connection connection, GameProfile gameProfile, ClientTelemetryManager clientTelemetryManager, CallbackInfo ci) {
        if (ClientNetworking.needsReset) {
            ClientNetworking.resetServerSettings();
            ClientNetworking.displayedChatMessage = false;
            ClientNetworking.displayedChatWarning = false;
            ClientNetworking.needsReset = false;
        }
    }

    @Inject(at = @At("TAIL"), method = "handleLogin")
    public void vivecraft$resetOnLogin(ClientboundLoginPacket p_105030_, CallbackInfo callback) {
        // clear old data
        ClientNetworking.resetServerSettings();

        // request server data
        ClientNetworking.sendVersionInfo();

        if (VRState.vrInitialized) {
            // set the timer, even if vr is currently not running
            ClientDataHolderVR.getInstance().vrPlayer.chatWarningTimer = 200;
            ClientDataHolderVR.getInstance().vrPlayer.teleportWarning = true;
            ClientDataHolderVR.getInstance().vrPlayer.vrSwitchWarning = false;
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setLevel(Lnet/minecraft/client/multiplayer/ClientLevel;)V", shift = At.Shift.AFTER), method = "handleRespawn")
    public void vivecraft$resetOnDimensionChange(ClientboundRespawnPacket clientboundRespawnPacket, CallbackInfo ci) {
        // clear old data
        ClientNetworking.resetServerSettings();

        // request server data
        ClientNetworking.sendVersionInfo();

        if (VRState.vrInitialized) {
            // set the timer, even if vr is currently not running
            ClientDataHolderVR.getInstance().vrPlayer.chatWarningTimer = 200;
            ClientDataHolderVR.getInstance().vrPlayer.teleportWarning = true;
            ClientDataHolderVR.getInstance().vrPlayer.vrSwitchWarning = false;
        }
    }

    @Inject(at = @At("TAIL"), method = "cleanup")
    public void vivecraft$cleanup(CallbackInfo ci) {
        ClientNetworking.resetServerSettings();
        ClientNetworking.displayedChatMessage = false;
        ClientNetworking.displayedChatWarning = false;
        ClientNetworking.needsReset = true;
    }

    @Inject(at = @At("TAIL"), method = "handlePlayerChat")
    public void vivecraft$chat(ClientboundPlayerChatPacket clientboundPlayerChatPacket, CallbackInfo ci) {
        String lastMsg = ((PlayerExtension) minecraft.player).vivecraft$getLastMsg();
        ((PlayerExtension) minecraft.player).vivecraft$setLastMsg(null);
        if (VRState.vrRunning && (minecraft.player == null || lastMsg == null || clientboundPlayerChatPacket.message().signedHeader().sender() == minecraft.player.getUUID())) {
            vivecraft$triggerHapticSound();
        }
    }

    @Inject(at = @At("TAIL"), method = "handleSystemChat")
    public void vivecraft$chatSystem(ClientboundSystemChatPacket clientboundSystemChatPacket, CallbackInfo ci) {
        String lastMsg = ((PlayerExtension) minecraft.player).vivecraft$getLastMsg();
        ((PlayerExtension) minecraft.player).vivecraft$setLastMsg(null);
        if (VRState.vrRunning && (minecraft.player == null || lastMsg == null || clientboundSystemChatPacket.content().getString().contains(lastMsg))) {
            vivecraft$triggerHapticSound();
        }
    }

    @Unique
    private void vivecraft$triggerHapticSound() {
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        if (dataholder.vrSettings.chatNotifications != VRSettings.ChatNotifications.NONE) {
            if ((dataholder.vrSettings.chatNotifications == VRSettings.ChatNotifications.HAPTIC || dataholder.vrSettings.chatNotifications == VRSettings.ChatNotifications.BOTH) && !dataholder.vrSettings.seated) {
                dataholder.vr.triggerHapticPulse(ControllerType.LEFT, 0.2F, 1000.0F, 1.0F);
            }

            if (dataholder.vrSettings.chatNotifications == VRSettings.ChatNotifications.SOUND || dataholder.vrSettings.chatNotifications == VRSettings.ChatNotifications.BOTH) {
                Vec3 vec3 = dataholder.vrPlayer.vrdata_world_pre.getController(1).getPosition();
                minecraft.level.playLocalSound(vec3.x(), vec3.y(), vec3.z(), Registry.SOUND_EVENT.get(new ResourceLocation(dataholder.vrSettings.chatNotificationSound)), SoundSource.NEUTRAL, 0.3F, 0.1F, false);
            }
        }
    }

    @Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/network/protocol/game/ClientboundCustomPayloadPacket;getData()Lnet/minecraft/network/FriendlyByteBuf;"), method = "handleCustomPayload", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void vivecraft$handlepacket(ClientboundCustomPayloadPacket payloadPacket, CallbackInfo info, ResourceLocation channelID, FriendlyByteBuf buffer) {
        if (CommonNetworkHelper.CHANNEL.equals(channelID)) {
            ClientNetworking.handlePacket(
                CommonNetworkHelper.PacketDiscriminators.values()[buffer.readByte()],
                buffer);
            buffer.release();
            info.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "handleOpenScreen")
    public void vivecraft$markScreenActive(ClientboundOpenScreenPacket clientboundOpenScreenPacket, CallbackInfo ci) {
        GuiHandler.guiAppearOverBlockActive = true;
    }
}
