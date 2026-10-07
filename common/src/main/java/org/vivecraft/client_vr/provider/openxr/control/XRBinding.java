package org.vivecraft.client_vr.provider.openxr.control;

import org.vivecraft.client_vr.provider.control.ActionType;
import org.vivecraft.client_vr.provider.control.VRInputActionSet;
import org.vivecraft.client_vr.provider.openxr.MCOpenXR;

import javax.annotation.Nullable;
import java.util.HashSet;

public record XRBinding(@Nullable VRInputActionSet actionSet, String key, String controller, ActionType actionType) {

    public static HashSet<String> supportedHeadsets() {
        HashSet<String> set = new HashSet<>();
        if (MCOpenXR.get().session.getCapabilities().XR_HTC_vive_cosmos_controller_interaction) {
            set.add("/interaction_profiles/htc/vive_cosmos_controller");
        }

        if (MCOpenXR.get().session.getCapabilities().XR_BD_controller_interaction) {
            set.add("/interaction_profiles/bytedance/pico4_controller");
            set.add("/interaction_profiles/bytedance/pico_neo3_controller");
        }

        set.add("/interaction_profiles/khr/simple_controller");
        set.add("/interaction_profiles/oculus/touch_controller");
        set.add("/interaction_profiles/htc/vive_controller");
        set.add("/interaction_profiles/valve/index_controller");
        return set;
    }

    private static HashSet<XRBinding> quest2Bindings() {
        HashSet<XRBinding> set = new HashSet<>();

        set.add(
            new XRBinding(null, "/actions/global/in/vivecraft.key.ingameMenuButton", "/user/hand/left/input/y/click",
                ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/global/in/key.inventory", "/user/hand/left/input/x/click",
            ActionType.BOOLEAN));

        set.add(new XRBinding(null, "/actions/gui/in/vivecraft.key.guiShift", "/user/hand/left/input/squeeze/value",
            ActionType.VEC1));
        set.add(
            new XRBinding(null, "/actions/gui/in/vivecraft.key.guiMiddleClick", "/user/hand/right/input/squeeze/value",
                ActionType.VEC1));
        set.add(
            new XRBinding(null, "/actions/gui/in/vivecraft.key.guiLeftClick", "/user/hand/right/input/trigger/value",
                ActionType.VEC1));
        set.add(new XRBinding(null, "/actions/gui/in/vivecraft.key.guiRightClick", "/user/hand/right/input/a/click",
            ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/gui/in/vivecraft.key.guiScrollAxis", "/user/hand/right/input/thumbstick/y",
                ActionType.VEC1));

        set.add(
            new XRBinding(null, "/actions/ingame/in/vivecraft.key.hotbarPrev", "/user/hand/left/input/squeeze/value",
                ActionType.VEC1));
        set.add(
            new XRBinding(null, "/actions/ingame/in/vivecraft.key.hotbarNext", "/user/hand/right/input/squeeze/value",
                ActionType.VEC1));
        set.add(new XRBinding(null, "/actions/ingame/in/key.attack", "/user/hand/right/input/trigger/value",
            ActionType.VEC1));
        set.add(new XRBinding(null, "/actions/ingame/in/vivecraft.key.teleport", "/user/hand/left/input/trigger/value",
            ActionType.VEC1));
        set.add(new XRBinding(null, "/actions/ingame/in/vivecraft.key.radialMenu", "/user/hand/right/input/b/click",
            ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/ingame/in/key.use", "/user/hand/right/input/a/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/vivecraft.key.teleportFallback",
            "/user/hand/left/input/trigger/value", ActionType.VEC1));
        set.add(new XRBinding(null, "/actions/ingame/in/key.jump", "/user/hand/left/input/thumbstick/click",
            ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/ingame/in/vivecraft.key.freeMoveStrafe", "/user/hand/left/input/thumbstick",
                ActionType.VEC2));
        set.add(new XRBinding(null, "/actions/ingame/in/key.sneak", "/user/hand/right/input/thumbstick/click",
            ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/vivecraft.key.rotateAxis", "/user/hand/right/input/thumbstick",
            ActionType.VEC2));

        set.add(new XRBinding(null, "/actions/keyboard/in/vivecraft.key.keyboardShift",
            "/user/hand/left/input/squeeze/value", ActionType.VEC1));
        set.add(new XRBinding(null, "/actions/keyboard/in/vivecraft.key.keyboardShift",
            "/user/hand/right/input/squeeze/value", ActionType.VEC1));
        set.add(new XRBinding(null, "/actions/keyboard/in/vivecraft.key.keyboardClick",
            "/user/hand/left/input/trigger/value", ActionType.VEC1));
        set.add(new XRBinding(null, "/actions/keyboard/in/vivecraft.key.keyboardClick",
            "/user/hand/right/input/trigger/value", ActionType.VEC1));

        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.vrInteract",
            "/user/hand/left/input/squeeze/value", ActionType.VEC1));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.vrInteract",
            "/user/hand/right/input/squeeze/value", ActionType.VEC1));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.vrInteract",
            "/user/hand/left/input/trigger/value", ActionType.VEC1));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.vrInteract",
            "/user/hand/right/input/trigger/value", ActionType.VEC1));

        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.climbeyGrab",
            "/user/hand/left/input/squeeze/value", ActionType.VEC1));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.climbeyGrab",
            "/user/hand/right/input/squeeze/value", ActionType.VEC1));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.climbeyGrab",
            "/user/hand/left/input/trigger/value", ActionType.VEC1));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.climbeyGrab",
            "/user/hand/right/input/trigger/value", ActionType.VEC1));
        return set;
    }

    private static HashSet<XRBinding> viveBindings() {
        HashSet<XRBinding> set = new HashSet<>();

        set.add(
            new XRBinding(null, "/actions/global/in/vivecraft.key.ingameMenuButton", "/user/hand/left/input/menu/click",
                ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/global/in/key.inventory", "/user/hand/right/input/trackpad/click",
            ActionType.BOOLEAN));

        set.add(new XRBinding(null, "/actions/gui/in/vivecraft.key.guiShift", "/user/hand/left/input/squeeze/click",
            ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/gui/in/vivecraft.key.guiMiddleClick", "/user/hand/right/input/squeeze/click",
                ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/gui/in/vivecraft.key.guiLeftClick", "/user/hand/right/input/trigger/click",
                ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/gui/in/vivecraft.key.guiRightClick", "/user/hand/right/input/trackpad/click",
                ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/gui/in/vivecraft.key.guiScrollAxis", "/user/hand/right/input/trackpad/y",
            ActionType.VEC1));

        set.add(
            new XRBinding(null, "/actions/ingame/in/vivecraft.key.hotbarPrev", "/user/hand/left/input/squeeze/click",
                ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/ingame/in/vivecraft.key.hotbarNext", "/user/hand/right/input/squeeze/click",
                ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/key.attack", "/user/hand/right/input/trigger/click",
            ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/vivecraft.key.teleport", "/user/hand/left/input/trigger/click",
            ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/vivecraft.key.radialMenu", "/user/hand/right/input/menu/click",
            ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/key.use", "/user/hand/right/input/trackpad/click",
            ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/vivecraft.key.teleportFallback",
            "/user/hand/left/input/trigger/value", ActionType.VEC1));
        set.add(
            new XRBinding(null, "/actions/ingame/in/key.jump", "/user/hand/left/input/trackpad/x", ActionType.VEC1));
        set.add(new XRBinding(null, "/actions/ingame/in/vivecraft.key.freeMoveStrafe", "/user/hand/left/input/trackpad",
            ActionType.VEC2));
        set.add(
            new XRBinding(null, "/actions/ingame/in/key.sneak", "/user/hand/left/input/trackpad/y", ActionType.VEC1));
        set.add(new XRBinding(null, "/actions/ingame/in/vivecraft.key.rotateAxis", "/user/hand/right/input/trackpad",
            ActionType.VEC2));

        set.add(new XRBinding(null, "/actions/keyboard/in/vivecraft.key.keyboardShift",
            "/user/hand/left/input/squeeze/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/keyboard/in/vivecraft.key.keyboardClick",
            "/user/hand/left/input/trigger/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/keyboard/in/vivecraft.key.keyboardClick",
            "/user/hand/right/input/trigger/click", ActionType.BOOLEAN));

        set.add(new XRBinding(null, "/actions/technical/in/vivecraft.key.trackpadTouch",
            "/user/hand/left/input/trackpad/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/technical/in/vivecraft.key.trackpadTouch",
            "/user/hand/right/input/trackpad/touch", ActionType.BOOLEAN));

        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.vrInteract",
            "/user/hand/left/input/squeeze/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.vrInteract",
            "/user/hand/right/input/squeeze/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.vrInteract",
            "/user/hand/left/input/trigger/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.vrInteract",
            "/user/hand/right/input/trigger/click", ActionType.BOOLEAN));

        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.climbeyGrab",
            "/user/hand/left/input/squeeze/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.climbeyGrab",
            "/user/hand/right/input/squeeze/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.climbeyGrab",
            "/user/hand/left/input/trigger/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.climbeyGrab",
            "/user/hand/right/input/trigger/click", ActionType.BOOLEAN));

        return set;
    }

    private static HashSet<XRBinding> cosmosBindings() {
        HashSet<XRBinding> set = new HashSet<>();

        set.add(
            new XRBinding(null, "/actions/global/in/vivecraft.key.ingameMenuButton", "/user/hand/left/input/y/click",
                ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/global/in/key.inventory", "/user/hand/left/input/x/click",
            ActionType.BOOLEAN));

        set.add(new XRBinding(null, "/actions/gui/in/vivecraft.key.guiShift", "/user/hand/left/input/squeeze/click",
            ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/gui/in/vivecraft.key.guiMiddleClick", "/user/hand/right/input/squeeze/click",
                ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/gui/in/vivecraft.key.guiLeftClick", "/user/hand/right/input/trigger/click",
                ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/gui/in/vivecraft.key.guiRightClick", "/user/hand/right/input/a/click",
            ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/gui/in/vivecraft.key.guiScrollAxis", "/user/hand/right/input/thumbstick",
            ActionType.VEC2));

        set.add(
            new XRBinding(null, "/actions/ingame/in/vivecraft.key.hotbarPrev", "/user/hand/left/input/squeeze/click",
                ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/ingame/in/vivecraft.key.hotbarNext", "/user/hand/right/input/squeeze/click",
                ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/key.attack", "/user/hand/right/input/trigger/click",
            ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/vivecraft.key.teleport", "/user/hand/left/input/trigger/click",
            ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/vivecraft.key.radialMenu", "/user/hand/right/input/b/click",
            ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/ingame/in/key.use", "/user/hand/right/input/a/click", ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/ingame/in/vivecraft.key.freeMoveStrafe", "/user/hand/left/input/thumbstick",
                ActionType.VEC2));
        set.add(new XRBinding(null, "/actions/ingame/in/vivecraft.key.rotateAxis", "/user/hand/right/input/thumbstick",
            ActionType.VEC2));
        set.add(new XRBinding(null, "/actions/ingame/in/vivecraft.key.teleportFallback",
            "/user/hand/left/input/trigger/value", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/key.jump", "/user/hand/left/input/shoulder/click",
            ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/key.sneak", "/user/hand/right/input/shoulder/click",
            ActionType.BOOLEAN));

        set.add(new XRBinding(null, "/actions/keyboard/in/vivecraft.key.keyboardShift",
            "/user/hand/left/input/squeeze/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/keyboard/in/vivecraft.key.keyboardClick",
            "/user/hand/left/input/trigger/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/keyboard/in/vivecraft.key.keyboardClick",
            "/user/hand/right/input/trigger/click", ActionType.BOOLEAN));

        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.vrInteract",
            "/user/hand/left/input/squeeze/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.vrInteract",
            "/user/hand/right/input/squeeze/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.vrInteract",
            "/user/hand/left/input/trigger/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.vrInteract",
            "/user/hand/right/input/trigger/click", ActionType.BOOLEAN));

        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.climbeyGrab",
            "/user/hand/left/input/squeeze/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.climbeyGrab",
            "/user/hand/right/input/squeeze/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.climbeyGrab",
            "/user/hand/left/input/trigger/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.climbeyGrab",
            "/user/hand/right/input/trigger/click", ActionType.BOOLEAN));
        return set;
    }

    private static HashSet<XRBinding> picoBindings() {
        HashSet<XRBinding> set = new HashSet<>();

        set.add(
            new XRBinding(null, "/actions/global/in/vivecraft.key.ingameMenuButton", "/user/hand/left/input/y/click",
                ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/global/in/key.inventory", "/user/hand/left/input/x/click",
            ActionType.BOOLEAN));

        set.add(new XRBinding(null, "/actions/gui/in/vivecraft.key.guiShift", "/user/hand/left/input/squeeze/click",
            ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/gui/in/vivecraft.key.guiMiddleClick", "/user/hand/right/input/squeeze/click",
                ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/gui/in/vivecraft.key.guiLeftClick", "/user/hand/right/input/trigger/click",
                ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/gui/in/vivecraft.key.guiRightClick", "/user/hand/right/input/a/click",
            ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/gui/in/vivecraft.key.guiScrollAxis", "/user/hand/right/input/thumbstick/y",
                ActionType.VEC1));

        set.add(
            new XRBinding(null, "/actions/ingame/in/vivecraft.key.hotbarPrev", "/user/hand/left/input/squeeze/click",
                ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/ingame/in/vivecraft.key.hotbarNext", "/user/hand/right/input/squeeze/click",
                ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/key.attack", "/user/hand/right/input/trigger/click",
            ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/vivecraft.key.teleport", "/user/hand/left/input/trigger/click",
            ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/vivecraft.key.radialMenu", "/user/hand/right/input/b/click",
            ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/ingame/in/key.use", "/user/hand/right/input/a/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/vivecraft.key.teleportFallback",
            "/user/hand/left/input/trigger/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/key.jump", "/user/hand/left/input/thumbstick/click",
            ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/ingame/in/vivecraft.key.freeMoveStrafe", "/user/hand/left/input/thumbstick",
                ActionType.VEC2));
        set.add(new XRBinding(null, "/actions/ingame/in/key.sneak", "/user/hand/right/input/thumbstick/click",
            ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/vivecraft.key.rotateAxis", "/user/hand/right/input/thumbstick",
            ActionType.VEC2));

        set.add(new XRBinding(null, "/actions/keyboard/in/vivecraft.key.keyboardShift",
            "/user/hand/left/input/squeeze/click", ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/keyboard/in/vivecraft.key.keyboardShift",
                "/user/hand/right/input/squeeze/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/keyboard/in/vivecraft.key.keyboardClick",
            "/user/hand/left/input/trigger/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/keyboard/in/vivecraft.key.keyboardClick",
            "/user/hand/right/input/trigger/click", ActionType.BOOLEAN));

        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.vrInteract",
            "/user/hand/left/input/squeeze/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.vrInteract",
            "/user/hand/right/input/squeeze/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.vrInteract",
            "/user/hand/left/input/trigger/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.vrInteract",
            "/user/hand/right/input/trigger/click", ActionType.BOOLEAN));

        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.climbeyGrab",
            "/user/hand/left/input/squeeze/click", ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/contextual/in/vivecraft.key.climbeyGrab",
                "/user/hand/right/input/squeeze/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.climbeyGrab",
            "/user/hand/left/input/trigger/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.climbeyGrab",
            "/user/hand/right/input/trigger/click", ActionType.BOOLEAN));
        return set;
    }

    private static HashSet<XRBinding> indexBindings() {
        HashSet<XRBinding> set = new HashSet<>();

        set.add(
            new XRBinding(null, "/actions/global/in/vivecraft.key.ingameMenuButton", "/user/hand/left/input/b/click",
                ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/global/in/key.inventory", "/user/hand/left/input/a/click",
            ActionType.BOOLEAN));

        set.add(new XRBinding(null, "/actions/gui/in/vivecraft.key.guiShift", "/user/hand/left/input/squeeze/force",
            ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/gui/in/vivecraft.key.guiMiddleClick", "/user/hand/right/input/squeeze/force",
                ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/gui/in/vivecraft.key.guiLeftClick", "/user/hand/right/input/trigger/click",
                ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/gui/in/vivecraft.key.guiRightClick", "/user/hand/right/input/trackpad",
            ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/gui/in/vivecraft.key.guiScrollAxis", "/user/hand/right/input/thumbstick/y",
                ActionType.VEC1));
        set.add(new XRBinding(null, "/actions/gui/in/vivecraft.key.guiScrollAxis", "/user/hand/right/input/trackpad/y",
            ActionType.VEC1));

        set.add(
            new XRBinding(null, "/actions/ingame/in/vivecraft.key.hotbarPrev", "/user/hand/left/input/squeeze/force",
                ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/ingame/in/vivecraft.key.hotbarNext", "/user/hand/right/input/squeeze/force",
                ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/key.attack", "/user/hand/right/input/trigger/click",
            ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/vivecraft.key.teleport", "/user/hand/left/input/trigger/click",
            ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/vivecraft.key.radialMenu", "/user/hand/right/input/b/click",
            ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/key.use", "/user/hand/right/input/trackpad", ActionType.VEC2));
        set.add(new XRBinding(null, "/actions/ingame/in/vivecraft.key.teleportFallback",
            "/user/hand/left/input/trigger/value", ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/ingame/in/key.jump", "/user/hand/left/input/thumbstick", ActionType.VEC2));
        set.add(
            new XRBinding(null, "/actions/ingame/in/vivecraft.key.freeMoveStrafe", "/user/hand/left/input/thumbstick",
                ActionType.VEC2));
        set.add(
            new XRBinding(null, "/actions/ingame/in/key.sneak", "/user/hand/right/input/a/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/vivecraft.key.rotateAxis", "/user/hand/right/input/thumbstick",
            ActionType.VEC2));

        set.add(new XRBinding(null, "/actions/keyboard/in/vivecraft.key.keyboardShift",
            "/user/hand/left/input/squeeze/force", ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/keyboard/in/vivecraft.key.keyboardShift",
                "/user/hand/right/input/squeeze/force", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/keyboard/in/vivecraft.key.keyboardClick",
            "/user/hand/left/input/trigger/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/keyboard/in/vivecraft.key.keyboardClick",
            "/user/hand/right/input/trigger/click", ActionType.BOOLEAN));

        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.vrInteract",
            "/user/hand/left/input/squeeze/force", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.vrInteract",
            "/user/hand/right/input/squeeze/force", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.vrInteract",
            "/user/hand/left/input/trigger/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.vrInteract",
            "/user/hand/right/input/trigger/click", ActionType.BOOLEAN));

        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.climbeyGrab",
            "/user/hand/left/input/squeeze/force", ActionType.BOOLEAN));
        set.add(
            new XRBinding(null, "/actions/contextual/in/vivecraft.key.climbeyGrab",
                "/user/hand/right/input/squeeze/force", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.climbeyGrab",
            "/user/hand/left/input/trigger/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.climbeyGrab",
            "/user/hand/right/input/trigger/click", ActionType.BOOLEAN));
        return set;
    }

    private static HashSet<XRBinding> defaultBindings() {
        HashSet<XRBinding> set = new HashSet<>();

        set.add(
            new XRBinding(null, "/actions/global/in/vivecraft.key.ingameMenuButton", "/user/hand/left/input/menu/click",
                ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/gui/in/vivecraft.key.guiLeftClick", "/user/hand/right/input/select/click",
            ActionType.BOOLEAN));

        set.add(new XRBinding(null, "/actions/ingame/in/key.attack", "/user/hand/right/input/select/click",
            ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/vivecraft.key.teleport", "/user/hand/left/input/select/click",
            ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/ingame/in/vivecraft.key.teleportFallback",
            "/user/hand/left/input/select/click", ActionType.BOOLEAN));

        set.add(new XRBinding(null, "/actions/keyboard/in/vivecraft.key.keyboardClick",
            "/user/hand/left/input/select/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/keyboard/in/vivecraft.key.keyboardClick",
            "/user/hand/right/input/select/click", ActionType.BOOLEAN));

        set.add(
            new XRBinding(null, "/actions/contextual/in/vivecraft.key.vrInteract", "/user/hand/left/input/select/click",
                ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.vrInteract",
            "/user/hand/right/input/select/click", ActionType.BOOLEAN));

        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.climbeyGrab",
            "/user/hand/left/input/select/click", ActionType.BOOLEAN));
        set.add(new XRBinding(null, "/actions/contextual/in/vivecraft.key.climbeyGrab",
            "/user/hand/right/input/select/click", ActionType.BOOLEAN));

        return set;
    }

    public static HashSet<XRBinding> getBinding(String Headset) {
        switch (Headset) {
            case "/interaction_profiles/htc/vive_cosmos_controller" -> {
                return cosmosBindings();
            }
            case "/interaction_profiles/htc/vive_controller" -> {
                return viveBindings();
            }
            case "/interaction_profiles/valve/index_controller" -> {
                return indexBindings();
            }
            case "/interaction_profiles/oculus/touch_controller" -> {
                return quest2Bindings();
            }
            case "/interaction_profiles/bytedance/pico4_controller",
                 "/interaction_profiles/bytedance/pico_neo3_controller" -> {
                return picoBindings();
            }
            default -> {
                return defaultBindings();
            }
        }
    }
}
