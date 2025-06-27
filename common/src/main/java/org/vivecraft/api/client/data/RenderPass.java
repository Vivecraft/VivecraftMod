package org.vivecraft.api.client.data;

import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;

/**
 * A pass used to render things. What is rendered during a RenderPass depends on the pass, most are RenderPass that
 * render the whole level, others, like the {@link RenderPass#GUI} pass only render the Gui/Hud.
 * <br>
 * More passes may be added in the future.
 *
 * @since 1.3.0
 */
public enum RenderPass {
    /**
     * Renders the level from the view of the Left eye
     *
     * @since 1.3.0
     */
    LEFT,
    /**
     * Renders the level from the view of the Right eye
     *
     * @since 1.3.0
     */
    RIGHT,
    /**
     * Renders the level from the view of the First-Person Mirror
     *
     * @since 1.3.0
     */
    CENTER,
    /**
     * Renders the level from the view of the Third-Person Mirror
     *
     * @since 1.3.0
     */
    THIRD,
    /**
     * Renders the Gui/Hud to a RenderTarget to be rendered in the world in other passes.
     *
     * @since 1.3.0
     */
    GUI,
    /**
     * Renders the level from the view of the Spyglass, when held in the main-hand
     *
     * @since 1.3.0
     */
    SCOPER,
    /**
     * Renders the level from the view of the Spyglass, when held in the off-hand
     *
     * @since 1.3.0
     */
    SCOPEL,
    /**
     * Renders the level from the view of the placeable Screenshot Camera
     *
     * @since 1.3.0
     */
    CAMERA,
    /**
     * Blits the mirror to the desktop screen
     *
     * @since 1.3.0
     */
    MIRROR;

    /**
     * Returns whether the provided RenderPass is rendered from the player's perspective.
     *
     * @param pass The RenderPass in question.
     * @return Whether the provided RenderPass is from the player's first-person perspective.
     */
    public static boolean isFirstPerson(RenderPass pass) {
        return pass == LEFT || pass == RIGHT || pass == CENTER;
    }

    /**
     * Returns whether the provided RenderPass is rendered from a third-person perspective.
     *
     * @param pass The RenderPass in question.
     * @return Whether the provided RenderPass is from a third-person perspective relative to the player.
     */
    public static boolean isThirdPerson(RenderPass pass) {
        return pass == THIRD || pass == CAMERA;
    }

    /**
     * Returns whether the RenderPass is supposed to render the player model.
     *
     * @param pass The RenderPass in question.
     * @return Whether the provided RenderPass is supposed to render the player.
     */
    public static boolean renderPlayer(RenderPass pass) {
        return pass == CAMERA ||
            (isFirstPerson(pass) && ClientDataHolderVR.getInstance().vrSettings.shouldRenderSelf) || (pass == THIRD &&
            ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON
        );
    }
}
