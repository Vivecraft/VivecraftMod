package org.vivecraft.mixin.world.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.ClientDataHolder;

@Mixin(PotionItem.class)
public class PotionItemVRMixin {
    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    private void drinkEasterEgg(ItemStack itemStack, Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir){
        if (livingEntity instanceof LocalPlayer && itemStack.getHoverName().getString().equals("DRINK ME")) {
            ClientDataHolder.getInstance().vrPlayer.wfMode = -0.05;
            ClientDataHolder.getInstance().vrPlayer.wfCount = 400;
        }
    }
}
