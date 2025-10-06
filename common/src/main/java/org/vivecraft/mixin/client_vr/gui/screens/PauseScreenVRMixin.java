package org.vivecraft.mixin.client_vr.gui.screens;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.gui.screens.FBTCalibrationScreen;
import org.vivecraft.client.gui.settings.GuiQuickCommandsInGame;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.settings.AutoCalibration;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_vr.utils.external.jkatvr;

@Mixin(value = PauseScreen.class, priority = 900)
public abstract class PauseScreenVRMixin extends Screen {

    protected PauseScreenVRMixin(Component component) {
        super(component);
    }

    @Inject(method = "createPauseMenu", at = @At("TAIL"))
    private void vivecraft$addTopButtons(CallbackInfo ci) {
        if (!VRState.VR_INITIALIZED || !ClientDataHolderVR.getInstance().vrSettings.modifyPauseMenu) {
            return;
        }

        // in these cases we add so many buttons that  the overflow, so need to shift everything up
        boolean cameraButton = !ClientDataHolderVR.getInstance().vrSettings.seated ||
            ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON ||
            ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY;

        // we add your buttons in the middle, buttons above that are shifted up, below that down
        int threshold = this.height / 4 - 16 + 120;

        // move every button up a bit
        for (GuiEventListener widget : this.children()) {
            if (widget instanceof AbstractWidget abstractWidget) {
                if (abstractWidget.y >= threshold) {
                    abstractWidget.y += 24;
                } else if (cameraButton) {
                    abstractWidget.y -= 24;
                }
            }
        }

        int offset = cameraButton ? 0 : 24;

        // on a multiplayer server also add the social button
        if (!Minecraft.getInstance().isMultiplayerServer()) {
            this.addRenderableWidget(
                new Button(this.width / 2 - 102, this.height / 4 + 48 + -16 + offset, 98, 20,
                    Component.translatable("vivecraft.gui.chat"),
                    (p) -> this.minecraft.setScreen(new ChatScreen("")))
            );
        } else {
            this.addRenderableWidget(
                new Button(this.width / 2 - 102, this.height / 4 + 48 + -16 + offset, 46, 20,
                    Component.translatable("vivecraft.gui.chat"),
                    (p) -> this.minecraft.setScreen(new ChatScreen(""))));
            this.addRenderableWidget(
                new Button(this.width / 2 - 102 + 48, this.height / 4 + 48 + -16 + offset, 46, 20,
                    Component.translatable("vivecraft.gui.social"),
                    (p) -> this.minecraft.setScreen(new SocialInteractionsScreen())));
        }

        this.addRenderableWidget(
            new Button(this.width / 2 + 4, this.height / 4 + 48 + -16 + offset, 98, 20,
                Component.translatable("vivecraft.gui.commands"),
                (p) -> this.minecraft.setScreen(new GuiQuickCommandsInGame(this))));

        this.addRenderableWidget(
            new Button(this.width / 2 - 102, this.height / 4 + 96 + -16 + offset, 49, 20,
                Component.translatable("vivecraft.gui.overlay"),
                (p) -> {
                    this.minecraft.options.renderDebug = !this.minecraft.options.renderDebug;
                    this.minecraft.setScreen(null);
                }));
        this.addRenderableWidget
            (new Button(this.width / 2 - 52, this.height / 4 + 96 + -16 + offset, 49, 20,
                Component.translatable("vivecraft.gui.profiler"),
                (p) -> {
                    if (!this.minecraft.options.renderDebug) {
                        this.minecraft.options.renderDebugCharts = false;
                    }
                    this.minecraft.options.renderDebugCharts = !this.minecraft.options.renderDebugCharts;
                    this.minecraft.options.renderDebug = this.minecraft.options.renderDebugCharts;
                    this.minecraft.setScreen(null);
                }));

        this.addRenderableWidget(
            new Button(this.width / 2 + 4, this.height / 4 + 96 + -16 + offset, 98, 20,
                Component.translatable("vivecraft.gui.screenshot"),
                (p) -> {
                    this.minecraft.setScreen(null);
                    ClientDataHolderVR.getInstance().grabScreenShot = true;
                }));

        if (!ClientDataHolderVR.getInstance().vrSettings.seated) {
            if (ClientDataHolderVR.getInstance().vr.hasFBT() ||
                ClientDataHolderVR.getInstance().vr.getTrackers().size() >= 3)
            {
                this.addRenderableWidget(
                    new Button(this.width / 2 - 102, this.height / 4 + 120 + -16 + offset, 98, 20,
                        Component.translatable("vivecraft.options.screen.fbtcalibration.button"),
                        (p) -> this.minecraft.setScreen(new FBTCalibrationScreen(this))));
            } else {
                this.addRenderableWidget(
                    new Button(this.width / 2 - 102, this.height / 4 + 120 + -16 + offset, 98, 20,
                        Component.translatable("vivecraft.gui.calibrateheight"),
                        (p) -> {
                            AutoCalibration.calibrateManual();
                            ClientDataHolderVR.getInstance().vrSettings.saveOptions();
                            this.minecraft.setScreen(null);
                        }));
            }
        }

        if (ClientDataHolderVR.getInstance().katVr) {
            this.addRenderableWidget(
                new Button(this.width / 2 + 106, this.height / 4 + 120 + -16 + offset, 98, 20,
                    Component.translatable("vivecraft.gui.alignkatwalk"),
                    (p) -> {
                        jkatvr.resetYaw(ClientDataHolderVR.getInstance().vrPlayer.vrdata_room_pre.hmd.getYaw());
                        this.minecraft.setScreen(null);
                    }));
        }

        if (cameraButton) {
            this.addRenderableWidget(
                new Button(this.width / 2 + 4, this.height / 4 + 120 + -16 + offset, 98, 20,
                    Component.translatable("vivecraft.gui.movethirdpersoncam"),
                    (p) -> {
                        if (!VRHotkeys.isMovingThirdPersonCam()) {
                            VRHotkeys.startMovingThirdPersonCam(1, VRHotkeys.Triggerer.MENUBUTTON);
                        } else if (VRHotkeys.getMovingThirdPersonCamTriggerer() == VRHotkeys.Triggerer.MENUBUTTON) {
                            VRHotkeys.stopMovingThirdPersonCam();
                            ClientDataHolderVR.getInstance().vrSettings.saveOptions();
                        }
                    }));
        }
    }

    // hide buttons that we replace
    @WrapOperation(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/PauseScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", ordinal = 3))
    private GuiEventListener vivecraft$hideFeedback(
        PauseScreen instance, GuiEventListener child, Operation<GuiEventListener> original)
    {
        ((Button) child).visible =
            !VRState.VR_INITIALIZED || !ClientDataHolderVR.getInstance().vrSettings.modifyPauseMenu;
        return original.call(instance, child);
    }

    @WrapOperation(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/PauseScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", ordinal = 4))
    private GuiEventListener vivecraft$hideReportBugs(
        PauseScreen instance, GuiEventListener child, Operation<GuiEventListener> original)
    {
        ((Button) child).visible =
            !VRState.VR_INITIALIZED || !ClientDataHolderVR.getInstance().vrSettings.modifyPauseMenu;
        return original.call(instance, child);
    }
}
