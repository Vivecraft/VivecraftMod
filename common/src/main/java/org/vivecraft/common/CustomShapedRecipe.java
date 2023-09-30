package org.vivecraft.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CustomShapedRecipe {
    public static final Codec<ItemStack> VIVECRAFT_ITEMSTACK_OBJECT_CODEC = RecordCodecBuilder.create((instance) ->
        instance.group(
            BuiltInRegistries.ITEM.byNameCodec()
                .fieldOf("item")
                .forGetter(ItemStack::getItem),
            ExtraCodecs
                .strictOptionalField(BuiltInRegistries.ITEM.byNameCodec(), "vanillaitem", Items.AIR)
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
        ).apply(instance, (item, vanillaitem, count, name, unbreakable, hideflags) -> {
            if (vanillaitem != Items.AIR) {
                ItemStack itemStack = new ItemStack(vanillaitem, count);
                if (!name.getString().isEmpty()) {
                    itemStack.setHoverName(Component.translatable(name.getString()));
                }
                itemStack.getOrCreateTag().putInt("HideFlags", hideflags);
                itemStack.getOrCreateTag().putBoolean("Unbreakable", unbreakable);
                return itemStack;
            } else {
                return new ItemStack(item, count);
            }
        })
    );
}
