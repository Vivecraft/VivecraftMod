package org.vivecraft.mixin.client_vr.blaze3d.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.CompiledShader;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.HashMap;

@Mixin(CompiledShader.class)
public class CompiledShaderMixin {
    @Shadow
    private int shaderId;
    @Unique
    private static ResourceLocation SHADER_LOCATION;
    @Unique
    private static final Map<Integer, ResourceLocation> SHADER_MAP = new HashMap<>();

    @Inject(method = "compile", at = @At("HEAD"))
    private static void getId(
        ResourceLocation shaderId, CompiledShader.Type type, String source, CallbackInfoReturnable<CompiledShader> cir) {
        SHADER_LOCATION = shaderId;
    }

    @Redirect(method = "compile", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;glShaderSource(ILjava/lang/String;)V"))
    private static void onCompile(int i, String source) {
        SHADER_MAP.put(i, SHADER_LOCATION);
        if ((SHADER_LOCATION.getNamespace().contains("minecraft") && (!SHADER_LOCATION.getPath().contains("core/gui")
        && !SHADER_LOCATION.getPath().contains("core/rendertype_text")
        && !SHADER_LOCATION.getPath().contains("core/position_tex_color")
        && !SHADER_LOCATION.getPath().contains("core/position_color")))
        || !SHADER_LOCATION.getNamespace().contains("vivecraft")) {
            if(source.contains("ProjMat") && !source.contains("fragColor") && !source.contains("gl_FragColor")) {
                source = source.replace("#version 150", """
                #version 330 core
                #extension GL_OVR_multiview2 : enable
                
                layout(num_views = 2) in;""");
                source = source.replace("uniform mat4 ProjMat;", "uniform mat4 ProjMat[2];");

                String regex = "^(?!uniform\\s+mat4\\s+ProjMat\\[\\d*];).*?\\bProjMat\\b";

                Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(source);

                StringBuilder result = new StringBuilder();

                while (matcher.find()) {
                    String replacedLine = matcher.group().replace("ProjMat", "ProjMat[gl_ViewID_OVR]");
                    matcher.appendReplacement(result, Matcher.quoteReplacement(replacedLine));
                }

                matcher.appendTail(result);
                source = result.toString();
            }

            System.out.println("Debug shader print for " + SHADER_LOCATION + ":\n" + source);
            GlStateManager.glShaderSource(i, source);
    } else {
            System.out.println("Skipping shader overwrite for " + SHADER_LOCATION);
            GlStateManager.glShaderSource(i, source);;
        }
    }

    private static ResourceLocation vivecraft$getShaderLocation(int shaderId) {
        return SHADER_MAP.get(shaderId);
    }
}
