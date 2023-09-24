package org.vivecraft.mixin.client.player;

import org.vivecraft.client.network.PatreonReceiver;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void addPatreonInfo(ClientLevel clientLevel, GameProfile gameProfile, CallbackInfo ci) {
        PatreonReceiver.addPlayerInfo(((AbstractClientPlayer) (Object) this));
    }
}
