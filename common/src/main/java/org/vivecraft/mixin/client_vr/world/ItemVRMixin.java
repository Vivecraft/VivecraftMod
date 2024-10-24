package org.vivecraft.mixin.client_vr.world;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public class ItemVRMixin {

    @ModifyExpressionValue(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;canEat(Z)Z"))
    private boolean vivecraft$alwaysAllowEasterEggEating(boolean canEat, @Local ItemStack itemStack) {
        return canEat || itemStack.getHoverName().getString().equals("EAT ME");
    }
}
