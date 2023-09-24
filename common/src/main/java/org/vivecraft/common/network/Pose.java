package org.vivecraft.common.network;

import org.joml.Quaternionf;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public record Pose(Vec3 position, Quaternionf orientation) {

    public static Pose deserialize(FriendlyByteBuf byteBuf) {
        return new Pose(CommonNetworkHelper.deserializeFVec3(byteBuf), CommonNetworkHelper.deserializeVivecraftQuaternion(byteBuf));
    }

    public void serialize(FriendlyByteBuf buffer) {
        CommonNetworkHelper.serializeF(buffer, this.position);
        CommonNetworkHelper.serialize(buffer, this.orientation);
    }
}
