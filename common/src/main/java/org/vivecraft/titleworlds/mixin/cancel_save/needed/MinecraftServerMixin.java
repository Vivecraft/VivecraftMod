package org.vivecraft.titleworlds.mixin.cancel_save.needed;

import net.minecraft.server.MinecraftServer;
import org.vivecraft.titleworlds.TitleWorldsMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    /**
     * Prevent saving misc world data
     */
    @Inject(method = "saveAllChunks", at = @At("HEAD"), cancellable = true)
    void cancelSave(boolean bl, boolean bl2, boolean bl3, CallbackInfoReturnable<Boolean> cir) {
        if (TitleWorldsMod.state.isTitleWorld && TitleWorldsMod.state.noSave) {
            cir.setReturnValue(false);
        }
    }
}
