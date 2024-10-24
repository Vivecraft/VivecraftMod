package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL30C;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.extensions.MinecraftExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.client_xr.render_pass.WorldRenderPass;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

import java.util.List;

public class VRPassHelper {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();

    /**
     * renders a single RenderPass view
     * @param eye RenderPass to render
     * @param partialTick current partial tick for this frame
     * @param nanoTime time of this frame in nanoseconds
     * @param renderLevel if the level should be rendered, or just the screen
     */
    public static void renderSingleView(RenderPass eye, float partialTick, long nanoTime, boolean renderLevel) {
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
        RenderSystem.clear(GL13C.GL_COLOR_BUFFER_BIT | GL13C.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.enableDepthTest();

        // THIS IS WHERE EVERYTHING IS RENDERED
        mc.gameRenderer.render(partialTick, nanoTime, renderLevel);

        RenderHelper.checkGLError("post game render " + eye);

        if (dataHolder.currentPass == RenderPass.LEFT || dataHolder.currentPass == RenderPass.RIGHT) {
            // copies the rendered scene to eye tex with fsaa and other postprocessing effects.
            mc.getProfiler().push("postProcessEye");
            RenderTarget rendertarget = mc.getMainRenderTarget();

            if (dataHolder.vrSettings.useFsaa) {
                mc.getProfiler().push("fsaa");
                ShaderHelper.doFSAA(dataHolder.vrRenderer.framebufferVrRender,
                    dataHolder.vrRenderer.fsaaFirstPassResultFBO,
                    dataHolder.vrRenderer.fsaaLastPassResultFBO);
                rendertarget = dataHolder.vrRenderer.fsaaLastPassResultFBO;
                RenderHelper.checkGLError("fsaa " + eye);
                mc.getProfiler().pop();
            }

            if (eye == RenderPass.LEFT) {
                dataHolder.vrRenderer.framebufferEye0.bindWrite(true);
            } else {
                dataHolder.vrRenderer.framebufferEye1.bindWrite(true);
            }

            // do post-processing
            ShaderHelper.doVrPostProcess(eye, rendertarget, partialTick);

            RenderHelper.checkGLError("post overlay" + eye);
            mc.getProfiler().pop();
        }

        if (dataHolder.currentPass == RenderPass.CAMERA) {
            mc.getProfiler().push("cameraCopy");
            dataHolder.vrRenderer.cameraFramebuffer.bindWrite(true);
            RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
            RenderSystem.clear(GL13C.GL_COLOR_BUFFER_BIT | GL13C.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
            dataHolder.vrRenderer.cameraRenderFramebuffer.blitToScreen(
                dataHolder.vrRenderer.cameraFramebuffer.viewWidth,
                dataHolder.vrRenderer.cameraFramebuffer.viewHeight);
            mc.getProfiler().pop();
        }

        if (dataHolder.currentPass == RenderPass.THIRD &&
            dataHolder.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY &&
            renderLevel && mc.level != null &&
            OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive() &&
            OptifineHelper.bindShaderFramebuffer())
        {
            // copy optifine depth buffer, since we need it for the mixed reality split
            RenderSystem.activeTexture(GL13C.GL_TEXTURE0);
            RenderSystem.bindTexture(dataHolder.vrRenderer.framebufferMR.getDepthTextureId());
            RenderHelper.checkGLError("pre copy depth");
            GlStateManager._glCopyTexSubImage2D(GL13C.GL_TEXTURE_2D, 0, 0, 0, 0, 0, dataHolder.vrRenderer.framebufferMR.width, dataHolder.vrRenderer.framebufferMR.height);
            RenderHelper.checkGLError("post copy depth");
            // rebind the original buffer
            dataHolder.vrRenderer.framebufferMR.bindWrite(false);
        }
    }

    public static void renderAndSubmit(boolean renderLevel, long nanoTime, float actualPartialTick) {
        // still rendering
        mc.getProfiler().push("gameRenderer");

        mc.getProfiler().push("VR guis");

        // some mods mess with the depth mask?
        RenderSystem.depthMask(true);
        // some mods mess with the backface culling?
        RenderSystem.enableCull();

        // to render gui stuff
        GuiGraphics guiGraphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());

        mc.getProfiler().push("gui cursor");
        // draw cursor on Gui Layer
        if (mc.screen != null || !mc.mouseHandler.isMouseGrabbed()) {
            PoseStack poseStack = RenderSystem.getModelViewStack();
            poseStack.pushPose();
            poseStack.setIdentity();
            poseStack.translate(0.0f, 0.0f, -11000.0f);
            RenderSystem.applyModelViewMatrix();

            Matrix4f guiProjection = (new Matrix4f()).setOrtho(
                0.0F, mc.getWindow().getGuiScaledWidth(),
                mc.getWindow().getGuiScaledHeight(), 0.0F,
                1000.0F, 21000.0F);
            RenderSystem.setProjectionMatrix(guiProjection, VertexSorting.ORTHOGRAPHIC_Z);

            int x = (int) (mc.mouseHandler.xpos() * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getScreenWidth());
            int y = (int) (mc.mouseHandler.ypos() * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getScreenHeight());
            RenderHelper.drawMouseMenuQuad(guiGraphics, x, y);

            guiGraphics.flush();
            poseStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }

        mc.getProfiler().popPush("fps pie");
        // draw debug pie
        ((MinecraftExtension) mc).vivecraft$drawProfiler();

        // pop pose that we pushed before the gui
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();

        if (dataHolder.vrSettings.guiMipmaps) {
            // update mipmaps
            mc.mainRenderTarget.bindRead();
            GL30C.glGenerateMipmap(GL30C.GL_TEXTURE_2D);
            mc.mainRenderTarget.unbindRead();
        }

        mc.getProfiler().popPush("2D Keyboard");
        if (KeyboardHandler.Showing && !dataHolder.vrSettings.physicalKeyboard) {
            mc.mainRenderTarget = KeyboardHandler.Framebuffer;
            mc.mainRenderTarget.clear(Minecraft.ON_OSX);
            mc.mainRenderTarget.bindWrite(true);
            RenderHelper.drawScreen(guiGraphics, actualPartialTick, KeyboardHandler.UI, true);
            guiGraphics.flush();
        }

        mc.getProfiler().popPush("Radial Menu");
        if (RadialHandler.isShowing()) {
            mc.mainRenderTarget = RadialHandler.Framebuffer;
            mc.mainRenderTarget.clear(Minecraft.ON_OSX);
            mc.mainRenderTarget.bindWrite(true);
            RenderHelper.drawScreen(guiGraphics, actualPartialTick, RadialHandler.UI, true);
            guiGraphics.flush();
        }
        mc.getProfiler().pop();
        RenderHelper.checkGLError("post 2d ");

        // done with guis
        mc.getProfiler().pop();

        // render the different vr passes
        List<RenderPass> list = dataHolder.vrRenderer.getRenderPasses();
        dataHolder.isFirstPass = true;
        for (RenderPass renderpass : list) {
            dataHolder.currentPass = renderpass;

            if (dataHolder.vrSettings.displayMirrorUseScreenshotCamera && dataHolder.cameraTracker.isVisible()) {
                if (renderpass == RenderPass.CENTER) {
                    continue;
                } else if (renderpass == RenderPass.THIRD && dataHolder.vrSettings.displayMirrorMode != VRSettings.MirrorMode.MIXED_REALITY) {
                    continue;
                }
            }

            switch (renderpass) {
                case LEFT, RIGHT -> RenderPassManager.setWorldRenderPass(WorldRenderPass.stereoXR);
                case CENTER -> RenderPassManager.setWorldRenderPass(WorldRenderPass.center);
                case THIRD -> RenderPassManager.setWorldRenderPass(WorldRenderPass.mixedReality);
                case SCOPEL -> RenderPassManager.setWorldRenderPass(WorldRenderPass.leftTelescope);
                case SCOPER -> RenderPassManager.setWorldRenderPass(WorldRenderPass.rightTelescope);
                case CAMERA -> RenderPassManager.setWorldRenderPass(WorldRenderPass.camera);
            }

            mc.getProfiler().push("Eye:" + dataHolder.currentPass);
            mc.getProfiler().push("setup");
            mc.mainRenderTarget.bindWrite(true);
            mc.getProfiler().pop();
            VRPassHelper.renderSingleView(renderpass, actualPartialTick, nanoTime, renderLevel);
            mc.getProfiler().pop();

            if (dataHolder.grabScreenShot) {
                boolean flag;

                if (list.contains(RenderPass.CAMERA)) {
                    flag = renderpass == RenderPass.CAMERA;
                } else if (list.contains(RenderPass.CENTER)) {
                    flag = renderpass == RenderPass.CENTER;
                } else {
                    flag = dataHolder.vrSettings.displayMirrorLeftEye ?
                           renderpass == RenderPass.LEFT :
                           renderpass == RenderPass.RIGHT;
                }

                if (flag) {
                    RenderTarget rendertarget = mc.mainRenderTarget;

                    if (renderpass == RenderPass.CAMERA) {
                        rendertarget = dataHolder.vrRenderer.cameraFramebuffer;
                    }

                    mc.mainRenderTarget.unbindWrite();
                    Utils.takeScreenshot(rendertarget);
                    mc.getWindow().updateDisplay();
                    dataHolder.grabScreenShot = false;
                }
            }

            dataHolder.isFirstPass = false;
        }
        // now we are done with rendering
        mc.getProfiler().pop();

        dataHolder.vrPlayer.postRender(actualPartialTick);
        mc.getProfiler().push("Display/Reproject");

        try {
            dataHolder.vrRenderer.endFrame();
        } catch (RenderConfigException exception) {
            VRSettings.logger.error("Vivecraft: error ending frame: {}", exception.error);
        }
        mc.getProfiler().pop();
        RenderHelper.checkGLError("post submit");
    }
}
