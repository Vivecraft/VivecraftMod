package org.vivecraft.mod_compat_vr.sodium.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vivecraft.client_xr.render_pass.RenderPassType;

/**
 * always update chunk graph in VR to prevent missing chunks
 */
@Pseudo
@Mixin(targets = {
    "me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer",
    "net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer"
})
public class SodiumWorldRendererVRMixin {

    @Group(name = "forceChunkUpdate", min = 1, max = 1)
    @ModifyVariable(method = "updateChunks", at = @At("STORE"), ordinal = 1, remap = false, expect = 0)
    private boolean vivecraft$RenderUpdate(boolean dirty) {
        return !RenderPassType.isVanilla() || dirty;
    }

    @Group(name = "forceChunkUpdate", min = 1, max = 1)
    @ModifyVariable(method = "setupTerrain", at = @At("STORE"), ordinal = 2, remap = false, expect = 0)
    private boolean vivecraft$RenderUpdateSodium5(boolean dirty) {
        return !RenderPassType.isVanilla() || dirty;
    }
}
