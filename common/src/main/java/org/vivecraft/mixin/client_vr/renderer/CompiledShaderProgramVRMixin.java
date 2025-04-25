package org.vivecraft.mixin.client_vr.renderer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.shaders.CompiledShader;
import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.ShaderProgramConfig;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.render.helpers.ShaderHelper;
import org.vivecraft.mixin.client.blaze3d.RenderSystemAccessor;

import java.lang.reflect.Method;
import java.nio.IntBuffer;

@Mixin(CompiledShaderProgram.class)
public abstract class CompiledShaderProgramVRMixin {
    @Shadow
    protected abstract Uniform parseUniformNode(ShaderProgramConfig.Uniform uniform);

    @ModifyExpressionValue(method = "setDefaultUniforms", at = @At(value = "CONSTANT", args = "intValue=12"))
    private int vivecraft$moreTextures(int constant) {
        return Math.max(constant, RenderSystemAccessor.getShaderTextures().length);
    }

    @Inject(method = "apply", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glUseProgram(I)V"))
    private void vivecraft$apply(CallbackInfo ci) {
        ShaderHelper.doMultiview((CompiledShaderProgram) (Object) this);
    }

    @Redirect(method = "setupUniforms", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/CompiledShaderProgram;parseUniformNode(Lnet/minecraft/client/renderer/ShaderProgramConfig$Uniform;)Lcom/mojang/blaze3d/shaders/Uniform;"))
    private Uniform vivecraft$replaceUniform(CompiledShaderProgram instance, ShaderProgramConfig.Uniform uniform) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            int count = GL32.glGetProgrami(instance.getProgramId(), GL32.GL_ATTACHED_SHADERS);
            IntBuffer shaders = stack.callocInt(count);
            GL32.glGetAttachedShaders(instance.getProgramId(), null, shaders.rewind());

            int shaderId = 0;
            for(int i = 0; i < shaders.capacity(); i++) {
                IntBuffer typeBuf = stack.callocInt(1);
                GL32.glGetShaderiv(shaders.get(i), GL32.GL_SHADER_TYPE, typeBuf.rewind());
                if(typeBuf.get(0) == GL32.GL_VERTEX_SHADER) {
                    shaderId = shaders.get(i);
                    break;
                }
            }
            assert(shaderId > 0);

            // I am gonna bombard you with reflection
            try {
                Class<CompiledShader> compiledShaderClass = CompiledShader.class;
                Method getShaderLocationMethod = compiledShaderClass.getDeclaredMethod("vivecraft$getShaderLocation", int.class);
                getShaderLocationMethod.setAccessible(true);

                ResourceLocation location = (ResourceLocation) getShaderLocationMethod.invoke(null, shaderId);
                if ((location.getNamespace().contains("minecraft") && (location.getPath().contains("core/gui")
                    || location.getPath().contains("core/rendertype_text")
                    || location.getPath().contains("core/position_color")
                    || location.getPath().contains("core/position_tex_color")))
                    || location.getNamespace().contains("vivecraft")) {
                    return this.parseUniformNode(uniform);
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }

            System.out.println("CompiledShaderProgramMixin ShaderID: " + instance.getProgramId());
            if (uniform.type().equals("matrix4x4") && uniform.name().equals("ProjMat")) {
                int i = Uniform.getTypeFromString(uniform.type());
                int j = 32;
                int k = j > 1 && j <= 4 && i < 8 ? j - 1 : 0;
                return new Uniform(uniform.name(), i + k, j);
            }
        }
        return this.parseUniformNode(uniform);
    }
}
