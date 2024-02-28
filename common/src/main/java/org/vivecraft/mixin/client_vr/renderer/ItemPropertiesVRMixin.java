package org.vivecraft.mixin.client_vr.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.VRState;

@Mixin(ItemProperties.class)
public class ItemPropertiesVRMixin {

    @Inject(at = @At("HEAD"), method = "method_43611", cancellable = true)
    private static void vivecraft$noHornUseAnimFabric(ItemStack itemStack, ClientLevel clientLevel, LivingEntity livingEntity, int i, CallbackInfoReturnable<Float> cir) {
        if (VRState.vrRunning && livingEntity == Minecraft.getInstance().player) {
            cir.setReturnValue(0.0F);
        }
    }
}
