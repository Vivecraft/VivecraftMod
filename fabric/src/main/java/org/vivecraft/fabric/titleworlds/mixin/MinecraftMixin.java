package org.vivecraft.fabric.titleworlds.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.titleworlds.extensions.MinecraftTitleworldExtension;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    void init(GameConfig gameConfig, CallbackInfo ci) {
        ((MinecraftTitleworldExtension)this).tryLoadTitleWorld();
    }
}
