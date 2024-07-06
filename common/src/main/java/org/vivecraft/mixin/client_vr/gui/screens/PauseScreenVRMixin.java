package org.vivecraft.mixin.client_vr.gui.screens;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.ServerLinksScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerLinks;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.client.gui.settings.GuiQuickCommandsInGame;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.settings.AutoCalibration;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_vr.utils.external.jkatvr;
import org.vivecraft.mod_compat_vr.modmenu.ModMenuHelper;

import java.util.function.Supplier;

@Mixin(value = PauseScreen.class, priority = 900)
public abstract class PauseScreenVRMixin extends Screen {

    @Shadow
    @Final
    private static Component SERVER_LINKS;

    protected PauseScreenVRMixin(Component component) {
        super(component);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 4), method = "createPauseMenu", locals = LocalCapture.CAPTURE_FAILHARD)
    public void vivecraft$addInit(CallbackInfo ci, GridLayout gridWidget, GridLayout.RowHelper rowHelper) {
        if (!VRState.vrEnabled) {
            return;
        }
        // reset row to above
        try {
            if (!(ModMenuHelper.shouldOffsetButtons())) {
                rowHelper.addChild(null, -2);
            }
        } catch (IllegalArgumentException ignored) {
        }

        if (!Minecraft.getInstance().isMultiplayerServer()) {
            rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.chat"), (p) ->
            {
                this.minecraft.setScreen(new ChatScreen(""));
                if (ClientDataHolderVR.getInstance().vrSettings.autoOpenKeyboard) {
                    KeyboardHandler.setOverlayShowing(true);
                }
            }).width(98).build());
        } else {
            GridLayout gridWidgetChat_Social = new GridLayout();
            gridWidgetChat_Social.defaultCellSetting().paddingRight(1);
            GridLayout.RowHelper rowHelperChat_Social = gridWidgetChat_Social.createRowHelper(2);
            rowHelperChat_Social.addChild(new Button.Builder(Component.translatable("vivecraft.gui.chat"), (p) ->
            {
                this.minecraft.setScreen(new ChatScreen(""));
                if (ClientDataHolderVR.getInstance().vrSettings.autoOpenKeyboard) {
                    KeyboardHandler.setOverlayShowing(true);
                }
            }).width(48).build());

            rowHelperChat_Social.addChild(new Button.Builder(Component.translatable("vivecraft.gui.social"), (p) -> this.minecraft.setScreen(new SocialInteractionsScreen())).width(48).pos(50, 0).build());
            rowHelper.addChild(gridWidgetChat_Social);
        }

        rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.commands"), (p) -> this.minecraft.setScreen(new GuiQuickCommandsInGame(this))).width(98).build());
    }

    @Inject(at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/gui/screens/PauseScreen;disconnectButton:Lnet/minecraft/client/gui/components/Button;", shift = At.Shift.BY, by = -3), method = "createPauseMenu", locals = LocalCapture.CAPTURE_FAILHARD)
    public void vivecraft$addLowerButtons(CallbackInfo ci, GridLayout gridWidget, GridLayout.RowHelper rowHelper) {
        if (!VRState.vrEnabled) {
            return;
        }
        GridLayout gridWidgetOverlay_Profiler = new GridLayout();
        gridWidgetOverlay_Profiler.defaultCellSetting().paddingRight(1);
        GridLayout.RowHelper rowHelperOverlay_Profiler = gridWidgetOverlay_Profiler.createRowHelper(2);
        rowHelperOverlay_Profiler.addChild(new Button.Builder(Component.translatable("vivecraft.gui.overlay"), (p) ->
        {
            this.minecraft.gui.getDebugOverlay().toggleOverlay();
            this.minecraft.setScreen(null);
        }).width(48).build());

        rowHelperOverlay_Profiler.addChild(new Button.Builder(Component.translatable("vivecraft.gui.profiler"), (p) ->
        {
            this.minecraft.gui.getDebugOverlay().toggleProfilerChart();
            this.minecraft.setScreen(null);
        }).width(48).pos(50, 0).build());

        rowHelper.addChild(gridWidgetOverlay_Profiler);

        rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.screenshot"), (p) ->
        {
            this.minecraft.setScreen(null);
            ClientDataHolderVR.getInstance().grabScreenShot = true;
        }).width(98).build());

        if (!ClientDataHolderVR.getInstance().vrSettings.seated) {
            rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.calibrateheight"), (p) ->
            {
                AutoCalibration.calibrateManual();
                ClientDataHolderVR.getInstance().vrSettings.saveOptions();
                this.minecraft.setScreen(null);
            }).width(98).build());
        }

        if (ClientDataHolderVR.katvr) {
            rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.alignkatwalk"), (p) ->
            {
                jkatvr.resetYaw(ClientDataHolderVR.getInstance().vrPlayer.vrdata_room_pre.hmd.getYaw());
                this.minecraft.setScreen(null);
            }).width(98).build());
        }

        if (!ClientDataHolderVR.getInstance().vrSettings.seated || ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON || ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY) {
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

    @Redirect(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 2))
    private LayoutElement vivecraft$remove(GridLayout.RowHelper instance, LayoutElement layoutElement) {
        // Feedback button
        // don't remove, just hide, so mods that rely on it being there, still work
        ((Button) layoutElement).visible = !VRState.vrEnabled;
        return instance.addChild(layoutElement);
    }

    @Redirect(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 3))
    private LayoutElement vivecraft$remove2(GridLayout.RowHelper instance, LayoutElement layoutElement) {
        // report bugs button
        // don't remove, just hide, so mods that rely on it being there, still work
        ((Button) layoutElement).visible = !VRState.vrEnabled ||
            (ModMenuHelper.shouldOffsetButtons() && !this.minecraft.player.connection.serverLinks().isEmpty());
        return instance.addChild(layoutElement);
    }

    @WrapOperation(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/PauseScreen;openScreenButton(Lnet/minecraft/network/chat/Component;Ljava/util/function/Supplier;)Lnet/minecraft/client/gui/components/Button;", ordinal = 6))
    private Button vivecraft$linksInsteadOfReport(PauseScreen instance, Component component, Supplier<Screen> supplier, Operation<Button> original) {
        ServerLinks links = this.minecraft.player.connection.serverLinks();
        if (VRState.vrEnabled && !ModMenuHelper.shouldOffsetButtons() && !links.isEmpty()) {
            Supplier<Screen> sub = () -> new ServerLinksScreen(this, links);
            return original.call(instance, Component.empty().append(SERVER_LINKS), sub);
        } else {
            return original.call(instance, component, supplier);
        }
    }

    @Redirect(method = "addFeedbackButtons", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;"))
    private static LayoutElement vivecraft$remove3(GridLayout.RowHelper instance, LayoutElement layoutElement) {
        // Feedback/report bugs button
        // don't remove, just hide, so mods that rely on it being there, still work
        ((Button) layoutElement).visible = !VRState.vrEnabled;
        return instance.addChild(layoutElement);
    }
}
