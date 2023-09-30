package org.vivecraft.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.vivecraft.client.gui.widgets.TextScrollWidget;
import org.vivecraft.client.utils.UpdateChecker;


public class UpdateScreen extends Screen {

    private final Screen lastScreen;

    public UpdateScreen() {
        super(Component.literal("New Update Available"));
        lastScreen = Minecraft.getInstance().screen;
    }

    protected void init() {

        this.addRenderableWidget(new TextScrollWidget(this.width / 2 - 155, 30, 310, this.height - 30 - 60, UpdateChecker.changelog));

        this.addRenderableWidget(new Button.Builder(Component.literal("Download from Modrinth"),
            ConfirmLinkScreen.confirmLink("https://modrinth.com/mod/vivecraft", this, true))
            .pos(this.width / 2 - 155, this.height - 56)
            .size(150, 20)
            .build());

        this.addRenderableWidget(new Button.Builder(Component.literal("Download from Curseforge"),
            ConfirmLinkScreen.confirmLink("https://www.curseforge.com/minecraft/mc-mods/vivecraft", this, true))
            .pos(this.width / 2 + 5, this.height - 56)
            .size(150, 20)
            .build());

        this.addRenderableWidget(new Button.Builder(Component.translatable("gui.back"), (p) ->
            Minecraft.getInstance().setScreen(this.lastScreen))
            .pos(this.width / 2 - 75, this.height - 32)
            .size(150, 20)
            .build());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderBackground(guiGraphics, i, j, f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 16777215);
        super.render(guiGraphics, i, j, f);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastScreen);
    }
}
