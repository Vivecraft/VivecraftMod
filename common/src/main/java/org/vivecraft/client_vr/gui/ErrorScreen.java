package org.vivecraft.client_vr.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class ErrorScreen extends Screen {

    private final Screen lastScreen;
    private final Component error;

    public ErrorScreen(String title, Component error) {
        super(Component.literal(title));
        lastScreen = Minecraft.getInstance().screen;
        this.error = error;
    }

    protected void init() {

        this.addRenderableWidget(new Button.Builder(Component.translatable("gui.back"), (p) ->
                Minecraft.getInstance().setScreen(this.lastScreen))
                .pos(this.width / 2 + 5, this.height / 6 + 168)
                .size(150, 20)
                .build());
        this.addRenderableWidget(new Button.Builder(Component.translatable("chat.copy"), (p) ->
                Minecraft.getInstance().keyboardHandler.setClipboard(error.getString()))
                .pos(this.width / 2 - 155, this.height / 6 + 168)
                .size(150, 20)
                .build());
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 16777215);

        fill(poseStack, this.width / 2 - 155, 30, this.width / 2 + 155, this.height / 6 + 168 - 8, -6250336);
        fill(poseStack, this.width / 2 - 155 + 1, 30 + 1, this.width / 2 + 155 - 1, this.height / 6 + 168 - 8 - 1, -16777216);

        List<FormattedText> formattedText = font.getSplitter().splitLines(error, 300, Style.EMPTY);
        for (int line = 0; line < formattedText.size(); line++) {
            drawString(poseStack, this.font, formattedText.get(line).getString(), this.width / 2 - 150, 35 + line * 12, 16777215);
        }


        super.render(poseStack, i, j, f);
    }
}
