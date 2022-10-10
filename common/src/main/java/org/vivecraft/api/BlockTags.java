package org.vivecraft.api;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class BlockTags {
    public final static TagKey<Block> VIVECRAFT_CLIMBABLE = tag("climbable");
    public final static TagKey<Block> VIVECRAFT_CROPS = tag("crops");

    public final static TagKey<Block> VIVECRAFT_MUSIC_BLOCKS = tag("music_blocks");

    private static TagKey<Block> tag(String name){
        return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("vivecraft", name));
    }
}
