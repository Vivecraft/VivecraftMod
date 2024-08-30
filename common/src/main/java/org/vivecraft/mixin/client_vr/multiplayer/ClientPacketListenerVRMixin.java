package org.vivecraft.mixin.client_vr.multiplayer;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.network.packets.VivecraftDataPacket;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerVRMixin extends ClientCommonPacketListenerImpl {

    @Unique
    private String vivecraft$lastMsg = null;

    protected ClientPacketListenerVRMixin(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraft, connection, commonListenerCookie);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void vivecraft$init(CallbackInfo ci) {
        if (ClientNetworking.needsReset) {
            ClientNetworking.resetServerSettings();
            ClientNetworking.displayedChatMessage = false;
            ClientNetworking.displayedChatWarning = false;
            ClientNetworking.needsReset = false;
        }
    }

    @Inject(method = "handleLogin", at = @At("TAIL"))
    private void vivecraft$resetOnLogin(CallbackInfo ci) {
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

    @Inject(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setLevel(Lnet/minecraft/client/multiplayer/ClientLevel;)V", shift = At.Shift.AFTER))
    private void vivecraft$resetOnDimensionChange(CallbackInfo ci) {
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

    @Inject(method = "close", at = @At("TAIL"))
    private void vivecraft$cleanup(CallbackInfo ci) {
        ClientNetworking.resetServerSettings();
        ClientNetworking.displayedChatMessage = false;
        ClientNetworking.displayedChatWarning = false;
        ClientNetworking.needsReset = true;
    }

    @Inject(method = "sendChat", at = @At("TAIL"))
    private void vivecraft$storeChatMsg(String message, CallbackInfo ci) {
        this.vivecraft$lastMsg = message;
    }

    @Inject(method = "sendCommand", at = @At("TAIL"))
    private void vivecraft$storeCommandMsg(String command, CallbackInfo ci) {
        this.vivecraft$lastMsg = command;
    }

    @Inject(method = "handlePlayerChat", at = @At("TAIL"))
    private void vivecraft$chatHapticsPlayer(ClientboundPlayerChatPacket packet, CallbackInfo ci) {
        if (VRState.vrRunning && (minecraft.player == null || this.vivecraft$lastMsg == null || packet.sender() == minecraft.player.getUUID())) {
            vivecraft$triggerHapticSound();
        }
        this.vivecraft$lastMsg = null;
    }

    @Inject(method = "handleSystemChat", at = @At("TAIL"))
    private void vivecraft$chatHapticsSystem(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        if (VRState.vrRunning && (minecraft.player == null || this.vivecraft$lastMsg == null ||
            packet.content().getString().contains(this.vivecraft$lastMsg)
        ))
        {
            vivecraft$triggerHapticSound();
        }
        this.vivecraft$lastMsg = null;
    }

    @Unique
    private void vivecraft$triggerHapticSound() {
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
        if (dataHolder.vrSettings.chatNotifications != VRSettings.ChatNotifications.NONE) {
            if (!dataHolder.vrSettings.seated &&
                (dataHolder.vrSettings.chatNotifications == VRSettings.ChatNotifications.HAPTIC ||
                    dataHolder.vrSettings.chatNotifications == VRSettings.ChatNotifications.BOTH
                ))
            {
                dataHolder.vr.triggerHapticPulse(ControllerType.LEFT, 0.2F, 1000.0F, 1.0F);
            }

            if (dataHolder.vrSettings.chatNotifications == VRSettings.ChatNotifications.SOUND ||
                dataHolder.vrSettings.chatNotifications == VRSettings.ChatNotifications.BOTH)
            {
                Vec3 controllerPos = dataHolder.vrPlayer.vrdata_world_pre.getController(1).getPosition();
                minecraft.level.playLocalSound(
                    controllerPos.x(), controllerPos.y(), controllerPos.z(),
                    BuiltInRegistries.SOUND_EVENT.get(new ResourceLocation(dataHolder.vrSettings.chatNotificationSound)),
                    SoundSource.NEUTRAL, 0.3F, 0.1F, false);
            }
        }
    }

    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void vivecraft$handlepacket(CustomPacketPayload customPacketPayload, CallbackInfo info) {
        if (customPacketPayload instanceof VivecraftDataPacket dataPacket) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer()).writeBytes(dataPacket.buffer());
            ClientNetworking.handlePacket(dataPacket.packetId(), buffer);
            buffer.release();
            info.cancel();
        }
    }

    /**
     * this is just needed so that neoforge doesn't crash.
     * packets are handled with their events.
     * {@link org.vivecraft.neoforge.event.ClientEvents#handleVivePacket}
     */
    @Surrogate
    private void vivecraft$handlepacket(ClientboundCustomPayloadPacket packet, CustomPacketPayload customPacketPayload, CallbackInfo info) {
    }

    @Inject(method = "handleOpenScreen", at = @At("HEAD"))
    private void vivecraft$markScreenActive(ClientboundOpenScreenPacket clientboundOpenScreenPacket, CallbackInfo ci) {
        GuiHandler.guiAppearOverBlockActive = true;
    }
}
