package org.vivecraft.gui.settings;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

public class GuiQuickCommandsInGame extends Screen
{
    private int field_146445_a;
    private int field_146444_f;
    private static final String __OBFID = "CL_00000703";
    protected final Screen parentScreen;

    public GuiQuickCommandsInGame(Screen parent)
    {
        super(new TextComponent(""));
        this.parentScreen = parent;
    }

    public void init()
    {
        KeyMapping.releaseAll();
        this.field_146445_a = 0;
        this.clearWidgets();
        byte b0 = -16;
        boolean flag = true;
        String[] astring = this.minecraft.vrSettings.vrQuickCommands;
        int i = 0;

        for (int j = 0; j < astring.length; ++j)
        {
            i = j > 5 ? 1 : 0;
            String s = astring[j];
            this.addRenderableWidget(new Button(this.width / 2 - 125 + 127 * i, 36 + (j - 6 * i) * 24, 125, 20, s.toString(), (p) ->
            {
                this.minecraft.setScreen((Screen)null);
                this.minecraft.player.chat(p.getMessage().getString());
            }));
        }

        this.addRenderableWidget(new Button(this.width / 2 - 50, this.height - 30 + b0, 100, 20, "Cancel", (p) ->
        {
            this.minecraft.setScreen(this.parentScreen);
        }));
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        drawCenteredString(pMatrixStack, this.font, "Quick Commands", this.width / 2, 16, 16777215);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }
}
