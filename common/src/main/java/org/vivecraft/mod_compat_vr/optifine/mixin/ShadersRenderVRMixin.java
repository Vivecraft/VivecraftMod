package org.vivecraft.mod_compat_vr.optifine.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_xr.render_pass.RenderPassType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Pseudo
@Mixin(targets = "net.optifine.shaders.ShadersRender")
public class ShadersRenderVRMixin {

    @Shadow
    public static void updateActiveRenderInfo(Camera activeRenderInfo, Minecraft mc, float partialTicks){};

    @Unique
    private static Method setCameraShadow = null;

    @Inject(at = @At("HEAD"), method = "renderHand0", remap = false, cancellable = true)
    private static void noTranslucentHandsInVR (CallbackInfo ci){
        if (!RenderPassType.isVanilla()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "renderHand1", remap = false, cancellable = true)
    private static void noSolidHandsInVR (CallbackInfo ci){
        if (!RenderPassType.isVanilla()) {
            ci.cancel();
        }
    }

    @Inject(at = @At(value = "INVOKE",target = "Lnet/minecraft/client/Minecraft;getInstance()Lnet/minecraft/client/Minecraft;", remap = true), method = "renderShadowMap", remap = false, cancellable = true)
    private static void shadowsOnlyOnce(GameRenderer gameRenderer, Camera activeRenderInfo, int pass, float partialTicks, long finishTimeNano, CallbackInfo ci){
        if (!RenderPassType.isVanilla() && ClientDataHolderVR.getInstance().currentPass != RenderPass.LEFT) {
            if (setCameraShadow == null) {
                try {
                    setCameraShadow = Class.forName("net.optifine.shaders.Shaders").getMethod("setCameraShadow", PoseStack.class, Camera.class, float.class);

                } catch (NoSuchMethodException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                updateActiveRenderInfo(activeRenderInfo, Minecraft.getInstance(), partialTicks);
                setCameraShadow.invoke(null, new PoseStack(), activeRenderInfo, partialTicks);
            } catch (IllegalAccessException | InvocationTargetException ignored) {
            }
            ci.cancel();
        }
    }

    @ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V", remap = true, shift = At.Shift.BEFORE), ordinal = 0, method = "renderShadowMap", remap = false)
    private static Entity fixPlayerPos(Entity entity, GameRenderer gameRenderer, Camera activeRenderInfo){
        if (!RenderPassType.isVanilla() && entity == activeRenderInfo.entity) {
            ((GameRendererExtension)gameRenderer).restoreRVEPos((LivingEntity)entity);
        }
        return entity;
    }
    @ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V", remap = true, shift = At.Shift.AFTER), ordinal = 0, method = "renderShadowMap", remap = false)
    private static Entity restorePlayerPos(Entity entity, GameRenderer gameRenderer, Camera activeRenderInfo){
        if (!RenderPassType.isVanilla() && entity == activeRenderInfo.entity) {
            ((GameRendererExtension)gameRenderer).cacheRVEPos((LivingEntity)entity);
            ((GameRendererExtension)gameRenderer).setupRVE();
        }
        return entity;
    }
}
