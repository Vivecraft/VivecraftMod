package org.vivecraft.mixin.client_vr.world;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.vrInitialized;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.item.PotionItem.class)
public class PotionItemVRMixin {
    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    private void drinkEasterEgg(ItemStack itemStack, Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir){
        if (vrInitialized && livingEntity instanceof LocalPlayer && "DRINK ME".equals(itemStack.getHoverName().getString())) {
            dh.vrPlayer.wfMode = -0.05;
            dh.vrPlayer.wfCount = 400;
        }
    }
}
