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
                .strictOptionalField(ExtraCodecs.COMPONENT, "fallbackname", Component.empty())
                .forGetter(ItemStack::getHoverName),
            ExtraCodecs
                .strictOptionalField(Codec.BOOL, "unbreakable", false)
                .forGetter(itemStack -> itemStack.getOrCreateTag().getBoolean("Unbreakable")),
            ExtraCodecs
                .strictOptionalField(ExtraCodecs.POSITIVE_INT, "hideflags", 0)
                .forGetter(itemStack -> itemStack.getOrCreateTag().getInt("HideFlags")),
            ExtraCodecs
                .strictOptionalField(ExtraCodecs.POSITIVE_INT, "color", -1)
                .forGetter(itemStack -> itemStack.getOrCreateTagElement(ItemStack.TAG_DISPLAY).getInt(ItemStack.TAG_COLOR))
        ).apply(instance, (vanillaitem, count, name, fallbackname, unbreakable, hideflags, color) -> {
            ItemStack itemStack = new ItemStack(vanillaitem, count);
            if (!name.getString().isEmpty()) {
                if (!fallbackname.getString().isEmpty()) {
                    itemStack.setHoverName(Component.translatableWithFallback(
                        name.getString(),
                        fallbackname.getString()));
                } else {
                    itemStack.setHoverName(Component.translatable(name.getString()));
                }
            }
            itemStack.getOrCreateTag().putInt("HideFlags", hideflags);
            itemStack.getOrCreateTag().putBoolean("Unbreakable", unbreakable);
            if (color > -1) {
                itemStack.getOrCreateTagElement(ItemStack.TAG_DISPLAY).putInt(ItemStack.TAG_COLOR, color);
            }
            return itemStack;
        })
    );

    public static final Codec<ItemStack> CODEC = ExtraCodecs.either(VIVECRAFT_ITEMSTACK_OBJECT_CODEC, CraftingRecipeCodecs.ITEMSTACK_OBJECT_CODEC)
        .xmap(itemStackItemStackEither -> itemStackItemStackEither.map(stack -> stack, stack -> stack), Either::right);
}
