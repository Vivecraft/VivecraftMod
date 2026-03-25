package org.vivecraft.mixin.client_vr.renderer.item;

import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.vivecraft.client_vr.extensions.BlockModelWrapperExtension;

@Mixin(CuboidItemModelWrapper.class)
public class BlockModelWrapperVRMixin implements BlockModelWrapperExtension {

    @Unique
    private boolean vivecraft$generated;

    @Override
    public void vivecraft$setGenerated(boolean generated) {
        this.vivecraft$generated = generated;
    }

    @Override
    public boolean vivecraft$isGenerated() {
        return this.vivecraft$generated;
    }
}
