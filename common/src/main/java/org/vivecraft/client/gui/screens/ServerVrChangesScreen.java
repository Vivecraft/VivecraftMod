package org.vivecraft.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.vivecraft.client.gui.framework.widgets.TextScrollWidget;
import org.vivecraft.client.utils.LangHelper;

import java.util.Map;

public class ServerVrChangesScreen extends Screen {

    private final String changes;
    private final Screen lastScreen;

    public ServerVrChangesScreen(Map<String, String> changes) {
        super(Component.translatable("vivecraft.messages.nondefaultvrchanges.title"));
        this.lastScreen = Minecraft.getInstance().screen;
        StringBuilder builder = new StringBuilder();
        changes.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            builder.append(I18n.get("vivecraft.serverSettings." + entry.getKey())).append(": §c");
            if ("true".equals(entry.getValue()) || "false".equals(entry.getValue())) {
                builder.append(
                    I18n.get(Boolean.parseBoolean(entry.getValue()) ? LangHelper.ON_KEY : LangHelper.OFF_KEY));
            } else {
                builder.append(entry.getValue());
            }
            builder.append("§r\n");
        });
        this.changes = builder.toString();
    }

    @Override
    protected void init() {

        this.addRenderableWidget(
            new TextScrollWidget(this.width / 2 - 155, 30, 310, this.height - 30 - 36, this.changes));

        this.addRenderableWidget(new Button.Builder(Component.translatable("vivecraft.gui.ok"), (p) -> onClose())
            .pos(this.width / 2 - 75, this.height - 32)
            .size(150, 20)
            .build());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }
}
