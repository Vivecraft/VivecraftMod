package org.vivecraft.mixin.accessor.world.entity;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    // for correct vr rendering
    @Accessor
    float getEyeHeight();
    @Accessor
    void setEyeHeight(float eyeHeight);
}
