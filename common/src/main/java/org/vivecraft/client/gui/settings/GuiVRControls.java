package org.vivecraft.client.gui.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

import javax.annotation.Nonnull;

import static org.vivecraft.client.gui.framework.VROptionPosition.POS_CENTER;

public class GuiVRControls extends org.vivecraft.client.gui.framework.GuiVROptionsBase {
    public static String vrTitle = "vivecraft.options.screen.controls";

    public GuiVRControls(Screen par1GuiScreen) {
        super(par1GuiScreen);
    }

    @Override
    public void init() {
        super.clearWidgets();
        super.init(VrOptions.DUMMY, POS_CENTER);
        super.init(VrOptions.DUMMY, POS_CENTER);
        super.init(VrOptions.DUMMY, POS_CENTER);
        super.init(VrOptions.DUMMY, POS_CENTER);
        super.init(VrOptions.DUMMY, POS_CENTER);
        super.init(VrOptions.DUMMY, POS_CENTER);
        super.init(
            VrOptions.REVERSE_HANDS,
            VrOptions.RIGHT_CLICK_DELAY,
            VrOptions.ALLOW_ADVANCED_BINDINGS,
            VrOptions.THIRDPERSON_ITEMTRANSFORMS
        );
        super.addDefaultButtons();
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int middle = 240 / 2 - Minecraft.getInstance().font.lineHeight;
        int lineHeight = Minecraft.getInstance().font.lineHeight + 3;

        guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable("vivecraft.messages.controls.1"), this.width / 2, middle - lineHeight, 16777215);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable("vivecraft.messages.controls.2"), this.width / 2, middle, 16777215);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable("vivecraft.messages.controls.3"), this.width / 2, middle + lineHeight, 16777215);
    }
}
