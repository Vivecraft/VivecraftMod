package net.coderbot.iris.pipeline;

public interface WorldRenderingPipeline {
    void destroy();

    SodiumTerrainPipeline getSodiumTerrainPipeline();
}
