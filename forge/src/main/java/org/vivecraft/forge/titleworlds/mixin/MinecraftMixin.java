package org.vivecraft.forge.titleworlds.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.titleworlds.TitleWorldsMod;
import org.vivecraft.titleworlds.extensions.MinecraftTitleworldExtension;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin{
    @Inject(method = "lambda$new$1", at = @At("TAIL"))
    private void loadTitleWorld(String s, int i, CallbackInfo ci){
        // is runs after resource loading is finished, and the titlescreen opens
        TitleWorldsMod.onInitializeClient();
        ((MinecraftTitleworldExtension)this).tryLoadTitleWorld();
    }
}
