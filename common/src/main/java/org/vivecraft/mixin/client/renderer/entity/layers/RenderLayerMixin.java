package org.vivecraft.mixin.client.renderer.entity.layers;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.vivecraft.client.extensions.RenderLayerExtension;

/**
 * part of the hacky way, to copy RenderLayers from the regular PlayerRenderer, to the VRPlayerRenderer
 */

@Mixin(RenderLayer.class)
public class RenderLayerMixin<T extends Entity, M extends EntityModel<T>> implements Cloneable, RenderLayerExtension {
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
