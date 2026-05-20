package org.vivecraft.mixin.client_vr.renderer.rendertype;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.extensions.RenderSetupExtension;

@Mixin(RenderType.class)
public class RenderTypeVRMixin {
    @Shadow
    @Final
    private RenderSetup state;

    @Inject(method = "draw", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;bindDefaultUniforms(Lcom/mojang/blaze3d/systems/RenderPass;)V", shift = At.Shift.AFTER))
    private void vivecraft$uniformOverrides(CallbackInfo ci, @Local RenderPass renderPass) {
        ((RenderSetupExtension) (Object) this.state).vivecraft$applyUniformOverrides(renderPass);
    }
}
