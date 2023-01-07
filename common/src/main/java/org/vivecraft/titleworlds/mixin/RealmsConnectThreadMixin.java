package org.vivecraft.titleworlds.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.realms.RealmsConnect$1")
public class RealmsConnectThreadMixin {

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 1, shift = At.Shift.AFTER))
    void clearLevel(CallbackInfo ci){
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().clearLevel(Minecraft.getInstance().screen));
    }
}
