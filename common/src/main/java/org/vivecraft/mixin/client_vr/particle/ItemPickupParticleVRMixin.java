package org.vivecraft.mixin.client_vr.particle;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.mod_compat_vr.pehkui.PehkuiHelper;
import org.vivecraft.client.Xplat;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemPickupParticle.class)
public class ItemPickupParticleVRMixin {

    @Unique
    private static final Minecraft mc = Minecraft.getInstance();

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
        if (VRState.vrRunning && target == mc.player) {
            playerPos = ((GameRendererExtension) mc.gameRenderer).getControllerRenderPos(0);
            e = f = playerPos.x;
        }

        return Mth.lerp(d, e, f);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 1), method = "render")
    public double updateY(double d, double e, double f) {
        if (VRState.vrRunning && target == mc.player) {
            float offset = 0.5F;
            if (Xplat.isModLoaded("pehkui")) {
                // pehkui changes the offset, need to account for that
                offset *= PehkuiHelper.getPlayerScale(target, (float) d);
            }
            // offset, so the particle is centered around the arm
            offset += itemEntity.getBbHeight();
            e = f = playerPos.y - offset;
        }

        return Mth.lerp(d, e, f);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 2), method = "render")
    public double updateZ(double d, double e, double f) {
        if (VRState.vrRunning && target == mc.player) {
            e = f = playerPos.z;
            playerPos = null;
        }

        return Mth.lerp(d, e, f);
    }
}
