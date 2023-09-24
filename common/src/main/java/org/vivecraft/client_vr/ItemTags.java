package org.vivecraft.client_vr;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ItemTags {
    public static final TagKey<Item> VIVECRAFT_ARROWS = tag("arrows");

    public static final TagKey<Item> VIVECRAFT_BRUSHES = tag("brushes");

    public static final TagKey<Item> VIVECRAFT_COMPASSES = tag("compasses");

    public static final TagKey<Item> VIVECRAFT_CROSSBOWS = tag("crossbows");

    public static final TagKey<Item> VIVECRAFT_FISHING_RODS = tag("fishing_rods");

    public static final TagKey<Item> VIVECRAFT_FOOD_STICKS = tag("food_sticks");

    public static final TagKey<Item> VIVECRAFT_HOES = tag("hoes");

    public static final TagKey<Item> VIVECRAFT_MAPS = tag("maps");

    public static final TagKey<Item> VIVECRAFT_SCYTHES = tag("scythes");

    public static final TagKey<Item> VIVECRAFT_SHIELDS = tag("shields");

    public static final TagKey<Item> VIVECRAFT_SPEARS = tag("spears");

    public static final TagKey<Item> VIVECRAFT_SWORDS = tag("swords");

    public static final TagKey<Item> VIVECRAFT_TELESCOPE = tag("telescope");

    public static final TagKey<Item> VIVECRAFT_THROW_ITEMS = tag("throw_items");

    public static final TagKey<Item> VIVECRAFT_TOOLS = tag("tools");

    private static TagKey<Item> tag(final String name){
        return TagKey.create(Registries.ITEM, new ResourceLocation("vivecraft", name));
    }
}
