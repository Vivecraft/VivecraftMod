package org.vivecraft.modCompatMixin.sodiumMixin;

import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;


@Pseudo
@Mixin(SodiumWorldRenderer.class)
public class SodiumWorldRendererVRMixin {

    @ModifyVariable(at = @At("STORE"), ordinal = 1, method = "updateChunks")
    public boolean RenderUpdate(boolean b) {
        return true;
    }

}
