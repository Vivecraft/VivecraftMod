package org.vivecraft.mixin.client_vr.renderer.item.properties.conditional;

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
import org.vivecraft.client_vr.VRState;

@Mixin(IsUsingItem.class)
public class IsUsingItemVRMixin {

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void vivecraft$noHornUseAnim(
        CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) ItemStack itemStack,
        @Local(argsOnly = true) LivingEntity entity)
    {
        if (VRState.VR_RUNNING && itemStack.is(Items.GOAT_HORN) && entity == Minecraft.getInstance().player) {
            cir.setReturnValue(false);
        }
    }
}
