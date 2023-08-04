package org.vivecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.vivecraft.client.gui.widgets.TextScrollWidget;


public class ErrorScreen extends Screen {

    private final Screen lastScreen;
    private final Component error;

    public ErrorScreen(String title, Component error) {
        super(Component.literal(title));
        lastScreen = Minecraft.getInstance().screen;
        this.error = error;
    }

    protected void init() {

        this.addRenderableWidget(new TextScrollWidget(this.width / 2 - 155, 30, 310, this.height - 30 - 36, error));

        this.addRenderableWidget(new Button.Builder(Component.translatable("gui.back"), (p) ->
                Minecraft.getInstance().setScreen(this.lastScreen))
                .pos(this.width / 2 + 5, this.height - 32)
                .size(150, 20)
                .build());
        this.addRenderableWidget(new Button.Builder(Component.translatable("chat.copy"), (p) ->
                Minecraft.getInstance().keyboardHandler.setClipboard(error.getString()))
                .pos(this.width / 2 - 155, this.height - 32)
                .size(150, 20)
                .build());
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 16777215);

        super.render(poseStack, i, j, f);
    }
}
