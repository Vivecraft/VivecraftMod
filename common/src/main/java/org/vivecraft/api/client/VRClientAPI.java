package org.vivecraft.api.client;

import org.vivecraft.api.VRAPI;
import org.vivecraft.api.data.FBTMode;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;
import org.vivecraft.client.api_impl.VRClientAPIImpl;

import javax.annotation.Nullable;

/**
 * The main interface for interacting with the local player using Vivecraft from client code. For rendering, one should use
 * {@link VRRenderingAPI}.
 */
public interface VRClientAPI {

    /**
     * @return The Vivecraft API instance for interacting with Vivecraft's client API.
     */
    static VRClientAPI instance() {
        return VRClientAPIImpl.INSTANCE;
    }

    /**
     * Registers the tracker to the list of all trackers to be run for the local player. See the documentation for
     * {@link Tracker} for more information on what a tracker is.
     *
     * @param tracker Tracker to register.
     */
    void registerTracker(Tracker tracker);

    /**
     * Registers the interact module to the list of all interact modules to be run for the local player.
     * See the documentation for {@link InteractModule} for more information on what an interact modules is.
     *
     * @param module InteractModule to register.
     */
    void registerInteractModule(InteractModule module);

    /**
     * Gets the VR pose representing the player in the room after the most recent poll of VR hardware.
     *
     * @return The most up-to-date VR pose representing the player in the room, or null if the local player isn't in VR.
     */
    @Nullable
    VRPose getLatestRoomPose();

    /**
     * Gets the VR pose representing the player in the room after the game tick.
     * Note that this pose is gathered AFTER mod loaders' post-tick events.
     *
     * @return The VR pose representing the player in the room post-tick, or null if the local player isn't in VR.
     */
    @Nullable
    VRPose getPostTickRoomPose();

    /**
     * Gets the VR pose representing the player in Minecraft world coordinates before the game tick. If you're unsure
     * which {@link VRPose} method to use, you very likely want to use this one.
     * Note that this pose is gathered BEFORE mod loaders' pre-tick events.
     *
     * @return The VR pose representing the player in world space pre-tick, or null if the local player isn't in VR.
     */
    @Nullable
    VRPose getPreTickWorldPose();

    /**
     * Gets the VR pose representing the player in Minecraft world coordinates after the game tick.
     * This is the pose sent to the server, and also used to calculate the pose in {@link #getWorldRenderPose()}.
     * Note that this pose is gathered AFTER mod loaders' post-tick events.
     *
     * @return The VR pose representing the player in Minecraft space post-tick, or null if the local player isn't in VR.
     */
    @Nullable
    VRPose getPostTickWorldPose();

    /**
     * Gets the VR pose representing the player in Minecraft world coordinates interpolated for rendering.
     *
     * @return The VR pose representing the player in Minecraft space post-tick interpolated for rendering, or null if
     * the local player isn't in VR.
     */
    @Nullable
    VRPose getWorldRenderPose();

    /**
     * Causes a haptic pulse (vibration/rumble) for the specified VRBodyPart, if possible.
     * This function silently fails if called for players not in VR or players who are in seated mode.
     *
     * @param bodyPart  The VRBodyPart to trigger a haptic pulse on.
     * @param duration  The duration of the haptic pulse in seconds. Note that this number is passed to the
     *                  underlying VR API used by Vivecraft, and may act with a shorter length than expected beyond
     *                  very short pulses.
     * @param frequency The frequency of the haptic pulse in Hz. 160 Hz is a safe bet for this number, with Vivecraft's codebase
     *                  using anywhere from 160 Hz for actions such as a bite on a fishing line, to 1000 Hz for things such
     *                  as a chat notification.
     * @param amplitude The amplitude of the haptic pulse. This should be kept between 0 and 1.
     * @param delay     An amount of time to delay until creating the haptic pulse. The majority of the time, one should use 0 here.
     */
    void triggerHapticPulse(VRBodyPart bodyPart, float duration, float frequency, float amplitude, float delay);

    /**
     * Causes a haptic pulse (vibration/rumble) for the specified VRBodyPart, if possible.
     * This function silently fails if called for players not in VR or players who are in seated mode.
     *
     * @param bodyPart The VRBodyPart to trigger a haptic pulse on.
     * @param duration The duration of the haptic pulse in seconds. Note that this number is passed to the
     *                 underlying VR API used by Vivecraft, and may act with a shorter length than expected beyond
     *                 very short pulses.
     */
    default void triggerHapticPulse(VRBodyPart bodyPart, float duration) {
        triggerHapticPulse(bodyPart, duration, 160F, 1F, 0F);
    }

    /**
     * @return Whether the local player is currently in seated mode.
     */
    boolean isSeated();

    /**
     * @return Whether the local player is playing with left-handed controls.
     */
    boolean isLeftHanded();

    /**
     * @return The full-body tracking mode currently in-use or some default value if the local player is not in VR.
     */
    FBTMode getFBTMode();

    /**
     * @return Whether VR support is initialized.
     */
    boolean isVRInitialized();

    /**
     * @return Whether the client is actively in VR.
     */
    boolean isVRActive();

    /**
     * @return The currently active world scale.
     */
    float getWorldScale();

    /**
     * Requests the number of ticks of history wanted for {@link #getHistoricalVRPoses()}. Any value larger than 200
     * will be capped at 200.
     * <br>
     * Requests for the number of ticks for players other than the local player should be made with
     * {@link VRAPI#requestTicksOfHistory(int)}.
     *
     * @param maxTicksBack The maximum number of ticks of history wanted.
     * @throws IllegalArgumentException If a non-positive number is supplied.
     */
    void requestTicksOfHistory(int maxTicksBack) throws IllegalArgumentException;

    /**
     * Returns the history of VR poses for the local player. One should make one call to
     * {@link #requestTicksOfHistory(int)} before calling this method to inform Vivecraft of the amount of history to
     * keep for the local player.
     * <br>
     * If one wants historical VR poses for other players and/or on the server, use
     * {@link VRAPI#getHistoricalVRPoses(net.minecraft.world.entity.player.Player)} instead.
     *
     * @return The history of VR poses for the player. Will be null if the player isn't in VR or if
     * {@link #requestTicksOfHistory(int)} has yet to be called.
     */
    @Nullable
    VRPoseHistory getHistoricalVRPoses();

    /**
     * Opens or closes Vivecraft's keyboard. Will fail silently if the user isn't in VR or if the keyboard's new state
     * is the same as the old.
     *
     * @param isNowOpen Whether the keyboard should now be open. If false, the keyboard will attempt to close.
     * @return Whether the keyboard is currently showing after attempting to open/close it.
     */
    boolean setKeyboardState(boolean isNowOpen);
}
