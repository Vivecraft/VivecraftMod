package net.coderbot.iris.pipeline;

import java.util.function.Function;

public class PipelineManager {
    private WorldRenderingPipeline pipeline;
    private final Function<Object, WorldRenderingPipeline> pipelineFactory = null;

    private void resetTextureState() {
    }
}
