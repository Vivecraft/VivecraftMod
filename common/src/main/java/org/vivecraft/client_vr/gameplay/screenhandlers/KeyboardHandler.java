package org.vivecraft.client_vr.gameplay.screenhandlers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gui.GuiKeyboard;
import org.vivecraft.client_vr.gui.PhysicalKeyboard;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.common.utils.lwjgl.Matrix4f;
import org.vivecraft.common.utils.lwjgl.Vector3f;
import org.vivecraft.common.utils.math.Vector3;

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
        if (ClientDataHolderVR.kiosk) {
            return false;
        } else {
            if (dh.vrSettings.seated) {
                showingState = false;
            }

            int i = 1;

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
    }

    public static void processGui() {
        PointedL = false;
        PointedR = false;

        if (Showing) {
            if (!dh.vrSettings.seated) {
                if (Rotation_room != null) {
                    if (dh.vrSettings.physicalKeyboard) {
                        physicalKeyboard.process();
                    } else {
                        Vec2 vec2 = GuiHandler.getTexCoordsForCursor(Pos_room, Rotation_room, mc.screen, GuiHandler.guiScale, dh.vrPlayer.vrdata_room_pre.getController(1));
                        Vec2 vec21 = GuiHandler.getTexCoordsForCursor(Pos_room, Rotation_room, mc.screen, GuiHandler.guiScale, dh.vrPlayer.vrdata_room_pre.getController(0));
                        float f = vec21.x;
                        float f1 = vec21.y;

                        if (!(f < 0.0F) && !(f1 < 0.0F) && !(f > 1.0F) && !(f1 > 1.0F)) {
                            if (UI.cursorX2 == -1.0F) {
                                UI.cursorX2 = (float) ((int) (f * (float) GuiHandler.guiWidth));
                                UI.cursorY2 = (float) ((int) (f1 * (float) GuiHandler.guiHeight));
                                PointedR = true;
                            } else {
                                float f2 = (float) ((int) (f * (float) GuiHandler.guiWidth));
                                float f3 = (float) ((int) (f1 * (float) GuiHandler.guiHeight));
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
                                UI.cursorX1 = (float) ((int) (f * (float) GuiHandler.guiWidth));
                                UI.cursorY1 = (float) ((int) (f1 * (float) GuiHandler.guiHeight));
                                PointedL = true;
                            } else {
                                float f4 = (float) ((int) (f * (float) GuiHandler.guiWidth));
                                float f5 = (float) ((int) (f1 * (float) GuiHandler.guiHeight));
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
    }

    public static void orientOverlay(boolean guiRelative) {
        keyboardForGui = false;

        if (Showing) {
            keyboardForGui = guiRelative;
            Matrix4f matrix4f = new Matrix4f();

            if (dh.vrSettings.physicalKeyboard) {
                Vec3 vec3 = dh.vrPlayer.vrdata_room_pre.hmd.getPosition();
                Vec3 vec31 = new Vec3(0.0D, -0.5D, 0.3D);
                vec31 = vec31.yRot((float) Math.toRadians(-dh.vrPlayer.vrdata_room_pre.hmd.getYaw()));
                Pos_room = new Vec3(vec3.x + vec31.x, vec3.y + vec31.y, vec3.z + vec31.z);
                float f = (float) Math.PI + (float) Math.toRadians(-dh.vrPlayer.vrdata_room_pre.hmd.getYaw());
                Rotation_room = org.vivecraft.common.utils.math.Matrix4f.rotationY(f);
                Rotation_room = org.vivecraft.common.utils.math.Matrix4f.multiply(Rotation_room, Utils.rotationXMatrix(2.5132742F));
            } else if (guiRelative && GuiHandler.guiRotation_room != null) {
                Matrix4f matrix4f1 = Utils.convertOVRMatrix(GuiHandler.guiRotation_room);
                Vec3 vec35 = new Vec3(matrix4f1.m10, matrix4f1.m11, matrix4f1.m12);
                Vec3 vec37 = (new Vec3(matrix4f1.m20, matrix4f1.m21, matrix4f1.m22)).scale(0.25D * GuiHandler.guiScale);
                vec35 = vec35.scale(0.8F);
                matrix4f.translate(new Vector3f((float) (GuiHandler.guiPos_room.x - vec35.x), (float) (GuiHandler.guiPos_room.y - vec35.y), (float) (GuiHandler.guiPos_room.z - vec35.z)));
                matrix4f.translate(new Vector3f((float) vec37.x, (float) vec37.y, (float) vec37.z));
                Matrix4f.mul(matrix4f, matrix4f1, matrix4f);
                matrix4f.rotate((float) Math.toRadians(30.0D), new Vector3f(-1.0F, 0.0F, 0.0F));
                Rotation_room = Utils.convertToOVRMatrix(matrix4f);
                Pos_room = new Vec3(Rotation_room.M[0][3], Rotation_room.M[1][3], Rotation_room.M[2][3]);
                Rotation_room.M[0][3] = 0.0F;
                Rotation_room.M[1][3] = 0.0F;
                Rotation_room.M[2][3] = 0.0F;
            } else {
                Vec3 vec33 = dh.vrPlayer.vrdata_room_pre.hmd.getPosition();
                Vec3 vec34 = new Vec3(0.0D, -0.5D, -2.0D);
                Vec3 vec36 = dh.vrPlayer.vrdata_room_pre.hmd.getCustomVector(vec34);
                Pos_room = new Vec3(vec36.x / 2.0D + vec33.x, vec36.y / 2.0D + vec33.y, vec36.z / 2.0D + vec33.z);
                Vec3 vec32 = dh.vrPlayer.vrdata_room_pre.hmd.getPosition();
                Vector3 vector3 = new Vector3();
                vector3.setX((float) (Pos_room.x - vec32.x));
                vector3.setY((float) (Pos_room.y - vec32.y));
                vector3.setZ((float) (Pos_room.z - vec32.z));
                float f1 = (float) Math.asin(vector3.getY() / vector3.length());
                float f2 = (float) ((double) (float) Math.PI + Math.atan2(vector3.getX(), vector3.getZ()));
                Rotation_room = org.vivecraft.common.utils.math.Matrix4f.rotationY(f2);
            }
        }
    }

    public static void processBindings() {
        if (Showing) {
            if (dh.vrSettings.physicalKeyboard) {
                physicalKeyboard.processBindings();
                return;
            }

            double d0 = (double) Math.min(Math.max((int) UI.cursorX1, 0), GuiHandler.guiWidth) * (double) UI.width / (double) GuiHandler.guiWidth;
            double d1 = (double) Math.min(Math.max((int) UI.cursorY1, 0), GuiHandler.guiHeight) * (double) UI.height / (double) GuiHandler.guiHeight;

            if (PointedL && GuiHandler.keyKeyboardClick.consumeClick(ControllerType.LEFT)) {
                UI.mouseClicked((int) d0, (int) d1, 0);
                lastPressedClickL = true;
            }

            if (!GuiHandler.keyKeyboardClick.isDown(ControllerType.LEFT) && lastPressedClickL) {
                UI.mouseReleased((int) d0, (int) d1, 0);
                lastPressedClickL = false;
            }

            d0 = (double) Math.min(Math.max((int) UI.cursorX2, 0), GuiHandler.guiWidth) * (double) UI.width / (double) GuiHandler.guiWidth;
            d1 = (double) Math.min(Math.max((int) UI.cursorY2, 0), GuiHandler.guiHeight) * (double) UI.height / (double) GuiHandler.guiHeight;

            if (PointedR && GuiHandler.keyKeyboardClick.consumeClick(ControllerType.RIGHT)) {
                UI.mouseClicked((int) d0, (int) d1, 0);
                lastPressedClickR = true;
            }

            if (!GuiHandler.keyKeyboardClick.isDown(ControllerType.RIGHT) && lastPressedClickR) {
                UI.mouseReleased((int) d0, (int) d1, 0);
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

    public static boolean isUsingController(ControllerType type) {
        return type == ControllerType.LEFT ? PointedL : PointedR;
    }
}
