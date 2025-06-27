package org.vivecraft.api.data;

import net.minecraft.world.InteractionHand;
import org.vivecraft.api.client.VRClientAPI;

/**
 * Corresponds to the different tracked device roles that are supported by Vivecraft.
 *
 * @since 1.3.0
 */
public enum VRBodyPart {
    /**
     * Main hand of the player, this is the hand the player points with. Which one that is can be identified with
     * {@link VRPose#isLeftHanded()} or {@link VRClientAPI#isLeftHanded()} for the local player
     *
     * @since 1.3.0
     */
    MAIN_HAND,
    OFF_HAND,
    RIGHT_FOOT,
    LEFT_FOOT,
    WAIST,
    RIGHT_KNEE,
    LEFT_KNEE,
    RIGHT_ELBOW,
    LEFT_ELBOW,
    /**
     * corresponds to the player's headset, so it is at their eye position
     *
     * @since 1.3.0
     */
    HEAD;

    /**
     * Gets the VRBodyPart which is the same type but on the opposite side of the body. VRBodyParts that don't have an
     * opposite counterpart will return itself.
     *
     * @return the opposite VRBodyPart
     * @since 1.3.0
     */
    public VRBodyPart opposite() {
        return switch (this) {
            case MAIN_HAND -> OFF_HAND;
            case OFF_HAND -> MAIN_HAND;
            case RIGHT_FOOT -> LEFT_FOOT;
            case LEFT_FOOT -> RIGHT_FOOT;
            case RIGHT_KNEE -> LEFT_KNEE;
            case LEFT_KNEE -> RIGHT_KNEE;
            case RIGHT_ELBOW -> LEFT_ELBOW;
            case LEFT_ELBOW -> RIGHT_ELBOW;
            default -> this;
        };
    }

    /**
     * Gets the corresponding VRBodyPart to the provided InteractionHand
     *
     * @param hand InteractionHand to convert
     * @return VRBodyPart that corresponds to the given InteractionHand
     * @since 1.3.0
     */
    public static VRBodyPart fromInteractionHand(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? MAIN_HAND : OFF_HAND;
    }

    /**
     * Whether this body part type is available in the provided full-body tracking mode.
     *
     * @param fbtMode The full-body tracking mode to check.
     * @return Whether this body part has available data in the provided mode.
     * @since 1.3.0
     */
    public boolean availableInMode(FBTMode fbtMode) {
        return switch (this) {
            case MAIN_HAND, OFF_HAND, HEAD -> true;
            case RIGHT_FOOT, LEFT_FOOT, WAIST -> fbtMode != FBTMode.ARMS_ONLY;
            case RIGHT_KNEE, LEFT_KNEE, RIGHT_ELBOW, LEFT_ELBOW -> fbtMode == FBTMode.WITH_JOINTS;
        };
    }

    /**
     * Checks if {@code this} VRBodyPart is a foot
     *
     * @return Whether this body part is a foot.
     * @since 1.3.0
     */
    public boolean isFoot() {
        return this == RIGHT_FOOT || this == LEFT_FOOT;
    }

    /**
     * Checks if {@code this} VRBodyPart is a hand
     *
     * @return Whether this body part is a hand.
     * @since 1.3.0
     */
    public boolean isHand() {
        return this == MAIN_HAND || this == OFF_HAND;
    }
}
