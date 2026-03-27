package org.vivecraft.mod_compat_vr.xaerosworldmap.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.extensions.FieldDependentMixin;

@Pseudo
@FieldDependentMixin("DEST_TRANSPARENCY")
@Mixin(targets = {"xaero.map.graphics.CustomRenderTypes", "xaero.lib.client.graphics.XaeroRenderType"})
public class CustomRenderTypesVRMixin {
    @Final
    @Mutable
    @Shadow
    protected static BlendFunction DEST_TRANSPARENCY;

    @WrapOperation(method = "<clinit>*", at = {
        @At(value = "FIELD", target = "Lxaero/map/graphics/CustomRenderTypes;DEST_TRANSPARENCY:Lcom/mojang/blaze3d/pipeline/BlendFunction;", opcode = Opcodes.PUTSTATIC),
        @At(value = "FIELD", target = "Lxaero/lib/client/graphics/XaeroRenderType;DEST_TRANSPARENCY:Lcom/mojang/blaze3d/pipeline/BlendFunction;", opcode = Opcodes.PUTSTATIC)
    })
    private static void vivecraft$fixMapBlend(BlendFunction value, Operation<Void> original) {
        // refetch main target when not an improved buffer, to get the new gui buffer
        original.call(
            new BlendFunction(SourceFactor.ONE, DestFactor.ZERO, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA));
    }
}
