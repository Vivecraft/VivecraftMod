package org.vivecraft.client_vr.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;

public interface LevelRendererExtension {
    /**
     * renders the currently submitted Gizmos, this should only be called when the level is not rendered
     *
     * @param poseStack       PoseStack for transforming
     * @param cameraState     camera state
     * @param modelViewMatrix current modelview matrix
     */
    void vivecraft$renderGizmos(PoseStack poseStack, CameraRenderState cameraState, Matrix4fc modelViewMatrix);
}
