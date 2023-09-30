package org.vivecraft.mixin.world.item.crafting;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.common.CustomShapedRecipe;

import java.util.function.Function;

@Mixin(targets = "net.minecraft.world.item.crafting.ShapedRecipe$Serializer$RawShapedRecipe")
public abstract class ShapedRecipeMixin {

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/crafting/CraftingRecipeCodecs;ITEMSTACK_OBJECT_CODEC:Lcom/mojang/serialization/Codec;"), method = "method_53750")
    private static Codec<ItemStack> vivecraft$handleVivecraftRecipe() {
        // CODEC needs to be external, or it isn't initialized, when the other static codec want's to access it
        return CustomShapedRecipe.CODEC;
    }
}
