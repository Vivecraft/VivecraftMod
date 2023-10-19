package org.vivecraft.mixin.accessor.world.level.biome;

import net.minecraft.world.level.biome.AmbientParticleSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AmbientParticleSettings.class)
public interface AmbientParticleSettingsAccessor {
    @Accessor
    float getProbability();
}
