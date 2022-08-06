package com.example.vivecraftfabric.sodiumMixin;

import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;


@Debug(print = true)
@Pseudo
@Mixin(SodiumWorldRenderer.class)
public class SodiumWorldRendererVRMixin {

    @ModifyVariable(at = @At("STORE"), ordinal = 0, method = "updateChunks", print = true)
    public boolean RenderUpdate(boolean b) {
        return true;
    }

}
