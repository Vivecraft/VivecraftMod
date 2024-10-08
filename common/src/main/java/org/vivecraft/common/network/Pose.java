package org.vivecraft.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.common.utils.math.Quaternion;

/**
 * holds a device Pose
 * @param position position of the device in player local space
 * @param orientation orientation of the device in world space
 */
public record Pose(Vec3 position, Quaternion orientation) {

    /**
     * @param buffer buffer to read from
     * @return a Pose read from the given {@code buffer}
     */
    public static Pose deserialize(FriendlyByteBuf buffer) {
        return new Pose(
            CommonNetworkHelper.deserializeFVec3(buffer),
            CommonNetworkHelper.deserializeVivecraftQuaternion(buffer)
        );
    }

    /**
     * writes this Pose to the given {@code buffer}
     * @param buffer buffer to write to
     */
    public void serialize(FriendlyByteBuf buffer) {
        CommonNetworkHelper.serializeF(buffer, this.position);
        CommonNetworkHelper.serialize(buffer, this.orientation);
    }
}
