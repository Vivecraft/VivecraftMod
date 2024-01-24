package org.vivecraft.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public class CommonNetworkHelper {

    public static final ResourceLocation CHANNEL = new ResourceLocation("vivecraft:data");

    // maximum supported network version
    public static final int MAX_SUPPORTED_NETWORK_VERSION = 0;
    // minimum supported network version
    public static final int MIN_SUPPORTED_NETWORK_VERSION = 0;

    public enum PacketDiscriminators {
        VERSION,
        REQUESTDATA,
        HEADDATA,
        CONTROLLER0DATA,
        CONTROLLER1DATA,
        WORLDSCALE,
        DRAW,
        MOVEMODE,
        UBERPACKET,
        TELEPORT,
        CLIMBING,
        SETTING_OVERRIDE,
        HEIGHT,
        ACTIVEHAND,
        CRAWL,
        NETWORK_VERSION,
        VR_SWITCHING,
        IS_VR_ACTIVE,
        VR_PLAYER_STATE
    }

    public static void serializeF(FriendlyByteBuf buffer, Vec3 vec3) {
        buffer.writeFloat((float) vec3.x);
        buffer.writeFloat((float) vec3.y);
        buffer.writeFloat((float) vec3.z);
    }

    public static void serialize(FriendlyByteBuf buffer, Quaternionfc quat) {
        buffer.writeFloat(quat.w());
        buffer.writeFloat(quat.x());
        buffer.writeFloat(quat.y());
        buffer.writeFloat(quat.z());
    }

    public static Vec3 deserializeFVec3(FriendlyByteBuf buffer) {
        return new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    public static Quaternionf deserializeQuaternionf(FriendlyByteBuf buffer) {
        Quaternionf q = new Quaternionf();
        q.w = buffer.readFloat();
        q.x = buffer.readFloat();
        q.y = buffer.readFloat();
        q.z = buffer.readFloat();
        return q;
    }
}
