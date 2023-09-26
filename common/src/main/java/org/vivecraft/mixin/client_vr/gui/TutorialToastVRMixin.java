package org.vivecraft.mixin.client_vr.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

    @Unique
    private int vivecraft$offset;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V", shift = At.Shift.AFTER), method = "render")
    private void vivecraft$extendToast(GuiGraphics guiGraphics, ToastComponent toastComponent, long l, CallbackInfoReturnable<Visibility> cir) {
        int width = Math.max(toastComponent.getMinecraft().font.width(this.title), message != null ? toastComponent.getMinecraft().font.width(this.message) : 0) + 34;
        vivecraft$offset = Math.min(this.width() - width, 0);
        if (vivecraft$offset < 0) {
            // draw a bigger toast from right to left, to override the left border
            for (int i = vivecraft$offset - (this.width() - 8) * (vivecraft$offset / (this.width() - 8)); i >= vivecraft$offset; i -= this.width() - 8) {
                guiGraphics.blit(TEXTURE, i, 0, 0, 96, this.width() - 4, this.height());
            }
        }
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/TutorialToast$Icons;render(Lnet/minecraft/client/gui/GuiGraphics;II)V"), method = "render", index = 1)
    private int vivecraft$offsetIcon(int x) {
        return x + vivecraft$offset;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"), method = "render", index = 2)
    private int vivecraft$offsetText(int x) {
        return x + vivecraft$offset;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"), method = "render", index = 0)
    private int vivecraft$offsetProgressStart(int x) {
        return x + vivecraft$offset;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 1), method = "render", index = 2)
    private int vivecraft$offsetProgressEnd(int x) {
        return x + vivecraft$offset - (int) ((float) x / TutorialToast.PROGRESS_BAR_WIDTH * vivecraft$offset);
    }
}
