package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL13C;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.extensions.MinecraftExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.render.RenderConfigException;
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
     * @param eye         RenderPass to render
     * @param partialTick current partial tick for this frame
     * @param nanoTime    time of this frame in nanoseconds
     * @param renderLevel if the level should be rendered, or just the screen
     */
    public static void renderSingleView(RenderPass eye, float partialTick, long nanoTime, boolean renderLevel) {
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
        RenderSystem.clear(GL13C.GL_COLOR_BUFFER_BIT | GL13C.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.enableDepthTest();

        // some mods mess with these
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();

        // THIS IS WHERE EVERYTHING IS RENDERED
        MC.gameRenderer.render(partialTick, nanoTime, renderLevel);

        RenderHelper.checkGLError("post game render " + eye);

        if (ShadersHelper.isShaderActive()) {
            // some shaders don't write an alpha value to the final image
            RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
            RenderSystem.colorMask(false, false, false, true);
            RenderSystem.clear(GL13C.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);
            RenderSystem.colorMask(true, true, true, true);
        }

        if (DATA_HOLDER.currentPass == RenderPass.LEFT || DATA_HOLDER.currentPass == RenderPass.RIGHT) {
            // copies the rendered scene to eye tex with fsaa and other postprocessing effects.
            MC.getProfiler().push("postProcessEye");
            RenderTarget rendertarget = MC.getMainRenderTarget();

            if (DATA_HOLDER.vrSettings.useFsaa) {
                MC.getProfiler().push("fsaa");
                ShaderHelper.doFSAA(DATA_HOLDER.vrRenderer.framebufferVrRender,
                    DATA_HOLDER.vrRenderer.fsaaFirstPassResultFBO,
                    DATA_HOLDER.vrRenderer.fsaaLastPassResultFBO);
                rendertarget = DATA_HOLDER.vrRenderer.fsaaLastPassResultFBO;
                RenderHelper.checkGLError("fsaa " + eye);
                MC.getProfiler().pop();
            }

            if (eye == RenderPass.LEFT) {
                DATA_HOLDER.vrRenderer.framebufferEye0.bindWrite(true);
            } else {
                DATA_HOLDER.vrRenderer.framebufferEye1.bindWrite(true);
            }

            // do post-processing
            ShaderHelper.doVrPostProcess(eye, rendertarget, partialTick);

            RenderHelper.checkGLError("post overlay" + eye);
            MC.getProfiler().pop();
        }

        if (DATA_HOLDER.currentPass == RenderPass.CAMERA) {
            MC.getProfiler().push("cameraCopy");
            DATA_HOLDER.vrRenderer.cameraFramebuffer.bindWrite(true);
            ShaderHelper.blit(DATA_HOLDER.vrRenderer.cameraRenderFramebuffer, false);
            MC.getProfiler().pop();
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
     * @param renderLevel       if the level is being rendered
     * @param nanoTime          nano time for the current frame
     * @param actualPartialTick partial tick for rendering
     */
    public static void renderAndSubmit(boolean renderLevel, long nanoTime, float actualPartialTick) {
        // still rendering
        MC.getProfiler().push("gameRenderer");

        MC.getProfiler().push("VR guis");

        // some mods mess with the depth mask?
        RenderSystem.depthMask(true);
        // some mods mess with the backface culling?
        RenderSystem.enableCull();

        // to render gui stuff
        GuiGraphics guiGraphics = new GuiGraphics(MC, MC.renderBuffers().bufferSource());

        MC.getProfiler().push("gui cursor");
        // draw cursor on Gui Layer
        if (MC.screen != null || !MC.mouseHandler.isMouseGrabbed()) {
            PoseStack poseStack = RenderSystem.getModelViewStack();
            poseStack.pushPose();
            poseStack.setIdentity();
            poseStack.translate(0.0f, 0.0f, -11000.0f);
            RenderSystem.applyModelViewMatrix();

            Matrix4f guiProjection = (new Matrix4f()).setOrtho(
                0.0F, MC.getWindow().getGuiScaledWidth(),
                MC.getWindow().getGuiScaledHeight(), 0.0F,
                1000.0F, 21000.0F);
            RenderSystem.setProjectionMatrix(guiProjection, VertexSorting.ORTHOGRAPHIC_Z);

            int x = (int) (
                MC.mouseHandler.xpos() * (double) MC.getWindow().getGuiScaledWidth() /
                    (double) MC.getWindow().getScreenWidth()
            );
            int y = (int) (
                MC.mouseHandler.ypos() * (double) MC.getWindow().getGuiScaledHeight() /
                    (double) MC.getWindow().getScreenHeight()
            );
            RenderHelper.drawMouseMenuQuad(guiGraphics, x, y);

            guiGraphics.flush();
            poseStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }

        MC.getProfiler().popPush("fps pie");
        // draw debug pie
        ((MinecraftExtension) MC).vivecraft$drawProfiler();

        // pop pose that we pushed before the gui
        // when using quickplay, the inject that does the push somehow gets skipped so need to catch if the stack is empty
        if (!RenderSystem.getModelViewStack().clear()) {
            RenderSystem.getModelViewStack().popPose();
        }
        RenderSystem.applyModelViewMatrix();

        if (DATA_HOLDER.vrSettings.guiMipmaps) {
            // update mipmaps
            OpenGLHelper.genMipmaps(MC.mainRenderTarget);
        }

        MC.getProfiler().popPush("2D Keyboard");
        if (KeyboardHandler.SHOWING && !DATA_HOLDER.vrSettings.physicalKeyboard) {
            MC.mainRenderTarget = KeyboardHandler.FRAMEBUFFER;
            MC.mainRenderTarget.clear(Minecraft.ON_OSX);
            MC.mainRenderTarget.bindWrite(true);
            RenderHelper.drawScreen(guiGraphics, actualPartialTick, KeyboardHandler.UI, true);
        }

        MC.getProfiler().popPush("Radial Menu");
        if (RadialHandler.isShowing()) {
            MC.mainRenderTarget = RadialHandler.FRAMEBUFFER;
            MC.mainRenderTarget.clear(Minecraft.ON_OSX);
            MC.mainRenderTarget.bindWrite(true);
            RenderHelper.drawScreen(guiGraphics, actualPartialTick, RadialHandler.UI, true);
        }
        MC.getProfiler().pop();
        RenderHelper.checkGLError("post 2d ");

        // done with guis
        MC.getProfiler().pop();

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

            MC.getProfiler().push("Eye:" + DATA_HOLDER.currentPass);
            MC.getProfiler().push("setup");
            MC.mainRenderTarget.bindWrite(true);
            MC.getProfiler().pop();
            VRPassHelper.renderSingleView(renderpass, actualPartialTick, nanoTime, renderLevel);
            MC.getProfiler().pop();

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

                    MC.mainRenderTarget.unbindWrite();
                    ClientUtils.takeScreenshot(rendertarget);
                    MC.getWindow().updateDisplay();
                    DATA_HOLDER.grabScreenShot = false;
                }
            }

            DATA_HOLDER.isFirstPass = false;
        }
        // now we are done with rendering
        MC.getProfiler().pop();

        DATA_HOLDER.vrPlayer.postRender(actualPartialTick);
        MC.getProfiler().push("Display/Reproject");

        try {
            DATA_HOLDER.vrRenderer.endFrame();
        } catch (RenderConfigException exception) {
            VRSettings.LOGGER.error("Vivecraft: error ending frame: {}", exception.error.getString());
        }
        MC.getProfiler().pop();
        RenderHelper.checkGLError("post submit");
    }
}
