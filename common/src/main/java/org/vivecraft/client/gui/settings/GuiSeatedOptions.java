package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiSeatedOptions extends GuiVROptionsBase {
    private final VROptionEntry[] seatedOptions = new VROptionEntry[]{
        new VROptionEntry(VRSettings.VrOptions.X_SENSITIVITY),
        new VROptionEntry(VRSettings.VrOptions.Y_SENSITIVITY),
        new VROptionEntry(VRSettings.VrOptions.KEYHOLE),
        new VROptionEntry(VRSettings.VrOptions.SEATED_HUD_XHAIR),
        new VROptionEntry(VRSettings.VrOptions.WALK_UP_BLOCKS),
        new VROptionEntry(VRSettings.VrOptions.WORLD_ROTATION_INCREMENT),
        new VROptionEntry(VRSettings.VrOptions.VEHICLE_ROTATION),
        new VROptionEntry(VRSettings.VrOptions.REVERSE_HANDS),
        new VROptionEntry(VRSettings.VrOptions.SEATED_FREE_MOVE, true),
        new VROptionEntry(VRSettings.VrOptions.RIGHT_CLICK_DELAY, false),
        new VROptionEntry("vivecraft.options.screen.teleport.button", (button, mousePos) -> {
            this.minecraft.setScreen(new GuiTeleportSettings(this));
            return true;
        }),
        new VROptionEntry("vivecraft.options.screen.freemove.button", (button, mousePos) -> {
            this.minecraft.setScreen(new GuiFreeMoveSettings(this));
            return true;
        })
    };

    public GuiSeatedOptions(Screen guiScreen) {
        super(guiScreen);
    }

    public void init() {
        this.vrTitle = "vivecraft.options.screen.seated";
        super.init(this.seatedOptions, true);
        super.addDefaultButtons();
    }
}
