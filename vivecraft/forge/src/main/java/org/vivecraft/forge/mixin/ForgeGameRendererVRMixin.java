package org.vivecraft.forge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.extensions.GameRendererExtension;
import org.vivecraft.render.RenderPass;

@Mixin(GameRenderer.class)
public class ForgeGameRendererVRMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setAnglesInternal(FF)V", remap = false), method = "renderLevel")
    public void forgeInternal(Camera camera, float yaw, float pitch) {
        if(ClientDataHolder.getInstance().currentPass != RenderPass.LEFT && ClientDataHolder.getInstance().currentPass != RenderPass.RIGHT) {
            camera.setAnglesInternal(yaw, pitch);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lcom/mojang/math/Quaternion;)V", ordinal = 2), method = "renderLevel")
    public void forgeMulposZ(PoseStack poseStack, Quaternion quaternion) {
        if(ClientDataHolder.getInstance().currentPass != RenderPass.LEFT && ClientDataHolder.getInstance().currentPass != RenderPass.RIGHT) {
            poseStack.mulPose(quaternion);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lcom/mojang/math/Quaternion;)V ", ordinal = 3), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void removeMulposeX(PoseStack s, Quaternion quaternion) {
        return;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lcom/mojang/math/Quaternion;)V ", ordinal = 4), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void removeMulposeY(PoseStack s, Quaternion quaternion) {
        ((GameRendererExtension)this).applyVRModelView(ClientDataHolder.getInstance().currentPass, s);
    }

}
