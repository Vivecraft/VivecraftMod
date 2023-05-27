package org.vivecraft.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.common.utils.math.Quaternion;

public class CommonNetworkHelper {

    public static final ResourceLocation CHANNEL = new ResourceLocation("vivecraft:data");

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
        NEW_NETWORKING,
        VR_SWITCHING,
        IS_VR_ACTIVE,
        VR_PLAYER_STATE
    }

    public static void serializeF(FriendlyByteBuf buffer, Vec3 vec3) {
        buffer.writeFloat((float) vec3.x);
        buffer.writeFloat((float) vec3.y);
        buffer.writeFloat((float) vec3.z);
    }

    public static void serialize(FriendlyByteBuf buffer, Quaternion quat) {
        buffer.writeFloat(quat.w);
        buffer.writeFloat(quat.x);
        buffer.writeFloat(quat.y);
        buffer.writeFloat(quat.z);
    }

    public static Vec3 deserializeFVec3(FriendlyByteBuf buffer) {
        return new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    public static Quaternion deserializeVivecraftQuaternion(FriendlyByteBuf buffer) {
        return new Quaternion(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }
}
