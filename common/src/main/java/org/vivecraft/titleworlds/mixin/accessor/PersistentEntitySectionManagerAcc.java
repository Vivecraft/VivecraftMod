package org.vivecraft.titleworlds.mixin.accessor;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(PersistentEntitySectionManager.class)
public interface PersistentEntitySectionManagerAcc {

    @Accessor
    EntityPersistentStorage<Entity> getPermanentStorage();
}
