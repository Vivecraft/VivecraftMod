package org.vivecraft.mixin.client_vr.gui.screens;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.gui.settings.GuiMainVRSettings;
import org.vivecraft.client_vr.ClientDataHolderVR;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(OptionsScreen.class)
public class OptionsScreenVRMixin extends Screen {

    @Unique
    private Button vivecraft$settings;

    @Unique
    private final Map<AbstractWidget, Triple<Integer, Integer, Integer>> vivecraft$alteredButtons = new LinkedHashMap<>();

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
        rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.options.screen.main.button"),
            (p) -> {
                Minecraft.getInstance().options.save();
                Minecraft.getInstance().setScreen(new GuiMainVRSettings((Screen) (Object) this));
            }).build());
    }

    @WrapMethod(method = "repositionElements")
    private void vivecraft$fitButtons(Operation<Void> original) {
        // restore old buttons, if we moved them already
        for (Map.Entry<AbstractWidget, Triple<Integer, Integer, Integer>> button : this.vivecraft$alteredButtons.entrySet()) {
            button.getKey().setX(button.getValue().getLeft());
            button.getKey().setY(button.getValue().getMiddle());
            button.getKey().setWidth(button.getValue().getRight());
        }
        this.vivecraft$alteredButtons.clear();

        original.call();

        if (ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonEnabled) {
            int leftEdge = this.width / 2 - 154;
            int rightEdge = this.width / 2 + 154;

            // search for colliding buttons
            for (GuiEventListener child : children()) {
                if (child instanceof AbstractWidget button && button != this.vivecraft$settings) {
                    // only change buttons that are in the main columns and at the same height as ours
                    if (button.getX() < rightEdge && button.getRight() > leftEdge &&
                        button.getY() + button.getHeight() > this.vivecraft$settings.getY() &&
                        button.getY() < this.vivecraft$settings.getY() + this.vivecraft$settings.getHeight())
                    {
                        this.vivecraft$alteredButtons.put(button,
                            Triple.of(button.getX(), button.getY(), button.getWidth()));
                    }
                }
            }

            // if there is something colliding, rearrange them
            if (!this.vivecraft$alteredButtons.isEmpty()) {
                Triple<Integer, Integer, Integer> vivecraftSettingsTriple = Triple.of(this.vivecraft$settings.getX(),
                    this.vivecraft$settings.getY(), this.vivecraft$settings.getWidth());

                float buttonWidth = 308F / (this.vivecraft$alteredButtons.size() + 1);

                int index = 0;
                // alter our button to fit
                if (ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonPositionLeft) {
                    index++;
                } else {
                    this.vivecraft$settings.setX(rightEdge - this.vivecraft$settings.getWidth());
                }
                this.vivecraft$settings.setWidth((int) buttonWidth - 4);

                // alter other buttons
                for (AbstractWidget button : this.vivecraft$alteredButtons.keySet()) {
                    button.setWidth(
                        (int) buttonWidth - ((index > 0 && index < this.vivecraft$alteredButtons.size()) ? 8 : 4));
                    // move vertically, so it aligns with ours
                    button.setY(this.vivecraft$settings.getY());
                    // move them to the side
                    button.setX(leftEdge + (int) (buttonWidth * index + 0.5F) + (index > 0 ? 4 : 0));
                    index++;
                }

                // add our own button as altered
                this.vivecraft$alteredButtons.put(this.vivecraft$settings, vivecraftSettingsTriple);
            }
        }
    }
}
