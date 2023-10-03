package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.vivecraft.client.Xplat;
import org.vivecraft.client.extensions.RenderTargetExtension;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_vr.settings.VRSettings.MirrorMode;
import org.vivecraft.mod_compat_vr.iris.IrisHelper;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

import static java.lang.Math.pow;
import static net.minecraft.client.Minecraft.ON_OSX;
import static org.joml.Math.*;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.logger;

public class VRPassHelper {

    private static float fovReduction = 1.0F;

    public static void renderSingleView(RenderPass eye, float partialTicks, long nanoTime, boolean renderWorld) {
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
        RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT, ON_OSX);
        RenderSystem.enableDepthTest();
        mc.getProfiler().push("updateCameraAndRender");
        mc.gameRenderer.render(partialTicks, nanoTime, renderWorld);
        mc.getProfiler().pop();
        checkGLError("post game render " + eye.name());

        switch (eye) {
            case LEFT, RIGHT -> {
                mc.getProfiler().push("postProcessEye");
                RenderTarget rendertarget = mc.getMainRenderTarget();

                if (dh.vrSettings.useFsaa) {
                    RenderSystem.clearColor(RenderSystem.getShaderFogColor()[0], RenderSystem.getShaderFogColor()[1], RenderSystem.getShaderFogColor()[2], RenderSystem.getShaderFogColor()[3]);
                    if (eye == RenderPass.LEFT) {
                        dh.vrRenderer.framebufferEye0.bindWrite(true);
                    } else {
                        dh.vrRenderer.framebufferEye1.bindWrite(true);
                    }
                    RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT, ON_OSX);
                    mc.getProfiler().push("fsaa");
                    dh.vrRenderer.doFSAA(false);
                    rendertarget = dh.vrRenderer.fsaaLastPassResultFBO;
                    checkGLError("fsaa " + eye.name());
                    mc.getProfiler().pop();
                }

                if (eye == RenderPass.LEFT) {
                    dh.vrRenderer.framebufferEye0.bindWrite(true);
                } else {
                    dh.vrRenderer.framebufferEye1.bindWrite(true);
                }

                if (dh.vrSettings.useFOVReduction && dh.vrPlayer.getFreeMove()) {
                    if (mc.player != null && (abs(mc.player.zza) > 0.0F || abs(mc.player.xxa) > 0.0F)) {
                        fovReduction -= 0.05F;

                        if (fovReduction < dh.vrSettings.fovReductionMin) {
                            fovReduction = dh.vrSettings.fovReductionMin;
                        }
                    } else {
                        fovReduction += 0.01F;

                        if (fovReduction > 0.8F) {
                            fovReduction = 0.8F;
                        }
                    }
                } else {
                    fovReduction = 1.0F;
                }

                VRShaders._FOVReduction_OffsetUniform.set(dh.vrSettings.fovRedutioncOffset);
                float red = 0.0F;
                float black = 0.0F;
                float blue = 0.0F;
                float time = Util.getMillis() / 1000.0F;

                if (mc.player != null && mc.level != null) {
                    GameRendererExtension GRE = (GameRendererExtension) mc.gameRenderer;
                    if (GRE.vivecraft$wasInWater() != GRE.vivecraft$isInWater()) {
                        dh.watereffect = 2.3F;
                    } else {
                        if (GRE.vivecraft$isInWater()) {
                            dh.watereffect -= 0.008333334F;
                        } else {
                            dh.watereffect -= 0.016666668F;
                        }

                        if (dh.watereffect < 0.0F) {
                            dh.watereffect = 0.0F;
                        }
                    }

                    GRE.vivecraft$setWasInWater(GRE.vivecraft$isInWater());

                    if (Xplat.isModLoaded("iris") || Xplat.isModLoaded("oculus")) {
                        if (!IrisHelper.hasWaterEffect()) {
                            dh.watereffect = 0.0F;
                        }
                    }

                    if (GRE.vivecraft$isInPortal()) {
                        dh.portaleffect = 1.0F;
                    } else {
                        dh.portaleffect -= 0.016666668F;

                        if (dh.portaleffect < 0.0F) {
                            dh.portaleffect = 0.0F;
                        }
                    }

                    ItemStack itemstack = mc.player.getInventory().getArmor(3);

                    if (itemstack.getItem() == Blocks.CARVED_PUMPKIN.asItem()
                        && (!itemstack.hasTag() || itemstack.getTag().getInt("CustomModelData") == 0)) {
                        dh.pumpkineffect = 1.0F;
                    } else {
                        dh.pumpkineffect = 0.0F;
                    }

                    float hurtTimer = mc.player.hurtTime - partialTicks;
                    float healthPercent = 1.0F - mc.player.getHealth() / mc.player.getMaxHealth();
                    healthPercent = (healthPercent - 0.5F) * 0.75F;

                    if (hurtTimer > 0.0F) { // hurt flash
                        hurtTimer = hurtTimer / mc.player.hurtDuration;
                        hurtTimer = fma(sin((float) pow(hurtTimer, 4) * (float) PI), 0.5F, healthPercent);
                        red = hurtTimer;
                    } else if (dh.vrSettings.low_health_indicator) { // red due to low health
                        red = healthPercent * abs(sin((2.5F * time) / (1.0F - healthPercent + 0.1F)));

                        if (mc.player.isCreative()) {
                            red = 0.0F;
                        }
                    }

                    float freeze = mc.player.getPercentFrozen();
                    if (freeze > 0) {
                        blue = red;
                        blue = max(freeze / 2, blue);
                        red = 0;
                    }

                    if (mc.player.isSleeping()) {
                        black = 0.5F + 0.3F * mc.player.getSleepTimer() * 0.01F;
                    }

                    if (dh.vr.isWalkingAbout && black < 0.8F) {
                        black = 0.5F;
                    }
                } else {
                    dh.watereffect = 0.0F;
                    dh.portaleffect = 0.0F;
                    dh.pumpkineffect = 0.0F;
                }

                if (dh.pumpkineffect > 0.0F) {
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
                VRShaders._Overlay_waterAmplitude.set(dh.watereffect);
                VRShaders._Overlay_portalAmplitutde.set(dh.portaleffect);
                VRShaders._Overlay_pumpkinAmplitutde.set(dh.pumpkineffect);

                VRShaders._Overlay_eye.set(eye == RenderPass.LEFT ? 1 : -1);
                ((RenderTargetExtension) rendertarget).vivecraft$blitFovReduction(
                    VRShaders.fovReductionShader,
                    dh.vrRenderer.framebufferEye0.viewWidth,
                    dh.vrRenderer.framebufferEye0.viewHeight
                );
                ProgramManager.glUseProgram(0);
                checkGLError("post overlay" + eye);
                mc.getProfiler().pop();
            }
            case CAMERA -> {
                mc.getProfiler().push("cameraCopy");
                dh.vrRenderer.cameraFramebuffer.bindWrite(true);
                RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
                RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT, ON_OSX);
                ((RenderTargetExtension) dh.vrRenderer.cameraRenderFramebuffer).vivecraft$blitToScreen(
                    0,
                    dh.vrRenderer.cameraFramebuffer.viewWidth,
                    dh.vrRenderer.cameraFramebuffer.viewHeight,
                    0,
                    true,
                    0.0F,
                    0.0F,
                    false
                );
                mc.getProfiler().pop();
            }
            case THIRD -> {
                if (dh.vrSettings.displayMirrorMode == MirrorMode.MIXED_REALITY && OptifineHelper.isOptifineLoaded() &&
                    renderWorld && mc.level != null && OptifineHelper.isShaderActive() &&
                    OptifineHelper.bindShaderFramebuffer()
                ) {
                    // copy optifine depth buffer, since we need it for the mixed reality split
                    RenderSystem.activeTexture(GL13C.GL_TEXTURE0);
                    RenderSystem.bindTexture(dh.vrRenderer.framebufferMR.getDepthTextureId());
                    checkGLError("pre copy depth");
                    GlStateManager._glCopyTexSubImage2D(GL13C.GL_TEXTURE_2D, 0, 0, 0, 0, 0, dh.vrRenderer.framebufferMR.width, dh.vrRenderer.framebufferMR.height);
                    checkGLError("post copy depth");
                    // rebind the original buffer
                    dh.vrRenderer.framebufferMR.bindWrite(false);
                }
            }
        }
    }

    public static void checkGLError(String string) {
        int i = GlStateManager._getError();
        if (i != 0) {
            logger.error("{}: {}", string, i);
        }
    }
}
