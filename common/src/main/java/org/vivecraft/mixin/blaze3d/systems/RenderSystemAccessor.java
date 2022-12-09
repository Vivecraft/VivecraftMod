package org.vivecraft.mixin.blaze3d.systems;

import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderSystem.class)
public interface RenderSystemAccessor {

    // needs remap because of forge
    @Accessor
    public static int[] getShaderTextures(){
        return null;
    }

    @Accessor
    public static Vector3f[] getShaderLightDirections(){
        return null;
    }
}
