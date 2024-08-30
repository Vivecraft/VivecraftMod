package org.vivecraft.mixin.client_vr.particle;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client.Xplat;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.mod_compat_vr.pehkui.PehkuiHelper;

@Mixin(ItemPickupParticle.class)
public class ItemPickupParticleVRMixin {

    @Final
    @Shadow
    private Entity target;
    @Final
    @Shadow
    private Entity itemEntity;

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 0))
    private double vivecraft$updateX(
        double delta, double start, double end, Operation<Double> original,
        @Share("controllerPos") LocalRef<Vec3> controllerPos)
    {
        if (VRState.vrRunning && target == Minecraft.getInstance().player) {
            controllerPos.set(RenderHelper.getControllerRenderPos(0));
            return controllerPos.get().x;
        } else {
            return original.call(delta, start, end);
        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 1))
    private double vivecraft$updateY(
        double delta, double start, double end, Operation<Double> original,
        @Share("controllerPos") LocalRef<Vec3> controllerPos)
    {
        if (VRState.vrRunning && target == Minecraft.getInstance().player) {
            float offset = 0.5F;
            if (Xplat.isModLoaded("pehkui")) {
                // pehkui changes the offset, need to account for that
                offset *= PehkuiHelper.getPlayerScale(target, (float) delta);
            }
            // offset, so the particle is centered around the arm
            offset += itemEntity.getBbHeight();
            return controllerPos.get().y - offset;
        } else {
            return original.call(delta, start, end);
        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 2))
    private double vivecraft$updateZ(
        double delta, double start, double end, Operation<Double> original,
        @Share("controllerPos") LocalRef<Vec3> controllerPos)
    {
        if (VRState.vrRunning && target == Minecraft.getInstance().player) {
            return  controllerPos.get().z;
        } else {
            return original.call(delta, start, end);
        }
    }
}
