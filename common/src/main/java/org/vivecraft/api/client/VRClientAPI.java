package org.vivecraft.api.client;

import org.vivecraft.api.VRAPI;
import org.vivecraft.api.client.data.CloseKeyboardContext;
import org.vivecraft.api.client.data.OpenKeyboardContext;
import org.vivecraft.api.client.event.VivecraftClientRegistrationEvent;
import org.vivecraft.api.data.FBTMode;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;
import org.vivecraft.client.api_impl.VRClientAPIImpl;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * The main interface for interacting with the local player using Vivecraft from client code. For rendering, one should
 * use {@link VRRenderingAPI}.
 *
 * @since 1.3.0
 */
public interface VRClientAPI {

    /**
     * Gets the API instance for interacting with Vivecraft from the client-side, for interactions not related to
     * rendering.
     *
     * @return The Vivecraft API instance for interacting with Vivecraft's client API.
     * @since 1.3.0
     */
    static VRClientAPI instance() {
        return VRClientAPIImpl.INSTANCE;
    }

    /**
     * Registers a handler, which consumes a {@link VivecraftClientRegistrationEvent}.
     * With this one can register custom  {@link Tracker} and {@link InteractModule} for the local player.
     * <br>
     * Needs to be called before the game loop starts.
     *
     * @param handler handler to add.
     * @throws IllegalStateException When called after the handlers were already processed.
     * @since 1.3.0
     */
    void addClientRegistrationHandler(Consumer<VivecraftClientRegistrationEvent> handler) throws IllegalStateException;

    /**
     * Get whether VR support is currently initialized. This is NOT the same as whether the local player is actively in
     * VR, which can instead be checked with {@link #isVRActive()}.
     *
     * @return Whether VR support is initialized.
     * @since 1.3.0
     */
    boolean isVRInitialized();

    /**
     * Get whether the client is actively in VR.
     *
     * @return Whether the client is actively in VR.
     * @since 1.3.0
     */
    boolean isVRActive();

    /**
     * Gets the VR pose representing the player in the room after the most recent poll of VR hardware.
     *
     * @return The most up-to-date VR pose representing the player in the room, or {@code null} if the local player isn't in VR.
     * @since 1.3.0
     */
    @Nullable
    VRPose getLatestRoomPose();

    /**
     * Gets the VR pose representing the player in the room after the game tick.
     * Note that this pose is gathered AFTER mod loaders' post-tick events.
     *
     * @return The VR pose representing the player in the room post-tick, or {@code null} if the local player isn't in VR.
     * @since 1.3.0
     */
    @Nullable
    VRPose getPostTickRoomPose();

    /**
     * Gets the VR pose representing the player in Minecraft world coordinates before the game tick. If you're unsure
     * which {@link VRPose} method to use, you very likely want to use this one.
     * Note that this pose is gathered BEFORE mod loaders' pre-tick events.
     *
     * @return The VR pose representing the player in world space pre-tick, or {@code null} if the local player isn't in VR.
     * @since 1.3.0
     */
    @Nullable
    VRPose getPreTickWorldPose();

    /**
     * Gets the VR pose representing the player in Minecraft world coordinates after the game tick.
     * This is the pose sent to the server, and also used to calculate the pose in {@link #getWorldRenderPose()}.
     * Note that this pose is gathered AFTER mod loaders' post-tick events.
     *
     * @return The VR pose representing the player in Minecraft space post-tick, or {@code null} if the local player isn't in VR.
     * @since 1.3.0
     */
    @Nullable
    VRPose getPostTickWorldPose();

    /**
     * Gets the VR pose representing the player in Minecraft world coordinates interpolated for rendering.
     *
     * @return The VR pose representing the player in Minecraft space post-tick interpolated for rendering, or {@code null} if
     * the local player isn't in VR.
     * @since 1.3.0
     */
    @Nullable
    VRPose getWorldRenderPose();

    /**
     * Returns the history of VR poses for the local player. If one wants historical VR poses for other players and/or
     * on the server, use {@link VRAPI#getHistoricalVRPoses(net.minecraft.world.entity.player.Player)} instead.
     *
     * @return The history of VR poses for the player. Will be {@code null} if the player isn't in VR or no pose data
     * is available yet.
     * @since 1.3.0
     */
    @Nullable
    VRPoseHistory getHistoricalVRPoses();

    /**
     * Causes a haptic pulse (vibration/rumble) for the specified VRBodyPart, if possible.
     * This function silently fails if called for players not in VR or players who are in seated mode.
     *
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
     * @param delay     An amount of time to delay until creating the haptic pulse. The majority of the time, one should use 0 here.
     * @since 1.3.0
     */
    void triggerHapticPulse(VRBodyPart bodyPart, float duration, float frequency, float amplitude, float delay);

    /**
     * Causes a haptic pulse (vibration/rumble) at full strength with 160 Hz for the specified VRBodyPart, if possible.
     * <br>
     * If one wants more control over the used parameters one should use {@link #triggerHapticPulse(VRBodyPart, float, float, float, float)} instead.
     * <br>
     * This function silently fails if called for players not in VR or players who are in seated mode.
     *
     * @param bodyPart The VRBodyPart to trigger a haptic pulse on.
     * @param duration The duration of the haptic pulse in seconds. Note that this number is passed to the
     *                 underlying VR API used by Vivecraft, and may act with a shorter length than expected beyond
     *                 very short pulses.
     * @since 1.3.0
     */
    default void triggerHapticPulse(VRBodyPart bodyPart, float duration) {
        triggerHapticPulse(bodyPart, duration, 160F, 1F, 0F);
    }

    /**
     * Get whether the local player is currently configured to be in seated mode when in VR.
     * This doesn't check if VR is active/enabled, it just checks the user setting.
     *
     * @return Whether the local player is currently in seated mode when in VR.
     * @since 1.3.0
     */
    boolean isSeated();

    /**
     * Get whether the local player is currently configured to use left-handed mode when in VR.
     * This doesn't check if VR is active/enabled, it just checks the user setting.
     *
     * @return Whether the local player is playing with left-handed controls when in VR.
     * @since 1.3.0
     */
    boolean isLeftHanded();

    /**
     * Gets the mode used for full-body tracking for the local player. Will return a sane default value if the
     * local player isn't in VR.
     *
     * @return The full-body tracking mode currently in-use or some default value if the local player is not in VR.
     * @since 1.3.0
     */
    FBTMode getFBTMode();

    /**
     * Get the currently active world scale.
     *
     * @return The currently active world scale.
     * @since 1.3.0
     */
    float getWorldScale();

    /**
     * Opens Vivecraft's keyboard, doing nothing if the keyboard is already opened or the provided
     * {@code openKeyboardContext} doesn't result in the keyboard opening. Will silently fail if the player isn't in VR.
     *
     * @param openKeyboardContext The context to use for opening the keyboard.
     * @return Whether the keyboard is showing after this method was called.
     * @since 1.3.0
     */
    boolean openKeyboard(OpenKeyboardContext openKeyboardContext);

    /**
     * Closes Vivecraft's keyboard, doing nothing if the keyboard is already opened or the provided
     * {@code closeKeyboardContext} doesn't result in the keyboard closing. Will silently fail if the player isn't in
     * VR.
     *
     * @param closeKeyboardContext The context to use for closing the keyboard.
     * @return Whether the keyboard is showing after this method was called.
     * @since 1.3.0
     */
    boolean closeKeyboard(CloseKeyboardContext closeKeyboardContext);
}
