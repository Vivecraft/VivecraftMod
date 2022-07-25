package com.example.examplemod.mixin.client;

import com.example.examplemod.DataHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(Options.class)
public abstract class OptionsMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    void processOptionsMixin(Minecraft minecraft, File file, CallbackInfo ci) {
        ((Options) (Object) this).keyMappings = DataHolder.getInstance().vr.initializeBindings(((Options) (Object) this).keyMappings);
    }
}
