package org.vivecraft.titleworlds.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageException;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.titleworlds.TitleWorldsMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Component component) {
        super(component);
    }

    @Unique
    private boolean noLevels;

    @Inject(method = "init", at = @At("HEAD"))
    void checkLevelStorage(CallbackInfo ci) {
        if (!ClientDataHolderVR.getInstance().vrSettings.menuWorldSelection) {
            return;
        }
        if (TitleWorldsMod.state.isTitleWorld) {
            this.noLevels = false;
        } else {
            try {
                this.noLevels = TitleWorldsMod.levelSource.findLevelCandidates().isEmpty();
            } catch (LevelStorageException e) {
                TitleWorldsMod.LOGGER.error(e);
            }
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PanoramaRenderer;render(FF)V"), method = "render")
    void cancelCubemapRender(PanoramaRenderer instance, float f, float g, PoseStack poseStack) {
        if (Minecraft.getInstance().level == null || !Minecraft.getInstance().isRunning()) {
            if (TitleWorldsMod.state.isTitleWorld) {
                this.renderDirtBackground(poseStack);
            } else {
                instance.render(f, g);
            }
        }
    }

    //@Inject(method = "render", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/gui/screens/TitleScreen;drawString(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"))
    void render(PoseStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.noLevels) {
            GuiComponent.drawCenteredString(matrices, font, "Put one or more worlds in the titleworlds folder and restart the game", this.width / 2, 2, 16777215);
        }
    }

    @Inject(method = "isPauseScreen", cancellable = true, at = @At("HEAD"))
    void isPauseScreen(CallbackInfoReturnable<Boolean> cir) {
        if (TitleWorldsMod.state.isTitleWorld) {
            cir.setReturnValue(TitleWorldsMod.state.pause);
        }
    }

    @Inject(method = "shouldCloseOnEsc", cancellable = true, at = @At("HEAD"))
    void shouldCloseOnEsc(CallbackInfoReturnable<Boolean> cir) {
        if (!TitleWorldsMod.state.isTitleWorld) {
            cir.setReturnValue(true);
        }
    }
}
