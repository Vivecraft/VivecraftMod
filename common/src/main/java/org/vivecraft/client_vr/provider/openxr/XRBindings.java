package org.vivecraft.client_vr.provider.openxr;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.vivecraft.client_vr.provider.control.VRInputActionSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class XRBindings {

    public static HashSet<String> supportedHeadsets() {
        HashSet<String> set = new HashSet<>();
        if (MCOpenXR.get().session.getCapabilities().XR_HTC_vive_cosmos_controller_interaction) {
            set.add("/interaction_profiles/htc/vive_cosmos_controller");
        }
        set.add("/interaction_profiles/htc/vive_controller");

        return set;
    }

    private static HashSet<Pair<String, String>> viveBindings() {
        HashSet<Pair<String, String>> set = new HashSet<>();

        set.add(new MutablePair<>("/actions/global/in/vivecraft.key.ingameMenuButton", "/user/hand/left/input/menu/click"));
        set.add(new MutablePair<>("/actions/global/in/vivecraft.key.ingameMenuButton", "/user/hand/left/input/menu/click"));
        set.add(new MutablePair<>("/actions/global/in/vivecraft.key.ingameMenuButton", "/user/hand/left/input/menu/click"));
        set.add(new MutablePair<>("/actions/global/in/vivecraft.key.toggleKeyboard", "/user/hand/left/input/squeeze/click"));
        set.add(new MutablePair<>("/actions/global/in/key.inventory", "/user/hand/right/input/trackpad/click"));

        set.add(new MutablePair<>("/actions/gui/in/vivecraft.key.guiShift", "/user/hand/left/input/squeeze/click"));
        set.add(new MutablePair<>("/actions/gui/in/vivecraft.key.guiMiddleClick", "/user/hand/right/input/squeeze/click"));
        set.add(new MutablePair<>("/actions/gui/in/vivecraft.key.guiLeftClick", "/user/hand/right/input/trigger/click"));
        set.add(new MutablePair<>("/actions/gui/in/vivecraft.key.guiRightClick", "/user/hand/right/input/trackpad/click"));
        set.add(new MutablePair<>("/actions/gui/in/vivecraft.key.guiScrollAxis", "/user/hand/right/input/trackpad/y"));

        set.add(new MutablePair<>("/actions/ingame/in/vivecraft.key.hotbarPrev", "/user/hand/left/input/squeeze/click"));
        set.add(new MutablePair<>("/actions/ingame/in/vivecraft.key.hotbarNext", "/user/hand/right/input/squeeze/click"));
        set.add(new MutablePair<>("/actions/ingame/in/key.attack", "/user/hand/right/input/trigger/click"));
        set.add(new MutablePair<>("/actions/ingame/in/vivecraft.key.teleport", "/user/hand/left/input/trigger/click"));
        set.add(new MutablePair<>("/actions/ingame/in/vivecraft.key.radialMenu", "/user/hand/right/input/menu/click"));
        set.add(new MutablePair<>("/actions/ingame/in/key.use", "/user/hand/right/input/trackpad/click"));
        set.add(new MutablePair<>("/actions/ingame/in/vivecraft.key.teleportFallback", "/user/hand/left/input/trigger/value"));
        set.add(new MutablePair<>("/actions/ingame/in/key.jump", "/user/hand/left/input/trackpad/x"));
        set.add(new MutablePair<>("/actions/ingame/in/key.sneak", "/user/hand/left/input/trackpad/y"));

        set.add(new MutablePair<>("/actions/keyboard/in/vivecraft.key.keyboardShift", "/user/hand/left/input/squeeze/click"));
        set.add(new MutablePair<>("/actions/keyboard/in/vivecraft.key.keyboardClick", "/user/hand/left/input/trigger/click"));
        set.add(new MutablePair<>("/actions/keyboard/in/vivecraft.key.keyboardClick", "/user/hand/right/input/trigger/click"));

        set.add(new MutablePair<>("/actions/technical/in/vivecraft.key.trackpadTouch", "/user/hand/left/input/trackpad/click"));
        set.add(new MutablePair<>("/actions/technical/in/vivecraft.key.trackpadTouch", "/user/hand/right/input/trackpad/touch"));

        return set;
    }

    private static HashSet<Pair<String, String>> cosmosBindings() {
        HashSet<Pair<String, String>> set = new HashSet<>();

        set.add(new MutablePair<>("/actions/global/in/vivecraft.key.ingamemenubutton", "/user/hand/left/input/y/click"));
        set.add(new MutablePair<>("/actions/global/in/vivecraft.key.togglekeyboard", "/user/hand/left/input/y/long"));
        set.add(new MutablePair<>("/actions/global/in/key.inventory", "/user/hand/left/input/x/click"));

        set.add(new MutablePair<>("/actions/gui/in/vivecraft.key.guishift", "/user/hand/left/input/grip/click"));
        set.add(new MutablePair<>("/actions/gui/in/vivecraft.key.guimiddleclick", "/user/hand/right/input/grip/click"));
        set.add(new MutablePair<>("/actions/gui/in/vivecraft.key.guileftclick", "/user/hand/right/input/trigger/click"));
        set.add(new MutablePair<>("/actions/gui/in/vivecraft.key.guirightclick", "/user/hand/right/input/a/click"));
        set.add(new MutablePair<>("/actions/gui/in/vivecraft.key.guiscrollaxis", "/user/hand/right/input/joystick/scroll"));

        set.add(new MutablePair<>("/actions/ingame/in/vivecraft.key.hotbarprev", "/user/hand/left/input/grip/click"));
        set.add(new MutablePair<>("/actions/ingame/in/vivecraft.key.hotbarnext", "/user/hand/right/input/grip/click"));
        set.add(new MutablePair<>("/actions/ingame/in/key.attack", "/user/hand/right/input/trigger/click"));
        set.add(new MutablePair<>("/actions/ingame/in/vivecraft.key.teleport", "/user/hand/left/input/trigger/click"));
        set.add(new MutablePair<>("/actions/ingame/in/vivecraft.key.radialmenu", "/user/hand/right/input/b/click"));
        set.add(new MutablePair<>("/actions/ingame/in/key.use", "/user/hand/right/input/a/click"));
        set.add(new MutablePair<>("/actions/ingame/in/vivecraft.key.freemovestrafe", "/user/hand/left/input/joystick/position"));
        set.add(new MutablePair<>("/actions/ingame/in/vivecraft.key.rotateaxis", "/user/hand/right/input/joystick/position"));
        set.add(new MutablePair<>("/actions/ingame/in/vivecraft.key.teleportfallback", "/user/hand/left/input/trigger/pull"));
        set.add(new MutablePair<>("/actions/ingame/in/key.jump", "/user/hand/left/input/bumper/click"));
        set.add(new MutablePair<>("/actions/ingame/in/key.sneak", "/user/hand/right/input/bumper/click"));

        set.add(new MutablePair<>("/actions/keyboard/in/vivecraft.key.keyboardshift", "/user/hand/left/input/grip/click"));
        set.add(new MutablePair<>("/actions/keyboard/in/vivecraft.key.keyboardclick", "/user/hand/left/input/trigger/click"));
        set.add(new MutablePair<>("/actions/keyboard/in/vivecraft.key.keyboardclick", "/user/hand/right/input/trigger/click"));
        return set;
    }

    public static HashSet<Pair<String, String>> getBinding(String Headset){
        switch (Headset) {
            case "/interaction_profiles/htc/vive_cosmos_controller" -> {
                return cosmosBindings();
            }
            case "/interaction_profiles/htc/vive_controller" -> {
                return viveBindings();
            }
            default -> {
                return viveBindings();
            }
        }
    }
}
