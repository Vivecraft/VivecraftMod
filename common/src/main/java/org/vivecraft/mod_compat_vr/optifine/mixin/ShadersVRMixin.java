package org.vivecraft.mod_compat_vr.optifine.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_xr.render_pass.RenderPassType;

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
            return ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getEye(RenderPass.CENTER).getPosition();
        }
    }

    @ModifyVariable(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack$Pose;pose()Lcom/mojang/math/Matrix4f;", shift = At.Shift.AFTER, remap = true), method = "setCameraShadow", remap = false)
    private static PoseStack offsetShadow(PoseStack shadowModelViewMat){
        if (!RenderPassType.isVanilla()) {
            Vec3 offset = ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getEye(ClientDataHolderVR.getInstance().currentPass).getPosition().subtract(ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getEye(RenderPass.CENTER).getPosition());
            shadowModelViewMat.translate((float) offset.x, (float) offset.y, (float) offset.z);
        }
        return shadowModelViewMat;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getX()D", remap = true), method = "setCameraOffset", remap = false)
    private static double sameX(Entity entity) {
        if (RenderPassType.isVanilla()) {
            return entity.getX();
        } else {
            return ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getEye(RenderPass.CENTER).getPosition().x;
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getZ()D", remap = true), method = "setCameraOffset", remap = false)
    private static double sameZ(Entity entity) {
        if (RenderPassType.isVanilla()) {
            return entity.getZ();
        } else {
            return ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getEye(RenderPass.CENTER).getPosition().z;
        }
    }
}
