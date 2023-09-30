package org.vivecraft.common;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipeCodecs;

public class CustomShapedRecipe {

    public static final Codec<ItemStack> VIVECRAFT_ITEMSTACK_OBJECT_CODEC = RecordCodecBuilder.create((instance) ->
        instance.group(
            BuiltInRegistries.ITEM.byNameCodec()
                .fieldOf("vanillaitem")
                .forGetter(ItemStack::getItem),
            ExtraCodecs
                .strictOptionalField(ExtraCodecs.POSITIVE_INT, "count", 1)
                .forGetter(ItemStack::getCount),
            ExtraCodecs
                .strictOptionalField(ExtraCodecs.COMPONENT, "name", Component.empty())
                .forGetter(ItemStack::getHoverName),
            ExtraCodecs
                .strictOptionalField(Codec.BOOL, "unbreakable", false)
                .forGetter(itemStack -> itemStack.getOrCreateTag().getBoolean("Unbreakable")),
            ExtraCodecs
                .strictOptionalField(ExtraCodecs.POSITIVE_INT, "hideflags", 0)
                .forGetter(itemStack -> itemStack.getOrCreateTag().getInt("HideFlags"))
        ).apply(instance, (vanillaitem, count, name, unbreakable, hideflags) -> {
            ItemStack itemStack = new ItemStack(vanillaitem, count);
            if (!name.getString().isEmpty()) {
                itemStack.setHoverName(Component.translatable(name.getString()));
            }
            itemStack.getOrCreateTag().putInt("HideFlags", hideflags);
            itemStack.getOrCreateTag().putBoolean("Unbreakable", unbreakable);
            return itemStack;
        })
    );

    public static final Codec<ItemStack> CODEC = ExtraCodecs.either(VIVECRAFT_ITEMSTACK_OBJECT_CODEC, CraftingRecipeCodecs.ITEMSTACK_OBJECT_CODEC)
        .xmap(itemStackItemStackEither -> itemStackItemStackEither.map(stack -> stack, stack -> stack), Either::right);

}
