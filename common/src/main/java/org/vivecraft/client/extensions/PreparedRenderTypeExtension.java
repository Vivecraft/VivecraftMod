package org.vivecraft.client.extensions;

import net.minecraft.client.renderer.fog.FogRenderer;

public interface PreparedRenderTypeExtension {

    /**
     * sets a Fog override, to render things without fog mid level
     *
     * @param fogOverride FogMode to use as override
     */
    void vivecraft$setFogOverride(FogRenderer.FogMode fogOverride);

    /**
     * sets a Fog override, to render things without fog mid level
     */
    void vivecraft$setUndistorted();
}
