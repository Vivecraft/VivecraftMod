package org.vivecraft.client.gui.settings;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.framework.screens.GuiGroupedListEditorScreen;
import org.vivecraft.client_vr.provider.control.ActionSet;
import org.vivecraft.client_vr.provider.control.Source;
import org.vivecraft.client_vr.provider.openxr.MCOpenXR;
import org.vivecraft.client_vr.provider.control.BindingProfile;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiBindings extends GuiGroupedListEditorScreen<Source> {
    public GuiBindings(Screen lastScreen) {
        super(Component.empty(), lastScreen, false, () ->
            {
                if (MCOpenXR.get() == null) return List.of();
                List<Source> sources = new ArrayList<>();
                try {
                    if (MCOpenXR.get() == null) return List.of();
                    BindingProfile profile = BindingProfile.getCurrentProfile();
                    if (profile == null) profile = BindingProfile.getDefaultBinding(MCOpenXR.get().getCurrentInteractionProfile());
                    if (profile == null) profile = BindingProfile.getDefaultBinding("");
                    for (ActionSet set : profile.sets().values()) {sources.addAll(set.sources());}
                } catch (FileNotFoundException e) {
                    // Show error to user about missing profile
                }

                return sources;
            },
            () -> {},
            save -> {
                Map<String, ActionSet> bindings = new HashMap<>();
                bindings.put("/actions/custom",
                    new ActionSet(save, null, null, null)
                );

                BindingProfile profile = new BindingProfile(
                    "Custom Profile",
                    "New custom profile",
                    "Custom",
                    null,
                    bindings
                );

                if (profile.saveProfile()) {
                    // TODO success message
                } else {
                    // TODO error message
                }
            },
            action -> Component.translatable(action.path()).getString()
        );

        this.searchable = false;
    }

    @Override
    protected Source createNewValue(String category) {
        return null;
    }

    @Override
    protected ValueEntry<Source> toEntry(Source value, int index) {
        return new RemovableEntry<>(Component.translatable(value.path()), value, 0);
    }

    protected static class RemovableEntry<T> extends ValueEntry<T> {
        protected final T value;

        private final int index;
        private final Button editButton;
        private final Button removeButton;

        public RemovableEntry(Component name, T value, int index) {
            super(name, null);
            this.value = value;
            this.index = index;

            this.editButton = Button.builder(Component.translatable("vivecraft.options.edit"),
                    button -> {})
                .bounds(0, 0, 100, 20).build();
            this.removeButton = Button.builder(Component.literal("-"),
                    button -> {})
                .bounds(0, 0, 20, 20).build();
        }

        @Override
        public void renderContent(
            GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovering, float partialTick)
        {
            super.renderContent(guiGraphics, mouseX, mouseY, hovering, partialTick);

            int textY = this.getY() + this.getHeight() / 2 - Minecraft.getInstance().font.lineHeight / 2 + 2;
            guiGraphics.drawString(Minecraft.getInstance().font, this.name, this.getContentX(), textY,
                this.textColor());
            this.removeButton.active = this.isActive();
            this.editButton.active = this.isActive();

            this.editButton.setX(this.getContentRight() - this.editButton.getWidth() - 20);
            this.editButton.setY(this.getY());
            this.editButton.render(guiGraphics, mouseX, mouseY, partialTick);
            this.removeButton.setX(this.getContentRight() - 20);
            this.removeButton.setY(this.getY());
            this.removeButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.editButton, this.removeButton);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.editButton, this.removeButton);
        }

        @Override
        public void setActive(boolean active) {
            super.setActive(active);
            this.removeButton.active = active;
            this.editButton.active = active;
        }

        @Override
        public T getValue() {
            return this.value;
        }
    }
}
