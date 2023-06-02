package org.vivecraft.mixin.client_vr;

import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ClientDataHolderVR;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.VRState;

@Mixin(Options.class)
public abstract class OptionsVRMixin {
    @Shadow
    public KeyMapping[] keyMappings;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;load()V"))
    void processOptionsMixin(Options instance) {
        this.keyMappings = VivecraftVRMod.INSTANCE.initializeBindings(this.keyMappings);
        instance.load();
    }

    @Group(name = "reinitFrameBuffers", min = 1, max = 1)
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;allChanged()V", remap = true), method = "method_42464", remap = false, expect = 0)
    private static void reinitFabric(OptionInstance optionInstance, GraphicsStatus graphicsStatus, CallbackInfo ci) {
        if (VRState.vrInitialized) {
            ClientDataHolderVR.getInstance().vrRenderer.reinitFrameBuffers("gfx setting change");
        }
    }

    @Group(name = "reinitFrameBuffers", min = 1, max = 1)
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;allChanged()V",remap = true), method = "m_231861_", remap = false, expect = 0)
    private static void reinitForge(OptionInstance optionInstance, GraphicsStatus graphicsStatus, CallbackInfo ci) {
        if (VRState.vrInitialized) {
            ClientDataHolderVR.getInstance().vrRenderer.reinitFrameBuffers("gfx setting change");
        }
    }

}
