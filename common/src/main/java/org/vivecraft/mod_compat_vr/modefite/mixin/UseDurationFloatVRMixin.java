package org.vivecraft.mod_compat_vr.modefite.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.trackers.BowTracker;

@Pseudo
@Mixin(targets = "timmychips.modefiteitemdefinitions.property.resolver.rangeentry.UseDurationFloat", remap = false)
public class UseDurationFloatVRMixin {

    @ModifyReturnValue(method = "getValue", at = @At(value = "RETURN"))
    private float vivecraft$roomscaleBowNotch(
        float useTime, @Local(argsOnly = true) ItemStack itemStack, @Local(argsOnly = true) LivingEntity livingEntity,
        @Local(ordinal = 0) boolean use_remaining)
    {
        // some resourcepacks use the use duration for the first bow step, instead of item use
        return
            !use_remaining && useTime < 2F && VRState.VR_RUNNING && livingEntity == Minecraft.getInstance().player &&
                BowTracker.isBow(itemStack) && ClientDataHolderVR.getInstance().bowTracker.isNotched()
                ? 2F : useTime;
    }
}
