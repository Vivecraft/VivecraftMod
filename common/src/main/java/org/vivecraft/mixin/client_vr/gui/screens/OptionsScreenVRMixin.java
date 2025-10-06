package org.vivecraft.mixin.client_vr.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.gui.settings.GuiMainVRSettings;
import org.vivecraft.client_vr.ClientDataHolderVR;

import java.util.HashSet;
import java.util.Set;

// we want to be late here to be able to fix button collisions with other mods
@Mixin(value = OptionsScreen.class, priority = 1100)
public class OptionsScreenVRMixin extends Screen {

    @Unique
    private Button vivecraft$settings;

    protected OptionsScreenVRMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void vivecraft$addVivecraftSettings(CallbackInfo ci) {
        if (ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonEnabled) {
            int xOffset = ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonPositionLeft ? -155 : 5;

            this.addRenderableWidget(
                new Button(this.width / 2 + xOffset, this.height / 6 - 12 + 24, 150, 20,
                    Component.translatable("vivecraft.options.screen.main.button"), (p) -> {
                    Minecraft.getInstance().options.save();
                    Minecraft.getInstance().setScreen(new GuiMainVRSettings(this));
                }));
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void vivecraft$fitButtons(CallbackInfo ci) {
        if (ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonEnabled) {
            int leftEdge = this.width / 2 - 154;
            int rightEdge = this.width / 2 + 154;

            Set<AbstractWidget> collidingButtons = new HashSet<>();

            // search for colliding buttons
            for (GuiEventListener child : children()) {
                if (child instanceof AbstractWidget button && button != this.vivecraft$settings) {
                    // only change buttons that are in the main columns and at the same height as ours
                    if (button.getX() < rightEdge && (button.getX() + button.getWidth()) > leftEdge &&
                        button.getY() + button.getHeight() > this.vivecraft$settings.getY() &&
                        button.getY() < this.vivecraft$settings.getY() + this.vivecraft$settings.getHeight())
                    {
                        collidingButtons.add(button);
                    }
                }
            }

            // if there is something colliding, rearrange them
            if (!collidingButtons.isEmpty()) {
                float buttonWidth = 308F / (collidingButtons.size() + 1);

                int index = 0;
                // alter our button to fit
                if (ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonPositionLeft) {
                    index++;
                } else {
                    this.vivecraft$settings.setX(rightEdge - this.vivecraft$settings.getWidth());
                }
                this.vivecraft$settings.setWidth((int) buttonWidth - 4);

                // alter other buttons
                for (AbstractWidget button : collidingButtons) {
                    button.setWidth(
                        (int) buttonWidth - ((index > 0 && index < collidingButtons.size()) ? 8 : 4));
                    // move vertically, so it aligns with ours
                    button.setY(this.vivecraft$settings.getY());
                    // move them to the side
                    button.setX(leftEdge + (int) (buttonWidth * index + 0.5F) + (index > 0 ? 4 : 0));
                    index++;
                }
            }
        }
    }
}
