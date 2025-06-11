package org.vivecraft.mod_compat_vr.iris.extensions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface IrisChunkProgramOverridesExtension {

    /**
     * creates the sodium pipeline for each RenderPass
     * this is done this way, to try to avoid having to have a mixin for each sodium version
     *
     * @param sodiumTerrainPipeline original sodium pipeline that was provided
     * @param chunkVertexType       original chunkVertexType
     * @param createShaders         reflection Method to call to create the shader
     */
    void vivecraft$createAllPipelinesShadersSodiumProcessing(
        Object sodiumTerrainPipeline, Object chunkVertexType,
        Method createShaders) throws InvocationTargetException, IllegalAccessException;
}
