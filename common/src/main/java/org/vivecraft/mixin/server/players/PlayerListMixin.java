package org.vivecraft.mixin.server.players;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.server.ServerUtil;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Inject(at = @At("HEAD"), method = "placeNewPlayer")
    private void vivecraft$scheduleLoginMessages(Connection connection, ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie, CallbackInfo ci) {
        ServerUtil.scheduleWelcomeMessageOrKick(serverPlayer);
        ServerUtil.sendUpdateNotificationIfOP(serverPlayer);
    }
}
