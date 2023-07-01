package org.vivecraft.fabric.mixin.world.level.biome;

import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Biome.class)
public interface BiomeAccessor{
    @Accessor("climateSettings")
    Biome.ClimateSettings getClimateSettings();
}
