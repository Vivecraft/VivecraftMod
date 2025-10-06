package org.vivecraft.mixin.client_vr.gui.screens;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
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

    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;I)Lnet/minecraft/client/gui/layouts/LayoutElement;"))
    private int vivecraft$makeSpacer1wide(int occupiedColumns) {
        // if we add the VR settings button, use one of the spacer slots
        return ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonEnabled ? 1 : occupiedColumns;
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;I)Lnet/minecraft/client/gui/layouts/LayoutElement;"))
    private void vivecraft$addVivecraftSettingsLeft(CallbackInfo ci, @Local GridLayout.RowHelper rowHelper) {
        if (ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonEnabled &&
            ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonPositionLeft)
        {
            vivecraft$addVivecraftButton(rowHelper);
        }
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;I)Lnet/minecraft/client/gui/layouts/LayoutElement;", shift = At.Shift.AFTER))
    private void vivecraft$addVivecraftSettingsRight(CallbackInfo ci, @Local GridLayout.RowHelper rowHelper) {
        if (ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonEnabled &&
            !ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonPositionLeft)
        {
            vivecraft$addVivecraftButton(rowHelper);
        }
    }

    @Unique
    private void vivecraft$addVivecraftButton(GridLayout.RowHelper rowHelper) {
        this.vivecraft$settings = new Button.Builder(Component.translatable("vivecraft.options.screen.main.button"),
            (p) -> {
                Minecraft.getInstance().options.save();
                Minecraft.getInstance().setScreen(new GuiMainVRSettings(this));
            }).build();
        rowHelper.addChild(this.vivecraft$settings);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout;arrangeElements()V"))
    private void vivecraft$noBigButtonsPlease(CallbackInfo ci, @Local GridLayout gridLayout) {
        if (ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonEnabled) {
            // if we don't do this it causes issues with bedrockify
            gridLayout.visitChildren(child -> {
                if (child.getWidth() > 150 && child instanceof Button button) {
                    button.setWidth(150);
                }
            });
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
