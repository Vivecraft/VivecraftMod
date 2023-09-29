package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.Xplat;
import org.vivecraft.client.gui.VivecraftClickEvent;
import org.vivecraft.client.gui.VivecraftClickEvent.VivecraftAction;
import org.vivecraft.client.gui.settings.GuiOtherHUDSettings;
import org.vivecraft.client.gui.settings.GuiRenderOpticsSettings;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.extensions.LevelRendererExtension;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.settings.VRSettings.RenderPointerElement;
import org.vivecraft.client_vr.settings.VRSettings.ShaderGUIRender;
import org.vivecraft.mod_compat_vr.ShadersHelper;
import org.vivecraft.mod_compat_vr.immersiveportals.ImmersivePortalsHelper;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

import java.util.Calendar;
import java.util.Optional;
import java.util.stream.Stream;

import static net.minecraft.client.Minecraft.ON_OSX;
import static org.joml.Math.*;
import static org.vivecraft.client.utils.Utils.message;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.convertToVector3f;
import static org.vivecraft.common.utils.Utils.logger;

public class VREffectsHelper {

    public static boolean isInsideOpaqueBlock(Vector3f in) {
        if (mc.level == null) {
            return false;
        } else {
            BlockPos blockpos = BlockPos.containing(in.x(), in.y(), in.z());
            return mc.level.getBlockState(blockpos).isSolidRender(mc.level, blockpos);
        }
    }

    public static Triple<Float, BlockState, BlockPos> getNearOpaqueBlock(Vec3 in, double dist) {
        if (mc.level == null) {
            return null;
        } else {
            AABB aabb = new AABB(in.subtract(dist, dist, dist), in.add(dist, dist, dist));
            Stream<BlockPos> stream = BlockPos.betweenClosedStream(aabb).filter((bp) ->
                mc.level.getBlockState(bp).isSolidRender(mc.level, bp));
            Optional<BlockPos> optional = stream.findFirst();
            return optional.isPresent()
                   ? Triple.of(1.0F, mc.level.getBlockState(optional.get()), optional.get())
                   : null;
        }
    }

    public static void drawScopeFB(PoseStack matrixStackIn, int i) {
        if (dh.currentPass != RenderPass.SCOPEL && dh.currentPass != RenderPass.SCOPER) {
            // TODO wasn't there
            mc.gameRenderer.lightTexture().turnOffLightLayer();
            matrixStackIn.pushPose();
            RenderSystem.enableDepthTest();

            if (i == 0) {
                dh.vrRenderer.telescopeFramebufferR.bindRead();
                RenderSystem.setShaderTexture(0, dh.vrRenderer.telescopeFramebufferR.getColorTextureId());
            } else {
                dh.vrRenderer.telescopeFramebufferL.bindRead();
                RenderSystem.setShaderTexture(0, dh.vrRenderer.telescopeFramebufferL.getColorTextureId());
            }

            float scale = 0.0785F;
            //actual framebuffer
            float alpha = TelescopeTracker.viewPercent(i);
            // this.drawSizedQuad(720.0F, 720.0F, scale, new float[]{alpha, alpha, alpha, 1}, matrixStackIn.last().pose());
            RenderHelper.drawSizedQuadSolid(720.0F, 720.0F, scale, new float[]{alpha, alpha, alpha, 1}, matrixStackIn.last().pose());

            RenderSystem.setShaderTexture(0, new ResourceLocation("textures/misc/spyglass_scope.png"));
            RenderSystem.enableBlend();
            matrixStackIn.last().pose().translate(0.0F, 0.0F, 0.00001F);
            int light = LevelRenderer.getLightColor(mc.level, BlockPos.containing(dh.vrPlayer.vrdata_world_render.getController(i).getPosition()));
            RenderHelper.drawSizedQuadWithLightmapCutout(720.0F, 720.0F, scale, light, matrixStackIn.last().pose());

            matrixStackIn.popPose();
            mc.gameRenderer.lightTexture().turnOnLightLayer();
        }
    }

    private static boolean wasStencilOn;

    private static boolean showedStencilMessage = false;

    public static void drawEyeStencil(boolean flag1) {
        wasStencilOn = GL11C.glIsEnabled(GL11C.GL_STENCIL_TEST);

        if (wasStencilOn && !showedStencilMessage && dh.vrSettings.vrUseStencil && dh.vrSettings.showChatMessageStencil) {
            showedStencilMessage = true;
            message(Component.translatable("vivecraft.messages.stencil",
                Component.translatable("vivecraft.messages.3options",
                        Component.translatable("options.title"),
                        Component.translatable("vivecraft.options.screen.main"),
                        Component.translatable("vivecraft.options.screen.stereorendering"))
                    .withStyle(style -> style.withClickEvent(new VivecraftClickEvent(VivecraftAction.OPEN_SCREEN, new GuiRenderOpticsSettings(null)))
                        .withHoverEvent(new HoverEvent(Action.SHOW_TEXT, Component.translatable("vivecraft.messages.openSettings")))
                        .withColor(ChatFormatting.GREEN)
                        .withItalic(true)),
                Component.translatable("vivecraft.messages.3options",
                        Component.translatable("options.title"),
                        Component.translatable("vivecraft.options.screen.main"),
                        Component.translatable("vivecraft.options.screen.guiother"))
                    .withStyle(style -> style.withClickEvent(new VivecraftClickEvent(VivecraftAction.OPEN_SCREEN, new GuiOtherHUDSettings(null)))
                        .withHoverEvent(new HoverEvent(Action.SHOW_TEXT, Component.translatable("vivecraft.messages.openSettings")))
                        .withColor(ChatFormatting.GREEN)
                        .withItalic(true))
            ));
        }

        // don't touch the stencil if we don't use it
        // stencil only for left/right VR view
        if ((dh.currentPass == RenderPass.LEFT || dh.currentPass == RenderPass.RIGHT)
            && dh.vrSettings.vrUseStencil
            && (!Xplat.isModLoaded("immersive_portals") || !ImmersivePortalsHelper.isRenderingPortal())) {
//				net.optifine.shaders.Program program = Shaders.activeProgram;
//
//				if (shaders && Shaders.dfb != null) {
//					Shaders.dfb.bindFramebuffer();
//					Shaders.useProgram(Shaders.ProgramNone);
//
//					for (int i = 0; i < Shaders.usedDepthBuffers; ++i) {
//						GlStateManager._bindTexture(Shaders.dfb.depthTextures.get(i));
//						this.minecraft.vrRenderer.doStencil(false);
//					}
//
//					Shaders.useProgram(program);
//				} else {
            dh.vrRenderer.doStencil(false);
//				}
        }
    }

    public static void disableStencilTest() {
        // if we did enable the stencil test, disable it
        if (!wasStencilOn) {
            GL11C.glDisable(GL11C.GL_STENCIL_TEST);
        }
    }

    public static void renderJrbuddasAwesomeMainMenuRoomNew(PoseStack pMatrixStack) {
        int repeat = 4; // texture wraps per meter
        float height = 2.5F;
        float oversize = 1.3F;
        Vector2f area = dh.vr.getPlayAreaSize();
        if (area == null) {
            area = new Vector2f(2, 2);
        }

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT, ON_OSX);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, Screen.BACKGROUND_LOCATION);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        pMatrixStack.pushPose();

        float width = area.x + oversize;
        float length = area.y + oversize;
        Matrix4f matrix = pMatrixStack.last().pose().translate(-width / 2.0F, 0.0F, -length / 2.0F);
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);

        float r, g, b, a;
        r = g = b = 0.8F;
        a = 1.0F;

        bufferbuilder.vertex(matrix, 0, 0, 0)
            .uv(0, 0).color(r, g, b, a).normal(0, 1, 0).endVertex();
        bufferbuilder.vertex(matrix, 0, 0, length)
            .uv(0, repeat * length).color(r, g, b, a).normal(0, 1, 0).endVertex();
        bufferbuilder.vertex(matrix, width, 0, length)
            .uv(repeat * width, repeat * length).color(r, g, b, a).normal(0, 1, 0).endVertex();
        bufferbuilder.vertex(matrix, width, 0, 0)
            .uv(repeat * width, 0).color(r, g, b, a).normal(0, 1, 0).endVertex();

        bufferbuilder.vertex(matrix, 0, height, length)
            .uv(0, 0).color(r, g, b, a).normal(0, -1, 0).endVertex();
        bufferbuilder.vertex(matrix, 0, height, 0)
            .uv(0, repeat * length).color(r, g, b, a).normal(0, -1, 0).endVertex();
        bufferbuilder.vertex(matrix, width, height, 0)
            .uv(repeat * width, repeat * length).color(r, g, b, a).normal(0, -1, 0).endVertex();
        bufferbuilder.vertex(matrix, width, height, length)
            .uv(repeat * width, 0).color(r, g, b, a).normal(0, -1, 0).endVertex();

        bufferbuilder.vertex(matrix, 0, 0, 0)
            .uv(0, 0).color(r, g, b, a).normal(1, 0, 0).endVertex();
        bufferbuilder.vertex(matrix, 0, height, 0)
            .uv(0, repeat * height).color(r, g, b, a).normal(1, 0, 0).endVertex();
        bufferbuilder.vertex(matrix, 0, height, length)
            .uv(repeat * length, repeat * height).color(r, g, b, a).normal(1, 0, 0).endVertex();
        bufferbuilder.vertex(matrix, 0, 0, length)
            .uv(repeat * length, 0).color(r, g, b, a).normal(1, 0, 0).endVertex();

        bufferbuilder.vertex(matrix, width, 0, 0)
            .uv(0, 0).color(r, g, b, a).normal(-1, 0, 0).endVertex();
        bufferbuilder.vertex(matrix, width, 0, length)
            .uv(repeat * length, 0).color(r, g, b, a).normal(-1, 0, 0).endVertex();
        bufferbuilder.vertex(matrix, width, height, length)
            .uv(repeat * length, repeat * height).color(r, g, b, a).normal(-1, 0, 0).endVertex();
        bufferbuilder.vertex(matrix, width, height, 0)
            .uv(0, repeat * height).color(r, g, b, a).normal(-1, 0, 0).endVertex();

        bufferbuilder.vertex(matrix, 0, 0, 0)
            .uv(0, 0).color(r, g, b, a).normal(0, 0, 1).endVertex();
        bufferbuilder.vertex(matrix, width, 0, 0)
            .uv(repeat * width, 0).color(r, g, b, a).normal(0, 0, 1).endVertex();
        bufferbuilder.vertex(matrix, width, height, 0)
            .uv(repeat * width, repeat * height).color(r, g, b, a).normal(0, 0, 1).endVertex();
        bufferbuilder.vertex(matrix, 0, height, 0)
            .uv(0, repeat * height).color(r, g, b, a).normal(0, 0, 1).endVertex();

        bufferbuilder.vertex(matrix, 0, 0, length).
            uv(0, 0).color(r, g, b, a).normal(0, 0, -1).endVertex();
        bufferbuilder.vertex(matrix, 0, height, length)
            .uv(0, repeat * height).color(r, g, b, a).normal(0, 0, -1).endVertex();
        bufferbuilder.vertex(matrix, width, height, length)
            .uv(repeat * width, repeat * height).color(r, g, b, a).normal(0, 0, -1).endVertex();
        bufferbuilder.vertex(matrix, width, 0, length)
            .uv(repeat * width, 0).color(r, g, b, a).normal(0, 0, -1).endVertex();

        BufferUploader.drawWithShader(bufferbuilder.end());
        pMatrixStack.popPose();
    }

    public static void renderTechjarsAwesomeMainMenuRoom(PoseStack poseStack) {
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        RenderSystem.enableDepthTest();
        //RenderSystem.enableTexture();
        RenderSystem.enableBlend();
        RenderSystem.enableCull();

        poseStack.pushPose();

        int tzOffset = Calendar.getInstance().get(Calendar.ZONE_OFFSET);
        dh.menuWorldRenderer.time = (
            dh.menuWorldRenderer.fastTime ?
            (long) (dh.menuWorldRenderer.ticks * 10L + 10 * mc.getFrameTime()) :
            (long) ((System.currentTimeMillis() + tzOffset - 21600000) / 86400000D * 24000D)
        );

        dh.menuWorldRenderer.fogRenderer.setupFogColor();
        RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT, ON_OSX);

        dh.menuWorldRenderer.updateLightmap();
        dh.menuWorldRenderer.render(poseStack);

        Vector2f area = dh.vr.getPlayAreaSize();
        if (area != null) {
            poseStack.pushPose();
            float width = area.x;//(float)ceil(area.x);
            float length = area.y;//(float)ceil(area.y);

            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
            RenderSystem.setShaderTexture(0, Screen.BACKGROUND_LOCATION);
            float sun = dh.menuWorldRenderer.getSkyDarken();
            RenderSystem.setShaderColor(sun, sun, sun, 0.3F);


            RenderSystem.defaultBlendFunc();
            RenderSystem.enableBlend();
            Matrix4f matrix4f = poseStack.last().pose().translate(-width / 2.0F, 0.0F, -length / 2.0F);
            BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
            bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
            bufferbuilder
                .vertex(matrix4f, 0, 0.005F, 0)
                .uv(0, 0)
                .color(1F, 1F, 1F, 1F)
                .normal(0, 1, 0).endVertex();
            bufferbuilder
                .vertex(matrix4f, 0, 0.005F, length)
                .uv(0, 4 * length)
                .color(1F, 1F, 1F, 1F)
                .normal(0, 1, 0).endVertex();
            bufferbuilder
                .vertex(matrix4f, width, 0.005F, length)
                .uv(4 * width, 4 * length)
                .color(1F, 1F, 1F, 1F)
                .normal(0, 1, 0).endVertex();
            bufferbuilder
                .vertex(matrix4f, width, 0.005F, 0)
                .uv(4 * width, 0)
                .color(1F, 1F, 1F, 1F)
                .normal(0, 1, 0).endVertex();

            BufferUploader.drawWithShader(bufferbuilder.end());

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            poseStack.popPose();
        }

        poseStack.popPose();
        RenderSystem.defaultBlendFunc();
    }

    public static void renderVRFabulous(float partialTicks, LevelRenderer levelRenderer, boolean menuHandRight, boolean menuHandLeft, PoseStack poseStack) {
        if (dh.currentPass == RenderPass.SCOPEL || dh.currentPass == RenderPass.SCOPER) {
            return;
        }
        mc.getProfiler().popPush("VR");
        renderCrosshairAtDepth(!dh.vrSettings.useCrosshairOcclusion, poseStack);
        mc.getMainRenderTarget().unbindWrite();
        ((LevelRendererExtension) levelRenderer).vivecraft$getAlphaSortVROccludedFramebuffer().clear(ON_OSX);
        ((LevelRendererExtension) levelRenderer).vivecraft$getAlphaSortVROccludedFramebuffer().copyDepthFrom(mc.getMainRenderTarget());
        ((LevelRendererExtension) levelRenderer).vivecraft$getAlphaSortVROccludedFramebuffer().bindWrite(true);

        if (shouldOccludeGui()) {
            renderGuiAndShadow(poseStack, partialTicks, false, false);
        }

        ((LevelRendererExtension) levelRenderer).vivecraft$getAlphaSortVRUnoccludedFramebuffer().clear(ON_OSX);
        ((LevelRendererExtension) levelRenderer).vivecraft$getAlphaSortVRUnoccludedFramebuffer().bindWrite(true);

        if (!shouldOccludeGui()) {
            renderGuiAndShadow(poseStack, partialTicks, false, false);
        }

        renderVRSelfEffects(partialTicks);
        VRWidgetHelper.renderVRThirdPersonCamWidget();
        VRWidgetHelper.renderVRHandheldCameraWidget();
        boolean renderHands = VRArmHelper.shouldRenderHands();

        VRArmHelper.renderVRHands(partialTicks, renderHands && menuHandRight, renderHands && menuHandLeft, true, true, poseStack);

        ((LevelRendererExtension) levelRenderer).vivecraft$getAlphaSortVRHandsFramebuffer().clear(ON_OSX);
        ((LevelRendererExtension) levelRenderer).vivecraft$getAlphaSortVRHandsFramebuffer().copyDepthFrom(mc.getMainRenderTarget());
        ((LevelRendererExtension) levelRenderer).vivecraft$getAlphaSortVRHandsFramebuffer().bindWrite(true);

        VRArmHelper.renderVRHands(partialTicks, renderHands && !menuHandRight, renderHands && !menuHandLeft, false, false, poseStack);

        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        mc.getMainRenderTarget().bindWrite(true);
    }

    public static void renderVrFast(float partialTicks, boolean secondPass, boolean menuHandRight, boolean menuHandLeft,
        PoseStack poseStack) {
        if (dh.currentPass == RenderPass.SCOPEL || dh.currentPass == RenderPass.SCOPER) {
            return;
        }
        mc.getProfiler().popPush("VR");
        mc.gameRenderer.lightTexture().turnOffLightLayer();

        if (!secondPass) {
            renderCrosshairAtDepth(!dh.vrSettings.useCrosshairOcclusion, poseStack);
            VRWidgetHelper.renderVRThirdPersonCamWidget();
            VRWidgetHelper.renderVRHandheldCameraWidget();
        } else {
            renderGuiAndShadow(poseStack, partialTicks, !shouldOccludeGui(), true);
        }

        // render hands in second pass when gui is open
        boolean renderHandsSecond = RadialHandler.isShowing() || KeyboardHandler.isShowing() || Minecraft.getInstance().screen != null;
        if (secondPass == renderHandsSecond) {
            // should render hands in second pass if menus are open, else in the first pass
            // only render the hands only once
            VRArmHelper.renderVRHands(partialTicks, VRArmHelper.shouldRenderHands(), VRArmHelper.shouldRenderHands(), menuHandRight, menuHandLeft, poseStack);
        }
        renderVRSelfEffects(partialTicks);
    }

    private static boolean shouldOccludeGui() {
        if (dh.currentPass != RenderPass.THIRD && dh.currentPass != RenderPass.CAMERA) {
            return !((GameRendererExtension) mc.gameRenderer).vivecraft$isInMenuRoom() && mc.screen == null && !KeyboardHandler.isShowing()
                && !RadialHandler.isShowing() && dh.vrSettings.hudOcclusion
                && !isInsideOpaqueBlock(dh.vrPlayer.vrdata_world_render.getEye(dh.currentPass).getPosition().toVector3f());
        } else {
            return true;
        }
    }

    private static void renderGuiAndShadow(PoseStack poseStack, float partialTicks, boolean depthAlways, boolean shadowFirst) {
        if (shadowFirst) {
            VREffectsHelper.renderVrShadow(partialTicks, depthAlways, poseStack);
        }
        if (Minecraft.getInstance().screen != null || !KeyboardHandler.isShowing()) {
            renderGuiLayer(partialTicks, depthAlways, poseStack);
        }
        if (!shadowFirst) {
            VREffectsHelper.renderVrShadow(partialTicks, depthAlways, poseStack);
        }

        if (KeyboardHandler.isShowing()) {
            if (dh.vrSettings.physicalKeyboard) {
                renderPhysicalKeyboard(partialTicks, poseStack);
            } else {
                render2D(partialTicks, KeyboardHandler.Framebuffer, KeyboardHandler.Pos_room,
                    KeyboardHandler.Rotation_room, depthAlways, poseStack);
            }
        }

        if (RadialHandler.isShowing()) {
            render2D(partialTicks, RadialHandler.Framebuffer, RadialHandler.Pos_room,
                RadialHandler.Rotation_room, depthAlways, poseStack);
        }
    }

    public static void renderVrShadow(float partialTicks, boolean depthAlways, PoseStack poseStack) {
        if (dh.currentPass != RenderPass.THIRD && dh.currentPass != RenderPass.CAMERA && mc.player.isAlive() &&
            !(((PlayerExtension) mc.player).vivecraft$getRoomYOffsetFromPose() < 0.0D) &&
            mc.player.getVehicle() == null
        ) {
            mc.getProfiler().push("vr shadow");
            AABB aabb = mc.player.getBoundingBox();

            if (dh.vrSettings.vrShowBlueCircleBuddy && aabb != null) {

                poseStack.pushPose();
                poseStack.setIdentity();
                RenderSystem.disableCull();
                RenderHelper.applyVRModelView(dh.currentPass, poseStack);
                Vec3 vec3 = dh.vrPlayer.vrdata_world_render
                    .getEye(dh.currentPass).getPosition();
                Vec3 interpolatedPlayerPos = ((GameRendererExtension) mc.gameRenderer).vivecraft$getRvePos(partialTicks);
                Vec3 pos = interpolatedPlayerPos.subtract(vec3).add(0.0D, 0.005D, 0.0D);
                RenderHelper.setupPolyRendering(true);
                RenderSystem.enableDepthTest();

                if (depthAlways) {
                    RenderSystem.depthFunc(GL11C.GL_ALWAYS);
                } else {
                    RenderSystem.depthFunc(GL11C.GL_LEQUAL);
                }

                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                mc.getTextureManager().bindForSetup(new ResourceLocation("vivecraft:textures/white.png"));
                RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));

                RenderHelper.renderFlatQuad(pos, (float) (aabb.maxX - aabb.minX), (float) (aabb.maxZ - aabb.minZ),
                    0.0F, 0, 0, 0, 64, poseStack
                );
                RenderSystem.depthFunc(GL11C.GL_LEQUAL);
                RenderHelper.setupPolyRendering(false);
                poseStack.popPose();
                RenderSystem.enableCull();
            }
            mc.getProfiler().pop();
        }
    }

    private static void renderVRSelfEffects(float par1) {
        if (((GameRendererExtension) mc.gameRenderer).vivecraft$isOnFire() && dh.currentPass != RenderPass.THIRD
            && dh.currentPass != RenderPass.CAMERA) {
            VREffectsHelper.renderFireInFirstPerson();
        }
        mc.gameRenderer.renderItemActivationAnimation(0, 0, par1);
    }

    public static void renderFireInFirstPerson() {
        PoseStack posestack = new PoseStack();
        RenderHelper.applyVRModelView(dh.currentPass, posestack);
        RenderHelper.applyStereo(dh.currentPass, posestack);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);

        if (dh.currentPass == RenderPass.THIRD || dh.currentPass == RenderPass.CAMERA) {
            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        TextureAtlasSprite fireSprite = ModelBakery.FIRE_1.sprite();
        RenderSystem.enableDepthTest();

        if (OptifineHelper.isOptifineLoaded()) {
            OptifineHelper.markTextureAsActive(fireSprite);
        }

        // code adapted from net.minecraft.client.renderer.ScreenEffectRenderer.renderFire

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, fireSprite.atlasLocation());
        float uMin = fireSprite.getU0();
        float uMax = fireSprite.getU1();
        float uMid = (uMin + uMax) / 2.0F;
        float vMin = fireSprite.getV0();
        float vMax = fireSprite.getV1();
        float vMid = (vMin + vMax) / 2.0F;
        float ShrinkRatio = fireSprite.uvShrinkRatio();
        float u0 = lerp(uMin, uMid, ShrinkRatio);
        float u1 = lerp(uMax, uMid, ShrinkRatio);
        float v0 = lerp(vMin, vMid, ShrinkRatio);
        float v1 = lerp(vMax, vMid, ShrinkRatio);
        float a = 0.3F;
        float b = (float) (dh.vrPlayer.vrdata_world_render.getHeadPivot().y - ((GameRendererExtension) mc.gameRenderer).vivecraft$getRveY());

        for (int i = 0; i < 4; ++i) {
            posestack.pushPose();
            float ang = toRadians(90.0F * i - dh.vrPlayer.vrdata_world_render.getBodyYaw());
            posestack.last().normal().rotateY(ang);
            Matrix4f matrix4f = posestack.last().pose().rotateY(ang).translate(0.0F, -b, 0.0F);
            bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex(matrix4f, -a, 0.0F, -a).uv(u1, v1).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            bufferbuilder.vertex(matrix4f, a, 0.0F, -a).uv(u0, v1).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            bufferbuilder.vertex(matrix4f, a, b, -a).uv(u0, v0).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            bufferbuilder.vertex(matrix4f, -a, b, -a).uv(u1, v0).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            BufferUploader.drawWithShader(bufferbuilder.end());

            posestack.popPose();
        }

        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.disableBlend();
    }

    public static void renderPhysicalKeyboard(float partialTicks, PoseStack poseStack) {
        if (!dh.bowTracker.isDrawing) {
            ((GameRendererExtension) mc.gameRenderer).vivecraft$resetProjectionMatrix(partialTicks);
            poseStack.pushPose();
            poseStack.setIdentity();

            mc.getProfiler().push("applyPhysicalKeyboardModelView");
            Vector3f eye = dh.vrPlayer.vrdata_world_render.getEye(dh.currentPass).getPosition().toVector3f();

            //convert previously calculated coords to world coords
            Vector3f guiPos = VRPlayer.room_to_world_pos(KeyboardHandler.Pos_room, dh.vrPlayer.vrdata_world_render).toVector3f();

            poseStack.last().pose()
                .mul(dh.vrPlayer.vrdata_world_render.getEye(dh.currentPass).getMatrix())
                // offset from eye to gui pos
                .translate(guiPos.sub(eye))
                .mul(new Matrix4f(KeyboardHandler.Rotation_room).rotateY(dh.vrPlayer.vrdata_world_render.rotation_radians));

            float scale = dh.vrPlayer.vrdata_world_render.worldScale;
            poseStack.scale(scale, scale, scale);

            mc.getProfiler().pop();

            KeyboardHandler.physicalKeyboard.render(poseStack);
            poseStack.popPose();
        }
    }

    private static void setupScreenRendering(PoseStack poseStack, float partialTicks) {
        // remove nausea effect from projection matrix, for vanilla, and poseStack for iris
        ((GameRendererExtension) mc.gameRenderer).vivecraft$resetProjectionMatrix(partialTicks);
        poseStack.pushPose();
        poseStack.setIdentity();
        RenderHelper.applyVRModelView(dh.currentPass, poseStack);

        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.setIdentity();
        RenderSystem.applyModelViewMatrix();
    }

    private static void finishScreenRendering(PoseStack poseStack) {
        poseStack.popPose();
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();
    }

    private static void renderScreen(PoseStack poseStack, RenderTarget screenFramebuffer, boolean depthAlways, boolean noFog, Vector3f screenPos) {
        screenFramebuffer.bindRead();
        RenderSystem.disableCull();
        RenderSystem.setShaderTexture(0, screenFramebuffer.getColorTextureId());

        // cache fog distance
        float fogStart = RenderSystem.getShaderFogStart();
        float[] color = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
        if (!((GameRendererExtension) mc.gameRenderer).vivecraft$isInMenuRoom()) {
            if (mc.screen == null) {
                color[3] = dh.vrSettings.hudOpacity;
            }
            if (noFog || mc.screen != null) {
                // disable fog for menus
                RenderSystem.setShaderFogStart(Float.MAX_VALUE);
            }

            if (mc.player != null && mc.player.isShiftKeyDown()) {
                color[3] *= 0.75F;
            }

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE_MINUS_DST_ALPHA, DestFactor.ONE
            );
            if (dh.vrSettings.shaderGUIRender == ShaderGUIRender.BEFORE_TRANSLUCENT_SOLID && ShadersHelper.isShaderActive()) {
                RenderSystem.disableBlend();
            }
        } else {
            // enable blend for overlay transition in menuworld to not be jarring
            RenderSystem.enableBlend();
        }

        if (depthAlways) {
            RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        } else {
            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();

        if (mc.level != null) {
            if (isInsideOpaqueBlock(screenPos) || ((GameRendererExtension) mc.gameRenderer).vivecraft$isInBlock() > 0.0F) {
                convertToVector3f(dh.vrPlayer.vrdata_world_render.hmd.getPosition(), screenPos);
            }

            int minLight = ShadersHelper.ShaderLight();
            int light = Utils.getCombinedLightWithMin(
                mc.level, BlockPos.containing(screenPos.x(), screenPos.y(), screenPos.z()), minLight
            );
            RenderHelper.drawSizedQuadWithLightmapCutout((float) mc.getWindow().getGuiScaledWidth(),
                (float) mc.getWindow().getGuiScaledHeight(), 1.5F, light, color,
                poseStack.last().pose());
        } else {
            RenderHelper.drawSizedQuad((float) mc.getWindow().getGuiScaledWidth(),
                (float) mc.getWindow().getGuiScaledHeight(), 1.5F, color,
                poseStack.last().pose());
        }

        // reset fog
        RenderSystem.setShaderFogStart(fogStart);
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
    }

    public static void renderGuiLayer(float partialTicks, boolean depthAlways, PoseStack poseStack) {
        if (!dh.bowTracker.isDrawing && (mc.screen != null || !mc.options.hideGui) && !RadialHandler.isShowing()) {
            mc.getProfiler().push("GuiLayer");

            setupScreenRendering(poseStack, partialTicks);

            // MAIN MENU ENVIRONMENT
            if (((GameRendererExtension) mc.gameRenderer).vivecraft$isInMenuRoom()) {
                // render the screen always on top in the menu room to prevent z fighting
                depthAlways = true;

                poseStack.pushPose();
                poseStack.last().pose()
                    .translate(
                        dh.vrPlayer.vrdata_world_render.origin.toVector3f()
                            .sub(dh.vrPlayer.vrdata_world_render.getEye(dh.currentPass).getPosition().toVector3f())
                    )
                    // remove world rotation or the room doesn't align with the screen
                    .rotateY(dh.vrPlayer.vrdata_world_render.rotation_radians);
                poseStack.last().normal().rotateY(dh.vrPlayer.vrdata_world_render.rotation_radians);

                if (dh.menuWorldRenderer.isReady()) {
                    try {
                        renderTechjarsAwesomeMainMenuRoom(poseStack);
                    } catch (Exception exception) {
                        logger.info("Error rendering main menu world, unloading to prevent more errors");
                        exception.printStackTrace();
                        dh.menuWorldRenderer.destroy();
                    }
                } else {
                    renderJrbuddasAwesomeMainMenuRoomNew(poseStack);
                }
                poseStack.popPose();
            }
            // END AWESOME MAIN MENU ENVIRONMENT

            renderScreen(poseStack, GuiHandler.guiFramebuffer, depthAlways, false, GuiHandler.applyGUIModelView(dh.currentPass, poseStack));

            finishScreenRendering(poseStack);
            mc.getProfiler().pop();
        }
    }

    public static void render2D(float partialTicks, RenderTarget framebuffer, Vec3 pos, Matrix4f rot, boolean depthAlways, PoseStack poseStack) {
        if (!dh.bowTracker.isDrawing) {
            setupScreenRendering(poseStack, partialTicks);

            mc.getProfiler().push("apply2DModelView");

            Vector3f eye = dh.vrPlayer.vrdata_world_render.getEye(dh.currentPass).getPosition().toVector3f();

            Vector3f guiPos = VRPlayer.room_to_world_pos(pos, dh.vrPlayer.vrdata_world_render).toVector3f();

            poseStack.last().pose()
                .translate(guiPos.sub(eye))
                .mul(rot)
                .rotateY(dh.vrPlayer.vrdata_world_render.rotation_radians);

            float scale = GuiHandler.guiScale * dh.vrPlayer.vrdata_world_render.worldScale;
            poseStack.scale(scale, scale, scale);

            mc.getProfiler().pop();

            renderScreen(poseStack, framebuffer, depthAlways, true, guiPos);

            finishScreenRendering(poseStack);
        }
    }

    public static void renderFaceOverlay(float partialTicks, PoseStack poseStack) {
        if (((GameRendererExtension) mc.gameRenderer).vivecraft$isInBlock() > 0.0F) {
            renderFaceInBlock();

            renderGuiAndShadow(poseStack, partialTicks, true, true);

            VRArmHelper.renderVRHands(partialTicks, true, true, true, true, poseStack);
        }
    }

    public static void renderFaceInBlock() {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);

        // orthographic matrix, (-1, -1) to (1, 1), near = 0.0, far 2.0
        Matrix4f mat = new Matrix4f();
        mat.m00(1.0F);
        mat.m11(1.0F);
        mat.m22(-1.0F);
        mat.m33(1.0F);
        mat.m32(-1.0F);

        RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(mat, -1.5F, -1.5F, 0.0F).endVertex();
        bufferbuilder.vertex(mat, 1.5F, -1.5F, 0.0F).endVertex();
        bufferbuilder.vertex(mat, 1.5F, 1.5F, 0.0F).endVertex();
        bufferbuilder.vertex(mat, -1.5F, 1.5F, 0.0F).endVertex();
        tesselator.end();
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static boolean shouldRenderCrosshair() {
        if (dh.viewonly) {
            return false;
        } else if (mc.level == null) {
            return false;
        } else if (mc.screen != null) {
            return false;
        } else if (dh.vrSettings.renderInGameCrosshairMode != RenderPointerElement.ALWAYS
            && (dh.vrSettings.renderInGameCrosshairMode != RenderPointerElement.WITH_HUD
            || mc.options.hideGui)) {
            return false;
        } else if (dh.currentPass != RenderPass.LEFT
            && dh.currentPass != RenderPass.RIGHT
            && dh.currentPass != RenderPass.CENTER) {
            // it doesn't look very good
            return false;
        } else if (KeyboardHandler.isShowing()) {
            return false;
        } else if (RadialHandler.isUsingController(ControllerType.RIGHT)) {
            return false;
        } else if (dh.bowTracker.isNotched()) {
            return false;
        } else if (dh.vr.getInputAction(VivecraftVRMod.keyVRInteract).isEnabledRaw(ControllerType.RIGHT)
            || VivecraftVRMod.keyVRInteract.isDown(ControllerType.RIGHT)) {
            return false;
        } else if (dh.vr.getInputAction(VivecraftVRMod.keyClimbeyGrab).isEnabledRaw(ControllerType.RIGHT)
            || VivecraftVRMod.keyClimbeyGrab.isDown(ControllerType.RIGHT)) {
            return false;
        } else if (dh.teleportTracker.isAiming()) {
            return false;
        } else if (dh.climbTracker.isGrabbingLadder(0)) {
            return false;
        } else {
            return !(dh.vrPlayer.worldScale > 15.0F);
        }
    }

    public static void renderCrosshairAtDepth(boolean depthAlways, PoseStack poseStack) {
        // TODO broken
        if (shouldRenderCrosshair()) {
            mc.getProfiler().push("crosshair");

            // white crosshair, with blending
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Vec3 crosshairRenderPos = ((GameRendererExtension) mc.gameRenderer).vivecraft$getCrossVec();
            Vec3 aim = crosshairRenderPos.subtract(dh.vrPlayer.vrdata_world_render.getController(0).getPosition());
            float crossDepth = (float) aim.length();
            float scale = 0.125F * dh.vrSettings.crosshairScale * sqrt(dh.vrPlayer.vrdata_world_render.worldScale);

            //scooch closer a bit for light calc.
            crosshairRenderPos = crosshairRenderPos.add(aim.normalize().scale(-0.01D));

            poseStack.pushPose();
            poseStack.setIdentity();
            RenderHelper.applyVRModelView(dh.currentPass, poseStack);

            Vec3 translate = crosshairRenderPos.subtract(mc.getCameraEntity().position());
            poseStack.last().pose().translate((float) translate.x, (float) translate.y, (float) translate.z);

            if (mc.hitResult instanceof BlockHitResult hit && hit.getType() == Type.BLOCK) {
                switch (hit.getDirection()) {
                    case DOWN -> {
                        float ang1 = toRadians(dh.vrPlayer.vrdata_world_render.getController(0).getYaw());
                        float ang2 = toRadians(-90.0F);
                        poseStack.last().pose().rotateY(ang1).rotateX(ang2);
                        poseStack.last().normal().rotateY(ang1).rotateX(ang2);
                    }
                    case UP -> {
                        float ang1 = toRadians(-dh.vrPlayer.vrdata_world_render.getController(0).getYaw());
                        float ang2 = toRadians(90.0F);
                        poseStack.last().pose().rotateY(ang1).rotateX(ang2);
                        poseStack.last().normal().rotateY(ang1).rotateX(ang2);
                    }
                    case WEST -> {
                        float ang = toRadians(90.0F);
                        poseStack.last().pose().rotateY(ang);
                        poseStack.last().normal().rotateY(ang);
                    }
                    case EAST -> {
                        float ang = toRadians(-90.0F);
                        poseStack.last().pose().rotateY(ang);
                        poseStack.last().normal().rotateY(ang);
                    }
                    case SOUTH -> {
                        float ang = toRadians(180.0F);
                        poseStack.last().pose().rotateY(ang);
                        poseStack.last().normal().rotateY(ang);
                    }
                }
            } else {
                float ang1 = toRadians(-dh.vrPlayer.vrdata_world_render.getController(0).getYaw());
                float ang2 = toRadians(-dh.vrPlayer.vrdata_world_render.getController(0).getPitch());
                poseStack.last().pose()
                    .rotateY(ang1)
                    .rotateX(ang2);
                poseStack.last().normal()
                    .rotateY(ang1)
                    .rotateX(ang2);
            }

            if (dh.vrSettings.crosshairScalesWithDistance) {
                float depthscale = 0.3F + 0.2F * crossDepth;
                scale *= depthscale;
            }

            mc.gameRenderer.lightTexture().turnOnLightLayer();
            poseStack.scale(scale, scale, scale);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();

            if (depthAlways) {
                RenderSystem.depthFunc(GL11C.GL_ALWAYS);
            } else {
                RenderSystem.depthFunc(GL11C.GL_LEQUAL);
            }

            RenderSystem.enableBlend(); // Fuck it, we want a proper crosshair
            RenderSystem.blendFuncSeparate(
                SourceFactor.ONE_MINUS_DST_COLOR, DestFactor.ZERO, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA
            );
            int light = LevelRenderer.getLightColor(mc.level, BlockPos.containing(crosshairRenderPos));
            float brightness = 1.0F;

            if (mc.hitResult == null || mc.hitResult.getType() == Type.MISS) {
                brightness = 0.5F;
            }

            RenderSystem.setShaderTexture(0, Gui.GUI_ICONS_LOCATION);
            float uMax = 15.0F / 256.0F;
            float vMax = 15.0F / 256.0F;

            BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();

            RenderSystem.setShader(GameRenderer::getRendertypeEntityCutoutNoCullShader);
            bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);

            bufferbuilder.vertex(poseStack.last().pose(), -1.0F, 1.0F, 0.0F).color(brightness, brightness, brightness, 1.0F)
                .uv(0.0F, vMax).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(poseStack.last().pose(), 1.0F, 1.0F, 0.0F).color(brightness, brightness, brightness, 1.0F)
                .uv(uMax, vMax).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(poseStack.last().pose(), 1.0F, -1.0F, 0.0F).color(brightness, brightness, brightness, 1.0F)
                .uv(uMax, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(poseStack.last().pose(), -1.0F, -1.0F, 0.0F).color(brightness, brightness, brightness, 1.0F)
                .uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();

            BufferUploader.drawWithShader(bufferbuilder.end());

            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
            poseStack.popPose();
            mc.getProfiler().pop();
        }
    }
}
