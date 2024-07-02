package org.vivecraft.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.vivecraft.client.gui.widgets.TextScrollWidget;

public class ErrorScreen extends Screen {

    private final Screen lastScreen;
    private final Component error;

    public ErrorScreen(Component title, Component error) {
        super(title);
        this.lastScreen = Minecraft.getInstance().screen;
        this.error = error;
    }

    @Override
    protected void init() {

        this.addRenderableWidget(new TextScrollWidget(this.width / 2 - 155, 30, 310, this.height - 30 - 36, this.error));

        this.addRenderableWidget(new Button.Builder(Component.translatable("gui.back"), (p) ->
            Minecraft.getInstance().setScreen(this.lastScreen))
            .pos(this.width / 2 + 5, this.height - 32)
            .size(150, 20)
            .build());
        this.addRenderableWidget(new Button.Builder(Component.translatable("chat.copy.click"), (p) ->
            Minecraft.getInstance().keyboardHandler.setClipboard(this.title.getString() + "\n" + this.error.getString()))
            .pos(this.width / 2 - 155, this.height - 32)
            .size(150, 20)
            .build());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
