package org.vivecraft.client_vr.gameplay.screenhandlers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.gui.GuiRadial;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.common.utils.math.Matrix4f;
import org.vivecraft.common.utils.math.Vector3;

public class RadialHandler {
    public static Minecraft mc = Minecraft.getInstance();
    public static ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
    private static boolean Showing = false;
    public static GuiRadial UI = new GuiRadial();
    public static Vec3 Pos_room = new Vec3(0.0D, 0.0D, 0.0D);
    public static Matrix4f Rotation_room = new Matrix4f();
    private static boolean PointedL;
    private static boolean PointedR;
    public static RenderTarget Framebuffer = null;
    private static ControllerType activecontroller;
    private static boolean lastPressedClickL;
    private static boolean lastPressedClickR;
    private static boolean lastPressedShiftL;
    private static boolean lastPressedShiftR;

    public static boolean setOverlayShowing(boolean showingState, ControllerType controller) {
        if (ClientDataHolderVR.kiosk) {
            return false;
        } else {
            if (dh.vrSettings.seated) {
                showingState = false;
            }

            int i = 1;

            if (showingState) {
                UI.init(Minecraft.getInstance(), GuiHandler.scaledWidth, GuiHandler.scaledHeight);
                Showing = true;
                activecontroller = controller;
                orientOverlay(activecontroller);
            } else {
                Showing = false;
                activecontroller = null;
            }

            return isShowing();
        }
    }

    public static void processGui() {
        PointedL = false;
        PointedR = false;

        if (isShowing()) {
            if (!dh.vrSettings.seated) {
                if (Rotation_room != null) {
                    Vec2 vec2 = GuiHandler.getTexCoordsForCursor(Pos_room, Rotation_room, mc.screen, GuiHandler.guiScale, dh.vrPlayer.vrdata_room_pre.getController(1));
                    Vec2 vec21 = GuiHandler.getTexCoordsForCursor(Pos_room, Rotation_room, mc.screen, GuiHandler.guiScale, dh.vrPlayer.vrdata_room_pre.getController(0));
                    float f = vec21.x;
                    float f1 = vec21.y;

                    if (!(f < 0.0F) && !(f1 < 0.0F) && !(f > 1.0F) && !(f1 > 1.0F)) {
                        if (UI.cursorX2 == -1.0F) {
                            UI.cursorX2 = (float) ((int) (f * GuiHandler.guiWidth));
                            UI.cursorY2 = (float) ((int) (f1 * GuiHandler.guiHeight));
                            PointedR = true;
                        } else {
                            float f2 = (float) ((int) (f * GuiHandler.guiWidth));
                            float f3 = (float) ((int) (f1 * GuiHandler.guiHeight));
                            UI.cursorX2 = UI.cursorX2 * 0.7F + f2 * 0.3F;
                            UI.cursorY2 = UI.cursorY2 * 0.7F + f3 * 0.3F;
                            PointedR = true;
                        }
                    } else {
                        UI.cursorX2 = -1.0F;
                        UI.cursorY2 = -1.0F;
                        PointedR = false;
                    }

                    f = vec2.x;
                    f1 = vec2.y;

                    if (!(f < 0.0F) && !(f1 < 0.0F) && !(f > 1.0F) && !(f1 > 1.0F)) {
                        if (UI.cursorX1 == -1.0F) {
                            UI.cursorX1 = (float) ((int) (f * GuiHandler.guiWidth));
                            UI.cursorY1 = (float) ((int) (f1 * GuiHandler.guiHeight));
                            PointedL = true;
                        } else {
                            float f4 = (float) ((int) (f * GuiHandler.guiWidth));
                            float f5 = (float) ((int) (f1 * GuiHandler.guiHeight));
                            UI.cursorX1 = UI.cursorX1 * 0.7F + f4 * 0.3F;
                            UI.cursorY1 = UI.cursorY1 * 0.7F + f5 * 0.3F;
                            PointedL = true;
                        }
                    } else {
                        UI.cursorX1 = -1.0F;
                        UI.cursorY1 = -1.0F;
                        PointedL = false;
                    }
                }
            }
        }
    }

    public static void orientOverlay(ControllerType controller) {
        if (isShowing()) {
            VRData.VRDevicePose vrdata$vrdevicepose = dh.vrPlayer.vrdata_room_pre.hmd;
            float f = 2.0F;
            int i = 0;

            if (controller == ControllerType.LEFT) {
                i = 1;
            }

            if (dh.vrSettings.radialModeHold) {
                vrdata$vrdevicepose = dh.vrPlayer.vrdata_room_pre.getController(i);
                f = 1.2F;
            }

            new Matrix4f();
            Vec3 vec3 = vrdata$vrdevicepose.getPosition();
            Vec3 vec31 = new Vec3(0.0D, 0.0D, -f);
            Vec3 vec32 = vrdata$vrdevicepose.getCustomVector(vec31);
            Pos_room = new Vec3(vec32.x / 2.0D + vec3.x, vec32.y / 2.0D + vec3.y, vec32.z / 2.0D + vec3.z);
            Vector3 vector3 = new Vector3();
            vector3.setX((float) (Pos_room.x - vec3.x));
            vector3.setY((float) (Pos_room.y - vec3.y));
            vector3.setZ((float) (Pos_room.z - vec3.z));
            float f1 = (float) Math.asin(vector3.getY() / vector3.length());
            float f2 = (float) ((double) (float) Math.PI + Math.atan2(vector3.getX(), vector3.getZ()));
            Rotation_room = Matrix4f.rotationY(f2);
            Matrix4f matrix4f = Utils.rotationXMatrix(f1);
            Rotation_room = Matrix4f.multiply(Rotation_room, matrix4f);
        }
    }

    public static void processBindings() {
        if (isShowing()) {
            if (PointedL && GuiHandler.keyKeyboardShift.consumeClick(ControllerType.LEFT)) {
                UI.setShift(true);
                lastPressedShiftL = true;
            }

            if (!GuiHandler.keyKeyboardShift.isDown(ControllerType.LEFT) && lastPressedShiftL) {
                UI.setShift(false);
                lastPressedShiftL = false;
            }

            if (PointedR && GuiHandler.keyKeyboardShift.consumeClick(ControllerType.RIGHT)) {
                UI.setShift(true);
                lastPressedShiftR = true;
            }

            if (!GuiHandler.keyKeyboardShift.isDown(ControllerType.RIGHT) && lastPressedShiftR) {
                UI.setShift(false);
                lastPressedShiftR = false;
            }

            double d0 = (double) Math.min(Math.max((int) UI.cursorX1, 0), GuiHandler.guiWidth) * (double) UI.width / (double) GuiHandler.guiWidth;
            double d1 = (double) Math.min(Math.max((int) UI.cursorY1, 0), GuiHandler.guiHeight) * (double) UI.height / (double) GuiHandler.guiHeight;
            double d2 = (double) Math.min(Math.max((int) UI.cursorX2, 0), GuiHandler.guiWidth) * (double) UI.width / (double) GuiHandler.guiWidth;
            double d3 = (double) Math.min(Math.max((int) UI.cursorY2, 0), GuiHandler.guiHeight) * (double) UI.height / (double) GuiHandler.guiHeight;

            if (dh.vrSettings.radialModeHold) {
                if (activecontroller == null) {
                    return;
                }

                if (!VivecraftVRMod.INSTANCE.keyRadialMenu.isDown()) {
                    if (activecontroller == ControllerType.LEFT) {
                        UI.mouseClicked((int) d0, (int) d1, 0);
                    } else {
                        UI.mouseClicked((int) d2, (int) d3, 0);
                    }

                    setOverlayShowing(false, null);
                }
            } else {
                if (PointedL && GuiHandler.keyKeyboardClick.consumeClick(ControllerType.LEFT)) {
                    UI.mouseClicked((int) d0, (int) d1, 0);
                    lastPressedClickL = true;
                }

                if (!GuiHandler.keyKeyboardClick.isDown(ControllerType.LEFT) && lastPressedClickL) {
                    UI.mouseReleased((int) d0, (int) d1, 0);
                    lastPressedClickL = false;
                }

                if (PointedR && GuiHandler.keyKeyboardClick.consumeClick(ControllerType.RIGHT)) {
                    UI.mouseClicked((int) d2, (int) d3, 0);
                    lastPressedClickR = true;
                }

                if (!GuiHandler.keyKeyboardClick.isDown(ControllerType.RIGHT) && lastPressedClickR) {
                    UI.mouseReleased((int) d2, (int) d3, 0);
                    lastPressedClickR = false;
                }
            }
        }
    }

    public static boolean isShowing() {
        return Showing;
    }

    public static boolean isUsingController(ControllerType controller) {
        return controller == ControllerType.LEFT ? PointedL : PointedR;
    }
}
