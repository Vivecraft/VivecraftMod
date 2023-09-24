package org.vivecraft.mod_compat_vr.optifine.mixin;

import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_xr.render_pass.RenderPassType;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import static org.vivecraft.client_vr.VRState.dh;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.optifine.shaders.Shaders")
public class ShadersVRMixin {
    @Inject(at = @At("TAIL"), method = "beginRender", remap = false)
    private static void resetBlend(CallbackInfo ci){
        // somehow the blend state is wrong here after shadows, when a spider gets rendered?
        RenderSystem.defaultBlendFunc();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getPosition()Lnet/minecraft/world/phys/Vec3;", remap = true), method = "setCameraShadow", remap = false)
    private static Vec3 positionCameraForShadows(Camera camera){
        if (RenderPassType.isVanilla()) {
            return camera.getPosition();
        } else {
            return dh.vrPlayer.getVRDataWorld().getEye(RenderPass.CENTER).getPosition();
        }
    }

    @ModifyVariable(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack$Pose;pose()Lorg/joml/Matrix4f;", shift = Shift.AFTER, remap = true), method = "setCameraShadow", remap = false)
    private static PoseStack offsetShadow(PoseStack shadowModelViewMat){
        if (!RenderPassType.isVanilla()) {
            Vec3 offset = dh.vrPlayer.getVRDataWorld().getEye(dh.currentPass).getPosition().subtract(dh.vrPlayer.getVRDataWorld().getEye(RenderPass.CENTER).getPosition());
            shadowModelViewMat.last().pose().translate((float) offset.x, (float) offset.y, (float) offset.z);
        }
        return shadowModelViewMat;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getX()D", remap = true), method = "setCameraOffset", remap = false)
    private static double sameX(Entity entity) {
        if (RenderPassType.isVanilla()) {
            return entity.getX();
        } else {
            return dh.vrPlayer.getVRDataWorld().getEye(RenderPass.CENTER).getPosition().x;
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getZ()D", remap = true), method = "setCameraOffset", remap = false)
    private static double sameZ(Entity entity) {
        if (RenderPassType.isVanilla()) {
            return entity.getZ();
        } else {
            return dh.vrPlayer.getVRDataWorld().getEye(RenderPass.CENTER).getPosition().z;
        }
    }
}
