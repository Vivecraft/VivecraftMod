package org.vivecraft.mixin.client;

import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.DataHolder;
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
public abstract class OptionsVRMixin {
    @Shadow
    public KeyMapping[] keyMappings;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;load()V"))
    void processOptionsMixin(Options instance) {
        this.keyMappings = DataHolder.getInstance().vr.initializeBindings(this.keyMappings);
        instance.load();
    }
}
