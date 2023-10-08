package org.vivecraft.client_vr.gameplay.screenhandlers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.vivecraft.client_vr.gui.GuiKeyboard;
import org.vivecraft.client_vr.gui.PhysicalKeyboard;
import org.vivecraft.client_vr.provider.ControllerType;

import static org.joml.Math.*;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.convertToVec3;
import static org.vivecraft.common.utils.Utils.convertToVector3f;

public class KeyboardHandler {
    private static boolean Showing = false;
    public static GuiKeyboard UI = new GuiKeyboard();
    public static PhysicalKeyboard physicalKeyboard = new PhysicalKeyboard();
    public static Vector3f Pos_room = new Vector3f();
    public static Matrix4f Rotation_room = new Matrix4f();
    private static boolean PointedL;
    private static boolean PointedR;
    public static boolean keyboardForGui;
    public static RenderTarget Framebuffer = null;
    private static boolean lastPressedClickL;
    private static boolean lastPressedClickR;
    private static boolean lastPressedShift;

    public static boolean setOverlayShowing(boolean showingState) {
        if (dh.kiosk) {
            return false;
        } else {
            if (showingState && !dh.vrSettings.seated) {

                if (dh.vrSettings.physicalKeyboard) {
                    physicalKeyboard.show();
                } else {
                    UI.init(mc, GuiHandler.scaledWidth, GuiHandler.scaledHeight);
                }

                Showing = true;
                orientOverlay(mc.screen != null);
                RadialHandler.setOverlayShowing(false, null);

                if (dh.vrSettings.physicalKeyboard && mc.screen != null) {
                    GuiHandler.onScreenChanged(mc.screen, mc.screen, false);
                }
            } else {
                Showing = false;
            }

            return Showing;
        }
    }

    public static void processGui() {
        PointedL = false;
        PointedR = false;

        if (isShowing() && !dh.vrSettings.seated && Rotation_room != null) {
            if (dh.vrSettings.physicalKeyboard) {
                physicalKeyboard.process();
            } else {
                Vec2 conOne = GuiHandler.getTexCoordsForCursor(Pos_room, Rotation_room, GuiHandler.guiScale, dh.vrPlayer.vrdata_room_pre.getController(1));
                Vec2 conZero = GuiHandler.getTexCoordsForCursor(Pos_room, Rotation_room, GuiHandler.guiScale, dh.vrPlayer.vrdata_room_pre.getController(0));
                float u = conZero.x;
                float v = conZero.y;

                if (!(u < 0.0F) && !(v < 0.0F) && !(u > 1.0F) && !(v > 1.0F)) {
                    if (UI.cursorX2 == -1.0F) {
                        UI.cursorX2 = (int) (u * GuiHandler.guiWidth);
                        UI.cursorY2 = (int) (v * GuiHandler.guiHeight);
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
                        UI.cursorX1 = (int) (u * GuiHandler.guiWidth);
                        UI.cursorY1 = (int) (v * GuiHandler.guiHeight);
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
    }

    public static void orientOverlay(boolean guiRelative) {
        if (isShowing()) {
            keyboardForGui = guiRelative;

            if (dh.vrSettings.physicalKeyboard) {
                Vector3f hmdDir = dh.vrPlayer.vrdata_room_pre.hmd.getPosition(new Vector3f());
                float hmdYawRad = toRadians(-dh.vrPlayer.vrdata_room_pre.hmd.getYaw());
                Pos_room.set(0.0F, -0.5F, 0.3F).rotateY(hmdYawRad).add(hmdDir.x(), hmdDir.y(), hmdDir.z());
                Rotation_room.rotationY((float) PI + hmdYawRad).rotateX((float) PI * 0.8F);
            } else if (guiRelative && GuiHandler.guiRotation_room != null) {
                Vector3f guiUp = GuiHandler.guiRotation_room.getColumn(1, new Vector3f()).mul(-0.8F).add(GuiHandler.guiPos_room);
                Vector3f guiFwd = GuiHandler.guiRotation_room.getColumn(2, new Vector3f()).mul(0.25F * GuiHandler.guiScale);
                Matrix4f guiRot = new Matrix4f()
                    .translate(guiUp)
                    .translate(guiFwd)
                    .mul(GuiHandler.guiRotation_room)
                    .rotateX((float) PI / -6.0F);
                Pos_room.set(guiRot.getTranslation(new Vector3f()));
                Rotation_room.set(guiRot.setTranslation(new Vector3f()));
            } else {
                Vector3f hmdDir = dh.vrPlayer.vrdata_room_pre.hmd.getPosition(new Vector3f());
                Pos_room.set(dh.vrPlayer.vrdata_room_pre.hmd.getCustomVector(new Vector3f(0.0F, -0.5F, -2.0F)).mul(0.5F));
                // float pitch = asin(Pos_room.y() / Pos_room.length());
                Rotation_room.rotationY((float) PI + atan2(Pos_room.x(), Pos_room.z()));
                Pos_room.sub(hmdDir);
            }
        } else {
            keyboardForGui = false;
        }
    }

    public static void processBindings() {
        if (isShowing()) {
            if (dh.vrSettings.physicalKeyboard) {
                physicalKeyboard.processBindings();
            } else {

                final int i0 = clamp(0, GuiHandler.guiWidth, (int) UI.cursorX1) * UI.width / GuiHandler.guiWidth;
                final int i1 = clamp(0, GuiHandler.guiHeight, (int) UI.cursorY1) * UI.height / GuiHandler.guiHeight;

                if (PointedL && GuiHandler.keyKeyboardClick.consumeClick(ControllerType.LEFT)) {
                    UI.mouseClicked(i0, i1, 0);
                    lastPressedClickL = true;
                }

                if (!GuiHandler.keyKeyboardClick.isDown(ControllerType.LEFT) && lastPressedClickL) {
                    UI.mouseReleased(i0, i1, 0);
                    lastPressedClickL = false;
                }

                final int i2 = clamp(0, GuiHandler.guiWidth, (int) UI.cursorX2) * UI.width / GuiHandler.guiWidth;
                final int i3 = clamp(0, GuiHandler.guiHeight, (int) UI.cursorY2) * UI.height / GuiHandler.guiHeight;

                if (PointedR && GuiHandler.keyKeyboardClick.consumeClick(ControllerType.RIGHT)) {
                    UI.mouseClicked(i2, i3, 0);
                    lastPressedClickR = true;
                }

                if (!GuiHandler.keyKeyboardClick.isDown(ControllerType.RIGHT) && lastPressedClickR) {
                    UI.mouseReleased(i2, i3, 0);
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
        }
    }

    public static boolean isShowing() {
        return Showing;
    }

    public static boolean isUsingController(ControllerType type) {
        return type == ControllerType.LEFT ? PointedL : PointedR;
    }
}
