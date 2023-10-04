package org.vivecraft.client_vr.gameplay.screenhandlers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.vivecraft.client_vr.VRData.VRDevicePose;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.HandedKeyBinding;
import org.vivecraft.client_vr.provider.InputSimulator;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.settings.VRSettings.HUDLock;

import static org.joml.Math.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.vivecraft.client_vr.VRState.*;
import static org.vivecraft.common.utils.Utils.*;

public class GuiHandler {
    static boolean lastPressedLeftClick;
    static boolean lastPressedRightClick;
    static boolean lastPressedMiddleClick;
    static boolean lastPressedShift;
    static boolean lastPressedCtrl;
    static boolean lastPressedAlt;
    private static double controllerMouseX = -1.0D;
    private static double controllerMouseY = -1.0D;
    public static boolean controllerMouseValid;
    public static int controllerMouseTicks;
    public static boolean guiAppearOverBlockActive = false;
    public static float guiScale = 1.0F;
    public static float guiScaleApplied = 1.0F;
    public static Vec3 guiPos_room;
    public static Matrix4f guiRotation_room;
    public static final KeyMapping keyLeftClick = new KeyMapping("vivecraft.key.guiLeftClick", GLFW_KEY_UNKNOWN, "vivecraft.key.category.gui");
    public static final KeyMapping keyRightClick = new KeyMapping("vivecraft.key.guiRightClick", GLFW_KEY_UNKNOWN, "vivecraft.key.category.gui");
    public static final KeyMapping keyMiddleClick = new KeyMapping("vivecraft.key.guiMiddleClick", GLFW_KEY_UNKNOWN, "vivecraft.key.category.gui");
    public static final KeyMapping keyShift = new KeyMapping("vivecraft.key.guiShift", GLFW_KEY_UNKNOWN, "vivecraft.key.category.gui");
    public static final KeyMapping keyCtrl = new KeyMapping("vivecraft.key.guiCtrl", GLFW_KEY_UNKNOWN, "vivecraft.key.category.gui");
    public static final KeyMapping keyAlt = new KeyMapping("vivecraft.key.guiAlt", GLFW_KEY_UNKNOWN, "vivecraft.key.category.gui");
    public static final KeyMapping keyScrollUp = new KeyMapping("vivecraft.key.guiScrollUp", GLFW_KEY_UNKNOWN, "vivecraft.key.category.gui");
    public static final KeyMapping keyScrollDown = new KeyMapping("vivecraft.key.guiScrollDown", GLFW_KEY_UNKNOWN, "vivecraft.key.category.gui");
    public static final KeyMapping keyScrollAxis = new KeyMapping("vivecraft.key.guiScrollAxis", GLFW_KEY_UNKNOWN, "vivecraft.key.category.gui");
    public static final HandedKeyBinding keyKeyboardClick = new HandedKeyBinding("vivecraft.key.keyboardClick", GLFW_KEY_UNKNOWN, "vivecraft.key.category.keyboard") {
        @Override
        public boolean isPriorityOnController(ControllerType type) {
            if (KeyboardHandler.isShowing() && !dh.vrSettings.physicalKeyboard) {
                return KeyboardHandler.isUsingController(type);
            } else {
                return RadialHandler.isShowing() && RadialHandler.isUsingController(type);
            }
        }
    };
    public static final HandedKeyBinding keyKeyboardShift = new HandedKeyBinding("vivecraft.key.keyboardShift", GLFW_KEY_UNKNOWN, "vivecraft.key.category.keyboard") {
        @Override
        public boolean isPriorityOnController(ControllerType type) {
            if (KeyboardHandler.isShowing()) {
                return dh.vrSettings.physicalKeyboard || KeyboardHandler.isUsingController(type);
            } else {
                return RadialHandler.isShowing() && RadialHandler.isUsingController(type);
            }
        }
    };
    public static RenderTarget guiFramebuffer;
    public static int guiWidth = 1280;
    public static int guiHeight = 720;
    public static int guiScaleFactor = calculateScale(0, false, guiWidth, guiHeight);
    public static int scaledWidth;
    public static int scaledHeight;

    public static int calculateScale(int scaleIn, boolean forceUnicode, int framebufferWidth, int framebufferHeight) {
        int j = 1;

        while (
            j != scaleIn &&
                j < framebufferWidth &&
                j < framebufferHeight &&
                framebufferWidth / (j + 1) >= 320 &&
                framebufferHeight / (j + 1) >= 240
        ) {
            ++j;
        }

        if (forceUnicode && j % 2 != 0) {
            ++j;
        }

        int widthFloor = framebufferWidth / j;
        scaledWidth = framebufferWidth / j > widthFloor ? widthFloor + 1 : widthFloor;

        int heightFloor = framebufferHeight / j;
        scaledHeight = framebufferHeight / j > heightFloor ? heightFloor + 1 : heightFloor;

        return j;
    }

    public static void processGui() {
        if (mc.screen != null && !dh.vrSettings.seated && guiRotation_room != null && dh.vr.isControllerTracking(0)) {
            Vec2 vec2 = getTexCoordsForCursor(
                guiPos_room,
                guiRotation_room,
                guiScale,
                dh.vrPlayer.vrdata_room_pre.getController(0)
            );
            float f = vec2.x;
            float f1 = vec2.y;

            if (!(f < 0.0F) && !(f1 < 0.0F) && !(f > 1.0F) && !(f1 > 1.0F)) {
                if (controllerMouseX == -1.0D) {
                    controllerMouseX = (int) (f * (float) mc.getWindow().getScreenWidth());
                    controllerMouseY = (int) (f1 * (float) mc.getWindow().getScreenHeight());
                } else {
                    float f2 = (int) (f * (float) mc.getWindow().getScreenWidth());
                    float f3 = (int) (f1 * (float) mc.getWindow().getScreenHeight());
                    controllerMouseX = controllerMouseX * (double) 0.7F + (double) (f2 * 0.3F);
                    controllerMouseY = controllerMouseY * (double) 0.7F + (double) (f3 * 0.3F);
                }
            } else {
                controllerMouseX = -1.0D;
                controllerMouseY = -1.0D;
            }

            if (controllerMouseX >= 0.0D && controllerMouseX < (double) mc.getWindow().getScreenWidth() &&
                controllerMouseY >= 0.0D && controllerMouseY < (double) mc.getWindow().getScreenHeight()) {
                double d1 = clamp((int) controllerMouseX, 0, mc.getWindow().getScreenWidth());
                double d0 = clamp((int) controllerMouseY, 0, mc.getWindow().getScreenHeight());

                if (dh.vr.isControllerTracking(ControllerType.RIGHT)) {
                    InputSimulator.setMousePos(d1, d0);
                    controllerMouseValid = true;
                }
            } else {
                if (controllerMouseTicks == 0) {
                    controllerMouseValid = false;
                }

                if (controllerMouseTicks > 0) {
                    --controllerMouseTicks;
                }
            }
        }
    }

    public static Vec2 getTexCoordsForCursor(Vec3 guiPos_room, Matrix4f guiRotation_room, float guiScale, VRDevicePose controller) {
        Vec3 vec3 = controller.getPosition();
        Vector3f vector3 = convertToVector3f(vec3, new Vector3f());
        Vec3 vec31 = controller.getDirection();
        Vector3f vector31 = convertToVector3f(vec31, new Vector3f());
        Vector3f vector33 = guiRotation_room.transformProject(backward, new Vector3f());
        Vector3f vector34 = guiRotation_room.transformProject(right, new Vector3f());
        Vector3f vector35 = guiRotation_room.transformProject(up, new Vector3f());
        float f = vector33.dot(vector31);

        if (abs(f) > 1.0E-5F) {
            float f1 = 1.0F;
            float f2 = f1 * 0.5F;
            float f3 = 1.0F;
            float f4 = f3 * 0.5F;
            Vector3f vector36 = convertToVector3f(guiPos_room, new Vector3f());
            Vector3f vector37 = vector36.sub(vector35.div(1.0F / f4, new Vector3f()), new Vector3f()).sub(vector34.div(1.0F / f2, new Vector3f()), new Vector3f());
            float f5 = -vector33.dot(vector3.sub(vector37, new Vector3f())) / f;

            if (f5 > 0.0F) {
                Vector3f vector38 = vector3.add(vector31.div(1.0F / f5, new Vector3f()), new Vector3f());
                Vector3f vector39 = vector38.sub(vector37, new Vector3f());
                float f6 = vector39.dot(vector34.div(1.0F / f1, new Vector3f()));
                float f7 = vector39.dot(vector35.div(1.0F / f1, new Vector3f()));
                float f8 = (float) mc.getWindow().getGuiScaledHeight() / (float) mc.getWindow().getGuiScaledWidth();
                f6 = (f6 - 0.5F) / 1.5F / guiScale + 0.5F;
                f7 = (f7 - 0.5F) / f8 / 1.5F / guiScale + 0.5F;
                f7 = 1.0F - f7;
                return new Vec2(f6, f7);
            }
        }

        return new Vec2(-1.0F, -1.0F);
    }

    public static void processBindingsGui() {
        boolean flag = controllerMouseX >= 0.0D && controllerMouseX < (double) mc.getWindow().getScreenWidth() && controllerMouseY >= 0.0D && controllerMouseY < (double) mc.getWindow().getScreenWidth();

        if (keyLeftClick.consumeClick() && mc.screen != null && flag) {
            InputSimulator.pressMouse(GLFW_MOUSE_BUTTON_LEFT);
            lastPressedLeftClick = true;
        }

        if (!keyLeftClick.isDown() && lastPressedLeftClick) {
            InputSimulator.releaseMouse(GLFW_MOUSE_BUTTON_LEFT);
            lastPressedLeftClick = false;
        }

        if (keyRightClick.consumeClick() && mc.screen != null && flag) {
            InputSimulator.pressMouse(GLFW_MOUSE_BUTTON_RIGHT);
            lastPressedRightClick = true;
        }

        if (!keyRightClick.isDown() && lastPressedRightClick) {
            InputSimulator.releaseMouse(GLFW_MOUSE_BUTTON_RIGHT);
            lastPressedRightClick = false;
        }

        if (keyMiddleClick.consumeClick() && mc.screen != null && flag) {
            InputSimulator.pressMouse(GLFW_MOUSE_BUTTON_MIDDLE);
            lastPressedMiddleClick = true;
        }

        if (!keyMiddleClick.isDown() && lastPressedMiddleClick) {
            InputSimulator.releaseMouse(GLFW_MOUSE_BUTTON_MIDDLE);
            lastPressedMiddleClick = false;
        }

        if (keyShift.consumeClick() && mc.screen != null) {
            InputSimulator.pressKey(GLFW_KEY_LEFT_SHIFT);
            lastPressedShift = true;
        }

        if (!keyShift.isDown() && lastPressedShift) {
            InputSimulator.releaseKey(GLFW_KEY_LEFT_SHIFT);
            lastPressedShift = false;
        }

        if (keyCtrl.consumeClick() && mc.screen != null) {
            InputSimulator.pressKey(GLFW_KEY_LEFT_CONTROL);
            lastPressedCtrl = true;
        }

        if (!keyCtrl.isDown() && lastPressedCtrl) {
            InputSimulator.releaseKey(GLFW_KEY_LEFT_CONTROL);
            lastPressedCtrl = false;
        }

        if (keyAlt.consumeClick() && mc.screen != null) {
            InputSimulator.pressKey(GLFW_KEY_LEFT_ALT);
            lastPressedAlt = true;
        }

        if (!keyAlt.isDown() && lastPressedAlt) {
            InputSimulator.releaseKey(GLFW_KEY_LEFT_ALT);
            lastPressedAlt = false;
        }

        if (keyScrollUp.consumeClick() && mc.screen != null) {
            InputSimulator.scrollMouse(0.0D, 4.0D);
        }

        if (keyScrollDown.consumeClick() && mc.screen != null) {
            InputSimulator.scrollMouse(0.0D, -4.0D);
        }
    }

    public static void onScreenChanged(Screen previousGuiScreen, Screen newScreen, boolean unpressKeys) {
        if (!vrRunning) {
            return;
        }

        if (unpressKeys) {
            dh.vr.ignorePressesNextFrame = true;
        }

        if (newScreen == null) {
            guiPos_room = null;
            guiRotation_room = null;
            guiScale = 1.0F;

            if (KeyboardHandler.keyboardForGui) {
                KeyboardHandler.setOverlayShowing(false);
            }
        } else {
            RadialHandler.setOverlayShowing(false, null);
        }

        if (mc.level != null && !(newScreen instanceof WinScreen)) {
            if (dh.vrSettings.worldRotationCached != 0.0F) {
                dh.vrSettings.worldRotation = dh.vrSettings.worldRotationCached;
                dh.vrSettings.worldRotationCached = 0.0F;
            }
        } else {
            dh.vrSettings.worldRotationCached = dh.vrSettings.worldRotation;
            dh.vrSettings.worldRotation = 0.0F;
        }

        // check if the new screen is meant to show the MenuRoom, instead of the current screen
        boolean staticScreen = ((GameRendererExtension) mc.gameRenderer).vivecraft$willBeInMenuRoom(newScreen);
        staticScreen = staticScreen & (!dh.vrSettings.seated && !dh.vrSettings.menuAlwaysFollowFace);

        if (staticScreen) {
            guiScale = 2.0F;
            Vector2f afloat = dh.vr.getPlayAreaSize();
            // slight offset to center of the room, to prevent z fighting
            guiPos_room = new Vec3(0.02D, 1.3F, -max(afloat != null ? afloat.y / 2.0F : 0.0F, 1.5F));
            guiRotation_room = new Matrix4f();
        } else {
            if (previousGuiScreen == null && newScreen != null || newScreen instanceof ChatScreen || newScreen instanceof BookEditScreen || newScreen instanceof AbstractSignEditScreen) {
                // check if screen is a container screen
                // and if the pointed at block is the same that was last interacted with
                boolean isBlockScreen = newScreen instanceof AbstractContainerScreen
                    && mc.hitResult != null
                    && (mc.hitResult.getType() == Type.BLOCK);

                // check if screen is a container screen
                // and if the pointed at entity is the same that was last interacted with
                boolean isEntityScreen = newScreen instanceof AbstractContainerScreen
                    && mc.hitResult instanceof EntityHitResult
                    && ((EntityHitResult) mc.hitResult).getEntity() instanceof ContainerEntity;

                Vec3 hmdPos = dh.vrPlayer.vrdata_room_pre.hmd.getPosition();

                if (guiAppearOverBlockActive && (isBlockScreen || isEntityScreen) && dh.vrSettings.guiAppearOverBlock) {
                    Vec3 sourcePos;
                    if (isEntityScreen) {
                        EntityHitResult entityHitResult = (EntityHitResult) mc.hitResult;
                        sourcePos = new Vec3(entityHitResult.getEntity().getX(), entityHitResult.getEntity().getY(), entityHitResult.getEntity().getZ());
                    } else {
                        BlockHitResult blockHitResult = (BlockHitResult) mc.hitResult;
                        sourcePos = new Vec3(((float) blockHitResult.getBlockPos().getX() + 0.5F), blockHitResult.getBlockPos().getY(), ((float) blockHitResult.getBlockPos().getZ() + 0.5F));
                    }

                    Vec3 roomPos = VRPlayer.world_to_room_pos(sourcePos, dh.vrPlayer.vrdata_world_pre);
                    double distance = roomPos.subtract(hmdPos).length();
                    guiScale = (float) sqrt(distance);
                    Vec3 sourcePosWorld = new Vec3(sourcePos.x, sourcePos.y + 1.1D + (double) (0.5F * guiScale / 2.0F), sourcePos.z);
                    guiPos_room = VRPlayer.world_to_room_pos(sourcePosWorld, dh.vrPlayer.vrdata_world_pre);
                } else {
                    Vec3 vec3 = new Vec3(0.0D, 0.0D, -2.0D);

                    if (newScreen instanceof ChatScreen) {
                        vec3 = new Vec3(0.0D, 0.5D, -2.0D);
                    } else if (newScreen instanceof BookEditScreen || newScreen instanceof AbstractSignEditScreen) {
                        vec3 = new Vec3(0.0D, 0.25D, -2.0D);
                    }

                    Vec3 vec32 = dh.vrPlayer.vrdata_room_pre.hmd.getCustomVector(vec3);
                    guiPos_room = new Vec3(vec32.x / 2.0D + hmdPos.x, vec32.y / 2.0D + hmdPos.y, vec32.z / 2.0D + hmdPos.z);

                    if (dh.vrSettings.physicalKeyboard && KeyboardHandler.isShowing() && guiPos_room.y < hmdPos.y + 0.2D) {
                        guiPos_room = new Vec3(guiPos_room.x, hmdPos.y + 0.2D, guiPos_room.z);
                    }
                }

                // orient screen
                Vector3f look = new Vector3f((float) (guiPos_room.x - hmdPos.x), (float) (guiPos_room.y - hmdPos.y), (float) (guiPos_room.z - hmdPos.z));
                float pitch = asin((look.y / look.length()));
                float yaw = (float) (PI + atan2(look.x, look.z));
                guiRotation_room = new Matrix4f().rotationY(yaw).rotateX(pitch);
            }

            KeyboardHandler.orientOverlay(newScreen != null);
        }
    }

    public static Vector3f applyGUIModelView(RenderPass currentPass, PoseStack pMatrixStack) {
        mc.getProfiler().push("applyGUIModelView");

        if (mc.screen != null && guiPos_room == null) {
            //naughty mods!
            onScreenChanged(null, mc.screen, false);
        } else if (mc.screen == null && guiPos_room != null) {
            //even naughtier mods!
            // someone canceled the setScreen, so guiPos didn't get reset
            onScreenChanged(null, null, false);
        }

        Vector3f guipos = new Vector3f();
        Matrix4f guirot = new Matrix4f();
        Vector3f guilocal = new Vector3f();
        float scale = guiScale;

        if (guiPos_room == null) {
            if (guiRotation_room != null) {
                guirot.set(guiRotation_room);
            }
            scale = 1.0F;

            if (mc.level != null && (mc.screen == null || !dh.vrSettings.floatInventory)) {
                int hand = dh.vrSettings.reverseHands ? -1 : 1;
                guirot.rotationY(dh.vrPlayer.vrdata_world_render.rotation_radians);
                switch (dh.vrSettings.seated ? HUDLock.HEAD : dh.vrSettings.vrHudLockMode) {
                    case HAND -> {
                        guirot.mul0(dh.vr.getAimRotation(1))
                            .rotateX(-(float) PI / 5F)
                            .rotateY(((float) PI * 0.1F) * hand);
                        scale = 0.58823526F;
                        guilocal.y = 0.32F * dh.vrPlayer.vrdata_world_render.worldScale;
                        convertToVector3f(RenderHelper.getControllerRenderPos(1), guipos);
                        dh.vr.hudPopup = true;
                    }
                    case WRIST -> {
                        guirot.mul0(dh.vr.getAimRotation(1))
                            .rotateZ(((float) PI / 2F) * hand)
                            .rotateY(((float) PI * 0.3F) * hand);
                        convertToVector3f(RenderHelper.getControllerRenderPos(1), guipos);
                        dh.vr.hudPopup = true;
                        scale = 0.4F;
                        guilocal.set(
                            hand * -0.136F * dh.vrPlayer.vrdata_world_render.worldScale,
                            ("slim".equals(mc.player.getSkin().model().id()) ? 0.13F : 0.12F) * dh.vrPlayer.vrdata_world_render.worldScale,
                            0.06F * dh.vrPlayer.vrdata_world_render.worldScale
                        );
                        guirot.rotateY(((float) PI / 5F) * hand);
                    }
                    default -> {
                        guirot.mul0(dh.vr.hmdRotation);
                        convertToVector3f(dh.vrPlayer.vrdata_world_render.hmd.getPosition(), guipos);
                        Vector3f vec34 = new Vector3f();

                        if (dh.vrSettings.seated && dh.vrSettings.seatedHudAltMode) {
                            convertToVector3f(dh.vrPlayer.vrdata_world_render.getController(0).getDirection(), vec34);
                            guirot.mul0(dh.vr.getAimRotation(0));
                        } else {
                            convertToVector3f(dh.vrPlayer.vrdata_world_render.hmd.getDirection(), vec34);
                        }

                        guipos.add(vec34.mul(dh.vrPlayer.vrdata_world_render.worldScale * dh.vrSettings.hudDistance));

                        scale = dh.vrSettings.hudScale;
                    }
                }
            }
        } else {
            convertToVector3f(VRPlayer.room_to_world_pos(guiPos_room, dh.vrPlayer.vrdata_world_render), guipos);
            guirot.set(guiRotation_room).rotateY(dh.vrPlayer.vrdata_world_render.rotation_radians);
        }

        if ((dh.vrSettings.seated || dh.vrSettings.menuAlwaysFollowFace) && ((GameRendererExtension) mc.gameRenderer).vivecraft$isInMenuRoom()) {
            scale = 2.0F;
            Vector3d vec35 = new Vector3d();

            for (Vec3 vec37 : dh.vr.hmdPosSamples) {
                vec35.add(vec37.x, vec37.y, vec37.z);
            }

            vec35.div(dh.vr.hmdPosSamples.size());
            float f1 = 0.0F;

            for (float f3 : dh.vr.hmdYawSamples) {
                f1 += f3;
            }

            f1 /= dh.vr.hmdYawSamples.size();
            f1 = toRadians(f1);
            float f4 = ((GameRendererExtension) mc.gameRenderer).vivecraft$isInMenuRoom() ? 2.5F * dh.vrPlayer.vrdata_world_render.worldScale : dh.vrSettings.hudDistance;
            vec35.add(-sin(f1) * f4, 0, cos(f1) * f4);
            guiPos_room = convertToVec3(vec35);
            convertToVector3f(VRPlayer.room_to_world_pos(guiPos_room, dh.vrPlayer.vrdata_world_render), guipos);
            guiRotation_room = guirot.rotationY((float) PI - f1).rotateY(dh.vrPlayer.vrdata_world_render.rotation_radians);
            guiScale = 2.0F;
        }

        Vector3f eye = dh.vrPlayer.vrdata_world_render.getEye(currentPass).getPosition().toVector3f();
        pMatrixStack.last().pose().translate(guipos.sub(eye)).mul(guirot).translate(guilocal);
        float f2 = scale * dh.vrPlayer.vrdata_world_render.worldScale;
        pMatrixStack.scale(f2, f2, f2);
        guiScaleApplied = f2;
        mc.getProfiler().pop();
        return guipos;
    }
}
