package org.vivecraft.api.client.data;

import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;

/**
 * A "round" of rendering that is done to render a given frame fully in VR. More passes may be added in the future.
 *
 * @since 1.3.0
 */
public enum RenderPass {
    /**
     * The left eye
     * @since 1.3.0
     */
    LEFT,
    /**
     * The right eye
     * @since 1.3.0
     */
    RIGHT,
    CENTER,
    THIRD,
    GUI,
    SCOPER,
    SCOPEL,
    CAMERA,
    MIRROR;

    /**
     * Returns whether the provided render pass is a render pass that's done from the player's first-person perspective.
     *
     * @param pass The RenderPass in question.
     * @return Whether the provided RenderPass is from the player's first-person perspective.
     */
    public static boolean isFirstPerson(RenderPass pass) {
        return pass == LEFT || pass == RIGHT || pass == CENTER;
    }

    /**
     * Returns whether the provided render pass is a render pass that's done from a third-person perspective.
     *
     * @param pass The RenderPass in question.
     * @return Whether the provided RenderPass is from a third-person perspective relative to the player.
     */
    public static boolean isThirdPerson(RenderPass pass) {
        return pass == THIRD || pass == CAMERA;
    }

    /**
     * Returns whether the render pass may render the player.
     *
     * @param pass The RenderPass in question.
     * @return Whether the provided RenderPass may render the player.
     */
    public static boolean renderPlayer(RenderPass pass) {
        return pass == CAMERA ||
            (isFirstPerson(pass) && ClientDataHolderVR.getInstance().vrSettings.shouldRenderSelf) || (pass == THIRD &&
            ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON
        );
    }
}
