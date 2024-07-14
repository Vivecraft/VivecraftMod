package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11C;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.Xplat;
import org.vivecraft.client.gui.VivecraftClickEvent;
import org.vivecraft.client.gui.settings.GuiOtherHUDSettings;
import org.vivecraft.client.gui.settings.GuiRenderOpticsSettings;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;
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
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.mod_compat_vr.ShadersHelper;
import org.vivecraft.mod_compat_vr.immersiveportals.ImmersivePortalsHelper;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

import java.util.Calendar;
import java.util.Optional;
import java.util.stream.Stream;

public class VREffectsHelper {

    private static final ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
    private static final Minecraft mc = Minecraft.getInstance();

    public static boolean isInsideOpaqueBlock(Vec3 in) {
        if (mc.level == null) {
            return false;
        } else {
            BlockPos blockpos = BlockPos.containing(in);
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
        if (dataHolder.currentPass != RenderPass.SCOPEL && dataHolder.currentPass != RenderPass.SCOPER) {
            // TODO wasn't there
            mc.gameRenderer.lightTexture().turnOffLightLayer();
            matrixStackIn.pushPose();
            RenderSystem.enableDepthTest();

            if (i == 0) {
                dataHolder.vrRenderer.telescopeFramebufferR.bindRead();
                RenderSystem.setShaderTexture(0, dataHolder.vrRenderer.telescopeFramebufferR.getColorTextureId());
            } else {
                dataHolder.vrRenderer.telescopeFramebufferL.bindRead();
                RenderSystem.setShaderTexture(0, dataHolder.vrRenderer.telescopeFramebufferL.getColorTextureId());
            }

            float scale = 0.0785F;
            //actual framebuffer
            float alpha = TelescopeTracker.viewPercent(i);
            RenderHelper.drawSizedQuadSolid(720.0F, 720.0F, scale, new float[]{alpha, alpha, alpha, 1}, matrixStackIn.last().pose());

            RenderSystem.setShaderTexture(0, new ResourceLocation("textures/misc/spyglass_scope.png"));
            RenderSystem.enableBlend();
            matrixStackIn.translate(0.0D, 0.0D, 0.00001D);
            int light = LevelRenderer.getLightColor(mc.level, BlockPos.containing(dataHolder.vrPlayer.vrdata_world_render.getController(i).getPosition()));
            RenderHelper.drawSizedQuadWithLightmapCutout(720.0F, 720.0F, scale, light, matrixStackIn.last().pose(), true);

            matrixStackIn.popPose();
            mc.gameRenderer.lightTexture().turnOnLightLayer();
        }
    }

    private static boolean wasStencilOn;

    private static boolean showedStencilMessage = false;

    public static void drawEyeStencil(boolean flag1) {
        wasStencilOn = GL11C.glIsEnabled(GL11C.GL_STENCIL_TEST);

        if (wasStencilOn && !showedStencilMessage && dataHolder.vrSettings.vrUseStencil && dataHolder.vrSettings.showChatMessageStencil) {
            showedStencilMessage = true;
            mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.stencil",
                Component.translatable("vivecraft.messages.3options",
                        Component.translatable("options.title"),
                        Component.translatable("vivecraft.options.screen.main"),
                        Component.translatable("vivecraft.options.screen.stereorendering"))
                    .withStyle(style -> style.withClickEvent(new VivecraftClickEvent(VivecraftClickEvent.VivecraftAction.OPEN_SCREEN, new GuiRenderOpticsSettings(null)))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("vivecraft.messages.openSettings")))
                        .withColor(ChatFormatting.GREEN)
                        .withItalic(true)),
                Component.translatable("vivecraft.messages.3options",
                        Component.translatable("options.title"),
                        Component.translatable("vivecraft.options.screen.main"),
                        Component.translatable("vivecraft.options.screen.guiother"))
                    .withStyle(style -> style.withClickEvent(new VivecraftClickEvent(VivecraftClickEvent.VivecraftAction.OPEN_SCREEN, new GuiOtherHUDSettings(null)))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("vivecraft.messages.openSettings")))
                        .withColor(ChatFormatting.GREEN)
                        .withItalic(true))
            ));
        }

        // don't touch the stencil if we don't use it
        // stencil only for left/right VR view
        if ((dataHolder.currentPass == RenderPass.LEFT
            || dataHolder.currentPass == RenderPass.RIGHT)
            && dataHolder.vrSettings.vrUseStencil
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
            dataHolder.vrRenderer.doStencil(false);
//				}
        }
    }

    public static void disableStencilTest() {
        // if we did enable the stencil test, disable it
        if (!wasStencilOn) {
            GL11C.glDisable(GL11C.GL_STENCIL_TEST);
        }
    }

    private static final ResourceLocation cubeFront = new ResourceLocation("textures/gui/title/background/panorama_0.png");
    private static final ResourceLocation cubeRight = new ResourceLocation("textures/gui/title/background/panorama_1.png");
    private static final ResourceLocation cubeBack = new ResourceLocation("textures/gui/title/background/panorama_2.png");
    private static final ResourceLocation cubeLeft = new ResourceLocation("textures/gui/title/background/panorama_3.png");
    private static final ResourceLocation cubeUp = new ResourceLocation("textures/gui/title/background/panorama_4.png");
    private static final ResourceLocation cubeDown = new ResourceLocation("textures/gui/title/background/panorama_5.png");
    private static final ResourceLocation dirt = new ResourceLocation("minecraft:textures/block/dirt.png");
    private static final ResourceLocation grass = new ResourceLocation("minecraft:textures/block/grass_block_top.png");

    public static void renderMenuPanorama(Matrix4fStack poseStack) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        poseStack.pushMatrix();

        poseStack.translate(-50F, -50F, -50.0F);

        // down
        RenderSystem.setShaderTexture(0, cubeDown);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(poseStack, 0, 0, 0)
            .uv(0, 0).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(poseStack, 0, 0, 100)
            .uv(0, 1).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(poseStack, 100, 0, 100)
            .uv(1, 1).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(poseStack, 100, 0, 0)
            .uv(1, 0).color(255, 255, 255, 255).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());

        // up
        RenderSystem.setShaderTexture(0, cubeUp);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(poseStack, 0, 100, 100)
            .uv(0, 0).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(poseStack, 0, 100, 0)
            .uv(0, 1).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(poseStack, 100, 100, 0)
            .uv(1, 1).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(poseStack, 100, 100, 100)
            .uv(1, 0).color(255, 255, 255, 255).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());

        // left
        RenderSystem.setShaderTexture(0, cubeLeft);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(poseStack, 0, 0, 0)
            .uv(1, 1).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(poseStack, 0, 100, 0)
            .uv(1, 0).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(poseStack, 0, 100, 100)
            .uv(0, 0).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(poseStack, 0, 0, 100)
            .uv(0, 1).color(255, 255, 255, 255).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());

        // right
        RenderSystem.setShaderTexture(0, cubeRight);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(poseStack, 100, 0, 0)
            .uv(0, 1).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(poseStack, 100, 0, 100)
            .uv(1, 1).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(poseStack, 100, 100, 100)
            .uv(1, 0).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(poseStack, 100, 100, 0)
            .uv(0, 0).color(255, 255, 255, 255).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());

        // front
        RenderSystem.setShaderTexture(0, cubeFront);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(poseStack, 0, 0, 0)
            .uv(0, 1).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(poseStack, 100, 0, 0)
            .uv(1, 1).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(poseStack, 100, 100, 0)
            .uv(1, 0).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(poseStack, 0, 100, 0)
            .uv(0, 0).color(255, 255, 255, 255).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());

        //back
        RenderSystem.setShaderTexture(0, cubeBack);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(poseStack, 0, 0, 100).
            uv(1, 1).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(poseStack, 0, 100, 100)
            .uv(1, 0).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(poseStack, 100, 100, 100)
            .uv(0, 0).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(poseStack, 100, 0, 100)
            .uv(0, 1).color(255, 255, 255, 255).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());

        poseStack.popMatrix();

        Vector2f area = dataHolder.vr.getPlayAreaSize();
        if (area == null) {
            area = new Vector2f(2, 2);
        }
        for (int i = 0; i < 2; i++) {
            float width = area.x + i * 2;
            float length = area.y + i * 2;

            poseStack.pushMatrix();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

            int r, g, b;
            if (i == 0) {
                RenderSystem.setShaderTexture(0, grass);
                // plains grass color, but a bit darker
                r = 114;
                g = 148;
                b = 70;
            } else {
                RenderSystem.setShaderTexture(0, dirt);
                r = g = b = 128;
            }

            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            poseStack.translate(-width / 2.0F, 0.0F, -length / 2.0F);

            final int repeat = 4; // texture wraps per meter

            bufferbuilder
                .vertex(poseStack, 0, 0.005f * -i, 0)
                .uv(0, 0)
                .color(r, g, b, 255)
                .normal(0, 1, 0).endVertex();
            bufferbuilder
                .vertex(poseStack, 0, 0.005f * -i, length)
                .uv(0, repeat * length)
                .color(r, g, b, 255)
                .normal(0, 1, 0).endVertex();
            bufferbuilder
                .vertex(poseStack, width, 0.005f * -i, length)
                .uv(repeat * width, repeat * length)
                .color(r, g, b, 255)
                .normal(0, 1, 0).endVertex();
            bufferbuilder
                .vertex(poseStack, width, 0.005f * -i, 0)
                .uv(repeat * width, 0)
                .color(r, g, b, 255)
                .normal(0, 1, 0).endVertex();

            BufferUploader.drawWithShader(bufferbuilder.end());
            poseStack.popMatrix();
        }
    }

    public static void renderJrbuddasAwesomeMainMenuRoomNew(Matrix4fStack pMatrixStack) {
        int repeat = 4; // texture wraps per meter
        float height = 2.5F;
        float oversize = 1.3F;
        Vector2f area = dataHolder.vr.getPlayAreaSize();
        if (area == null) {
            area = new Vector2f(2, 2);
        }

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, Screen.MENU_BACKGROUND);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        pMatrixStack.pushMatrix();

        float width = area.x + oversize;
        float length = area.y + oversize;
        pMatrixStack.translate(-width / 2.0F, 0.0F, -length / 2.0F);

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        float r, g, b, a;
        r = g = b = 0.8f;
        a = 1.0f;

        bufferbuilder.vertex(pMatrixStack, 0, 0, 0)
            .uv(0, 0).color(r, g, b, a).normal(0, 1, 0).endVertex();
        bufferbuilder.vertex(pMatrixStack, 0, 0, length)
            .uv(0, repeat * length).color(r, g, b, a).normal(0, 1, 0).endVertex();
        bufferbuilder.vertex(pMatrixStack, width, 0, length)
            .uv(repeat * width, repeat * length).color(r, g, b, a).normal(0, 1, 0).endVertex();
        bufferbuilder.vertex(pMatrixStack, width, 0, 0)
            .uv(repeat * width, 0).color(r, g, b, a).normal(0, 1, 0).endVertex();

        bufferbuilder.vertex(pMatrixStack, 0, height, length)
            .uv(0, 0).color(r, g, b, a).normal(0, -1, 0).endVertex();
        bufferbuilder.vertex(pMatrixStack, 0, height, 0)
            .uv(0, repeat * length).color(r, g, b, a).normal(0, -1, 0).endVertex();
        bufferbuilder.vertex(pMatrixStack, width, height, 0)
            .uv(repeat * width, repeat * length).color(r, g, b, a).normal(0, -1, 0).endVertex();
        bufferbuilder.vertex(pMatrixStack, width, height, length)
            .uv(repeat * width, 0).color(r, g, b, a).normal(0, -1, 0).endVertex();

        bufferbuilder.vertex(pMatrixStack, 0, 0, 0)
            .uv(0, 0).color(r, g, b, a).normal(1, 0, 0).endVertex();
        bufferbuilder.vertex(pMatrixStack, 0, height, 0)
            .uv(0, repeat * height).color(r, g, b, a).normal(1, 0, 0).endVertex();
        bufferbuilder.vertex(pMatrixStack, 0, height, length)
            .uv(repeat * length, repeat * height).color(r, g, b, a).normal(1, 0, 0).endVertex();
        bufferbuilder.vertex(pMatrixStack, 0, 0, length)
            .uv(repeat * length, 0).color(r, g, b, a).normal(1, 0, 0).endVertex();

        bufferbuilder.vertex(pMatrixStack, width, 0, 0)
            .uv(0, 0).color(r, g, b, a).normal(-1, 0, 0).endVertex();
        bufferbuilder.vertex(pMatrixStack, width, 0, length)
            .uv(repeat * length, 0).color(r, g, b, a).normal(-1, 0, 0).endVertex();
        bufferbuilder.vertex(pMatrixStack, width, height, length)
            .uv(repeat * length, repeat * height).color(r, g, b, a).normal(-1, 0, 0).endVertex();
        bufferbuilder.vertex(pMatrixStack, width, height, 0)
            .uv(0, repeat * height).color(r, g, b, a).normal(-1, 0, 0).endVertex();

        bufferbuilder.vertex(pMatrixStack, 0, 0, 0)
            .uv(0, 0).color(r, g, b, a).normal(0, 0, 1).endVertex();
        bufferbuilder.vertex(pMatrixStack, width, 0, 0)
            .uv(repeat * width, 0).color(r, g, b, a).normal(0, 0, 1).endVertex();
        bufferbuilder.vertex(pMatrixStack, width, height, 0)
            .uv(repeat * width, repeat * height).color(r, g, b, a).normal(0, 0, 1).endVertex();
        bufferbuilder.vertex(pMatrixStack, 0, height, 0)
            .uv(0, repeat * height).color(r, g, b, a).normal(0, 0, 1).endVertex();

        bufferbuilder.vertex(pMatrixStack, 0, 0, length).
            uv(0, 0).color(r, g, b, a).normal(0, 0, -1).endVertex();
        bufferbuilder.vertex(pMatrixStack, 0, height, length)
            .uv(0, repeat * height).color(r, g, b, a).normal(0, 0, -1).endVertex();
        bufferbuilder.vertex(pMatrixStack, width, height, length)
            .uv(repeat * width, repeat * height).color(r, g, b, a).normal(0, 0, -1).endVertex();
        bufferbuilder.vertex(pMatrixStack, width, 0, length)
            .uv(repeat * width, 0).color(r, g, b, a).normal(0, 0, -1).endVertex();

        BufferUploader.drawWithShader(bufferbuilder.end());
        pMatrixStack.popMatrix();
    }

    public static void renderTechjarsAwesomeMainMenuRoom(Matrix4fStack poseStack) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.enableDepthTest();
        //RenderSystem.enableTexture();
        RenderSystem.enableBlend();
        RenderSystem.enableCull();

        poseStack.pushMatrix();
        // transfer the  rotation
        RenderSystem.getModelViewStack().mul(poseStack, poseStack);
        RenderSystem.getModelViewStack().pushMatrix().identity();
        RenderSystem.applyModelViewMatrix();

        int tzOffset = Calendar.getInstance().get(Calendar.ZONE_OFFSET);
        dataHolder.menuWorldRenderer.time = dataHolder.menuWorldRenderer.fastTime
                                            ? (long) (dataHolder.menuWorldRenderer.ticks * 10L + 10 * mc.getFrameTime())
                                            : (long) ((System.currentTimeMillis() + tzOffset - 21600000) / 86400000D * 24000D);

        dataHolder.menuWorldRenderer.fogRenderer.setupFogColor();
        RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

        dataHolder.menuWorldRenderer.updateLightmap();
        dataHolder.menuWorldRenderer.render(poseStack);

        Vector2f area = dataHolder.vr.getPlayAreaSize();
        if (area != null) {
            poseStack.pushMatrix();
            float width = area.x;//(float)Math.ceil(area.x);
            float length = area.y;//(float)Math.ceil(area.y);

            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, Screen.MENU_BACKGROUND);
            float sun = dataHolder.menuWorldRenderer.getSkyDarken();
            RenderSystem.setShaderColor(sun, sun, sun, 0.3f);


            RenderSystem.defaultBlendFunc();
            RenderSystem.enableBlend();
            BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            poseStack.translate(-width / 2.0F, 0.0F, -length / 2.0F);
            bufferbuilder
                .vertex(poseStack, 0, 0.005f, 0)
                .uv(0, 0)
                .color(1f, 1f, 1f, 1f)
                .endVertex();
            bufferbuilder
                .vertex(poseStack, 0, 0.005f, length)
                .uv(0, 4 * length)
                .color(1f, 1f, 1f, 1f)
                .endVertex();
            bufferbuilder
                .vertex(poseStack, width, 0.005f, length)
                .uv(4 * width, 4 * length)
                .color(1f, 1f, 1f, 1f)
                .endVertex();
            bufferbuilder
                .vertex(poseStack, width, 0.005f, 0)
                .uv(4 * width, 0)
                .color(1f, 1f, 1f, 1f)
                .endVertex();

            BufferUploader.drawWithShader(bufferbuilder.end());

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            poseStack.popMatrix();
        }

        poseStack.popMatrix();
        RenderSystem.defaultBlendFunc();

        RenderSystem.getModelViewStack().popMatrix();
        RenderSystem.applyModelViewMatrix();
    }

    public static void renderVRFabulous(float partialTicks, LevelRenderer levelRenderer, boolean menuHandRight, boolean menuHandLeft) {
        if (ClientDataHolderVR.getInstance().currentPass == RenderPass.SCOPEL || ClientDataHolderVR.getInstance().currentPass == RenderPass.SCOPER) {
            return;
        }
        mc.getProfiler().popPush("VR");
        renderCrosshairAtDepth(!ClientDataHolderVR.getInstance().vrSettings.useCrosshairOcclusion);
        mc.getMainRenderTarget().unbindWrite();
        ((LevelRendererExtension) levelRenderer).vivecraft$getAlphaSortVROccludedFramebuffer().clear(Minecraft.ON_OSX);
        ((LevelRendererExtension) levelRenderer).vivecraft$getAlphaSortVROccludedFramebuffer().copyDepthFrom(mc.getMainRenderTarget());
        ((LevelRendererExtension) levelRenderer).vivecraft$getAlphaSortVROccludedFramebuffer().bindWrite(true);

        if (shouldOccludeGui()) {
            renderGuiAndShadow(partialTicks, false, false);
        }

        ((LevelRendererExtension) levelRenderer).vivecraft$getAlphaSortVRUnoccludedFramebuffer().clear(Minecraft.ON_OSX);
        ((LevelRendererExtension) levelRenderer).vivecraft$getAlphaSortVRUnoccludedFramebuffer().bindWrite(true);

        if (!shouldOccludeGui()) {
            renderGuiAndShadow(partialTicks, false, false);
        }

        renderVRSelfEffects(partialTicks);
        VRWidgetHelper.renderVRThirdPersonCamWidget();
        VRWidgetHelper.renderVRHandheldCameraWidget();
        boolean renderHands = VRArmHelper.shouldRenderHands();

        VRArmHelper.renderVRHands(partialTicks, renderHands && menuHandRight, renderHands && menuHandLeft, true, true);

        ((LevelRendererExtension) levelRenderer).vivecraft$getAlphaSortVRHandsFramebuffer().clear(Minecraft.ON_OSX);
        ((LevelRendererExtension) levelRenderer).vivecraft$getAlphaSortVRHandsFramebuffer().copyDepthFrom(mc.getMainRenderTarget());
        ((LevelRendererExtension) levelRenderer).vivecraft$getAlphaSortVRHandsFramebuffer().bindWrite(true);

        VRArmHelper.renderVRHands(partialTicks, renderHands && !menuHandRight, renderHands && !menuHandLeft, false, false);

        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        mc.getMainRenderTarget().bindWrite(true);
    }

    public static void renderVrFast(float partialTicks, boolean secondPass, boolean menuHandRight, boolean menuHandLeft) {
        if (dataHolder.currentPass == RenderPass.SCOPEL
            || dataHolder.currentPass == RenderPass.SCOPER) {
            return;
        }
        mc.getProfiler().popPush("VR");
        mc.gameRenderer.lightTexture().turnOffLightLayer();

        if (!secondPass) {
            renderCrosshairAtDepth(!dataHolder.vrSettings.useCrosshairOcclusion);
            VRWidgetHelper.renderVRThirdPersonCamWidget();
            VRWidgetHelper.renderVRHandheldCameraWidget();
        } else {
            renderGuiAndShadow(partialTicks, !shouldOccludeGui(), true);
        }

        // render hands in second pass when gui is open
        boolean renderHandsSecond = RadialHandler.isShowing() || KeyboardHandler.Showing || Minecraft.getInstance().screen != null;
        if (secondPass == renderHandsSecond) {
            // should render hands in second pass if menus are open, else in the first pass
            // only render the hands only once
            VRArmHelper.renderVRHands(partialTicks, VRArmHelper.shouldRenderHands(), VRArmHelper.shouldRenderHands(), menuHandRight, menuHandLeft);
        }
        renderVRSelfEffects(partialTicks);
    }

    private static boolean shouldOccludeGui() {
        Vec3 pos = dataHolder.vrPlayer.vrdata_world_render.getEye(dataHolder.currentPass)
            .getPosition();

        if (dataHolder.currentPass != RenderPass.THIRD
            && dataHolder.currentPass != RenderPass.CAMERA) {
            return !MethodHolder.isInMenuRoom() && mc.screen == null && !KeyboardHandler.Showing
                && !RadialHandler.isShowing() && dataHolder.vrSettings.hudOcclusion
                && !isInsideOpaqueBlock(pos);
        } else {
            return true;
        }
    }

    private static void renderGuiAndShadow(float partialTicks, boolean depthAlways, boolean shadowFirst) {
        if (shadowFirst) {
            VREffectsHelper.renderVrShadow(partialTicks, depthAlways);
        }
        if (Minecraft.getInstance().screen != null || !KeyboardHandler.Showing) {
            renderGuiLayer(partialTicks, depthAlways);
        }
        if (!shadowFirst) {
            VREffectsHelper.renderVrShadow(partialTicks, depthAlways);
        }

        if (KeyboardHandler.Showing) {
            if (dataHolder.vrSettings.physicalKeyboard) {
                renderPhysicalKeyboard(partialTicks);
            } else {
                render2D(partialTicks, KeyboardHandler.Framebuffer, KeyboardHandler.Pos_room,
                    KeyboardHandler.Rotation_room, depthAlways);
            }
        }

        if (RadialHandler.isShowing()) {
            render2D(partialTicks, RadialHandler.Framebuffer, RadialHandler.Pos_room,
                RadialHandler.Rotation_room, depthAlways);
        }
    }

    public static void renderVrShadow(float partialTicks, boolean depthAlways) {
        if (dataHolder.currentPass != RenderPass.THIRD && dataHolder.currentPass != RenderPass.CAMERA) {
            if (mc.player.isAlive()) {
                if (!(((PlayerExtension) mc.player).vivecraft$getRoomYOffsetFromPose() < 0.0D)) {
                    if (mc.player.getVehicle() == null) {
                        mc.getProfiler().push("vr shadow");
                        AABB aabb = mc.player.getBoundingBox();

                        if (dataHolder.vrSettings.vrShowBlueCircleBuddy && aabb != null) {
                            Matrix4f modelView = new Matrix4f();
                            RenderSystem.disableCull();

                            Vec3 vec3 = RenderHelper.getSmoothCameraPosition(dataHolder.currentPass, dataHolder.vrPlayer.vrdata_world_render);
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
                                0.0F, 0, 0, 0, 64, modelView);

                            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
                            RenderHelper.setupPolyRendering(false);
                            RenderSystem.enableCull();
                        }
                        mc.getProfiler().pop();
                    }
                }
            }
        }
    }

    private static void renderVRSelfEffects(float par1) {
        if (((GameRendererExtension) mc.gameRenderer).vivecraft$isOnFire() && dataHolder.currentPass != RenderPass.THIRD
            && dataHolder.currentPass != RenderPass.CAMERA) {
            VREffectsHelper.renderFireInFirstPerson();
        }
        mc.gameRenderer.renderItemActivationAnimation(0, 0, par1);
    }

    public static void renderFireInFirstPerson() {
        PoseStack posestack = new PoseStack();
        RenderHelper.applyStereo(dataHolder.currentPass, posestack);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);

        if (dataHolder.currentPass == RenderPass.THIRD
            || dataHolder.currentPass == RenderPass.CAMERA) {
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

        float u0 = Mth.lerp(ShrinkRatio, uMin, uMid);
        float u1 = Mth.lerp(ShrinkRatio, uMax, uMid);
        float v0 = Mth.lerp(ShrinkRatio, vMin, vMid);
        float v1 = Mth.lerp(ShrinkRatio, vMax, vMid);

        float a = 0.3F;
        float b = (float) (dataHolder.vrPlayer.vrdata_world_render.getHeadPivot().y - ((GameRendererExtension) mc.gameRenderer).vivecraft$getRveY());

        for (int i = 0; i < 4; ++i) {
            posestack.pushPose();
            posestack.mulPose(Axis.YP.rotationDegrees(
                i * 90.0F - dataHolder.vrPlayer.vrdata_world_render.getBodyYaw()));
            posestack.translate(0.0D, -b, 0.0D);
            Matrix4f matrix4f = posestack.last().pose();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex(matrix4f, -a, 0.0F, -a)
                .uv(u1, v1).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            bufferbuilder.vertex(matrix4f, a, 0.0F, -a)
                .uv(u0, v1).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            bufferbuilder.vertex(matrix4f, a, b, -a)
                .uv(u0, v0).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            bufferbuilder.vertex(matrix4f, -a, b, -a)
                .uv(u1, v0).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            BufferUploader.drawWithShader(bufferbuilder.end());

            posestack.popPose();
        }

        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.disableBlend();
    }

    public static void renderPhysicalKeyboard(float partialTicks) {
        if (!dataHolder.bowTracker.isDrawing) {
            ((GameRendererExtension) mc.gameRenderer).vivecraft$resetProjectionMatrix(partialTicks);

            mc.getProfiler().push("applyPhysicalKeyboardModelView");
            Vec3 eye = RenderHelper.getSmoothCameraPosition(dataHolder.currentPass, dataHolder.vrPlayer.vrdata_world_render);

            //convert previously calculated coords to world coords
            Vec3 guiPos = VRPlayer.room_to_world_pos(KeyboardHandler.Pos_room, dataHolder.vrPlayer.vrdata_world_render);
            org.vivecraft.common.utils.math.Matrix4f rot = org.vivecraft.common.utils.math.Matrix4f.rotationY(dataHolder.vrPlayer.vrdata_world_render.rotation_radians);
            org.vivecraft.common.utils.math.Matrix4f guiRot = org.vivecraft.common.utils.math.Matrix4f.multiply(rot, KeyboardHandler.Rotation_room);

            Matrix4fStack modelView = new Matrix4fStack(3);

            // offset from eye to gui pos
            modelView.translate((float) (guiPos.x - eye.x), (float) (guiPos.y - eye.y), (float) (guiPos.z - eye.z));
            modelView.mul(guiRot.toMCMatrix());

            float scale = dataHolder.vrPlayer.vrdata_world_render.worldScale;
            modelView.scale(scale, scale, scale);

            mc.getProfiler().pop();

            KeyboardHandler.physicalKeyboard.render(modelView);
        }
    }

    private static void renderScreen(Matrix4f poseStack, RenderTarget screenFramebuffer, boolean depthAlways, boolean noFog, Vec3 screenPos) {
        screenFramebuffer.bindRead();
        RenderSystem.disableCull();
        RenderSystem.setShaderTexture(0, screenFramebuffer.getColorTextureId());

        // cache fog distance
        float fogStart = RenderSystem.getShaderFogStart();
        float[] color = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
        if (!MethodHolder.isInMenuRoom()) {
            if (mc.screen == null) {
                color[3] = dataHolder.vrSettings.hudOpacity;
            }
            if (noFog || mc.screen != null) {
                // disable fog for menus
                RenderSystem.setShaderFogStart(Float.MAX_VALUE);
            }

            if (mc.player != null && mc.player.isShiftKeyDown()) {
                color[3] *= 0.75F;
            }

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE_MINUS_DST_ALPHA, GlStateManager.DestFactor.ONE);
            if (dataHolder.vrSettings.shaderGUIRender == VRSettings.ShaderGUIRender.BEFORE_TRANSLUCENT_SOLID && ShadersHelper.isShaderActive()) {
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
                screenPos = dataHolder.vrPlayer.vrdata_world_render.hmd.getPosition();
            }

            int minLight = ShadersHelper.ShaderLight();
            int light = Utils.getCombinedLightWithMin(mc.level, BlockPos.containing(screenPos), minLight);
            RenderHelper.drawSizedQuadWithLightmapCutout((float) mc.getWindow().getGuiScaledWidth(),
                (float) mc.getWindow().getGuiScaledHeight(), 1.5F, light, color,
                poseStack, false);
        } else {
            RenderHelper.drawSizedQuad((float) mc.getWindow().getGuiScaledWidth(),
                (float) mc.getWindow().getGuiScaledHeight(), 1.5F, color,
                poseStack);
        }

        // reset fog
        RenderSystem.setShaderFogStart(fogStart);
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
    }

    public static void renderGuiLayer(float partialTicks, boolean depthAlways) {
        if (!dataHolder.bowTracker.isDrawing) {
            if (mc.screen != null || !mc.options.hideGui) {
                if (!RadialHandler.isShowing()) {
                    mc.getProfiler().push("GuiLayer");

                    Matrix4fStack modelView = new Matrix4fStack(8);
                    // remove nausea effect from projection matrix
                    ((GameRendererExtension) mc.gameRenderer).vivecraft$resetProjectionMatrix(partialTicks);

                    // MAIN MENU ENVIRONMENT
                    if (MethodHolder.isInMenuRoom()) {
                        // render the screen always on top in the menu room to prevent z fighting
                        depthAlways = true;

                        modelView.pushMatrix();
                        Vec3 eye = RenderHelper.getSmoothCameraPosition(dataHolder.currentPass, dataHolder.vrPlayer.vrdata_world_render);
                        modelView.translate((float) (dataHolder.vrPlayer.vrdata_world_render.origin.x - eye.x),
                            (float) (dataHolder.vrPlayer.vrdata_world_render.origin.y - eye.y),
                            (float) (dataHolder.vrPlayer.vrdata_world_render.origin.z - eye.z));

                        // remove world rotation or the room doesn't align with the screen
                        modelView.rotate(Axis.YN.rotation(-dataHolder.vrPlayer.vrdata_world_render.rotation_radians));

                        if (dataHolder.menuWorldRenderer.isReady()) {
                            try {
                                renderTechjarsAwesomeMainMenuRoom(modelView);
                            } catch (Exception exception) {
                                System.out.println("Error rendering main menu world, unloading to prevent more errors");
                                exception.printStackTrace();
                                dataHolder.menuWorldRenderer.destroy();
                            }
                        } else {
                            if (dataHolder.vrSettings.menuWorldFallbackPanorama) {
                                renderMenuPanorama(modelView);
                            } else {
                                renderJrbuddasAwesomeMainMenuRoomNew(modelView);
                            }
                        }
                        modelView.popMatrix();
                    }
                    // END AWESOME MAIN MENU ENVIRONMENT

                    Vec3 guiPos = GuiHandler.applyGUIModelView(dataHolder.currentPass, modelView);

                    renderScreen(modelView, GuiHandler.guiFramebuffer, depthAlways, false, guiPos);

                    mc.getProfiler().pop();
                }
            }
        }
    }

    public static void render2D(float partialTicks, RenderTarget framebuffer, Vec3 pos, org.vivecraft.common.utils.math.Matrix4f rot, boolean depthAlways) {
        if (!dataHolder.bowTracker.isDrawing) {
            // remove nausea effect from projection matrix
            ((GameRendererExtension) mc.gameRenderer).vivecraft$resetProjectionMatrix(partialTicks);

            mc.getProfiler().push("apply2DModelView");

            Vec3 eye = RenderHelper.getSmoothCameraPosition(dataHolder.currentPass, dataHolder.vrPlayer.vrdata_world_render);

            Vec3 guiPos = VRPlayer.room_to_world_pos(pos, dataHolder.vrPlayer.vrdata_world_render);
            org.vivecraft.common.utils.math.Matrix4f yRot = org.vivecraft.common.utils.math.Matrix4f
                .rotationY(dataHolder.vrPlayer.vrdata_world_render.rotation_radians);
            org.vivecraft.common.utils.math.Matrix4f guiRot = org.vivecraft.common.utils.math.Matrix4f.multiply(yRot, rot);

            Matrix4f modelView = new Matrix4f();

            modelView.translate((float) (guiPos.x - eye.x), (float) (guiPos.y - eye.y), (float) (guiPos.z - eye.z));
            modelView.mul(guiRot.toMCMatrix());

            float scale = GuiHandler.guiScale * dataHolder.vrPlayer.vrdata_world_render.worldScale;
            modelView.scale(scale, scale, scale);

            mc.getProfiler().pop();

            renderScreen(modelView, framebuffer, depthAlways, true, guiPos);
        }
    }

    public static void renderFaceOverlay(float partialTicks) {
        if (((GameRendererExtension) mc.gameRenderer).vivecraft$isInBlock() > 0.0F) {
            renderFaceInBlock();

            renderGuiAndShadow(partialTicks, true, true);

            VRArmHelper.renderVRHands(partialTicks, true, true, true, true);
        }
    }

    public static void renderFaceInBlock() {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0f);

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
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(mat, -1.5F, -1.5F, 0.0F).endVertex();
        bufferbuilder.vertex(mat, 1.5F, -1.5F, 0.0F).endVertex();
        bufferbuilder.vertex(mat, 1.5F, 1.5F, 0.0F).endVertex();
        bufferbuilder.vertex(mat, -1.5F, 1.5F, 0.0F).endVertex();
        tesselator.end();
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static boolean shouldRenderCrosshair() {
        if (ClientDataHolderVR.viewonly) {
            return false;
        } else if (mc.level == null) {
            return false;
        } else if (mc.screen != null) {
            return false;
        } else if (dataHolder.vrSettings.renderInGameCrosshairMode != VRSettings.RenderPointerElement.ALWAYS
            && (dataHolder.vrSettings.renderInGameCrosshairMode != VRSettings.RenderPointerElement.WITH_HUD
            || mc.options.hideGui)) {
            return false;
        } else if (dataHolder.currentPass != RenderPass.LEFT
            && dataHolder.currentPass != RenderPass.RIGHT
            && dataHolder.currentPass != RenderPass.CENTER) {
            // it doesn't look very good
            return false;
        } else if (KeyboardHandler.Showing) {
            return false;
        } else if (RadialHandler.isUsingController(ControllerType.RIGHT)) {
            return false;
        } else if (GuiHandler.guiPos_room != null) {
            // don't show it, when a screen is open, or a popup
            return false;
        } else if (dataHolder.bowTracker.isNotched()) {
            return false;
        } else if (dataHolder.vr.getInputAction(VivecraftVRMod.INSTANCE.keyVRInteract).isEnabledRaw(ControllerType.RIGHT)
            || VivecraftVRMod.INSTANCE.keyVRInteract.isDown(ControllerType.RIGHT)) {
            return false;
        } else if (dataHolder.vr.getInputAction(VivecraftVRMod.INSTANCE.keyClimbeyGrab).isEnabledRaw(ControllerType.RIGHT)
            || VivecraftVRMod.INSTANCE.keyClimbeyGrab.isDown(ControllerType.RIGHT)) {
            return false;
        } else if (dataHolder.teleportTracker.isAiming()) {
            return false;
        } else if (dataHolder.climbTracker.isGrabbingLadder(0)) {
            return false;
        } else {
            return !(dataHolder.vrPlayer.worldScale > 15.0F);
        }
    }

    public static void renderCrosshairAtDepth(boolean depthAlways) {
        if (shouldRenderCrosshair()) {
            mc.getProfiler().push("crosshair");

            // white crosshair, with blending
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Vec3 crosshairRenderPos = ((GameRendererExtension) mc.gameRenderer).vivecraft$getCrossVec();
            Vec3 aim = crosshairRenderPos.subtract(dataHolder.vrPlayer.vrdata_world_render.getController(0).getPosition());
            float crossDepth = (float) aim.length();
            float scale = (float) (0.125F * dataHolder.vrSettings.crosshairScale * Math.sqrt(dataHolder.vrPlayer.vrdata_world_render.worldScale));

            //scooch closer a bit for light calc.
            crosshairRenderPos = crosshairRenderPos.add(aim.normalize().scale(-0.01D));

            Matrix4f modelView = new Matrix4f();

            Vec3 translate = crosshairRenderPos.subtract(mc.getCameraEntity().position());
            modelView.translate((float) translate.x, (float) translate.y, (float) translate.z);

            if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockhitresult = (BlockHitResult) mc.hitResult;

                switch (blockhitresult.getDirection()) {
                    case DOWN -> {
                        MethodHolder.rotateDeg(modelView,
                            dataHolder.vrPlayer.vrdata_world_render.getController(0).getYaw(), 0.0F, 1.0F,
                            0.0F);
                        MethodHolder.rotateDeg(modelView, -90.0F, 1.0F, 0.0F, 0.0F);
                    }
                    case UP -> {
                        MethodHolder.rotateDeg(modelView,
                            -dataHolder.vrPlayer.vrdata_world_render.getController(0).getYaw(), 0.0F,
                            1.0F, 0.0F);
                        MethodHolder.rotateDeg(modelView, 90.0F, 1.0F, 0.0F, 0.0F);
                    }
                    case WEST -> MethodHolder.rotateDeg(modelView, 90.0F, 0.0F, 1.0F, 0.0F);
                    case EAST -> MethodHolder.rotateDeg(modelView, -90.0F, 0.0F, 1.0F, 0.0F);
                    case SOUTH -> MethodHolder.rotateDeg(modelView, 180.0F, 0.0F, 1.0F, 0.0F);
                }
            } else {
                MethodHolder.rotateDeg(modelView,
                    -dataHolder.vrPlayer.vrdata_world_render.getController(0).getYaw(), 0.0F, 1.0F,
                    0.0F);
                MethodHolder.rotateDeg(modelView,
                    -dataHolder.vrPlayer.vrdata_world_render.getController(0).getPitch(), 1.0F, 0.0F,
                    0.0F);
            }

            if (dataHolder.vrSettings.crosshairScalesWithDistance) {
                float depthscale = 0.3F + 0.2F * crossDepth;
                scale *= depthscale;
            }

            mc.gameRenderer.lightTexture().turnOnLightLayer();
            modelView.scale(scale, scale, scale);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();

            if (depthAlways) {
                RenderSystem.depthFunc(GL11C.GL_ALWAYS);
            } else {
                RenderSystem.depthFunc(GL11C.GL_LEQUAL);
            }

            RenderSystem.enableBlend(); // Fuck it, we want a proper crosshair
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                GlStateManager.DestFactor.ZERO, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            int light = LevelRenderer.getLightColor(mc.level, BlockPos.containing(crosshairRenderPos));
            float brightness = 1.0F;

            if (mc.hitResult == null || mc.hitResult.getType() == HitResult.Type.MISS) {
                brightness = 0.5F;
            }

            TextureAtlasSprite crosshairSprite = Minecraft.getInstance().getGuiSprites().getSprite(Gui.CROSSHAIR_SPRITE);
            RenderSystem.setShaderTexture(0, crosshairSprite.atlasLocation());
            float uMax = 15.0F / 256.0F;
            float vMax = 15.0F / 256.0F;

            BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();

            RenderSystem.setShader(GameRenderer::getRendertypeEntityCutoutNoCullShader);
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);

            bufferbuilder.vertex(modelView, -1.0F, 1.0F, 0.0F).color(brightness, brightness, brightness, 1.0F)
                .uv(crosshairSprite.getU0(), crosshairSprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(modelView, 1.0F, 1.0F, 0.0F).color(brightness, brightness, brightness, 1.0F)
                .uv(crosshairSprite.getU1(), crosshairSprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(modelView, 1.0F, -1.0F, 0.0F).color(brightness, brightness, brightness, 1.0F)
                .uv(crosshairSprite.getU1(), crosshairSprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(modelView, -1.0F, -1.0F, 0.0F).color(brightness, brightness, brightness, 1.0F)
                .uv(crosshairSprite.getU0(), crosshairSprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();

            BufferUploader.drawWithShader(bufferbuilder.end());

            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
            mc.getProfiler().pop();
        }
    }
}
