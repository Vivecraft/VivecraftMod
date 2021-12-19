package org.vivecraft.utils;

import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public interface ITeleporterDummy
{
    void placeEntity(Level var1, Entity var2, float var3);

default boolean isVanilla()
    {
        return true;
    }

default Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity)
    {
        return repositionEntity.apply(true);
    }
}
