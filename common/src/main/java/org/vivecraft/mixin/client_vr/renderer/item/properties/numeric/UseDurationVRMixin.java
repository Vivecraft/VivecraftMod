package org.vivecraft.mixin.client_vr.renderer.item.properties.numeric;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.properties.numeric.UseDuration;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.trackers.BowTracker;

@Mixin(UseDuration.class)
public class UseDurationVRMixin {

    @Shadow
    @Final
    private boolean remaining;

    @ModifyReturnValue(method = "get", at = @At(value = "RETURN"))
    private float vivecraft$roomscaleBowNotch(
        float useTime, @Local(argsOnly = true) ItemStack itemStack, @Local(argsOnly = true) ItemOwner livingEntity)
    {
        // some resourcepacks use the use duration for the first bow step, instead of item use
        return
            !this.remaining && useTime < 2F && VRState.VR_RUNNING && livingEntity == Minecraft.getInstance().player &&
                BowTracker.isBow(itemStack) && ClientDataHolderVR.getInstance().bowTracker.isNotched()
                ? 2F : useTime;
    }
}
