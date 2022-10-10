package org.vivecraft.gui.settings;

import org.vivecraft.gui.framework.GuiVROptionsBase;
import org.vivecraft.gui.framework.VROptionEntry;
import org.vivecraft.settings.VRSettings;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GuiVRControls extends GuiVROptionsBase
{
    private static VROptionEntry[] controlsSettings = new VROptionEntry[] {
            new VROptionEntry(VRSettings.VrOptions.DUMMY, true),
            new VROptionEntry(VRSettings.VrOptions.DUMMY, true),
            new VROptionEntry(VRSettings.VrOptions.DUMMY, true),
            new VROptionEntry(VRSettings.VrOptions.DUMMY, true),
            new VROptionEntry(VRSettings.VrOptions.DUMMY, true),
            new VROptionEntry(VRSettings.VrOptions.DUMMY, true),
            new VROptionEntry(VRSettings.VrOptions.REVERSE_HANDS),
            new VROptionEntry(VRSettings.VrOptions.RIGHT_CLICK_DELAY),
            new VROptionEntry(VRSettings.VrOptions.ALLOW_ADVANCED_BINDINGS),
            new VROptionEntry(VRSettings.VrOptions.THIRDPERSON_ITEMTRANSFORMS)
    };

    public GuiVRControls(Screen par1GuiScreen)
    {
        super(par1GuiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.controls";
        super.init(controlsSettings, true);
        super.addDefaultButtons();
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        drawCenteredString(pMatrixStack, this.minecraft.font, Component.translatable("vivecraft.messages.controls.1"), this.width / 2, this.height / 2 - 9 / 2 - 9 - 3, 16777215);
        drawCenteredString(pMatrixStack, this.minecraft.font, Component.translatable("vivecraft.messages.controls.2"), this.width / 2, this.height / 2 - 9 / 2, 16777215);
        drawCenteredString(pMatrixStack, this.minecraft.font, Component.translatable("vivecraft.messages.controls.3"), this.width / 2, this.height / 2 - 9 / 2 + 9 + 3, 16777215);
    }
}
