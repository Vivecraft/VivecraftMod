package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.util.profiling.Profiler;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client.extensions.LevelRenderStateExtension;
import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_vr.render.helpers.opengl.OpenGLHelper;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.client_xr.render_pass.WorldRenderPass;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

import java.util.List;

public class VRPassHelper {

    private static final Minecraft MC = Minecraft.getInstance();
    private static final ClientDataHolderVR DATA_HOLDER = ClientDataHolderVR.getInstance();

    /**
     * renders a single RenderPass view
     *
     * @param eye          RenderPass to render
     * @param deltaTracker tracker to get the partial tick from
     * @param renderLevel  if the level should be rendered, or just the screen
     */
    public static void renderSingleView(RenderPass eye, DeltaTracker.Timer deltaTracker, boolean renderLevel) {
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
            MC.getMainRenderTarget().getColorTexture(), 0xFF000000,
            MC.getMainRenderTarget().getDepthTexture(), 1.0);

        // THIS IS WHERE EVERYTHING IS RENDERED
        // reextract world state for the new pass
        Profiler.get().push("update");
        ((GameRendererExtension) MC.gameRenderer).vivecraft$cacheRVEPos(MC.getCameraEntity());
        ((GameRendererExtension) MC.gameRenderer).vivecraft$setupRVE();
        MC.gameRenderer.update(deltaTracker, renderLevel);
        Profiler.get().popPush("extract");
        MC.gameRenderer.extract(deltaTracker, renderLevel);
        Profiler.get().pop();

        // actually render
        MC.gameRenderer.render(deltaTracker, renderLevel);

        // restore player
        ((GameRendererExtension) MC.gameRenderer).vivecraft$restoreRVEPos(MC.getCameraEntity());

        // flip buffers for the next pass, in vanilla this is only done when flipping the backbuffer
        MC.levelRenderer.endFrame();
        MC.gameRenderer.getSubmitNodeStorage().endFrame();
        MC.gameRenderer.getFeatureRenderDispatcher().endFrame();

        RenderHelper.checkGLError("post game render " + eye);

        if (ShadersHelper.isShaderActive()) {
            // some shaders don't write an alpha value to the final image
            ShaderHelper.renderFullscreenQuad(() -> "alpha clear", VRShaders.SOLID_ALPHA_PIPELINE, pass -> {},
                MC.getMainRenderTarget().getColorTextureView());
        }

        if (DATA_HOLDER.currentPass == RenderPass.LEFT || DATA_HOLDER.currentPass == RenderPass.RIGHT) {
            // copies the rendered scene to eye tex with fsaa and other postprocessing effects.
            Profiler.get().push("postProcessEye");
            RenderTarget rendertarget = MC.getMainRenderTarget();

            if (DATA_HOLDER.vrSettings.useFsaa) {
                Profiler.get().push("fsaa");
                ShaderHelper.doFSAA(DATA_HOLDER.vrRenderer.framebufferVrRender,
                    DATA_HOLDER.vrRenderer.fsaaFirstPassResultFBO,
                    DATA_HOLDER.vrRenderer.fsaaLastPassResultFBO);
                rendertarget = DATA_HOLDER.vrRenderer.fsaaLastPassResultFBO;
                RenderHelper.checkGLError("fsaa " + eye);
                Profiler.get().pop();
            }

            // do post-processing
            ShaderHelper.doVrPostProcess(eye, rendertarget,
                eye == RenderPass.LEFT ? DATA_HOLDER.vrRenderer.framebufferEye0 :
                    DATA_HOLDER.vrRenderer.framebufferEye1,
                ((LevelRenderStateExtension) MC.gameRenderer.getGameRenderState().levelRenderState).vivecraft$getVRRenderState().postProcessState);

            RenderHelper.checkGLError("post overlay" + eye);
            Profiler.get().pop();
        }

        if (DATA_HOLDER.currentPass == RenderPass.CAMERA) {
            Profiler.get().push("cameraCopy");
            ShaderHelper.blit(DATA_HOLDER.vrRenderer.cameraRenderFramebuffer, DATA_HOLDER.vrRenderer.cameraFramebuffer,
                false);
            Profiler.get().pop();
        }

        if (DATA_HOLDER.currentPass == RenderPass.THIRD &&
            DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY &&
            renderLevel && MC.level != null &&
            OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive())
        {
            // copy optifine depth buffer, since we need it for the mixed reality split
            OptifineHelper.copyOptifineShaderDepth(DATA_HOLDER.vrRenderer.framebufferMR);
        }
    }

    /**
     * renders all passes, and submits the final frames to the VR runtime
     *
     * @param renderLevel  if the level is being rendered
     * @param deltaTracker tracker to get the partial tick from
     */
    public static void renderAndSubmit(boolean renderLevel, DeltaTracker.Timer deltaTracker) {
        // still rendering
        Profiler.get().push("render");

        Profiler.get().push("VR guis");

        Profiler.get().push("gui cursor");
        // draw cursor on Gui Layer
        if (MC.screen != null || !MC.mouseHandler.isMouseGrabbed()) {
            int x = (int) (
                MC.mouseHandler.xpos() * (double) MC.getWindow().getGuiScaledWidth() /
                    (double) MC.getWindow().getScreenWidth()
            );
            int y = (int) (
                MC.mouseHandler.ypos() * (double) MC.getWindow().getGuiScaledHeight() /
                    (double) MC.getWindow().getScreenHeight()
            );
            RenderHelper.drawMouseMenuQuad(GuiRenderHelper.getGuiGraphics(), x, y);
            GuiRenderHelper.finish();
        }

        // pop pose that we pushed before the gui
        // when using quickplay, the inject that does the push somehow gets skipped so need to catch if the stack is empty
        try {
            RenderSystem.getModelViewStack().popMatrix();
        } catch (IllegalStateException ignore) {
            VRSettings.LOGGER.error("Vivecraft: ModelViewStack was empty!");
        }

        if (DATA_HOLDER.vrSettings.guiMipmaps) {
            // update mipmaps
            OpenGLHelper.genMipmaps(MC.mainRenderTarget.getColorTexture());
        }

        Profiler.get().popPush("2D Keyboard");
        if (KeyboardHandler.SHOWING && !DATA_HOLDER.vrSettings.physicalKeyboard) {
            MC.mainRenderTarget = KeyboardHandler.FRAMEBUFFER;
            RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
                KeyboardHandler.FRAMEBUFFER.getColorTexture(), 0x00000000,
                KeyboardHandler.FRAMEBUFFER.getDepthTexture(), 1.0);
            RenderHelper.drawScreen(KeyboardHandler.UI, true);
        }

        Profiler.get().popPush("Radial Menu");
        if (RadialHandler.isShowing()) {
            MC.mainRenderTarget = RadialHandler.FRAMEBUFFER;
            RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
                RadialHandler.FRAMEBUFFER.getColorTexture(), 0x00000000,
                RadialHandler.FRAMEBUFFER.getDepthTexture(), 1.0);
            RenderHelper.drawScreen(RadialHandler.UI, true);
        }
        Profiler.get().pop();
        RenderHelper.checkGLError("post 2d ");

        // done with guis
        Profiler.get().pop();

        // resize happened in the gui pass, set it to false or it will mess with stuff
        MC.getWindow().resetIsResized();
        // don't reextract the gui for the world passes
        ((GameRendererExtension) MC.gameRenderer).vivecraft$setShouldDrawScreen(false);

        // render the different vr passes
        List<RenderPass> list = DATA_HOLDER.vrRenderer.getRenderPasses(false);
        DATA_HOLDER.isFirstPass = true;
        for (RenderPass renderpass : list) {
            DATA_HOLDER.currentPass = renderpass;

            if (DATA_HOLDER.vrSettings.displayMirrorUseScreenshotCamera && DATA_HOLDER.cameraTracker.isVisible()) {
                if (renderpass == RenderPass.CENTER) {
                    continue;
                } else if (renderpass == RenderPass.THIRD &&
                    DATA_HOLDER.vrSettings.displayMirrorMode != VRSettings.MirrorMode.MIXED_REALITY)
                {
                    continue;
                }
            }

            switch (renderpass) {
                case LEFT, RIGHT -> RenderPassManager.setWorldRenderPass(WorldRenderPass.STEREO_XR);
                case CENTER -> RenderPassManager.setWorldRenderPass(WorldRenderPass.CENTER);
                case THIRD -> RenderPassManager.setWorldRenderPass(WorldRenderPass.MIXED_REALITY);
                case SCOPEL -> RenderPassManager.setWorldRenderPass(WorldRenderPass.LEFT_TELESCOPE);
                case SCOPER -> RenderPassManager.setWorldRenderPass(WorldRenderPass.RIGHT_TELESCOPE);
                case CAMERA -> RenderPassManager.setWorldRenderPass(WorldRenderPass.CAMERA);
            }

            Profiler.get().push("Eye:" + DATA_HOLDER.currentPass);
            Profiler.get().push("setup");
            Profiler.get().pop();
            VRPassHelper.renderSingleView(renderpass, deltaTracker, renderLevel);
            Profiler.get().pop();

            if (DATA_HOLDER.grabScreenShot) {
                boolean flag;

                if (list.contains(RenderPass.CAMERA)) {
                    flag = renderpass == RenderPass.CAMERA;
                } else if (list.contains(RenderPass.CENTER)) {
                    flag = renderpass == RenderPass.CENTER;
                } else {
                    flag = DATA_HOLDER.vrSettings.displayMirrorLeftEye ?
                        renderpass == RenderPass.LEFT :
                        renderpass == RenderPass.RIGHT;
                }

                if (flag) {
                    RenderTarget rendertarget = MC.mainRenderTarget;

                    if (renderpass == RenderPass.CAMERA) {
                        rendertarget = DATA_HOLDER.vrRenderer.cameraFramebuffer;
                    }

                    ClientUtils.takeScreenshot(rendertarget);
                    RenderSystem.flipFrame(null);
                    DATA_HOLDER.grabScreenShot = false;
                }
            }

            DATA_HOLDER.isFirstPass = false;
        }
        // now we are done with rendering
        Profiler.get().pop();

        DATA_HOLDER.vrPlayer.postRender(deltaTracker.getGameTimeDeltaPartialTick(true));
        Profiler.get().push("Display/Reproject");

        try {
            DATA_HOLDER.vrRenderer.endFrame();
        } catch (RenderConfigException exception) {
            VRSettings.LOGGER.error("Vivecraft: error ending frame: {}", exception.error.getString());
        }
        Profiler.get().pop();
        RenderHelper.checkGLError("post submit");
    }
}
