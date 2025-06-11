package org.vivecraft.mixin.client.blaze3d;

import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderSystem.class)
public interface RenderSystemAccessor {

    /**
     * @return ShaderTextures id array, used to get the actual size of the array
     */
    @Accessor
    static int[] getShaderTextures() {
        return null;
    }

    /**
     * @return current light vectors used for entity shading, used to restore the lights after setting custom lights
     */
    @Accessor
    static Vector3f[] getShaderLightDirections() {
        return null;
    }
}
