package org.vivecraft.client_vr.gameplay.screenhandlers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.VRData.VRDevicePose;
import org.vivecraft.client_vr.gui.GuiRadial;
import org.vivecraft.client_vr.provider.ControllerType;

import static org.joml.Math.*;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

public class RadialHandler {
    private static boolean Showing = false;
    public static GuiRadial UI = new GuiRadial();
    public static final Vector3f Pos_room = new Vector3f();
    public static final Matrix4f Rotation_room = new Matrix4f();
    private static boolean PointedL;
    private static boolean PointedR;
    public static RenderTarget Framebuffer;
    private static ControllerType activecontroller;
    private static boolean lastPressedClickL;
    private static boolean lastPressedClickR;
    private static boolean lastPressedShiftL;
    private static boolean lastPressedShiftR;

    public static boolean setOverlayShowing(boolean showingState, ControllerType controller) {
        if (dh.kiosk) {
            return false;
        } else {
            if (showingState && !dh.vrSettings.seated) {
                UI.init(mc, GuiHandler.scaledWidth, GuiHandler.scaledHeight);
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

        if (isShowing() && !dh.vrSettings.seated) {
            Vec2 conOne = GuiHandler.getTexCoordsForCursor(Pos_room, Rotation_room, GuiHandler.guiScale, dh.vrPlayer.vrdata_room_pre.getController(1));
            Vec2 conZero = GuiHandler.getTexCoordsForCursor(Pos_room, Rotation_room, GuiHandler.guiScale, dh.vrPlayer.vrdata_room_pre.getController(0));
            float u = conZero.x;
            float v = conZero.y;

            if (!(u < 0.0F) && !(v < 0.0F) && !(u > 1.0F) && !(v > 1.0F)) {
                if (UI.cursorX2 == -1.0F) {
                    UI.cursorX2 = (float) ((int) (u * GuiHandler.guiWidth));
                    UI.cursorY2 = (float) ((int) (v * GuiHandler.guiHeight));
                } else {
                    UI.cursorX2 = UI.cursorX2 * 0.7F + (float) ((int) (u * GuiHandler.guiWidth)) * 0.3F;
                    UI.cursorY2 = UI.cursorY2 * 0.7F + (float) ((int) (v * GuiHandler.guiHeight)) * 0.3F;
                }
                PointedR = true;
            } else {
                UI.cursorX2 = -1.0F;
                UI.cursorY2 = -1.0F;
                PointedR = false;
            }

            u = conOne.x;
            v = conOne.y;

            if (!(u < 0.0F) && !(v < 0.0F) && !(u > 1.0F) && !(v > 1.0F)) {
                if (UI.cursorX1 == -1.0F) {
                    UI.cursorX1 = (float) ((int) (u * GuiHandler.guiWidth));
                    UI.cursorY1 = (float) ((int) (v * GuiHandler.guiHeight));
                } else {
                    UI.cursorX1 = UI.cursorX1 * 0.7F + (float) ((int) (u * GuiHandler.guiWidth)) * 0.3F;
                    UI.cursorY1 = UI.cursorY1 * 0.7F + (float) ((int) (v * GuiHandler.guiHeight)) * 0.3F;
                }
                PointedL = true;
            } else {
                UI.cursorX1 = -1.0F;
                UI.cursorY1 = -1.0F;
                PointedL = false;
            }
        }
    }

    public static void orientOverlay(ControllerType controller) {
        if (isShowing()) {
            int con = (controller == ControllerType.LEFT) ? 1 : 0;
            VRDevicePose pose;
            float f;

            if (dh.vrSettings.radialModeHold) {
                pose = dh.vrPlayer.vrdata_room_pre.getController(con);
                f = 1.2F;
            } else {
                pose = dh.vrPlayer.vrdata_room_pre.hmd;
                f = 2.0F;
            }

            pose.getPosition(Pos_room);
            Vector3f look = pose.getCustomVector(new Vector3f(0.0F, 0.0F, -f)).mul(0.5F);
            Pos_room.add(look);
            float pitch = asin(look.y / look.length());
            float yaw = (float) PI + atan2(look.x, look.z);
            Rotation_room.rotationY(yaw).rotateX(pitch);
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

            int i0 = (int) (clamp(0, GuiHandler.guiWidth, (int) UI.cursorX1) * (double) UI.width / (double) GuiHandler.guiWidth);
            int i1 = (int) (clamp(0, GuiHandler.guiHeight, (int) UI.cursorY1) * (double) UI.height / (double) GuiHandler.guiHeight);
            int i2 = (int) (clamp(0, GuiHandler.guiWidth, (int) UI.cursorX2) * (double) UI.width / (double) GuiHandler.guiWidth);
            int i3 = (int) (clamp(0, GuiHandler.guiHeight, (int) UI.cursorY2) * (double) UI.height / (double) GuiHandler.guiHeight);

            if (dh.vrSettings.radialModeHold) {
                if (activecontroller == null) {
                    return;
                }

                if (!VivecraftVRMod.keyRadialMenu.isDown()) {
                    if (activecontroller == ControllerType.LEFT) {
                        UI.mouseClicked(i0, i1, 0);
                    } else {
                        UI.mouseClicked(i2, i3, 0);
                    }

                    setOverlayShowing(false, null);
                }
            } else {
                if (PointedL && GuiHandler.keyKeyboardClick.consumeClick(ControllerType.LEFT)) {
                    UI.mouseClicked(i0, i1, 0);
                    lastPressedClickL = true;
                }

                if (!GuiHandler.keyKeyboardClick.isDown(ControllerType.LEFT) && lastPressedClickL) {
                    UI.mouseReleased(i0, i1, 0);
                    lastPressedClickL = false;
                }

                if (PointedR && GuiHandler.keyKeyboardClick.consumeClick(ControllerType.RIGHT)) {
                    UI.mouseClicked(i2, i3, 0);
                    lastPressedClickR = true;
                }

                if (!GuiHandler.keyKeyboardClick.isDown(ControllerType.RIGHT) && lastPressedClickR) {
                    UI.mouseReleased(i2, i3, 0);
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
