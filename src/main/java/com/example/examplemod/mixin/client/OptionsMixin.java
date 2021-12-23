package com.example.examplemod.mixin.client;

import com.example.examplemod.DataHolder;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public abstract class OptionsMixin {

    @Inject(method = "processOptions", at = @At("TAIL"))
    void processOptionsMixin(Options.FieldAccess fieldAccess, CallbackInfo ci) {
        ((Options) (Object) this).keyMappings = DataHolder.getInstance().vr.initializeBindings(((Options) (Object) this).keyMappings);
    }
}
