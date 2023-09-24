package org.vivecraft.mixin.client_vr.renderer;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.client_vr.VRState.vrRunning;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.client.renderer.item.ItemProperties.class)
public class ItemPropertiesVRMixin {

    @Group(name = "disableGoatHornAnimation", min = 1, max = 1)
    @Inject(at = @At("HEAD"), method = "method_43611"       // fabric
            , remap = false, cancellable = true, expect = 0)
    private static void noHornUseAnimFabric(ItemStack itemStack, ClientLevel clientLevel, LivingEntity livingEntity, int i, CallbackInfoReturnable<Float> cir){
        if (vrRunning && livingEntity == mc.player) {
            cir.setReturnValue(0.0F);
        }
    }

    @Group(name = "disableGoatHornAnimation", min = 1, max = 1)
    @Inject(at = @At("HEAD"), method = "m_234977_"     // forge
            , remap = false, cancellable = true, expect = 0)
    private static void noHornUseAnimForge(ItemStack itemStack, ClientLevel clientLevel, LivingEntity livingEntity, int i, CallbackInfoReturnable<Float> cir){
        if (vrRunning && livingEntity == mc.player) {
            cir.setReturnValue(0.0F);
        }
    }
}
