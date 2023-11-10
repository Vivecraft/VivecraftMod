package org.vivecraft.mod_compat_vr.sodium.mixin;

import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vivecraft.client_xr.render_pass.RenderPassType;


@Pseudo
@Mixin(SodiumWorldRenderer.class)
public class SodiumWorldRendererVRMixin {

    @Group(name = "forceChunkUpdate", min = 1, max = 1)
    @ModifyVariable(at = @At("STORE"), ordinal = 1, method = "updateChunks", expect = 0)
    public boolean vivecraft$RenderUpdate(boolean b) {
        // always update chunk graph in VR to prevent missing chunks
        return !RenderPassType.isVanilla() || b;
    }

    @Group(name = "forceChunkUpdate", min = 1, max = 1)
    @ModifyVariable(at = @At("STORE"), ordinal = 2, method = "setupTerrain", expect = 0)
    public boolean vivecraft$RenderUpdateSodium5(boolean b) {
        // always update chunk graph in VR to prevent missing chunks
        return !RenderPassType.isVanilla() || b;
    }
}
