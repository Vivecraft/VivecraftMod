package org.vivecraft.api.data;

import com.google.common.annotations.Beta;
import net.minecraft.world.InteractionHand;

/**
 * Represents all VR data associated with a given player, mainly the pose of the HMD and both controllers
 * of the player.
 */
public interface VRData {

    /**
     * @return Pose data for the HMD.
     */
    VRPose getHMD();

    /**
     * Gets the pose data for a given controller.
     *
     * @param controller The controller number to get, with 0 being the primary controller.
     * @return The specified controller's pose data.
     */
    VRPose getController(int controller);

    /**
     * @return Whether the player is currently in seated mode.
     */
    boolean isSeated();

    /**
     * @return Whether the player is using reversed hands.
     */
    boolean usingReversedHands();

    /**
     * Gets the pose for a given controller.
     *
     * @param hand The interaction hand to get controller data for.
     * @return The specified controller's pose data.
     */
    default VRPose getController(InteractionHand hand) {
        return getController(hand.ordinal());
    }

    /**
     * Gets the pose for the primary controller.
     *
     * @return The main controller's pose data.
     */
    default VRPose getController0() {
        return getController(0);
    }

    /**
     * Gets the pose for the secondary controller.
     *
     * @return The main controller's pose data.
     */
    default VRPose getController1() {
        return getController(1);
    }
}
