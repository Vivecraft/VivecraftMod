package org.vivecraft.mixin.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.Builder;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.gui.screens.UpdateScreen;
import org.vivecraft.client.utils.UpdateChecker;

import static org.joml.Math.max;
import static org.vivecraft.client_vr.VRState.*;

@Mixin(net.minecraft.client.gui.screens.TitleScreen.class)
public abstract class TitleScreenMixin extends net.minecraft.client.gui.screens.Screen {

    protected TitleScreenMixin(Component component) {
        super(component);
    }

    @Unique
    private Button vivecraft$vrModeButton;
    @Unique
    private Button vivecraft$updateButton;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", shift = Shift.AFTER, ordinal = 1), method = "createNormalMenuOptions")
    public void vivecraft$initFullGame(CallbackInfo ci) {
        this.vivecraft$addVRModeButton();
    }

    @Inject(at = @At("TAIL"), method = "createDemoMenuOptions")
    public void vivecraft$initDemo(CallbackInfo ci) {
        this.vivecraft$addVRModeButton();
    }

    @Unique
    private void vivecraft$addVRModeButton() {

        this.vivecraft$vrModeButton = new Builder(Component.translatable("vivecraft.gui.vr", vrEnabled ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF), (button) -> {
            vrEnabled = !vrEnabled;
            dh.vrSettings.vrEnabled = vrEnabled;
            dh.vrSettings.saveOptions();

            button.setMessage(Component.translatable("vivecraft.gui.vr", vrEnabled ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
        })
            .size(56, 20)
            .pos(this.width / 2 + 104, this.height / 4 + 72)
            .build();
        this.vivecraft$vrModeButton.visible = dh.vrSettings.vrToggleButtonEnabled;

        this.addRenderableWidget(this.vivecraft$vrModeButton);

        this.vivecraft$updateButton = new Builder(Component.translatable("vivecraft.gui.update"), (button) -> mc.setScreen(new UpdateScreen()))
            .size(56, 20)
            .pos(this.width / 2 + 104, this.height / 4 + 96)
            .build();

        this.vivecraft$updateButton.visible = UpdateChecker.hasUpdate;

        this.addRenderableWidget(this.vivecraft$updateButton);
    }

    @Inject(at = @At("TAIL"), method = "render")
    public void vivecraft$renderToolTip(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci) {
        this.vivecraft$updateButton.visible = UpdateChecker.hasUpdate;

        if (this.vivecraft$vrModeButton.visible && this.vivecraft$vrModeButton.isMouseOver(i, j)) {
            guiGraphics.renderTooltip(this.font, this.font.split(Component.translatable("vivecraft.options.VR_MODE.tooltip"), max(this.width / 2 - 43, 170)), i, j);
        }
        if (vrInitialized && !vrRunning) {
            Component hotswitchMessage = Component.translatable("vivecraft.messages.vrhotswitchinginfo");
            guiGraphics.renderTooltip(this.font, this.font.split(hotswitchMessage, 280), this.width / 2 - 140 - 12, 17);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PanoramaRenderer;render(FF)V"), method = "render")
    public void vivecraft$maybeNoPanorama(PanoramaRenderer instance, float f, float g) {
        if (vrRunning && dh.menuWorldRenderer != null && dh.menuWorldRenderer.isReady()) {
            return;
        }
        instance.render(f, g);
    }
}
