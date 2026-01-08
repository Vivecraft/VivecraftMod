package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.screens.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiWeaponCollisionSettings extends GuiVROptionsBase {
    private static final VRSettings.VrOptions[] WEAPON_COLLISION_SETTINGS = new VRSettings.VrOptions[]{
        VRSettings.VrOptions.WEAPON_COLLISION,
        VRSettings.VrOptions.FEET_COLLISION,
        VRSettings.VrOptions.REALISTIC_OPENING,
        VRSettings.VrOptions.SWORD_BLOCK_COLLISION,
        VRSettings.VrOptions.ONLY_SWORD_COLLISION,
        VRSettings.VrOptions.REDUCED_PLAYER_REACH
    };

    public GuiWeaponCollisionSettings(Screen lastScreen) {
        super(lastScreen);
    }

    @Override
    public void init() {
        this.vrTitle = "vivecraft.options.screen.weaponcollision";
        super.init(WEAPON_COLLISION_SETTINGS, true);
        super.addDefaultButtons();
    }
}
