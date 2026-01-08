package org.vivecraft.mixin.client.renderer.entity;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.HitboxRenderState;
import net.minecraft.client.renderer.entity.state.HitboxesRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.common.utils.Utils;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @Inject(method = "extractHitboxes(Lnet/minecraft/world/entity/Entity;FZ)Lnet/minecraft/client/renderer/entity/state/HitboxesRenderState;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;extractAdditionalHitboxes(Lnet/minecraft/world/entity/Entity;Lcom/google/common/collect/ImmutableList$Builder;F)V"))
    private void vivecraft$headHitbox(
        CallbackInfoReturnable<HitboxesRenderState> cir,
        @Local(argsOnly = true) Entity entity, @Local ImmutableList.Builder<HitboxRenderState> builder)
    {
        AABB headBox;
        if (ClientDataHolderVR.getInstance().vrSettings.renderHeadHitbox &&
            (headBox = Utils.getEntityHeadHitbox(entity, 0.0)) != null)
        {
            // raw head box
            builder.add(new HitboxRenderState(
                headBox.minX - entity.getX(), headBox.minY - entity.getY(), headBox.minZ - entity.getZ(),
                headBox.maxX - entity.getX(), headBox.maxY - entity.getY(), headBox.maxZ - entity.getZ(),
                1.0F, 1.0F, 0.0F));

            // inflated head box for arrows
            headBox = Utils.getEntityHeadHitbox(entity, 0.3);
            builder.add(new HitboxRenderState(
                headBox.minX - entity.getX(), headBox.minY - entity.getY(), headBox.minZ - entity.getZ(),
                headBox.maxX - entity.getX(), headBox.maxY - entity.getY(), headBox.maxZ - entity.getZ(),
                1.0F, 0.0F, 0.0F));
        }
    }
}
