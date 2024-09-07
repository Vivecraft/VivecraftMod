package org.vivecraft.client_vr.gameplay.screenhandlers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.phys.*;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.WindowExtension;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.HandedKeyBinding;
import org.vivecraft.client_vr.provider.InputSimulator;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.openvr_lwjgl.OpenVRUtil;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.math.Matrix4f;
import org.vivecraft.common.utils.math.Quaternion;
import org.vivecraft.common.utils.math.Vector3;

public class GuiHandler {
    public static Minecraft mc = Minecraft.getInstance();
    public static ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
    static boolean lastPressedLeftClick;
    static boolean lastPressedRightClick;
    static boolean lastPressedMiddleClick;
    static boolean lastPressedShift;
    static boolean lastPressedCtrl;
    static boolean lastPressedAlt;

    // For mouse menu emulation
    private static double controllerMouseX = -1.0D;
    private static double controllerMouseY = -1.0D;
    public static boolean controllerMouseValid;
    public static int controllerMouseTicks;
    public static boolean guiAppearOverBlockActive = false;
    public static float guiScale = 1.0F;
    public static float guiScaleApplied = 1.0F;
    public static Vec3 guiPos_room = null;
    public static Matrix4f guiRotation_room = null;
    public static final KeyMapping keyLeftClick = new KeyMapping("vivecraft.key.guiLeftClick", -1, "vivecraft.key.category.gui");
    public static final KeyMapping keyRightClick = new KeyMapping("vivecraft.key.guiRightClick", -1, "vivecraft.key.category.gui");
    public static final KeyMapping keyMiddleClick = new KeyMapping("vivecraft.key.guiMiddleClick", -1, "vivecraft.key.category.gui");
    public static final KeyMapping keyShift = new KeyMapping("vivecraft.key.guiShift", -1, "vivecraft.key.category.gui");
    public static final KeyMapping keyCtrl = new KeyMapping("vivecraft.key.guiCtrl", -1, "vivecraft.key.category.gui");
    public static final KeyMapping keyAlt = new KeyMapping("vivecraft.key.guiAlt", -1, "vivecraft.key.category.gui");
    public static final KeyMapping keyScrollUp = new KeyMapping("vivecraft.key.guiScrollUp", -1, "vivecraft.key.category.gui");
    public static final KeyMapping keyScrollDown = new KeyMapping("vivecraft.key.guiScrollDown", -1, "vivecraft.key.category.gui");
    public static final KeyMapping keyScrollAxis = new KeyMapping("vivecraft.key.guiScrollAxis", -1, "vivecraft.key.category.gui");
    public static final HandedKeyBinding keyKeyboardClick = new HandedKeyBinding("vivecraft.key.keyboardClick", -1, "vivecraft.key.category.keyboard") {
        @Override
        public boolean isPriorityOnController(ControllerType type) {
            if (KeyboardHandler.Showing && !GuiHandler.dh.vrSettings.physicalKeyboard) {
                return KeyboardHandler.isUsingController(type);
            } else {
                return RadialHandler.isShowing() && RadialHandler.isUsingController(type);
            }
        }
    };
    public static final HandedKeyBinding keyKeyboardShift = new HandedKeyBinding("vivecraft.key.keyboardShift", -1, "vivecraft.key.category.keyboard") {
        @Override
        public boolean isPriorityOnController(ControllerType type) {
            if (KeyboardHandler.Showing) {
                return GuiHandler.dh.vrSettings.physicalKeyboard || KeyboardHandler.isUsingController(type);
            } else {
                return RadialHandler.isShowing() && RadialHandler.isUsingController(type);
            }
        }
    };
    public static RenderTarget guiFramebuffer = null;

    // for GUI scale override
    public static int guiWidth = 1280;
    public static int guiHeight = 720;
    public static int guiScaleFactorMax;
    public static int guiScaleFactor = calculateScale(0, false, guiWidth, guiHeight);
    public static int scaledWidth;
    public static int scaledHeight;
    public static int scaledWidthMax;
    public static int scaledHeightMax;
    private static int prevGuiScale = -1;

    /**
     * copy of the vanilla method to calculate gui resolution and max scale
     */
    public static int calculateScale(int scaleIn, boolean forceUnicode, int framebufferWidth, int framebufferHeight) {
        int scale = 1;
        int maxScale = 1;

        while (maxScale < framebufferWidth &&
            maxScale < framebufferHeight &&
            framebufferWidth / (maxScale + 1) >= 320 &&
            framebufferHeight / (maxScale + 1) >= 240) {
            if (scale < scaleIn || scaleIn == 0) {
                scale++;
            }
            maxScale++;
        }

        if (forceUnicode) {
            if (scale % 2 != 0) {
                scale++;
            }
            if (maxScale % 2 != 0) {
                maxScale++;
            }
        }

        guiScaleFactorMax = maxScale;

        scaledWidth = Mth.ceil(framebufferWidth / (float) scale);
        scaledWidthMax = Mth.ceil(framebufferWidth / (float) maxScale);

        scaledHeight = Mth.ceil(framebufferHeight / (float) scale);
        scaledHeightMax = Mth.ceil(framebufferHeight / (float) maxScale);

        return scale;
    }

    /**
     * updates the gui resolution, and scales the cursor position
     * @return if the gui scale/size changed
     */
    public static boolean updateResolution() {
        int oldWidth = guiWidth;
        int oldHeight = guiHeight;
        int oldGuiScale = guiScaleFactor;
        guiWidth = dh.vrSettings.doubleGUIResolution ? 2560 : 1280;
        guiHeight = dh.vrSettings.doubleGUIResolution ? 1440 : 720;

        int newGuiScale = dh.vrSettings.doubleGUIResolution ?
            dh.vrSettings.guiScale : (int) Math.ceil(dh.vrSettings.guiScale * 0.5f);

        if (oldWidth != guiWidth || prevGuiScale != newGuiScale) {
            // only recalculate when scale or size changed
            guiScaleFactor = calculateScale(newGuiScale, false, guiWidth, guiHeight);
            prevGuiScale = newGuiScale;
        }
        if (oldWidth != guiWidth) {
            // move cursor to right position
            InputSimulator.setMousePos(
                mc.mouseHandler.xpos() * ((WindowExtension) (Object) mc.getWindow()).vivecraft$getActualScreenWidth() / oldWidth,
                mc.mouseHandler.ypos() * ((WindowExtension) (Object) mc.getWindow()).vivecraft$getActualScreenHeight() / oldHeight);
            controllerMouseX *= (double) guiWidth / oldWidth;
            controllerMouseY *= (double) guiHeight / oldHeight;
            return true;
        } else {
            return oldGuiScale != guiScaleFactor;
        }
    }

    /**
     * calculates and sets the cursor position
     */
    public static void processGui() {
        if (guiRotation_room == null) return;
        if (dh.vrSettings.seated) return;
        if (!MCVR.get().isControllerTracking(0)) return;
        // some mods ungrab the mouse when there is no screen
        if (mc.screen == null && mc.mouseHandler.isMouseGrabbed()) return;

        Vec2 tex = getTexCoordsForCursor(guiPos_room, guiRotation_room, guiScale, dh.vrPlayer.vrdata_room_pre.getController(0));
        float u = tex.x;
        float v = tex.y;

        if (u < 0 || v < 0 || u > 1 || v > 1) {
            // offscreen
            controllerMouseX = -1.0f;
            controllerMouseY = -1.0f;
            controllerMouseValid = false;
        } else if (!controllerMouseValid) {
            controllerMouseX = (int) (u * mc.getWindow().getWidth());
            controllerMouseY = (int) (v * mc.getWindow().getHeight());
            controllerMouseValid = true;
        } else {
            // apply some smoothing between mouse positions
            float newX = (int) (u * mc.getWindow().getWidth());
            float newY = (int) (v * mc.getWindow().getHeight());
            controllerMouseX = controllerMouseX * 0.7f + newX * 0.3f;
            controllerMouseY = controllerMouseY * 0.7f + newY * 0.3f;
            controllerMouseValid = true;
        }

        if (controllerMouseValid) {
            // mouse on screen
            InputSimulator.setMousePos(
                controllerMouseX * (((WindowExtension) (Object) mc.getWindow()).vivecraft$getActualScreenWidth() / (double) mc.getWindow().getScreenWidth()),
                controllerMouseY * (((WindowExtension) (Object) mc.getWindow()).vivecraft$getActualScreenHeight() / (double) mc.getWindow().getScreenHeight()));
        }
    }

    /**
     * calculates the relative cursor position on the gui
     * @param guiPos_room position of the gui
     * @param guiRotation_room orientation of the gui
     * @param guiScale size of the gui layer
     * @param controller device pose to get the cursor for
     * @return relative position on the gui, anchored top left.<br>
     *  If offscreen returns Vec2(-1,-1)
     */
    public static Vec2 getTexCoordsForCursor(Vec3 guiPos_room, Matrix4f guiRotation_room, float guiScale, VRData.VRDevicePose controller) {
        Vec3 con = controller.getPosition();
        Vector3 controllerPos = new Vector3(con);
        Vec3 conDir = controller.getDirection();
        Vector3 controllerDir = new Vector3((float) conDir.x, (float) conDir.y, (float) conDir.z);
        Vector3 forward = new Vector3(0.0F, 0.0F, 1.0F);
        Vector3 guiNormal = guiRotation_room.transform(forward);
        Vector3 guiRight = guiRotation_room.transform(new Vector3(1.0F, 0.0F, 0.0F));
        Vector3 guiUp = guiRotation_room.transform(new Vector3(0.0F, 1.0F, 0.0F));
        float guiDotController = guiNormal.dot(controllerDir);

        if (Math.abs(guiDotController) > 1.0E-5F) {
            // pointed normal to the GUI
            float guiWidth = 1.0F;
            float guiHalfWidth = guiWidth * 0.5F;
            float guiHeight = 1.0F;
            float guiHalfHeight = guiHeight * 0.5F;
            Vector3 guiPos = new Vector3();
            guiPos.setX((float) guiPos_room.x);
            guiPos.setY((float) guiPos_room.y);
            guiPos.setZ((float) guiPos_room.z);

            Vector3 guiTopLeft = guiPos.subtract(guiUp.multiply(guiHalfHeight)).subtract(guiRight.multiply(guiHalfWidth));

            float intersectDist = -guiNormal.dot(controllerPos.subtract(guiTopLeft)) / guiDotController;

            if (intersectDist > 0.0F) {
                Vector3 pointOnPlane = controllerPos.add(controllerDir.multiply(intersectDist));

                Vector3 relativePoint = pointOnPlane.subtract(guiTopLeft);
                float u = relativePoint.dot(guiRight.multiply(guiWidth));
                float v = relativePoint.dot(guiUp.multiply(guiWidth));

                float aspect = (float) mc.getWindow().getGuiScaledHeight() / (float) mc.getWindow().getGuiScaledWidth();
                u = (u - 0.5F) / 1.5F / guiScale + 0.5F;
                v = (v - 0.5F) / aspect / 1.5F / guiScale + 0.5F;
                v = 1.0F - v;
                return new Vec2(u, v);
            }
        }

        return new Vec2(-1.0F, -1.0F);
    }

    /**
     * processes key presses for the GUI
     */
    public static void processBindingsGui() {
        // only click mouse keys, when cursor is on screen
        boolean mouseValid = controllerMouseX >= 0.0D && controllerMouseX < mc.getWindow().getScreenWidth() &&
            controllerMouseY >= 0.0D && controllerMouseY < mc.getWindow().getScreenWidth();

        // LMB
        if (keyLeftClick.consumeClick() && mc.screen != null && mouseValid) {
            InputSimulator.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
            lastPressedLeftClick = true;
        }
        if (!keyLeftClick.isDown() && lastPressedLeftClick) {
            InputSimulator.releaseMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
            lastPressedLeftClick = false;
        }

        // RMB
        if (keyRightClick.consumeClick() && mc.screen != null && mouseValid) {
            InputSimulator.pressMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
            lastPressedRightClick = true;
        }
        if (!keyRightClick.isDown() && lastPressedRightClick) {
            InputSimulator.releaseMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
            lastPressedRightClick = false;
        }

        // MMB
        if (keyMiddleClick.consumeClick() && mc.screen != null && mouseValid) {
            InputSimulator.pressMouse(GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
            lastPressedMiddleClick = true;
        }
        if (!keyMiddleClick.isDown() && lastPressedMiddleClick) {
            InputSimulator.releaseMouse(GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
            lastPressedMiddleClick = false;
        }

        // Shift
        if (keyShift.consumeClick() && mc.screen != null) {
            InputSimulator.pressKey(GLFW.GLFW_KEY_LEFT_SHIFT);
            lastPressedShift = true;
        }
        if (!keyShift.isDown() && lastPressedShift) {
            InputSimulator.releaseKey(GLFW.GLFW_KEY_LEFT_SHIFT);
            lastPressedShift = false;
        }

        // Crtl
        if (keyCtrl.consumeClick() && mc.screen != null) {
            InputSimulator.pressKey(GLFW.GLFW_KEY_LEFT_CONTROL);
            lastPressedCtrl = true;
        }
        if (!keyCtrl.isDown() && lastPressedCtrl) {
            InputSimulator.releaseKey(GLFW.GLFW_KEY_LEFT_CONTROL);
            lastPressedCtrl = false;
        }

        // Alt
        if (keyAlt.consumeClick() && mc.screen != null) {
            InputSimulator.pressKey(GLFW.GLFW_KEY_LEFT_ALT);
            lastPressedAlt = true;
        }
        if (!keyAlt.isDown() && lastPressedAlt) {
            InputSimulator.releaseKey(GLFW.GLFW_KEY_LEFT_ALT);
            lastPressedAlt = false;
        }

        // scroll mouse
        if (keyScrollUp.consumeClick() && mc.screen != null) {
            InputSimulator.scrollMouse(0.0D, 4.0D);
        }

        if (keyScrollDown.consumeClick() && mc.screen != null) {
            InputSimulator.scrollMouse(0.0D, -4.0D);
        }
    }

    public static void onScreenChanged(Screen previousGuiScreen, Screen newScreen, boolean unpressKeys) {
        onScreenChanged(previousGuiScreen, newScreen, unpressKeys, false);
    }

    public static void onScreenChanged(Screen previousGuiScreen, Screen newScreen, boolean unpressKeys, boolean infrontOfHand) {
        if (!VRState.vrRunning) {
            return;
        }

        if (unpressKeys) {
            dh.vr.ignorePressesNextFrame = true;
        }

        if (newScreen == null) {
            // just insurance
            guiPos_room = null;
            guiRotation_room = null;
            guiScale = 1.0F;

            if (KeyboardHandler.keyboardForGui && dh.vrSettings.autoCloseKeyboard) {
                KeyboardHandler.setOverlayShowing(false);
            }
        } else {
            RadialHandler.setOverlayShowing(false, null);
        }

        if (mc.level == null || newScreen instanceof WinScreen) {
            dh.vrSettings.worldRotationCached = dh.vrSettings.worldRotation;
            dh.vrSettings.worldRotation = 0.0F;
        } else {
            // these dont update when screen open.
            if (dh.vrSettings.worldRotationCached != 0.0F) {
                dh.vrSettings.worldRotation = dh.vrSettings.worldRotationCached;
                dh.vrSettings.worldRotationCached = 0.0F;
            }
        }

        // check if the new screen is meant to show the MenuRoom, instead of the current screen
        boolean staticScreen = MethodHolder.willBeInMenuRoom(newScreen);
        staticScreen &= !dh.vrSettings.seated && !dh.vrSettings.menuAlwaysFollowFace;

        if (staticScreen) {
            guiScale = 2.0F;
            Vector2f playArea = MCVR.get().getPlayAreaSize();
            // slight offset to center of the room, to prevent z fighting
            guiPos_room = new Vec3(0.02D, 1.3F, -Math.max(playArea != null ? playArea.y / 2.0F : 0.0F, 1.5F));
            guiRotation_room = new Matrix4f();
            return;
        }
        if ((previousGuiScreen == null && newScreen != null) ||
            newScreen instanceof ChatScreen ||
            newScreen instanceof BookEditScreen ||
            newScreen instanceof AbstractSignEditScreen)
        {
            // check if screen is a container screen
            // and if the pointed at block is the same that was last interacted with
            boolean isBlockScreen = newScreen instanceof AbstractContainerScreen &&
                mc.hitResult != null &&
                mc.hitResult.getType() == HitResult.Type.BLOCK;

            // check if screen is a container screen
            // and if the pointed at entity is the same that was last interacted with
            boolean isEntityScreen = newScreen instanceof AbstractContainerScreen &&
                mc.hitResult instanceof EntityHitResult &&
                ((EntityHitResult) mc.hitResult).getEntity() instanceof ContainerEntity;

            VRData.VRDevicePose facingDevice = infrontOfHand ? dh.vrPlayer.vrdata_room_pre.getController(0) : dh.vrPlayer.vrdata_room_pre.hmd;

            if (guiAppearOverBlockActive && (isBlockScreen || isEntityScreen) && dh.vrSettings.guiAppearOverBlock) {
                // appear over block / entity
                Vec3 sourcePos;
                if (isEntityScreen) {
                    EntityHitResult entityHitResult = (EntityHitResult) mc.hitResult;
                    sourcePos = new Vec3(entityHitResult.getEntity().getX(), entityHitResult.getEntity().getY(), entityHitResult.getEntity().getZ());
                } else {
                    BlockHitResult blockHitResult = (BlockHitResult) mc.hitResult;
                    sourcePos = new Vec3(((float) blockHitResult.getBlockPos().getX() + 0.5F), blockHitResult.getBlockPos().getY(), ((float) blockHitResult.getBlockPos().getZ() + 0.5F));
                }

                Vec3 roomPos = VRPlayer.world_to_room_pos(sourcePos, dh.vrPlayer.vrdata_world_pre);
                Vec3 hmdPos = dh.vrPlayer.vrdata_room_pre.hmd.getPosition();
                double distance = roomPos.subtract(hmdPos).length();
                guiScale = (float) Math.sqrt(distance);
                Vec3 sourcePosWorld = new Vec3(sourcePos.x, sourcePos.y + 1.1D + (double) (0.5F * guiScale / 2.0F), sourcePos.z);
                guiPos_room = VRPlayer.world_to_room_pos(sourcePosWorld, dh.vrPlayer.vrdata_world_pre);
            } else {
                // static screens like menu, inventory, and dead.
                Vec3 offset = new Vec3(0.0D, 0.0D, -2.0D);

                if (newScreen instanceof ChatScreen) {
                    offset = new Vec3(0.0D, 0.5D, -2.0D);
                } else if (newScreen instanceof BookEditScreen || newScreen instanceof AbstractSignEditScreen) {
                    offset = new Vec3(0.0D, 0.25D, -2.0D);
                }

                Vec3 hmdPos = facingDevice.getPosition();
                Vec3 look = facingDevice.getCustomVector(offset);
                guiPos_room = new Vec3(
                    look.x / 2.0D + hmdPos.x,
                    look.y / 2.0D + hmdPos.y,
                    look.z / 2.0D + hmdPos.z);

                if (dh.vrSettings.physicalKeyboard && KeyboardHandler.Showing && guiPos_room.y < hmdPos.y + 0.2D) {
                    guiPos_room = new Vec3(guiPos_room.x, hmdPos.y + 0.2D, guiPos_room.z);
                }
            }

            // orient screen
            Vec3 hmdPos = facingDevice.getPosition();
            Vector3 look = new Vector3();
            look.setX((float) (guiPos_room.x - hmdPos.x));
            look.setY((float) (guiPos_room.y - hmdPos.y));
            look.setZ((float) (guiPos_room.z - hmdPos.z));
            float pitch = (float) Math.asin((look.getY() / look.length()));
            float yaw = (float) (Math.PI + Math.atan2(look.getX(), look.getZ()));
            guiRotation_room = Matrix4f.rotationY(yaw);
            Matrix4f tilt = Utils.rotationXMatrix(pitch);
            guiRotation_room = Matrix4f.multiply(guiRotation_room, tilt);
        }

        KeyboardHandler.orientOverlay(newScreen != null);
    }

    public static Vec3 applyGUIModelView(RenderPass currentPass, PoseStack pMatrixStack) {
        mc.getProfiler().push("applyGUIModelView");

        if (mc.screen != null && guiPos_room == null) {
            //naughty mods!
            onScreenChanged(null, mc.screen, false);
        } else if (mc.screen == null && !mc.mouseHandler.isMouseGrabbed()) {
            // some mod want's to do a mouse selection overlay
            if (guiPos_room == null) {
                onScreenChanged(null, new Screen(Component.empty()) {
                }, false, true);
            }
        } else if (mc.screen == null && guiPos_room != null) {
            //even naughtier mods!
            // someone canceled the setScreen, so guiPos didn't get reset
            onScreenChanged(null, null, false);
        }

        Vec3 guipos = guiPos_room;
        Matrix4f guirot = guiRotation_room;
        Vec3 guilocal = new Vec3(0.0D, 0.0D, 0.0D);
        float scale = guiScale;

        if (guipos == null) {
            guirot = null;
            scale = 1.0F;

            if (mc.level != null && (mc.screen == null || !dh.vrSettings.floatInventory)) {
                // HUD view - attach to head or controller
                int i = 1;

                if (dh.vrSettings.reverseHands) {
                    i = -1;
                }

                if (dh.vrSettings.seated || dh.vrSettings.vrHudLockMode == VRSettings.HUDLock.HEAD) {
                    Matrix4f rot = Matrix4f.rotationY(dh.vrPlayer.vrdata_world_render.rotation_radians);
                    Matrix4f max = Matrix4f.multiply(rot, dh.vr.hmdRotation);

                    Vec3 position = dh.vrPlayer.vrdata_world_render.hmd.getPosition();
                    Vec3 direction = dh.vrPlayer.vrdata_world_render.hmd.getDirection();

                    if (dh.vrSettings.seated && dh.vrSettings.seatedHudAltMode) {
                        direction = dh.vrPlayer.vrdata_world_render.getController(0).getDirection();
                        max = Matrix4f.multiply(rot, dh.vr.getAimRotation(0));
                    }

                    guipos = new Vec3(
                        position.x + direction.x * dh.vrPlayer.vrdata_world_render.worldScale * dh.vrSettings.hudDistance,
                        position.y + direction.y * dh.vrPlayer.vrdata_world_render.worldScale * dh.vrSettings.hudDistance,
                        position.z + direction.z * dh.vrPlayer.vrdata_world_render.worldScale * dh.vrSettings.hudDistance);

                    Quaternion orientationQuat = OpenVRUtil.convertMatrix4ftoRotationQuat(max);
                    guirot = new Matrix4f(orientationQuat);
                    scale = dh.vrSettings.hudScale;
                } else {
                    if (dh.vrSettings.vrHudLockMode == VRSettings.HUDLock.HAND) {
                        // hud on hand
                        Matrix4f out = dh.vr.getAimRotation(1);
                        Matrix4f rot = Matrix4f.rotationY(dh.vrPlayer.vrdata_world_render.rotation_radians);
                        Matrix4f guiRotationPose = Matrix4f.multiply(rot, out);
                        guirot = Matrix4f.multiply(guiRotationPose, Utils.rotationXMatrix(((float) Math.PI * -0.2F)));
                        guirot = Matrix4f.multiply(guirot, Matrix4f.rotationY((float) Math.PI * 0.1F * i));
                        scale = 0.58823526F;

                        guilocal = new Vec3(guilocal.x, 0.32D * dh.vrPlayer.vrdata_world_render.worldScale, guilocal.z);

                        guipos = RenderHelper.getControllerRenderPos(1);

                        dh.vr.hudPopup = true;
                    } else if (dh.vrSettings.vrHudLockMode == VRSettings.HUDLock.WRIST) {
                        // hud on wrist
                        Matrix4f out = dh.vr.getAimRotation(1);
                        Matrix4f rot = Matrix4f.rotationY(dh.vrPlayer.vrdata_world_render.rotation_radians);
                        guirot = Matrix4f.multiply(rot, out);

                        guirot = Matrix4f.multiply(guirot, Utils.rotationZMatrix((float) Math.PI * 0.5F * i));
                        guirot = Matrix4f.multiply(guirot, Matrix4f.rotationY((float) Math.PI * 0.3F * i));

                        guipos = RenderHelper.getControllerRenderPos(1);
                        dh.vr.hudPopup = true;

                        boolean slim = mc.player.getSkin().model().id().equals("slim");
                        scale = 0.4F;
                        float offset = mc.player.getMainArm().getOpposite() == (dh.vrSettings.reverseHands ? HumanoidArm.LEFT : HumanoidArm.RIGHT) ? -0.166F : -0.136F;
                        guilocal = new Vec3(
                            i * offset * dh.vrPlayer.vrdata_world_render.worldScale,
                            (slim ? 0.13D : 0.12D) * dh.vrPlayer.vrdata_world_render.worldScale,
                            0.06D * dh.vrPlayer.vrdata_world_render.worldScale);
                        guirot = Matrix4f.multiply(guirot, Matrix4f.rotationY((float) Math.PI * 0.2F * i));
                    }
                }
            }
        } else {
            // convert previously calculated coords to world coords
            guipos = VRPlayer.room_to_world_pos(guipos, dh.vrPlayer.vrdata_world_render);
            Matrix4f rot = Matrix4f.rotationY(dh.vrPlayer.vrdata_world_render.rotation_radians);
            guirot = Matrix4f.multiply(rot, guirot);
        }

        if ((dh.vrSettings.seated || dh.vrSettings.menuAlwaysFollowFace) && MethodHolder.isInMenuRoom()) {
            // main menu slow yaw tracking thing
            scale = 2.0F;
            Vec3 posAvg = new Vec3(0.0D, 0.0D, 0.0D);

            for (Vec3 sample : dh.vr.hmdPosSamples) {
                posAvg = posAvg.add(sample);
            }

            posAvg = new Vec3(
                posAvg.x / dh.vr.hmdPosSamples.size(),
                posAvg.y / dh.vr.hmdPosSamples.size(),
                posAvg.z / dh.vr.hmdPosSamples.size());

            float yawAvg = 0.0F;

            for (float sample : dh.vr.hmdYawSamples) {
                yawAvg += sample;
            }

            yawAvg /= dh.vr.hmdYawSamples.size();
            yawAvg = (float) Math.toRadians(yawAvg);

            Vec3 dir = new Vec3(-Math.sin(yawAvg), 0.0D, Math.cos(yawAvg));
            float dist = MethodHolder.isInMenuRoom() ?
                2.5F * dh.vrPlayer.vrdata_world_render.worldScale : dh.vrSettings.hudDistance;

            Vec3 pos = posAvg.add(new Vec3(dir.x * dist, dir.y * dist, dir.z * dist));

            Matrix4f guiRotation = Matrix4f.rotationY((float) Math.PI - yawAvg);
            guirot = Matrix4f.multiply(guiRotation, Matrix4f.rotationY(dh.vrPlayer.vrdata_world_render.rotation_radians));
            guipos = VRPlayer.room_to_world_pos(pos, dh.vrPlayer.vrdata_world_render);

            // for mouse control
            guiRotation_room = guiRotation;
            guiScale = 2.0F;
            guiPos_room = pos;
        }

        if (guipos == null) {
            VRSettings.logger.error("guipos was null, how did that happen. vrRunning: {}", VRState.vrRunning);
            new RuntimeException().printStackTrace();
            guiPos_room = new Vec3(0, 0, 0);
            guipos = VRPlayer.room_to_world_pos(guiPos_room, dh.vrPlayer.vrdata_world_render);
            guiRotation_room = new Matrix4f();
            guirot = new Matrix4f();
            guiScale = 1.0F;
        }

        Vec3 eye = RenderHelper.getSmoothCameraPosition(currentPass, dh.vrPlayer.vrdata_world_render);

        Vec3 translation = guipos.subtract(eye);
        pMatrixStack.translate(translation.x, translation.y, translation.z);

        // offset from eye to gui pos
        pMatrixStack.mulPoseMatrix(guirot.toMCMatrix());
        pMatrixStack.translate(guilocal.x, guilocal.y, guilocal.z);

        float thescale = scale * dh.vrPlayer.vrdata_world_render.worldScale;
        pMatrixStack.scale(thescale, thescale, thescale);

        guiScaleApplied = thescale;

        mc.getProfiler().pop();

        return guipos;
    }
}
