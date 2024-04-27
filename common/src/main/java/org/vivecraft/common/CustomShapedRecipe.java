package org.vivecraft.common;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.Unbreakable;

public class CustomShapedRecipe {

    public static final Codec<ItemStack> VIVECRAFT_ITEMSTACK_OBJECT_CODEC = RecordCodecBuilder.create((instance) ->
        instance.group(
            BuiltInRegistries.ITEM.byNameCodec()
                .fieldOf("vanillaitem")
                .forGetter(ItemStack::getItem),
            ExtraCodecs.POSITIVE_INT
                .fieldOf("count")
                .orElse(1)
                .forGetter(ItemStack::getCount),
            ComponentSerialization.CODEC
                .fieldOf("name")
                .orElse(Component.empty())
                .forGetter(ItemStack::getHoverName),
            ComponentSerialization.CODEC
                .fieldOf("fallbackname")
                .orElse(Component.empty())
                .forGetter(ItemStack::getHoverName),
            Codec.BOOL
                .fieldOf("unbreakable")
                .orElse(false)
                .forGetter(itemStack -> itemStack.has(DataComponents.UNBREAKABLE)),
            ExtraCodecs.POSITIVE_INT
                .fieldOf("color")
                .orElse(1)
                .forGetter(itemStack -> DyedItemColor.getOrDefault(itemStack, -1))
        ).apply(instance, (vanillaitem, count, name, fallbackname, unbreakable, color) -> {
            ItemStack itemStack = new ItemStack(vanillaitem, count);
            if (!name.getString().isEmpty()) {
                if (!fallbackname.getString().isEmpty()) {
                    itemStack.set(DataComponents.CUSTOM_NAME, Component.translatableWithFallback(
                        name.getString(),
                        fallbackname.getString()));
                } else {
                    itemStack.set(DataComponents.CUSTOM_NAME, Component.translatable(name.getString()));
                }
            }
            if (unbreakable) {
                itemStack.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
            }
            if (color > -1) {
                itemStack.set(DataComponents.DYED_COLOR, new DyedItemColor(color, false));
            }
            return itemStack;
        })
    );

    public static final Codec<ItemStack> CODEC = Codec.either(VIVECRAFT_ITEMSTACK_OBJECT_CODEC, ItemStack.STRICT_CODEC)
        .xmap(itemStackItemStackEither -> itemStackItemStackEither.map(stack -> stack, stack -> stack), Either::right);
}
