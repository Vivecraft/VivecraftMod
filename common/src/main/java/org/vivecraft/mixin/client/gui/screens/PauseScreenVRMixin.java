package org.vivecraft.mixin.client.gui.screens;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.GridWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.network.chat.Component;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.gui.settings.GuiQuickCommandsInGame;
import org.vivecraft.settings.AutoCalibration;
import org.vivecraft.settings.VRHotkeys;
import org.vivecraft.settings.VRSettings;
import org.vivecraft.utils.external.jkatvr;

@Mixin(PauseScreen.class)
public abstract class PauseScreenVRMixin extends Screen {

    private ClientDataHolder dataholder = ClientDataHolder.getInstance();

    protected PauseScreenVRMixin(Component component) {
        super(component);
    }


    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/GridWidget$RowHelper;addChild(Lnet/minecraft/client/gui/components/AbstractWidget;)Lnet/minecraft/client/gui/components/AbstractWidget;", ordinal = 4), method = "createPauseMenu", locals = LocalCapture.CAPTURE_FAILHARD)
    public void addInit(CallbackInfo ci, GridWidget gridWidget, GridWidget.RowHelper rowHelper) {
        // reset row to above
        try {
            rowHelper.addChild(null, -2);
        } catch (IllegalArgumentException e) {}

        if (!Minecraft.getInstance().isMultiplayerServer()) {
            rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.chat"), (p) ->
                    {
                        this.minecraft.setScreen(new ChatScreen(""));
                        if (ClientDataHolder.getInstance().vrSettings.autoOpenKeyboard)
                            KeyboardHandler.setOverlayShowing(true);
                    }).width(98).build());
        } else {
            GridWidget gridWidgetChat_Social = new GridWidget();
            gridWidgetChat_Social.defaultCellSetting().paddingRight(1);
            GridWidget.RowHelper rowHelperChat_Social = gridWidgetChat_Social.createRowHelper(2);
            rowHelperChat_Social.addChild(new Button.Builder(Component.translatable("vivecraft.gui.chat"), (p) ->
                    {
                        this.minecraft.setScreen(new ChatScreen(""));
                        if (ClientDataHolder.getInstance().vrSettings.autoOpenKeyboard)
                            KeyboardHandler.setOverlayShowing(true);
                    }).width(48).build());

            rowHelperChat_Social.addChild(new Button.Builder(Component.translatable("vivecraft.gui.social"), (p) -> this.minecraft.setScreen(new SocialInteractionsScreen())).width(48).pos(50, 0).build());
            rowHelper.addChild(gridWidgetChat_Social);
        }

        rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.commands"), (p) -> this.minecraft.setScreen(new GuiQuickCommandsInGame(this))).width(98).build());
    }

    @Inject(at =  @At(value = "FIELD", opcode = Opcodes.PUTFIELD,target = "Lnet/minecraft/client/gui/screens/PauseScreen;disconnectButton:Lnet/minecraft/client/gui/components/Button;", shift = At.Shift.BY, by = -3), method = "createPauseMenu", locals = LocalCapture.CAPTURE_FAILHARD)
    public void addLowerButtons(CallbackInfo ci, GridWidget gridWidget, GridWidget.RowHelper rowHelper) {
        GridWidget gridWidgetOverlay_Profiler = new GridWidget();
        gridWidgetOverlay_Profiler.defaultCellSetting().paddingRight(1);
        GridWidget.RowHelper rowHelperOverlay_Profiler = gridWidgetOverlay_Profiler.createRowHelper(2);
        rowHelperOverlay_Profiler.addChild(new Button.Builder(Component.translatable("vivecraft.gui.overlay"), (p) ->
        {
            this.minecraft.options.renderDebug = !this.minecraft.options.renderDebug;
            this.minecraft.setScreen((Screen) null);
        }).width(48).build());

        rowHelperOverlay_Profiler.addChild(new Button.Builder(Component.translatable("vivecraft.gui.profiler"), (p) ->
        {
            if (!this.minecraft.options.renderDebug) this.minecraft.options.renderDebugCharts = false;
            this.minecraft.options.renderDebugCharts = !this.minecraft.options.renderDebugCharts;
            this.minecraft.options.renderDebug = this.minecraft.options.renderDebugCharts;
            this.minecraft.setScreen((Screen) null);
        }).width(48).pos(50, 0).build());

        rowHelper.addChild(gridWidgetOverlay_Profiler);

        rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.screenshot"), (p) ->
        {
            this.minecraft.setScreen((Screen) null);
            ClientDataHolder.getInstance().grabScreenShot = true;
        }).width(98).build());

        if (!ClientDataHolder.getInstance().vrSettings.seated) {
            rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.calibrateheight"), (p) ->
            {
                AutoCalibration.calibrateManual();
                ClientDataHolder.getInstance().vrSettings.saveOptions();
                this.minecraft.setScreen((Screen) null);
            }).width(98).build());
        }

        if (ClientDataHolder.katvr) {
            rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.alignkatwalk"), (p) ->
            {
                jkatvr.resetYaw(ClientDataHolder.getInstance().vrPlayer.vrdata_room_pre.hmd.getYaw());
                this.minecraft.setScreen((Screen) null);
            }).width(98).build());
        }

        if (!ClientDataHolder.getInstance().vrSettings.seated || ClientDataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON || ClientDataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY) {
            rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.movethirdpersoncam"), (p) ->
            {
                if (!VRHotkeys.isMovingThirdPersonCam()) {
                    VRHotkeys.startMovingThirdPersonCam(1, VRHotkeys.Triggerer.MENUBUTTON);
                } else if (VRHotkeys.getMovingThirdPersonCamTriggerer() == VRHotkeys.Triggerer.MENUBUTTON) {
                    VRHotkeys.stopMovingThirdPersonCam();
                    ClientDataHolder.getInstance().vrSettings.saveOptions();
                }
            }).width(98).build());
        }
    }

    @Redirect(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/GridWidget$RowHelper;addChild(Lnet/minecraft/client/gui/components/AbstractWidget;)Lnet/minecraft/client/gui/components/AbstractWidget;", ordinal = 2))
    private AbstractWidget remove(GridWidget.RowHelper instance, AbstractWidget abstractWidget) {
        // Feedback button
        // don't remove, just hide, so mods that rely on it being there, still work
        abstractWidget.visible = false;
        return instance.addChild(abstractWidget);
    }
    @Redirect(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/GridWidget$RowHelper;addChild(Lnet/minecraft/client/gui/components/AbstractWidget;)Lnet/minecraft/client/gui/components/AbstractWidget;", ordinal = 3))
    private AbstractWidget remove2(GridWidget.RowHelper instance, AbstractWidget abstractWidget) {
        // report bugs button
        // don't remove, just hide, so mods that rely on it being there, still work
        abstractWidget.visible = false;
        return instance.addChild(abstractWidget);
    }
    @Redirect(method = "createPauseMenu", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/Button;active:Z"))
    private void remove3(Button instance, boolean value) {}
}
