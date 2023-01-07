package org.vivecraft.titleworlds.mixin.cancel_save;

import net.minecraft.server.level.ChunkMap;
import org.vivecraft.titleworlds.TitleWorldsMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {

    /**
     * Prevent the server waiting for updates to finish
     */
    @Inject(method = "hasWork", at = @At("HEAD"), cancellable = true)
    void skipWork(CallbackInfoReturnable<Boolean> cir) {
        if (TitleWorldsMod.state.isTitleWorld && TitleWorldsMod.state.noSave) {
            cir.setReturnValue(false);
        }
    }
}
