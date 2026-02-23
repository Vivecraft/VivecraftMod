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
import org.vivecraft.client_vr.provider.openxr.MCOpenXR;
import org.vivecraft.client_vr.provider.control.Action;
import org.vivecraft.client_vr.provider.openxr.control.XRBindingProfile;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class GuiBindings extends GuiGroupedListEditorScreen<Action> {
    public GuiBindings(Screen lastScreen) {
        super(Component.empty(), lastScreen, false, () ->
            {
                if (MCOpenXR.get() == null) return List.of();
                List<Action> actions = new ArrayList<>();
                try {
                    if (MCOpenXR.get() == null) return List.of();
                    XRBindingProfile profile = XRBindingProfile.getCurrentProfile();
                    if (profile == null) profile = XRBindingProfile.getDefaultBinding(MCOpenXR.get().getCurrentInteractionProfile());
                    if (profile == null) profile = XRBindingProfile.getDefaultBinding("");
                    actions.addAll(profile.actions());
                } catch (FileNotFoundException e) {
                    // Show error to user about missing profile
                }

                return actions;
            },
            () -> {},
            save -> {
                XRBindingProfile profile = new XRBindingProfile(
                    "Custom Profile",
                    new String[]{MCOpenXR.get().getCurrentInteractionProfile()},
                    true,
                    save
                );
                if (profile.saveProfile()) {
                    // Show success to user
                } else {
                    // Show error to user
                }
            },
            action -> XRBindingProfile.getPrettyName(action.buttons().getFirst().path())
        );

        this.searchable = false;
    }

    @Override
    protected Action createNewValue(String category) {
        return null;
    }

    @Override
    protected ValueEntry<Action> toEntry(Action value, int index) {
        return new RemovableEntry<>(Component.translatable(value.key().substring(value.key().lastIndexOf('/') + 1)), value, 0);
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
