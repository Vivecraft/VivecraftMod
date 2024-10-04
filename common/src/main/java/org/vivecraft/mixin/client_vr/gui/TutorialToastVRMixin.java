package org.vivecraft.mixin.client_vr.gui;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TutorialToast.class)
public abstract class TutorialToastVRMixin implements Toast {

    @Shadow
    @Final
    private Component title;

    @Shadow
    @Final
    private Component message;

    @Inject(method = "render", at = @At("HEAD"))
    private void vivecraft$extendToast(
        GuiGraphics guiGraphics, ToastComponent toastComponent, long timeSinceLastVisible,
        CallbackInfoReturnable<Visibility> cir, @Share("offset") LocalRef<Integer> offset)
    {
        int width = Math.max(toastComponent.getMinecraft().font.width(this.title),
            this.message != null ? toastComponent.getMinecraft().font.width(this.message) : 0) + 34;
        offset.set(Math.min(this.width() - width, 0));
    }

    // change toast size
    // the texture gets stretched, but there seems to be no way to cut it in pieces, so that is probably the best option
    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"), index = 1)
    private int vivecraft$offsetToast(int x, @Share("offset") LocalRef<Integer> offset) {
        return x + offset.get();
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"), index = 3)
    private int vivecraft$changeToastWidth(int width, @Share("offset") LocalRef<Integer> offset) {
        return width - offset.get();
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/TutorialToast$Icons;render(Lnet/minecraft/client/gui/GuiGraphics;II)V"), index = 1)
    private int vivecraft$offsetIcon(int x, @Share("offset") LocalRef<Integer> offset) {
        return x + offset.get();
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"), index = 2)
    private int vivecraft$offsetText(int x, @Share("offset") LocalRef<Integer> offset) {
        return x + offset.get();
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"), index = 0)
    private int vivecraft$offsetProgressStart(int x, @Share("offset") LocalRef<Integer> offset) {
        return x + offset.get();
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 1), index = 2)
    private int vivecraft$offsetProgressEnd(int x, @Share("offset") LocalRef<Integer> offset) {
        return x + offset.get() - (int) ((float) x / TutorialToast.PROGRESS_BAR_WIDTH * offset.get());
    }
}
