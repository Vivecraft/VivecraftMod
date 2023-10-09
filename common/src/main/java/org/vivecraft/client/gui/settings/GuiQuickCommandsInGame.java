package org.vivecraft.client.gui.settings;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button.Builder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client_vr.ClientDataHolderVR;

public class GuiQuickCommandsInGame extends Screen {
    protected final Screen parentScreen;

    public GuiQuickCommandsInGame(Screen parent) {
        super(Component.literal(""));
        this.parentScreen = parent;
    }

    @Override
    public void init() {
        KeyMapping.releaseAll();
        this.clearWidgets();
        String[] astring = ClientDataHolderVR.getInstance().vrSettings.vrQuickCommands;
        int i;

        for (int j = 0; j < astring.length; ++j) {
            i = j > 5 ? 1 : 0;
            String s = astring[j];
            this.addRenderableWidget(new Builder(Component.translatable(s), (p) ->
                {
                    Minecraft.getInstance().setScreen(null);
                    if (p.getMessage().getString().startsWith("/")) {
                        Minecraft.getInstance().player.connection.sendCommand(p.getMessage().getString().substring(1));
                    } else {
                        Minecraft.getInstance().player.connection.sendChat(p.getMessage().getString());
                    }
                })
                    .size(125, 20)
                    .pos(this.width / 2 - 125 + 127 * i, 36 + (j - 6 * i) * 24)
                    .build()
            );
        }

        this.addRenderableWidget(new Builder(Component.translatable("gui.cancel"), (p) -> Minecraft.getInstance().setScreen(this.parentScreen))
            .size(100, 20)
            .pos(this.width / 2 - 50, this.height - 46)
            .build()
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        this.renderBackground(guiGraphics, pMouseX, pMouseY, pPartialTicks);
        guiGraphics.drawCenteredString(this.font, "Quick Commands", this.width / 2, 16, 16777215);
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
    }
}
