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
     * @return The position of the body part in Minecraft world coordinates.
     * @since 1.3.0
     */
    Vec3 getPos();

    /**
     * @return The forward direction of the body part.
     * @since 1.3.0
     */
    Vec3 getDir();

    /**
     * @return The pitch of the body part in radians.
     * @since 1.3.0
     */
    double getPitch();

    /**
     * @return The yaw of the body part in radians.
     * @since 1.3.0
     */
    double getYaw();

    /**
     * @return The roll of the body part in radians.
     * @since 1.3.0
     */
    double getRoll();

    /**
     * @return The quaternion representing the rotation of the body part.
     * @since 1.3.0
     */
    Quaternionfc getRotation();
}
