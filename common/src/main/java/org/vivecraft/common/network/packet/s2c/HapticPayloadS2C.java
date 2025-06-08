package org.vivecraft.common.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * sends a haptic request to the player
 *
 * @param controllerType of the controller to trigger on
 * @param durationSeconds duration in seconds
 * @param frequency       frequency in Hz
 * @param amplitude       strength 0.0 - 1.0
 * @param delaySeconds    delay for when to trigger in seconds
 */
public record HapticPayloadS2C(ControllerType controllerType, float durationSeconds, float frequency, float amplitude, float delaySeconds) implements VivecraftPayloadS2C
{

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.HAPTIC;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeEnum(this.controllerType);
        buffer.writeFloat(this.durationSeconds);
        buffer.writeFloat(this.frequency);
        buffer.writeFloat(this.amplitude);
        buffer.writeFloat(this.delaySeconds);
    }

    public static HapticPayloadS2C read(FriendlyByteBuf buffer) {
        return new HapticPayloadS2C(
            buffer.readEnum(ControllerType.class),
            buffer.readFloat(),
            buffer.readFloat(),
            buffer.readFloat(),
            buffer.readFloat());
    }
}
