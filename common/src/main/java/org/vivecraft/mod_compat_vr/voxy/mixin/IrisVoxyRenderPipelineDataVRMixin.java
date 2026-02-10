package org.vivecraft.mod_compat_vr.voxy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

@Pseudo
@Mixin(targets = "me.cortex.voxy.client.iris.IrisVoxyRenderPipelineData")
public class IrisVoxyRenderPipelineDataVRMixin {

    @Inject(method = "createUniformSet", at = @At("RETURN"), cancellable = true, remap = false)
    private static void vivecraft$sortUniforms(CallbackInfoReturnable<Object> cir) {
        Object original = cir.getReturnValue();
        if (original instanceof List<?> list) {
            list.sort((o1, o2) -> {
                try {
                    return ((String) o1.getClass().getMethod("name").invoke(o1)).compareTo(
                        (String) o2.getClass().getMethod("name").invoke(o2));
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    return 0;
                }
            });
            cir.setReturnValue(original);
        } else {
            Arrays.sort((Object[]) original, (o1, o2) -> {
                try {
                    return ((String) o1.getClass().getMethod("name").invoke(o1)).compareTo(
                        (String) o2.getClass().getMethod("name").invoke(o2));
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    return 0;
                }
            });
            cir.setReturnValue(original);
        }
    }
}
