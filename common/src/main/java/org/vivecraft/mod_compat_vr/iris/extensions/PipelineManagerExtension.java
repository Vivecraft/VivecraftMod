package org.vivecraft.mod_compat_vr.iris.extensions;

import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.shadows.ShadowRenderTargets;
import org.vivecraft.client_vr.render.RenderPass;

public interface PipelineManagerExtension {

    ShadowRenderTargets vivecraft$getShadowRenderTargets();

    void vivecraft$setShadowRenderTargets(ShadowRenderTargets targets);

    // needed for sodium terrain shaders, to get all pipelines, and not just the one from the current pass
    WorldRenderingPipeline vivecraft$getVRPipeline(RenderPass pass);

    WorldRenderingPipeline vivecraft$getVanillaPipeline();
}
