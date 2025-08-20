package org.vivecraft.mixin.client_vr.renderer.item.properties.conditional;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.properties.conditional.IsUsingItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.trackers.BowTracker;

@Mixin(IsUsingItem.class)
public class IsUsingItemVRMixin {

    // loom doesn't want to remap this for some reason
    @Inject(method = {"get", "method_65638"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void vivecraft$noHornUseAnim(
        CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) ItemStack itemStack,
        @Local(argsOnly = true) LivingEntity entity)
    {
        if (VRState.VR_RUNNING && itemStack.is(Items.GOAT_HORN) && entity == Minecraft.getInstance().player) {
            cir.setReturnValue(false);
        }
    }

    @ModifyReturnValue(method = {"get", "method_65638"}, at = @At(value = "RETURN", ordinal = 1), remap = false)
    private boolean vivecraft$roomscaleBowNotch(
        boolean usingItem, @Local(argsOnly = true) ItemStack itemStack,
        @Local(argsOnly = true) LivingEntity livingEntity)
    {
        return usingItem ||
            (VRState.VR_RUNNING && livingEntity == Minecraft.getInstance().player && BowTracker.isBow(itemStack) &&
                ClientDataHolderVR.getInstance().bowTracker.isNotched()
            );
    }
}
