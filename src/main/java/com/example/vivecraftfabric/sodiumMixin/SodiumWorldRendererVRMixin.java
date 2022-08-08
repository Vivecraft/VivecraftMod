package com.example.vivecraftfabric.sodiumMixin;

import com.example.vivecraftfabric.DataHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.render.RenderPass;

@Pseudo
@Mixin(SodiumWorldRenderer.class)
public class SodiumWorldRendererVRMixin {

    @ModifyVariable(at = @At("STORE"), ordinal = 0, method = "updateChunks", print = true)
    public boolean RenderUpdate(boolean b) {
        return true;
    }

}
