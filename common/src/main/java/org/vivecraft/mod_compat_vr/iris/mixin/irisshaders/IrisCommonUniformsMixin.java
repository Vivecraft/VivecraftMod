package org.vivecraft.mod_compat_vr.iris.mixin.irisshaders;

import com.llamalad7.mixinextras.sugar.Local;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

import java.util.function.Supplier;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.uniforms.CommonUniforms", remap = false)
public class IrisCommonUniformsMixin {
    @Inject(method = "generalCommonUniforms", at = @At("TAIL"))
    private static void vivecraft$addVivecraftUniforms(
        CallbackInfo ci, @Local(argsOnly = true) UniformHolder uniforms)
    {
        for (Triple<String, ShadersHelper.UniformType, Supplier<?>> uniform : ShadersHelper.getUniforms()) {
            switch (uniform.getMiddle()) {
                case MATRIX4F -> uniforms.uniformMatrix(UniformUpdateFrequency.PER_FRAME, uniform.getLeft(),
                    () -> (Matrix4fc) uniform.getRight().get());
                case VECTOR3F -> uniforms.uniform3f(UniformUpdateFrequency.PER_FRAME, uniform.getLeft(),
                    () -> (Vector3f) uniform.getRight().get());
                case INTEGER -> uniforms.uniform1i(UniformUpdateFrequency.PER_FRAME, uniform.getLeft(),
                    () -> (int) uniform.getRight().get());
                case BOOLEAN -> uniforms.uniform1b(UniformUpdateFrequency.PER_FRAME, uniform.getLeft(),
                    () -> (boolean) uniform.getRight().get());
                default -> throw new IllegalStateException("Unexpected uniform type: " + uniform.getMiddle());
            }
        }
    }
}
