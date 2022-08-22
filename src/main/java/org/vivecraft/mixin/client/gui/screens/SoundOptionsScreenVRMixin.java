package org.vivecraft.mixin.client.gui.screens;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SoundOptionsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.DataHolder;
import org.vivecraft.settings.VRSettings;

@Mixin(SoundOptionsScreen.class)
public class SoundOptionsScreenVRMixin extends Screen {
    protected SoundOptionsScreenVRMixin(Component component) {
        super(component);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addHRTF(CallbackInfo ci) {
        int i = this.height / 6 - 12;
        // TODO: Remove in 1.19
        this.addRenderableWidget(new Button(this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + 24 * (i >> 1), 150, 20, new TextComponent(DataHolder.getInstance().vrSettings.getButtonDisplayString(VRSettings.VrOptions.HRTF_SELECTION)), (p_213104_1_) ->
        {
            this.clearWidgets();
            DataHolder.getInstance().vrSettings.setOptionValue(VRSettings.VrOptions.HRTF_SELECTION);
            DataHolder.getInstance().vrSettings.saveOptions();
        }));
    }
}
