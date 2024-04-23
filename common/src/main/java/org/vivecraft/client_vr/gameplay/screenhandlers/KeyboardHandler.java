package org.vivecraft.client_vr.gameplay.screenhandlers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gui.GuiKeyboard;
import org.vivecraft.client_vr.gui.PhysicalKeyboard;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.common.utils.lwjgl.Matrix4f;
import org.vivecraft.common.utils.lwjgl.Vector3f;

public class KeyboardHandler {
    public static Minecraft mc = Minecraft.getInstance();
    public static ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
    public static boolean Showing = false;
    public static GuiKeyboard UI = new GuiKeyboard();
    public static PhysicalKeyboard physicalKeyboard = new PhysicalKeyboard();
    public static Vec3 Pos_room = new Vec3(0.0D, 0.0D, 0.0D);
    public static org.vivecraft.common.utils.math.Matrix4f Rotation_room = new org.vivecraft.common.utils.math.Matrix4f();
    private static boolean PointedL;
    private static boolean PointedR;
    public static boolean keyboardForGui;
    public static RenderTarget Framebuffer = null;
    private static boolean lastPressedClickL;
    private static boolean lastPressedClickR;
    private static boolean lastPressedShift;

    public static boolean setOverlayShowing(boolean showingState) {
        if (ClientDataHolderVR.kiosk) return false;
        if (dh.vrSettings.seated) {
            showingState = false;
        }

        if (showingState) {
            if (dh.vrSettings.physicalKeyboard) {
                physicalKeyboard.show();
            } else {
                UI.init(Minecraft.getInstance(), GuiHandler.scaledWidth, GuiHandler.scaledHeight);
            }

            Showing = true;
            orientOverlay(mc.screen != null);
            RadialHandler.setOverlayShowing(false, null);

            if (dh.vrSettings.physicalKeyboard && mc.screen != null) {
                GuiHandler.onScreenChanged(mc.screen, mc.screen, false);
            }
        } else {
            Showing = false;
            if (dh.vrSettings.physicalKeyboard) {
                physicalKeyboard.unpressAllKeys();
            }
        }

        return Showing;
    }

    public static void processGui() {
        PointedL = false;
        PointedR = false;

        if (!Showing) return;
        if (dh.vrSettings.seated) return;
        if (Rotation_room == null) return;

        if (dh.vrSettings.physicalKeyboard) {
            physicalKeyboard.process();
            // Skip the rest of this
            return;
        }

        // process cursors
        PointedR = UI.processCursor(Pos_room, Rotation_room, false);
        PointedL = UI.processCursor(Pos_room, Rotation_room, true);
    }

    public static void orientOverlay(boolean guiRelative) {
        keyboardForGui = false;

        if (!Showing) return;

        keyboardForGui = guiRelative;

        if (dh.vrSettings.physicalKeyboard) {
            Vec3 pos = dh.vrPlayer.vrdata_room_pre.hmd.getPosition();
            Vec3 offset = new Vec3(0.0D, -0.5D, 0.3D);
            offset = offset.yRot((float) Math.toRadians(-dh.vrPlayer.vrdata_room_pre.hmd.getYaw()));

            Pos_room = pos.add(offset);
            float yaw = (float) Math.PI + (float) Math.toRadians(-dh.vrPlayer.vrdata_room_pre.hmd.getYaw());

            Rotation_room = org.vivecraft.common.utils.math.Matrix4f.rotationY(yaw);
            Rotation_room = org.vivecraft.common.utils.math.Matrix4f.multiply(Rotation_room, Utils.rotationXMatrix((float)Math.PI * 0.8f));
        } else if (guiRelative && GuiHandler.guiRotation_room != null) {
            // put the keyboard below the current screen
            Matrix4f guiRot = Utils.convertOVRMatrix(GuiHandler.guiRotation_room);
            Vec3 guiUp = new Vec3(guiRot.m10, guiRot.m11, guiRot.m12);
            Vec3 guiFwd = (new Vec3(guiRot.m20, guiRot.m21, guiRot.m22)).scale(0.25D * GuiHandler.guiScale);
            guiUp = guiUp.scale(0.8F);

            Matrix4f roomRotation = new Matrix4f();
            roomRotation.translate(new Vector3f((float) (GuiHandler.guiPos_room.x - guiUp.x), (float) (GuiHandler.guiPos_room.y - guiUp.y), (float) (GuiHandler.guiPos_room.z - guiUp.z)));
            roomRotation.translate(new Vector3f((float) guiFwd.x, (float) guiFwd.y, (float) guiFwd.z));
            Matrix4f.mul(roomRotation, guiRot, roomRotation);
            roomRotation.rotate((float) Math.toRadians(30.0D), new Vector3f(-1.0F, 0.0F, 0.0F));

            Rotation_room = Utils.convertToOVRMatrix(roomRotation);
            Pos_room = new Vec3(Rotation_room.M[0][3], Rotation_room.M[1][3], Rotation_room.M[2][3]);
            Rotation_room.M[0][3] = 0.0F;
            Rotation_room.M[1][3] = 0.0F;
            Rotation_room.M[2][3] = 0.0F;
        } else {
            // copy from GuiHandler.onScreenChanged for static screens
            Vec3 offset = new Vec3(0.0D, -0.5D, -2.0D);

            Vec3 hmdPos = dh.vrPlayer.vrdata_room_pre.hmd.getPosition();
            Vec3 look = dh.vrPlayer.vrdata_room_pre.hmd.getCustomVector(offset).scale(0.5F);

            Pos_room = look.add(hmdPos);

            // orient screen
            float yaw = (float) (Math.PI + Math.atan2(look.x, look.z));
            Rotation_room = org.vivecraft.common.utils.math.Matrix4f.rotationY(yaw);
        }
    }

    public static void processBindings() {
        if (!Showing) return;

        if (dh.vrSettings.physicalKeyboard) {
            physicalKeyboard.processBindings();
            return;
        }

        // scale virtual cursor coords to actual screen coords
        float uiScaleX = (float) UI.width / (float) GuiHandler.guiWidth;
        float uiScaleY = (float) UI.height / (float) GuiHandler.guiHeight;

        int x1 = (int) (Math.min(Math.max((int) UI.cursorX1, 0), GuiHandler.guiWidth) * uiScaleX);
        int y1 = (int) (Math.min(Math.max((int) UI.cursorY1, 0), GuiHandler.guiHeight) * uiScaleY);
        int x2 = (int) (Math.min(Math.max((int) UI.cursorX2, 0), GuiHandler.guiWidth) * uiScaleX);
        int y2 = (int) (Math.min(Math.max((int) UI.cursorY2, 0), GuiHandler.guiHeight) * uiScaleY);

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

        if (GuiHandler.keyKeyboardShift.consumeClick()) {
            UI.setShift(true);
            lastPressedShift = true;
        }

        if (!GuiHandler.keyKeyboardShift.isDown() && lastPressedShift) {
            UI.setShift(false);
            lastPressedShift = false;
        }
    }

    public static boolean isUsingController(ControllerType type) {
        return type == ControllerType.LEFT ? PointedL : PointedR;
    }
}
