package org.vivecraft.mixin.client_vr.renderer.rendertype;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client.extensions.PreparedRenderTypeExtension;
import org.vivecraft.client.extensions.RenderSetupExtension;

@Mixin(RenderType.class)
public class RenderTypeVRMixin {
    @Shadow
    @Final
    private RenderSetup state;

    @ModifyReturnValue(method = "prepare", at = @At("RETURN"))
    private PreparedRenderType vivecraft$uniformOverrides(PreparedRenderType original) {
        RenderSetupExtension extendedState = (RenderSetupExtension) (Object) this.state;
        if (extendedState.vivecraft$getUndistorted()) {
            ((PreparedRenderTypeExtension) (Object) original).vivecraft$setUndistorted();
        }
        if (extendedState.vivecraft$getFogOverride() != null) {
            ((PreparedRenderTypeExtension) (Object) original).vivecraft$setFogOverride(
                extendedState.vivecraft$getFogOverride());
        }
        return original;
    }
}
