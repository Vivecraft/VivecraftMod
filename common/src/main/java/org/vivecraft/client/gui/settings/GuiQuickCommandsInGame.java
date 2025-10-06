package org.vivecraft.client.gui.settings;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import org.vivecraft.client_vr.ClientDataHolderVR;

public class GuiQuickCommandsInGame extends Screen {
    protected ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
    protected final Screen parentScreen;

    public GuiQuickCommandsInGame(Screen lastScreen) {
        super(new TranslatableComponent("vivecraft.options.screen.quickcommands"));
        this.parentScreen = lastScreen;
    }

    @Override
    public void init() {
        KeyMapping.releaseAll();
        this.clearWidgets();
        String[] chatCommands = this.dataHolder.vrSettings.vrQuickCommands;

        for (int i = 0; i < chatCommands.length; i++) {
            int rightColumn = i > 5 ? 1 : 0;
            String command = chatCommands[i];
            this.addRenderableWidget(
                new Button(this.width / 2 - 125 + 127 * rightColumn, 36 + (i - 6 * rightColumn) * 24, 125, 20,
                    new TranslatableComponent(command),
                    (p) -> {
                        this.minecraft.setScreen(null);
                        this.minecraft.player.chat(p.getMessage().getString());
                    }
                ));
        }

        this.addRenderableWidget(new Button(this.width / 2 - 50, this.height - 46, 100, 20,
            new TranslatableComponent("gui.cancel"),
            (p) -> this.minecraft.setScreen(this.parentScreen)));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        drawCenteredString(poseStack, this.font, this.getTitle(), this.width / 2, 16, 0xFFFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }
}
