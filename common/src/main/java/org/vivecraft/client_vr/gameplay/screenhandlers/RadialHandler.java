package org.vivecraft.client_vr.gameplay.screenhandlers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.utils.MathUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.gui.GuiRadial;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.common.utils.math.Matrix4f;

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
    private static ControllerType activeController;
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

            if (showingState) {
                UI.init(Minecraft.getInstance(), GuiHandler.scaledWidthMax, GuiHandler.scaledHeightMax);
                Showing = true;
                activeController = controller;
                orientOverlay(activeController);
            } else {
                Showing = false;
                activeController = null;
            }

            return isShowing();
        }
    }

    public static void processGui() {
        PointedL = false;
        PointedR = false;

        if (!Showing) return;
        if (dh.vrSettings.seated) return;
        if (Rotation_room == null) return;

        // process cursors
        PointedR = UI.processCursor(Pos_room, Rotation_room, false);
        PointedL = UI.processCursor(Pos_room, Rotation_room, true);
    }

    public static void orientOverlay(ControllerType controller) {
        if (!isShowing()) return;

        VRData.VRDevicePose pose = dh.vrPlayer.vrdata_room_pre.hmd; //normal menu.
        float distance = 2.0F;
        int id = 0;

        if (controller == ControllerType.LEFT) {
            id = 1;
        }

        if (dh.vrSettings.radialModeHold) {
            // open with controller centered, consistent motions.
            pose = dh.vrPlayer.vrdata_room_pre.getController(id);
            distance = 1.2F;
        }

        Vec3 position = pose.getPosition();
        Vec3 offset = pose.getDirection().scale(distance * 0.5F);

        Pos_room = position.add(offset);

        float pitch = (float) Math.asin(offset.y / offset.length());
        float yaw = (float) (Math.PI + Math.atan2(offset.x, offset.z));

        Rotation_room = Matrix4f.rotationY(yaw);
        Matrix4f tilt = MathUtils.rotationXMatrix(pitch);
        Rotation_room = Matrix4f.multiply(Rotation_room, tilt);
    }

    public static void processBindings() {
        if (!isShowing()) return;

        // TODO: this is the cause for issue https://github.com/Vivecraft/VivecraftMod/issues/240

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

        // scale virtual cursor coords to actual screen coords
        float uiScaleX = (float) UI.width / (float) GuiHandler.guiWidth;
        float uiScaleY = (float) UI.height / (float) GuiHandler.guiHeight;

        int x1 = (int) (Math.min(Math.max((int) UI.cursorX1, 0), GuiHandler.guiWidth) * uiScaleX);
        int y1 = (int) (Math.min(Math.max((int) UI.cursorY1, 0), GuiHandler.guiHeight) * uiScaleY);
        int x2 = (int) (Math.min(Math.max((int) UI.cursorX2, 0), GuiHandler.guiWidth) * uiScaleX);
        int y2 = (int) (Math.min(Math.max((int) UI.cursorY2, 0), GuiHandler.guiHeight) * uiScaleY);

        if (dh.vrSettings.radialModeHold) {
            if (activeController == null) {
                return;
            }

            if (!VivecraftVRMod.INSTANCE.keyRadialMenu.isDown()) {
                if (activeController == ControllerType.LEFT) {
                    UI.mouseClicked(x1, y1, GLFW.GLFW_MOUSE_BUTTON_LEFT);
                } else {
                    UI.mouseClicked(x2, y2, GLFW.GLFW_MOUSE_BUTTON_LEFT);
                }

                setOverlayShowing(false, null);
            }
        } else {
            if (PointedL && GuiHandler.keyKeyboardClick.consumeClick(ControllerType.LEFT)) {
                UI.mouseClicked(x1, y1, GLFW.GLFW_MOUSE_BUTTON_LEFT);
                lastPressedClickL = true;
            }

            if (!GuiHandler.keyKeyboardClick.isDown(ControllerType.LEFT) && lastPressedClickL) {
                UI.mouseReleased(x1, y1, GLFW.GLFW_MOUSE_BUTTON_LEFT);
                lastPressedClickL = false;
            }

            if (PointedR && GuiHandler.keyKeyboardClick.consumeClick(ControllerType.RIGHT)) {
                UI.mouseClicked(x2, y2, GLFW.GLFW_MOUSE_BUTTON_LEFT);
                lastPressedClickR = true;
            }

            if (!GuiHandler.keyKeyboardClick.isDown(ControllerType.RIGHT) && lastPressedClickR) {
                UI.mouseReleased(x2, y2, GLFW.GLFW_MOUSE_BUTTON_LEFT);
                lastPressedClickR = false;
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
