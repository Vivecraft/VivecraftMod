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
     * Gets the body part pose data for the head.
     *
     * @return Body part pose data for the head.
     * @since 1.3.0
     */
    default VRBodyPartData getHead() {
        return getBodyPartData(VRBodyPart.HEAD);
    }

    /**
     * Gets whether the player was in seated mode when the pose was created.
     *
     * @return Whether the player was in seated mode when the pose was created.
     * @since 1.3.0
     */
    boolean isSeated();

    /**
     * Gets whether the player was using left-handed mode when the pose was created.
     *
     * @return Whether the player was using left-handed mode when the pose was created.
     * @since 1.3.0
     */
    boolean isLeftHanded();

    /**
     * Gets the full-body tracking mode the player was using when the pose was created.
     *
     * @return The full-body tracking mode the player was using when the pose was created.
     * @since 1.3.0
     */
    FBTMode getFBTMode();

    /**
     * Gets the body part data for a given hand.
     *
     * @param hand The interaction hand to get hand data for.
     * @return The specified hand's body part data.
     * @since 1.3.0
     */
    default VRBodyPartData getHand(InteractionHand hand) {
        return getBodyPartData(VRBodyPart.fromInteractionHand(hand));
    }

    /**
     * Gets the body part data for the main-hand.
     *
     * @return The main-hand's body part data.
     * @since 1.3.0
     */
    default VRBodyPartData getMainHand() {
        return getBodyPartData(VRBodyPart.MAIN_HAND);
    }

    /**
     * Gets the body part data for the off-hand.
     *
     * @return The off-hand's body part data.
     * @since 1.3.0
     */
    default VRBodyPartData getOffHand() {
        return getBodyPartData(VRBodyPart.OFF_HAND);
    }
}
