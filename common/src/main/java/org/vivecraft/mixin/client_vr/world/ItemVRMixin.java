package org.vivecraft.mixin.client_vr.world;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(net.minecraft.world.item.Item.class)
public class ItemVRMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;canEat(Z)Z", shift = Shift.BEFORE), method = "use", locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void alwaysAllowEasterEggEating(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir, ItemStack itemStack) {
        if ("EAT ME".equals(itemStack.getHoverName().getString())) {
            player.startUsingItem(interactionHand);
            cir.setReturnValue(InteractionResultHolder.consume(itemStack));
        }
    }
}
