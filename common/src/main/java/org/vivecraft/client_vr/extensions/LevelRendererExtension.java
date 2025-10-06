package org.vivecraft.client_vr.extensions;

import net.minecraft.world.entity.Entity;

public interface LevelRendererExtension {
    /**
     * @return which entity is currently being rendered, {@code null} if there is none
     */
    Entity vivecraft$getRenderedEntity();
}
