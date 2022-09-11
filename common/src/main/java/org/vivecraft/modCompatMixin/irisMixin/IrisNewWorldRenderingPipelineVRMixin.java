package org.vivecraft.modCompatMixin.irisMixin;

import org.vivecraft.ClientDataHolder;
import net.coderbot.iris.mixin.LevelRendererAccessor;
import net.coderbot.iris.pipeline.ClearPass;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.coderbot.iris.vendored.joml.Vector4f;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(NewWorldRenderingPipeline.class)
public class IrisNewWorldRenderingPipelineVRMixin {
 @Inject(method = "renderShadows", at = @At("HEAD"), cancellable = true, remap = false)
    private void cancelShadows(LevelRendererAccessor par1, Camera par2, CallbackInfo ci) {
     if (!ClientDataHolder.getInstance().isFirstPass) {
            ci.cancel();
        }
    }

    @Redirect(method = "beginLevelRendering", remap = false, at = @At(value = "INVOKE", target = "Lnet/coderbot/iris/uniforms/FrameUpdateNotifier;onNewFrame()V"))
    private void no(FrameUpdateNotifier instance) {
        if (ClientDataHolder.getInstance().isFirstPass) {
            instance.onNewFrame();
        }
    }
    @Redirect(method = "beginLevelRendering", remap = false, at = @At(value = "INVOKE", target = "Lnet/coderbot/iris/pipeline/ClearPass;execute(Lnet/coderbot/iris/vendored/joml/Vector4f;)V", ordinal = 0))
    private void noX2(ClearPass instance, Vector4f vector4f) {
        if (ClientDataHolder.getInstance().isFirstPass) {
            instance.execute(vector4f);
        }
    }

}
