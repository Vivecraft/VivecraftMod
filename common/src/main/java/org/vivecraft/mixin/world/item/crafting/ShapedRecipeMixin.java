package org.vivecraft.mixin.world.item.crafting;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.common.CustomShapedRecipe;

@Mixin(ShapedRecipe.Serializer.class)
public abstract class ShapedRecipeMixin {

    @Redirect(method = "method_55071", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/ItemStack;ITEM_WITH_COUNT_CODEC:Lcom/mojang/serialization/Codec;"))
    private static Codec<ItemStack> vivecraft$handleVivecraftRecipe() {
        // CODEC needs to be external, or it isn't initialized, when the other static codec want's to access it
        return CustomShapedRecipe.CODEC;
    }
}
