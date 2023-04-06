package org.vivecraft.mixin.client.renderer.entity;

import org.vivecraft.client.ClientDataHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.client.render.RenderPass;

@Mixin(GuardianRenderer.class)
public abstract class GuardianRendererVRMixin {

    @Shadow
    protected abstract Vec3 getPosition(LivingEntity livingEntity, double d, float f);

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/GuardianRenderer;getPosition(Lnet/minecraft/world/entity/LivingEntity;DF)Lnet/minecraft/world/phys/Vec3;"), method = "render(Lnet/minecraft/world/entity/monster/Guardian;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    public Vec3 changeEye(GuardianRenderer instance, LivingEntity livingEntity, double d, float f) {
        if (livingEntity == Minecraft.getInstance().getCameraEntity()) {
            return ClientDataHolder.getInstance().vrPlayer.getVRDataWorld().getEye(RenderPass.CENTER).getPosition().subtract(0.0D, 0.3D * (double) ClientDataHolder.getInstance().vrPlayer.worldScale, 0.0D);
        }
        return this.getPosition(livingEntity, d, f);
    }
}
