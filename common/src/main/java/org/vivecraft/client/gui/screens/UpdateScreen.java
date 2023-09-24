package org.vivecraft.client.gui.screens;

import org.vivecraft.client.gui.widgets.TextScrollWidget;
import org.vivecraft.client.utils.UpdateChecker;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button.Builder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.URISyntaxException;

import static org.vivecraft.client_vr.VRState.mc;


public class UpdateScreen extends Screen
{

    private final Screen lastScreen;

    public UpdateScreen()
    {
        super(Component.literal("New Update Available"));
        lastScreen = mc.screen;
    }

    @Override
    protected void init()
    {

        this.addRenderableWidget(new TextScrollWidget(
            this.width / 2 - 155,
            30,
            310,
            this.height - 30 - 60,
            UpdateChecker.changelog
        ));

        this.addRenderableWidget(new Builder(Component.literal("Download from Modrinth"), (p) -> {
            try {
                Util.getPlatform().openUri(new URI("https://modrinth.com/mod/vivecraft"));
            } catch (URISyntaxException ignored) {
            }
        })
                .pos(this.width / 2 - 155, this.height - 56)
                .size(150, 20)
                .build());

        this.addRenderableWidget(new Builder(Component.literal("Download from Curseforge"), (p) -> {
            try {
                Util.getPlatform().openUri(new URI("https://www.curseforge.com/minecraft/mc-mods/vivecraft"));
            } catch (URISyntaxException ignored) {
            }
        })
                .pos(this.width / 2 + 5, this.height - 56)
                .size(150, 20)
                .build());

        this.addRenderableWidget(new Builder(Component.translatable("gui.back"), (p) ->
                mc.setScreen(this.lastScreen))
                .pos(this.width / 2 - 75, this.height - 32)
                .size(150, 20)
                .build());
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int i, int j, float f)
    {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 16777215);
        super.render(guiGraphics, i, j, f);
    }

    @Override
    public void onClose()
    {
        mc.setScreen(lastScreen);
    }
}
