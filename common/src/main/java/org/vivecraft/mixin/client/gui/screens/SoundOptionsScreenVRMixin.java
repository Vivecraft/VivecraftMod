package org.vivecraft.mixin.client.gui.screens;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SoundOptionsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.settings.VRSettings;

@Mixin(SoundOptionsScreen.class)
public class SoundOptionsScreenVRMixin extends Screen {
    protected SoundOptionsScreenVRMixin(Component component) {
        super(component);
    }

    @ModifyVariable(method = "init", at = @At(value = "LOAD", ordinal = 4), ordinal = 2)
    private int addHRTF(int k) {
        int i = this.height / 6 - 12;
        // TODO: Remove in 1.19
        this.addRenderableWidget(new Button(this.width / 2 - 155 + k % 2 * 160, i + 22 * (k >> 1), 150, 20, new TextComponent(ClientDataHolder.getInstance().vrSettings.getButtonDisplayString(VRSettings.VrOptions.HRTF_SELECTION)), (p_213104_1_) ->
        {
            this.clearWidgets();
            ClientDataHolder.getInstance().vrSettings.setOptionValue(VRSettings.VrOptions.HRTF_SELECTION);
            ClientDataHolder.getInstance().vrSettings.saveOptions();
            this.init();
        }));
        return k;
    }
}
