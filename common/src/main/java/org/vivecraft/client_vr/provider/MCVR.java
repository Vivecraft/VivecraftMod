package org.vivecraft.client_vr.provider;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.QuaternionfHistory;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.Vec3History;
import org.vivecraft.client_vr.extensions.GuiExtension;
import org.vivecraft.client_vr.extensions.WindowExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.gameplay.trackers.ClimbTracker;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;
import org.vivecraft.client_vr.provider.openvr_lwjgl.control.VRInputActionSet;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.lwjgl.Matrix4f;
import org.vivecraft.common.utils.lwjgl.Vector3f;
import org.vivecraft.common.utils.math.Quaternion;
import org.vivecraft.common.utils.math.Vector2;
import org.vivecraft.common.utils.math.Vector3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class MCVR {
    protected Minecraft mc;
    protected ClientDataHolderVR dh;
    protected static MCVR me;
    protected static VivecraftVRMod mod;

    protected HardwareType detectedHardware = HardwareType.VIVE;

    // position/orientation of headset and eye offsets
    protected org.vivecraft.common.utils.math.Matrix4f hmdPose = new org.vivecraft.common.utils.math.Matrix4f();
    public org.vivecraft.common.utils.math.Matrix4f hmdRotation = new org.vivecraft.common.utils.math.Matrix4f();
    protected org.vivecraft.common.utils.math.Matrix4f hmdPoseLeftEye = new org.vivecraft.common.utils.math.Matrix4f();
    protected org.vivecraft.common.utils.math.Matrix4f hmdPoseRightEye = new org.vivecraft.common.utils.math.Matrix4f();

    public Vec3History hmdHistory = new Vec3History();
    public Vec3History hmdPivotHistory = new Vec3History();
    public QuaternionfHistory hmdRotHistory = new QuaternionfHistory();
    protected boolean headIsTracking;
    protected org.vivecraft.common.utils.math.Matrix4f[] controllerPose = new org.vivecraft.common.utils.math.Matrix4f[3];
    protected org.vivecraft.common.utils.math.Matrix4f[] controllerRotation = new org.vivecraft.common.utils.math.Matrix4f[3];
    protected boolean[] controllerTracking = new boolean[3];
    protected org.vivecraft.common.utils.math.Matrix4f[] handRotation = new org.vivecraft.common.utils.math.Matrix4f[3];
    public Vec3History[] controllerHistory = new Vec3History[]{new Vec3History(), new Vec3History()};
    public Vec3History[] controllerForwardHistory = new Vec3History[]{new Vec3History(), new Vec3History()};
    public Vec3History[] controllerUpHistory = new Vec3History[]{new Vec3History(), new Vec3History()};
    protected double gunAngle = 0.0D;
    protected boolean gunStyle;
    public boolean initialized;
    public String initStatus;
    public boolean initSuccess;
    protected org.vivecraft.common.utils.math.Matrix4f[] poseMatrices;
    protected Vec3[] deviceVelocity;
    protected Vec3[] aimSource = new Vec3[3];
    public static final Vector3 forward = new Vector3(0.0F, 0.0F, -1.0F);
    public static final Vector3 up = new Vector3(0.0F, 1.0F, 0.0F);

    //hmd sampling
    private static final int HMD_AVG_MAX_SAMPLES = 90;
    public LinkedList<Vec3> hmdPosSamples = new LinkedList<>();
    public LinkedList<Float> hmdYawSamples = new LinkedList<>();
    protected float hmdYawTotal;
    protected float hmdYawLast;
    protected boolean trigger;
    public boolean mrMovingCamActive;
    protected HapticScheduler hapticScheduler;

    //seated
    public float seatedRot;
    public float aimPitch = 0.0F;
    //
    public boolean hudPopup = true;
    protected int moveModeSwitchCount = 0;
    public boolean isWalkingAbout;
    protected boolean isFreeRotate;
    protected boolean isFlickStick;
    protected float flickStickRot;
    protected ControllerType walkaboutController;
    protected ControllerType freeRotateController;
    protected float walkaboutYawStart;
    protected float hmdForwardYaw = 180;
    public boolean ignorePressesNextFrame = false;
    protected int quickTorchPreviousSlot;
    protected Map<String, VRInputAction> inputActions = new HashMap<>();
    protected Map<String, VRInputAction> inputActionsByKeyBinding = new HashMap<>();

    /**
     * creates the MCVR instance
     * @param mc instance of Minecraft to use
     * @param dh instance of ClientDataHolderVR to use
     * @param vrMod instance of VivecraftVRMod to use
     */
    public MCVR(Minecraft mc, ClientDataHolderVR dh, VivecraftVRMod vrMod) {
        this.mc = mc;
        this.dh = dh;
        mod = vrMod;
        me = this;

        // initialize all controller/tracker fields
        for (int c = 0; c < 3; c++) {
            this.aimSource[c] = new Vec3(0.0D, 0.0D, 0.0D);
            this.controllerPose[c] = new org.vivecraft.common.utils.math.Matrix4f();
            this.controllerRotation[c] = new org.vivecraft.common.utils.math.Matrix4f();
            this.handRotation[c] = new org.vivecraft.common.utils.math.Matrix4f();
        }
    }

    /**
     * @return the current active MCVR implementation
     */
    public static MCVR get() {
        return me;
    }

    /**
     * initializes the api connection, and sets everything up.
     * @return if init was successful
     * @throws RenderConfigException if there was a critical error
     */
    public abstract boolean init() throws RenderConfigException;

    /**
     * stops the api connection and releases any allocated objects
     */
    public abstract void destroy();

    /**
     * triggers a haptic pulse on the give controller, as soon as possible
     * @param controller controller to trigger on
     * @param durationSeconds duration in seconds
     * @param frequency frequency in Hz
     * @param amplitude strength 0.0 - 1.0
     */
    public void triggerHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude) {
        this.triggerHapticPulse(controller, durationSeconds, frequency, amplitude, 0.0F);
    }

    /**
     * triggers a haptic pulse on the give controller, after the specified delay
     * @param controller controller to trigger on
     * @param durationSeconds duration in seconds
     * @param frequency frequency in Hz
     * @param amplitude strength 0.0 - 1.0
     * @param delaySeconds delay for when to trigger in seconds
     */
    public void triggerHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude, float delaySeconds) {
        if (this.dh.vrSettings.seated) return;
        if (this.dh.vrSettings.reverseHands) {
            controller = controller == ControllerType.RIGHT ? ControllerType.LEFT : ControllerType.RIGHT;
        }
        this.hapticScheduler.queueHapticPulse(controller, durationSeconds, frequency, amplitude, delaySeconds);
    }

    /**
     * triggers a haptic pulse on the give controller
     * uses a fixed frequency and amplitude, just changes duration
     * legacy method for simplicity
     * @param controller controller to trigger on
     * @param strength how long to trigger in microseconds
     */
    @Deprecated
    public void triggerHapticPulse(ControllerType controller, int strength) {
        if (strength >= 1) {
            // Through careful analysis of the haptics in the legacy API (read: I put the controller to
            // my ear, listened to the vibration, and reproduced the frequency in Audacity), I have determined
            // that the old haptics used 160Hz. So, these parameters will match the "feel" of the old haptics.
            this.triggerHapticPulse(controller, (float) strength / 1000000.0F, 160.0F, 1.0F);
        }
    }

    /**
     * triggers a haptic pulse on the give controller
     * uses a fixed frequency and amplitude, just changes duration
     * legacy method for simplicity
     * @param controller controller to trigger on
     * @param strength how long to trigger in microseconds
     */
    @Deprecated
    public void triggerHapticPulse(int controller, int strength) {
        if (controller >= 0 && controller < ControllerType.values().length) {
            this.triggerHapticPulse(ControllerType.values()[controller], strength);
        }
    }

    /**
     * finds the controller that has the given KeyMapping bound, and triggers a haptic there
     * legacy method for simplicity
     * @param keyMapping the KeyMapping to trigger for
     * @param strength how long to trigger in microseconds
     */
    @Deprecated
    protected void triggerBindingHapticPulse(KeyMapping keyMapping, int strength) {
        ControllerType controller = this.findActiveBindingControllerType(keyMapping);
        if (controller != null) {
            this.triggerHapticPulse(controller, strength);
        }
    }

    /**
     * @return the angle at which stuff is hold in the hand
     */
    public double getGunAngle() {
        return this.gunAngle;
    }

    /**
     * @param controller controller/tracker to get the aim rotation for
     * @return aim rotation of the specified controller/tracker in room space
     */
    public org.vivecraft.common.utils.math.Matrix4f getAimRotation(int controller) {
        return this.controllerRotation[controller];
    }

    /**
     * @param controller controller/tracker to get the aim position for
     * @return aim position of the specified controller/tracker in room space
     */
    public Vec3 getAimSource(int controller) {
        Vec3 out = new Vec3(this.aimSource[controller].x, this.aimSource[controller].y, this.aimSource[controller].z);

        if (!this.dh.vrSettings.seated && this.dh.vrSettings.allowStandingOriginOffset) {
            if (this.dh.vr.isHMDTracking()) {
                out = out.add(
                    this.dh.vrSettings.originOffset.getX(),
                    this.dh.vrSettings.originOffset.getY(),
                    this.dh.vrSettings.originOffset.getZ());
            }
        }
        return out;
    }

    /**
     * @param controller controller/tracker to get the aim direction vector for
     * @return forward aim direction of the specified controller/tracker in room space
     */
    public Vec3 getAimVector(int controller) {
        Vector3 aim = this.controllerRotation[controller].transform(forward);
        return aim.toVector3d();
    }

    /**
     * @param controller controller/tracker to get the visual hand rotation for
     * @return visual hand rotation of the specified controller/tracker in room space
     */
    public org.vivecraft.common.utils.math.Matrix4f getHandRotation(int controller) {
        return this.handRotation[controller];
    }

    /**
     * @param controller controller/tracker to get the visual hand direction vector for
     * @return visual hand forward direction of the specified controller/tracker in room space
     */
    public Vec3 getHandVector(int controller) {
        Vector3 forward = new Vector3(0.0F, 0.0F, -1.0F);
        org.vivecraft.common.utils.math.Matrix4f aimRotation = this.handRotation[controller];
        Vector3 controllerDirection = aimRotation.transform(forward);
        return controllerDirection.toVector3d();
    }

    /**
     * @param eye LEFT, RIGHT or CENTER eye
     * @return position of the given eye, in room space
     */
    public Vec3 getEyePosition(RenderPass eye) {
        org.vivecraft.common.utils.math.Matrix4f pose = switch (eye) {
            case LEFT -> org.vivecraft.common.utils.math.Matrix4f.multiply(this.hmdPose, this.hmdPoseLeftEye);
            case RIGHT -> org.vivecraft.common.utils.math.Matrix4f.multiply(this.hmdPose, this.hmdPoseRightEye);
            default -> this.hmdPose;
        };

        Vector3 pos = Utils.convertMatrix4ftoTranslationVector(pose);

        if (this.dh.vrSettings.seated || this.dh.vrSettings.allowStandingOriginOffset) {
            if (this.dh.vr.isHMDTracking()) {
                pos = pos.add(this.dh.vrSettings.originOffset);
            }
        }

        return pos.toVector3d();
    }

    /**
     * @param eye LEFT, RIGHT or CENTER eye
     * @return rotation of the given eye, in room space
     */
    public org.vivecraft.common.utils.math.Matrix4f getEyeRotation(RenderPass eye) {
        org.vivecraft.common.utils.math.Matrix4f hmdToEye = switch (eye) {
            case LEFT -> this.hmdPoseLeftEye;
            case RIGHT -> this.hmdPoseRightEye;
            default -> null;
        };

        if (hmdToEye != null) {
            org.vivecraft.common.utils.math.Matrix4f eyeRot = new org.vivecraft.common.utils.math.Matrix4f();
            eyeRot.Set3x3(hmdToEye);
            return org.vivecraft.common.utils.math.Matrix4f.multiply(this.hmdRotation, eyeRot);
        } else {
            return this.hmdRotation;
        }
    }

    /**
     * @return forward vector of the headset, in room space
     */
    public Vec3 getHmdVector() {
        Vector3 look = this.hmdRotation.transform(forward);
        return look.toVector3d();
    }

    /**
     * @param keyMapping KeyMapping to get the VRInputAction for
     * @return VRInputAction that is linked to the given KeyMapping
     */
    public VRInputAction getInputAction(KeyMapping keyMapping) {
        return this.getInputAction(keyMapping.getName());
    }

    /**
     * @param name name of the KeyMapping to get the VRInputAction for
     * @return VRInputAction that is linked to the given KeyMapping
     */
    public VRInputAction getInputAction(String name) {
        return this.inputActionsByKeyBinding.get(name);
    }

    /**
     * gets the VRInputAction by name, a VRInputAction name is built like "(action set)/in/(keyMapping name)"
     * @param name name of the VRInputAction to get
     * @return VRInputAction that is linked to the given action name
     */
    public VRInputAction getInputActionByName(String name) {
        return this.inputActions.get(name);
    }

    /**
     * @return unmodifiable collection of all loaded VRInputActions
     */
    public Collection<VRInputAction> getInputActions() {
        return Collections.unmodifiableCollection(this.inputActions.values());
    }

    /**
     * @param set VRInputActionSet to get the VRInputActions for
     * @return unmodifiable collection of all VRInputActions in the given set
     */
    public Collection<VRInputAction> getInputActionsInSet(VRInputActionSet set) {
        return Collections.unmodifiableCollection(this.inputActions.values().stream().filter((action) ->
            action.actionSet == set).collect(Collectors.toList()));
    }

    /**
     * @param controller controller to check
     * @return if the controller is currently tracking
     */
    public boolean isControllerTracking(ControllerType controller) {
        return this.isControllerTracking(controller.ordinal());
    }

    /**
     * @param controller controller/tracker to check
     * @return if the controller/tracker is currently tracking
     */
    public boolean isControllerTracking(int controller) {
        return this.controllerTracking[controller];
    }

    /**
     * @return if the headset is currently tracking
     */
    public boolean isHMDTracking() {
        return this.headIsTracking;
    }

    /**
     * sets the room origin to the current headset position. assumes a 1.62 meter headset height
     */
    public void resetPosition() {
        // get the center position, and remove the old origin offset from it
        Vec3 pos = this.getEyePosition(RenderPass.CENTER).scale(-1.0D)
            .add(this.dh.vrSettings.originOffset.getX(),
                this.dh.vrSettings.originOffset.getY(),
                this.dh.vrSettings.originOffset.getZ());
        this.dh.vrSettings.originOffset = new Vector3((float) pos.x, (float) pos.y + 1.62F, (float) pos.z);
    }

    /**
     * clears the room origin offset
     */
    public void clearOffset() {
        this.dh.vrSettings.originOffset = new Vector3(0.0F, 0.0F, 0.0F);
    }

    /**
     * changes teh selected hotbar slot in the given direction.
     * @param dir direction to change to, negative is right, positive is left
     */
    protected void changeHotbar(int dir) {
        if (this.mc.player != null &&
            // never let go, jack.
            (!this.dh.climbTracker.isGrabbingLadder() || !ClimbTracker.isClaws(this.mc.player.getMainHandItem())))
        {
            if (this.mc.screen == null) {
                InputSimulator.scrollMouse(0.0D, dir * 4);
            } else {
                this.mc.player.getInventory().swapPaint(dir);
            }
        }
    }

    /**
     * processes the interactive hotbar
     */
    protected void processHotbar() {
        int previousSlot = this.dh.interactTracker.hotbar;
        this.dh.interactTracker.hotbar = -1;

        if (this.mc.player == null) return;
        // this shouldn't happen, it's final
        if (this.mc.player.getInventory() == null) return;
        if (this.dh.climbTracker.isGrabbingLadder() && ClimbTracker.isClaws(this.mc.player.getMainHandItem())) return;
        if (!this.dh.interactTracker.isActive(this.mc.player)) return;

        Vec3 main = this.getAimSource(0);
        Vec3 off = this.getAimSource(1);
        Vec3 barStartPos;
        Vec3 barEndPos;

        float offsetDir = this.dh.vrSettings.reverseHands ? -1F : 1F;

        // hotbar position based on settings
        if (this.dh.vrSettings.vrHudLockMode == VRSettings.HUDLock.WRIST) {
            float offset = this.mc.player.getMainArm().getOpposite() == (this.dh.vrSettings.reverseHands ? HumanoidArm.LEFT : HumanoidArm.RIGHT) ? 0.03F : 0.0F;
            barStartPos = this.getAimRotation(1).transform(new Vector3(offsetDir * 0.02F, 0.05F, 0.26F + offset)).toVector3d();
            barEndPos = this.getAimRotation(1).transform(new Vector3(offsetDir * 0.02F, 0.05F, 0.01F + offset)).toVector3d();
        } else if (this.dh.vrSettings.vrHudLockMode == VRSettings.HUDLock.HAND) {
            barStartPos = this.getAimRotation(1).transform(new Vector3(offsetDir * -0.18F, 0.08F, -0.01F)).toVector3d();
            barEndPos = this.getAimRotation(1).transform(new Vector3(offsetDir * 0.19F, 0.04F, -0.08F)).toVector3d();
        } else {
            return; //how did u get here
        }

        float guiScaleFactor = (float) this.mc.getWindow().getGuiScale() / GuiHandler.guiScaleFactorMax;

        Vec3 barMidPos = barStartPos.add(barEndPos).scale(0.5);

        Vec3 barStart = off.add(
            Mth.lerp(guiScaleFactor, barMidPos.x, barStartPos.x),
            Mth.lerp(guiScaleFactor, barMidPos.y, barStartPos.y),
            Mth.lerp(guiScaleFactor, barMidPos.z, barStartPos.z));
        Vec3 barEnd = off.add(
            Mth.lerp(guiScaleFactor, barMidPos.x, barEndPos.x),
            Mth.lerp(guiScaleFactor, barMidPos.y, barEndPos.y),
            Mth.lerp(guiScaleFactor, barMidPos.z, barEndPos.z));


        Vec3 barLine = barStart.subtract(barEnd);
        Vec3 handToBar = barStart.subtract(main);

        // check if the hand is close enough
        float dist = (float) (handToBar.cross(barLine).length() / barLine.length());
        if (dist > 0.06) return;

        // check that the controller is to the right of the offhand slot, and how far it's to the right
        float fact = (float) (handToBar.dot(barLine) / (barLine.x * barLine.x + barLine.y * barLine.y + barLine.z * barLine.z));
        if (fact < -1) return;

        // get the closest point from the hand to the hotbar
        Vec3 w2 = barLine.scale(fact).subtract(handToBar);
        Vec3 point = main.subtract(w2);

        float barSize = (float) barLine.length();
        float ilen = (float) barStart.subtract(point).length();
        if (fact < 0) {
            ilen *= -1;
        }
        float pos = ilen / barSize * 9;

        if (this.dh.vrSettings.reverseHands) {
            pos = 9 - pos;
        }

        // actual slot that is selected
        int box = (int) Math.floor(pos);

        if (box > 8) {
            if (this.mc.player.getMainArm().getOpposite() == HumanoidArm.RIGHT && pos >= 9.5 && pos <= 10.5) {
                box = 9;
            } else {
                return;
            }
        } else if (box < 0) {
            if (this.mc.player.getMainArm().getOpposite() == HumanoidArm.LEFT && pos <= -0.5 && pos >= -1.5) {
                box = 9;
            } else {
                return;
            }
        }

        // all that maths for this.
        this.dh.interactTracker.hotbar = box;
        if (previousSlot != this.dh.interactTracker.hotbar) {
            triggerHapticPulse(0, 750);
        }
    }

    /**
     * searches a KeyMapping by name
     * @param name name to search the KeyMapping for
     * @return found KeyMapping or null if none was found
     */
    protected KeyMapping findKeyBinding(String name) {
        return Stream.concat(Arrays.stream(this.mc.options.keyMappings), mod.getHiddenKeyBindings().stream())
            .filter((kb) -> name.equals(kb.getName())).findFirst().orElse(null);
    }

    /**
     * manages the HMD position and rotation average
     */
    protected void hmdSampling() {
        if (this.hmdPosSamples.size() == HMD_AVG_MAX_SAMPLES) {
            this.hmdPosSamples.removeFirst();
        }

        if (this.hmdYawSamples.size() == HMD_AVG_MAX_SAMPLES) {
            this.hmdYawSamples.removeFirst();
        }

        // position samples are taken always
        this.hmdPosSamples.add(this.dh.vrPlayer.vrdata_room_pre.hmd.getPosition());

        // yaw sampling below
        float yaw = this.dh.vrPlayer.vrdata_room_pre.hmd.getYaw();

        if (yaw < 0.0F) {
            yaw += 360.0F;
        }

        this.hmdYawTotal += Utils.angleDiff(yaw, this.hmdYawLast);
        this.hmdYawLast = yaw;

        if (Math.abs(Utils.angleNormalize(this.hmdYawTotal) - this.hmdYawLast) > 1.0F || this.hmdYawTotal > 100000.0F) {
            this.hmdYawTotal = this.hmdYawLast;
            VRSettings.logger.info("HMD yaw desync/overflow corrected");
        }

        float yawAvg = 0.0F;

        if (!this.hmdYawSamples.isEmpty()) {
            for (float sample : this.hmdYawSamples) {
                yawAvg += sample;
            }
            yawAvg /= (float) this.hmdYawSamples.size();
        }

        // only count this sample, if the headset moved enough, and is not still
        // this is like that, to make the menu not move constantly in seated/follow mode
        if (Math.abs(this.hmdYawTotal - yawAvg) > 20.0F) {
            this.trigger = true;
        }

        if (Math.abs(this.hmdYawTotal - yawAvg) < 1.0F) {
            this.trigger = false;
        }

        // only add the current yaw if it's the first one, or if the head moved significantly
        if (this.trigger || this.hmdYawSamples.isEmpty()) {
            this.hmdYawSamples.add(this.hmdYawTotal);
        }
    }

    /**
     * updates headset and controller matrices, also does seated controller override
     */
    protected void updateAim() {
        // hmd
        this.hmdRotation.SetIdentity();
        this.hmdRotation.Set3x3(this.hmdPose);

        Vec3 eye = this.getEyePosition(RenderPass.CENTER);
        this.hmdHistory.add(eye);

        Vector3 pivot = this.hmdRotation.transform(new Vector3(0.0F, -0.1F, 0.1F));
        this.hmdPivotHistory.add(new Vec3(pivot.getX() + eye.x, pivot.getY() + eye.y, pivot.getZ() + eye.z));

        this.hmdRotHistory.add(new Quaternionf().setFromNormalized(this.hmdRotation.transposed().toMCMatrix()
            .rotateY((float) -Math.toRadians(this.dh.vrSettings.worldRotation))));


        // controllers
        for (int c = 0; c < 2; c++) {
            org.vivecraft.common.utils.math.Matrix4f controllerPoseTip;
            org.vivecraft.common.utils.math.Matrix4f controllerPoseHand;

            if (this.dh.vrSettings.seated) {
                // seated: use the hmd orientation for the controllers
                this.controllerPose[c] = new org.vivecraft.common.utils.math.Matrix4f(this.hmdPose);
                controllerPoseHand = this.controllerPose[c];
                controllerPoseTip = this.controllerPose[c];
            } else {
                // just parse the controllers as is
                controllerPoseHand = org.vivecraft.common.utils.math.Matrix4f.multiply(this.controllerPose[c],
                    this.getControllerComponentTransform(c, "handgrip"));
                controllerPoseTip = org.vivecraft.common.utils.math.Matrix4f.multiply(this.controllerPose[c],
                    this.getControllerComponentTransform(c, "tip"));
            }

            this.handRotation[c].SetIdentity();
            this.handRotation[c].Set3x3(controllerPoseHand);

            // grab controller position in tracker space, scaled to minecraft units
            Vector3 controllerPos = Utils.convertMatrix4ftoTranslationVector(controllerPoseTip);
            this.aimSource[c] = controllerPos.toVector3d();
            this.controllerHistory[c].add(this.getAimSource(c));

            // build matrix describing controller rotation
            this.controllerRotation[c].SetIdentity();
            this.controllerRotation[c].Set3x3(controllerPoseTip);

            // special case for seated main controller
            if (c == 0 && this.dh.vrSettings.seated && this.mc.screen == null &&
                this.mc.mouseHandler.isMouseGrabbed())
            {
                Matrix4f temp = new Matrix4f();
                if (this.mc.isWindowActive()) {
                    final float hRange = 110.0F;
                    final float vRange = 180.0F;

                    int screenWidth = this.mc.getWindow().getScreenWidth();
                    int screenHeight = this.mc.getWindow().getScreenHeight();

                    if (screenHeight % 2 != 0) {
                        // fix drifting vertical mouse.
                        screenHeight--;
                    }

                    double hPos = this.mc.mouseHandler.xpos() / (double) screenWidth * hRange - (hRange * 0.5F);
                    double vPos = -this.mc.mouseHandler.ypos() / (double) screenHeight * vRange + (vRange * 0.5F);

                    float rotStart = this.dh.vrSettings.keyholeX;
                    float rotSpeed = 20.0F * this.dh.vrSettings.xSensitivity;
                    int leftEdge = (int) ((-rotStart + hRange * 0.5F) * (double) screenWidth / hRange) + 1;
                    int rightEdge = (int) ((rotStart + hRange * 0.5F) * (double) screenWidth / hRange) - 1;

                    // Scaled 0...1 from rotStart to FOV edge
                    float rotMul = ((float) Math.abs(hPos) - rotStart) / (hRange * 0.5F - rotStart);
                    double xPos = this.mc.mouseHandler.xpos();

                    Vec3 hmdDir = this.getHmdVector();

                    if (hPos < -rotStart) {
                        this.seatedRot += rotSpeed * rotMul;
                        this.seatedRot %= 360.0F;
                        this.hmdForwardYaw = (float) Math.toDegrees(Math.atan2(hmdDir.x, hmdDir.z));
                        xPos = leftEdge;
                        hPos = -rotStart;
                    } else if (hPos > rotStart) {
                        this.seatedRot -= rotSpeed * rotMul;
                        this.seatedRot %= 360.0F;
                        this.hmdForwardYaw = (float) Math.toDegrees(Math.atan2(hmdDir.x, hmdDir.z));
                        xPos = rightEdge;
                        hPos = rotStart;
                    }

                    double ySpeed = 0.5F * this.dh.vrSettings.ySensitivity;

                    double nPitch = this.aimPitch + vPos * ySpeed;
                    nPitch = Mth.clamp(nPitch, -89.9D, 89.9D);

                    double screenX = xPos *
                        (((WindowExtension) (Object) this.mc.getWindow()).vivecraft$getActualScreenWidth() /
                            (double) screenWidth
                        );
                    double screenY = (screenHeight * 0.5F) *
                        (((WindowExtension) (Object) this.mc.getWindow()).vivecraft$getActualScreenHeight() /
                            (double) this.mc.getWindow().getScreenHeight()
                        );

                    InputSimulator.setMousePos(screenX, screenY);
                    GLFW.glfwSetCursorPos(this.mc.getWindow().getWindow(), screenX, screenY);

                    temp.rotate((float) Math.toRadians(-nPitch), new Vector3f(1.0F, 0.0F, 0.0F));
                    temp.rotate((float) Math.toRadians(-180.0D + hPos - (double) this.hmdForwardYaw),
                        new Vector3f(0.0F, 1.0F, 0.0F));
                }

                this.controllerRotation[c].Set3x3(temp);
                this.handRotation[c].Set3x3(temp);
            }

            Vec3 aimDir = this.getAimVector(c);
            if (c == 0) {
                // controller 0 determines seated aim
                this.aimPitch = (float) Math.toDegrees(Math.asin(aimDir.y / aimDir.length()));
            }

            this.controllerForwardHistory[c].add(aimDir);
            Vec3 upDir = this.controllerRotation[c].transform(up).toVector3d();
            this.controllerUpHistory[c].add(upDir);
        }


        if (this.dh.vrSettings.seated) {
            // seated uses head as aim source
            this.aimSource[0] = this.getEyePosition(RenderPass.CENTER);
            this.aimSource[1] = this.getEyePosition(RenderPass.CENTER);
        }

        // trackers
        // when set attaches the 3rd person camera tracker to the right controller
        boolean debugCameraTracker = false;

        if (debugCameraTracker) {
            this.controllerPose[2] = this.controllerPose[0];
        }

        this.controllerRotation[2].SetIdentity();
        this.controllerRotation[2].Set3x3(this.controllerPose[2]);

        if (debugCameraTracker || hasCameraTracker() &&
            (this.dh.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY ||
                this.dh.vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON
            ))
        {
            this.mrMovingCamActive = true;
            Vector3 thirdControllerPos = Utils.convertMatrix4ftoTranslationVector(this.controllerPose[2]);
            this.aimSource[2] = thirdControllerPos.toVector3d();
        } else {
            this.mrMovingCamActive = false;
            this.aimSource[2] = new Vec3(
                this.dh.vrSettings.vrFixedCamposX,
                this.dh.vrSettings.vrFixedCamposY,
                this.dh.vrSettings.vrFixedCamposZ);
        }
    }

    /**
     * processes vr specific keys
     */
    public void processBindings() {
        if (this.inputActions.isEmpty()) return;

        boolean sleeping = this.mc.level != null && this.mc.player != null && this.mc.player.isSleeping();
        boolean gui = this.mc.screen != null;
        boolean toggleMovementPressed = mod.keyToggleMovement.consumeClick();

        // allow movement switching with long pressing pick block
        if (this.mc.options.keyPickItem.isDown() || toggleMovementPressed) {
            if (++this.moveModeSwitchCount == 80 || toggleMovementPressed) {
                if (this.dh.vrSettings.seated) {
                    this.dh.vrSettings.seatedFreeMove = !this.dh.vrSettings.seatedFreeMove;
                    this.mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.movementmodeswitch",
                        this.dh.vrSettings.seatedFreeMove ? Component.translatable("vivecraft.options.freemove") :
                            Component.translatable("vivecraft.options.teleport")));
                } else if (this.dh.vrPlayer.isTeleportSupported()) {
                    this.dh.vrSettings.forceStandingFreeMove = !this.dh.vrSettings.forceStandingFreeMove;
                    this.mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.movementmodeswitch",
                        this.dh.vrSettings.seatedFreeMove ? Component.translatable("vivecraft.options.freemove") :
                            Component.translatable("vivecraft.options.teleport")));
                } else if (this.dh.vrPlayer.isTeleportOverridden()) {
                    this.dh.vrPlayer.setTeleportOverride(false);
                    this.mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.teleportdisabled"));
                } else {
                    this.dh.vrPlayer.setTeleportOverride(true);
                    this.mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.teleportenabled"));
                }
            }
        } else {
            this.moveModeSwitchCount = 0;
        }

        Vec3 main = this.getAimVector(0);
        Vec3 off = this.getAimVector(1);

        float mainYaw = (float) Math.toDegrees(Math.atan2(-main.x, main.z));
        float offYaw = (float) Math.toDegrees(Math.atan2(-off.x, off.z));

        if (!gui) {
            // world rotation
            if (mod.keyWalkabout.isDown()) {
                ControllerType controller = this.findActiveBindingControllerType(mod.keyWalkabout);

                float yaw = controller == ControllerType.LEFT ? offYaw : mainYaw;

                if (!this.isWalkingAbout) {
                    this.isWalkingAbout = true;
                    this.walkaboutYawStart = this.dh.vrSettings.worldRotation - yaw;
                } else {
                    this.dh.vrSettings.worldRotation = this.walkaboutYawStart + yaw;
                    // Prevent stupidly large values (can they even happen here?)
                    this.dh.vrSettings.worldRotation %= 360.0F;
                }
            } else {
                this.isWalkingAbout = false;
            }

            if (mod.keyRotateFree.isDown()) {
                ControllerType controller = this.findActiveBindingControllerType(mod.keyRotateFree);

                float yaw = controller == ControllerType.LEFT ? offYaw : mainYaw;

                if (!this.isFreeRotate) {
                    this.isFreeRotate = true;
                    this.walkaboutYawStart = this.dh.vrSettings.worldRotation + yaw;
                } else {
                    this.dh.vrSettings.worldRotation = this.walkaboutYawStart - yaw;
                }
            } else {
                this.isFreeRotate = false;
            }
        }

        if (this.dh.vrSettings.worldRotationIncrement == 0.0F) {
            // smooth rotation
            float ax = this.getInputAction(mod.keyRotateAxis).getAxis2DUseTracked().getX();

            if (ax == 0.0F) {
                ax = this.getInputAction(mod.keyFreeMoveRotate).getAxis2DUseTracked().getX();
            }

            // single direction keys
            ax -= Math.abs(this.getInputAction(mod.keyRotateLeft).getAxis1DUseTracked());
            ax += Math.abs(this.getInputAction(mod.keyRotateRight).getAxis1DUseTracked());

            if (ax != 0.0F) {
                float analogRotSpeed = 10.0F * ax;
                this.dh.vrSettings.worldRotation -= analogRotSpeed;
                this.dh.vrSettings.worldRotation %= 360.0F;
            }
        } else if (mod.keyRotateAxis.consumeClick() || mod.keyFreeMoveRotate.consumeClick()) {
            // axis snap turning
            float ax = this.getInputAction(mod.keyRotateAxis).getAxis2D(false).getX();

            if (ax == 0.0F) {
                ax = this.getInputAction(mod.keyFreeMoveRotate).getAxis2D(false).getX();
            }

            // dead zone
            if (Math.abs(ax) > 0.5F) {
                this.dh.vrSettings.worldRotation -= this.dh.vrSettings.worldRotationIncrement * Math.signum(ax);
                this.dh.vrSettings.worldRotation %= 360.0F;
            }
        } else if (mod.keyRotateLeft.consumeClick()){
            // button snap turning
            this.dh.vrSettings.worldRotation += this.dh.vrSettings.worldRotationIncrement;
            this.dh.vrSettings.worldRotation %= 360.0F;
        } else if (mod.keyRotateRight.consumeClick()){
            this.dh.vrSettings.worldRotation -= this.dh.vrSettings.worldRotationIncrement;
            this.dh.vrSettings.worldRotation %= 360.0F;
        }

        Vector2 axis = this.getInputAction(mod.keyFlickStick).getAxis2DUseTracked();
        if (axis.getX() != 0F || axis.getY() != 0F) {
            float rotation = (float) Math.toDegrees(Math.atan2(axis.getX(), axis.getY()));
            if (isFlickStick) {
                this.dh.vrSettings.worldRotation += this.flickStickRot - rotation;
            } else {
                isFlickStick = true;
                this.dh.vrSettings.worldRotation -= rotation;
            }

            this.dh.vrSettings.worldRotation %= 360.0F;
            this.flickStickRot = rotation;
        } else {
            this.flickStickRot = 0F;
            isFlickStick = false;
        }

        this.seatedRot = this.dh.vrSettings.worldRotation;

        if (mod.keyHotbarNext.consumeClick()) {
            this.changeHotbar(-1);
            this.triggerBindingHapticPulse(mod.keyHotbarNext, 250);
        }

        if (mod.keyHotbarPrev.consumeClick()) {
            this.changeHotbar(1);
            this.triggerBindingHapticPulse(mod.keyHotbarPrev, 250);
        }

        // quick torch, checks for a torch in the hotbar, and places it
        if (mod.keyQuickTorch.consumeClick() && this.mc.player != null && this.mc.screen == null) {
            for (int slot = 0; slot < 9; slot++) {
                ItemStack itemstack = this.mc.player.getInventory().getItem(slot);

                if (itemstack.getItem() instanceof BlockItem item && item.getBlock() instanceof TorchBlock) {
                    this.quickTorchPreviousSlot = this.mc.player.getInventory().selected;
                    this.mc.player.getInventory().selected = slot;
                    this.mc.startUseItem();
                    // switch back immediately
                    this.mc.player.getInventory().selected = this.quickTorchPreviousSlot;
                    this.quickTorchPreviousSlot = -1;
                    break;
                }
            }
        }

        // if you start moving, close any UI
        if (gui && !sleeping && this.mc.options.keyUp.isDown() && !(this.mc.screen instanceof WinScreen) && this.mc.player != null) {
            this.mc.player.closeContainer();
        }

        // containers only listens directly to the keyboard to close.
        if (this.mc.screen instanceof AbstractContainerScreen && this.mc.options.keyInventory.consumeClick() && this.mc.player != null) {
            this.mc.player.closeContainer();
        }

        // allow toggling chat window with chat keybind
        if (this.mc.screen instanceof ChatScreen && this.mc.options.keyChat.consumeClick()) {
            this.mc.setScreen(null);
        }

        // swap slow mirror between Third and First Person
        if (mod.keySwapMirrorView.consumeClick()) {
            if (this.dh.vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON) {
                this.dh.vrSettings.displayMirrorMode = VRSettings.MirrorMode.FIRST_PERSON;
            } else if (this.dh.vrSettings.displayMirrorMode == VRSettings.MirrorMode.FIRST_PERSON) {
                this.dh.vrSettings.displayMirrorMode = VRSettings.MirrorMode.THIRD_PERSON;
            }
            this.dh.vrRenderer.reinitWithoutShaders("Mirror Setting Changed");
        }

        // start third person cam movement
        if (mod.keyMoveThirdPersonCam.consumeClick() && !ClientDataHolderVR.kiosk && !this.dh.vrSettings.seated && (this.dh.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY || this.dh.vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON)) {
            ControllerType controller = this.findActiveBindingControllerType(mod.keyMoveThirdPersonCam);
            if (controller != null) {
                VRHotkeys.startMovingThirdPersonCam(controller.ordinal(), VRHotkeys.Triggerer.BINDING);
            }
        }

        // stop third person cam movement
        if (VRHotkeys.isMovingThirdPersonCam()) {
            VRHotkeys.Triggerer trigger = VRHotkeys.getMovingThirdPersonCamTriggerer();
            // check type first, to not consume unrelated clicks
            if ((trigger == VRHotkeys.Triggerer.MENUBUTTON && mod.keyMenuButton.consumeClick()) ||
                (trigger == VRHotkeys.Triggerer.BINDING && !mod.keyMoveThirdPersonCam.isDown()))
            {
                VRHotkeys.stopMovingThirdPersonCam();
                this.dh.vrSettings.saveOptions();
            }
        }

        // keyboard
        if (mod.keyToggleKeyboard.consumeClick()) {
            KeyboardHandler.setOverlayShowing(!KeyboardHandler.Showing);
        }

        // close keyboard with ESC
        if (KeyboardHandler.Showing && this.mc.screen == null && mod.keyMenuButton.consumeClick()) {
            KeyboardHandler.setOverlayShowing(false);
        }

        // radial menu
        if (mod.keyRadialMenu.consumeClick() && !gui) {
            ControllerType controller = this.findActiveBindingControllerType(mod.keyRadialMenu);
            if (controller != null) {
                RadialHandler.setOverlayShowing(!RadialHandler.isShowing(), controller);
            }
        }

        // close radial with ESC when not hold mode
        if (RadialHandler.isShowing() && mod.keyMenuButton.consumeClick()) {
            RadialHandler.setOverlayShowing(false, null);
        }

        if (mod.keyMenuButton.consumeClick()) {
            // handle menu directly
            if (!gui) {
                if (!ClientDataHolderVR.kiosk) {
                    this.mc.pauseGame(false);
                }
            } else {
                InputSimulator.pressKey(GLFW.GLFW_KEY_ESCAPE);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_ESCAPE);
            }

            KeyboardHandler.setOverlayShowing(false);
        }

        // player list
        if (mod.keyTogglePlayerList.consumeClick()) {
            ((GuiExtension) this.mc.gui).vivecraft$setShowPlayerList(!((GuiExtension) this.mc.gui).vivecraft$getShowPlayerList());
        }

        // screenshot cam
        boolean toggleCam = mod.keyToggleHandheldCam.consumeClick();
        boolean quickCam = mod.keyQuickHandheldCam.consumeClick();
        if (this.mc.player != null && (toggleCam || quickCam)) {
            if (toggleCam || !this.dh.cameraTracker.isVisible()) {
                this.dh.cameraTracker.toggleVisibility();
            }

            // if the cam is now visible position it
            if (this.dh.cameraTracker.isVisible()) {
                ControllerType hand = this.findActiveBindingControllerType(
                    toggleCam ? mod.keyToggleHandheldCam : mod.keyQuickHandheldCam);

                if (hand == null) {
                    hand = ControllerType.RIGHT;
                }

                VRData.VRDevicePose handPose = this.dh.vrPlayer.vrdata_world_pre.getController(hand.ordinal());
                this.dh.cameraTracker.setPosition(handPose.getPosition());
                this.dh.cameraTracker.setRotation(new Quaternion(handPose.getMatrix().transposed()));

                if (quickCam) {
                    // start moving
                    this.dh.cameraTracker.startMoving(hand.ordinal(), true);
                }
            }
        }

        // stop quick cam
        if (!mod.keyQuickHandheldCam.isDown() && this.dh.cameraTracker.isMoving() && this.dh.cameraTracker.isQuickMode() && this.mc.player != null) {
            this.dh.cameraTracker.stopMoving();
            this.dh.grabScreenShot = true;
        }

        GuiHandler.processBindingsGui();
        RadialHandler.processBindings();
        KeyboardHandler.processBindings();
        this.dh.interactTracker.processBindings();
    }

    /**
     * creates VRInputActions for all registered keyMappings, should be called in {@link #init}
     */
    public void populateInputActions() {
        Map<String, ActionParams> actionParams = this.getSpecialActionParams();

        // iterate over all minecraft keys, and our hidden keys
        for (KeyMapping keyMapping : Stream.concat(Arrays.stream(this.mc.options.keyMappings), mod.getHiddenKeyBindings().stream()).toList()) {
            ActionParams params = actionParams.getOrDefault(keyMapping.getName(), ActionParams.DEFAULT);
            VRInputAction action = new VRInputAction(keyMapping, params.requirement, params.type, params.actionSetOverride);

            this.inputActions.put(action.name, action);
            this.inputActionsByKeyBinding.put(action.keyBinding.getName(), action);
        }

        this.getInputAction(mod.keyVRInteract).setPriority(5).setEnabled(false);
        this.getInputAction(mod.keyClimbeyGrab).setPriority(10).setEnabled(false);
        this.getInputAction(mod.keyClimbeyJump).setEnabled(false);
        this.getInputAction(GuiHandler.keyKeyboardClick).setPriority(50);
        this.getInputAction(GuiHandler.keyKeyboardShift).setPriority(50);
    }

    /**
     * This is for bindings with specific requirement/type params, anything not listed will default to optional and boolean <br>
     * See OpenVR docs for valid values: <a href="https://github.com/ValveSoftware/openvr/wiki/Action-manifest#actions">Action-manifest#actions</a>
     * @return map of Keymappings with non default ActionParameters
     */

    public Map<String, ActionParams> getSpecialActionParams() {
        Map<String, ActionParams> map = new HashMap<>();

        this.addActionParams(map, this.mc.options.keyUp, "optional", "vector1", null);
        this.addActionParams(map, this.mc.options.keyDown, "optional", "vector1", null);
        this.addActionParams(map, this.mc.options.keyLeft, "optional", "vector1", null);
        this.addActionParams(map, this.mc.options.keyRight, "optional", "vector1", null);
        this.addActionParams(map, this.mc.options.keyInventory, "suggested", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, this.mc.options.keyAttack, "suggested", "boolean", null);
        this.addActionParams(map, this.mc.options.keyUse, "suggested", "boolean", null);
        this.addActionParams(map, this.mc.options.keyChat, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, mod.keyHotbarScroll, "optional", "vector2", null);
        this.addActionParams(map, mod.keyHotbarSwipeX, "optional", "vector2", null);
        this.addActionParams(map, mod.keyHotbarSwipeY, "optional", "vector2", null);
        this.addActionParams(map, mod.keyMenuButton, "suggested", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, mod.keyTeleportFallback, "suggested", "vector1", null);
        this.addActionParams(map, mod.keyFreeMoveRotate, "optional", "vector2", null);
        this.addActionParams(map, mod.keyFreeMoveStrafe, "optional", "vector2", null);
        this.addActionParams(map, mod.keyRotateLeft, "optional", "vector1", null);
        this.addActionParams(map, mod.keyRotateRight, "optional", "vector1", null);
        this.addActionParams(map, mod.keyRotateAxis, "optional", "vector2", null);
        this.addActionParams(map, mod.keyFlickStick, "optional", "vector2", null);
        this.addActionParams(map, mod.keyRadialMenu, "suggested", "boolean", null);
        this.addActionParams(map, mod.keySwapMirrorView, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, mod.keyToggleKeyboard, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, mod.keyMoveThirdPersonCam, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, mod.keyToggleHandheldCam, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, mod.keyQuickHandheldCam, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, mod.keyTrackpadTouch, "optional", "boolean", VRInputActionSet.TECHNICAL);
        this.addActionParams(map, mod.keyVRInteract, "suggested", "boolean", VRInputActionSet.CONTEXTUAL);
        this.addActionParams(map, mod.keyClimbeyGrab, "suggested", "boolean", null);
        this.addActionParams(map, mod.keyClimbeyJump, "suggested", "boolean", null);
        this.addActionParams(map, GuiHandler.keyLeftClick, "suggested", "boolean", null);
        this.addActionParams(map, GuiHandler.keyScrollAxis, "optional", "vector2", null);
        this.addActionParams(map, GuiHandler.keyRightClick, "suggested", "boolean", null);
        this.addActionParams(map, GuiHandler.keyShift, "suggested", "boolean", null);
        this.addActionParams(map, GuiHandler.keyKeyboardClick, "suggested", "boolean", null);
        this.addActionParams(map, GuiHandler.keyKeyboardShift, "suggested", "boolean", null);

        // users can provide their own action parameters if they want
        // this allows them to split mod KeyMappings into GUI, INGAME and GLOBAL categories
        File file = new File("customactionsets.txt");

        if (file.exists()) {
            VRSettings.logger.info("Loading custom action set definitions...");
            String line;

            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                while ((line = bufferedReader.readLine()) != null) {
                    String[] tokens = line.split(":", 2);

                    if (tokens.length < 2) {
                        VRSettings.logger.warn("Invalid tokens: {}", line);
                        continue;
                    }
                    KeyMapping keyMapping = this.findKeyBinding(tokens[0]);

                    if (keyMapping == null) {
                        VRSettings.logger.warn("Unknown key binding: {}", tokens[0]);
                    } else if (mod.getAllKeyBindings().contains(keyMapping)) {
                        VRSettings.logger.warn("NO! Don't touch Vivecraft bindings!: {}", keyMapping.getName());
                    } else {
                        VRInputActionSet actionSet = switch (tokens[1].toLowerCase()) {
                            case "ingame" -> VRInputActionSet.INGAME;
                            case "gui" -> VRInputActionSet.GUI;
                            case "global" -> VRInputActionSet.GLOBAL;
                            default -> null;
                        };

                        if (actionSet == null) {
                            VRSettings.logger.warn("Unknown action set: {}", tokens[1]);
                        } else {
                            this.addActionParams(map, keyMapping, "optional", "boolean", actionSet);
                        }
                    }
                }
            } catch (IOException e) {
                VRSettings.logger.error("Failed to read customactionsets.txt: {}", e.getMessage());
            }
        }

        return map;
    }

    /**
     * convenience method to create an ActionParam and add to the map
     * @param map Map to add the ActionParam to
     * @param keyMapping KeyMapping the ActionParam belongs to
     * @param requirement requirement of the action. See {@link ActionParams#requirement}
     * @param type input type of the action. See {@link ActionParams#type}
     * @param actionSetOverride actionset this should be in. See {@link ActionParams#actionSetOverride}
     */
    private void addActionParams(Map<String, ActionParams> map, KeyMapping keyMapping, String requirement, String type, VRInputActionSet actionSetOverride) {
        ActionParams actionparams = new ActionParams(requirement, type, actionSetOverride);
        map.put(keyMapping.getName(), actionparams);
    }

    /**
     * handles any keyboard inputs that are specific to this MCVR implementation
     * @param key GLFW key that is handled
     * @param scanCode scanCode of the handled key
     * @param action if the key was pressed, released or repeated
     * @param modifiers key modifiers that are active
     * @return true if a key was handled
     */
    public boolean handleKeyboardInputs(int key, int scanCode, int action, int modifiers) {
        return false;
    }

    /**
     * @return the name of this MCVR implementation
     */
    public abstract String getName();

    /**
     * processes the fetched inputs from the VR runtime, and maps them to the ingame keys
     */
    public abstract void processInputs();

    /**
     * @param keyMapping KeyMapping to check where it is bound at
     * @return controller this Keymapping is mapped on, null if it isn't mapped
     */
    protected abstract ControllerType findActiveBindingControllerType(KeyMapping keyMapping);

    /**
     * polls VR events, and fetches new device poses and inputs
     * @param frameIndex index of the current VR frame. Some VR runtimes need that
     */
    public abstract void poll(long frameIndex);

    /**
     * @return size of the play area or null if not available
     */
    public abstract Vector2f getPlayAreaSize();

    /**
     * @param controllerIndex index of the controller to get the transform for
     * @param componentName name of the transform. `tip` or `handgrip`
     * @return the controller transform with the given name, that was fetched during {@link MCVR#poll}
     */
    public abstract org.vivecraft.common.utils.math.Matrix4f getControllerComponentTransform(int controllerIndex, String componentName);

    /**
     * @return if there is a tracker for the camera
     */
    public abstract boolean hasCameraTracker();

    /**
     * @param action VRInputAction to query origins for
     * @return a list containing all currently active origin handles for that action
     */
    public abstract List<Long> getOrigins(VRInputAction action);

    /**
     * @param origin the origin handle of an input action
     * @return String describing what button/input the given origin is pointing to
     */
    public abstract String getOriginName(long origin);

    /**
     * @return the VRRenderer that corresponds to this MCVR
     */
    public abstract VRRenderer createVRRenderer();

    /**
     * @return if the headset is active and the game should be in VR, when this returns false, the game will switch to NONVR
     */
    public abstract boolean isActive();

    /**
     * determines if the vanilla framecap should still be applied,
     * by default this returns false, since the VR runtime should handle any frame caps
     * @return if the game should still apply the vanilla framecap
     */
    public boolean capFPS() {
        return false;
    }

    /**
     * @return the ipd in meters
     */
    public abstract float getIPD();

    /**
     * @return the name of the VR runtime
     */
    public abstract String getRuntimeName();
}
