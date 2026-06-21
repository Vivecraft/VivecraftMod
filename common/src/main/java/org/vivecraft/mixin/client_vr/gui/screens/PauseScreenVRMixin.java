package org.vivecraft.mixin.client_vr.gui.screens;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dialog.Dialog;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
import org.vivecraft.mod_compat_vr.modmenu.ModMenuHelper;

import java.util.Optional;

@Mixin(value = PauseScreen.class, priority = 900)
public abstract class PauseScreenVRMixin extends Screen {

    @Shadow
    protected abstract Optional<? extends Holder<Dialog>> getCustomAdditions();

    @Shadow
    @Final
    private static Tooltip CUSTOM_OPTIONS_TOOLTIP;

    protected PauseScreenVRMixin(Component component) {
        super(component);
    }

    @Inject(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 2))
    private void vivecraft$addTopButtons(CallbackInfo ci, @Local GridLayout.RowHelper rowHelper) {
        if (!VRState.VR_INITIALIZED || !ClientDataHolderVR.getInstance().vrSettings.modifyPauseMenu) {
            return;
        }
        // reset row to above
        // we hide 2 buttons but keep them in, so need to reset the RowHelper
        try {
            if (!(ModMenuHelper.shouldOffsetButtons())) {
                rowHelper.addChild(null, -2);
            }
        } catch (IllegalArgumentException ignored) {
            // RowHelper doesn't actually allow negative offsets, but it does update the index before throwing this exception
        }

        // on a multiplayer server also add the social button
        if (!Minecraft.getInstance().isMultiplayerServer()) {
            rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.chat"),
                (p) -> this.minecraft.setScreen(new ChatScreen("", false))).width(98).build());
        } else {
            GridLayout gridWidgetChat_Social = new GridLayout();
            GridLayout.RowHelper rowHelperChat_Social = gridWidgetChat_Social.createRowHelper(2);
            rowHelperChat_Social.addChild(new Button.Builder(Component.translatable("vivecraft.gui.chat"),
                    (p) -> this.minecraft.setScreen(new ChatScreen("", false))).width(48).build(),
                LayoutSettings.defaults().paddingRight(2));

            rowHelperChat_Social.addChild(new Button.Builder(Component.translatable("vivecraft.gui.social"),
                (p) -> this.minecraft.setScreen(new SocialInteractionsScreen())).width(48).build());
            rowHelper.addChild(gridWidgetChat_Social);
        }

        rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.commands"),
            (p) -> this.minecraft.setScreen(new GuiQuickCommandsInGame(this))).width(98).build());
    }

    // use the disconnect button as an anchor, and shift by -3 to shift before the addChild call
    @Inject(method = "createPauseMenu", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/gui/screens/PauseScreen;disconnectButton:Lnet/minecraft/client/gui/components/Button;", shift = At.Shift.BY, by = -3))
    private void vivecraft$addLowerButtons(CallbackInfo ci, @Local GridLayout.RowHelper rowHelper) {
        if (!VRState.VR_INITIALIZED || !ClientDataHolderVR.getInstance().vrSettings.modifyPauseMenu) {
            return;
        }
        GridLayout gridWidgetOverlay_Profiler = new GridLayout();
        GridLayout.RowHelper rowHelperOverlay_Profiler = gridWidgetOverlay_Profiler.createRowHelper(2);
        rowHelperOverlay_Profiler.addChild(new Button.Builder(Component.translatable("vivecraft.gui.overlay"),
            (p) -> {
                this.minecraft.debugEntries.toggleDebugOverlay();
                this.minecraft.setScreen(null);
            }).width(48).build(), LayoutSettings.defaults().paddingRight(2));

        rowHelperOverlay_Profiler.addChild(new Button.Builder(Component.translatable("vivecraft.gui.profiler"),
            (p) -> {
                this.minecraft.gui.getDebugOverlay().toggleProfilerChart();
                this.minecraft.setScreen(null);
            }).width(48).build());

        rowHelper.addChild(gridWidgetOverlay_Profiler);

        rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.screenshot"),
            (p) -> {
                this.minecraft.setScreen(null);
                ClientDataHolderVR.getInstance().grabScreenShot = true;
            }).width(98).build());

        if (!ClientDataHolderVR.getInstance().vrSettings.seated) {
            if (ClientDataHolderVR.getInstance().vr.hasFBT() ||
                ClientDataHolderVR.getInstance().vr.getTrackers().size() >= 3)
            {
                rowHelper.addChild(new Button.Builder(
                    Component.translatable("vivecraft.options.screen.fbtcalibration.button"),
                    (p) -> this.minecraft.setScreen(new FBTCalibrationScreen(this)))
                    .width(98).build());
            } else {
                rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.calibrateheight"),
                    (p) -> {
                        AutoCalibration.calibrateManual();
                        ClientDataHolderVR.getInstance().vrSettings.saveOptions();
                        this.minecraft.setScreen(null);
                    }).width(98).build());
            }
        }

        if (ClientDataHolderVR.getInstance().katVr) {
            rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.alignkatwalk"),
                (p) -> {
                    jkatvr.resetYaw(ClientDataHolderVR.getInstance().vrPlayer.vrdata_room_pre.hmd.getYaw());
                    this.minecraft.setScreen(null);
                }).width(98).build());
        }

        if (ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON ||
            ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY)
        {
            rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.movethirdpersoncam"), (p) ->
            {
                if (!VRHotkeys.isMovingThirdPersonCam()) {
                    VRHotkeys.startMovingThirdPersonCam(1, VRHotkeys.Triggerer.MENUBUTTON);
                } else if (VRHotkeys.getMovingThirdPersonCamTriggerer() == VRHotkeys.Triggerer.MENUBUTTON) {
                    VRHotkeys.stopMovingThirdPersonCam();
                    ClientDataHolderVR.getInstance().vrSettings.saveOptions();
                }
            }).width(98).build());
        }
    }

    // hide buttons that we replace
    @WrapOperation(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 4))
    private LayoutElement vivecraft$linksInsteadOfReport(
        GridLayout.RowHelper instance, LayoutElement child, Operation<LayoutElement> original)
    {
        Optional<? extends Holder<Dialog>> optional = this.getCustomAdditions();
        if (VRState.VR_INITIALIZED && !ModMenuHelper.shouldOffsetButtons() && optional.isPresent()) {
            return original.call(instance, Button.builder((optional.get().value()).common().computeExternalTitle(),
                    (button) -> this.minecraft.player.connection.showDialog(optional.get(), this)).width(98)
                .tooltip(CUSTOM_OPTIONS_TOOLTIP).build());
        } else {
            return original.call(instance, child);
        }
    }

    @WrapOperation(method = {"addFeedbackButtons", "addFeedbackSubscreenAndCustomDialogButtons"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;"))
    private static LayoutElement vivecraft$hideReportBugs(
        GridLayout.RowHelper rowHelper, LayoutElement child, Operation<LayoutElement> original)
    {
        ((Button) child).visible =
            !VRState.VR_INITIALIZED || !ClientDataHolderVR.getInstance().vrSettings.modifyPauseMenu;
        return original.call(rowHelper, child);
    }
}
