package org.vivecraft.mixin.world.item.crafting;

import com.mojang.serialization.Codec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.common.CustomShapedRecipe;

@Mixin(targets = "net.minecraft.world.item.crafting.ShapedRecipe$Serializer$RawShapedRecipe")
public class ShapedRecipeMixin {
    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/crafting/CraftingRecipeCodecs;ITEMSTACK_OBJECT_CODEC:Lcom/mojang/serialization/Codec;"), method = "method_53750")
    private static Codec vivecraft$handleVivecraftRecipe() {
        // CODEC needs to be external, or it isn't initialized, when the other static codec want's to access it
        return CustomShapedRecipe.VIVECRAFT_ITEMSTACK_OBJECT_CODEC;
    }
}
