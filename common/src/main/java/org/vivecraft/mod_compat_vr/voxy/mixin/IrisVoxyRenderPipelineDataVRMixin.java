package org.vivecraft.mod_compat_vr.voxy.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Pseudo
@Mixin(targets = "me.cortex.voxy.client.iris.IrisVoxyRenderPipelineData")
public class IrisVoxyRenderPipelineDataVRMixin {

    @ModifyReturnValue(method = "createUniformSet", at = @At("RETURN"))
    private static List<?> vivecraft$sortUniforms(List<?> original) {
        original.sort((o1, o2) -> {
            try {
                return ((String) o1.getClass().getMethod("name").invoke(o1)).compareTo(
                    (String) o2.getClass().getMethod("name").invoke(o2));
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                return 0;
            }
        });
        return original;
    }
}
