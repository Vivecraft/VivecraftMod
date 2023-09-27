package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiVRControls extends GuiVROptionsBase {
    private static final VROptionEntry[] controlsSettings = new VROptionEntry[]{
        new VROptionEntry(VRSettings.VrOptions.DUMMY, true),
        new VROptionEntry(VRSettings.VrOptions.DUMMY, true),
        new VROptionEntry(VRSettings.VrOptions.DUMMY, true),
        new VROptionEntry(VRSettings.VrOptions.DUMMY, true),
        new VROptionEntry(VRSettings.VrOptions.DUMMY, true),
        new VROptionEntry(VRSettings.VrOptions.DUMMY),
        new VROptionEntry(VRSettings.VrOptions.INGAME_BINDINGS_IN_GUI),
        new VROptionEntry(VRSettings.VrOptions.REVERSE_HANDS),
        new VROptionEntry(VRSettings.VrOptions.RIGHT_CLICK_DELAY),
        new VROptionEntry(VRSettings.VrOptions.ALLOW_ADVANCED_BINDINGS),
        new VROptionEntry(VRSettings.VrOptions.THIRDPERSON_ITEMTRANSFORMS)
    };

    public GuiVRControls(Screen par1GuiScreen) {
        super(par1GuiScreen);
    }

    public void init() {
        this.vrTitle = "vivecraft.options.screen.controls";
        super.init(controlsSettings, true);
        super.addDefaultButtons();
    }

    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);

        int middle = 240 / 2 - this.minecraft.font.lineHeight / 2 - 24;
        int lineHeight = this.minecraft.font.lineHeight + 3;

        guiGraphics.drawCenteredString(this.minecraft.font, Component.translatable("vivecraft.messages.controls.1"), this.width / 2, middle - lineHeight, 16777215);
        guiGraphics.drawCenteredString(this.minecraft.font, Component.translatable("vivecraft.messages.controls.2"), this.width / 2, middle, 16777215);
        guiGraphics.drawCenteredString(this.minecraft.font, Component.translatable("vivecraft.messages.controls.3"), this.width / 2, middle + lineHeight, 16777215);
    }
}
