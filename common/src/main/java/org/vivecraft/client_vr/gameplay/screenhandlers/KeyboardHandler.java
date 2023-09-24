package org.vivecraft.client_vr.gameplay.screenhandlers;

import org.vivecraft.client_vr.gui.GuiKeyboard;
import org.vivecraft.client_vr.gui.PhysicalKeyboard;
import org.vivecraft.client_vr.provider.ControllerType;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.pipeline.RenderTarget;

import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.convertToVec3;

import static org.joml.Math.*;

public class KeyboardHandler
{
    private static boolean Showing = false;
    public static GuiKeyboard UI = new GuiKeyboard();
    public static PhysicalKeyboard physicalKeyboard = new PhysicalKeyboard();
    public static Vec3 Pos_room = new Vec3(0.0D, 0.0D, 0.0D);
    public static Matrix4f Rotation_room = new Matrix4f();
    private static boolean lpl;
    private static boolean lps;
    private static boolean PointedL;
    private static boolean PointedR;
    public static boolean keyboardForGui;
    public static RenderTarget Framebuffer = null;
    private static boolean lastPressedClickL;
    private static boolean lastPressedClickR;
    private static boolean lastPressedShift;

    public static boolean setOverlayShowing(boolean showingState)
    {
        if (dh.kiosk)
        {
            return false;
        }
        else
        {
            if (dh.vrSettings.seated)
            {
                showingState = false;
            }

            int i = 1;

            if (showingState)
            {
                int j = mc.getWindow().getGuiScaledWidth();
                int k = mc.getWindow().getGuiScaledHeight();

                if (dh.vrSettings.physicalKeyboard)
                {
                    physicalKeyboard.show();
                }
                else
                {
                    UI.init(mc, j, k);
                }

                Showing = true;
                orientOverlay(mc.screen != null);
                RadialHandler.setOverlayShowing(false, (ControllerType)null);

                if (dh.vrSettings.physicalKeyboard && mc.screen != null)
                {
                    GuiHandler.onScreenChanged(mc.screen, mc.screen, false);
                }
            }
            else
            {
                Showing = false;
            }

            return Showing;
        }
    }

    public static void processGui()
    {
        PointedL = false;
        PointedR = false;

        if (Showing)
        {
            if (!dh.vrSettings.seated)
            {
                if (Rotation_room != null)
                {
                    if (dh.vrSettings.physicalKeyboard)
                    {
                        physicalKeyboard.process();
                    }
                    else
                    {
                        Vec2 vec2 = GuiHandler.getTexCoordsForCursor(Pos_room, Rotation_room, GuiHandler.guiScale, dh.vrPlayer.vrdata_room_pre.getController(1));
                        Vec2 vec21 = GuiHandler.getTexCoordsForCursor(Pos_room, Rotation_room, GuiHandler.guiScale, dh.vrPlayer.vrdata_room_pre.getController(0));
                        float f = vec21.x;
                        float f1 = vec21.y;

                        if (!(f < 0.0F) && !(f1 < 0.0F) && !(f > 1.0F) && !(f1 > 1.0F))
                        {
                            if (UI.cursorX2 == -1.0F)
                            {
                                UI.cursorX2 = (float)((int)(f * (float)mc.getWindow().getScreenWidth()));
                                UI.cursorY2 = (float)((int)(f1 * (float)mc.getWindow().getScreenHeight()));
                            }
                            else
                            {
                                float f2 = (float)((int)(f * (float)mc.getWindow().getScreenWidth()));
                                float f3 = (float)((int)(f1 * (float)mc.getWindow().getScreenHeight()));
                                UI.cursorX2 = UI.cursorX2 * 0.7F + f2 * 0.3F;
                                UI.cursorY2 = UI.cursorY2 * 0.7F + f3 * 0.3F;
                            }
                            PointedR = true;
                        }
                        else
                        {
                            UI.cursorX2 = -1.0F;
                            UI.cursorY2 = -1.0F;
                            PointedR = false;
                        }

                        f = vec2.x;
                        f1 = vec2.y;

                        if (!(f < 0.0F) && !(f1 < 0.0F) && !(f > 1.0F) && !(f1 > 1.0F))
                        {
                            if (UI.cursorX1 == -1.0F)
                            {
                                UI.cursorX1 = (float)((int)(f * (float)mc.getWindow().getScreenWidth()));
                                UI.cursorY1 = (float)((int)(f1 * (float)mc.getWindow().getScreenHeight()));
                            }
                            else
                            {
                                float f4 = (float)((int)(f * (float)mc.getWindow().getScreenWidth()));
                                float f5 = (float)((int)(f1 * (float)mc.getWindow().getScreenHeight()));
                                UI.cursorX1 = UI.cursorX1 * 0.7F + f4 * 0.3F;
                                UI.cursorY1 = UI.cursorY1 * 0.7F + f5 * 0.3F;
                            }
                            PointedL = true;
                        }
                        else
                        {
                            UI.cursorX1 = -1.0F;
                            UI.cursorY1 = -1.0F;
                            PointedL = false;
                        }
                    }
                }
            }
        }
    }

    public static void orientOverlay(boolean guiRelative)
    {
        if (Showing)
        {
            keyboardForGui = guiRelative;

            if (dh.vrSettings.physicalKeyboard)
            {
                Vec3 vec3 = dh.vrPlayer.vrdata_room_pre.hmd.getPosition();
                Vec3 vec31 = new Vec3(0.0D, -0.5D, 0.3D);
                vec31 = vec31.yRot(toRadians(-dh.vrPlayer.vrdata_room_pre.hmd.getYaw()));
                Pos_room = new Vec3(vec3.x + vec31.x, vec3.y + vec31.y, vec3.z + vec31.z);
                float f = (float)PI + toRadians(-dh.vrPlayer.vrdata_room_pre.hmd.getYaw());
                Rotation_room.rotationY(f).rotateX((float)PI * 0.8F);
            }
            else if (guiRelative && GuiHandler.guiRotation_room != null)
            {
                Vector3f vec35 = GuiHandler.guiRotation_room.getColumn(1, new Vector3f()).mul(0.8F);
                Vector3f vec37 = GuiHandler.guiRotation_room.getColumn(2, new Vector3f()).mul(0.25F * GuiHandler.guiScale);
                Matrix4f matrix4f = new Matrix4f()
                    .translate((float)(GuiHandler.guiPos_room.x - vec35.x), (float)(GuiHandler.guiPos_room.y - vec35.y), (float)(GuiHandler.guiPos_room.z - vec35.z))
                    .translate(vec37)
                    .mul(GuiHandler.guiRotation_room)
                    .rotateX(toRadians(-30.0F));
                Pos_room = convertToVec3(matrix4f.getTranslation(new Vector3f()));
                Rotation_room.set(matrix4f.setTranslation(new Vector3f(0.0F, 0.0F, 0.0F)));
            }
            else
            {
                Vec3 vec33 = dh.vrPlayer.vrdata_room_pre.hmd.getPosition();
                Vec3 vec34 = new Vec3(0.0D, -0.5D, -2.0D);
                Vec3 vec36 = dh.vrPlayer.vrdata_room_pre.hmd.getCustomVector(vec34);
                Pos_room = new Vec3(vec36.x / 2.0D + vec33.x, vec36.y / 2.0D + vec33.y, vec36.z / 2.0D + vec33.z);
                Vec3 vec32 = dh.vrPlayer.vrdata_room_pre.hmd.getPosition();
                Vector3f vector3 = new Vector3f(
                    (float)(Pos_room.x - vec32.x),
                    (float)(Pos_room.y - vec32.y),
                    (float)(Pos_room.z - vec32.z)
                );
                float f1 = asin(vector3.y / vector3.length());
                float f2 = (float)PI + atan2(vector3.x, vector3.z);
                Rotation_room.rotationY(f2);
            }
        }
        else
        {
            keyboardForGui = false;
        }
    }

    public static void processBindings()
    {
        if (Showing)
        {
            if (dh.vrSettings.physicalKeyboard)
            {
                physicalKeyboard.processBindings();
                return;
            }

            double d0 = (double)min(max((int)UI.cursorX1, 0), mc.getWindow().getScreenWidth()) * (double)mc.getWindow().getGuiScaledWidth() / (double)mc.getWindow().getScreenWidth();
            double d1 = (double)min(max((int)UI.cursorY1, 0), mc.getWindow().getScreenWidth()) * (double)mc.getWindow().getGuiScaledHeight() / (double)mc.getWindow().getScreenHeight();

            if (PointedL && GuiHandler.keyKeyboardClick.consumeClick(ControllerType.LEFT))
            {
                UI.mouseClicked(d0, d1, 0);
                lastPressedClickL = true;
            }

            if (!GuiHandler.keyKeyboardClick.isDown(ControllerType.LEFT) && lastPressedClickL)
            {
                UI.mouseReleased(d0, d1, 0);
                lastPressedClickL = false;
            }

            d0 = (double)min(max((int)UI.cursorX2, 0), mc.getWindow().getScreenWidth()) * (double)mc.getWindow().getGuiScaledWidth() / (double)mc.getWindow().getScreenWidth();
            d1 = (double)min(max((int)UI.cursorY2, 0), mc.getWindow().getScreenWidth()) * (double)mc.getWindow().getGuiScaledHeight() / (double)mc.getWindow().getScreenHeight();

            if (PointedR && GuiHandler.keyKeyboardClick.consumeClick(ControllerType.RIGHT))
            {
                UI.mouseClicked(d0, d1, 0);
                lastPressedClickR = true;
            }

            if (!GuiHandler.keyKeyboardClick.isDown(ControllerType.RIGHT) && lastPressedClickR)
            {
                UI.mouseReleased(d0, d1, 0);
                lastPressedClickR = false;
            }

            if (GuiHandler.keyKeyboardShift.consumeClick())
            {
                UI.setShift(true);
                lastPressedShift = true;
            }

            if (!GuiHandler.keyKeyboardShift.isDown() && lastPressedShift)
            {
                UI.setShift(false);
                lastPressedShift = false;
            }
        }
    }

    public static boolean isShowing()
    {
        return Showing;
    }
    public static boolean isUsingController(ControllerType type)
    {
        return type == ControllerType.LEFT ? PointedL : PointedR;
    }
}
