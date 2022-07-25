package com.example.examplemod.mixin.client.multiplayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.provider.openvr_jna.control.VivecraftMovementInput;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerVRMixin {

    @Shadow @Final private Minecraft minecraft;

    @Redirect(at = @At(value = "NEW", target = "Lnet/minecraft/client/player/KeyboardInput;<init>(Lnet/minecraft/client/Options;)V"), method = "handleLogin")
    public KeyboardInput login(Options options) {
        return null;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;adjustPlayer(Lnet/minecraft/world/entity/player/Player;)V"), method = "handleLogin")
    public void readdInput(ClientboundLoginPacket clientboundLoginPacket, CallbackInfo ci) {
        this.minecraft.player.input = new VivecraftMovementInput(this.minecraft.options);
    }

    @Redirect(at = @At(value = "NEW", target = "Lnet/minecraft/client/player/KeyboardInput;<init>(Lnet/minecraft/client/Options;)V"), method = "handleRespawn")
    public KeyboardInput respawn(Options options) {
        return null;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setYRot(F)V"), method = "handleRespawn")
    public void readdInput2(ClientboundRespawnPacket clientboundRespawnPacket, CallbackInfo ci) {
        this.minecraft.player.input = new VivecraftMovementInput(this.minecraft.options);
    }
}
