package org.vivecraft.mixin.client.renderer.state;

import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.vivecraft.client.extensions.FirstPersonHandsAndItemsStateExtension;
import org.vivecraft.client_vr.render.renderstates.FirstPersonHandsAdditions;

@Mixin(FirstPersonHandsAndItemsRenderState.class)
public class FirstPersonHandsAndItemsStateMixin implements FirstPersonHandsAndItemsStateExtension {

    @Unique
    private FirstPersonHandsAdditions vivecraft$additions;

    @Override
    public FirstPersonHandsAdditions vivecraft$getAdditions() {
        if (this.vivecraft$additions == null) {
            this.vivecraft$additions = new FirstPersonHandsAdditions();
        }
        return this.vivecraft$additions;
    }
}
