package org.vivecraft.mixin.client_vr.renderer;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.trackers.BowTracker;

@Mixin(ItemProperties.class)
public class ItemPropertiesVRMixin {

    @Inject(method = "method_43611", at = @At("HEAD"), cancellable = true)
    private static void vivecraft$noHornUseAnim(
        CallbackInfoReturnable<Float> cir, @Local(argsOnly = true) LivingEntity entity)
    {
        if (VRState.VR_RUNNING && entity == Minecraft.getInstance().player) {
            cir.setReturnValue(0.0F);
        }
    }

    @ModifyReturnValue(method = "method_27889", at = @At("RETURN"))
    private static float vivecraft$roomscaleBowNotch(
        float original, @Local(argsOnly = true) ItemStack itemStack, @Local(argsOnly = true) LivingEntity livingEntity)
    {
        if (VRState.VR_RUNNING && livingEntity == Minecraft.getInstance().player && BowTracker.isBow(itemStack) &&
            ClientDataHolderVR.getInstance().bowTracker.isNotched())
        {
            return 1F;
        } else {
            return original;
        }
    }
}
