package org.vivecraft.client.gui.widgets;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.client.gui.settings.GuiListValueEditScreen;
import org.vivecraft.server.config.ConfigBuilder;

public class SettingsList extends ContainerObjectSelectionList<SettingsList.BaseEntry> {
    final Screen parent;
    int maxNameWidth;

    public SettingsList(Screen parent, Minecraft minecraft, List<SettingsList.BaseEntry> entries) {
        super(minecraft, parent.width + 45, parent.height, 20, parent.height - 32, 20);
        this.parent = parent;
        for (SettingsList.BaseEntry entry : entries) {
            int i;
            if ((i = minecraft.font.width(entry.name)) > this.maxNameWidth) {
                this.maxNameWidth = i;
            }
            this.addEntry(entry);
        }
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 8;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 32;
    }

    public static BaseEntry ConfigToEntry(ConfigBuilder.ConfigValue configValue, Component name) {
        AbstractWidget widget;
        if (configValue instanceof ConfigBuilder.NumberValue<?> numberValue) {
            widget = new AbstractSliderButton(0, 0, ResettableEntry.valueButtonWidth, 20, Component.literal("" + numberValue.get()), numberValue.normalize()) {
                @Override
                protected void updateMessage() {
                    setMessage(Component.literal("" + numberValue.get()));
                }

                @Override
                protected void applyValue() {
                    numberValue.fromNormalized(value);
                }
            };
        } else if (configValue instanceof ConfigBuilder.StringValue stringValue) {
            EditBox box = new EditBox(Minecraft.getInstance().font, 0, 0, ResettableEntry.valueButtonWidth - 1, 20, Component.literal(stringValue.get())) {
                @Override
                public boolean charTyped(char c, int i) {
                    boolean ret = super.charTyped(c, i);
                    stringValue.set(this.getValue());
                    return ret;
                }

                @Override
                public boolean keyPressed(int i, int j, int k) {
                    boolean ret = super.keyPressed(i, j, k);
                    stringValue.set(this.getValue());
                    return ret;
                }
            };
            box.setMaxLength(1000);
            box.setValue(stringValue.get());
            widget = box;
        } else if (configValue instanceof ConfigBuilder.BooleanValue booleanValue) {
            widget = CycleButton
                .onOffBuilder(booleanValue.get())
                .displayOnlyValue()
                .withTooltip((bool) -> configValue.getComment() != null ? Tooltip.create(Component.literal(configValue.getComment())) : null)
                .create(0, 0, ResettableEntry.valueButtonWidth, 20, Component.empty(), (button, bool) -> booleanValue.set(bool));
        } else if (configValue instanceof ConfigBuilder.InListValue inListValue) {
            widget = CycleButton.builder((newValue) -> Component.literal("" + newValue))
                .withValues(inListValue.getValidValues())
                .displayOnlyValue()
                .withTooltip((bool) -> configValue.getComment() != null ? Tooltip.create(Component.literal(configValue.getComment())) : null)
                .create(0, 0, ResettableEntry.valueButtonWidth, 20, Component.empty(), (button, newValue) -> inListValue.set(newValue));
        } else if (configValue instanceof ConfigBuilder.ListValue listValue) {
            widget = Button.builder(Component.translatable("vivecraft.options.editlist"), button -> Minecraft.getInstance().setScreen(new GuiListValueEditScreen(name, Minecraft.getInstance().screen, listValue))).size(ResettableEntry.valueButtonWidth, 20).build();
        } else {
            widget = Button.builder(Component.literal("" + configValue.get()), button -> {
                configValue.reset();
            }).bounds(0, 0, ResettableEntry.valueButtonWidth, 20).build();
        }
        if (configValue.getComment() != null) {
            widget.setTooltip(Tooltip.create(Component.literal(configValue.getComment())));
        }

        return new ResettableEntry(name, widget, button -> {
            configValue.reset();
            if (!(configValue instanceof ConfigBuilder.ListValue)) {
                widget.setMessage(Component.literal("" + configValue.get()));
                if (widget instanceof AbstractSliderButton slider) {
                    slider.onClick(widget.getX() + 4 + (widget.getWidth() - 8) * ((ConfigBuilder.NumberValue<?>) configValue).normalize(), 0.0);
                } else if (widget instanceof CycleButton cycle) {
                    cycle.setValue(configValue.get());
                } else if (widget instanceof EditBox box) {
                    box.setValue((String) configValue.get());
                }
            }
        }, () -> !configValue.isDefault());
    }

    public static class CategoryEntry extends BaseEntry {
        private final int width;
        public CategoryEntry(Component name) {
            super(name);
            this.width = Minecraft.getInstance().font.width(this.name);
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            Minecraft.getInstance().font.draw(poseStack, this.name, (float)(Minecraft.getInstance().screen.width / 2 - this.width / 2), (float)(j + m - Minecraft.getInstance().font.lineHeight - 1), 0xFFFFFF);
        }

        @Override
        @Nullable
        public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
            return null;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(new NarratableEntry(){
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

    public static class ResettableEntry extends WidgetEntry {
        private final Button resetButton;
        private final BooleanSupplier canReset;

        public static final int valueButtonWidth = 125;
        public ResettableEntry(Component name, AbstractWidget valueWidget, Button.OnPress resetAction, BooleanSupplier canReset) {
            super(name, valueWidget);

            this.canReset = canReset;
            this.resetButton = Button.builder(Component.literal("X"), resetAction).tooltip(Tooltip.create(Component.translatable("controls.reset")))
                .bounds(0, 0, 20, 20).build();
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            super.render(poseStack, i, j, k, l, m, n, o, bl, f);
            this.resetButton.setX(k + 230);
            this.resetButton.setY(j);
            this.resetButton.active = canReset.getAsBoolean();
            this.resetButton.render(poseStack, n, o, f);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.valueWidget, this.resetButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.valueWidget, this.resetButton);
        }
    }

    public static class WidgetEntry extends BaseEntry {
        protected final AbstractWidget valueWidget;

        public static final int valueButtonWidth = 145;

        public WidgetEntry(Component name, AbstractWidget valueWidget) {
            super(name);
            this.valueWidget = valueWidget;
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            Minecraft.getInstance().font.draw(poseStack, this.name, (float)(k + 90 - 140), (float)(j + m / 2 - Minecraft.getInstance().font.lineHeight / 2), 0xFFFFFF);
            this.valueWidget.setX(k + 105);
            this.valueWidget.setY(j);
            this.valueWidget.render(poseStack, n, o, f);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.valueWidget);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.valueWidget);
        }
    }

    public static abstract class BaseEntry extends Entry<BaseEntry> {

        protected final Component name;
        public BaseEntry(Component name) {
            this.name = name;
        }

    }
}

