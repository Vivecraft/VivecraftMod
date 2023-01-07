package org.vivecraft.titleworlds.mixin.cancel_save;

import net.minecraft.server.level.ServerChunkCache;
import org.vivecraft.titleworlds.TitleWorldsMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheMixin {

    /**
     * Prevent save on close() to optimize title world close time
     */
    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    void cancelSave(boolean bl, CallbackInfo ci){
        if (TitleWorldsMod.state.isTitleWorld && TitleWorldsMod.state.noSave) {
            ci.cancel();
        }
    }
}
