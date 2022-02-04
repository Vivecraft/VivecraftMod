package com.example.examplemod;

import java.util.List;

import org.vivecraft.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.render.RenderConfigException;
import org.vivecraft.render.RenderPass;
import org.vivecraft.settings.VRHotkeys;
import org.vivecraft.utils.Utils;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;

public class Render {

	private Minecraft mc;
	private NewMinecraftExtension mcx;
	private DataHolder dataHolder = DataHolder.getInstance();

	
	public Render() {
		this.mc = Minecraft.getInstance();
		this.mcx = (NewMinecraftExtension) this.mc;
	}

	public void renderTick() {
		long l = Util.getNanos();
		
		try {
			dataHolder.vrRenderer.setupRenderConfiguration();
		} catch (RenderConfigException renderconfigexception) {

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.mcx.preRender(true);

		mc.mouseHandler.turnPlayer();
		mc.getWindow().setErrorSection("Render");
		VRHotkeys.updateMovingThirdPersonCam();
		mc.getProfiler().push("sound");
		mc.getSoundManager().updateSource(mc.gameRenderer.getMainCamera());
		mc.getProfiler().pop();

		float f = 0;

		dataHolder.currentPass = RenderPass.GUI;
		GlStateManager._depthMask(true);
		GlStateManager._colorMask(true, true, true, true);
		mc.mainRenderTarget = GuiHandler.guiFramebuffer;
		mc.mainRenderTarget.clear(Minecraft.ON_OSX);
		mc.mainRenderTarget.bindWrite(true);
		((GameRendererExtension) mc.gameRenderer).drawFramebufferNEW(f, true, new PoseStack());

		if (org.vivecraft.gameplay.screenhandlers.KeyboardHandler.Showing && !dataHolder.vrSettings.physicalKeyboard) {
			mc.mainRenderTarget = org.vivecraft.gameplay.screenhandlers.KeyboardHandler.Framebuffer;
			mc.mainRenderTarget.clear(Minecraft.ON_OSX);
			mc.mainRenderTarget.bindWrite(true);
			((GameRendererExtension) mc.gameRenderer).drawScreen(f,
					org.vivecraft.gameplay.screenhandlers.KeyboardHandler.UI, new PoseStack());
		}

		if (RadialHandler.isShowing()) {
			mc.mainRenderTarget = RadialHandler.Framebuffer;
			mc.mainRenderTarget.clear(Minecraft.ON_OSX);
			mc.mainRenderTarget.bindWrite(true);
			((GameRendererExtension) mc.gameRenderer).drawScreen(f, RadialHandler.UI, new PoseStack());
		}

		dataHolder.currentPass = RenderPass.CENTER;

		this.mcx.doRender(false, l);

		if (!mc.noRender) {
			List<RenderPass> list = dataHolder.vrRenderer.getRenderPasses();

			for (RenderPass renderpass : list) {
				dataHolder.currentPass = renderpass;

				switch (renderpass) {
				case LEFT:
				case RIGHT:
					mc.mainRenderTarget = dataHolder.vrRenderer.framebufferVrRender;
					break;

				case CENTER:
					mc.mainRenderTarget = dataHolder.vrRenderer.framebufferUndistorted;
					break;

				case THIRD:
					mc.mainRenderTarget = dataHolder.vrRenderer.framebufferMR;
					break;

				case SCOPEL:
					mc.mainRenderTarget = dataHolder.vrRenderer.telescopeFramebufferL;
					break;

				case SCOPER:
					mc.mainRenderTarget = dataHolder.vrRenderer.telescopeFramebufferR;
					break;

				case CAMERA:
					mc.mainRenderTarget = dataHolder.vrRenderer.cameraRenderFramebuffer;
				}

				mc.getProfiler().push("Eye:" + dataHolder.currentPass.ordinal());
				mc.getProfiler().push("setup");
				mc.mainRenderTarget.bindWrite(true);
				mc.getProfiler().pop();
				//this.renderSingleView(renderpass.ordinal(), f, true);
				mc.getProfiler().pop();

				if (dataHolder.grabScreenShot) {
					boolean flag;

					if (list.contains(RenderPass.CAMERA)) {
						flag = renderpass == RenderPass.CAMERA;
					} else if (list.contains(RenderPass.CENTER)) {
						flag = renderpass == RenderPass.CENTER;
					} else {
						flag = dataHolder.vrSettings.displayMirrorLeftEye ? renderpass == RenderPass.LEFT
								: renderpass == RenderPass.RIGHT;
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
			}

			if (true) {
				dataHolder.vrPlayer.postRender(f);
				mc.getProfiler().push("Display/Reproject");
				try {
					dataHolder.vrRenderer.endFrame();
				} catch (Exception exception) {
					
				}

				mc.getProfiler().pop();
			}


			mc.getProfiler().push("mirror");
			mc.mainRenderTarget.unbindWrite();
			this.copyToMirror();
			this.drawNotifyMirror();
			mc.getProfiler().pop();
		}

		this.mcx.posRender(true);

	}

	private void drawNotifyMirror() {
		// TODO Auto-generated method stub
		
	}

	private void copyToMirror() {
		// TODO Auto-generated method stub
		
	}

}
