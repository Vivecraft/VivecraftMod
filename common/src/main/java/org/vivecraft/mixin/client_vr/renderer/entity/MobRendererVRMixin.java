package org.vivecraft.mixin.client_vr.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(MobRenderer.class)
public class MobRendererVRMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getRopeHoldPosition(F)Lnet/minecraft/world/phys/Vec3;"), method = "renderLeash")
    public Vec3 vivecraft$leash(Entity instance, float f) {
        if (!RenderPassType.isVanilla() && instance == Minecraft.getInstance().player) {
            return RenderHelper.getControllerRenderPos(0);
        }
        return instance.getRopeHoldPosition(f);
    }
}
