package org.vivecraft.settings;

import com.google.common.util.concurrent.Runnables;
import com.mojang.blaze3d.platform.InputConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.phys.Vec3;
import net.optifine.Lang;
import org.vivecraft.api.VRData;
import org.vivecraft.utils.LangHelper;
import org.vivecraft.utils.Utils;
import org.vivecraft.utils.math.Angle;
import org.vivecraft.utils.math.Axis;
import org.vivecraft.utils.math.Matrix4f;
import org.vivecraft.utils.math.Quaternion;
import org.vivecraft.utils.math.Vector3;

public class VRHotkeys
{
    static long nextRead = 0L;
    static final long COOLOFF_PERIOD_MILLIS = 500L;
    static boolean debug = false;
    private static int startController;
    private static VRData.VRDevicePose startControllerPose;
    private static float startCamposX;
    private static float startCamposY;
    private static float startCamposZ;
    private static Quaternion startCamrotQuat;
    private static VRHotkeys.Triggerer camTriggerer;

    public static boolean handleKeyboardInputs(int key, int scanCode, int action, int modifiers)
    {
        if (nextRead != 0L && System.currentTimeMillis() < nextRead)
        {
            return false;
        }
        else
        {
            Minecraft minecraft = Minecraft.getInstance();
            boolean flag = false;

            if (action == 1 && key == 344 && InputConstants.isKeyDown(345))
            {
                minecraft.vrSettings.storeDebugAim = true;
                minecraft.gui.getChat().addMessage(new TranslatableComponent("vivecraft.messages.showaim"));
                flag = true;
            }

            if (action == 1 && key == 66 && InputConstants.isKeyDown(345))
            {
                minecraft.vrSettings.walkUpBlocks = !minecraft.vrSettings.walkUpBlocks;
                minecraft.gui.getChat().addMessage(new TranslatableComponent("vivecraft.messages.walkupblocks", minecraft.vrSettings.walkUpBlocks ? LangHelper.getYes() : LangHelper.getNo()));
                flag = true;
            }

            if (action == 1 && key == 73 && InputConstants.isKeyDown(345))
            {
                minecraft.vrSettings.inertiaFactor = minecraft.vrSettings.inertiaFactor.getNext();
                minecraft.gui.getChat().addMessage(new TranslatableComponent("vivecraft.messages.playerinertia", Lang.get(minecraft.vrSettings.inertiaFactor.getLangKey())));

                flag = true;
            }

            if (action == 1 && key == 82 && InputConstants.isKeyDown(345))
            {
                if (minecraft.vrPlayer.isTeleportOverridden())
                {
                    minecraft.vrPlayer.setTeleportOverride(false);
                    minecraft.gui.getChat().addMessage(new TranslatableComponent("vivecraft.messages.teleportdisabled"));
                }
                else
                {
                    minecraft.vrPlayer.setTeleportOverride(true);
                    minecraft.gui.getChat().addMessage(new TranslatableComponent("vivecraft.messages.teleportenabled"));
                }

                flag = true;
            }

            if (action == 1 && key == 268 && InputConstants.isKeyDown(345))
            {
                snapMRCam(0);
                flag = true;
            }

            if (action == 1 && key == 301 && debug)
            {
                minecraft.setScreen(new WinScreen(false, Runnables.doNothing()));
                flag = true;
            }

            if ((minecraft.level == null || minecraft.screen != null) && action == 1 && key == 294)
            {
                minecraft.vrSettings.setOptionValue(VRSettings.VrOptions.MIRROR_DISPLAY);
                minecraft.notifyMirror(minecraft.vrSettings.getButtonDisplayString(VRSettings.VrOptions.MIRROR_DISPLAY), false, 3000);
            }

            if (flag)
            {
                minecraft.vrSettings.saveOptions();
            }

            return flag;
        }
    }

    public static void handleMRKeys()
    {
        Minecraft minecraft = Minecraft.getInstance();
        boolean flag = false;

        if (InputConstants.isKeyDown(263) && InputConstants.isKeyDown(345) && !InputConstants.isKeyDown(344))
        {
            adjustCamPos(new Vector3(-0.01F, 0.0F, 0.0F));
            flag = true;
        }

        if (InputConstants.isKeyDown(262) && InputConstants.isKeyDown(345) && !InputConstants.isKeyDown(344))
        {
            adjustCamPos(new Vector3(0.01F, 0.0F, 0.0F));
            flag = true;
        }

        if (InputConstants.isKeyDown(265) && InputConstants.isKeyDown(345) && !InputConstants.isKeyDown(344))
        {
            adjustCamPos(new Vector3(0.0F, 0.0F, -0.01F));
            flag = true;
        }

        if (InputConstants.isKeyDown(264) && InputConstants.isKeyDown(345) && !InputConstants.isKeyDown(344))
        {
            adjustCamPos(new Vector3(0.0F, 0.0F, 0.01F));
            flag = true;
        }

        if (InputConstants.isKeyDown(266) && InputConstants.isKeyDown(345) && !InputConstants.isKeyDown(344))
        {
            adjustCamPos(new Vector3(0.0F, 0.01F, 0.0F));
            flag = true;
        }

        if (InputConstants.isKeyDown(267) && InputConstants.isKeyDown(345) && !InputConstants.isKeyDown(344))
        {
            adjustCamPos(new Vector3(0.0F, -0.01F, 0.0F));
            flag = true;
        }

        if (InputConstants.isKeyDown(265) && InputConstants.isKeyDown(345) && InputConstants.isKeyDown(344))
        {
            adjustCamRot(Axis.PITCH, 0.5F);
            flag = true;
        }

        if (InputConstants.isKeyDown(264) && InputConstants.isKeyDown(345) && InputConstants.isKeyDown(344))
        {
            adjustCamRot(Axis.PITCH, -0.5F);
            flag = true;
        }

        if (InputConstants.isKeyDown(263) && InputConstants.isKeyDown(345) && InputConstants.isKeyDown(344))
        {
            adjustCamRot(Axis.YAW, 0.5F);
            flag = true;
        }

        if (InputConstants.isKeyDown(262) && InputConstants.isKeyDown(345) && InputConstants.isKeyDown(344))
        {
            adjustCamRot(Axis.YAW, -0.5F);
            flag = true;
        }

        if (InputConstants.isKeyDown(266) && InputConstants.isKeyDown(345) && InputConstants.isKeyDown(344))
        {
            adjustCamRot(Axis.ROLL, 0.5F);
            flag = true;
        }

        if (InputConstants.isKeyDown(267) && InputConstants.isKeyDown(345) && InputConstants.isKeyDown(344))
        {
            adjustCamRot(Axis.ROLL, -0.5F);
            flag = true;
        }

        if (InputConstants.isKeyDown(260) && InputConstants.isKeyDown(345) && !InputConstants.isKeyDown(344))
        {
            ++minecraft.options.fov;
            flag = true;
        }

        if (InputConstants.isKeyDown(261) && InputConstants.isKeyDown(345) && !InputConstants.isKeyDown(344))
        {
            --minecraft.options.fov;
            flag = true;
        }

        if (InputConstants.isKeyDown(260) && InputConstants.isKeyDown(345) && InputConstants.isKeyDown(344))
        {
            ++minecraft.vrSettings.mixedRealityFov;
            flag = true;
        }

        if (InputConstants.isKeyDown(261) && InputConstants.isKeyDown(345) && InputConstants.isKeyDown(344))
        {
            --minecraft.vrSettings.mixedRealityFov;
            flag = true;
        }

        if (flag)
        {
            minecraft.vrSettings.saveOptions();

            if (minecraft.vr.mrMovingCamActive)
            {
                Minecraft.getInstance().gui.getChat().addMessage(new TextComponent(LangHelper.get("vivecraft.messages.coords", minecraft.vrSettings.mrMovingCamOffsetX, minecraft.vrSettings.mrMovingCamOffsetY, minecraft.vrSettings.mrMovingCamOffsetZ)));
                Angle angle = minecraft.vrSettings.mrMovingCamOffsetRotQuat.toEuler();
                Minecraft.getInstance().gui.getChat().addMessage(new TextComponent(LangHelper.get("vivecraft.messages.angles", angle.getPitch(), angle.getYaw(), angle.getRoll())));
            }
            else
            {
                Minecraft.getInstance().gui.getChat().addMessage(new TextComponent(LangHelper.get("vivecraft.messages.coords", minecraft.vrSettings.vrFixedCamposX, minecraft.vrSettings.vrFixedCamposY, minecraft.vrSettings.vrFixedCamposZ)));
                Angle angle1 = minecraft.vrSettings.vrFixedCamrotQuat.toEuler();
                Minecraft.getInstance().gui.getChat().addMessage(new TextComponent(LangHelper.get("vivecraft.messages.angles", angle1.getPitch(), angle1.getYaw(), angle1.getRoll())));
            }
        }
    }

    private static void adjustCamPos(Vector3 offset)
    {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.vr.mrMovingCamActive)
        {
            offset = minecraft.vrSettings.mrMovingCamOffsetRotQuat.multiply(offset);
            minecraft.vrSettings.mrMovingCamOffsetX += offset.getX();
            minecraft.vrSettings.mrMovingCamOffsetY += offset.getY();
            minecraft.vrSettings.mrMovingCamOffsetZ += offset.getZ();
        }
        else
        {
            offset = minecraft.vrSettings.vrFixedCamrotQuat.inverse().multiply(offset);
            minecraft.vrSettings.vrFixedCamposX += offset.getX();
            minecraft.vrSettings.vrFixedCamposY += offset.getY();
            minecraft.vrSettings.vrFixedCamposZ += offset.getZ();
        }
    }

    private static void adjustCamRot(Axis axis, float degrees)
    {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.vr.mrMovingCamActive)
        {
            minecraft.vrSettings.mrMovingCamOffsetRotQuat.set(minecraft.vrSettings.mrMovingCamOffsetRotQuat.rotate(axis, degrees, true));
        }
        else
        {
            minecraft.vrSettings.vrFixedCamrotQuat.set(minecraft.vrSettings.vrFixedCamrotQuat.rotate(axis, degrees, false));
        }
    }

    public static void snapMRCam(int controller)
    {
        Minecraft minecraft = Minecraft.getInstance();
        Vec3 vec3 = minecraft.vrPlayer.vrdata_room_pre.getController(controller).getPosition();
        minecraft.vrSettings.vrFixedCamposX = (float)vec3.x;
        minecraft.vrSettings.vrFixedCamposY = (float)vec3.y;
        minecraft.vrSettings.vrFixedCamposZ = (float)vec3.z;
        Quaternion quaternion = new Quaternion(Utils.convertOVRMatrix(minecraft.vrPlayer.vrdata_room_pre.getController(controller).getMatrix()));
        minecraft.vrSettings.vrFixedCamrotQuat.set(quaternion);
    }

    public static void updateMovingThirdPersonCam()
    {
        Minecraft minecraft = Minecraft.getInstance();

        if (startControllerPose != null)
        {
            VRData.VRDevicePose vrdata$vrdevicepose = minecraft.vrPlayer.vrdata_room_pre.getController(startController);
            Vec3 vec3 = startControllerPose.getPosition();
            Vec3 vec31 = vrdata$vrdevicepose.getPosition().subtract(vec3);
            Matrix4f matrix4f = Matrix4f.multiply(vrdata$vrdevicepose.getMatrix(), startControllerPose.getMatrix().inverted());
            Vector3 vector3 = new Vector3(startCamposX - (float)vec3.x, startCamposY - (float)vec3.y, startCamposZ - (float)vec3.z);
            Vector3 vector31 = matrix4f.transform(vector3);
            minecraft.vrSettings.vrFixedCamposX = startCamposX + (float)vec31.x + (vector31.getX() - vector3.getX());
            minecraft.vrSettings.vrFixedCamposY = startCamposY + (float)vec31.y + (vector31.getY() - vector3.getY());
            minecraft.vrSettings.vrFixedCamposZ = startCamposZ + (float)vec31.z + (vector31.getZ() - vector3.getZ());
            minecraft.vrSettings.vrFixedCamrotQuat.set(startCamrotQuat.multiply(new Quaternion(Utils.convertOVRMatrix(matrix4f))));
        }
    }

    public static void startMovingThirdPersonCam(int controller, VRHotkeys.Triggerer triggerer)
    {
        Minecraft minecraft = Minecraft.getInstance();
        startController = controller;
        startControllerPose = minecraft.vrPlayer.vrdata_room_pre.getController(controller);
        startCamposX = minecraft.vrSettings.vrFixedCamposX;
        startCamposY = minecraft.vrSettings.vrFixedCamposY;
        startCamposZ = minecraft.vrSettings.vrFixedCamposZ;
        startCamrotQuat = minecraft.vrSettings.vrFixedCamrotQuat.copy();
        camTriggerer = triggerer;
    }

    public static void stopMovingThirdPersonCam()
    {
        startControllerPose = null;
    }

    public static boolean isMovingThirdPersonCam()
    {
        return startControllerPose != null;
    }

    public static int getMovingThirdPersonCamController()
    {
        return startController;
    }

    public static VRHotkeys.Triggerer getMovingThirdPersonCamTriggerer()
    {
        return camTriggerer;
    }

    public static void loadExternalCameraConfig()
    {
        File file1 = new File("ExternalCamera.cfg");

        if (file1.exists())
        {
            float f = 0.0F;
            float f1 = 0.0F;
            float f2 = 0.0F;
            float f3 = 0.0F;
            float f4 = 0.0F;
            float f5 = 0.0F;
            float f6 = 40.0F;
            String s;

            try (BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(new FileInputStream(file1), StandardCharsets.UTF_8)))
            {
                while ((s = bufferedreader.readLine()) != null)
                {
                    String[] astring = s.split("=", 2);
                    String s1 = astring[0];

                    switch (s1)
                    {
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
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
                return;
            }

            Minecraft minecraft = Minecraft.getInstance();
            Quaternion quaternion = new Quaternion(f3, f4, f5, minecraft.vrSettings.externalCameraAngleOrder);
            minecraft.vrSettings.mrMovingCamOffsetX = f;
            minecraft.vrSettings.mrMovingCamOffsetY = f1;
            minecraft.vrSettings.mrMovingCamOffsetZ = f2;
            minecraft.vrSettings.mrMovingCamOffsetRotQuat.set(quaternion);
            minecraft.vrSettings.vrFixedCamposX = f;
            minecraft.vrSettings.vrFixedCamposY = f1;
            minecraft.vrSettings.vrFixedCamposZ = f2;
            minecraft.vrSettings.vrFixedCamrotQuat.set(quaternion);
            minecraft.vrSettings.mixedRealityFov = f6;
        }
    }

    public static boolean hasExternalCameraConfig()
    {
        return (new File("ExternalCamera.cfg")).exists();
    }

    public static enum Triggerer
    {
        BINDING,
        MENUBUTTON,
        INTERACTION;
    }
}
