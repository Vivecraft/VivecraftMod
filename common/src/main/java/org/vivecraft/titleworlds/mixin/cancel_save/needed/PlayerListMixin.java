package org.vivecraft.titleworlds.mixin.cancel_save.needed;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.vivecraft.titleworlds.SnapshotCreateServer;
import org.vivecraft.titleworlds.TitleWorldsMod;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Shadow @Final private MinecraftServer server;

    /**
     * Prevent saving player data
     */
    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    void cancelSave(ServerPlayer serverPlayer, CallbackInfo ci) {
        if (TitleWorldsMod.state.isTitleWorld && TitleWorldsMod.state.noSave
                || this.server instanceof SnapshotCreateServer) {
            ci.cancel();
        }
    }
}
