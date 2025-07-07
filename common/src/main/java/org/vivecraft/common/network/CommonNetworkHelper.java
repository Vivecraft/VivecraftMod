package org.vivecraft.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class CommonNetworkHelper {

    public static final ResourceLocation CHANNEL = ResourceLocation.parse("vivecraft:data");

    public static final int NETWORK_VERSION_LEGACY = -1;
    // adds full body tracker data
    public static final int NETWORK_VERSION_FBT = 1;
    // adds dual wielding packet and server logic
    public static final int NETWORK_VERSION_DUAL_WIELDING = 2;
    // adds the head as a valid active BodyPart, and adds a useForAim flag
    public static final int NETWORK_VERSION_HEAD_AIM = 3;
    // allows sending haptic events to the client
    public static final int NETWORK_VERSION_HAPTIC_PACKET = 4;
    // adds a packet, to inform the client what vr changes are on non default values
    public static final int NETWORK_VERSION_SERVER_VR_CHANGES = 5;
    // adds packets to send/receive damage directions
    public static final int NETWORK_VERSION_DAMAGE_DIRECTION = 6;

    // maximum supported network version
    public static final int MAX_SUPPORTED_NETWORK_VERSION = NETWORK_VERSION_DAMAGE_DIRECTION;
    // minimum supported network version
    public static final int MIN_SUPPORTED_NETWORK_VERSION = 0;

    public static void serializeF(FriendlyByteBuf buffer, Vector3fc vec3) {
        buffer.writeFloat(vec3.x());
        buffer.writeFloat(vec3.y());
        buffer.writeFloat(vec3.z());
    }

    public static Vector3fc deserializeFVec3(FriendlyByteBuf buffer) {
        return new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    public static void serialize(FriendlyByteBuf buffer, Quaternionfc quat) {
        buffer.writeFloat(quat.w());
        buffer.writeFloat(quat.x());
        buffer.writeFloat(quat.y());
        buffer.writeFloat(quat.z());
    }

    public static Quaternionf deserializeVivecraftQuaternion(FriendlyByteBuf buffer) {
        float w = buffer.readFloat();
        return new Quaternionf(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), w);
    }
}
