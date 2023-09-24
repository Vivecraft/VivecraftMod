package org.vivecraft.mixin.client_vr.renderer.blockentity;

import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_xr.render_pass.RenderPassType;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.renderer.RenderStateShard.MultiTextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TheEndPortalRenderer.class)
public class TheEndPortalRendererVRMixin {

    @Unique
    private static final RenderType END_PORTAL_VR = RenderType.create(
        "end_portal",
        DefaultVertexFormat.POSITION,
        Mode.QUADS,
        256,
        false,
        false,
        CompositeState.builder()
            .setShaderState(new ShaderStateShard(VRShaders::getRendertypeEndPortalShaderVR))
            .setTextureState(
                MultiTextureStateShard
                    .builder()
                    .add(TheEndPortalRenderer.END_SKY_LOCATION, false, false)
                    .add(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false)
                    .build())
            .createCompositeState(false)
    );

    @Inject(at = @At("HEAD"), method = "renderType", cancellable = true)
    private void differentShaderInVR(CallbackInfoReturnable<RenderType> cir){
        if (!RenderPassType.isVanilla()){
            cir.setReturnValue(END_PORTAL_VR);
        }
    }
}
