package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.vivecraft.client.Xplat;
import org.vivecraft.client.extensions.RenderTargetExtension;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.mod_compat_vr.iris.IrisHelper;

public class VRPassHelper {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();

    private static float fovReduction = 1.0F;

    public static void renderSingleView(RenderPass eye, float partialTicks, long nanoTime, boolean renderWorld) {
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
        RenderSystem.clear(16384, Minecraft.ON_OSX);
        RenderSystem.enableDepthTest();
        mc.getProfiler().push("updateCameraAndRender");
        mc.gameRenderer.render(partialTicks, nanoTime, renderWorld);
        mc.getProfiler().pop();
        checkGLError("post game render " + eye.name());

        if (dataHolder.currentPass == RenderPass.LEFT || dataHolder.currentPass == RenderPass.RIGHT) {
            mc.getProfiler().push("postProcessEye");
            RenderTarget rendertarget = mc.getMainRenderTarget();

            if (dataHolder.vrSettings.useFsaa) {
                RenderSystem.clearColor(RenderSystem.getShaderFogColor()[0], RenderSystem.getShaderFogColor()[1], RenderSystem.getShaderFogColor()[2], RenderSystem.getShaderFogColor()[3]);
                if (eye == RenderPass.LEFT) {
                    dataHolder.vrRenderer.framebufferEye0.bindWrite(true);
                } else {
                    dataHolder.vrRenderer.framebufferEye1.bindWrite(true);
                }
                RenderSystem.clear(16384, Minecraft.ON_OSX);
                mc.getProfiler().push("fsaa");
                dataHolder.vrRenderer.doFSAA(false);
                rendertarget = dataHolder.vrRenderer.fsaaLastPassResultFBO;
                checkGLError("fsaa " + eye.name());
                mc.getProfiler().pop();
            }

            if (eye == RenderPass.LEFT) {
                dataHolder.vrRenderer.framebufferEye0.bindWrite(true);
            } else {
                dataHolder.vrRenderer.framebufferEye1.bindWrite(true);
            }

            if (dataHolder.vrSettings.useFOVReduction
                && dataHolder.vrPlayer.getFreeMove()) {
                if (mc.player != null && (Math.abs(mc.player.zza) > 0.0F || Math.abs(mc.player.xxa) > 0.0F)) {
                    fovReduction = fovReduction - 0.05F;

                    if (fovReduction < dataHolder.vrSettings.fovReductionMin) {
                        fovReduction = dataHolder.vrSettings.fovReductionMin;
                    }
                } else {
                    fovReduction = fovReduction + 0.01F;

                    if (fovReduction > 0.8F) {
                        fovReduction = 0.8F;
                    }
                }
            } else {
                fovReduction = 1.0F;
            }

            VRShaders._FOVReduction_OffsetUniform.set(
                dataHolder.vrSettings.fovRedutioncOffset);
            float red = 0.0F;
            float black = 0.0F;
            float blue = 0.0F;
            float time = (float) Util.getMillis() / 1000.0F;

            if (mc.player != null && mc.level != null) {
                if (((GameRendererExtension) mc.gameRenderer)
                    .vivecraft$wasInWater() != ((GameRendererExtension) mc.gameRenderer).vivecraft$isInWater()) {
                    dataHolder.watereffect = 2.3F;
                } else {
                    if (((GameRendererExtension) mc.gameRenderer).vivecraft$isInWater()) {
                        dataHolder.watereffect -= 0.008333334F;
                    } else {
                        dataHolder.watereffect -= 0.016666668F;
                    }

                    if (dataHolder.watereffect < 0.0F) {
                        dataHolder.watereffect = 0.0F;
                    }
                }

                ((GameRendererExtension) mc.gameRenderer)
                    .vivecraft$setWasInWater(((GameRendererExtension) mc.gameRenderer).vivecraft$isInWater());

                if (Xplat
                    .isModLoaded("iris") || Xplat.isModLoaded("oculus")) {
                    if (!IrisHelper.hasWaterEffect()) {
                        dataHolder.watereffect = 0.0F;
                    }
                }

                if (((GameRendererExtension) mc.gameRenderer).vivecraft$isInPortal()) {
                    dataHolder.portaleffect = 1.0F;
                } else {
                    dataHolder.portaleffect -= 0.016666668F;

                    if (dataHolder.portaleffect < 0.0F) {
                        dataHolder.portaleffect = 0.0F;
                    }
                }

                ItemStack itemstack = mc.player.getInventory().getArmor(3);

                if (itemstack.getItem() == Blocks.CARVED_PUMPKIN.asItem()
                    && (!itemstack.hasTag() || itemstack.getTag().getInt("CustomModelData") == 0)) {
                    dataHolder.pumpkineffect = 1.0F;
                } else {
                    dataHolder.pumpkineffect = 0.0F;
                }

                float hurtTimer = (float) mc.player.hurtTime - partialTicks;
                float healthPercent = 1.0F - mc.player.getHealth() / mc.player.getMaxHealth();
                healthPercent = (healthPercent - 0.5F) * 0.75F;

                if (hurtTimer > 0.0F) { // hurt flash
                    hurtTimer = hurtTimer / (float) mc.player.hurtDuration;
                    hurtTimer = healthPercent + Mth.sin(hurtTimer * hurtTimer * hurtTimer * hurtTimer * (float) Math.PI) * 0.5F;
                    red = hurtTimer;
                } else if (dataHolder.vrSettings.low_health_indicator) { // red due to low health
                    red = healthPercent * Mth.abs(Mth.sin((2.5F * time) / (1.0F - healthPercent + 0.1F)));

                    if (mc.player.isCreative()) {
                        red = 0.0F;
                    }
                }

                float freeze = mc.player.getPercentFrozen();
                if (freeze > 0) {
                    blue = red;
                    blue = Math.max(freeze / 2, blue);
                    red = 0;
                }

                if (mc.player.isSleeping()) {
                    black = 0.5F + 0.3F * mc.player.getSleepTimer() * 0.01F;
                }

                if (dataHolder.vr.isWalkingAbout && black < 0.8F) {
                    black = 0.5F;
                }
            } else {
                dataHolder.watereffect = 0.0F;
                dataHolder.portaleffect = 0.0F;
                dataHolder.pumpkineffect = 0.0F;
            }

            if (dataHolder.pumpkineffect > 0.0F) {
                VRShaders._FOVReduction_RadiusUniform.set(0.3F);
                VRShaders._FOVReduction_BorderUniform.set(0.0F);
            } else {
                VRShaders._FOVReduction_RadiusUniform.set(fovReduction);
                VRShaders._FOVReduction_BorderUniform.set(0.06F);
            }

            VRShaders._Overlay_HealthAlpha.set(red);
            VRShaders._Overlay_FreezeAlpha.set(blue);
            VRShaders._Overlay_BlackAlpha.set(black);
            VRShaders._Overlay_time.set(time);
            VRShaders._Overlay_waterAmplitude.set(dataHolder.watereffect);
            VRShaders._Overlay_portalAmplitutde.set(dataHolder.portaleffect);
            VRShaders._Overlay_pumpkinAmplitutde.set(dataHolder.pumpkineffect);

            VRShaders._Overlay_eye.set(dataHolder.currentPass == RenderPass.LEFT ? 1 : -1);
            ((RenderTargetExtension) rendertarget).vivecraft$blitFovReduction(VRShaders.fovReductionShader, dataHolder.vrRenderer.framebufferEye0.viewWidth, dataHolder.vrRenderer.framebufferEye0.viewHeight);
            GlStateManager._glUseProgram(0);
            checkGLError("post overlay" + eye);
            mc.getProfiler().pop();
        }

        if (dataHolder.currentPass == RenderPass.CAMERA) {
            mc.getProfiler().push("cameraCopy");
            dataHolder.vrRenderer.cameraFramebuffer.bindWrite(true);
            RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
            RenderSystem.clear(16640, Minecraft.ON_OSX);
            ((RenderTargetExtension) dataHolder.vrRenderer.cameraRenderFramebuffer).vivecraft$blitToScreen(0,
                dataHolder.vrRenderer.cameraFramebuffer.viewWidth,
                dataHolder.vrRenderer.cameraFramebuffer.viewHeight, 0, true, 0.0F, 0.0F, false);
            mc.getProfiler().pop();
        }
    }

    private static void checkGLError(String string) {
        if (GlStateManager._getError() != 0) {
            System.err.println(string);
        }
    }
}
