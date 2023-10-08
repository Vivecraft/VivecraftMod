package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.widgets.SettingsList;
import org.vivecraft.client.gui.widgets.SettingsList.BaseEntry;

import java.util.List;

import static org.vivecraft.client_vr.VRState.mc;

public abstract class GuiListScreen extends Screen {

    protected final Screen lastScreen;

    protected SettingsList list;

    protected boolean reinit = false;

    public GuiListScreen(Component title, Screen lastScreen) {
        super(title);
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        clearWidgets();
        double scrollAmount = list != null ? list.getScrollAmount() : 0.0D;

        this.list = new SettingsList(this, getEntries());
        list.setScrollAmount(scrollAmount);
        this.addWidget(this.list);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> mc.setScreen(this.lastScreen)).bounds(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    protected abstract List<BaseEntry> getEntries();

    @Override
    public void onClose() {
        mc.setScreen(lastScreen);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderDirtBackground(guiGraphics);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        if (reinit) {
            init();
            reinit = false;
        }
        super.render(guiGraphics, i, j, f);
        list.render(guiGraphics, i, j, f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
    }
}
