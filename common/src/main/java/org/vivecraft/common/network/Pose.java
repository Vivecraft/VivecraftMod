package org.vivecraft.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public record Pose(Vec3 position, Quaternionf orientation) {

    public static Pose deserialize(FriendlyByteBuf byteBuf) {
        return new Pose(CommonNetworkHelper.deserializeFVec3(byteBuf), CommonNetworkHelper.deserializeQuaternionf(byteBuf));
    }

    public void serialize(FriendlyByteBuf buffer) {
        CommonNetworkHelper.serializeF(buffer, this.position);
        CommonNetworkHelper.serialize(buffer, this.orientation);
    }
}
