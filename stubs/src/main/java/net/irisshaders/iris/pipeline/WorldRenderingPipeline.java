package net.irisshaders.iris.pipeline;

public interface WorldRenderingPipeline {
    SodiumTerrainPipeline getSodiumTerrainPipeline();

    void destroy();
}
