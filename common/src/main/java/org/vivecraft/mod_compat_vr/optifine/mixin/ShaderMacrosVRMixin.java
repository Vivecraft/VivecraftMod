package org.vivecraft.mod_compat_vr.optifine.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

@Pseudo
@Mixin(targets = "net.optifine.shaders.config.ShaderMacros", remap = false)
public class ShaderMacrosVRMixin {

    @Shadow
    private static void addMacroLine(StringBuilder stringBuilder, String string, int n) {}

    @Shadow
    private static void addMacroLine(StringBuilder stringBuilder, String string) {}

    @Inject(method = "getFixedMacroLines", at = @At(value = "INVOKE", target = "Lnet/optifine/shaders/config/ShaderMacros;addMacroLine(Ljava/lang/StringBuilder;Ljava/lang/String;I)V"))
    private static void vivecraft$addVivecraftMacros(
        CallbackInfoReturnable<String> cir, @Local StringBuilder stringBuilder)
    {
        ShadersHelper.addMacros(
            (string) -> addMacroLine(stringBuilder, string),
            (string, value) -> addMacroLine(stringBuilder, string, value)
        );
    }
}
