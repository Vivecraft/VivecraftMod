package org.vivecraft.mod_compat_vr.physicsmod.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;

// priority 1100 so we inject after physics mod
@Mixin(value = OptionsScreen.class, priority = 1100)
public abstract class OptionsScreenMixin extends Screen {

    protected OptionsScreenMixin(Component component) {
        super(component);
    }

    @Inject(at = @At("TAIL"), method = "init")
    private void vivecraft$reducePhysicsmodButtonSize(CallbackInfo ci) {
        if (ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonEnabled) {
            for (GuiEventListener guiEventListener : children()) {
                if (guiEventListener instanceof Button button) {
                    if (button.getMessage().getContents() instanceof TranslatableContents contents && "physicsmod.menu.main.title".equals(contents.getKey())) {
                        // physics mods button would collide with ours, so make it half size to the right
                        if (ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonPositionLeft) {
                            button.setX(button.getX() + button.getWidth() / 2 + 5);
                        }
                        button.setWidth(button.getWidth() / 2 - 5);
                        // move it up, so it aligns with ours
                        button.setY(button.getY() - 6);
                    }
                }
            }
        }
    }
}
