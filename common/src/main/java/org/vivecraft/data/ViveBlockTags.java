package org.vivecraft.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * holds Vivecrafts tags to identify Blocks
 */
public class ViveBlockTags {
    public static final TagKey<Block> VIVECRAFT_CLIMBABLE = tag("climbable");

    public static final TagKey<Block> VIVECRAFT_CROPS = tag("crops");

    public static final TagKey<Block> VIVECRAFT_MUSIC_BLOCKS = tag("music_blocks");

    private static TagKey<Block> tag(String name) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("vivecraft", name));
    }
}
