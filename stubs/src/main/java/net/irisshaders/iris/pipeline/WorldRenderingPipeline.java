package net.irisshaders.iris.pipeline;

public interface WorldRenderingPipeline {
    void destroy();

    SodiumTerrainPipeline getSodiumTerrainPipeline();
}
