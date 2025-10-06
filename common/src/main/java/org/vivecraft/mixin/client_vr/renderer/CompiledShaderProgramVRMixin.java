package org.vivecraft.mixin.client_vr.renderer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.CompiledShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.mixin.client.blaze3d.RenderSystemAccessor;

@Mixin(CompiledShaderProgram.class)
public class CompiledShaderProgramVRMixin {
    @ModifyExpressionValue(method = "setDefaultUniforms", at = @At(value = "CONSTANT", args = "intValue=12"))
    private int vivecraft$moreTextures(int constant) {
        return Math.max(constant, RenderSystemAccessor.getShaderTextures().length);
    }
}
