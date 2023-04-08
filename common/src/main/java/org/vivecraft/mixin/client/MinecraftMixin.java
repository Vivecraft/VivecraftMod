package org.vivecraft.mixin.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.VRState;
import org.vivecraft.render.PlayerModelController;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {


    @Shadow
    protected abstract int getFramerateLimit();

    @Inject(method = "createTitle", at = @At(value = "INVOKE", target = "Ljava/lang/StringBuilder;toString()Ljava/lang/String;", shift = Shift.BEFORE),  locals = LocalCapture.CAPTURE_FAILHARD)
    private void title(CallbackInfoReturnable<String> cir, StringBuilder stringBuilder) {
        if (VRState.isVR) {
            stringBuilder.append(" (VR)");
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;tick(Z)V", shift = Shift.BEFORE), method = "tick()V", cancellable = true)
    public void music(CallbackInfo info) {
        PlayerModelController.getInstance().tick();
    }


}
