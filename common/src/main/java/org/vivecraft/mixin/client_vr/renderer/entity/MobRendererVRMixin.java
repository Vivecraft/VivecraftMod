package org.vivecraft.mixin.client_vr.renderer.entity;

import org.vivecraft.client_vr.ClientDataHolderVR;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(MobRenderer.class)
public class MobRendererVRMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getRopeHoldPosition(F)Lnet/minecraft/world/phys/Vec3;"), method = "renderLeash")
    public Vec3 leash(Entity instance, float f) {
        if (!RenderPassType.isVanilla() && instance == Minecraft.getInstance().player) {
            return ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_render.getController(0).getPosition();
        }
        return instance.getRopeHoldPosition(f);
    }
}
