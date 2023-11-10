package org.vivecraft.client_vr.settings;

import com.google.common.util.concurrent.Runnables;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.network.chat.Component;
import org.joml.Math;
import org.joml.*;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client.utils.LangHelper;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.MinecraftExtension;
import org.vivecraft.common.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class VRHotkeys {
    static long nextRead = 0L;
    static final long COOLOFF_PERIOD_MILLIS = 500L;
    static boolean debug = false;
    private static int startController;
    private static VRData.VRDevicePose startControllerPose;
    private static final Vector3f startCampos = new Vector3f();
    private static final Quaternionf startCamrotQuat = new Quaternionf();
    private static Triggerer camTriggerer;
    private static final File ExternalCameraCFG = new File("ExternalCamera.cfg");

    public static boolean handleKeyboardInputs(int key, int scanCode, int action, int modifiers) {
        if (nextRead != 0L && System.currentTimeMillis() < nextRead) {
            return false;
        } else {
            Minecraft minecraft = Minecraft.getInstance();
            ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
            boolean gotKey = false;

            if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_RIGHT_SHIFT && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL)) {
                dataholder.vrSettings.storeDebugAim = true;
                minecraft.gui.getChat().addMessage(Component.translatable("vivecraft.messages.showaim"));
                gotKey = true;
            }

            if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_B && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL)) {
                dataholder.vrSettings.walkUpBlocks = !dataholder.vrSettings.walkUpBlocks;
                minecraft.gui.getChat().addMessage(Component.translatable("vivecraft.messages.walkupblocks", dataholder.vrSettings.walkUpBlocks ? LangHelper.getYes() : LangHelper.getNo()));
                gotKey = true;
            }

            if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_I && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL)) {
                dataholder.vrSettings.inertiaFactor = dataholder.vrSettings.inertiaFactor.getNext();
                minecraft.gui.getChat().addMessage(Component.translatable("vivecraft.messages.playerinertia", Component.translatable(dataholder.vrSettings.inertiaFactor.getLangKey())));

                gotKey = true;
            }

            if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_R && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL)) {
                if (dataholder.vrPlayer.isTeleportOverridden()) {
                    dataholder.vrPlayer.setTeleportOverride(false);
                    minecraft.gui.getChat().addMessage(Component.translatable("vivecraft.messages.teleportdisabled"));
                } else {
                    dataholder.vrPlayer.setTeleportOverride(true);
                    minecraft.gui.getChat().addMessage(Component.translatable("vivecraft.messages.teleportenabled"));
                }

                gotKey = true;
            }

            if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_HOME && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL)) {
                Utils.convertVector(dataholder.vrPlayer.vrdata_room_pre.getController(0).getPosition(), dataholder.vrSettings.vrFixedCampos);
                dataholder.vrSettings.vrFixedCamrotQuat.setFromNormalized(dataholder.vrPlayer.vrdata_room_pre.getController(0).getMatrix(new Matrix4f()).transpose()).invert();
                gotKey = true;
            }

            if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_F12 && debug) {
                minecraft.setScreen(new WinScreen(false, Runnables.doNothing()));
                gotKey = true;
            }

            if ((minecraft.level == null || minecraft.screen != null) && action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_F5) {
                dataholder.vrSettings.setOptionValue(VRSettings.VrOptions.MIRROR_DISPLAY);
                ((MinecraftExtension) minecraft).vivecraft$notifyMirror(dataholder.vrSettings.getButtonDisplayString(VRSettings.VrOptions.MIRROR_DISPLAY), false, 3000);
            }

            // toggle VR with a keyboard shortcut
            if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_F7 && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL)) {
                VRState.vrEnabled = !VRState.vrEnabled;
                ClientDataHolderVR.getInstance().vrSettings.vrEnabled = VRState.vrEnabled;
                gotKey = true;
            }

            if (gotKey) {
                dataholder.vrSettings.saveOptions();
            }

            return gotKey;
        }
    }

    public static void handleMRKeys() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        final Quaternionf cam;
        final Vector3f pos;
        if (dataholder.vr.mrMovingCamActive) {
            cam = dataholder.vrSettings.mrMovingCamOffsetRotQuat;
            pos = dataholder.vrSettings.mrMovingCamOffset;
        } else {
            cam = dataholder.vrSettings.vrFixedCamrotQuat;
            pos = dataholder.vrSettings.vrFixedCampos;
        }
        boolean flag = false;

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_LEFT) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && !MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            pos.add(cam.transformUnit(-0.01F, 0.0F, 0.0F, new Vector3f()));
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && !MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            pos.add(cam.transformUnit(0.01F, 0.0F, 0.0F, new Vector3f()));
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_UP) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && !MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            pos.add(cam.transformUnit(0.0F, 0.0F, -0.01F, new Vector3f()));
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_DOWN) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && !MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            pos.add(cam.transformUnit(0.0F, 0.0F, 0.01F, new Vector3f()));
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_PAGE_UP) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && !MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            pos.add(cam.transformUnit(0.0F, 0.01F, 0.0F, new Vector3f()));
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_PAGE_DOWN) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && !MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            pos.add(cam.transformUnit(0.0F, -0.01F, 0.0F, new Vector3f()));
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_UP) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            cam.rotateX(Math.toRadians(0.5F));
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_DOWN) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            cam.rotateX(Math.toRadians(-0.5F));
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_LEFT) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            cam.rotateY(Math.toRadians(0.5F));
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            cam.rotateY(Math.toRadians(-0.5F));
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_PAGE_UP) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            cam.rotateZ(Math.toRadians(0.5F));
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_PAGE_DOWN) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            cam.rotateZ(Math.toRadians(-0.5F));
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_INSERT) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && !MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            minecraft.options.fov().set(minecraft.options.fov().get() + 1);
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_DELETE) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && !MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            minecraft.options.fov().set(minecraft.options.fov().get() - 1);
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_INSERT) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            ++dataholder.vrSettings.mixedRealityFov;
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_DELETE) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            --dataholder.vrSettings.mixedRealityFov;
            flag = true;
        }

        if (flag) {
            dataholder.vrSettings.saveOptions();
            Minecraft.getInstance().gui.getChat().addMessage(Component.literal(LangHelper.get("vivecraft.messages.coords", pos.x, pos.y, pos.z)));
            Vector3fc angles = cam.getEulerAnglesYXZ(new Vector3f()); // TODO: verify
            Minecraft.getInstance().gui.getChat().addMessage(Component.literal(LangHelper.get("vivecraft.messages.angles", angles.x(), angles.y(), angles.z())));
        }
    }

    public static void updateMovingThirdPersonCam() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();

        if (startControllerPose != null) {
            VRData.VRDevicePose vrDevPose = dataholder.vrPlayer.vrdata_room_pre.getController(startController);
            Vector3f vrOldPosition = Utils.convertVector(startControllerPose.getPosition(), new Vector3f());
            Vector3f vrDevPosition = Utils.convertVector(vrDevPose.getPosition(), new Vector3f()).sub(vrOldPosition);
            Matrix4f matrix4f = vrDevPose.getMatrix(new Matrix4f()).transpose().mul0(startControllerPose.getMatrix(new Matrix4f()).transpose().invert());
            matrix4f.transformProject(startCampos.sub(vrOldPosition, vrOldPosition), dataholder.vrSettings.vrFixedCampos).sub(vrOldPosition).add(vrDevPosition).add(startCampos);
            startCamrotQuat.mul(dataholder.vrSettings.vrFixedCamrotQuat.setFromNormalized(matrix4f).invert(), dataholder.vrSettings.vrFixedCamrotQuat);
        }
    }

    public static void startMovingThirdPersonCam(int controller, Triggerer triggerer) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        startController = controller;
        startControllerPose = dataholder.vrPlayer.vrdata_room_pre.getController(controller);
        startCampos.set(dataholder.vrSettings.vrFixedCampos);
        startCamrotQuat.set(dataholder.vrSettings.vrFixedCamrotQuat);
        camTriggerer = triggerer;
    }

    public static void stopMovingThirdPersonCam() {
        startControllerPose = null;
    }

    public static boolean isMovingThirdPersonCam() {
        return startControllerPose != null;
    }

    public static int getMovingThirdPersonCamController() {
        return startController;
    }

    public static Triggerer getMovingThirdPersonCamTriggerer() {
        return camTriggerer;
    }

    public static void loadExternalCameraConfig() {
        if (ExternalCameraCFG.exists()) {
            float x = 0.0F;
            float y = 0.0F;
            float z = 0.0F;
            float rx = 0.0F;
            float ry = 0.0F;
            float rz = 0.0F;
            float fov = 40.0F;

            try (BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(new FileInputStream(ExternalCameraCFG), StandardCharsets.UTF_8))) {
                String s;
                while ((s = bufferedreader.readLine()) != null) {
                    String[] astring = s.split("=", 2);
                    String s1 = astring[0];

                    switch (s1) {
                        case "x" -> x = Float.parseFloat(astring[1]);
                        case "y" -> y = Float.parseFloat(astring[1]);
                        case "z" -> z = Float.parseFloat(astring[1]);
                        case "rx" -> rx = Math.toRadians(Float.parseFloat(astring[1]));
                        case "ry" -> ry = Math.toRadians(Float.parseFloat(astring[1]));
                        case "rz" -> rz = Math.toRadians(Float.parseFloat(astring[1]));
                        case "fov" -> fov = Float.parseFloat(astring[1]);
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                return;
            }

            Minecraft minecraft = Minecraft.getInstance();
            ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
            dataholder.vrSettings.mrMovingCamOffsetRotQuat.set(switch (dataholder.vrSettings.externalCameraAngleOrder) {
                case XYZ -> dataholder.vrSettings.vrFixedCamrotQuat.rotationXYZ(rx, ry, rz);
                case ZYX -> dataholder.vrSettings.vrFixedCamrotQuat.rotationZYX(rz, ry, rx);
                case YXZ -> dataholder.vrSettings.vrFixedCamrotQuat.rotationYXZ(ry, rx, rz);
                case ZXY -> // TODO: add rotationZXY to JOML
                    dataholder.vrSettings.vrFixedCamrotQuat.rotationZ(rz).rotateX(rx).rotateY(ry);
                case YZX -> // TODO: add rotationYZX to JOML
                    dataholder.vrSettings.vrFixedCamrotQuat.rotationY(ry).rotateZ(rz).rotateX(rx);
                case XZY -> // TODO: add rotationXZY to JOML
                    // default angle order
                    dataholder.vrSettings.vrFixedCamrotQuat.rotationX(rx).rotateZ(rz).rotateY(ry);
            });
            dataholder.vrSettings.mrMovingCamOffset.set(x, y, z);
            dataholder.vrSettings.vrFixedCampos.set(x, y, z);
            dataholder.vrSettings.mixedRealityFov = fov;
        }
    }

    public static boolean hasExternalCameraConfig() {
        return ExternalCameraCFG.exists();
    }

    public enum Triggerer {
        BINDING,
        MENUBUTTON,
        INTERACTION
    }
}
