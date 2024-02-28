package org.vivecraft.mod_compat_vr.iris.extensions;

import org.vivecraft.client_vr.render.RenderPass;

public interface PipelineManagerExtension {

    Object vivecraft$getShadowRenderTargets();

    void vivecraft$setShadowRenderTargets(Object targets);

    // needed for sodium terrain shaders, to get all pipelines, and not just the one from the current pass
    Object vivecraft$getVRPipeline(RenderPass pass);

    Object vivecraft$getVanillaPipeline();
}
