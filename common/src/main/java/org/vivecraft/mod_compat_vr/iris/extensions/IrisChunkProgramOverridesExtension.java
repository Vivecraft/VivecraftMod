package org.vivecraft.mod_compat_vr.iris.extensions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface IrisChunkProgramOverridesExtension {

    void vivecraft$createAllPipelinesShadersSodiumProcessing(Object sodiumTerrainPipeline, Object chunkVertexType, Method createShaders) throws InvocationTargetException, IllegalAccessException;
}
