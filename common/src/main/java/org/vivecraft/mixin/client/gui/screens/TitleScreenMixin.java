package org.vivecraft.mixin.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.VRState;
import org.vivecraft.Xplat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Component component) {
        super(component);
    }

    private final Properties vrConfig = new Properties();
    private final Path vrConfigPath = Xplat.getConfigPath("vivecraft-config.properties");
    private boolean showRestart = false;
    private boolean showError = false;
    private AbstractWidget firstButton;
    private Button vrModeButton;

    @Inject(at =  @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", shift = At.Shift.AFTER, ordinal = 1), method = "createNormalMenuOptions")
    public void initFullGame(CallbackInfo ci) {
        addVRModeButton();
    }
    @Inject(at =  @At("TAIL"), method = "createDemoMenuOptions")
    public void initDemo(CallbackInfo ci) {
        addVRModeButton();
    }
    private void addVRModeButton() {

        // get first button, to position warnings
        firstButton = (AbstractWidget)renderables.get(0);

        try {
            if (!Files.exists(vrConfigPath)) {
                Files.createFile(vrConfigPath);
            }
            vrConfig.load(Files.newInputStream(vrConfigPath));
            if (!vrConfig.containsKey("vrStatus")) {
                vrConfig.setProperty("vrStatus", String.valueOf(VRState.isVR));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String vrMode = Boolean.parseBoolean(vrConfig.getProperty("vrStatus")) ? "VR" : "NONVR";
        vrModeButton = new Button(this.width / 2 + 104, this.height / 4 + 72, 56, 20, Component.literal(getIcon() + vrMode), (button) -> {
            showError = false;
            String newMode;
            if (button.getMessage().getString().endsWith("NONVR")) {
                vrConfig.setProperty("vrStatus", String.valueOf(true));
                newMode = "VR";
            } else {
                vrConfig.setProperty("vrStatus", String.valueOf(false));
                newMode = "NONVR";
            }
            try {
                vrConfig.store(Files.newOutputStream(vrConfigPath), "This file stores if VR should be enabled.");
            } catch (IOException e) {
                showError = true;
            }

            button.setMessage(Component.translatable(getIcon() + newMode));
        });
        this.addRenderableWidget(vrModeButton);
    }

    private String getIcon() {
        showRestart = Boolean.parseBoolean(vrConfig.getProperty("vrStatus")) != VRState.isVR;

        return (showError ? "§c\u26A0§r " : (showRestart ? "§6\u24D8§r ": ""));
    }

    @Inject(at =  @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", shift = At.Shift.BEFORE, ordinal = 0), method = "render")
    public void renderText(PoseStack poseStack, int i, int j, float f, CallbackInfo ci) {
        int l = this.height / 4 + 49;
        drawString(poseStack, this.font, "Vivecraft", this.width / 2 + 106, l, 16777215);
        drawString(poseStack, this.font, Component.translatable("vivecraft.messages.mode"), this.width / 2 + 106, l + 10, 16777215);
    }
    @Inject(at =  @At("TAIL"), method = "render")
    public void renderWarning(PoseStack poseStack, int i, int j, float f, CallbackInfo ci) {

        if (vrModeButton.isMouseOver(i, j)) {
            renderTooltip(poseStack, font.split(Component.translatable("vivecraft.options.VR_MODE.tooltip"), Math.max(width / 2 - 43, 170)), i, j);
        }

        int warningHeight = firstButton.y - 10;
        Component warning = null;
        if (showError) {
            warning = Component.translatable("vivecraft.messages.configWriteError");
        } else if (showRestart) {
            warning = Component.translatable("vivecraft.messages.configChangeRestart");
            warningHeight = this.height / 2;
        }
        if (warning != null) {
            int length = 0;
            List<FormattedCharSequence> splitString = font.split(FormattedText.of(warning.getString()), 360);
            for(FormattedCharSequence string : splitString) {
                length = Math.max(length, this.font.width(string));
            }
            // move in front of button tooltips
            poseStack.pushPose();
            poseStack.translate(0,0,500);
            GuiComponent.fill(poseStack, this.width / 2 - length / 2 - 4, warningHeight - 4, this.width / 2 + length / 2 + 4, warningHeight + (font.lineHeight + 3)  * splitString.size(), -536870912);
            for (int line = 0; line < splitString.size(); line++) {
                drawCenteredString(poseStack, this.font, splitString.get(line), this.width / 2, warningHeight + (font.lineHeight + 2) * line, 16777215);
            }
            poseStack.popPose();
        }
    }
}
