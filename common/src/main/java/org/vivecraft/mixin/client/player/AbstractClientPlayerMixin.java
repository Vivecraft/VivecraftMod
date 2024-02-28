package org.vivecraft.mixin.client.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.network.SupporterReceiver;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void vivecraft$addPatreonInfo(ClientLevel clientLevel, GameProfile gameProfile, CallbackInfo ci) {
        SupporterReceiver.addPlayerInfo(((AbstractClientPlayer) (Object) this));
    }
}
