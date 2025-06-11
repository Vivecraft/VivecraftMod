package org.vivecraft.mixin.client.renderer.entity.layers;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.vivecraft.client.extensions.RenderLayerExtension;

/**
 * part of the hacky way, to copy RenderLayers from the regular PlayerRenderer, to the VRPlayerRenderer
 */

@Mixin(RenderLayer.class)
public class RenderLayerMixin<S extends EntityRenderState, M extends EntityModel<? super S>> implements Cloneable, RenderLayerExtension {
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
