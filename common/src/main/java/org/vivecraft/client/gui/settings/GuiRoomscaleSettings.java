package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiRoomscaleSettings extends GuiVROptionsBase {
    private static final VRSettings.VrOptions[] roomscaleSettings = new VRSettings.VrOptions[]{
        VRSettings.VrOptions.WEAPON_COLLISION,
        VRSettings.VrOptions.REALISTIC_JUMP,
        VRSettings.VrOptions.REALISTIC_SNEAK,
        VRSettings.VrOptions.REALISTIC_CLIMB,
        VRSettings.VrOptions.REALISTIC_ROW,
        VRSettings.VrOptions.REALISTIC_SWIM,
        VRSettings.VrOptions.BOW_MODE,
        VRSettings.VrOptions.BACKPACK_SWITCH,
        VRSettings.VrOptions.ALLOW_CRAWLING,
        VRSettings.VrOptions.REALISTIC_DISMOUNT
    };

    public GuiRoomscaleSettings(Screen lastScreen) {
        super(lastScreen);
    }

    @Override
    public void init() {
        this.vrTitle = "vivecraft.options.screen.roomscale";
        super.init(roomscaleSettings, true);
        super.addDefaultButtons();
    }
}
