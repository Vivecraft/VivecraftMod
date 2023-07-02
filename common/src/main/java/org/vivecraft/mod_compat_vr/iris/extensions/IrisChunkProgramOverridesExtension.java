package org.vivecraft.mod_compat_vr.iris.extensions;

import net.coderbot.iris.pipeline.SodiumTerrainPipeline;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface IrisChunkProgramOverridesExtension {

    void createAllPipelinesShadersSodiumProcessing(SodiumTerrainPipeline sodiumTerrainPipeline, Object chunkVertexType, Method createShaders) throws InvocationTargetException, IllegalAccessException;

}
