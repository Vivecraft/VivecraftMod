package org.vivecraft.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.api.ClientDataHolder;
import org.vivecraft.api.extensions.GameRendererExtension;

@Mixin(GameRenderer.class)
public class FabricGameRendererVRMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lcom/mojang/math/Quaternion;)V ", ordinal = 2), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void removeMulposeX(PoseStack s, Quaternion quaternion) {
        return;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lcom/mojang/math/Quaternion;)V ", ordinal = 3), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void removeMulposeY(PoseStack s, Quaternion quaternion) {
        ((GameRendererExtension)this).applyVRModelView(ClientDataHolder.getInstance().currentPass, s);
    }
}
