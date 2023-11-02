package org.vivecraft.client_vr.settings;

import com.google.common.util.concurrent.Runnables;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client.utils.LangHelper;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.MinecraftExtension;
import org.vivecraft.common.utils.math.*;

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
    private static float startCamposX;
    private static float startCamposY;
    private static float startCamposZ;
    private static Quaternion startCamrotQuat;
    private static Triggerer camTriggerer;

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
                snapMRCam(0);
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
        boolean flag = false;

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_LEFT) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && !MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            adjustCamPos(new Vector3(-0.01F, 0.0F, 0.0F));
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && !MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            adjustCamPos(new Vector3(0.01F, 0.0F, 0.0F));
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_UP) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && !MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            adjustCamPos(new Vector3(0.0F, 0.0F, -0.01F));
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_DOWN) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && !MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            adjustCamPos(new Vector3(0.0F, 0.0F, 0.01F));
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_PAGE_UP) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && !MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            adjustCamPos(new Vector3(0.0F, 0.01F, 0.0F));
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_PAGE_DOWN) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && !MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            adjustCamPos(new Vector3(0.0F, -0.01F, 0.0F));
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_UP) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            adjustCamRot(Axis.PITCH, 0.5F);
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_DOWN) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            adjustCamRot(Axis.PITCH, -0.5F);
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_LEFT) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            adjustCamRot(Axis.YAW, 0.5F);
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            adjustCamRot(Axis.YAW, -0.5F);
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_PAGE_UP) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            adjustCamRot(Axis.ROLL, 0.5F);
            flag = true;
        }

        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_PAGE_DOWN) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            adjustCamRot(Axis.ROLL, -0.5F);
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

            if (dataholder.vr.mrMovingCamActive) {
                Minecraft.getInstance().gui.getChat().addMessage(Component.literal(LangHelper.get("vivecraft.messages.coords", dataholder.vrSettings.mrMovingCamOffsetX, dataholder.vrSettings.mrMovingCamOffsetY, dataholder.vrSettings.mrMovingCamOffsetZ)));
                Angle angle = dataholder.vrSettings.mrMovingCamOffsetRotQuat.toEuler();
                Minecraft.getInstance().gui.getChat().addMessage(Component.literal(LangHelper.get("vivecraft.messages.angles", angle.getPitch(), angle.getYaw(), angle.getRoll())));
            } else {
                Minecraft.getInstance().gui.getChat().addMessage(Component.literal(LangHelper.get("vivecraft.messages.coords", dataholder.vrSettings.vrFixedCamposX, dataholder.vrSettings.vrFixedCamposY, dataholder.vrSettings.vrFixedCamposZ)));
                Angle angle1 = dataholder.vrSettings.vrFixedCamrotQuat.toEuler();
                Minecraft.getInstance().gui.getChat().addMessage(Component.literal(LangHelper.get("vivecraft.messages.angles", angle1.getPitch(), angle1.getYaw(), angle1.getRoll())));
            }
        }
    }

    private static void adjustCamPos(Vector3 offset) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();

        if (dataholder.vr.mrMovingCamActive) {
            offset = dataholder.vrSettings.mrMovingCamOffsetRotQuat.multiply(offset);
            dataholder.vrSettings.mrMovingCamOffsetX += offset.getX();
            dataholder.vrSettings.mrMovingCamOffsetY += offset.getY();
            dataholder.vrSettings.mrMovingCamOffsetZ += offset.getZ();
        } else {
            offset = dataholder.vrSettings.vrFixedCamrotQuat.inverse().multiply(offset);
            dataholder.vrSettings.vrFixedCamposX += offset.getX();
            dataholder.vrSettings.vrFixedCamposY += offset.getY();
            dataholder.vrSettings.vrFixedCamposZ += offset.getZ();
        }
    }

    private static void adjustCamRot(Axis axis, float degrees) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();

        if (dataholder.vr.mrMovingCamActive) {
            dataholder.vrSettings.mrMovingCamOffsetRotQuat.set(dataholder.vrSettings.mrMovingCamOffsetRotQuat.rotate(axis, degrees, true));
        } else {
            dataholder.vrSettings.vrFixedCamrotQuat.set(dataholder.vrSettings.vrFixedCamrotQuat.rotate(axis, degrees, false));
        }
    }

    public static void snapMRCam(int controller) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        Vec3 vec3 = dataholder.vrPlayer.vrdata_room_pre.getController(controller).getPosition();
        dataholder.vrSettings.vrFixedCamposX = (float) vec3.x;
        dataholder.vrSettings.vrFixedCamposY = (float) vec3.y;
        dataholder.vrSettings.vrFixedCamposZ = (float) vec3.z;
        Quaternion quaternion = new Quaternion(Utils.convertOVRMatrix(dataholder.vrPlayer.vrdata_room_pre.getController(controller).getMatrix()));
        dataholder.vrSettings.vrFixedCamrotQuat.set(quaternion);
    }

    public static void updateMovingThirdPersonCam() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();

        if (startControllerPose != null) {
            VRData.VRDevicePose vrdata$vrdevicepose = dataholder.vrPlayer.vrdata_room_pre.getController(startController);
            Vec3 vec3 = startControllerPose.getPosition();
            Vec3 vec31 = vrdata$vrdevicepose.getPosition().subtract(vec3);
            Matrix4f matrix4f = Matrix4f.multiply(vrdata$vrdevicepose.getMatrix(), startControllerPose.getMatrix().inverted());
            Vector3 vector3 = new Vector3(startCamposX - (float) vec3.x, startCamposY - (float) vec3.y, startCamposZ - (float) vec3.z);
            Vector3 vector31 = matrix4f.transform(vector3);
            dataholder.vrSettings.vrFixedCamposX = startCamposX + (float) vec31.x + (vector31.getX() - vector3.getX());
            dataholder.vrSettings.vrFixedCamposY = startCamposY + (float) vec31.y + (vector31.getY() - vector3.getY());
            dataholder.vrSettings.vrFixedCamposZ = startCamposZ + (float) vec31.z + (vector31.getZ() - vector3.getZ());
            dataholder.vrSettings.vrFixedCamrotQuat.set(startCamrotQuat.multiply(new Quaternion(Utils.convertOVRMatrix(matrix4f))));
        }
    }

    public static void startMovingThirdPersonCam(int controller, Triggerer triggerer) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        startController = controller;
        startControllerPose = dataholder.vrPlayer.vrdata_room_pre.getController(controller);
        startCamposX = dataholder.vrSettings.vrFixedCamposX;
        startCamposY = dataholder.vrSettings.vrFixedCamposY;
        startCamposZ = dataholder.vrSettings.vrFixedCamposZ;
        startCamrotQuat = dataholder.vrSettings.vrFixedCamrotQuat.copy();
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
        File file1 = new File("ExternalCamera.cfg");

        if (file1.exists()) {
            float f = 0.0F;
            float f1 = 0.0F;
            float f2 = 0.0F;
            float f3 = 0.0F;
            float f4 = 0.0F;
            float f5 = 0.0F;
            float f6 = 40.0F;
            String s;

            try (BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(new FileInputStream(file1), StandardCharsets.UTF_8))) {
                while ((s = bufferedreader.readLine()) != null) {
                    String[] astring = s.split("=", 2);
                    String s1 = astring[0];

                    switch (s1) {
                        case "x":
                            f = Float.parseFloat(astring[1]);
                            break;

                        case "y":
                            f1 = Float.parseFloat(astring[1]);
                            break;

                        case "z":
                            f2 = Float.parseFloat(astring[1]);
                            break;

                        case "rx":
                            f3 = Float.parseFloat(astring[1]);
                            break;

                        case "ry":
                            f4 = Float.parseFloat(astring[1]);
                            break;

                        case "rz":
                            f5 = Float.parseFloat(astring[1]);
                            break;

                        case "fov":
                            f6 = Float.parseFloat(astring[1]);
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                return;
            }

            Minecraft minecraft = Minecraft.getInstance();
            ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
            Quaternion quaternion = new Quaternion(f3, f4, f5, dataholder.vrSettings.externalCameraAngleOrder);
            dataholder.vrSettings.mrMovingCamOffsetX = f;
            dataholder.vrSettings.mrMovingCamOffsetY = f1;
            dataholder.vrSettings.mrMovingCamOffsetZ = f2;
            dataholder.vrSettings.mrMovingCamOffsetRotQuat.set(quaternion);
            dataholder.vrSettings.vrFixedCamposX = f;
            dataholder.vrSettings.vrFixedCamposY = f1;
            dataholder.vrSettings.vrFixedCamposZ = f2;
            dataholder.vrSettings.vrFixedCamrotQuat.set(quaternion);
            dataholder.vrSettings.mixedRealityFov = f6;
        }
    }

    public static boolean hasExternalCameraConfig() {
        return (new File("ExternalCamera.cfg")).exists();
    }

    public enum Triggerer {
        BINDING,
        MENUBUTTON,
        INTERACTION
    }
}
