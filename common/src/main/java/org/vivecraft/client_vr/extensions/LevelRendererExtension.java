package org.vivecraft.client_vr.extensions;

import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public interface LevelRendererExtension {
    /**
     * renders the currently submitted Gizmos, this should only be called when the level is not rendered
     *
     * @param cameraState camera state
     * @param output      SubmitNodeStorage to output to
     * @param dispatcher  FeatureRenderDispatcher to render with
     */
    void vivecraft$renderGizmos(
        CameraRenderState cameraState, SubmitNodeStorage output, FeatureRenderDispatcher dispatcher);
}
