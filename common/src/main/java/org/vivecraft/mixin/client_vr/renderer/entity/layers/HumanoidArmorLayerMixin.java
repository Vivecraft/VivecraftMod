package org.vivecraft.mixin.client_vr.renderer.entity.layers;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {

    @Unique
    private HumanoidRenderState vivecraft$currentRenderState;

    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At("HEAD"))
    private void vivecraft$storeState(
        CallbackInfo ci, @Local(argsOnly = true) HumanoidRenderState renderState)
    {
        this.vivecraft$currentRenderState = renderState;
    }

    @Inject(method = "renderArmorPiece*", at = @At("HEAD"), cancellable = true)
    private void vivecraft$noHelmetInFirstPerson(CallbackInfo ci, @Local(argsOnly = true) EquipmentSlot slot) {
        if (slot == EquipmentSlot.HEAD &&
            VREffectsHelper.isRenderingFirstPersonPlayer(this.vivecraft$currentRenderState))
        {
            ci.cancel();
        }
    }
}
