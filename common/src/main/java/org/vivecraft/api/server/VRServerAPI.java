package org.vivecraft.api.server;

import net.minecraft.server.level.ServerPlayer;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.server.api_impl.VRServerAPIImpl;

/**
 * The main interface for interacting with Vivecraft from server code.
 *
 * @since 1.3.0
 */
public interface VRServerAPI {

    /**
     * Gets API instance for interacting with Vivecraft's server API
     *
     * @return The Vivecraft API instance for interacting with Vivecraft's server API.
     * @since 1.3.0
     */
    static VRServerAPI instance() {
        return VRServerAPIImpl.INSTANCE;
    }

    /**
     * Sends a haptic pulse (vibration/rumble) for the specified VRBodyPart, if possible, to the given player.
     * To directly trigger a haptic pulse for the local player, use {@link VRClientAPI#triggerHapticPulse}
     * This function silently fails if called for players not in VR or players who are in seated mode.
     *
     * @param player    Player to send the haptic pulse to.
     * @param bodyPart  The VRBodyPart to trigger a haptic pulse on.
     * @param duration  The duration of the haptic pulse in seconds. Note that this number is passed to the
     *                  underlying VR API used by Vivecraft, and may act with a shorter length than expected beyond
     *                  very short pulses.
     * @param frequency The frequency of the haptic pulse in Hz. (might be ignored if the targeted device doesn't support it)
     *                  <br>
     *                  160 Hz is a safe bet for this number, with Vivecraft's codebase
     *                  using anywhere from 160 Hz for actions such as a bite on a fishing line, to 1000 Hz for things such
     *                  as a chat notification.
     * @param amplitude The amplitude of the haptic pulse. This should be kept between 0 and 1.
     * @param delay     An amount of time to delay until creating the haptic pulse. The majority of the time, one should use 0 here. This starts counting when the client receives the packet.
     * @since 1.3.0
     */
    void sendHapticPulse(
        ServerPlayer player, VRBodyPart bodyPart, float duration, float frequency, float amplitude, float delay);

    /**
     * Sends a haptic pulse (vibration/rumble) at full strength with 160 Hz for the specified VRBodyPart, if possible, to the given player.
     * <br>
     * If one wants more control over the used parameters one should use {@link #sendHapticPulse(ServerPlayer, VRBodyPart, float, float, float, float)} instead.
     * <br>
     * To directly trigger a haptic pulse for the local player, use {@link VRClientAPI#triggerHapticPulse}
     * This function silently fails if called for players not in VR or players who are in seated mode.
     *
     * @param player   Player to send the haptic pulse to.
     * @param bodyPart The VRBodyPart to trigger a haptic pulse on.
     * @param duration The duration of the haptic pulse in seconds. Note that this number is passed to the
     *                 underlying VR API used by Vivecraft, and may act with a shorter length than expected beyond
     *                 very short pulses.
     * @since 1.3.0
     */
    default void sendHapticPulse(ServerPlayer player, VRBodyPart bodyPart, float duration) {
        sendHapticPulse(player, bodyPart, duration, 160F, 1F, 0F);
    }
}
