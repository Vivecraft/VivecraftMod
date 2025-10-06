package org.vivecraft.mixin.client.renderer.entity.layers;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;

@Mixin(PlayerItemInHandLayer.class)
public class PlayerItemInHandLayerMixin {
    @ModifyExpressionValue(method = "renderArmWithItem(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;isEmpty()Z", ordinal = 1))
    private boolean vivecraft$noSpyglassInFirstPerson(
        boolean heldOnHead, @Local(argsOnly = true) PlayerRenderState renderState)
    {
        return heldOnHead && !VREffectsHelper.isRenderingFirstPersonPlayer(renderState);
    }
}
