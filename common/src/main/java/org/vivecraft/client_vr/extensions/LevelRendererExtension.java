package org.vivecraft.client_vr.extensions;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.level.CameraRenderState;

import javax.annotation.Nullable;

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

    /**
     * @return the rendertarget to render the VR hands into
     */
    @Nullable
    RenderTarget vivecraft$getHandsTarget();

    /**
     * @return the rendertarget to render VR occluded things into
     */
    @Nullable
    RenderTarget vivecraft$getVrOccludedTarget();

    /**
     * @return the rendertarget to render VR unoccluded things into
     */
    @Nullable
    RenderTarget vivecraft$getVrUnoccludedTarget();
}
