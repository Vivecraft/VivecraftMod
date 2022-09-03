package org.vivecraft.mixin.blaze3d.systems;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.mojang.blaze3d.systems.RenderSystem.blendFuncSeparate;

@Mixin(RenderSystem.class)
public interface RenderSystemAccessor {

    // needs remap because of forge
    @Accessor
    public static int[] getShaderTextures(){
        return null;
    }
}
