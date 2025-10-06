package org.vivecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.NotNull;
import org.vivecraft.client.gui.framework.screens.ChangeableParentScreen;
import org.vivecraft.client.gui.framework.widgets.TextScrollWidget;
import org.vivecraft.client_vr.ClientDataHolderVR;


public class GarbageCollectorScreen extends Screen implements ChangeableParentScreen {

    private static final String GUIDE_URL = "https://github.com/Vivecraft/VivecraftMod/wiki/Memory-and-GC-Setup";

    private final String currentGarbageCollector;
    private Screen lastScreen;
    private TextScrollWidget text;

    public GarbageCollectorScreen(String currentGarbageCollector) {
        super(new TranslatableComponent("vivecraft.messages.gctitle"));
        this.lastScreen = Minecraft.getInstance().screen;
        this.currentGarbageCollector = currentGarbageCollector;
    }

    @Override
    public void setParent(Screen parent) {
        this.lastScreen = parent;
    }

    @Override
    protected void init() {
        Component message = new TranslatableComponent("vivecraft.messages.gcinfo",
            new TextComponent(this.currentGarbageCollector).withStyle(s -> s.withColor(ChatFormatting.RED)),
            new TextComponent("ZGC"),
            new TextComponent(Integer.toString(6)),
            new TextComponent("-XX:+UseZGC").withStyle(s -> s
                .withColor(ChatFormatting.GOLD)
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "-XX:+UseZGC"))
                .withHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.copy.click")))),
            new TranslatableComponent("vivecraft.gui.openguide").withStyle(style -> style
                .withUnderlined(true)
                .withColor(ChatFormatting.GREEN)
                .withHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent(("chat.link.open"))))
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, GUIDE_URL))));
        this.text = this.addRenderableWidget(
            new TextScrollWidget(this.width / 2 - 155, 30, 310, this.height - 30 - 60, message));

        this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 56, 150, 20,
            new TranslatableComponent("vivecraft.gui.dontshowagain"), (p) -> {
            ClientDataHolderVR.getInstance().vrSettings.disableGarbageCollectorMessage = true;
            ClientDataHolderVR.getInstance().vrSettings.saveOptions();
            onClose();
        }));

        this.addRenderableWidget(new Button(this.width / 2 + 5, this.height - 56, 150, 20,
            new TranslatableComponent("vivecraft.gui.ok"), (p) ->
            onClose()));

        this.addRenderableWidget(new Button(this.width / 2 - 75, this.height - 32, 150, 20,
            new TranslatableComponent("vivecraft.gui.openguide"),
            button ->
                this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
                    if (bl) {
                        Util.getPlatform().openUri(GUIDE_URL);
                    }
                    this.minecraft.setScreen(this);
                }, GUIDE_URL, true))
        ));
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 0xFFFFFFFF);

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
