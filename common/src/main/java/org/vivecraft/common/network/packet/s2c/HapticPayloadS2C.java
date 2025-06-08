package org.vivecraft.common.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * sends a haptic request to the player
 *
 * @param controllerByte  id of the controller to trigger on
 * @param durationSeconds duration in seconds
 * @param frequency       frequency in Hz
 * @param amplitude       strength 0.0 - 1.0
 * @param delaySeconds    delay for when to trigger in seconds
 */
public record HapticPayloadS2C(byte controllerByte, float durationSeconds, float frequency, float amplitude, float delaySeconds) implements VivecraftPayloadS2C
{

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.HAPTIC;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeByte(this.controllerByte);
        buffer.writeFloat(this.durationSeconds);
        buffer.writeFloat(this.frequency);
        buffer.writeFloat(this.amplitude);
        buffer.writeFloat(this.delaySeconds);
    }

    public static HapticPayloadS2C read(FriendlyByteBuf buffer) {
        byte controllerByte = buffer.readByte();

        float durationSeconds = buffer.readFloat();
        float frequency = buffer.readFloat();
        float amplitude = buffer.readFloat();
        float delaySeconds = buffer.readFloat();

        // I hate this implementation, there has to be a better way
        ControllerType controllerType = switch (controllerByte) {
            case 0:  yield ControllerType.RIGHT;
            case 1:  yield ControllerType.LEFT;
            default: yield null;
        };

        MCVR mcvr = MCVR.get();
        if (controllerType != null) mcvr.triggerHapticPulse(controllerType, durationSeconds, frequency, amplitude, delaySeconds);
        else VRSettings.LOGGER.error("Vivecraft: Got unexpected controller identifier on client: {}", controllerByte);

        return new HapticPayloadS2C(
            controllerByte,
            durationSeconds,
            frequency,
            amplitude,
            delaySeconds);
    }
}
