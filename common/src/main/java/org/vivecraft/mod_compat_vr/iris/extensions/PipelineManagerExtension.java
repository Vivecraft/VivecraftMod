package org.vivecraft.mod_compat_vr.iris.extensions;

import org.vivecraft.client_vr.render.RenderPass;

public interface PipelineManagerExtension {

    /**
     * @return the actually used ShadowRenderTarget
     */
    Object vivecraft$getShadowRenderTargets();

    /**
     * sets the actually used ShadowRenderTarget
     *
     * @param targets ShadowRenderTarget to set
     */
    void vivecraft$setShadowRenderTargets(Object targets);

    /**
     * needed for sodium terrain shaders, to get all pipelines, and not just the one from the current pass
     */
    Object vivecraft$getVRPipeline(RenderPass pass);

    /**
     * @return the pipeline used for the vanilla pass
     */
    Object vivecraft$getVanillaPipeline();
}
