package org.vivecraft.mixin.client.renderer.entity;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.common.utils.Utils;

@Mixin(EntityHitboxDebugRenderer.class)
public class EntityHitboxDebugRendererMixin {
    @Inject(method = "showHitboxes", at = @At("TAIL"))
    private void vivecraft$headHitbox(
        CallbackInfo ci, @Local(argsOnly = true) Entity entity, @Local(argsOnly = true) boolean isServerSide,
        @Local(ordinal = 2) Vec3 partialTickOffset)
    {
        AABB headBox;
        if (!isServerSide && ClientDataHolderVR.getInstance().vrSettings.renderHeadHitbox &&
            (headBox = Utils.getEntityHeadHitbox(entity, 0.0)) != null)
        {
            // raw head box
            Gizmos.cuboid(headBox.move(partialTickOffset), GizmoStyle.stroke(0xFFFFFF00));

            // inflated head box for arrows
            headBox = Utils.getEntityHeadHitbox(entity, 0.3);
            Gizmos.cuboid(headBox.move(partialTickOffset), GizmoStyle.stroke(0xFFFF0000));
        }
    }
}
