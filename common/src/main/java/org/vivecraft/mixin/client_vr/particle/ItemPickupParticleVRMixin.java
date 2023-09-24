package org.vivecraft.mixin.client_vr.particle;

import org.vivecraft.client.Xplat;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.mod_compat_vr.pehkui.PehkuiHelper;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.client_vr.VRState.vrRunning;

import static org.joml.Math.*;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(net.minecraft.client.particle.ItemPickupParticle.class)
public class ItemPickupParticleVRMixin {

    @Final
    @Shadow
    private Entity target;
    @Final
    @Shadow
    private Entity itemEntity;

    @Unique
    private Vec3 playerPos;

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 0), method = "render")
    public double updateX(double d, double e, double f) {
        if (vrRunning && this.target == mc.player) {
            this.playerPos = ((GameRendererExtension) mc.gameRenderer).getControllerRenderPos(0);
            e = f = this.playerPos.x;
        }

        return lerp(e, f, d);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 1), method = "render")
    public double updateY(double d, double e, double f) {
        if (vrRunning && this.target == mc.player) {
            float offset = 0.5F;
            if (Xplat.isModLoaded("pehkui")) {
                // pehkui changes the offset, need to account for that
                offset *= PehkuiHelper.getPlayerScale(this.target, (float) d);
            }
            // offset, so the particle is centered around the arm
            offset += this.itemEntity.getBbHeight();
            e = f = this.playerPos.y - offset;
        }

        return lerp(e, f, d);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 2), method = "render")
    public double updateZ(double d, double e, double f) {
        if (vrRunning && this.target == mc.player) {
            e = f = this.playerPos.z;
            this.playerPos = null;
        }

        return lerp(e, f, d);
    }
}
