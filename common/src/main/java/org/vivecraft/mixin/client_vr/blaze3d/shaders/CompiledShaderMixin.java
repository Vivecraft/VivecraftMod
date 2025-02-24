package org.vivecraft.mixin.client_vr.blaze3d.shaders;

import com.mojang.blaze3d.shaders.CompiledShader;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CompiledShader.class)
public class CompiledShaderMixin {
    @Inject(method = "compile", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;glShaderSource(ILjava/lang/String;)V", shift = At.Shift.BEFORE))
    private static void onCompile(
        ResourceLocation shaderId, CompiledShader.Type type, String source, CallbackInfoReturnable<CompiledShader> cir) {
        if(type == CompiledShader.Type.VERTEX && source.contains("ModelViewMat")) {
            source = source.replace("#version 150", """
                #version 330 core
                #extension GL_OVR_multiview : enable
                
                layout(num_views = 2) in;""");
            source = source.replace("uniform mat4 ModelViewMat;", "uniform mat4 ModelViewMat[2];");
            source = source.replaceAll("^(?!uniform\\s+mat4\\s+ModelViewMat\\[\\d*];).*ModelViewMat.*", "ModelViewMat[gl_ViewIndex_OVR]");
        }
    }
}
