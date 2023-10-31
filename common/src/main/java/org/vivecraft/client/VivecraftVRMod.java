package org.vivecraft.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.provider.HandedKeyBinding;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VivecraftVRMod {

    public static final HandedKeyBinding keyClimbeyGrab = new HandedKeyBinding("vivecraft.key.climbeyGrab", GLFW.GLFW_KEY_UNKNOWN, "vivecraft.key.category.climbey");
    public static final HandedKeyBinding keyClimbeyJump = new HandedKeyBinding("vivecraft.key.climbeyJump", GLFW.GLFW_KEY_UNKNOWN, "vivecraft.key.category.climbey");
    public static final KeyMapping keyExportWorld = new KeyMapping("vivecraft.key.exportWorld", GLFW.GLFW_KEY_UNKNOWN, "key.categories.misc");
    public static final KeyMapping keyFreeMoveRotate = new KeyMapping("vivecraft.key.freeMoveRotate", GLFW.GLFW_KEY_UNKNOWN, "key.categories.movement");
    public static final KeyMapping keyFreeMoveStrafe = new KeyMapping("vivecraft.key.freeMoveStrafe", GLFW.GLFW_KEY_UNKNOWN, "key.categories.movement");
    public static final KeyMapping keyHotbarNext = new KeyMapping("vivecraft.key.hotbarNext", GLFW.GLFW_KEY_UNKNOWN, "key.categories.inventory");
    public static final KeyMapping keyHotbarPrev = new KeyMapping("vivecraft.key.hotbarPrev", GLFW.GLFW_KEY_UNKNOWN, "key.categories.inventory");
    public static final KeyMapping keyHotbarScroll = new KeyMapping("vivecraft.key.hotbarScroll", GLFW.GLFW_KEY_UNKNOWN, "key.categories.inventory");
    public static final KeyMapping keyHotbarSwipeX = new KeyMapping("vivecraft.key.hotbarSwipeX", GLFW.GLFW_KEY_UNKNOWN, "key.categories.inventory");
    public static final KeyMapping keyHotbarSwipeY = new KeyMapping("vivecraft.key.hotbarSwipeY", GLFW.GLFW_KEY_UNKNOWN, "key.categories.inventory");
    public static final KeyMapping keyMenuButton = new KeyMapping("vivecraft.key.ingameMenuButton", GLFW.GLFW_KEY_UNKNOWN, "key.categories.ui");
    public static final KeyMapping keyMoveThirdPersonCam = new KeyMapping("vivecraft.key.moveThirdPersonCam", GLFW.GLFW_KEY_UNKNOWN, "key.categories.misc");
    public static final KeyMapping keyQuickHandheldCam = new KeyMapping("vivecraft.key.quickHandheldCam", GLFW.GLFW_KEY_UNKNOWN, "key.categories.misc");
    public static final KeyMapping keyQuickTorch = new KeyMapping("vivecraft.key.quickTorch", GLFW.GLFW_KEY_UNKNOWN, "key.categories.gameplay");
    public static final KeyMapping keyRadialMenu = new KeyMapping("vivecraft.key.radialMenu", GLFW.GLFW_KEY_UNKNOWN, "key.categories.ui");
    public static final KeyMapping keyRotateAxis = new KeyMapping("vivecraft.key.rotateAxis", GLFW.GLFW_KEY_UNKNOWN, "key.categories.movement");
    public static final KeyMapping keyRotateFree = new KeyMapping("vivecraft.key.rotateFree", GLFW.GLFW_KEY_UNKNOWN, "key.categories.movement");
    public static final KeyMapping keyRotateLeft = new KeyMapping("vivecraft.key.rotateLeft", GLFW.GLFW_KEY_UNKNOWN, "key.categories.movement");
    public static final KeyMapping keyRotateRight = new KeyMapping("vivecraft.key.rotateRight", GLFW.GLFW_KEY_UNKNOWN, "key.categories.movement");
    public static final KeyMapping keySwapMirrorView = new KeyMapping("vivecraft.key.swapMirrorView", GLFW.GLFW_KEY_UNKNOWN, "key.categories.misc");
    public static final KeyMapping keyTeleport = new KeyMapping("vivecraft.key.teleport", GLFW.GLFW_KEY_UNKNOWN, "key.categories.movement");
    public static final KeyMapping keyTeleportFallback = new KeyMapping("vivecraft.key.teleportFallback", GLFW.GLFW_KEY_UNKNOWN, "key.categories.movement");
    public static final KeyMapping keyToggleHandheldCam = new KeyMapping("vivecraft.key.toggleHandheldCam", GLFW.GLFW_KEY_UNKNOWN, "key.categories.misc");
    public static final KeyMapping keyToggleKeyboard = new KeyMapping("vivecraft.key.toggleKeyboard", GLFW.GLFW_KEY_UNKNOWN, "key.categories.ui");
    public static final KeyMapping keyToggleMovement = new KeyMapping("vivecraft.key.toggleMovement", GLFW.GLFW_KEY_UNKNOWN, "key.categories.movement");
    public static final KeyMapping keyTogglePlayerList = new KeyMapping("vivecraft.key.togglePlayerList", GLFW.GLFW_KEY_UNKNOWN, "key.categories.multiplayer");
    public static final HandedKeyBinding keyTrackpadTouch = new HandedKeyBinding("vivecraft.key.trackpadTouch", GLFW.GLFW_KEY_UNKNOWN, "key.categories.misc");
    public static final HandedKeyBinding keyVRInteract = new HandedKeyBinding("vivecraft.key.vrInteract", GLFW.GLFW_KEY_UNKNOWN, "key.categories.gameplay");
    public static final KeyMapping keyWalkabout = new KeyMapping("vivecraft.key.walkabout", GLFW.GLFW_KEY_UNKNOWN, "key.categories.movement");

    /** Key binds provided by vivecraft, which the player may bind. */
    public static final Set<KeyMapping> userKeyBindingSet = new LinkedHashSet<>(Arrays.asList(
        keyRotateLeft,
        keyRotateRight,
        keyTeleport,
        keyTeleportFallback,
        keyToggleMovement,
        keyQuickTorch,
        keySwapMirrorView,
        keyExportWorld,
        keyMoveThirdPersonCam,
        keyTogglePlayerList,
        keyToggleHandheldCam,
        keyQuickHandheldCam
    ));

    /** Key binds internal to vivecraft, which the player may <u>not</u> bind. */
    public static final Set<KeyMapping> hiddenKeyBindingSet = new LinkedHashSet<>(Arrays.asList(
        GuiHandler.keyLeftClick,
        GuiHandler.keyRightClick,
        GuiHandler.keyMiddleClick,
        GuiHandler.keyShift,
        GuiHandler.keyCtrl,
        GuiHandler.keyAlt,
        GuiHandler.keyScrollUp,
        GuiHandler.keyScrollDown,
        GuiHandler.keyScrollAxis,
        GuiHandler.keyKeyboardClick,
        GuiHandler.keyKeyboardShift,
        keyClimbeyGrab,
        keyClimbeyJump,
        keyMenuButton,
        keyRadialMenu,
        keyToggleKeyboard,
        keyHotbarSwipeX,
        keyHotbarSwipeY,
        keyTrackpadTouch,
        keyRotateAxis,
        keyRotateFree,
        keyFreeMoveRotate,
        keyFreeMoveStrafe,
        keyHotbarNext,
        keyHotbarPrev,
        keyHotbarScroll,
        keyVRInteract,
        keyWalkabout
    ));

    /** Key binds provided by vanilla. */
    public static final Set<KeyMapping> vanillaBindingSet = new LinkedHashSet<>();

    /** Key binds provided to the player. */
    public static final Set<KeyMapping> allKeyBindingSet = Stream.concat(
        userKeyBindingSet.stream(),
        hiddenKeyBindingSet.stream()
    ).collect(Collectors.toSet());

    private static final Minecraft mc = Minecraft.getInstance();

    public static boolean isSafeBinding(KeyMapping kb) {
        return allKeyBindingSet.contains(kb) || kb == mc.options.keyChat || kb == mc.options.keyInventory;
    }

    /** checks if a key bind is NOT provided by vanilla NOR internal to vivecraft */
    public static boolean isModBinding(final KeyMapping kb) {
        return !hiddenKeyBindingSet.contains(kb) && !vanillaBindingSet.contains(kb);
    }
}
