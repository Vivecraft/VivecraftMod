package org.vivecraft.mixin.client_vr.renderer.entity.layers;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.render.armor.VRArmorModel_WithArms;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;
import org.vivecraft.client_vr.settings.VRSettings;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {

    @Unique
    private HumanoidRenderState vivecraft$currentRenderState;

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At("HEAD"))
    private void vivecraft$storeState(
        CallbackInfo ci, @Local(argsOnly = true) HumanoidRenderState renderState)
    {
        this.vivecraft$currentRenderState = renderState;
    }


    // no remapping, because of a loom quirk and forge/neoforge override
    @Inject(method = {"renderArmorPiece*", "method_4169"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void vivecraft$noHelmetInFirstPerson(CallbackInfo ci, @Local(argsOnly = true) EquipmentSlot slot) {
        if (slot == EquipmentSlot.HEAD &&
            VREffectsHelper.isRenderingFirstPersonPlayer(this.vivecraft$currentRenderState))
        {
            ci.cancel();
        }
    }

    @Inject(method = {"renderArmorPiece*", "method_4169"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;setPartVisibility(Lnet/minecraft/client/model/HumanoidModel;Lnet/minecraft/world/entity/EquipmentSlot;)V", shift = At.Shift.AFTER, remap = true), remap = false)
    private void vivecraft$noArmsInFirstPerson(
        CallbackInfo ci, @Local(argsOnly = true) EquipmentSlot slot,
        @Local(argsOnly = true, ordinal = 0) HumanoidModel model)
    {
        if (slot == EquipmentSlot.CHEST &&
            VREffectsHelper.isRenderingFirstPersonPlayer(this.vivecraft$currentRenderState))
        {
            VRSettings.ModelArmsMode mode = ClientDataHolderVR.getInstance().vrSettings.modelArmsMode;

            // hide the arm armor, when not showing the arms in first person
            if (model instanceof VRArmorModel_WithArms<?> armsModel) {
                // shoulders when not off
                armsModel.leftArm.visible &= mode != VRSettings.ModelArmsMode.OFF;
                armsModel.rightArm.visible &= mode != VRSettings.ModelArmsMode.OFF;

                // front only when complete
                armsModel.leftHand.visible &= mode == VRSettings.ModelArmsMode.COMPLETE;
                armsModel.rightHand.visible &= mode == VRSettings.ModelArmsMode.COMPLETE;
            } else {
                // front only when complete
                model.leftArm.visible &= mode == VRSettings.ModelArmsMode.COMPLETE;
                model.rightArm.visible &= mode == VRSettings.ModelArmsMode.COMPLETE;
            }
        }
    }
}
