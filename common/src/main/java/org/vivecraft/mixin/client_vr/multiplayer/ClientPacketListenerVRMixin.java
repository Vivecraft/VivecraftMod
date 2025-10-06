package org.vivecraft.mixin.client_vr.multiplayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerVRMixin {

    @Final
    @Shadow
    private Minecraft minecraft;

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

    @Inject(method = "cleanup", at = @At("TAIL"))
    private void vivecraft$cleanup(CallbackInfo ci) {
        ClientNetworking.resetServerSettings();
        ClientNetworking.resetOnceServerSettings();
        ClientNetworking.NEEDS_RESET = true;
    }

    @Inject(method = "handleChat", at = @At("TAIL"))
    private void vivecraft$chatHapticsPlayer(ClientboundChatPacket packet, CallbackInfo ci) {
        String lastMsg = ((PlayerExtension) this.minecraft.player).vivecraft$getLastMsg();
        ((PlayerExtension) this.minecraft.player).vivecraft$setLastMsg(null);
        if (VRState.VR_RUNNING &&
            (this.minecraft.player == null || lastMsg == null || packet.getMessage().getString().contains(lastMsg)))
        {
            ClientUtils.triggerChatHapticSound();
        }
    }

    @Inject(method = "handleOpenScreen", at = @At("HEAD"))
    private void vivecraft$markScreenActive(CallbackInfo ci) {
        GuiHandler.GUI_APPEAR_OVER_BLOCK_ACTIVE = true;
    }

    @Inject(at = @At("TAIL"), method = "handleExplosion")
    public void vivecraft$handleExplosion(ClientboundExplodePacket clientboundExplodePacket, CallbackInfo ci) {
        if (VRState.VR_INITIALIZED) {
            ClientDataHolderVR.getInstance().hapticTracker.handleExplode(
                new Vec3(clientboundExplodePacket.getX(),
                    clientboundExplodePacket.getY(),
                    clientboundExplodePacket.getZ()));
        }
    }
}
