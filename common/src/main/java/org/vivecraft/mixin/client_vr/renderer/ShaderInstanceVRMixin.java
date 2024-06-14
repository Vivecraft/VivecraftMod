package org.vivecraft.mixin.client_vr.renderer;

import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.vivecraft.mixin.client.blaze3d.RenderSystemAccessor;

@Mixin(ShaderInstance.class)
public class ShaderInstanceVRMixin {

    @ModifyConstant(method = "setDefaultUniforms", constant = @Constant(intValue = 12))
    public int vivecraft$moreTextures(int constant) {
        return RenderSystemAccessor.getShaderTextures().length;
    }
}
