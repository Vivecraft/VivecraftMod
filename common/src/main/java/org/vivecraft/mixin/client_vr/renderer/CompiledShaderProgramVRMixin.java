package org.vivecraft.mixin.client_vr.renderer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.ShaderProgramConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.render.helpers.ShaderHelper;
import org.vivecraft.mixin.client.blaze3d.RenderSystemAccessor;

@Mixin(CompiledShaderProgram.class)
public abstract class CompiledShaderProgramVRMixin {
    @Shadow
    protected abstract Uniform parseUniformNode(ShaderProgramConfig.Uniform uniform);

    @ModifyExpressionValue(method = "setDefaultUniforms", at = @At(value = "CONSTANT", args = "intValue=12"))
    private int vivecraft$moreTextures(int constant) {
        return Math.max(constant, RenderSystemAccessor.getShaderTextures().length);
    }

    @Inject(method = "apply", at = @At("RETURN"))
    private void vivecraft$apply(CallbackInfo ci) {
        ShaderHelper.doMultiview((CompiledShaderProgram) (Object) this);
    }

    @Redirect(method = "setupUniforms", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/CompiledShaderProgram;parseUniformNode(Lnet/minecraft/client/renderer/ShaderProgramConfig$Uniform;)Lcom/mojang/blaze3d/shaders/Uniform;"))
    private Uniform vivecraft$replaceUniform(CompiledShaderProgram instance, ShaderProgramConfig.Uniform uniform) {
        if(uniform.type().equals("matrix4x4") && uniform.name().equals("ModelViewMat")) {
            int i = Uniform.getTypeFromString(uniform.type());
            int j = 32;
            int k = j > 1 && j <= 4 && i < 8 ? j - 1 : 0;
            return new Uniform(uniform.name(), i + k, j);
        }
        return this.parseUniformNode(uniform);
    }
}
