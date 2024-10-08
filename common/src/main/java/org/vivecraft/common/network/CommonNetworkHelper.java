package org.vivecraft.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.common.utils.math.Quaternion;

public class CommonNetworkHelper {

    public static final ResourceLocation CHANNEL = new ResourceLocation("vivecraft:data");

    // maximum supported network version
    public static final int MAX_SUPPORTED_NETWORK_VERSION = 0;
    // minimum supported network version
    public static final int MIN_SUPPORTED_NETWORK_VERSION = 0;

    public static void serializeF(FriendlyByteBuf buffer, Vec3 vec3) {
        buffer.writeFloat((float) vec3.x);
        buffer.writeFloat((float) vec3.y);
        buffer.writeFloat((float) vec3.z);
    }

    public static Vec3 deserializeFVec3(FriendlyByteBuf buffer) {
        return new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    public static void serialize(FriendlyByteBuf buffer, Quaternion quat) {
        buffer.writeFloat(quat.w);
        buffer.writeFloat(quat.x);
        buffer.writeFloat(quat.y);
        buffer.writeFloat(quat.z);
    }

    public static Quaternion deserializeVivecraftQuaternion(FriendlyByteBuf buffer) {
        return new Quaternion(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }
}
