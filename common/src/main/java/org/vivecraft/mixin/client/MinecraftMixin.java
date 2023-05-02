package org.vivecraft.mixin.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.VRPlayersClient;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;tick(Z)V", shift = Shift.BEFORE), method = "tick()V")
    public void music(CallbackInfo info) {
        VRPlayersClient.getInstance().tick();
    }
}
