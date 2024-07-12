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
    static final boolean debug = false;
    private static int startController;
    private static VRData.VRDevicePose startControllerPose;
    private static float startCamposX;
    private static float startCamposY;
    private static float startCamposZ;
    private static Quaternion startCamrotQuat;
    private static Triggerer camTriggerer;

    /**
     * process debug keys
     * @param key GLFW key that got pressed
     * @param scanCode GLFW scancode of the key
     * @param action GLFW key action (pressed/released)
     * @param modifiers GLFW key modifier
     * @return if a key was processed
     */
    public static boolean handleKeyboardInputs(int key, int scanCode, int action, int modifiers) {
        // Support cool-off period for key presses - otherwise keys can get spammed...
        if (nextRead != 0L && System.currentTimeMillis() < nextRead) {
            return false;
        } else {
            Minecraft minecraft = Minecraft.getInstance();
            ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();

            // Capture Minecrift key events
            boolean gotKey = false;

            if (action == GLFW.GLFW_PRESS) {
                // control key combinations
                if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL)) {
                    // Debug aim
                    if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
                        dataholder.vrSettings.storeDebugAim = true;
                        minecraft.gui.getChat().addMessage(Component.translatable("vivecraft.messages.showaim"));
                        gotKey = true;
                    }

                    // Walk up blocks
                    if (key == GLFW.GLFW_KEY_B) {
                        dataholder.vrSettings.walkUpBlocks = !dataholder.vrSettings.walkUpBlocks;
                        minecraft.gui.getChat().addMessage(Component.translatable("vivecraft.messages.walkupblocks",
                            dataholder.vrSettings.walkUpBlocks ? LangHelper.getYes() : LangHelper.getNo()));
                        gotKey = true;
                    }

                    // Player inertia
                    if (key == GLFW.GLFW_KEY_I) {
                        dataholder.vrSettings.inertiaFactor = dataholder.vrSettings.inertiaFactor.getNext();
                        minecraft.gui.getChat().addMessage(Component.translatable("vivecraft.messages.playerinertia",
                            Component.translatable(dataholder.vrSettings.inertiaFactor.getLangKey())));

                        gotKey = true;
                    }

                    // for testing restricted client mode
                    if (key == GLFW.GLFW_KEY_R) {
                        if (dataholder.vrPlayer.isTeleportOverridden()) {
                            dataholder.vrPlayer.setTeleportOverride(false);
                            minecraft.gui.getChat()
                                .addMessage(Component.translatable("vivecraft.messages.teleportdisabled"));
                        } else {
                            dataholder.vrPlayer.setTeleportOverride(true);
                            minecraft.gui.getChat()
                                .addMessage(Component.translatable("vivecraft.messages.teleportenabled"));
                        }

                        gotKey = true;
                    }

                    // snap third person cam
                    if (key == GLFW.GLFW_KEY_HOME) {
                        snapMRCam(0);
                        gotKey = true;
                    }

                    // toggle VR with a keyboard shortcut
                    if (key == GLFW.GLFW_KEY_F7) {
                        VRState.vrEnabled = !VRState.vrEnabled;
                        ClientDataHolderVR.getInstance().vrSettings.vrEnabled = VRState.vrEnabled;
                        gotKey = true;
                    }
                }

                if (key == GLFW.GLFW_KEY_F12 && debug) {
                    minecraft.setScreen(new WinScreen(false, Runnables.doNothing()));
                    gotKey = true;
                }

                // toggle mirror mode
                if (key == GLFW.GLFW_KEY_F5 && (minecraft.level == null || minecraft.screen != null)) {
                    dataholder.vrSettings.setOptionValue(VRSettings.VrOptions.MIRROR_DISPLAY);
                    ((MinecraftExtension) minecraft).vivecraft$notifyMirror(
                        dataholder.vrSettings.getButtonDisplayString(VRSettings.VrOptions.MIRROR_DISPLAY), false, 3000);
                }
            }

            if (gotKey) {
                dataholder.vrSettings.saveOptions();
            }

            return gotKey;
        }
    }

    /**
     * move third person camera with keys
     */
    public static void handleMRKeys() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
        boolean gotKey = false;
        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL)) {
            if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
                // with shift do rotation
                if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_UP)) {
                    adjustCamRot(Axis.PITCH, 0.5F);
                    gotKey = true;
                }

                if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_DOWN)) {
                    adjustCamRot(Axis.PITCH, -0.5F);
                    gotKey = true;
                }

                if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_LEFT)) {
                    adjustCamRot(Axis.YAW, 0.5F);
                    gotKey = true;
                }

                if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT)) {
                    adjustCamRot(Axis.YAW, -0.5F);
                    gotKey = true;
                }

                if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_PAGE_UP)) {
                    adjustCamRot(Axis.ROLL, 0.5F);
                    gotKey = true;
                }

                if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_PAGE_DOWN)) {
                    adjustCamRot(Axis.ROLL, -0.5F);
                    gotKey = true;
                }
            } else {
                // without shift do position
                if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_LEFT)) {
                    adjustCamPos(new Vector3(-0.01F, 0.0F, 0.0F));
                    gotKey = true;
                }

                if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT)) {
                    adjustCamPos(new Vector3(0.01F, 0.0F, 0.0F));
                    gotKey = true;
                }

                if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_UP)) {
                    adjustCamPos(new Vector3(0.0F, 0.0F, -0.01F));
                    gotKey = true;
                }

                if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_DOWN)) {
                    adjustCamPos(new Vector3(0.0F, 0.0F, 0.01F));
                    gotKey = true;
                }

                if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_PAGE_UP)) {
                    adjustCamPos(new Vector3(0.0F, 0.01F, 0.0F));
                    gotKey = true;
                }

                if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_PAGE_DOWN)) {
                    adjustCamPos(new Vector3(0.0F, -0.01F, 0.0F));
                    gotKey = true;
                }
            }

            // change fov
            if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
                // third person fov
                if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_INSERT)) {
                    dataHolder.vrSettings.mixedRealityFov++;
                    gotKey = true;
                }

                if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_DELETE)) {
                    dataHolder.vrSettings.mixedRealityFov--;
                    gotKey = true;
                }
            } else {
                // first person fov
                if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_INSERT)) {
                    minecraft.options.fov().set(minecraft.options.fov().get() + 1);
                    gotKey = true;
                }

                if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_DELETE)) {
                    minecraft.options.fov().set(minecraft.options.fov().get() - 1);
                    gotKey = true;
                }
            }

        }

        if (gotKey) {
            dataHolder.vrSettings.saveOptions();

            if (dataHolder.vr.mrMovingCamActive) {
                minecraft.gui.getChat().addMessage(
                    Component.translatable("vivecraft.messages.coords",
                        dataHolder.vrSettings.mrMovingCamOffsetX,
                        dataHolder.vrSettings.mrMovingCamOffsetY,
                        dataHolder.vrSettings.mrMovingCamOffsetZ));
                Angle angle = dataHolder.vrSettings.mrMovingCamOffsetRotQuat.toEuler();
                minecraft.gui.getChat().addMessage(
                    Component.translatable("vivecraft.messages.angles",
                        angle.getPitch(), angle.getYaw(), angle.getRoll()));
            } else {
                minecraft.gui.getChat().addMessage(
                    Component.translatable("vivecraft.messages.coords",
                        dataHolder.vrSettings.vrFixedCamposX,
                        dataHolder.vrSettings.vrFixedCamposY,
                        dataHolder.vrSettings.vrFixedCamposZ));
                Angle angle1 = dataHolder.vrSettings.vrFixedCamrotQuat.toEuler();
                minecraft.gui.getChat().addMessage(
                    Component.translatable("vivecraft.messages.angles",
                        angle1.getPitch(), angle1.getYaw(), angle1.getRoll()));
            }
        }
    }

    /**
     * moves the camera position
     * @param offset offset to move the camera to, local to the camera
     */
    private static void adjustCamPos(Vector3 offset) {
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();

        if (dataHolder.vr.mrMovingCamActive) {
            offset = dataHolder.vrSettings.mrMovingCamOffsetRotQuat.multiply(offset);
            dataHolder.vrSettings.mrMovingCamOffsetX += offset.getX();
            dataHolder.vrSettings.mrMovingCamOffsetY += offset.getY();
            dataHolder.vrSettings.mrMovingCamOffsetZ += offset.getZ();
        } else {
            offset = dataHolder.vrSettings.vrFixedCamrotQuat.inverse().multiply(offset);
            dataHolder.vrSettings.vrFixedCamposX += offset.getX();
            dataHolder.vrSettings.vrFixedCamposY += offset.getY();
            dataHolder.vrSettings.vrFixedCamposZ += offset.getZ();
        }
    }

    /**
     * rotate the camera
     * @param axis camera local axis
     * @param degrees degree amount to rotate around {@code axis}
     */
    private static void adjustCamRot(Axis axis, float degrees) {
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();

        if (dataHolder.vr.mrMovingCamActive) {
            dataHolder.vrSettings.mrMovingCamOffsetRotQuat.set(dataHolder.vrSettings.mrMovingCamOffsetRotQuat.rotate(axis, degrees, true));
        } else {
            dataHolder.vrSettings.vrFixedCamrotQuat.set(dataHolder.vrSettings.vrFixedCamrotQuat.rotate(axis, degrees, false));
        }
    }

    /**
     * snaps the camera to the given controller
     * @param controller index of the controller to snap to
     */
    public static void snapMRCam(int controller) {
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
        Vec3 pos = dataHolder.vrPlayer.vrdata_room_pre.getController(controller).getPosition();
        dataHolder.vrSettings.vrFixedCamposX = (float) pos.x;
        dataHolder.vrSettings.vrFixedCamposY = (float) pos.y;
        dataHolder.vrSettings.vrFixedCamposZ = (float) pos.z;
        Quaternion quat = new Quaternion(Utils.convertOVRMatrix(dataHolder.vrPlayer.vrdata_room_pre.getController(controller).getMatrix()));
        dataHolder.vrSettings.vrFixedCamrotQuat.set(quat);
    }

    /**
     *
     */
    public static void updateMovingThirdPersonCam() {
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();

        if (startControllerPose != null) {
            VRData.VRDevicePose controllerPose = dataHolder.vrPlayer.vrdata_room_pre.getController(startController);
            Vec3 startPos = startControllerPose.getPosition();
            Vec3 deltaPos = controllerPose.getPosition().subtract(startPos);

            Matrix4f deltaMatrix = Matrix4f.multiply(controllerPose.getMatrix(), startControllerPose.getMatrix().inverted());
            Vector3 offset = new Vector3(
                startCamposX - (float) startPos.x,
                startCamposY - (float) startPos.y,
                startCamposZ - (float) startPos.z);
            Vector3 offsetRotated = deltaMatrix.transform(offset);

            dataHolder.vrSettings.vrFixedCamposX = startCamposX + (float) deltaPos.x + (offsetRotated.getX() - offset.getX());
            dataHolder.vrSettings.vrFixedCamposY = startCamposY + (float) deltaPos.y + (offsetRotated.getY() - offset.getY());
            dataHolder.vrSettings.vrFixedCamposZ = startCamposZ + (float) deltaPos.z + (offsetRotated.getZ() - offset.getZ());
            dataHolder.vrSettings.vrFixedCamrotQuat.set(startCamrotQuat.multiply(new Quaternion(Utils.convertOVRMatrix(deltaMatrix))));
        }
    }

    /**
     * starts moving the third person camera, stores the initial position
     * @param controller which controller moves the camera
     * @param triggerer what type of input caused the moving
     */
    public static void startMovingThirdPersonCam(int controller, Triggerer triggerer) {
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();

        startController = controller;
        startControllerPose = dataHolder.vrPlayer.vrdata_room_pre.getController(controller);
        startCamposX = dataHolder.vrSettings.vrFixedCamposX;
        startCamposY = dataHolder.vrSettings.vrFixedCamposY;
        startCamposZ = dataHolder.vrSettings.vrFixedCamposZ;
        startCamrotQuat = dataHolder.vrSettings.vrFixedCamrotQuat.copy();
        camTriggerer = triggerer;
    }

    /**
     * stops moving the third person camera
     */
    public static void stopMovingThirdPersonCam() {
        startControllerPose = null;
    }

    /**
     * @return if the third person camera is currently being moved
     */
    public static boolean isMovingThirdPersonCam() {
        return startControllerPose != null;
    }

    /**
     * @return which controller is moving the third person camera
     */
    public static int getMovingThirdPersonCamController() {
        return startController;
    }

    /**
     * @return what caused the third person camera movement
     */
    public static Triggerer getMovingThirdPersonCamTriggerer() {
        return camTriggerer;
    }

    /**
     * read camera config file and set the position/rotation
     */
    public static void loadExternalCameraConfig() {
        File file = new File("ExternalCamera.cfg");

        if (file.exists()) {
            float x = 0.0F, y = 0.0F, z = 0.0F;
            float rx = 0.0F, ry = 0.0F, rz = 0.0F;
            float fov = 40.0F;

            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] tokens = line.split("=", 2);

                    switch (tokens[0]) {
                        case "x" -> x = Float.parseFloat(tokens[1]);
                        case "y" -> y = Float.parseFloat(tokens[1]);
                        case "z" -> z = Float.parseFloat(tokens[1]);
                        case "rx" -> rx = Float.parseFloat(tokens[1]);
                        case "ry" -> ry = Float.parseFloat(tokens[1]);
                        case "rz" -> rz = Float.parseFloat(tokens[1]);
                        case "fov" -> fov = Float.parseFloat(tokens[1]);
                    }
                }
            } catch (Exception exception) {
                VRSettings.logger.error("error reading camera config:", exception);
                return;
            }

            // Eh just set everything, the fixed pos is overridden by the moving cam anyways
            ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
            Quaternion quaternion = new Quaternion(rx, ry, rz, dataHolder.vrSettings.externalCameraAngleOrder);
            dataHolder.vrSettings.mrMovingCamOffsetX = x;
            dataHolder.vrSettings.mrMovingCamOffsetY = y;
            dataHolder.vrSettings.mrMovingCamOffsetZ = z;
            dataHolder.vrSettings.mrMovingCamOffsetRotQuat.set(quaternion);
            dataHolder.vrSettings.vrFixedCamposX = x;
            dataHolder.vrSettings.vrFixedCamposY = y;
            dataHolder.vrSettings.vrFixedCamposZ = z;
            dataHolder.vrSettings.vrFixedCamrotQuat.set(quaternion);
            dataHolder.vrSettings.mixedRealityFov = fov;
        }
    }

    /**
     * @return if the user has a camera config file
     */
    public static boolean hasExternalCameraConfig() {
        return (new File("ExternalCamera.cfg")).exists();
    }

    public enum Triggerer {
        BINDING,
        MENUBUTTON,
        INTERACTION
    }
}
