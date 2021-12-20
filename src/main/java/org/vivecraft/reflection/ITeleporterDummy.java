package org.vivecraft.reflection;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public interface ITeleporterDummy
{
    void placeEntity(Level var1, Entity var2, float var3);

default boolean isVanilla()
    {
        return true;
    }
}
