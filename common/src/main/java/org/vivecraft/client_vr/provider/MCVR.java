package org.vivecraft.client_vr.provider;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.VRData.VRDevicePose;
import org.vivecraft.client_vr.Vec3History;
import org.vivecraft.client_vr.extensions.GuiExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;
import org.vivecraft.client_vr.provider.openvr_lwjgl.control.VRInputActionSet;
import org.vivecraft.client_vr.provider.openvr_lwjgl.control.VivecraftMovementInput;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRHotkeys.Triggerer;
import org.vivecraft.client_vr.settings.VRSettings.HUDLock;
import org.vivecraft.client_vr.settings.VRSettings.MirrorMode;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.mod_compat_vr.ShadersHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static org.joml.Math.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;
import static org.vivecraft.client.utils.Utils.message;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.*;

@ParametersAreNonnullByDefault
public abstract class MCVR {
    protected static MCVR me;
    protected Matrix4f hmdPose = new Matrix4f();
    public Matrix4f hmdRotation = new Matrix4f();
    public HardwareType detectedHardware = HardwareType.VIVE;
    protected Matrix4f hmdPoseLeftEye = new Matrix4f();
    protected Matrix4f hmdPoseRightEye = new Matrix4f();
    public Vec3History hmdHistory = new Vec3History();
    public Vec3History hmdPivotHistory = new Vec3History();
    protected boolean headIsTracking;
    protected Matrix4f[] controllerPose = new Matrix4f[]{new Matrix4f(), new Matrix4f()};
    protected Matrix4f[] controllerRotation = new Matrix4f[]{new Matrix4f(), new Matrix4f()};
    protected boolean[] controllerTracking = new boolean[]{false, false};
    protected int[] controllerSkeletalInputTrackingLevel = {0, 0};
    protected ArrayList<Float>[] gestureFingerSplay = new ArrayList[]{new ArrayList<Float>(), new ArrayList<Float>()};
    protected ArrayList<Float>[] gestureFingerCurl = new ArrayList[]{new ArrayList<Float>(), new ArrayList<Float>()};
    protected ArrayList<Vector4f>[] gestureFingerTransforms = new ArrayList[]{new ArrayList<Vector4f>(), new ArrayList<Vector4f>()};
    protected ArrayList<Quaternionf>[] gestureFingerOrientations = new ArrayList[]{new ArrayList<Quaternionf>(), new ArrayList<Quaternionf>()};
    protected Matrix4f[] gesturePose = new Matrix4f[2];
    protected Matrix4f[] gestureRotation = new Matrix4f[2];
    protected Vector3d[] gestureVelocity = new Vector3d[2];
    protected Matrix4f[] handRotation = new Matrix4f[]{new Matrix4f(), new Matrix4f()};
    public Vec3History[] controllerHistory = new Vec3History[]{new Vec3History(), new Vec3History()};
    public Vec3History[] controllerForwardHistory = new Vec3History[]{new Vec3History(), new Vec3History()};
    public Vec3History[] controllerUpHistory = new Vec3History[]{new Vec3History(), new Vec3History()};
    protected float gunAngle = 0.0F;
    protected boolean gunStyle;
    public boolean initialized;
    public String initStatus;
    public boolean initSuccess;
    protected Matrix4f[] poseMatrices;
    protected Vector3f[] aimSource = new Vector3f[]{new Vector3f(), new Vector3f()};
    public int hmdAvgLength = 90;
    public LinkedList<Vec3> hmdPosSamples = new LinkedList<>();
    public LinkedList<Float> hmdYawSamples = new LinkedList<>();
    protected float hmdYawTotal;
    protected float hmdYawLast;
    protected boolean trigger;
    public boolean mrMovingCamActive;
    public Vec3 mrControllerPos = Vec3.ZERO;
    public float mrControllerPitch;
    public float mrControllerYaw;
    public float mrControllerRoll;
    protected HapticScheduler hapticScheduler;
    public float seatedRot;
    public float aimPitch = 0.0F;
    public boolean hudPopup = true;
    protected int moveModeSwitchCount = 0;
    public boolean isWalkingAbout;
    protected boolean isFreeRotate;
    protected ControllerType walkaboutController;
    protected ControllerType freeRotateController;
    protected float walkaboutYawStart;
    protected float hmdForwardYaw = 180;
    public boolean ignorePressesNextFrame = false;
    protected Map<String, VRInputAction> inputActions = new HashMap<>();
    protected Map<String, VRInputAction> inputActionsByKeyBinding = new HashMap<>();

    public abstract String getName();

    public abstract String getID();

    public abstract void processInputs();

    public abstract void destroy();

    public float getGunAngle() {
        return this.gunAngle;
    }

    public Matrix4f getAimRotation(int controller) {
        return this.controllerRotation[controller];
    }

    public Vec3 getAimSource(int controller) {
        return convertToVec3(
            !dh.vrSettings.seated && dh.vrSettings.allowStandingOriginOffset && dh.vr.isHMDTracking() ?
            this.aimSource[controller].add(dh.vrSettings.originOffset, new Vector3f()) :
            this.aimSource[controller]
        );
    }

    public Vec3 getAimVector(int controller) {
        return convertToVec3(this.controllerRotation[controller].transformProject(forward, new Vector3f()));
    }

    public Vector3f getGesturePosition(int controller) {
        Vector3f vector3_position = this.gesturePose[controller].getTranslation(new Vector3f());
        return new Vector3f(vector3_position.x, vector3_position.y, vector3_position.z);
    }

    public Vector3f getGestureVector(int controller) {
        return this.gesturePose[controller].transformPosition(new Vector3f(up));
    }

    public ArrayList<Vector4f> getGestureFingerTransforms(int controller) {
        return this.gestureFingerTransforms[controller];
    }

    public ArrayList<Quaternionf> getGestureFingerOrientations(int controller) {
        return this.gestureFingerOrientations[controller];
    }

    public void triggerHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude) {
        this.triggerHapticPulse(controller, durationSeconds, frequency, amplitude, 0.0F);
    }

    public void triggerHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude, float delaySeconds) {
        if (!dh.vrSettings.seated) {
            if (dh.vrSettings.reverseHands) {
                if (controller == ControllerType.RIGHT) {
                    controller = ControllerType.LEFT;
                } else {
                    controller = ControllerType.RIGHT;
                }
            }

            this.hapticScheduler.queueHapticPulse(controller, durationSeconds, frequency, amplitude, delaySeconds);
        }
    }

    @Deprecated
    public void triggerHapticPulse(ControllerType controller, int strength) {
        if (strength >= 1) {
            this.triggerHapticPulse(controller, (float) strength / 1000000.0F, 160.0F, 1.0F);
        }
    }

    @Deprecated
    public void triggerHapticPulse(int controller, int strength) {
        if (controller >= 0 && controller < ControllerType.values().length) {
            this.triggerHapticPulse(ControllerType.values()[controller], strength);
        }
    }

    public Matrix4f getHandRotation(int controller) {
        return this.handRotation[controller];
    }

    public Vec3 getHandVector(int controller) {
        return convertToVec3(this.handRotation[controller].transformProject(forward, new Vector3f()));
    }

    public Vector3f getCenterEyePosition() {
        return (
            ((dh.vrSettings.seated || dh.vrSettings.allowStandingOriginOffset) && dh.vr.isHMDTracking()) ?
            this.hmdPose.getTranslation(new Vector3f()).add(dh.vrSettings.originOffset) :
            this.hmdPose.getTranslation(new Vector3f())
        );
    }

    public Vec3 getEyePosition(RenderPass eye) {
        Vector3f vector3 = switch (eye) {
            case LEFT -> {
                yield this.hmdPose.mul0(this.hmdPoseLeftEye, new Matrix4f()).getTranslation(new Vector3f());
            }
            case RIGHT -> {
                yield this.hmdPose.mul0(this.hmdPoseRightEye, new Matrix4f()).getTranslation(new Vector3f());
            }
            default -> {
                yield this.hmdPose.getTranslation(new Vector3f());
            }
        };

        if (dh.vrSettings.seated || dh.vrSettings.allowStandingOriginOffset) {
            if (dh.vr.isHMDTracking()) {
                vector3.add(dh.vrSettings.originOffset);
            }
        }

        return convertToVec3(vector3);
    }

    public HardwareType getHardwareType() {
        return dh.vrSettings.forceHardwareDetection > 0 ? HardwareType.values()[dh.vrSettings.forceHardwareDetection - 1] : this.detectedHardware;
    }

    public Vector3f getHmdVector() {
        return this.hmdRotation.transformProject(forward, new Vector3f());
    }

    public Matrix4f getEyeRotation(RenderPass eye) {
        Matrix4f matrix4f = new Matrix4f();

        if (eye == RenderPass.LEFT) {
            matrix4f.set(this.hmdPoseLeftEye);
        } else {
            matrix4f.set(this.hmdPoseRightEye);
        }

        return this.hmdRotation.mul0(new Matrix4f().set3x3(matrix4f), matrix4f);
    }

    public VRInputAction getInputAction(String keyBindingDesc) {
        return this.inputActionsByKeyBinding.get(keyBindingDesc);
    }

    public VRInputAction getInputActionByName(String name) {
        return this.inputActions.get(name);
    }

    public Collection<VRInputAction> getInputActions() {
        return Collections.unmodifiableCollection(this.inputActions.values());
    }

    public VRInputAction getInputAction(KeyMapping keyBinding) {
        return this.getInputAction(keyBinding.getName());
    }

    public Collection<VRInputAction> getInputActionsInSet(VRInputActionSet set) {
        return this.inputActions.values().stream().filter((action) -> action.actionSet == set).toList();
    }

    public boolean isControllerTracking(ControllerType controller) {
        return this.isControllerTracking(controller.ordinal());
    }

    public boolean isControllerTracking(int controller) {
        return this.controllerTracking[controller];
    }

    public int getControllerSkeletalTrackingLevel(int controller) {
        return this.controllerSkeletalInputTrackingLevel[controller];
    }

    public ArrayList<Float>[] getGestureFingerSplay() {
        return this.gestureFingerSplay;
    }

    public ArrayList<Float>[] getGestureFingerCurl() {
        return this.gestureFingerCurl;
    }

    public void resetPosition() {
        dh.vrSettings.originOffset.set(this.getCenterEyePosition().negate().add(dh.vrSettings.originOffset)).y += 1.62F;
    }

    public void clearOffset() {
        dh.vrSettings.originOffset.zero();
    }

    public boolean isHMDTracking() {
        return this.headIsTracking;
    }

    protected void processHotbar() {
        int previousSlot = dh.interactTracker.hotbar;
        dh.interactTracker.hotbar = -1;
        if (mc.player == null) {
            return;
        }
        if (mc.player.getInventory() == null) {
            return;
        }

        if (dh.climbTracker.isGrabbingLadder() && dh.climbTracker.isClaws(mc.player.getMainHandItem())) {
            return;
        }
        if (!dh.interactTracker.isActive()) {
            return;
        }

        Vec3 main = this.getAimSource(0);
        Vec3 off = this.getAimSource(1);
        Vec3 barStartos, barEndos;

        int i = dh.vrSettings.reverseHands ? 0 : 1;

        if (dh.vrSettings.vrHudLockMode == HUDLock.WRIST) {
            barStartos = convertToVec3(this.getAimRotation(1).transformProject((float) i * 0.02F, 0.05F, 0.26F, new Vector3f()));
            barEndos = convertToVec3(this.getAimRotation(1).transformProject((float) i * 0.02F, 0.05F, 0.01F, new Vector3f()));
        } else if (dh.vrSettings.vrHudLockMode == HUDLock.HAND) {
            barStartos = convertToVec3(this.getAimRotation(1).transformProject((float) i * -0.18F, 0.08F, -0.01F, new Vector3f()));
            barEndos = convertToVec3(this.getAimRotation(1).transformProject((float) i * 0.19F, 0.04F, -0.08F, new Vector3f()));
        } else {
            return; //how did u get here
        }


        Vec3 barStart = off.add(barStartos.x, barStartos.y, barStartos.z);
        Vec3 barEnd = off.add(barEndos.x, barEndos.y, barEndos.z);

        Vec3 u = barStart.subtract(barEnd);
        Vec3 pq = barStart.subtract(main);
        float dist = (float) (pq.cross(u).length() / u.length());

        if (dist > 0.06) {
            return;
        }

        float fact = (float) (pq.dot(u) / (u.x * u.x + u.y * u.y + u.z * u.z));

        if (fact < -1) {
            return;
        }

        Vec3 w2 = u.scale(fact).subtract(pq);

        Vec3 point = main.subtract(w2);
        float linelen = (float) u.length();
        float ilen = (float) barStart.subtract(point).length();
        if (fact < 0) {
            ilen *= -1;
        }
        float pos = ilen / linelen * 9;

        if (dh.vrSettings.reverseHands) {
            pos = 9 - pos;
        }

        int box = (int) floor(pos);

        if (box > 8) {
            return;
        }
        if (box < 0) {
            if (pos <= -0.5 && pos >= -1.5) //TODO fix reversed hands situation.
            {
                box = 9;
            } else {
                return;
            }
        }
        //all that maths for this.
        dh.interactTracker.hotbar = box;
        if (previousSlot != dh.interactTracker.hotbar) {
            triggerHapticPulse(0, 750);
        }
    }

    protected KeyMapping findKeyBinding(String name) {
        return Stream.concat(Arrays.stream(mc.options.keyMappings), VivecraftVRMod.hiddenKeyBindingSet.stream()).filter((kb) ->
        {
            return name.equals(kb.getName());
        }).findFirst().orElse(null);
    }

    protected void hmdSampling() {
        if (this.hmdPosSamples.size() == this.hmdAvgLength) {
            this.hmdPosSamples.removeFirst();
        }

        if (this.hmdYawSamples.size() == this.hmdAvgLength) {
            this.hmdYawSamples.removeFirst();
        }

        float f = dh.vrPlayer.vrdata_room_pre.hmd.getYaw();

        if (f < 0.0F) {
            f += 360.0F;
        }

        float f0 = abs(f - this.hmdYawLast) % 360.0F;
        float f1 = f0 > 180.0F ? 360.0F - f0 : f0;
        int i = (!(f - this.hmdYawLast >= 0.0F) || !(f - this.hmdYawLast <= 180.0F)) && (!(f - this.hmdYawLast <= -180.0F) || !(f - this.hmdYawLast >= -360.0F)) ? -1 : 1;
        this.hmdYawTotal += f1 * (float) i;
        this.hmdYawLast = f;

        float angle = this.hmdYawTotal;
        angle = angle % 360.0F;

        if (angle < 0.0F) {
            angle += 360.0F;
        }

        if (abs(angle - this.hmdYawLast) > 1.0F || this.hmdYawTotal > 100000.0F) {
            this.hmdYawTotal = this.hmdYawLast;
            logger.warn("HMD yaw desync/overflow corrected");
        }

        this.hmdPosSamples.add(dh.vrPlayer.vrdata_room_pre.hmd.getPosition());
        float f2 = 0.0F;

        if (this.hmdYawSamples.size() > 0) {
            for (float f3 : this.hmdYawSamples) {
                f2 += f3;
            }

            f2 /= (float) this.hmdYawSamples.size();
        }

        if (abs(this.hmdYawTotal - f2) > 20.0F) {
            this.trigger = true;
        }

        if (abs(this.hmdYawTotal - f2) < 1.0F) {
            this.trigger = false;
        }

        if (this.trigger || this.hmdYawSamples.isEmpty()) {
            this.hmdYawSamples.add(this.hmdYawTotal);
        }
    }

    protected void updateAim() {
        RenderPassManager.setGUIRenderPass();


        this.hmdRotation.identity();
        this.hmdRotation.set3x3(this.hmdPose);
        this.hmdPivotHistory.add(
            this.hmdRotation.transformProject(0.0F, -0.1F, 0.1F, new Vector3f())
                .add(this.hmdHistory.add(this.getCenterEyePosition()))
        );

        if (dh.vrSettings.seated) {
            this.controllerPose[0] = this.hmdPose.invert(new Matrix4f()).invert(new Matrix4f());
            this.controllerPose[1] = this.hmdPose.invert(new Matrix4f()).invert(new Matrix4f());
        }

        Matrix4f[] amatrix4f = new Matrix4f[]{new Matrix4f(), new Matrix4f()};
        Matrix4f[] amatrix4f1 = new Matrix4f[]{new Matrix4f(), new Matrix4f()};

        if (dh.vrSettings.seated) {
            amatrix4f1[0].set(this.controllerPose[0]);
        } else {
            this.controllerPose[0].mul0(this.getControllerComponentTransform(0, "handgrip"), amatrix4f1[0]);
        }

        this.handRotation[0].identity();
        this.handRotation[0].set3x3(amatrix4f1[0]);

        if (dh.vrSettings.seated) {
            amatrix4f[0].set(this.controllerPose[0]);
        } else {
            this.controllerPose[0].mul0(this.getControllerComponentTransform(0, "tip"), amatrix4f[0]);
        }

        amatrix4f[0].getTranslation(this.aimSource[0]);
        this.controllerHistory[0].add(this.getAimSource(0));
        this.controllerRotation[0].identity();
        this.controllerRotation[0].set3x3(amatrix4f[0]);

        if (dh.vrSettings.seated && mc.screen == null) {
            Matrix4f matrix4f = new Matrix4f();
            if (mc.isWindowActive()) {
                float f = 110.0F;
                float f1 = 180.0F;
                int i = mc.getWindow().getScreenHeight();

                if (i % 2 != 0) {
                    --i;
                }
                double d0 = mc.mouseHandler.xpos() / (double) mc.getWindow().getScreenWidth() * f - (f / 2.0F);
                double d3;
                Vector3f vec31 = this.getHmdVector();

                if (d0 < (double) (-dh.vrSettings.keyholeX)) {
                    this.seatedRot += 20.0F * dh.vrSettings.xSensitivity * (((float) abs(d0) - dh.vrSettings.keyholeX) / (f / 2.0F - dh.vrSettings.keyholeX));
                    this.seatedRot %= 360.0F;
                    this.hmdForwardYaw = (float) toDegrees(atan2(vec31.x, vec31.z));
                    d3 = ((int) ((double) (-dh.vrSettings.keyholeX + f / 2.0F) * (double) mc.getWindow().getScreenWidth() / (double) f) + 1);
                    d0 = -dh.vrSettings.keyholeX;
                } else if (d0 > (double) dh.vrSettings.keyholeX) {
                    this.seatedRot -= 20.0F * dh.vrSettings.xSensitivity * (((float) abs(d0) - dh.vrSettings.keyholeX) / (f / 2.0F - dh.vrSettings.keyholeX));
                    this.seatedRot %= 360.0F;
                    this.hmdForwardYaw = (float) toDegrees(atan2(vec31.x, vec31.z));
                    d3 = ((int) ((double) (dh.vrSettings.keyholeX + f / 2.0F) * (double) mc.getWindow().getScreenWidth() / (double) f) - 1);
                    d0 = dh.vrSettings.keyholeX;
                } else {
                    d3 = mc.mouseHandler.xpos();
                }

                InputSimulator.setMousePos(d3, i / 2.0D);
                glfwSetCursorPos(mc.getWindow().getWindow(), d3, i / 2.0D);
                matrix4f.rotateX((float) toRadians(-clamp(
                    -89.9D, 89.9D, this.aimPitch + (-mc.mouseHandler.ypos() / i * f1 + (f1 / 2.0F)) * 0.5F * dh.vrSettings.ySensitivity
                )));
                matrix4f.rotateY((float) toRadians(-180.0D + d0 - (double) this.hmdForwardYaw));
            }

            this.controllerRotation[0].set3x3(matrix4f);

            this.handRotation[0].set3x3(matrix4f);
        }

        Vec3 vec32 = this.getAimVector(0);
        this.aimPitch = (float) toDegrees(asin(vec32.y / vec32.length()));
        this.controllerForwardHistory[0].add(vec32);
        this.controllerUpHistory[0].add(this.controllerRotation[0].transformProject(up, new Vector3f()));

        if (dh.vrSettings.seated) {
            amatrix4f1[1].set(this.controllerPose[1]);
        } else {
            this.controllerPose[1].mul0(this.getControllerComponentTransform(1, "handgrip"), amatrix4f1[1]);
        }

        this.handRotation[1].identity();
        this.handRotation[1].set3x3(amatrix4f1[1]);

        if (dh.vrSettings.seated) {
            amatrix4f[1].set(this.controllerPose[1]);
        } else {
            this.controllerPose[1].mul0(this.getControllerComponentTransform(1, "tip"), amatrix4f[1]);
        }

        amatrix4f[1].getTranslation(this.aimSource[1]);
        this.controllerHistory[1].add(this.getAimSource(1));
        this.controllerRotation[1].identity();
        this.controllerRotation[1].set3x3(amatrix4f[1]);
        this.controllerForwardHistory[1].add(this.getAimVector(1));
        this.controllerUpHistory[1].add(this.controllerRotation[1].transformProject(up, new Vector3f()));

        if (dh.vrSettings.seated) {
            this.aimSource[1].set(this.getCenterEyePosition());
            this.aimSource[0].set(this.getCenterEyePosition());
        }

//        boolean flag = false;
//
//        if (flag)
//        {
//            this.controllerPose[2] = this.controllerPose[0];
//        }
//
//        this.controllerRotation[2].identity();
//        this.controllerRotation[2].set3x3(this.controllerPose[2]);
//
//        if ((!this.hasThirdController() || dh.vrSettings.displayMirrorMode != MirrorMode.MIXED_REALITY && dh.vrSettings.displayMirrorMode != MirrorMode.THIRD_PERSON) && !flag)
//        {
//            this.mrMovingCamActive = false;
//            this.aimSource[2] = new Vec3(dh.vrSettings.vrFixedCamposX, dh.vrSettings.vrFixedCamposY, dh.vrSettings.vrFixedCamposZ);
//        }
//        else
//        {
//            this.mrMovingCamActive = true;
//            Vector3f vector32 = this.controllerPose[2].getTranslation(new Vector3f());
//            this.aimSource[2] = convertToVec3(vector32);
//        }
    }

    public void processBindings() {
        if (!this.inputActions.isEmpty()) {
            boolean sleeping = mc.level != null && mc.player != null && mc.player.isSleeping();
            boolean gui = mc.screen != null;
            boolean toggleMovementPressed = VivecraftVRMod.keyToggleMovement.consumeClick();

            if (!mc.options.keyPickItem.isDown() && !toggleMovementPressed) {
                this.moveModeSwitchCount = 0;
            } else if (++this.moveModeSwitchCount == 80 || toggleMovementPressed) {
                if (dh.vrSettings.seated) {
                    dh.vrSettings.seatedFreeMove = !dh.vrSettings.seatedFreeMove;
                    message(Component.translatable("vivecraft.messages.movementmodeswitch", dh.vrSettings.seatedFreeMove ? Component.translatable("vivecraft.options.freemove") : Component.translatable("vivecraft.options.teleport")));
                } else if (dh.vrPlayer.isTeleportSupported()) {
                    dh.vrSettings.forceStandingFreeMove = !dh.vrSettings.forceStandingFreeMove;
                    message(Component.translatable("vivecraft.messages.movementmodeswitch", dh.vrSettings.seatedFreeMove ? Component.translatable("vivecraft.options.freemove") : Component.translatable("vivecraft.options.teleport")));
                } else if (dh.vrPlayer.isTeleportOverridden()) {
                    dh.vrPlayer.setTeleportOverride(false);
                    message(Component.translatable("vivecraft.messages.teleportdisabled"));
                } else {
                    dh.vrPlayer.setTeleportOverride(true);
                    message(Component.translatable("vivecraft.messages.teleportenabled"));
                }
            }

            Vec3 vec3 = this.getAimVector(0);
            Vec3 vec31 = this.getAimVector(1);
            float f = (float) toDegrees(atan2(-vec3.x, vec3.z));
            float f1 = (float) toDegrees(atan2(-vec31.x, vec31.z));

            if (!gui) {
                if (VivecraftVRMod.keyWalkabout.isDown()) {
                    float f2 = f;
                    ControllerType controllertype = this.findActiveBindingControllerType(VivecraftVRMod.keyWalkabout);

                    if (controllertype != null && controllertype == ControllerType.LEFT) {
                        f2 = f1;
                    }

                    if (!this.isWalkingAbout) {
                        this.isWalkingAbout = true;
                        this.walkaboutYawStart = dh.vrSettings.worldRotation - f2;
                    } else {
                        dh.vrSettings.worldRotation = this.walkaboutYawStart + f2;
                        dh.vrSettings.worldRotation %= 360.0F;
                    }
                } else {
                    this.isWalkingAbout = false;
                }

                if (VivecraftVRMod.keyRotateFree.isDown()) {
                    float f3 = f;
                    //oh this is ugly. TODO: cache which hand when binding button.
                    ControllerType controller = this.findActiveBindingControllerType(VivecraftVRMod.keyRotateFree);

                    if (controller == ControllerType.LEFT) {
                        f3 = f1;
                    }

                    if (!this.isFreeRotate) {
                        this.isFreeRotate = true;
                        this.walkaboutYawStart = dh.vrSettings.worldRotation + f3;
                    } else {
                        dh.vrSettings.worldRotation = this.walkaboutYawStart - f3;
                    }
                } else {
                    this.isFreeRotate = false;
                }
            }

            if (VivecraftVRMod.keyHotbarNext.consumeClick()) {
                this.changeHotbar(-1);
                this.triggerBindingHapticPulse(VivecraftVRMod.keyHotbarNext, 250);
            }

            if (VivecraftVRMod.keyHotbarPrev.consumeClick()) {
                this.changeHotbar(1);
                this.triggerBindingHapticPulse(VivecraftVRMod.keyHotbarPrev, 250);
            }

            if (VivecraftVRMod.keyQuickTorch.consumeClick() && mc.player != null && mc.screen == null) {
                Inventory inv = mc.player.getInventory();
                for (byte torchSlot = 0; torchSlot < 9; ++torchSlot) {
                    ItemStack itemstack = inv.getItem(torchSlot);

                    if (itemstack.getItem() instanceof BlockItem && ((BlockItem) itemstack.getItem()).getBlock() instanceof TorchBlock) {
                        int previous = inv.selected;
                        inv.selected = torchSlot;
                        mc.startUseItem();
                        // switch back immediately
                        inv.selected = previous;
                        torchSlot = Byte.MAX_VALUE - 1;
                    }
                }
            }

            if (gui && !sleeping && mc.options.keyUp.isDown() && !(mc.screen instanceof WinScreen) && mc.player != null) {
                mc.player.closeContainer();
            }

            if (mc.screen instanceof AbstractContainerScreen && mc.options.keyInventory.consumeClick() && mc.player != null) {
                mc.player.closeContainer();
            }

            if (mc.screen instanceof ChatScreen && mc.options.keyChat.consumeClick()) {
                mc.setScreen(null);
            }

            if (dh.vrSettings.worldRotationIncrement == 0.0F) {
                float f4 = this.getInputAction(VivecraftVRMod.keyRotateAxis).getAxis2DUseTracked().x();

                if (f4 == 0.0F) {
                    f4 = this.getInputAction(VivecraftVRMod.keyFreeMoveRotate).getAxis2DUseTracked().x();
                }

                if (f4 != 0.0F) {
                    float f8 = 10.0F * f4;
                    dh.vrSettings.worldRotation -= f8;
                    dh.vrSettings.worldRotation %= 360.0F;
                }
            } else if (VivecraftVRMod.keyRotateAxis.consumeClick() || VivecraftVRMod.keyFreeMoveRotate.consumeClick()) {
                float f5 = this.getInputAction(VivecraftVRMod.keyRotateAxis).getAxis2D(false).x();

                if (f5 == 0.0F) {
                    f5 = this.getInputAction(VivecraftVRMod.keyFreeMoveRotate).getAxis2D(false).x();
                }

                if (abs(f5) > 0.5F) {
                    dh.vrSettings.worldRotation -= dh.vrSettings.worldRotationIncrement * signum(f5);
                    dh.vrSettings.worldRotation %= 360.0F;
                }
            }

            if (dh.vrSettings.worldRotationIncrement == 0.0F) {
                float f6 = VivecraftMovementInput.getMovementAxisValue(VivecraftVRMod.keyRotateLeft);

                if (f6 > 0.0F) {
                    float f9 = 5.0F;

                    if (f6 > 0.0F) {
                        f9 = 10.0F * f6;
                    }

                    dh.vrSettings.worldRotation += f9;
                    dh.vrSettings.worldRotation %= 360.0F;
                }
            } else if (VivecraftVRMod.keyRotateLeft.consumeClick()) {
                dh.vrSettings.worldRotation += dh.vrSettings.worldRotationIncrement;
                dh.vrSettings.worldRotation %= 360.0F;
            }

            if (dh.vrSettings.worldRotationIncrement == 0.0F) {
                float f7 = VivecraftMovementInput.getMovementAxisValue(VivecraftVRMod.keyRotateRight);

                if (f7 > 0.0F) {
                    float f10 = 5.0F;

                    if (f7 > 0.0F) {
                        f10 = 10.0F * f7;
                    }

                    dh.vrSettings.worldRotation -= f10;
                    dh.vrSettings.worldRotation %= 360.0F;
                }
            } else if (VivecraftVRMod.keyRotateRight.consumeClick()) {
                dh.vrSettings.worldRotation -= dh.vrSettings.worldRotationIncrement;
                dh.vrSettings.worldRotation %= 360.0F;
            }

            this.seatedRot = dh.vrSettings.worldRotation;

            if (VivecraftVRMod.keyRadialMenu.consumeClick() && !gui) {
                ControllerType controller = this.findActiveBindingControllerType(VivecraftVRMod.keyRadialMenu);

                if (controller != null) {
                    RadialHandler.setOverlayShowing(!RadialHandler.isShowing(), controller);
                }
            }

            if (VivecraftVRMod.keySwapMirrorView.consumeClick()) {
                if (dh.vrSettings.displayMirrorMode == MirrorMode.THIRD_PERSON) {
                    dh.vrSettings.displayMirrorMode = MirrorMode.FIRST_PERSON;
                } else if (dh.vrSettings.displayMirrorMode == MirrorMode.FIRST_PERSON) {
                    dh.vrSettings.displayMirrorMode = MirrorMode.THIRD_PERSON;
                }

                if (!ShadersHelper.isShaderActive()) {
                    dh.vrRenderer.reinitFrameBuffers("Mirror Setting Changed");
                } else {
                    // in case if the last third person mirror was mixed reality
                    dh.vrRenderer.resizeFrameBuffers("Mirror Setting Changed");
                }
            }

            if (VivecraftVRMod.keyToggleKeyboard.consumeClick()) {
                KeyboardHandler.setOverlayShowing(!KeyboardHandler.isShowing());
            }

            if (VivecraftVRMod.keyMoveThirdPersonCam.consumeClick() && !dh.kiosk && !dh.vrSettings.seated && (dh.vrSettings.displayMirrorMode == MirrorMode.MIXED_REALITY || dh.vrSettings.displayMirrorMode == MirrorMode.THIRD_PERSON)) {
                ControllerType controllertype2 = this.findActiveBindingControllerType(VivecraftVRMod.keyMoveThirdPersonCam);

                if (controllertype2 != null) {
                    VRHotkeys.startMovingThirdPersonCam(controllertype2.ordinal(), Triggerer.BINDING);
                }
            }

            if (!VivecraftVRMod.keyMoveThirdPersonCam.isDown() && VRHotkeys.isMovingThirdPersonCam() && VRHotkeys.getMovingThirdPersonCamTriggerer() == Triggerer.BINDING) {
                VRHotkeys.stopMovingThirdPersonCam();
                dh.vrSettings.saveOptions();
            }

            if (VRHotkeys.isMovingThirdPersonCam() && VRHotkeys.getMovingThirdPersonCamTriggerer() == Triggerer.MENUBUTTON && VivecraftVRMod.keyMenuButton.consumeClick()) {
                VRHotkeys.stopMovingThirdPersonCam();
                dh.vrSettings.saveOptions();
            }

            if (KeyboardHandler.isShowing() && mc.screen == null && VivecraftVRMod.keyMenuButton.consumeClick()) {
                KeyboardHandler.setOverlayShowing(false);
            }

            if (RadialHandler.isShowing() && VivecraftVRMod.keyMenuButton.consumeClick()) {
                RadialHandler.setOverlayShowing(false, (ControllerType) null);
            }

            if (VivecraftVRMod.keyMenuButton.consumeClick()) {
                if (!gui) {
                    if (!dh.kiosk) {
                        mc.pauseGame(false);
                    }
                } else {
                    InputSimulator.pressKey(GLFW_KEY_ESCAPE);
                    InputSimulator.releaseKey(GLFW_KEY_ESCAPE);
                }

                KeyboardHandler.setOverlayShowing(false);
            }

            if (VivecraftVRMod.keyTogglePlayerList.consumeClick()) {
                ((GuiExtension) mc.gui).vivecraft$setShowPlayerList(!((GuiExtension) mc.gui).vivecraft$getShowPlayerList());
            }

            if (VivecraftVRMod.keyToggleHandheldCam.consumeClick() && mc.player != null) {
                dh.cameraTracker.toggleVisibility();

                if (dh.cameraTracker.isVisible()) {
                    ControllerType hand = this.findActiveBindingControllerType(VivecraftVRMod.keyToggleHandheldCam);

                    if (hand == null) {
                        hand = ControllerType.RIGHT;
                    }

                    VRDevicePose vrdata$vrdevicepose = dh.vrPlayer.vrdata_world_pre.getController(hand.ordinal());
                    dh.cameraTracker.setPosition(vrdata$vrdevicepose.getPosition());
                    dh.cameraTracker.setRotation(new Quaternionf().setFromNormalized(vrdata$vrdevicepose.getMatrix()));
                }
            }

            if (VivecraftVRMod.keyQuickHandheldCam.consumeClick() && mc.player != null) {
                if (!dh.cameraTracker.isVisible()) {
                    dh.cameraTracker.toggleVisibility();
                }

                ControllerType hand = this.findActiveBindingControllerType(VivecraftVRMod.keyQuickHandheldCam);

                if (hand == null) {
                    hand = ControllerType.RIGHT;
                }

                VRDevicePose vrdata$vrdevicepose1 = dh.vrPlayer.vrdata_world_pre.getController(hand.ordinal());
                dh.cameraTracker.setPosition(vrdata$vrdevicepose1.getPosition());
                dh.cameraTracker.setRotation(new Quaternionf().setFromNormalized(vrdata$vrdevicepose1.getMatrix()));
                dh.cameraTracker.startMoving(hand.ordinal(), true);
            }

            if (!VivecraftVRMod.keyQuickHandheldCam.isDown() && dh.cameraTracker.isMoving() && dh.cameraTracker.isQuickMode() && mc.player != null) {
                dh.cameraTracker.stopMoving();
                dh.grabScreenShot = true;
            }

            GuiHandler.processBindingsGui();
            RadialHandler.processBindings();
            KeyboardHandler.processBindings();
            dh.interactTracker.processBindings();
        }
    }

    public void populateInputActions() {
        Map<String, ActionParams> actionParams = this.getSpecialActionParams();

        // iterate over all minecraft keys, and our hidden keys
        for (KeyMapping keymapping : Stream.concat(Arrays.stream(mc.options.keyMappings), VivecraftVRMod.hiddenKeyBindingSet.stream()).toList()) {
            ActionParams params = actionParams.getOrDefault(keymapping.getName(), new ActionParams("optional", "boolean", null));
            VRInputAction action = new VRInputAction(keymapping, params.requirement, params.type, params.actionSetOverride);
            this.inputActions.put(action.name, action);
        }

        for (VRInputAction action : this.inputActions.values()) {
            this.inputActionsByKeyBinding.put(action.keyBinding.getName(), action);
        }

        this.getInputAction(VivecraftVRMod.keyVRInteract).setPriority(5).setEnabled(false);
        this.getInputAction(VivecraftVRMod.keyClimbeyGrab).setPriority(10).setEnabled(false);
        this.getInputAction(VivecraftVRMod.keyClimbeyJump).setEnabled(false);
        this.getInputAction(GuiHandler.keyKeyboardClick).setPriority(50);
        this.getInputAction(GuiHandler.keyKeyboardShift).setPriority(50);
    }

    public Map<String, ActionParams> getSpecialActionParams() {
        Map<String, ActionParams> map = new HashMap<>();
        this.addActionParams(map, mc.options.keyUp, "optional", "vector1", null);
        this.addActionParams(map, mc.options.keyDown, "optional", "vector1", null);
        this.addActionParams(map, mc.options.keyLeft, "optional", "vector1", null);
        this.addActionParams(map, mc.options.keyRight, "optional", "vector1", null);
        this.addActionParams(map, mc.options.keyInventory, "suggested", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, mc.options.keyAttack, "suggested", "boolean", null);
        this.addActionParams(map, mc.options.keyUse, "suggested", "boolean", null);
        this.addActionParams(map, mc.options.keyChat, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, VivecraftVRMod.keyHotbarScroll, "optional", "vector2", null);
        this.addActionParams(map, VivecraftVRMod.keyHotbarSwipeX, "optional", "vector2", null);
        this.addActionParams(map, VivecraftVRMod.keyHotbarSwipeY, "optional", "vector2", null);
        this.addActionParams(map, VivecraftVRMod.keyMenuButton, "suggested", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, VivecraftVRMod.keyTeleportFallback, "suggested", "vector1", null);
        this.addActionParams(map, VivecraftVRMod.keyFreeMoveRotate, "optional", "vector2", null);
        this.addActionParams(map, VivecraftVRMod.keyFreeMoveStrafe, "optional", "vector2", null);
        this.addActionParams(map, VivecraftVRMod.keyRotateLeft, "optional", "vector1", null);
        this.addActionParams(map, VivecraftVRMod.keyRotateRight, "optional", "vector1", null);
        this.addActionParams(map, VivecraftVRMod.keyRotateAxis, "optional", "vector2", null);
        this.addActionParams(map, VivecraftVRMod.keyRadialMenu, "suggested", "boolean", null);
        this.addActionParams(map, VivecraftVRMod.keySwapMirrorView, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, VivecraftVRMod.keyToggleKeyboard, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, VivecraftVRMod.keyMoveThirdPersonCam, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, VivecraftVRMod.keyToggleHandheldCam, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, VivecraftVRMod.keyQuickHandheldCam, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, VivecraftVRMod.keyTrackpadTouch, "optional", "boolean", VRInputActionSet.TECHNICAL);
//        this.addActionParams(map, VivecraftVRMod.keyExtensionMainThumb, "optional", "boolean", VRInputActionSet.GLOBAL);
//        this.addActionParams(map, VivecraftVRMod.keyExtensionMainIndex, "optional", "boolean", VRInputActionSet.GLOBAL);
//        this.addActionParams(map, VivecraftVRMod.keyExtensionMainMiddle, "optional", "boolean", VRInputActionSet.GLOBAL);
//        this.addActionParams(map, VivecraftVRMod.keyExtensionMainRing, "optional", "boolean", VRInputActionSet.GLOBAL);
//        this.addActionParams(map, VivecraftVRMod.keyExtensionMainLittle, "optional", "boolean", VRInputActionSet.GLOBAL);
//        this.addActionParams(map, VivecraftVRMod.keyExtensionOffThumb, "optional", "boolean", VRInputActionSet.GLOBAL);
//        this.addActionParams(map, VivecraftVRMod.keyExtensionOffIndex, "optional", "boolean", VRInputActionSet.GLOBAL);
//        this.addActionParams(map, VivecraftVRMod.keyExtensionOffMiddle, "optional", "boolean", VRInputActionSet.GLOBAL);
//        this.addActionParams(map, VivecraftVRMod.keyExtensionOffRing, "optional", "boolean", VRInputActionSet.GLOBAL);
//        this.addActionParams(map, VivecraftVRMod.keyExtensionOffLittle, "optional", "boolean", VRInputActionSet.GLOBAL);
//        this.addActionParams(map, VivecraftVRMod.keyCurlMainThumb, "optional", "boolean", VRInputActionSet.GLOBAL);
//        this.addActionParams(map, VivecraftVRMod.keyCurlMainIndex, "optional", "boolean", VRInputActionSet.GLOBAL);
//        this.addActionParams(map, VivecraftVRMod.keyCurlMainMiddle, "optional", "boolean", VRInputActionSet.GLOBAL);
//        this.addActionParams(map, VivecraftVRMod.keyCurlMainRing, "optional", "boolean", VRInputActionSet.GLOBAL);
//        this.addActionParams(map, VivecraftVRMod.keyCurlMainLittle, "optional", "boolean", VRInputActionSet.GLOBAL);
//        this.addActionParams(map, VivecraftVRMod.keyCurlOffThumb, "optional", "boolean", VRInputActionSet.GLOBAL);
//        this.addActionParams(map, VivecraftVRMod.keyCurlOffIndex, "optional", "boolean", VRInputActionSet.GLOBAL);
//        this.addActionParams(map, VivecraftVRMod.keyCurlOffMiddle, "optional", "boolean", VRInputActionSet.GLOBAL);
//        this.addActionParams(map, VivecraftVRMod.keyCurlOffRing, "optional", "boolean", VRInputActionSet.GLOBAL);
//        this.addActionParams(map, VivecraftVRMod.keyCurlOffLittle, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, VivecraftVRMod.keyVRInteract, "suggested", "boolean", VRInputActionSet.CONTEXTUAL);
        this.addActionParams(map, VivecraftVRMod.keyClimbeyGrab, "suggested", "boolean", null);
        this.addActionParams(map, VivecraftVRMod.keyClimbeyJump, "suggested", "boolean", null);
        this.addActionParams(map, GuiHandler.keyLeftClick, "suggested", "boolean", null);
        this.addActionParams(map, GuiHandler.keyScrollAxis, "optional", "vector2", null);
        this.addActionParams(map, GuiHandler.keyRightClick, "suggested", "boolean", null);
        this.addActionParams(map, GuiHandler.keyShift, "suggested", "boolean", null);
        this.addActionParams(map, GuiHandler.keyKeyboardClick, "suggested", "boolean", null);
        this.addActionParams(map, GuiHandler.keyKeyboardShift, "suggested", "boolean", null);
        File file = new File("customactionsets.txt");

        if (file.exists()) {
            logger.info("Loading custom action set definitions...");
            String line;

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                while ((line = br.readLine()) != null) {
                    String[] tokens = line.split(":", 2);

                    if (tokens.length < 2) {
                        logger.warn("Invalid tokens: {}", line);
                    } else {
                        KeyMapping keymapping = this.findKeyBinding(tokens[0]);

                        if (keymapping == null) {
                            logger.warn("Unknown key binding: {}", tokens[0]);
                        } else if (VivecraftVRMod.allKeyBindingSet.contains(keymapping)) {
                            logger.warn("NO! Don't touch Vivecraft bindings!");
                        } else {
                            VRInputActionSet actionSet = switch (tokens[1].toLowerCase()) {
                                case "ingame" -> VRInputActionSet.INGAME;
                                case "gui" -> VRInputActionSet.GUI;
                                case "global" -> VRInputActionSet.GLOBAL;
                                default -> null;
                            };

                            if (actionSet == null) {
                                logger.warn("Unknown action set: {}", tokens[1]);
                            } else {
                                this.addActionParams(map, keymapping, "optional", "boolean", actionSet);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return map;
    }

    protected void changeHotbar(int dir) {
        if (mc.player != null && (!dh.climbTracker.isGrabbingLadder() || !dh.climbTracker.isClaws(mc.player.getMainHandItem()))) {
            if (mc.screen == null) {
                InputSimulator.scrollMouse(0.0D, (double) (dir * 4));
            } else {
                mc.player.getInventory().swapPaint((double) dir);
            }
        }
    }

    private void addActionParams(Map<String, ActionParams> map, KeyMapping keymapping, String requirement, String type, @Nullable VRInputActionSet actionSetOverride) {
        ActionParams actionparams = new ActionParams(requirement, type, actionSetOverride);
        map.put(keymapping.getName(), actionparams);
    }

    protected abstract void triggerBindingHapticPulse(KeyMapping binding, int duration);

    protected abstract ControllerType findActiveBindingControllerType(KeyMapping binding);

    public abstract void poll(long frameIndex);

    public abstract Vector2f getPlayAreaSize();

    public abstract boolean init();

    public abstract boolean postinit() throws RenderConfigException;

    public abstract Matrix4f getControllerComponentTransform(int controllerIndex, String componentName);

    public abstract ControllerType getOriginControllerType(long inputValueHandle);

    public abstract boolean hasThirdController();

    public abstract List<Long> getOrigins(VRInputAction action);

    public abstract String getOriginName(long l);

    public abstract VRRenderer createVRRenderer();

    public abstract boolean isActive();
}
