package org.vivecraft.mod_compat_vr.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_xr.render_pass.RenderPassType;

//TODO Move rendering to here
@Pseudo
@Mixin(targets = {
    "net.irisshaders.iris.pathways.HandRenderer"
})
public class IrisHandRendererVRMixin {

    @Inject(at = @At("HEAD"), method = "setupGlState", cancellable = true, remap = false)
    public void vivecraft$glState(CallbackInfoReturnable<PoseStack> cir) {
        if (!RenderPassType.isVanilla()) {
            cir.setReturnValue(new PoseStack());
        }
    }

    @Inject(at = @At("HEAD"), method = "renderSolid", cancellable = true, remap = false)
    public void vivecraft$rendersolid(CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "renderTranslucent", cancellable = true, remap = false)
    public void vivecraft$rendertranslucent(CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            ci.cancel();
        }
    }
}
