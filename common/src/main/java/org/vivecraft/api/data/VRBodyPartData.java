package org.vivecraft.api.data;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

/**
 * Represents the data for a body part, or a device usually tied to a body part in VR, such as the HMD or a
 * controller.
 *
 * @since 1.3.0
 */
public interface VRBodyPartData {

    /**
     * Gets the world space position for this body part.
     *
     * @return The position of this body part in Minecraft world coordinates.
     * @since 1.3.0
     */
    Vec3 getPos();

    /**
     * Gets the forward direction this body part is facing.
     *
     * @return The forward direction of this body part.
     * @since 1.3.0
     */
    Vec3 getDir();

    /**
     * Gets the pitch of this body part.
     *
     * @return The pitch of this body part in radians.
     * @since 1.3.0
     */
    double getPitch();

    /**
     * Gets the yaw of this body part.
     *
     * @return The yaw of this body part in radians.
     * @since 1.3.0
     */
    double getYaw();

    /**
     * Gets the roll of this body part.
     *
     * @return The roll of this body part in radians.
     * @since 1.3.0
     */
    double getRoll();

    /**
     * Gets the quaternion representing the rotation of this body part.
     *
     * @return The quaternion representing the rotation of this body part.
     * @since 1.3.0
     */
    Quaternionfc getRotation();
}
