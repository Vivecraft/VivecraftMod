package org.vivecraft.mixin.client_vr.renderer.entity;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(GuardianRenderer.class)
public abstract class GuardianRendererVRMixin {

    @Inject(method = "getPosition", at = @At("HEAD"), cancellable = true)
    private void vivecraft$changeTargetPos(
        CallbackInfoReturnable<Vec3> cir, @Local(argsOnly = true) LivingEntity livingEntity)
    {
        if (!RenderPassType.isVanilla() && livingEntity == Minecraft.getInstance().getCameraEntity()) {
            cir.setReturnValue(
                ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().hmd.getPosition()
                    .subtract(0.0D, 0.3F * ClientDataHolderVR.getInstance().vrPlayer.worldScale, 0.0D));
        }
    }
}
