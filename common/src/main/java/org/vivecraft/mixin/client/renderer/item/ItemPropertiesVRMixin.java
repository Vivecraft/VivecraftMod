package org.vivecraft.mixin.client.renderer.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemProperties.class)
public class ItemPropertiesVRMixin {

    @Group(name = "disableGoatHornAnimation", min = 1, max = 1)
    @Inject(at = @At("HEAD"), method = "method_43611"       // fabric
            , remap = false, cancellable = true, expect = 0)
    private static void noHornUseAnimFabric(ItemStack itemStack, ClientLevel clientLevel, LivingEntity livingEntity, int i, CallbackInfoReturnable<Float> cir){
        if (livingEntity == Minecraft.getInstance().player) {
            cir.setReturnValue(0.0F);
        }
    }

    @Group(name = "disableGoatHornAnimation", min = 1, max = 1)
    @Inject(at = @At("HEAD"), method = "m_234977_"     // forge
            , remap = false, cancellable = true, expect = 0)
    private static void noHornUseAnimForge(ItemStack itemStack, ClientLevel clientLevel, LivingEntity livingEntity, int i, CallbackInfoReturnable<Float> cir){
        if (livingEntity == Minecraft.getInstance().player) {
            cir.setReturnValue(0.0F);
        }
    }
}
