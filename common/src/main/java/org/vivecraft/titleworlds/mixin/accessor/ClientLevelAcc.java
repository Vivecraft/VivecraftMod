package org.vivecraft.titleworlds.mixin.accessor;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ClientLevel.class)
public interface ClientLevelAcc {

    @Accessor
    TransientEntitySectionManager<Entity> getEntityStorage();

    @Accessor
    Map<String, MapItemSavedData> getMapData();
}
