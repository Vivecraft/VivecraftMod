package org.vivecraft.mod_compat_vr.iris.mixin.irisshaders;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.sugar.Local;
import net.irisshaders.iris.helpers.StringPair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

import java.util.ArrayList;
import java.util.List;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.gl.shader.StandardMacros", remap = false)
public class IrisStandardMacrosMixin {
    @Shadow
    private static void define(List<StringPair> defines, String key) {}

    @Shadow
    private static void define(List<StringPair> defines, String key, String value) {}

    @Inject(method = "createStandardEnvironmentDefines", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/gl/shader/StandardMacros;define(Ljava/util/List;Ljava/lang/String;Ljava/lang/String;)V", ordinal = 0))
    private static void vivecraft$addVivecraftUniforms(
        CallbackInfoReturnable<ImmutableList<StringPair>> cir, @Local ArrayList<StringPair> standardDefines)
    {
        ShadersHelper.addMacros(
            (string) -> define(standardDefines, string),
            (string, value) -> define(standardDefines, string, String.valueOf(value))
        );
    }
}
