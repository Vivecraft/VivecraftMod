package org.vivecraft.client.api_impl;

import org.vivecraft.api.client.InteractModule;
import org.vivecraft.api.client.Tracker;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.api.data.FBTMode;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.common.api_impl.data.VRPoseHistoryImpl;

import javax.annotation.Nullable;

public final class VRClientAPIImpl implements VRClientAPI {

    public static final VRClientAPIImpl INSTANCE = new VRClientAPIImpl();

    private final VRPoseHistoryImpl poseHistory = new VRPoseHistoryImpl();

    private VRClientAPIImpl() {
    }

    public void clearPoseHistory() {
        this.poseHistory.clear();
    }

    public void addPoseToHistory(VRPose pose) {
        this.poseHistory.addPose(pose);
    }

    @Nullable
    @Override
    public VRPose getLatestRoomPose() {
        if (!isVRActive()) {
            return null;
        }
        return ClientDataHolderVR.getInstance().vrPlayer.vrdata_room_pre.asVRPose();
    }

    @Nullable
    @Override
    public VRPose getPostTickRoomPose() {
        if (!isVRActive()) {
            return null;
        }
        return ClientDataHolderVR.getInstance().vrPlayer.vrdata_room_post.asVRPose();
    }

    @Nullable
    @Override
    public VRPose getPreTickWorldPose() {
        if (!isVRActive()) {
            return null;
        }
        return ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_pre.asVRPose();
    }

    @Nullable
    @Override
    public VRPose getPostTickWorldPose() {
        if (!isVRActive()) {
            return null;
        }
        return ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_post.asVRPose();
    }

    @Nullable
    @Override
    public VRPose getWorldRenderPose() {
        if (!isVRActive()) {
            return null;
        }
        return ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().asVRPose();
    }

    @Override
    public void triggerHapticPulse(VRBodyPart bodyPart, float duration, float frequency, float amplitude, float delay) {
        if (amplitude < 0F || amplitude > 1F) {
            throw new IllegalArgumentException("The amplitude of a haptic pulse must be between 0 and 1.");
        }
        if (isVRActive() && !isSeated()) {
            ClientDataHolderVR.getInstance().vr.triggerHapticPulse(
                bodyPart,
                duration,
                frequency,
                amplitude,
                delay
            );
        }
    }

    @Override
    public boolean isSeated() {
        return ClientDataHolderVR.getInstance().vrSettings.seated;
    }

    @Override
    public boolean isLeftHanded() {
        return ClientDataHolderVR.getInstance().vrSettings.reverseHands;
    }

    @Override
    public FBTMode getFBTMode() {
        // Need to check if VR is running, not just initialized, since the VR player is set after initialization
        if (!isVRActive()) {
            return FBTMode.ARMS_ONLY;
        }
        return ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_pre.fbtMode;
    }

    @Override
    public boolean isVRInitialized() {
        return VRState.VR_INITIALIZED;
    }

    @Override
    public boolean isVRActive() {
        return VRState.VR_RUNNING;
    }

    @Override
    public float getWorldScale() {
        if (isVRActive()) {
            return ClientDataHolderVR.getInstance().vrPlayer.worldScale;
        } else {
            return 1f;
        }
    }

    @Override
    @Nullable
    public VRPoseHistory getHistoricalVRPoses() {
        if (!isVRActive()) {
            return null;
        }
        return this.poseHistory;
    }

    @Override
    public void registerTracker(Tracker tracker) {
        ClientDataHolderVR.getInstance().registerTracker(tracker);
    }

    @Override
    public void registerInteractModule(InteractModule module) {
        ClientDataHolderVR.getInstance().interactTracker.registerModules(module);
    }

    @Override
    public boolean setKeyboardState(boolean isNowOpen) {
        if (isVRActive()) {
            return KeyboardHandler.setOverlayShowing(isNowOpen);
        }
        return false;
    }
}
