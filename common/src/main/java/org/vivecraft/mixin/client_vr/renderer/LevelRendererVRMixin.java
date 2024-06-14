package org.vivecraft.mixin.client_vr.renderer;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.extensions.LevelRendererExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.mod_compat_vr.ShadersHelper;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

import javax.annotation.Nullable;

// priority 999 to inject before iris, for the vrFast rendering
@Mixin(value = LevelRenderer.class, priority = 999)
public abstract class LevelRendererVRMixin implements ResourceManagerReloadListener, AutoCloseable, LevelRendererExtension {

    @Unique
    @Nullable
    public RenderTarget vivecraft$alphaSortVROccludedFramebuffer;
    @Unique
    @Nullable
    public RenderTarget vivecraft$alphaSortVRUnoccludedFramebuffer;
    @Unique
    @Nullable
    public RenderTarget vivecraft$alphaSortVRHandsFramebuffer;
    @Unique
    public float vivecraft$selR;
    @Unique
    public float vivecraft$selG;
    @Unique
    public float vivecraft$selB;

    @Unique
    private Entity vivecraft$capturedEntity;
    @Unique
    private Entity vivecraft$renderedEntity;

    @Final
    @Shadow
    private Minecraft minecraft;
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
    @Final
    @Shadow
    private RenderBuffers renderBuffers;

    @Shadow
    protected abstract void renderHitOutline(PoseStack poseStack, VertexConsumer buffer, Entity entity, double d, double e, double g, BlockPos blockpos, BlockState blockstate);

    @Shadow
    private static void renderShape(PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
    }

    @Override
    @Unique
    public Entity vivecraft$getRenderedEntity() {
        return this.vivecraft$renderedEntity;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I", ordinal = 0), method = "renderSnowAndRain")
    public double vivecraft$rainX(double x) {
        if (!RenderPassType.isVanilla() && (ClientDataHolderVR.getInstance().currentPass == RenderPass.LEFT || ClientDataHolderVR.getInstance().currentPass == RenderPass.RIGHT)) {
            return ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_render.getEye(RenderPass.CENTER).getPosition().x;
        }
        return x;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I", ordinal = 1), method = "renderSnowAndRain")
    public double vivecraft$rainY(double y) {
        if (!RenderPassType.isVanilla() && (ClientDataHolderVR.getInstance().currentPass == RenderPass.LEFT || ClientDataHolderVR.getInstance().currentPass == RenderPass.RIGHT)) {
            return ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_render.getEye(RenderPass.CENTER).getPosition().y;
        }
        return y;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I", ordinal = 2), method = "renderSnowAndRain")
    public double vivecraft$rainZ(double z) {
        if (!RenderPassType.isVanilla() && (ClientDataHolderVR.getInstance().currentPass == RenderPass.LEFT || ClientDataHolderVR.getInstance().currentPass == RenderPass.RIGHT)) {
            return ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_render.getEye(RenderPass.CENTER).getPosition().z;
        }
        return z;
    }

    @Inject(at = @At("TAIL"), method = "onResourceManagerReload")
    public void vivecraft$reinitVR(ResourceManager resourceManager, CallbackInfo ci) {
        if (VRState.vrInitialized) {
            ClientDataHolderVR.getInstance().vrRenderer.reinitFrameBuffers("Resource Reload");
        }
    }

    @Inject(at = @At("HEAD"), method = "renderLevel")
    public void vivecraft$setShaders(CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            this.vivecraft$setShaderGroup();
        }
    }

    /*
     * Start `renderLevel` lighting poll
     */

    // TODO maybe move to the ClientLevel
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;pollLightUpdates()V"), method = "renderLevel")
    public void vivecraft$onePollLightUpdates(ClientLevel instance) {
        if (RenderPassType.isVanilla() || ClientDataHolderVR.getInstance().currentPass == RenderPass.LEFT) {
            instance.pollLightUpdates();
        }
    }

    // TODO maybe move to the LevelLightEngine
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/lighting/LevelLightEngine;runLightUpdates()I"), method = "renderLevel")
    public int vivecraft$oneLightingUpdates(LevelLightEngine instance) {
        if (RenderPassType.isVanilla() || ClientDataHolderVR.getInstance().currentPass == RenderPass.LEFT) {
            return instance.runLightUpdates();
        } else {
            return 0;
        }
    }

    /*
     * End `renderLevel` lighting poll
     */

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;getRenderDistance()F", shift = Shift.BEFORE),
        method = "renderLevel")
    public void vivecraft$stencil(CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            this.minecraft.getProfiler().popPush("stencil");
            VREffectsHelper.drawEyeStencil(false);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z"), method = "renderLevel")
    public boolean vivecraft$noPlayerWhenSleeping(LivingEntity instance) {
        if (!RenderPassType.isVanilla()) {
            return false;
        } else {
            return instance.isSleeping();
        }
    }

    @Inject(at = @At("HEAD"), method = "renderEntity")
    public void vivecraft$captureEntityRestoreLoc(Entity entity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, CallbackInfo ci) {
        this.vivecraft$capturedEntity = entity;
        if (!RenderPassType.isVanilla() && vivecraft$capturedEntity == minecraft.getCameraEntity()) {
            ((GameRendererExtension) minecraft.gameRenderer).vivecraft$restoreRVEPos((LivingEntity) vivecraft$capturedEntity);
        }
        this.vivecraft$renderedEntity = vivecraft$capturedEntity;
    }

    @Inject(at = @At("TAIL"), method = "renderEntity")
    public void vivecraft$restoreLoc2(Entity entity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, CallbackInfo ci) {
        if (!RenderPassType.isVanilla() && vivecraft$capturedEntity == minecraft.getCameraEntity()) {
            ((GameRendererExtension) minecraft.gameRenderer).vivecraft$cacheRVEPos((LivingEntity) vivecraft$capturedEntity);
            ((GameRendererExtension) minecraft.gameRenderer).vivecraft$setupRVE();
        }
        this.vivecraft$renderedEntity = null;
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;", ordinal = 1), method = "renderLevel")
    public void vivecraft$interactOutline(DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci, @Local PoseStack poseStack) {
        if (!RenderPassType.isVanilla()) {
            this.level.getProfiler().popPush("interact outline");
            vivecraft$selR = vivecraft$selG = vivecraft$selB = 1f;
            Vec3 vec3 = camera.getPosition();
            double d = vec3.x();
            double e = vec3.y();
            double g = vec3.z();
            if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
                OptifineHelper.beginOutlineShader();
            }
            for (int c = 0; c < 2; c++) {
                if (ClientDataHolderVR.getInstance().interactTracker.isInteractActive(c) && (ClientDataHolderVR.getInstance().interactTracker.inBlockHit[c] != null || ClientDataHolderVR.getInstance().interactTracker.bukkit[c])) {
                    BlockPos blockpos = ClientDataHolderVR.getInstance().interactTracker.inBlockHit[c] != null ? ClientDataHolderVR.getInstance().interactTracker.inBlockHit[c].getBlockPos() : BlockPos.containing(ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_render.getController(c).getPosition());
                    BlockState blockstate = this.level.getBlockState(blockpos);
                    this.renderHitOutline(poseStack, this.renderBuffers.bufferSource().getBuffer(RenderType.lines()), camera.getEntity(), d, e, g, blockpos, blockstate);
                }
            }
            if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
                this.renderBuffers.bufferSource().endBatch(RenderType.lines());
                OptifineHelper.endOutlineShader();
            }
            // reset outline color
            vivecraft$selR = vivecraft$selG = vivecraft$selB = 0f;
        }
    }

    @ModifyVariable(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;", ordinal = 1), method = "renderLevel", ordinal = 0, argsOnly = true)
    public boolean vivecraft$noBlockoutlineOnInteract(boolean renderBlockOutline) {
        // don't draw the block outline when the interaction outline is active
        return renderBlockOutline && (RenderPassType.isVanilla() || !(ClientDataHolderVR.getInstance().interactTracker.isInteractActive(0) && (ClientDataHolderVR.getInstance().interactTracker.inBlockHit[0] != null || ClientDataHolderVR.getInstance().interactTracker.bukkit[0])));
    }

    @Unique
    private boolean vivecraft$menuHandleft;
    @Unique
    private boolean vivecraft$menuhandright;
    @Unique
    private boolean vivecraft$guiRendered = false;

    @Inject(at = @At("HEAD"), method = "renderLevel")
    public void vivecraft$resetGuiRendered(CallbackInfo ci) {
        vivecraft$guiRendered = false;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V", ordinal = 0, shift = Shift.AFTER), method = "renderLevel")
    public void vivecraft$renderVrStuffPart1(DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        if (RenderPassType.isVanilla()) {
            return;
        }
        vivecraft$menuHandleft = MethodHolder.isInMenuRoom() || this.minecraft.screen != null || KeyboardHandler.Showing;
        vivecraft$menuhandright = vivecraft$menuHandleft || ClientDataHolderVR.getInstance().interactTracker.hotbar >= 0 && ClientDataHolderVR.getInstance().vrSettings.vrTouchHotbar;

        if (transparencyChain != null) {
            VREffectsHelper.renderVRFabulous(deltaTracker.getGameTimeDeltaPartialTick(false), (LevelRenderer) (Object) this, vivecraft$menuhandright, vivecraft$menuHandleft);
        } else {
            VREffectsHelper.renderVrFast(deltaTracker.getGameTimeDeltaPartialTick(false), false, vivecraft$menuhandright, vivecraft$menuHandleft);
            if (ShadersHelper.isShaderActive() && ClientDataHolderVR.getInstance().vrSettings.shaderGUIRender == VRSettings.ShaderGUIRender.BEFORE_TRANSLUCENT_SOLID) {
                // shaders active, and render gui before translucents
                VREffectsHelper.renderVrFast(deltaTracker.getGameTimeDeltaPartialTick(false), true, vivecraft$menuhandright, vivecraft$menuHandleft);
                vivecraft$guiRendered = true;
            }
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getCloudsType()Lnet/minecraft/client/CloudStatus;", shift = Shift.BEFORE),
        method = "renderLevel")
    public void vivecraft$renderVrStuffPart2(DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        if (RenderPassType.isVanilla()) {
            return;
        }

        if (transparencyChain == null && (!ShadersHelper.isShaderActive() || ClientDataHolderVR.getInstance().vrSettings.shaderGUIRender == VRSettings.ShaderGUIRender.AFTER_TRANSLUCENT)) {
            // no shaders, or shaders, and gui after translucents
            VREffectsHelper.renderVrFast(deltaTracker.getGameTimeDeltaPartialTick(false), true, vivecraft$menuhandright, vivecraft$menuHandleft);
            vivecraft$guiRendered = true;
        }
    }

    // if the gui didn't render yet, and something canceled the level renderer, render it now.
    // or if shaders are on, and option AFTER_SHADER is selected
    @Inject(at = @At("RETURN"), method = "renderLevel")
    public void vivecraft$renderVrStuffFinal(DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        if (RenderPassType.isVanilla()) {
            return;
        }

        if (!vivecraft$guiRendered && transparencyChain == null) {
            RenderSystem.getModelViewStack().pushMatrix().identity();
            RenderHelper.applyVRModelView(ClientDataHolderVR.getInstance().currentPass, RenderSystem.getModelViewStack());
            RenderSystem.applyModelViewMatrix();

            VREffectsHelper.renderVrFast(deltaTracker.getGameTimeDeltaPartialTick(false), true, vivecraft$menuhandright, vivecraft$menuHandleft);

            RenderSystem.getModelViewStack().popMatrix();
            RenderSystem.applyModelViewMatrix();

            vivecraft$guiRendered = true;
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderShape(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/phys/shapes/VoxelShape;DDDFFFF)V"), method = "renderHitOutline")
    public void vivecraft$colorHitBox(PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
        renderShape(poseStack, vertexConsumer, voxelShape, d, e, f, this.vivecraft$selR, this.vivecraft$selG, this.vivecraft$selB, j);
    }

    @Inject(at = @At("HEAD"), method = "levelEvent")
    public void vivecraft$shakeOnSound(int i, BlockPos blockPos, int j, CallbackInfo ci) {
        boolean playerNearAndVR = VRState.vrRunning && this.minecraft.player != null && this.minecraft.player.isAlive() && this.minecraft.player.blockPosition().distSqr(blockPos) < 25.0D;
        if (playerNearAndVR) {
            switch (i) {
                /* pre 1.19.4, they are now separate
                case 1011,      // IRON_DOOR_CLOSE
                        1012,   // WOODEN_DOOR_CLOSE
                        1013,   // WOODEN_TRAPDOOR_CLOSE
                        1014,   // FENCE_GATE_CLOSE
                        1036    // IRON_TRAPDOOR_CLOSE
                        -> ClientDataHolderVR.getInstance().vr.triggerHapticPulse(0, 250);
                 */
                case 1019,      // ZOMBIE_ATTACK_WOODEN_DOOR
                    1020,   // ZOMBIE_ATTACK_IRON_DOOR
                    1021    // ZOMBIE_BREAK_WOODEN_DOOR
                    -> {
                    ClientDataHolderVR.getInstance().vr.triggerHapticPulse(0, 750);
                    ClientDataHolderVR.getInstance().vr.triggerHapticPulse(1, 750);
                }
                case 1030 ->    // ANVIL_USE
                    ClientDataHolderVR.getInstance().vr.triggerHapticPulse(0, 500);
                case 1031 -> {  // ANVIL_LAND
                    ClientDataHolderVR.getInstance().vr.triggerHapticPulse(0, 1250);
                    ClientDataHolderVR.getInstance().vr.triggerHapticPulse(1, 1250);
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = {"initOutline", "initTransparency"})
    public void vivecraft$restorePostChain(CallbackInfo ci) {
        if (VRState.vrInitialized) {
            vivecraft$restoreVanillaPostChains();
            ClientDataHolderVR.getInstance().vrRenderer.reinitFrameBuffers("Outline/Transparency shaders Reloaded");
        }
    }

    @Inject(at = @At("TAIL"), method = "initOutline")
    public void vivecraft$captureOutlineChain(CallbackInfo ci) {
        RenderPassManager.INSTANCE.vanillaOutlineChain = entityEffect;
    }

    @Inject(at = @At("TAIL"), method = "initTransparency")
    public void vivecraft$captureTransparencyChain(CallbackInfo ci) {
        RenderPassManager.INSTANCE.vanillaTransparencyChain = transparencyChain;
    }

    @Inject(at = @At("TAIL"), method = "deinitTransparency")
    public void vivecraft$removeTransparencyChain(CallbackInfo ci) {
        RenderPassManager.INSTANCE.vanillaTransparencyChain = null;
    }

    @Inject(at = @At("TAIL"), method = "close")
    public void vivecraft$removePostChains(CallbackInfo ci) {
        RenderPassManager.INSTANCE.vanillaOutlineChain = null;
        RenderPassManager.INSTANCE.vanillaTransparencyChain = null;
    }

    @Override
    @Unique
    public void vivecraft$restoreVanillaPostChains() {
        transparencyChain = RenderPassManager.INSTANCE.vanillaTransparencyChain;

        if (transparencyChain != null) {
            this.translucentTarget = transparencyChain.getTempTarget("translucent");
            this.itemEntityTarget = transparencyChain.getTempTarget("itemEntity");
            this.particlesTarget = transparencyChain.getTempTarget("particles");
            this.weatherTarget = transparencyChain.getTempTarget("weather");
            this.cloudsTarget = transparencyChain.getTempTarget("clouds");
        } else {
            this.translucentTarget = null;
            this.itemEntityTarget = null;
            this.particlesTarget = null;
            this.weatherTarget = null;
            this.cloudsTarget = null;
        }

        entityEffect = RenderPassManager.INSTANCE.vanillaOutlineChain;
        if (entityEffect != null) {
            this.entityTarget = entityEffect.getTempTarget("final");
        } else {
            this.entityTarget = null;
        }
    }

    @Unique
    private void vivecraft$setShaderGroup() {
        PostChain transparencyChain = RenderPassManager.wrp.transparencyChain;

        if (transparencyChain != null) {
            this.transparencyChain = transparencyChain;
            this.translucentTarget = transparencyChain.getTempTarget("translucent");
            this.itemEntityTarget = transparencyChain.getTempTarget("itemEntity");
            this.particlesTarget = transparencyChain.getTempTarget("particles");
            this.weatherTarget = transparencyChain.getTempTarget("weather");
            this.cloudsTarget = transparencyChain.getTempTarget("clouds");
            this.vivecraft$alphaSortVRHandsFramebuffer = transparencyChain.getTempTarget("vrhands");
            this.vivecraft$alphaSortVROccludedFramebuffer = transparencyChain.getTempTarget("vroccluded");
            this.vivecraft$alphaSortVRUnoccludedFramebuffer = transparencyChain.getTempTarget("vrunoccluded");
        } else {
            this.transparencyChain = null;
            this.translucentTarget = null;
            this.itemEntityTarget = null;
            this.particlesTarget = null;
            this.weatherTarget = null;
            this.cloudsTarget = null;
            this.vivecraft$alphaSortVRHandsFramebuffer = null;
            this.vivecraft$alphaSortVROccludedFramebuffer = null;
            this.vivecraft$alphaSortVRUnoccludedFramebuffer = null;
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
    @Unique
    public RenderTarget vivecraft$getAlphaSortVROccludedFramebuffer() {
        return vivecraft$alphaSortVROccludedFramebuffer;
    }

    @Override
    @Unique
    public RenderTarget vivecraft$getAlphaSortVRUnoccludedFramebuffer() {
        return vivecraft$alphaSortVRUnoccludedFramebuffer;
    }

    @Override
    @Unique
    public RenderTarget vivecraft$getAlphaSortVRHandsFramebuffer() {
        return vivecraft$alphaSortVRHandsFramebuffer;
    }
}
