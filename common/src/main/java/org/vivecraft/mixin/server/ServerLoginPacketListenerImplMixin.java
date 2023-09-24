package org.vivecraft.mixin.server;

import org.vivecraft.server.ServerUtil;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginPacketListenerImpl.class)
public class ServerLoginPacketListenerImplMixin {

    @Inject(at = @At("HEAD"), method = "placeNewPlayer")
    private void scheduleLoginMessages(ServerPlayer serverPlayer, CallbackInfo ci) {
        ServerUtil.scheduleWelcomeMessageOrKick(serverPlayer);
        ServerUtil.sendUpdateNotificationIfOP(serverPlayer);
    }

}
