package org.vivecraft.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.framework.screens.ChangeableParentScreen;
import org.vivecraft.client.gui.framework.widgets.TextScrollWidget;
import org.vivecraft.client_vr.ClientDataHolderVR;

public class ErrorScreen extends Screen implements ChangeableParentScreen {

    private final Component error;
    private Screen lastScreen;

    public ErrorScreen(Component title, Component error) {
        super(title);
        this.lastScreen = Minecraft.getInstance().screen;
        this.error = error;
    }

    @Override
    public void setParent(Screen parent) {
        this.lastScreen = parent;
    }

    @Override
    protected void init() {

        this.addRenderableWidget(
            new TextScrollWidget(this.width / 2 - 155, 30, 310, this.height - 30 - 36, this.error));

        this.addRenderableWidget(new Button.Builder(Component.translatable("gui.back"), (p) ->
            onClose())
            .pos(this.width / 2 + 5, this.height - 32)
            .size(150, 20)
            .build());
        this.addRenderableWidget(new Button.Builder(Component.translatable("chat.copy.click"), (p) ->
            Minecraft.getInstance().keyboardHandler.setClipboard(
                this.title.getString() + "\n" + this.error.getString()))
            .pos(this.width / 2 - 155, this.height - 32)
            .size(150, 20)
            .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, this.title, this.width / 2, 15, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        ClientDataHolderVR.getInstance().cachedScreen = null;
        this.minecraft.setScreen(this.lastScreen);
    }
}
