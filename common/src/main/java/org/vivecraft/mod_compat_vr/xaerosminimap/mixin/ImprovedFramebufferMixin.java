package org.vivecraft.mod_compat_vr.xaerosminimap.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "xaero.common.graphics.ImprovedFramebuffer")
public class ImprovedFramebufferMixin {
    @ModifyExpressionValue(method = "forceAsMainRenderTarget", at = @At(value = "FIELD", target = "Lxaero/common/graphics/ImprovedFramebuffer;mainRenderTargetBackup:Lcom/mojang/blaze3d/pipeline/RenderTarget;", ordinal = 0, remap = true), remap = false)
    private RenderTarget vivecraft$fixConstantReference(RenderTarget mainRenderTarget) {
        // refetch main target when not an improved buffer, to get the new gui buffer
        return this.getClass().isInstance(Minecraft.getInstance().mainRenderTarget) ? mainRenderTarget : null;
    }
}
