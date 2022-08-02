package com.example.vivecraftfabric.mixin.client;

import com.example.vivecraftfabric.DataHolder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(Options.class)
public abstract class OptionsMixin {
    @Shadow
    public KeyMapping[] keyMappings;

    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;renderDistance:I", shift = At.Shift.BEFORE))
    void processOptionsMixin(Minecraft minecraft, File file, CallbackInfo ci) {
        this.keyMappings = DataHolder.getInstance().vr.initializeBindings(this.keyMappings);
    }
}
