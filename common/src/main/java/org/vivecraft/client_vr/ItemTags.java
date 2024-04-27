package org.vivecraft.client_vr;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ItemTags {
    public final static TagKey<Item> VIVECRAFT_ARROWS = tag("arrows");

    public final static TagKey<Item> VIVECRAFT_BRUSHES = tag("brushes");

    public final static TagKey<Item> VIVECRAFT_COMPASSES = tag("compasses");

    public final static TagKey<Item> VIVECRAFT_CROSSBOWS = tag("crossbows");

    public final static TagKey<Item> VIVECRAFT_FISHING_RODS = tag("fishing_rods");

    public final static TagKey<Item> VIVECRAFT_FOOD_STICKS = tag("food_sticks");

    public final static TagKey<Item> VIVECRAFT_HOES = tag("hoes");

    public final static TagKey<Item> VIVECRAFT_MACES = tag("maces");


    public final static TagKey<Item> VIVECRAFT_MAPS = tag("maps");

    public final static TagKey<Item> VIVECRAFT_SCYTHES = tag("scythes");

    public final static TagKey<Item> VIVECRAFT_SHIELDS = tag("shields");

    public final static TagKey<Item> VIVECRAFT_SPEARS = tag("spears");

    public final static TagKey<Item> VIVECRAFT_SWORDS = tag("swords");

    public final static TagKey<Item> VIVECRAFT_TELESCOPE = tag("telescope");

    public final static TagKey<Item> VIVECRAFT_THROW_ITEMS = tag("throw_items");

    public final static TagKey<Item> VIVECRAFT_TOOLS = tag("tools");

    public final static TagKey<Item> VIVECRAFT_BOW_EXCLUSION = tag("bow_exclusion");

    private static TagKey<Item> tag(String name) {
        return TagKey.create(Registries.ITEM, new ResourceLocation("vivecraft", name));
    }
}
