package org.vivecraft.mixin.client_vr.renderer;

import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.extensions.LevelRendererExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.settings.VRSettings.ShaderGUIRender;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.mod_compat_vr.ShadersHelper;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

import org.joml.Matrix4f;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

import static org.vivecraft.client_vr.VRState.*;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// priority 999 to inject before iris, for the vrFast rendering
@Mixin(value = LevelRenderer.class, priority = 999)
public abstract class LevelRendererVRMixin implements ResourceManagerReloadListener, AutoCloseable, LevelRendererExtension {

    @Unique
    @Nullable
    public RenderTarget alphaSortVROccludedFramebuffer;
    @Unique
    @Nullable
    public RenderTarget alphaSortVRUnoccludedFramebuffer;
    @Unique
    @Nullable
    public RenderTarget alphaSortVRHandsFramebuffer;
    @Unique
    public float selR;
    @Unique
    public float selG;
    @Unique
    public float selB;

    @Unique
    private Entity capturedEntity;

    @Shadow
    private ClientLevel level;
    @Shadow
    private PostChain transparencyChain;
    @Shadow
    private RenderTarget translucentTarget;
    @Shadow
    private RenderTarget itemEntityTarget;
    @Shadow
    private RenderTarget particlesTarget;
    @Shadow
    private RenderTarget weatherTarget;
    @Shadow
    private RenderTarget cloudsTarget;
    @Shadow
    private PostChain entityEffect;
    @Shadow
    private RenderTarget entityTarget;
    @Shadow
    private boolean needsFullRenderChunkUpdate;
    @Final
    @Shadow
    private RenderBuffers renderBuffers;
    @Unique
    private Entity renderedEntity;

    @Shadow
    protected abstract void renderHitOutline(PoseStack poseStack, VertexConsumer buffer, Entity entity, double d, double e, double g, BlockPos blockpos, BlockState blockstate);

    @Shadow
    private static void renderShape(PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
    }

    @Override
    public Entity getRenderedEntity() {
        return this.renderedEntity;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I", ordinal = 0), method = "renderSnowAndRain")
    public double rainX(double x) {
        if (!RenderPassType.isVanilla() && (dh.currentPass == RenderPass.LEFT || dh.currentPass == RenderPass.RIGHT)) {
            return dh.vrPlayer.vrdata_world_render.getEye(RenderPass.CENTER).getPosition().x;
        }
        return x;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I", ordinal = 1), method = "renderSnowAndRain")
    public double rainY(double y) {
        if (!RenderPassType.isVanilla() && (dh.currentPass == RenderPass.LEFT || dh.currentPass == RenderPass.RIGHT)) {
            return dh.vrPlayer.vrdata_world_render.getEye(RenderPass.CENTER).getPosition().y;
        }
        return y;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I", ordinal = 2), method = "renderSnowAndRain")
    public double rainZ(double z) {
        if (!RenderPassType.isVanilla() && (dh.currentPass == RenderPass.LEFT || dh.currentPass == RenderPass.RIGHT)) {
            return dh.vrPlayer.vrdata_world_render.getEye(RenderPass.CENTER).getPosition().z;
        }
        return z;
    }

	@Inject(at = @At("TAIL"), method = "onResourceManagerReload")
	public void reinitVR(ResourceManager resourceManager, CallbackInfo ci) {
        if (vrInitialized) {
            dh.vrRenderer.reinitFrameBuffers("Resource Reload");
        }
	}

    /*
     * Start `renderLevel` lighting poll
     */

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;pollLightUpdates()V"), method = "renderLevel")
    public void onePollLightUpdates(ClientLevel instance) {
        if (RenderPassType.isVanilla() || dh.currentPass == RenderPass.LEFT) {
            instance.pollLightUpdates();
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/lighting/LevelLightEngine;runLightUpdates()I"), method = "renderLevel")
    public int oneLightingUpdates(LevelLightEngine instance) {
        if (RenderPassType.isVanilla() || dh.currentPass == RenderPass.LEFT) {
            instance.runLightUpdates();
        }
        if (!RenderPassType.isVanilla()) {
            this.setShaderGroup();
        }
        return 0;
    }

    /*
     * End `renderLevel` lighting poll
     */

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;getRenderDistance()F", shift = Shift.BEFORE),
            method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V ")
    public void stencil(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo info) {
        if (!RenderPassType.isVanilla()) {
            mc.getProfiler().popPush("stencil");
            ((GameRendererExtension) gameRenderer).drawEyeStencil(false);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z"), method = "renderLevel")
    public boolean noPlayerWhenSleeping(LivingEntity instance) {
        if (!RenderPassType.isVanilla()) {
            return false;
        } else {
            return instance.isSleeping();
        }
    }

    // TODO: could this mess with mods?
    @ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V", ordinal = 0), method = "renderLevel")
    public Entity captureEntityRestoreLoc(Entity entity, PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer) {
        this.capturedEntity = entity;
        if (!RenderPassType.isVanilla() && this.capturedEntity == camera.getEntity()) {
            ((GameRendererExtension) gameRenderer).restoreRVEPos((LivingEntity) this.capturedEntity);
        }
        this.renderedEntity = this.capturedEntity;
        return entity;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V", shift = Shift.AFTER), method = "renderLevel")
    public void restoreLoc2(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
        if (!RenderPassType.isVanilla() && this.capturedEntity == camera.getEntity()) {
            ((GameRendererExtension) gameRenderer).cacheRVEPos((LivingEntity) this.capturedEntity);
            ((GameRendererExtension) gameRenderer).setupRVE();
        }
        this.renderedEntity = null;
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;", ordinal = 1), method = "renderLevel")
    public void interactOutline(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            this.level.getProfiler().popPush("interact outline");
            this.selR = this.selG = this.selB = 1.0f;
            net.minecraft.world.phys.Vec3 vec3 = camera.getPosition();
            if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
                OptifineHelper.beginOutlineShader();
            }
            for (int c = 0; c < 2; c++) {
                if (dh.interactTracker.isInteractActive(c) && (dh.interactTracker.inBlockHit[c] != null || dh.interactTracker.bukkit[c])) {
                    BlockPos blockpos = dh.interactTracker.inBlockHit[c] != null ? dh.interactTracker.inBlockHit[c].getBlockPos() : BlockPos.containing(dh.vrPlayer.vrdata_world_render.getController(c).getPosition());
                    BlockState blockstate = this.level.getBlockState(blockpos);
                    this.renderHitOutline(poseStack, this.renderBuffers.bufferSource().getBuffer(RenderType.lines()), camera.getEntity(), vec3.x, vec3.y, vec3.z, blockpos, blockstate);
                }
            }
            if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
                this.renderBuffers.bufferSource().endBatch(RenderType.lines());
                OptifineHelper.endOutlineShader();
            }
            // reset outline color
            this.selR = this.selG = this.selB = 0.0f;
        }
    }

    @ModifyVariable(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;", ordinal = 1), method = "renderLevel", ordinal = 0, argsOnly = true)
    public boolean noBlockoutlineOnInteract(boolean renderBlockOutline) {
        // don't draw the block outline when the interaction outline is active
        return renderBlockOutline && (RenderPassType.isVanilla() || !(dh.interactTracker.isInteractActive(0) && (dh.interactTracker.inBlockHit[0] != null || dh.interactTracker.bukkit[0])));
    }

    @Unique
    private boolean menuHandLeft;
    @Unique
    private boolean menuHandRight;
    @Unique
    private boolean guiRendered = false;

    @Inject(at = @At("HEAD"), method = "renderLevel")
    public void resetGuiRendered(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
        this.guiRendered = false;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V", ordinal = 0, shift = Shift.AFTER), method = "renderLevel")
    public void renderVrStuffPart1(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
        if (RenderPassType.isVanilla()) {
            return;
        }
        this.menuHandLeft = ((GameRendererExtension) gameRenderer).isInMenuRoom() || mc.screen != null || KeyboardHandler.isShowing();
        this.menuHandRight = this.menuHandLeft || dh.interactTracker.hotbar >= 0 && dh.vrSettings.vrTouchHotbar;

        if (this.transparencyChain != null) {
            ((GameRendererExtension) gameRenderer).renderVRFabulous(f, (LevelRenderer) (Object) this, this.menuHandRight, this.menuHandLeft, poseStack);
        } else {
            ((GameRendererExtension) gameRenderer).renderVrFast(f, false, this.menuHandRight, this.menuHandLeft, poseStack);
            if (ShadersHelper.isShaderActive() && dh.vrSettings.shaderGUIRender == ShaderGUIRender.BEFORE_TRANSLUCENT_SOLID) {
                // shaders active, and render gui before translucents
                ((GameRendererExtension) gameRenderer).renderVrFast(f, true, this.menuHandRight, this.menuHandLeft, poseStack);
                this.guiRendered = true;
            }
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = Shift.BEFORE, ordinal = 3),
            method = "renderLevel")
    public void renderVrStuffPart2(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
        if (RenderPassType.isVanilla()) {
            return;
        }

        if (this.transparencyChain == null && (!ShadersHelper.isShaderActive() || dh.vrSettings.shaderGUIRender == ShaderGUIRender.AFTER_TRANSLUCENT)) {
            // no shaders, or shaders, and gui after translucents
            ((GameRendererExtension) gameRenderer).renderVrFast(f, true, this.menuHandRight, this.menuHandLeft, poseStack);
            this.guiRendered = true;
        }
    }

    // if the gui didn't render yet, and something canceled the level renderer, render it now.
    // or if shaders are on, and option AFTER_SHADER is selected
    @Inject(at = @At("RETURN"), method = "renderLevel")
    public void renderVrStuffFinal(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo info) {
        if (RenderPassType.isVanilla()) {
            return;
        }

        if (!this.guiRendered && this.transparencyChain == null) {
            ((GameRendererExtension) gameRenderer).renderVrFast(f, true, this.menuHandRight, this.menuHandLeft, poseStack);
            this.guiRendered = true;
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderShape(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/phys/shapes/VoxelShape;DDDFFFF)V"), method = "renderHitOutline")
    public void colorHitBox(PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
        renderShape(poseStack, vertexConsumer, voxelShape, d, e, f, this.selR, this.selG, this.selB, j);
    }

    @Inject(at = @At("HEAD"), method = "levelEvent")
    public void shakeOnSound(int i, BlockPos blockPos, int j, CallbackInfo ci) {
        boolean playerNearAndVR = vrRunning && mc.player != null && mc.player.isAlive() && mc.player.blockPosition().distSqr(blockPos) < 25.0D;
        if (playerNearAndVR) {
            switch (i) {
                /* pre 1.19.4, they are now separate
                case 1011,      // IRON_DOOR_CLOSE
                        1012,   // WOODEN_DOOR_CLOSE
                        1013,   // WOODEN_TRAPDOOR_CLOSE
                        1014,   // FENCE_GATE_CLOSE
                        1036    // IRON_TRAPDOOR_CLOSE
                        -> VRState.dh.vr.triggerHapticPulse(0, 250);
                 */
                case 1019,      // ZOMBIE_ATTACK_WOODEN_DOOR
                        1020,   // ZOMBIE_ATTACK_IRON_DOOR
                        1021    // ZOMBIE_BREAK_WOODEN_DOOR
                        -> {
                    dh.vr.triggerHapticPulse(0, 750);
                    dh.vr.triggerHapticPulse(1, 750);
                }
                case 1030 ->    // ANVIL_USE
                        dh.vr.triggerHapticPulse(0, 500);
                case 1031 -> {  // ANVIL_LAND
                    dh.vr.triggerHapticPulse(0, 1250);
                    dh.vr.triggerHapticPulse(1, 1250);
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = {"initOutline", "initTransparency"})
    public void restorePostChain(CallbackInfo ci){
        if (vrInitialized) {
            this.restoreVanillaPostChains();
            dh.vrRenderer.reinitFrameBuffers("Outline/Transparency shaders Reloaded");
        }
    }

    @Inject(at = @At("TAIL"), method = "initOutline")
    public void captureOutlineChain(CallbackInfo ci){
        RenderPassManager.INSTANCE.vanillaOutlineChain = this.entityEffect;
    }

    @Inject(at = @At("TAIL"), method = "initTransparency")
    public void captureTransparencyChain(CallbackInfo ci){
        RenderPassManager.INSTANCE.vanillaTransparencyChain = this.transparencyChain;
    }
    @Inject(at = @At("TAIL"), method = "deinitTransparency")
    public void removeTransparencyChain(CallbackInfo ci){
        RenderPassManager.INSTANCE.vanillaTransparencyChain = null;
    }

    @Inject(at = @At("TAIL"), method = "close")
    public void removePostChains(CallbackInfo ci){
        RenderPassManager.INSTANCE.vanillaOutlineChain = null;
        RenderPassManager.INSTANCE.vanillaTransparencyChain = null;
    }

    @Override
    public void restoreVanillaPostChains(){
        this.transparencyChain = RenderPassManager.INSTANCE.vanillaTransparencyChain;

        if (this.transparencyChain != null) {
            this.translucentTarget = this.transparencyChain.getTempTarget("translucent");
            this.itemEntityTarget = this.transparencyChain.getTempTarget("itemEntity");
            this.particlesTarget = this.transparencyChain.getTempTarget("particles");
            this.weatherTarget = this.transparencyChain.getTempTarget("weather");
            this.cloudsTarget = this.transparencyChain.getTempTarget("clouds");
        } else {
            this.translucentTarget = null;
            this.itemEntityTarget = null;
            this.particlesTarget = null;
            this.weatherTarget = null;
            this.cloudsTarget = null;
        }

        this.entityEffect = RenderPassManager.INSTANCE.vanillaOutlineChain;
        if (this.entityEffect != null) {
            this.entityTarget = this.entityEffect.getTempTarget("final");
        } else {
            this.entityTarget = null;
        }
    }

    public void setShaderGroup() {
        PostChain transparencyChain = RenderPassManager.wrp.transparencyChain;

        if (transparencyChain != null) {
            this.transparencyChain = transparencyChain;
            this.translucentTarget = transparencyChain.getTempTarget("translucent");
            this.itemEntityTarget = transparencyChain.getTempTarget("itemEntity");
            this.particlesTarget = transparencyChain.getTempTarget("particles");
            this.weatherTarget = transparencyChain.getTempTarget("weather");
            this.cloudsTarget = transparencyChain.getTempTarget("clouds");
            this.alphaSortVRHandsFramebuffer = transparencyChain.getTempTarget("vrhands");
            this.alphaSortVROccludedFramebuffer = transparencyChain.getTempTarget("vroccluded");
            this.alphaSortVRUnoccludedFramebuffer = transparencyChain.getTempTarget("vrunoccluded");
        } else {
            this.transparencyChain = null;
            this.translucentTarget = null;
            this.itemEntityTarget = null;
            this.particlesTarget = null;
            this.weatherTarget = null;
            this.cloudsTarget = null;
            this.alphaSortVRHandsFramebuffer = null;
            this.alphaSortVROccludedFramebuffer = null;
            this.alphaSortVRUnoccludedFramebuffer = null;
        }

        PostChain outlineChain = RenderPassManager.wrp.outlineChain;

        if (outlineChain != null) {
            this.entityEffect = outlineChain;
            this.entityTarget = outlineChain.getTempTarget("final");
        } else {
            this.entityEffect = null;
            this.entityTarget = null;
        }
    }

    @Override
    public RenderTarget getAlphaSortVROccludedFramebuffer() {
        return this.alphaSortVROccludedFramebuffer;
    }

    @Override
    public RenderTarget getAlphaSortVRUnoccludedFramebuffer() {
        return this.alphaSortVRUnoccludedFramebuffer;
    }

    @Override
    public RenderTarget getAlphaSortVRHandsFramebuffer() {
        return this.alphaSortVRHandsFramebuffer;
    }
}
