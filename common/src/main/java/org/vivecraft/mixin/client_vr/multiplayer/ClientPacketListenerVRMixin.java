package org.vivecraft.mixin.client_vr.multiplayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerVRMixin extends ClientCommonPacketListenerImpl {

    @Unique
    private String vivecraft$lastMsg = null;

    protected ClientPacketListenerVRMixin(
        Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie)
    {
        super(minecraft, connection, commonListenerCookie);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void vivecraft$init(CallbackInfo ci) {
        if (ClientNetworking.NEEDS_RESET) {
            ClientNetworking.resetServerSettings();
            ClientNetworking.resetOnceServerSettings();
            ClientNetworking.NEEDS_RESET = false;
        }
    }

    @Inject(method = "handleLogin", at = @At("TAIL"))
    private void vivecraft$resetOnLogin(CallbackInfo ci) {
        this.vivecraft$resetServerState();
    }

    @Inject(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setLevel(Lnet/minecraft/client/multiplayer/ClientLevel;)V", shift = At.Shift.AFTER))
    private void vivecraft$resetOnDimensionChange(CallbackInfo ci) {
        this.vivecraft$resetServerState();
    }

    @Unique
    private void vivecraft$resetServerState() {
        // clear old data
        ClientNetworking.resetServerSettings();

        // request server data
        ClientNetworking.sendVersionInfo();

        // set the timer, even if vr is currently not running
        ClientNetworking.CHAT_WARNING_TIMER = 200;
        ClientNetworking.ABLE_TO_DISPLAY_CHAT_WARNINGS = false;
        ClientNetworking.TELEPORT_WARNING = true;
        ClientNetworking.VR_SWITCHING_WARNING = false;
        ClientNetworking.HEAD_AIM_WARNING = false;
        ClientNetworking.REQUESTED_DAMAGE_DIRECTION = false;
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void vivecraft$cleanup(CallbackInfo ci) {
        ClientNetworking.resetServerSettings();
        ClientNetworking.resetOnceServerSettings();
        ClientNetworking.NEEDS_RESET = true;
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
        if (VRState.VR_RUNNING && (this.minecraft.player == null || this.vivecraft$lastMsg == null ||
            packet.sender() == this.minecraft.player.getUUID()
        ))
        {
            ClientUtils.triggerChatHapticSound();
        }
        this.vivecraft$lastMsg = null;
    }

    @Inject(method = "handleSystemChat", at = @At("TAIL"))
    private void vivecraft$chatHapticsSystem(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        if (VRState.VR_RUNNING && (this.minecraft.player == null || this.vivecraft$lastMsg == null ||
            packet.content().getString().contains(this.vivecraft$lastMsg)
        ))
        {
            ClientUtils.triggerChatHapticSound();
        }
        this.vivecraft$lastMsg = null;
    }

    @Inject(method = "handleOpenScreen", at = @At("HEAD"))
    private void vivecraft$markScreenActive(CallbackInfo ci) {
        GuiHandler.GUI_APPEAR_OVER_BLOCK_ACTIVE = true;
    }

    @Inject(at = @At("TAIL"), method = "handleExplosion")
    public void vivecraft$handleExplosion(ClientboundExplodePacket clientboundExplodePacket, CallbackInfo ci) {
        if (VRState.VR_INITIALIZED) {
            ClientDataHolderVR.getInstance().hapticTracker.handleExplode(clientboundExplodePacket.center());
        }
    }
}
