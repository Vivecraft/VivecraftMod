package org.vivecraft.api_beta.data;

import com.google.common.annotations.Beta;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * Represents the pose data, such as position and rotation, for a given trackable object, such as the HMD or
 * a controller.
 */
@Beta
public interface VRPose {

    /**
     * @return The position of the device in Minecraft world coordinates.
     */
    @Beta
    Vec3 getPos();

    /**
     * @return The rotation of the device.
     */
    @Beta
    Vec3 getRot();

    /**
     * @return The pitch of the device in radians.
     */
    @Beta
    double getPitchRad();

    /**
     * @return The yaw of the device in radians.
     */
    @Beta
    double getYawRad();

    /**
     * @return The roll of the device in radians.
     */
    @Beta
    default double getRollRad() {
        return Math.toRadians(getRollDeg());
    }

    /**
     * @return The pitch of the device in degrees.
     */
    @Beta
    default double getPitchDeg() {
        return Math.toDegrees(getPitchRad());
    }

    /**
     * @return The yaw of the device in degrees.
     */
    @Beta
    default double getYawDeg() {
        return Math.toDegrees(getYawRad());
    }

    /**
     * @return The roll of the device in degrees.
     */
    @Beta
    double getRollDeg();

    /**
     * @return The quaternion representing the rotation of the device.
     */
    @Beta
    Quaternionf getQuaternion();
}
