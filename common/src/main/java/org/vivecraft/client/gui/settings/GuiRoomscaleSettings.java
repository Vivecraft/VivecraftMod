package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client.gui.framework.screens.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiRoomscaleSettings extends GuiVROptionsBase {
    private final VROptionEntry[] roomScaleSettings = new VROptionEntry[]{
        new VROptionEntry("vivecraft.options.screen.weaponcollision.button", (button, mousePos) -> {
            this.minecraft.setScreen(new GuiWeaponCollisionSettings(this));
            return true;
        }),
        new VROptionEntry(VRSettings.VrOptions.REALISTIC_JUMP),
        new VROptionEntry(VRSettings.VrOptions.REALISTIC_SNEAK),
        new VROptionEntry(VRSettings.VrOptions.REALISTIC_CLIMB),
        new VROptionEntry(VRSettings.VrOptions.REALISTIC_ROW),
        new VROptionEntry(VRSettings.VrOptions.REALISTIC_SWIM),
        new VROptionEntry(VRSettings.VrOptions.BOW_MODE),
        new VROptionEntry(VRSettings.VrOptions.BACKPACK_SWITCH),
        new VROptionEntry(VRSettings.VrOptions.ALLOW_CRAWLING),
        new VROptionEntry(VRSettings.VrOptions.REALISTIC_DISMOUNT),
        new VROptionEntry(VRSettings.VrOptions.REALISTIC_BLOCK_INTERACT),
        new VROptionEntry(VRSettings.VrOptions.REALISTIC_ENTITY_INTERACT)
    };

    public GuiRoomscaleSettings(Screen lastScreen) {
        super(lastScreen);
    }

    @Override
    public void init() {
        this.vrTitle = "vivecraft.options.screen.roomscale";
        super.init(this.roomScaleSettings, true);
        super.addDefaultButtons();
    }
}
