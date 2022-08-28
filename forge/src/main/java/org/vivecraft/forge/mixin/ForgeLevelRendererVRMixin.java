package org.vivecraft.forge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.DataHolder;
import org.vivecraft.extensions.GameRendererExtension;
import org.vivecraft.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.mixin.client.MinecraftVRMixin;

@Mixin(LevelRenderer.class)
public class ForgeLevelRendererVRMixin {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V"),
            method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V")
    public void renderFast2(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo info) {
        boolean menuHandleft = ((GameRendererExtension) gameRenderer).isInMenuRoom() || this.minecraft.screen != null || KeyboardHandler.Showing;
        boolean menuhandright = menuHandleft || DataHolder.getInstance().interactTracker.hotbar >= 0 && DataHolder.getInstance().vrSettings.vrTouchHotbar;
        ((GameRendererExtension) gameRenderer).renderVrFast(f, true, menuhandright, menuHandleft, poseStack);
    }
}
