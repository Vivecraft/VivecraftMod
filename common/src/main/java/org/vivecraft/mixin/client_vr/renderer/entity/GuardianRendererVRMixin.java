package org.vivecraft.mixin.client_vr.renderer.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(GuardianRenderer.class)
public abstract class GuardianRendererVRMixin {

    @WrapOperation(method = "render(Lnet/minecraft/world/entity/monster/Guardian;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/GuardianRenderer;getPosition(Lnet/minecraft/world/entity/LivingEntity;DF)Lnet/minecraft/world/phys/Vec3;", ordinal = 0))
    private Vec3 vivecraft$changeTargetPos(
        GuardianRenderer instance, LivingEntity livingEntity, double yOffset, float partialTick,
        Operation<Vec3> original)
    {
        if (!RenderPassType.isVanilla() && livingEntity == Minecraft.getInstance().getCameraEntity()) {
            return ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getEye(RenderPass.CENTER).getPosition().subtract(0.0D, 0.3F * ClientDataHolderVR.getInstance().vrPlayer.worldScale, 0.0D);
        } else {
            return original.call(instance, livingEntity, yOffset, partialTick);
        }
    }
}
