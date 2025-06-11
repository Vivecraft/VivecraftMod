package org.vivecraft.mixin.server.players;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.server.ServerUtil;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Inject(method = "placeNewPlayer", at = @At("HEAD"))
    private void vivecraft$scheduleLoginMessages(CallbackInfo ci, @Local(argsOnly = true) ServerPlayer serverPlayer) {
        ServerUtil.scheduleWelcomeMessageOrKick(serverPlayer);
        ServerUtil.sendUpdateNotificationIfOP(serverPlayer);
    }
}
