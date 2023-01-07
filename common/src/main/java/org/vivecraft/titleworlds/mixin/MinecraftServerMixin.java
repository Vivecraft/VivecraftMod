package org.vivecraft.titleworlds.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import org.vivecraft.titleworlds.TitleWorldsMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow
    protected abstract void updateMobSpawningFlags();

    /**
     * Cuts down load time by approx 7s at the cost of having chunks pop in on world load
     */
    @Inject(method = "prepareLevels", at = @At("HEAD"), cancellable = true)
    void skipLoadingChunksAroundPlayer(ChunkProgressListener chunkProgressListener, CallbackInfo ci) {
        if (TitleWorldsMod.state.isTitleWorld) {
            ci.cancel();
            this.updateMobSpawningFlags();
        }
    }
}
