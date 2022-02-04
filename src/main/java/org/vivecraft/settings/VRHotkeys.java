package org.vivecraft.settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.vivecraft.api.VRData;
import org.vivecraft.utils.LangHelper;
import org.vivecraft.utils.Utils;
import org.vivecraft.utils.math.Angle;
import org.vivecraft.utils.math.Axis;
import org.vivecraft.utils.math.Matrix4f;
import org.vivecraft.utils.math.Quaternion;
import org.vivecraft.utils.math.Vector3;

import com.example.examplemod.DataHolder;
import com.example.examplemod.MinecraftExtension;
import com.google.common.util.concurrent.Runnables;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.phys.Vec3;

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
            DataHolder dataHolder = DataHolder.getInstance();
            boolean flag = false;

            if (action == 1 && key == 344 && InputConstants.isKeyDown(345, 0)) //TODO check
            {
            	dataHolder.vrSettings.storeDebugAim = true;
                minecraft.gui.getChat().addMessage(new TranslatableComponent("vivecraft.messages.showaim"));
                flag = true;
            }

            if (action == 1 && key == 66 && InputConstants.isKeyDown(345, 0))
            {
            	dataHolder.vrSettings.walkUpBlocks = !dataHolder.vrSettings.walkUpBlocks;
                minecraft.gui.getChat().addMessage(new TranslatableComponent("vivecraft.messages.walkupblocks", dataHolder.vrSettings.walkUpBlocks ? LangHelper.getYes() : LangHelper.getNo()));
                flag = true;
            }

            if (action == 1 && key == 73 && InputConstants.isKeyDown(345, 0))
            {
            	dataHolder.vrSettings.inertiaFactor = dataHolder.vrSettings.inertiaFactor.getNext();
                minecraft.gui.getChat().addMessage(new TranslatableComponent("vivecraft.messages.playerinertia", dataHolder.vrSettings.inertiaFactor.getLangKey())); //TODO Optifine

                flag = true;
            }

            if (action == 1 && key == 82 && InputConstants.isKeyDown(345, 0))
            {
                if (dataHolder.vrPlayer.isTeleportOverridden())
                {
                	dataHolder.vrPlayer.setTeleportOverride(false);
                    minecraft.gui.getChat().addMessage(new TranslatableComponent("vivecraft.messages.teleportdisabled"));
                }
                else
                {
                	dataHolder.vrPlayer.setTeleportOverride(true);
                    minecraft.gui.getChat().addMessage(new TranslatableComponent("vivecraft.messages.teleportenabled"));
                }

                flag = true;
            }

            if (action == 1 && key == 268 && InputConstants.isKeyDown(345, 0))
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
            	dataHolder.vrSettings.setOptionValue(VRSettings.VrOptions.MIRROR_DISPLAY);
                ((MinecraftExtension) minecraft).notifyMirror(dataHolder.vrSettings.getButtonDisplayString(VRSettings.VrOptions.MIRROR_DISPLAY), false, 3000);
            }

            if (flag)
            {
            	dataHolder.vrSettings.saveOptions();
            }

            return flag;
        }
    }

    public static void handleMRKeys()
    {
        Minecraft minecraft = Minecraft.getInstance();
        DataHolder dataHolder = DataHolder.getInstance();
        boolean flag = false;

        if (InputConstants.isKeyDown(263, 0) && InputConstants.isKeyDown(345, 0) && !InputConstants.isKeyDown(344, 0))
        {
            adjustCamPos(new Vector3(-0.01F, 0.0F, 0.0F));
            flag = true;
        }

        if (InputConstants.isKeyDown(262, 0) && InputConstants.isKeyDown(345, 0) && !InputConstants.isKeyDown(344, 0))
        {
            adjustCamPos(new Vector3(0.01F, 0.0F, 0.0F));
            flag = true;
        }

        if (InputConstants.isKeyDown(265, 0) && InputConstants.isKeyDown(345, 0) && !InputConstants.isKeyDown(344, 0))
        {
            adjustCamPos(new Vector3(0.0F, 0.0F, -0.01F));
            flag = true;
        }

        if (InputConstants.isKeyDown(264, 0) && InputConstants.isKeyDown(345, 0) && !InputConstants.isKeyDown(344, 0))
        {
            adjustCamPos(new Vector3(0.0F, 0.0F, 0.01F));
            flag = true;
        }

        if (InputConstants.isKeyDown(266, 0) && InputConstants.isKeyDown(345, 0) && !InputConstants.isKeyDown(344, 0))
        {
            adjustCamPos(new Vector3(0.0F, 0.01F, 0.0F));
            flag = true;
        }

        if (InputConstants.isKeyDown(267, 0) && InputConstants.isKeyDown(345, 0) && !InputConstants.isKeyDown(344, 0))
        {
            adjustCamPos(new Vector3(0.0F, -0.01F, 0.0F));
            flag = true;
        }

        if (InputConstants.isKeyDown(265, 0) && InputConstants.isKeyDown(345, 0) && InputConstants.isKeyDown(344, 0))
        {
            adjustCamRot(Axis.PITCH, 0.5F);
            flag = true;
        }

        if (InputConstants.isKeyDown(264, 0) && InputConstants.isKeyDown(345, 0) && InputConstants.isKeyDown(344, 0))
        {
            adjustCamRot(Axis.PITCH, -0.5F);
            flag = true;
        }

        if (InputConstants.isKeyDown(263, 0) && InputConstants.isKeyDown(345, 0) && InputConstants.isKeyDown(344, 0))
        {
            adjustCamRot(Axis.YAW, 0.5F);
            flag = true;
        }

        if (InputConstants.isKeyDown(262, 0) && InputConstants.isKeyDown(345, 0) && InputConstants.isKeyDown(344, 0))
        {
            adjustCamRot(Axis.YAW, -0.5F);
            flag = true;
        }

        if (InputConstants.isKeyDown(266, 0) && InputConstants.isKeyDown(345, 0) && InputConstants.isKeyDown(344, 0))
        {
            adjustCamRot(Axis.ROLL, 0.5F);
            flag = true;
        }

        if (InputConstants.isKeyDown(267, 0) && InputConstants.isKeyDown(345, 0) && InputConstants.isKeyDown(344, 0))
        {
            adjustCamRot(Axis.ROLL, -0.5F);
            flag = true;
        }

        if (InputConstants.isKeyDown(260, 0) && InputConstants.isKeyDown(345, 0) && !InputConstants.isKeyDown(344, 0))
        {
            ++minecraft.options.fov;
            flag = true;
        }

        if (InputConstants.isKeyDown(261, 0) && InputConstants.isKeyDown(345, 0) && !InputConstants.isKeyDown(344, 0))
        {
            --minecraft.options.fov;
            flag = true;
        }

        if (InputConstants.isKeyDown(260, 0) && InputConstants.isKeyDown(345, 0) && InputConstants.isKeyDown(344, 0))
        {
            ++dataHolder.vrSettings.mixedRealityFov;
            flag = true;
        }

        if (InputConstants.isKeyDown(261, 0) && InputConstants.isKeyDown(345, 0) && InputConstants.isKeyDown(344, 0))
        {
            --dataHolder.vrSettings.mixedRealityFov;
            flag = true;
        }

        if (flag)
        {
        	dataHolder.vrSettings.saveOptions();

            if (dataHolder.vr.mrMovingCamActive)
            {
                Minecraft.getInstance().gui.getChat().addMessage(new TextComponent(LangHelper.get("vivecraft.messages.coords", dataHolder.vrSettings.mrMovingCamOffsetX, dataHolder.vrSettings.mrMovingCamOffsetY, dataHolder.vrSettings.mrMovingCamOffsetZ)));
                Angle angle = dataHolder.vrSettings.mrMovingCamOffsetRotQuat.toEuler();
                Minecraft.getInstance().gui.getChat().addMessage(new TextComponent(LangHelper.get("vivecraft.messages.angles", angle.getPitch(), angle.getYaw(), angle.getRoll())));
            }
            else
            {
                Minecraft.getInstance().gui.getChat().addMessage(new TextComponent(LangHelper.get("vivecraft.messages.coords", dataHolder.vrSettings.vrFixedCamposX, dataHolder.vrSettings.vrFixedCamposY, dataHolder.vrSettings.vrFixedCamposZ)));
                Angle angle1 = dataHolder.vrSettings.vrFixedCamrotQuat.toEuler();
                Minecraft.getInstance().gui.getChat().addMessage(new TextComponent(LangHelper.get("vivecraft.messages.angles", angle1.getPitch(), angle1.getYaw(), angle1.getRoll())));
            }
        }
    }

    private static void adjustCamPos(Vector3 offset)
    {
        Minecraft minecraft = Minecraft.getInstance();
        DataHolder dataHolder = DataHolder.getInstance();

        if (dataHolder.vr.mrMovingCamActive)
        {
            offset = dataHolder.vrSettings.mrMovingCamOffsetRotQuat.multiply(offset);
            dataHolder.vrSettings.mrMovingCamOffsetX += offset.getX();
            dataHolder.vrSettings.mrMovingCamOffsetY += offset.getY();
            dataHolder.vrSettings.mrMovingCamOffsetZ += offset.getZ();
        }
        else
        {
            offset = dataHolder.vrSettings.vrFixedCamrotQuat.inverse().multiply(offset);
            dataHolder.vrSettings.vrFixedCamposX += offset.getX();
            dataHolder.vrSettings.vrFixedCamposY += offset.getY();
            dataHolder.vrSettings.vrFixedCamposZ += offset.getZ();
        }
    }

    private static void adjustCamRot(Axis axis, float degrees)
    {
        Minecraft minecraft = Minecraft.getInstance();
        DataHolder dataHolder = DataHolder.getInstance();

        if (dataHolder.vr.mrMovingCamActive)
        {
        	dataHolder.vrSettings.mrMovingCamOffsetRotQuat.set(dataHolder.vrSettings.mrMovingCamOffsetRotQuat.rotate(axis, degrees, true));
        }
        else
        {
        	dataHolder.vrSettings.vrFixedCamrotQuat.set(dataHolder.vrSettings.vrFixedCamrotQuat.rotate(axis, degrees, false));
        }
    }

    public static void snapMRCam(int controller)
    {
        Minecraft minecraft = Minecraft.getInstance();
        DataHolder dataHolder = DataHolder.getInstance();
        Vec3 vec3 = dataHolder.vrPlayer.vrdata_room_pre.getController(controller).getPosition();
        dataHolder.vrSettings.vrFixedCamposX = (float)vec3.x;
        dataHolder.vrSettings.vrFixedCamposY = (float)vec3.y;
        dataHolder.vrSettings.vrFixedCamposZ = (float)vec3.z;
        Quaternion quaternion = new Quaternion(Utils.convertOVRMatrix(dataHolder.vrPlayer.vrdata_room_pre.getController(controller).getMatrix()));
        dataHolder.vrSettings.vrFixedCamrotQuat.set(quaternion);
    }

    public static void updateMovingThirdPersonCam()
    {
        Minecraft minecraft = Minecraft.getInstance();
        DataHolder dataHolder = DataHolder.getInstance();

        if (startControllerPose != null)
        {
            VRData.VRDevicePose vrdata$vrdevicepose = dataHolder.vrPlayer.vrdata_room_pre.getController(startController);
            Vec3 vec3 = startControllerPose.getPosition();
            Vec3 vec31 = vrdata$vrdevicepose.getPosition().subtract(vec3);
            Matrix4f matrix4f = Matrix4f.multiply(vrdata$vrdevicepose.getMatrix(), startControllerPose.getMatrix().inverted());
            Vector3 vector3 = new Vector3(startCamposX - (float)vec3.x, startCamposY - (float)vec3.y, startCamposZ - (float)vec3.z);
            Vector3 vector31 = matrix4f.transform(vector3);
            dataHolder.vrSettings.vrFixedCamposX = startCamposX + (float)vec31.x + (vector31.getX() - vector3.getX());
            dataHolder.vrSettings.vrFixedCamposY = startCamposY + (float)vec31.y + (vector31.getY() - vector3.getY());
            dataHolder.vrSettings.vrFixedCamposZ = startCamposZ + (float)vec31.z + (vector31.getZ() - vector3.getZ());
            dataHolder.vrSettings.vrFixedCamrotQuat.set(startCamrotQuat.multiply(new Quaternion(Utils.convertOVRMatrix(matrix4f))));
        }
    }

    public static void startMovingThirdPersonCam(int controller, VRHotkeys.Triggerer triggerer)
    {
        Minecraft minecraft = Minecraft.getInstance();
        DataHolder dataHolder = DataHolder.getInstance();
        startController = controller;
        startControllerPose = dataHolder.vrPlayer.vrdata_room_pre.getController(controller);
        startCamposX = dataHolder.vrSettings.vrFixedCamposX;
        startCamposY = dataHolder.vrSettings.vrFixedCamposY;
        startCamposZ = dataHolder.vrSettings.vrFixedCamposZ;
        startCamrotQuat = dataHolder.vrSettings.vrFixedCamrotQuat.copy();
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
            DataHolder dataHolder = DataHolder.getInstance();
            Quaternion quaternion = new Quaternion(f3, f4, f5, dataHolder.vrSettings.externalCameraAngleOrder);
            dataHolder.vrSettings.mrMovingCamOffsetX = f;
            dataHolder.vrSettings.mrMovingCamOffsetY = f1;
            dataHolder.vrSettings.mrMovingCamOffsetZ = f2;
            dataHolder.vrSettings.mrMovingCamOffsetRotQuat.set(quaternion);
            dataHolder.vrSettings.vrFixedCamposX = f;
            dataHolder.vrSettings.vrFixedCamposY = f1;
            dataHolder.vrSettings.vrFixedCamposZ = f2;
            dataHolder.vrSettings.vrFixedCamrotQuat.set(quaternion);
            dataHolder.vrSettings.mixedRealityFov = f6;
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
