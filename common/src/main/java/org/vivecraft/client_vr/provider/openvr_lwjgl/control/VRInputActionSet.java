package org.vivecraft.client_vr.provider.openvr_lwjgl.control;

import net.minecraft.client.KeyMapping;
import org.vivecraft.client.VivecraftVRMod;

public enum VRInputActionSet {
    INGAME("/actions/ingame", "vivecraft.actionset.ingame", "leftright", false),
    GUI("/actions/gui", "vivecraft.actionset.gui", "leftright", false),
    GLOBAL("/actions/global", "vivecraft.actionset.global", "leftright", false),
    MOD("/actions/mod", "vivecraft.actionset.mod", "leftright", false),
    CONTEXTUAL("/actions/contextual", "vivecraft.actionset.contextual", "single", false),
    KEYBOARD("/actions/keyboard", "vivecraft.actionset.keyboard", "single", true),
    MIXED_REALITY("/actions/mixedreality", "vivecraft.actionset.mixedReality", "single", true),
    TECHNICAL("/actions/technical", "vivecraft.actionset.technical", "leftright", true);

    public final String name;
    public final String localizedName;
    public final String usage;
    public final boolean advanced;

    VRInputActionSet(String name, String localizedName, String usage, boolean advanced) {
        this.name = name;
        this.localizedName = localizedName;
        this.usage = usage;
        this.advanced = advanced;
    }

    public static VRInputActionSet fromKeyBinding(KeyMapping keyBinding) {
        String s = keyBinding.getCategory();

        switch (s) {
            case "vivecraft.key.category.gui":
                return GUI;

            case "vivecraft.key.category.climbey":
                return CONTEXTUAL;

            case "vivecraft.key.category.keyboard":
                return KEYBOARD;

            default:
                return VivecraftVRMod.INSTANCE.isModBinding(keyBinding) ? MOD : INGAME;
        }
    }
}
