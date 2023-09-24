package org.vivecraft.mixin.client_vr.renderer.entity;

import org.vivecraft.client_xr.render_pass.RenderPassType;

import net.minecraft.world.entity.Entity;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(net.minecraft.client.renderer.entity.MobRenderer.class)
public class MobRendererVRMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getRopeHoldPosition(F)Lnet/minecraft/world/phys/Vec3;"), method = "renderLeash")
    public net.minecraft.world.phys.Vec3 leash(Entity instance, float f) {
        if (!RenderPassType.isVanilla() && instance == mc.player) {
            return dh.vrPlayer.vrdata_world_render.getController(0).getPosition();
        }
        return instance.getRopeHoldPosition(f);
    }
}
