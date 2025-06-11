package org.vivecraft.client.gui.framework.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.framework.TooltipRenderer;
import org.vivecraft.client.gui.framework.widgets.SettingsList;

import java.util.List;

public abstract class GuiListScreen extends Screen {

    protected final Screen lastScreen;

    protected SettingsList list;

    protected EditBox searchBox;
    protected boolean searchable;

    protected int lastSelected = -1;
    protected boolean reinit = false;

    public GuiListScreen(Component title, Screen lastScreen) {
        super(title);
        this.lastScreen = lastScreen;
        this.searchable = true;
    }

    @Override
    protected void rebuildWidgets() {
        // need to do this here, because rebuildWidgets clears the selection
        this.lastSelected = this.list != null ? this.list.children().indexOf(this.list.getSelected()) : -1;
        super.rebuildWidgets();
    }

    @Override
    protected void init() {
        clearWidgets();
        double scrollAmount = this.list != null ? this.list.scrollAmount() : 0.0D;
        String filter = this.list != null ? this.list.getActiveFilter() : "";

        this.list = new SettingsList(this, this.minecraft, getEntries(), this.searchable);

        if (this.searchable) {
            this.list.filter(filter);

            this.searchBox = new EditBox(this.minecraft.font, this.width / 2 - 150, 20, 300, 20,
                Component.translatable("vivecraft.options.screen.search"));
            this.searchBox.setHint(Component.translatable("vivecraft.options.screen.search")
                .withStyle(ChatFormatting.GRAY)
                .withStyle(ChatFormatting.ITALIC));
            this.searchBox.setValue(filter);
            this.searchBox.setResponder(search -> this.list.filter(search));
            this.addRenderableWidget(this.searchBox);
        } else {
            this.searchBox = null;
        }

        this.list.setSelectedIndex(this.lastSelected);
        this.list.setFocused(this.list.getSelected());
        this.list.setScrollAmount(scrollAmount);
        this.addRenderableWidget(this.list);
        this.addLowerButtons(this.height - 26);
    }

    /**
     * method to add buttons below the list
     *
     * @param top Y position of the buttons
     */
    protected void addLowerButtons(int top) {
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(this.lastScreen))
                .bounds(this.width / 2 - 100, top, 200, 20).build());
    }

    /**
     * @return entries that should be in the list of this screen
     */
    protected abstract List<SettingsList.BaseEntry> getEntries();

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.reinit) {
            init();
            this.reinit = false;
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);

        // render custom tooltip
        SettingsList.BaseEntry entry = null;
        if (this.minecraft.getLastInputType().isKeyboard() && this.list.getSelected() != null) {
            // render custom tooltip
            entry = this.list.getSelected();
        } else if (this.list.getHovered() != null) {
            entry = this.list.getHovered();
        }
        if (entry != null && this.deferredTooltipRendering == null) {
            TooltipRenderer.renderTooltip(guiGraphics, entry.getTooltip(),
                this.width / 2, this.list.getRowTop(this.list.children().indexOf(entry)), this.list.getItemHeight());
        }
    }
}
