package org.vivecraft.extensions.iris;

import net.coderbot.iris.shadows.ShadowRenderTargets;

public interface PipelineManagerExtension {

    ShadowRenderTargets getShadowRenderTargets();
    void setShadowRenderTargets(ShadowRenderTargets targets);

}
