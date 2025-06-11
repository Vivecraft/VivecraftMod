package org.vivecraft.mixin.client.renderer.entity.state;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client.ClientVRPlayers;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.trackers.ClimbTracker;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;
import org.vivecraft.client_vr.settings.VRSettings;

@Mixin(ArmedEntityRenderState.class)
public class ArmedEntityRenderStateMixin {
    @ModifyExpressionValue(method = "extractArmedEntityRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getMainArm()Lnet/minecraft/world/entity/HumanoidArm;"))
    private static HumanoidArm vivecraft$leftHanded(HumanoidArm original, @Local(argsOnly = true) LivingEntity entity) {
        if (ClientVRPlayers.getInstance().isVRAndLeftHanded(entity.getUUID())) {
            return original.getOpposite();
        }
        return original;
    }

    @WrapOperation(method = "extractArmedEntityRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getItemHeldByArm(Lnet/minecraft/world/entity/HumanoidArm;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack vivecraft$clawOverride(
        LivingEntity instance, HumanoidArm humanoidArm, Operation<ItemStack> original)
    {
        ItemStack thisStack = original.call(instance, humanoidArm);

        if (ClientNetworking.SERVER_ALLOWS_CLIMBEY && ClientVRPlayers.getInstance().isVRPlayer(instance.getUUID()) &&
            !ClimbTracker.isClaws(thisStack))
        {
            ItemStack otherStack = original.call(instance, humanoidArm.getOpposite());
            if (ClimbTracker.isClaws(otherStack) &&
                !ClientVRPlayers.getInstance().isVRAndSeated(instance.getUUID()))
            {
                return otherStack;
            }
        }

        return thisStack;
    }

    @WrapOperation(method = "extractArmedEntityRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getItemHeldByArm(Lnet/minecraft/world/entity/HumanoidArm;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack vivecraft$noItemsInFirstPerson(
        LivingEntity instance, HumanoidArm arm, Operation<ItemStack> original,
        @Local(argsOnly = true) ArmedEntityRenderState renderState)
    {
        ItemStack itemStack = original.call(instance, arm);
        if (VREffectsHelper.isRenderingFirstPersonPlayer(renderState) &&
            // don't cancel climbing claws, unless menu hand
            (ClientDataHolderVR.getInstance().vrSettings.modelArmsMode != VRSettings.ModelArmsMode.COMPLETE ||
                ClientDataHolderVR.getInstance().isMenuHand(arm) ||
                !(ClientDataHolderVR.getInstance().climbTracker.isClimbeyClimb() || ClimbTracker.isClaws(itemStack))
            ))
        {
            return ItemStack.EMPTY;
        }
        return itemStack;
    }
}
