package org.vivecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import org.vivecraft.client.gui.framework.screens.ChangeableParentScreen;
import org.vivecraft.client.gui.framework.widgets.TextScrollWidget;
import org.vivecraft.client_vr.ClientDataHolderVR;

public class ErrorScreen extends Screen implements ChangeableParentScreen {

    private final Component error;
    private Screen lastScreen;
    private TextScrollWidget text;

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

        this.text = this.addRenderableWidget(
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
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFFFF);

        Style style = this.text.getMouseoverStyle(mouseX, mouseY);
        if (style != null) {
            renderComponentHoverEffect(poseStack, style, mouseX, mouseY);
        }
    }

    @Override
    public void onClose() {
        ClientDataHolderVR.getInstance().cachedScreen = null;
        this.minecraft.setScreen(this.lastScreen);
    }
}
