package org.vivecraft.common;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class CustomShapedRecipe {

    /**
     * CODEC that allows additional parameters in recipe jsons
     */
    public static final Codec<ItemStack> VIVECRAFT_ITEMSTACK_OBJECT_CODEC = RecordCodecBuilder.create((instance) ->
        instance.group(
            BuiltInRegistries.ITEM.byNameCodec()
                .fieldOf("vanillaitem")
                .forGetter(ItemStack::getItem),
            ExtraCodecs
                .strictOptionalField(ExtraCodecs.POSITIVE_INT, "count", 1)
                .forGetter(ItemStack::getCount),
            ExtraCodecs
                .strictOptionalField(ComponentSerialization.CODEC, "name", Component.empty())
                .forGetter(ItemStack::getHoverName),
            ExtraCodecs
                .strictOptionalField(ComponentSerialization.CODEC, "fallbackname", Component.empty())
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

    /**
     * CODEC that tries the vivecraft codec first, and falls back to the vanilla one if it fails
     */
    public static final Codec<ItemStack> CODEC = ExtraCodecs.either(VIVECRAFT_ITEMSTACK_OBJECT_CODEC, ItemStack.ITEM_WITH_COUNT_CODEC)
        .xmap(itemStackItemStackEither -> itemStackItemStackEither.map(stack -> stack, stack -> stack), Either::right);
}
