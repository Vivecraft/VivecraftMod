package org.vivecraft.mixin.client_vr.renderer.feature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.common.utils.MathUtils;

@Mixin(NameTagFeatureRenderer.Storage.class)
public class NameTagFeatureRendererVRMixin {

    @WrapOperation(method = "add", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/CameraRenderState;orientation:Lorg/joml/Quaternionf;"))
    private Quaternionf vivecraft$cameraOffset(
        CameraRenderState camera, Operation<Quaternionf> original, @Local(argsOnly = true) PoseStack poseStack)
    {
        if (RenderPassType.isVanilla() || RenderPass.isThirdPerson(ClientDataHolderVR.getInstance().currentPass)) {
            return original.call(camera);
        }
        // the poseStack translation has the offset of the nametag to the camera
        Vector3f direction = poseStack.last().pose().getTranslation(new Vector3f());

        // add the eye offset
        VRData world = ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld();
        direction.sub(MathUtils.subtractToVector3f(world.hmd.getPosition(),
            world.getEye(ClientDataHolderVR.getInstance().currentPass).getPosition()));
        direction.normalize();


        return new Quaternionf()
            .rotationYXZ(
                Mth.PI - (float) Math.atan2(-direction.x, direction.z),
                (float) Math.asin(direction.y / direction.length()),
                0.0F);
    }
}
