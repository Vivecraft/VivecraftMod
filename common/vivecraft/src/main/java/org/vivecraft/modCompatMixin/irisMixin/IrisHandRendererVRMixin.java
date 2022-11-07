package org.vivecraft.modCompatMixin.irisMixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.pipeline.HandRenderer;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//TODO Move rendering to here
@Pseudo
@Mixin(HandRenderer.class)
public class IrisHandRendererVRMixin {

    @Inject(at = @At("HEAD"), method = "setupGlState", cancellable = true)
    public void glState(GameRenderer par1, Camera par2, PoseStack par3, float par4, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "renderSolid", cancellable = true)
    public void rendersolid(PoseStack par1, float par2, Camera par3, GameRenderer par4, WorldRenderingPipeline par5, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "renderTranslucent", cancellable = true)
    public void rendertranslucent(PoseStack par1, float par2, Camera par3, GameRenderer par4, WorldRenderingPipeline par5, CallbackInfo ci) {
        ci.cancel();
    }
}
