package org.vivecraft.mixin.client_vr.blaze3d.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.CompiledShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(CompiledShader.class)
public class CompiledShaderMixin {
    @Redirect(method = "compile", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;glShaderSource(ILjava/lang/String;)V"))
    private static void onCompile(
        int i, String source) {
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

        System.out.println("Debug shader print:\n" + source);
        GlStateManager.glShaderSource(i, source);
    }
}
