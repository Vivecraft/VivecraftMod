package org.vivecraft.api.data;

import net.minecraft.world.InteractionHand;

import javax.annotation.Nullable;

/**
 * Represents the pose of the VR player. In other words, the position and rotation data of all tracked body parts of
 * the VR player.
 *
 * @since 1.3.0
 */
public interface VRPose {

    /**
     * Gets the pose data for a body part.
     *
     * @param vrBodyPart The body part to get the pose data for.
     * @return The specified body part's pose data, or {@code null} if that body part is not available with the current FBTMode, which can be checked with {@link #getFBTMode}.
     * @since 1.3.0
     */
    @Nullable
    VRBodyPartData getBodyPartData(VRBodyPart vrBodyPart);

    /**
     * @return Body part pose data for the HMD.
     * @since 1.3.0
     */
    default VRBodyPartData getHMD() {
        return getBodyPartData(VRBodyPart.HEAD);
    }

    /**
     * @return Whether the player is currently in seated mode.
     * @since 1.3.0
     */
    boolean isSeated();

    /**
     * @return Whether the player is playing with left-handed controls.
     * @since 1.3.0
     */
    boolean isLeftHanded();

    /**
     * @return The full-body tracking mode currently in-use.
     * @since 1.3.0
     */
    FBTMode getFBTMode();

    /**
     * Gets the pose for a given hand.
     *
     * @param hand The interaction hand to get hand data for.
     * @return The specified hand's pose data.
     * @since 1.3.0
     */
    default VRBodyPartData getHand(InteractionHand hand) {
        return getBodyPartData(VRBodyPart.fromInteractionHand(hand));
    }

    /**
     * Gets the pose for the main-hand.
     *
     * @return The main-hand's pose data.
     * @since 1.3.0
     */
    default VRBodyPartData getMainHand() {
        return getBodyPartData(VRBodyPart.MAIN_HAND);
    }

    /**
     * Gets the pose for the off-hand.
     *
     * @return The off-hand's pose data.
     * @since 1.3.0
     */
    default VRBodyPartData getOffHand() {
        return getBodyPartData(VRBodyPart.OFF_HAND);
    }
}
