package org.vivecraft.extensions.iris;

import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.shadows.ShadowRenderTargets;
import org.vivecraft.render.RenderPass;

public interface PipelineManagerExtension {

    ShadowRenderTargets getShadowRenderTargets();
    void setShadowRenderTargets(ShadowRenderTargets targets);

    // needed for sodium terrain shaders, to get all pipelines, and not just the one from the current pass
    WorldRenderingPipeline getVRPipeline(RenderPass pass);

}
