package org.vivecraft.common.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * sends a haptic event to the client to trigger on the given BodyPart
 *
 * @param bodyPart  VRBodyPart to trigger on
 * @param duration  duration in seconds
 * @param frequency frequency in Hz
 * @param amplitude amplitude, 0-1
 * @param delay     delay in seconds
 */
public record HapticPayloadS2C(VRBodyPart bodyPart, float duration, float frequency, float amplitude,
                               float delay) implements VivecraftPayloadS2C
{

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.HAPTIC;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeByte(this.bodyPart.ordinal());
        buffer.writeFloat(this.duration);
        buffer.writeFloat(this.frequency);
        buffer.writeFloat(this.amplitude);
        buffer.writeFloat(this.delay);
    }

    public static HapticPayloadS2C read(FriendlyByteBuf buffer) {
        return new HapticPayloadS2C(
            VRBodyPart.values()[buffer.readByte()],
            buffer.readFloat(),
            buffer.readFloat(),
            buffer.readFloat(),
            buffer.readFloat());
    }
}
