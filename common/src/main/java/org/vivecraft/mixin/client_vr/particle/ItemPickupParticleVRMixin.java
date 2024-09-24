package org.vivecraft.mixin.client_vr.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.render.helpers.RenderHelper;

@Mixin(ItemPickupParticle.class)
public class ItemPickupParticleVRMixin {

    @Unique
    private static final Minecraft vivecraft$mc = Minecraft.getInstance();

    @Final
    @Shadow
    private Entity target;
    @Final
    @Shadow
    private Entity itemEntity;

    @Unique
    private Vec3 vivecraft$playerPos;

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 0), method = "render")
    public double vivecraft$updateX(double d, double e, double f) {
        if (VRState.vrRunning && target == vivecraft$mc.player) {
            vivecraft$playerPos = RenderHelper.getControllerRenderPos(0);
            e = f = vivecraft$playerPos.x;
        }

        return Mth.lerp(d, e, f);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 1), method = "render")
    public double vivecraft$updateY(double d, double e, double f) {
        if (VRState.vrRunning && target == vivecraft$mc.player) {
            e = f = vivecraft$playerPos.y - itemEntity.getBbHeight();
        }

        return Mth.lerp(d, e, f);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 2), method = "render")
    public double vivecraft$updateZ(double d, double e, double f) {
        if (VRState.vrRunning && target == vivecraft$mc.player) {
            e = f = vivecraft$playerPos.z;
            vivecraft$playerPos = null;
        }

        return Mth.lerp(d, e, f);
    }
}
