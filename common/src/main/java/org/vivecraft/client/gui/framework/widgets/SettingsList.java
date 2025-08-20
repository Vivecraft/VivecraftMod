package org.vivecraft.client.gui.framework.widgets;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Pair;
import org.vivecraft.client.utils.StringSimilarity;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.TooltipUtil;
import org.vivecraft.server.ServerNetworking;
import org.vivecraft.server.config.ConfigBuilder;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SettingsList extends ContainerObjectSelectionList<SettingsList.BaseEntry> {

    private final List<SettingsList.BaseEntry> allEntries;

    private String activeFilter = "";

    private final Screen parent;

    public SettingsList(Screen parent, Minecraft minecraft, List<SettingsList.BaseEntry> entries, boolean searchable) {
        // arguments are
        // width, height, Y position, entry height
        super(minecraft, parent.width, parent.height - (searchable ? 74 : 52), searchable ? 42 : 20, 20);

        this.parent = parent;
        entries = entries.stream().filter(Objects::nonNull).toList();
        this.allEntries = new ArrayList<>(entries);

        this.replaceEntriesFlatten(this.allEntries);
    }

    private void replaceEntriesFlatten(List<SettingsList.BaseEntry> entries) {
        this.replaceEntriesFlatten(entries.stream());
    }

    private void replaceEntriesFlatten(Stream<SettingsList.BaseEntry> entries) {
        this.replaceEntries(entries.flatMap(entry -> entry.getEntries().stream()).toList());
    }

    public String getActiveFilter() {
        return this.activeFilter;
    }

    public void filter(String filter) {
        if (ClientDataHolderVR.getInstance().vrSettings.useFuzzySearch) {
            this.fuzzyFilter(filter);
        } else {
            this.exactFilter(filter);
        }
    }

    /**
     * filters for the given phrase with an exact search
     *
     * @param filter String to filter for (case-insensitive)
     */
    private void exactFilter(String filter) {
        if (!filter.trim().equals(this.activeFilter)) {
            // scroll to the top, to not be in the void
            this.setScrollAmount(0);
            this.activeFilter = filter.trim();
            String lowerCase = filter.trim().toLowerCase();
            this.replaceEntriesFlatten(this.allEntries.stream().filter(entry -> entry.filter(lowerCase)));
        }
    }

    /**
     * filter for the given phrase with a fuzzy search, and sorts the entries based on relevancy
     *
     * @param filter String to search for (case-insensitive)
     */
    private void fuzzyFilter(String filter) {
        if (filter.trim().isEmpty()) {
            this.replaceEntriesFlatten(this.allEntries);
        } else if (!filter.trim().equals(this.activeFilter)) {
            // scroll to the top, to not be in the void
            this.setScrollAmount(0);
            this.activeFilter = filter.trim();
            String lowerCase = filter.trim().toLowerCase();
            List<BaseEntry> entries = this.allEntries.stream()
                .flatMap(child -> child.getEntries().stream())
                .filter(entry -> !(entry instanceof CategoryEntry))
                .map(entry -> Pair.of(entry.search(lowerCase), entry))
                .filter(pair -> pair.getLeft() > 0.7F)
                .sorted((a, b) -> b.getLeft().compareTo(a.getLeft()))
                .map(Pair::getRight).toList();

            this.replaceEntries(entries);
        }
    }

    /**
     * override because the vanilla implementation is buggy, and the faulty {@code getEntryAtPosition} method is final
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isValidClickButton(button)) {
            this.updateScrolling(mouseX, mouseY, button);
            if (this.isMouseOver(mouseX, mouseY)) {
                SettingsList.BaseEntry hovered = this.getEntryAtPositionFixed(mouseX, mouseY);
                if (hovered != null && hovered.mouseClicked(mouseX, mouseY, button)) {
                    if (this.getFocused() != hovered && this.getFocused() != null) {
                        // unselect old entry
                        this.getFocused().setFocused(null);
                    }

                    this.setFocused(hovered);
                    this.setDragging(true);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * fixed version of {@link AbstractSelectionList#getEntryAtPosition(double, double)}
     * just checks if the position is left of the scrollbar, instead of some weird left limit
     */
    private SettingsList.BaseEntry getEntryAtPositionFixed(double mouseX, double mouseY) {
        int listY = Mth.floor(mouseY - this.getY()) - this.headerHeight + (int) this.scrollAmount() - 4;
        int hoveredItem = listY / this.itemHeight;
        return mouseX < this.scrollBarX() && hoveredItem >= 0 && listY >= 0 &&
            hoveredItem < this.getItemCount() ? this.children().get(hoveredItem) : null;
    }

    public boolean isEntryVisible(SettingsList.BaseEntry entry) {
        int index = this.children().indexOf(entry);
        return this.getRowTop(index) < this.getBottom() && this.getRowBottom(index) > this.getY();
    }

    @Override
    protected int scrollBarX() {
        return this.getRowRight() + 2;
    }

    @Override
    public int getRowWidth() {
        return Math.min(this.parent.width - 30, 350);
    }

    @Override
    public int getRowLeft() {
        return this.getX() + this.width / 2 - this.getRowWidth() / 2 + 2;
    }

    public int getItemHeight() {
        return this.itemHeight;
    }

    // there to make it public
    public BaseEntry getHovered() {
        return super.getHovered();
    }

    public static BaseEntry ConfigToEntry(ConfigBuilder.ConfigValue<?> configValue) {
        BaseEntry entry = new ResettableEntry(
            Component.translatable("vivecraft.serverSettings." + configValue.getPath()), configValue);
        entry.setActive(Minecraft.getInstance().level == null || Minecraft.getInstance().isLocalServer());
        return entry;
    }

    public static BaseEntry vrOptionToEntry(VRSettings.VrOptions option) {
        ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
        String optionString = "vivecraft.options." + option.name();

        BaseEntry entry;
        if (dh.vrSettings.hasValue(option)) {
            // has a setting so can be reset
            entry = new ResettableEntry(Component.translatable(optionString), option);
        } else {
            entry = new WidgetEntry(Component.translatable(optionString),
                vrOptionToWidget(option, WidgetEntry.VALUE_BUTTON_WIDTH),
                () -> TooltipUtil.getClientConfigTooltip(option),
                null);
        }
        if (dh.vrSettings.overrides.hasSetting(option) &&
            dh.vrSettings.overrides.getSetting(option).isValueOverridden())
        {
            entry.setActive(false);
        }
        return entry;
    }

    private static AbstractWidget vrOptionToWidget(VRSettings.VrOptions option, int width) {
        AbstractWidget widget;
        ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
        if (option.getEnumFloat()) {
            // slider button
            widget = new GuiVROptionSlider(option.returnEnumOrdinal(),
                0, 0,
                width, 20,
                option, true);
        } else {
            // regular button
            widget = Button.builder(Component.literal(dh.vrSettings.getButtonDisplayString(option, true))
                    , button -> {
                        dh.vrSettings.setOptionValue(option);
                        button.setMessage(Component.literal(dh.vrSettings.getButtonDisplayString(option, true)));
                    })
                .size(width, 20)
                .build();
        }
        return widget;
    }

    /**
     * Entry that has a title and child entries
     */
    public static class GroupedEntry extends CategoryEntry {
        private final ArrayList<BaseEntry> allChildren = new ArrayList<>();
        private final ArrayList<BaseEntry> activeChildren = new ArrayList<>();

        public GroupedEntry(Component name) {
            super(name);
        }

        public void add(BaseEntry entry) {
            this.allChildren.add(entry);
            this.activeChildren.add(entry);
        }

        /**
         * Overridden to show all children when the title matches, or filter individual children
         *
         * @param filter filter String to check for
         * @return if the title or any child matched
         */
        @Override
        public boolean filter(String filter) {
            this.activeChildren.clear();
            if (super.filter(filter)) {
                this.activeChildren.addAll(this.allChildren);
            } else {
                this.activeChildren.addAll(this.allChildren.stream().filter(entry -> entry.filter(filter)).toList());
            }
            return !this.activeChildren.isEmpty();
        }

        @Override
        public List<BaseEntry> getEntries() {
            return Stream.concat(Stream.of(this),
                this.activeChildren.stream().flatMap(child -> child.getEntries().stream())).toList();
        }
    }

    /**
     * Entry that has just a centered title
     */
    public static class CategoryEntry extends BaseEntry {
        private final int width;

        public CategoryEntry(Component name) {
            super(name, null);
            this.width = Minecraft.getInstance().font.width(this.name);
        }

        @Override
        public void render(
            GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY,
            boolean hovering, float partialTick)
        {
            guiGraphics.drawString(Minecraft.getInstance().font, this.name,
                Minecraft.getInstance().screen.width / 2 - this.width / 2,
                top + height - Minecraft.getInstance().font.lineHeight - 1, this.textColor());
        }

        @Override
        @Nullable
        public ComponentPath nextFocusPath(FocusNavigationEvent event) {
            return null;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(new NarratableEntry() {
                @Override
                public NarratableEntry.NarrationPriority narrationPriority() {
                    return NarratableEntry.NarrationPriority.HOVERED;
                }

                @Override
                public void updateNarration(NarrationElementOutput narrationElementOutput) {
                    narrationElementOutput.add(NarratedElementType.TITLE, CategoryEntry.this.name);
                }
            });
        }
    }

    /**
     * Entry that has a clickable label and an optional widget
     */
    public static class SelectableEntry extends BaseEntry {
        private final AbstractWidget mainWidget;
        private final AbstractWidget optionalWidget;

        public SelectableEntry(Component name, AbstractWidget optionalWidget, Runnable callback) {
            super(name, null);
            this.mainWidget = new TextOnlyButton(name, button -> callback.run());
            this.optionalWidget = optionalWidget;
        }

        @Override
        public void render(
            GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY,
            boolean hovering, float partialTick)
        {
            super.render(guiGraphics, index, top, left, width, height, mouseX, mouseY, hovering, partialTick);
            this.mainWidget.setX(left);
            this.mainWidget.setY(top);
            this.mainWidget.setWidth(this.optionalWidget == null ? width : width - 10 - this.optionalWidget.getWidth());
            this.mainWidget.render(guiGraphics, mouseX, mouseY, partialTick);

            if (this.optionalWidget != null) {
                this.optionalWidget.setX(left + width - this.optionalWidget.getWidth());
                this.optionalWidget.setY(top);
                this.optionalWidget.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.optionalWidget != null ? ImmutableList.of(this.mainWidget, this.optionalWidget) :
                ImmutableList.of(this.mainWidget);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.optionalWidget != null ? ImmutableList.of(this.mainWidget, this.optionalWidget) :
                ImmutableList.of(this.mainWidget);
        }

        @Override
        public void setActive(boolean active) {
            this.mainWidget.active = active;
            if (this.optionalWidget != null) {
                this.optionalWidget.active = active;
            }
        }
    }

    /**
     * Entry that has just a widget and a reset button
     */
    public static class ResettableEntry extends WidgetEntry {
        public static final int VALUE_BUTTON_WIDTH = 125;

        private final Button resetButton;
        private final BooleanSupplier canReset;

        public ResettableEntry(Component name, ConfigBuilder.ConfigValue<?> configValue) {
            this(name, configValue.getWidget(VALUE_BUTTON_WIDTH, 20).get(),
                () -> TooltipUtil.getServerConfigTooltip(configValue.getPath(), true),
                null,
                () -> !configValue.isDefault(),
                () -> {
                    configValue.reset();
                    if (Minecraft.getInstance().hasSingleplayerServer()) {
                        configValue.onUpdate(Minecraft.getInstance().getSingleplayerServer());
                        ServerNetworking.sendUpdatePacketToAll(Minecraft.getInstance().getSingleplayerServer(),
                            configValue);
                    }
                    return configValue.getWidget(VALUE_BUTTON_WIDTH, 20).get();
                });
        }

        public ResettableEntry(Component name, VRSettings.VrOptions option) {
            this(name, vrOptionToWidget(option, VALUE_BUTTON_WIDTH), () -> TooltipUtil.getClientConfigTooltip(option),
                () -> {
                    VRSettings.ServerOverrides overrides = ClientDataHolderVR.getInstance().vrSettings.overrides;
                    return option.isChangeable() &&
                        !(overrides.hasSetting(option) && overrides.getSetting(option).isValueOverridden());
                },
                () -> !ClientDataHolderVR.getInstance().vrSettings.isDefault(option),
                () -> {
                    ClientDataHolderVR.getInstance().vrSettings.loadDefault(option);
                    return vrOptionToWidget(option, VALUE_BUTTON_WIDTH);
                });
        }

        private ResettableEntry(
            Component name, AbstractWidget widget, Supplier<String> tooltipSupplier, BooleanSupplier isActive,
            BooleanSupplier canReset, Supplier<AbstractWidget> resetAction)
        {
            super(name, widget, tooltipSupplier, isActive);
            this.canReset = canReset;
            this.resetButton = Button.builder(Component.literal("X"), button -> this.valueWidget = resetAction.get())
                .tooltip(Tooltip.create(Component.translatable("controls.reset")))
                .bounds(0, 0, 20, 20).build();
            // need to set the tooltip delay to -1, or the main tooltip flickers on button change
            this.resetButton.setTooltipDelay(Duration.ofMillis(-1));
        }

        @Override
        public void render(
            GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY,
            boolean hovering, float partialTick)
        {
            super.render(guiGraphics, index, top, left, width, height, mouseX, mouseY, hovering, partialTick);
            this.resetButton.setX(left + width - 20);
            this.resetButton.setY(top);
            this.resetButton.active = this.isActive() && this.valueWidget.active && this.canReset.getAsBoolean();
            this.resetButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.valueWidget, this.resetButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.valueWidget, this.resetButton);
        }

        @Override
        public void setActive(boolean active) {
            super.setActive(active);
            this.resetButton.active = active;
        }
    }

    /**
     * Entry that opens a new screen
     */
    public static class ScreenEntry extends WidgetEntry {
        public ScreenEntry(String langKey, Function<Screen, Screen> screenFunction) {
            super(Component.translatable(langKey), Button.builder(Component.translatable(langKey),
                        b -> Minecraft.getInstance().setScreen(screenFunction.apply(Minecraft.getInstance().screen)))
                    .size(WidgetEntry.VALUE_BUTTON_WIDTH, 20)
                    .build(),
                () -> I18n.exists(langKey + ".tooltip") ? I18n.get(langKey + ".tooltip") : "");
        }
    }

    /**
     * Entry that has just a widget
     */
    public static class WidgetEntry extends BaseEntry {
        public static final int VALUE_BUTTON_WIDTH = 145;

        protected final BooleanSupplier widgetActive;
        protected AbstractWidget valueWidget;

        public WidgetEntry(
            Component name, AbstractWidget valueWidget, Supplier<String> tooltipSupplier, BooleanSupplier widgetActive)
        {
            super(name, tooltipSupplier);
            this.valueWidget = valueWidget;
            if (widgetActive == null) {
                this.widgetActive = () -> true;
            } else {
                this.widgetActive = widgetActive;
            }
        }

        public WidgetEntry(Component name, AbstractWidget valueWidget, Supplier<String> tooltipSupplier) {
            this(name, valueWidget, tooltipSupplier, null);
        }

        public WidgetEntry(Component name, AbstractWidget valueWidget) {
            this(name, valueWidget, null, null);
        }

        @Override
        public void render(
            GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY,
            boolean hovering, float partialTick)
        {
            super.render(guiGraphics, index, top, left, width, height, mouseX, mouseY, hovering, partialTick);

            int textWidth = Minecraft.getInstance().font.width(this.name);
            int textY = top + height / 2 - Minecraft.getInstance().font.lineHeight / 2 + 2;
            if (textWidth < width - VALUE_BUTTON_WIDTH) {
                guiGraphics.drawString(Minecraft.getInstance().font, this.name, left,
                    textY, this.textColor());
            } else {
                AbstractWidget.renderScrollingString(guiGraphics, Minecraft.getInstance().font, this.name, left,
                    textY, left + width - VALUE_BUTTON_WIDTH - 5,
                    textY + Minecraft.getInstance().font.lineHeight - 1, this.textColor());
            }

            this.valueWidget.setX(left + width - VALUE_BUTTON_WIDTH);
            this.valueWidget.setY(top);
            this.valueWidget.active = this.widgetActive.getAsBoolean() && this.isActive();
            this.valueWidget.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.valueWidget);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.valueWidget);
        }

        @Override
        public void setActive(boolean active) {
            super.setActive(active);
            this.valueWidget.active = active;
        }
    }

    public static abstract class BaseEntry extends Entry<BaseEntry> {

        protected final Component name;
        private final Supplier<String> tooltip;
        private boolean active = true;

        public BaseEntry(Component name, Supplier<String> tooltipSupplier) {
            this.name = name;
            this.tooltip = tooltipSupplier == null ? () -> "" : tooltipSupplier;
        }

        @Override
        public void render(
            GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY,
            boolean hovering, float partialTick)
        {
            if (this.isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard() ||
                hovering && Minecraft.getInstance().getLastInputType().isMouse())
            {
                guiGraphics.fill(RenderType.guiOverlay(), left - 2, top, left + width, top + 20, 0x80000000);
            }
        }

        @Override
        public ComponentPath focusPathAtIndex(FocusNavigationEvent event, int index) {
            if (!this.isActive()) {
                return ComponentPath.leaf(this);
            }
            // try to focus any selectable widget to the left, not just the one in line
            ComponentPath componentPath = null;
            for (int i = Math.min(index, this.children().size() - 1); componentPath == null && i >= 0; i--) {
                componentPath = this.children().get(i).nextFocusPath(event);
            }
            return ComponentPath.path(this, componentPath);
        }

        protected int textColor() {
            return this.active ? 0xFFFFFFFF : 0xFFA0A0A0;
        }

        public boolean isActive() {
            return this.active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        /**
         * checks if the translated name or the lang key contains the given filter
         *
         * @param filter filter String to check for
         * @return if this entry matches the given filter
         */
        public boolean filter(String filter) {
            boolean match = false;
            if (this.name.getContents() instanceof TranslatableContents trans) {
                match = trans.getKey().toLowerCase().contains(filter);
            }
            return match || this.name.getString().toLowerCase().contains(filter);
        }

        /**
         * returns the relevancy of this entry based on the search string
         *
         * @param search search String to check for
         * @return int that specifies relevancy
         */
        public float search(String search) {
            float matchKey = 0F;
            if (this.name.getContents() instanceof TranslatableContents trans) {
                // make this lower to prefer lang key matches
                matchKey = StringSimilarity.partial_ratio(search, trans.getKey().toLowerCase()) * 0.9F;
            }
            float matchString = StringSimilarity.partial_ratio(search, this.name.getString().toLowerCase());
            return Math.max(matchString, matchKey);
        }

        /**
         * @return list of all entries in this entry
         */
        public List<BaseEntry> getEntries() {
            return ImmutableList.of(this);
        }

        public String getTooltip() {
            return this.tooltip.get();
        }
    }
}

