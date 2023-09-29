package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;
import org.vivecraft.client.extensions.RenderTargetExtension;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.mixin.client.blaze3d.RenderSystemAccessor;

import java.util.function.Supplier;

import static org.joml.Math.PI;
import static org.joml.Math.toRadians;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.convertToVector3f;

public class RenderHelper {

    private static int polyBlendSrcA;
    private static int polyBlendDstA;
    private static int polyBlendSrcRGB;
    private static int polyBlendDstRGB;
    private static boolean polyBlend;
    private static boolean polyTex;
    private static boolean polyLight;
    private static boolean polyCull;

    public static void applyVRModelView(RenderPass currentPass, PoseStack poseStack) {
        Matrix4f modelView = dh.vrPlayer.vrdata_world_render.getEye(currentPass).getMatrix();
        poseStack.last().pose().mul(modelView);
        poseStack.last().normal().mul(new Matrix3f(modelView));
    }

    public static void applyStereo(RenderPass currentPass, PoseStack matrix) {
        if (currentPass == RenderPass.LEFT || currentPass == RenderPass.RIGHT) {
            matrix.last().pose().translate(convertToVector3f(
                dh.vrPlayer.vrdata_world_render.getEye(currentPass).getPosition()
                    .subtract(dh.vrPlayer.vrdata_world_render.getEye(RenderPass.CENTER).getPosition()),
                new Vector3f()
            ));
            // TODO .negate()?
        }
    }

    public static Vec3 getControllerRenderPos(int c) {
        if (!dh.vrSettings.seated) {
            return dh.vrPlayer.vrdata_world_render.getController(c).getPosition();
        } else {
            Vec3 out = null;

            if (mc.getCameraEntity() != null && mc.level != null) {
                Vec3 dir = dh.vrPlayer.vrdata_world_render.hmd.getDirection();
                dir = dir.yRot((float) toRadians(c == 0 ? -35.0D : 35.0D));
                dir = new Vec3(dir.x, 0.0D, dir.z);
                dir = dir.normalize();
                RenderPass renderpass = RenderPass.CENTER;
                if (TelescopeTracker.isTelescope(mc.player.getUseItem())) {
                    if (c == 0 && mc.player.getUsedItemHand() == InteractionHand.MAIN_HAND) {
                        out = dh.vrPlayer.vrdata_world_render.eye0.getPosition()
                            .add(dh.vrPlayer.vrdata_world_render.hmd.getDirection()
                                .scale(0.2 * dh.vrPlayer.vrdata_world_render.worldScale));
                    }
                    if (c == 1 && mc.player.getUsedItemHand() == InteractionHand.OFF_HAND) {
                        out = dh.vrPlayer.vrdata_world_render.eye1.getPosition()
                            .add(dh.vrPlayer.vrdata_world_render.hmd.getDirection()
                                .scale(0.2 * dh.vrPlayer.vrdata_world_render.worldScale));
                    }
                }
                if (out == null) {
                    out = dh.vrPlayer.vrdata_world_render.getEye(renderpass).getPosition().add(
                        dir.x * 0.3D * dh.vrPlayer.vrdata_world_render.worldScale,
                        -0.4D * dh.vrPlayer.vrdata_world_render.worldScale,
                        dir.z * 0.3D * dh.vrPlayer.vrdata_world_render.worldScale);
                }
            } else { //main menu
                Vec3 dir = dh.vrPlayer.vrdata_world_render.hmd.getDirection();
                dir = dir.yRot((float) toRadians(c == 0 ? -35.0D : 35.0D));
                dir = new Vec3(dir.x, 0.0D, dir.z);
                dir = dir.normalize();
                out = dh.vrPlayer.vrdata_world_render.hmd.getPosition().add(dir.x * 0.3D, -0.4D,
                    dir.z * 0.3D);
            }
            return out;
        }
    }

    public static void setupRenderingAtController(int controller, PoseStack matrix) {
        Vec3 aimSource = getControllerRenderPos(controller);
        aimSource = aimSource.subtract(dh.vrPlayer.getVRDataWorld().getEye(dh.currentPass).getPosition());
        matrix.last().pose().translate((float) aimSource.x, (float) aimSource.y, (float) aimSource.z);
        float sc = dh.vrPlayer.vrdata_world_render.worldScale;
        if (mc.level != null && TelescopeTracker.isTelescope(mc.player.getUseItem())) {
            matrix.last().pose()
                .mul(dh.vrPlayer.vrdata_world_render.hmd.getMatrix())
                .rotateX(toRadians(90.0F))
                .translate(controller == 0 ? 0.075F * sc : -0.075F * sc, -0.025F * sc, 0.0325F * sc);
            matrix.last().normal().rotateX(toRadians(90.0F));
        } else {
            matrix.last().pose().mul(dh.vrPlayer.vrdata_world_render.getController(controller).getMatrix());
        }

        matrix.scale(sc, sc, sc);
    }

    public static void renderDebugAxes(int r, int g, int b, float radius) {
        setupPolyRendering(true);
        RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
        renderCircle(new Vec3(0.0D, 0.0D, 0.0D), radius, 32, r, g, b, 255, 0);
        renderCircle(new Vec3(0.0D, 0.01D, 0.0D), radius * 0.75F, 32, r, g, b, 255, 0);
        renderCircle(new Vec3(0.0D, 0.02D, 0.0D), radius * 0.25F, 32, r, g, b, 255, 0);
        renderCircle(new Vec3(0.0D, 0.0D, 0.15D), radius * 0.5F, 32, r, g, b, 255, 2);
        setupPolyRendering(false);
    }

    public static void renderCircle(Vec3 pos, float radius, int edges, int r, int g, int b, int a, int side) {
        Tesselator tesselator = Tesselator.getInstance();
        tesselator.getBuilder().begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        tesselator.getBuilder().vertex(pos.x, pos.y, pos.z).color(r, g, b, a).endVertex();

        for (int i = 0; i < edges + 1; i++) {
            float startAngle = (float) i / (float) edges * (float) PI * 2.0F;
            switch(side) {
                case 0, 1 -> { //y
                    float x = (float) pos.x + (float) Math.cos(startAngle) * radius;
                    float y = (float) pos.y;
                    float z = (float) pos.z + (float) Math.sin(startAngle) * radius;
                    tesselator.getBuilder().vertex(x, y, z).color(r, g, b, a).endVertex();
                }
                case 2, 3 -> { //z
                    float x = (float) pos.x + (float) Math.cos(startAngle) * radius;
                    float y = (float) pos.y + (float) Math.sin(startAngle) * radius;
                    float z = (float) pos.z;
                    tesselator.getBuilder().vertex(x, y, z).color(r, g, b, a).endVertex();
                }
                case 4, 5 -> { //x
                    float x = (float) pos.x;
                    float y = (float) pos.y + (float) Math.cos(startAngle) * radius;
                    float z = (float) pos.z + (float) Math.sin(startAngle) * radius;
                    tesselator.getBuilder().vertex(x, y, z).color(r, g, b, a).endVertex();
                }
            }
        }
        tesselator.end();
    }

    public static void setupPolyRendering(boolean enable) {
        // boolean shadersMod = Config.isShaders(); TODO
        // boolean shadersModShadowPass = false;

        if (enable) {
            polyBlendSrcA = GlStateManager.BLEND.srcAlpha;
            polyBlendDstA = GlStateManager.BLEND.dstAlpha;
            polyBlendSrcRGB = GlStateManager.BLEND.srcRgb;
            polyBlendDstRGB = GlStateManager.BLEND.dstRgb;
            polyBlend = GL11C.glIsEnabled(GL11C.GL_BLEND);
            polyTex = true; // GL11C.glIsEnabled(GL11C.GL_TEXTURE_2D);
            polyLight = false;
            polyCull = true; // GL11C.glIsEnabled(GL11C.GL_CULL_FACE);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            // GlStateManager._disableLighting();
            RenderSystem.disableCull();

            // if (shadersMod) {
            //     this.prog = Shaders.activeProgram; TODO
            //     Shaders.useProgram(Shaders.ProgramTexturedLit);
            // }
        } else {
            RenderSystem.blendFuncSeparate(polyBlendSrcRGB, polyBlendDstRGB, polyBlendSrcA, polyBlendDstA);

            if (!polyBlend) {
                RenderSystem.disableBlend();
            }

            if (polyTex) {
            }

            if (polyLight) {
                // GlStateManager._enableLighting();
            }

            if (polyCull) {
                RenderSystem.enableCull();
            }

            // if (shadersMod && polyTex) {
            // Shaders.useProgram(this.prog); TODO
            // }
        }
    }

    public static void drawScreen(float f, Screen screen, GuiGraphics guiGraphics) {
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.setIdentity();
        posestack.translate(0.0D, 0.0D, -2000.0D);
        RenderSystem.applyModelViewMatrix();

        RenderSystem.blendFuncSeparate(
            SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE
        );

        screen.render(guiGraphics, 0, 0, f);

        RenderSystem.blendFuncSeparate(
            SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE
        );

        posestack.popPose();
        RenderSystem.applyModelViewMatrix();

        RenderTarget main = mc.getMainRenderTarget();
        main.bindRead();
        ((RenderTargetExtension) main).vivecraft$genMipMaps();
        main.unbindRead();
    }


    public static void drawSizedQuad(float displayWidth, float displayHeight, float size) {
        drawSizedQuad(displayWidth, displayHeight, size, new float[]{1, 1, 1, 1});
    }

    public static void drawSizedQuad(float displayWidth, float displayHeight, float size, float[] color) {
        float aspect = displayHeight / displayWidth;

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        bufferbuilder.vertex((-(size / 2.0F)), (-(size * aspect) / 2.0F), 0.0D)
            .uv(0.0F, 0.0F)
            .color(color[0], color[1], color[2], color[3])
            .normal(0.0F, 0.0F, 1.0F)
            .endVertex();
        bufferbuilder.vertex((size / 2.0F), (-(size * aspect) / 2.0F), 0.0D)
            .uv(1.0F, 0.0F)
            .color(color[0], color[1], color[2], color[3])
            .normal(0.0F, 0.0F, 1.0F)
            .endVertex();
        bufferbuilder.vertex((size / 2.0F), (size * aspect / 2.0F), 0.0D)
            .uv(1.0F, 1.0F)
            .color(color[0], color[1], color[2], color[3])
            .normal(0.0F, 0.0F, 1.0F)
            .endVertex();
        bufferbuilder.vertex((-(size / 2.0F)), (size * aspect / 2.0F), 0.0D)
            .uv(0.0F, 1.0F)
            .color(color[0], color[1], color[2], color[3])
            .normal(0.0F, 0.0F, 1.0F)
            .endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }

    public static void drawSizedQuad(float displayWidth, float displayHeight, float size, float[] color, Matrix4f pMatrix) {
        float aspect = displayHeight / displayWidth;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder
            .vertex(pMatrix, (-(size / 2.0F)), (-(size * aspect) / 2.0F), 0)
            .uv(0.0F, 0.0F)
            .endVertex();
        bufferbuilder
            .vertex(pMatrix, (size / 2.0F), (-(size * aspect) / 2.0F), 0)
            .uv(1.0F, 0.0F)
            .endVertex();
        bufferbuilder
            .vertex(pMatrix, (size / 2.0F), (size * aspect / 2.0F), 0)
            .uv(1.0F, 1.0F)
            .endVertex();
        bufferbuilder
            .vertex(pMatrix, (-(size / 2.0F)), (size * aspect / 2.0F), 0)
            .uv(0.0F, 1.0F)
            .endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }

    public static void drawSizedQuadWithLightmapCutout(float displayWidth, float displayHeight, float size, int lighti, Matrix4f pMatrix) {
        drawSizedQuadWithLightmapCutout(displayWidth, displayHeight, size, lighti, new float[]{1, 1, 1, 1}, pMatrix);
    }

    public static void drawSizedQuadWithLightmapCutout(float displayWidth, float displayHeight, float size, int lighti,
        float[] color, Matrix4f pMatrix) {
        drawSizedQuadWithLightmap(displayWidth, displayHeight, size, lighti, color, pMatrix, GameRenderer::getRendertypeEntityCutoutNoCullShader);
    }

    public static void drawSizedQuadSolid(float displayWidth, float displayHeight, float size,
        float[] color, Matrix4f pMatrix) {
        drawSizedQuadWithLightmap(displayWidth, displayHeight, size, LightTexture.pack(15, 15), color, pMatrix, GameRenderer::getRendertypeEntitySolidShader);
    }

    public static void drawSizedQuadWithLightmap(float displayWidth, float displayHeight, float size, int lighti,
        float[] color, Matrix4f pMatrix, Supplier<ShaderInstance> shader) {
        float aspect = displayHeight / displayWidth;
        RenderSystem.setShader(shader);
        mc.gameRenderer.lightTexture().turnOnLightLayer();
        mc.gameRenderer.overlayTexture().setupOverlayColor();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);

        // store old lights
        Vector3f light0Old = RenderSystemAccessor.getShaderLightDirections()[0];
        Vector3f light1Old = RenderSystemAccessor.getShaderLightDirections()[1];

        // set lights to front
        RenderSystem.setShaderLights(new Vector3f(0, 0, 1), new Vector3f(0, 0, 1));
        RenderSystem.setupShaderLights(RenderSystem.getShader());

        bufferbuilder.vertex(pMatrix, (-(size / 2.0F)), (-(size * aspect) / 2.0F), 0).color(color[0], color[1], color[2], color[3])
            .uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lighti).normal(0, 0, 1).endVertex();
        bufferbuilder.vertex(pMatrix, (size / 2.0F), (-(size * aspect) / 2.0F), 0).color(color[0], color[1], color[2], color[3])
            .uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lighti).normal(0, 0, 1).endVertex();
        bufferbuilder.vertex(pMatrix, (size / 2.0F), (size * aspect / 2.0F), 0).color(color[0], color[1], color[2], color[3])
            .uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lighti).normal(0, 0, 1).endVertex();
        bufferbuilder.vertex(pMatrix, (-(size / 2.0F)), (size * aspect / 2.0F), 0).color(color[0], color[1], color[2], color[3])
            .uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lighti).normal(0, 0, 1).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());

        mc.gameRenderer.lightTexture().turnOffLightLayer();

        // reset lights
        if (light0Old != null && light1Old != null) {
            RenderSystem.setShaderLights(light0Old, light1Old);
            RenderSystem.setupShaderLights(RenderSystem.getShader());
        }
    }

    public static void renderFlatQuad(Vec3 pos, float width, float height, float yaw, int r, int g, int b, int a, PoseStack poseStack) {
        Tesselator tesselator = Tesselator.getInstance();
        tesselator.getBuilder().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);

        Vec3 offset = (new Vec3((width / 2.0F), 0.0, height / 2.0F)).yRot(toRadians(-yaw));

        Matrix4f mat = poseStack.last().pose();
        tesselator.getBuilder().vertex(mat, (float) (pos.x + offset.x), (float) pos.y, (float) (pos.z + offset.z))
            .color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
        tesselator.getBuilder().vertex(mat, (float) (pos.x + offset.x), (float) pos.y, (float) (pos.z - offset.z))
            .color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
        tesselator.getBuilder().vertex(mat, (float) (pos.x - offset.x), (float) pos.y, (float) (pos.z - offset.z))
            .color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
        tesselator.getBuilder().vertex(mat, (float) (pos.x - offset.x), (float) pos.y, (float) (pos.z + offset.z))
            .color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
        tesselator.end();
    }

    public static void renderBox(Tesselator tes, Vec3 start, Vec3 end, float minX, float maxX, float minY, float maxY, Vec3i color, byte alpha, PoseStack poseStack) {
        Vec3 forward = start.subtract(end).normalize();
        Vec3 right = forward.cross(new Vec3(0.0D, 1.0D, 0.0D));
        Vec3 up = right.cross(forward);

        Vec3 left = right.scale(minX);
        right = right.scale(maxX);

        Vec3 down = up.scale(minY);
        up = up.scale(maxY);

        Vec3 upNormal = up.normalize();
        Vec3 rightNormal = right.normalize();

        Vec3 backRightBottom = start.add(right.x + down.x, right.y + down.y, right.z + down.z);
        Vec3 backRightTop = start.add(right.x + up.x, right.y + up.y, right.z + up.z);
        Vec3 backLeftBottom = start.add(left.x + down.x, left.y + down.y, left.z + down.z);
        Vec3 backLeftTop = start.add(left.x + up.x, left.y + up.y, left.z + up.z);

        Vec3 frontRightBottom = end.add(right.x + down.x, right.y + down.y, right.z + down.z);
        Vec3 frontRightTop = end.add(right.x + up.x, right.y + up.y, right.z + up.z);
        Vec3 frontLeftBottom = end.add(left.x + down.x, left.y + down.y, left.z + down.z);
        Vec3 frontLeftTop = end.add(left.x + up.x, left.y + up.y, left.z + up.z);

        BufferBuilder bufferbuilder = tes.getBuilder();
        Matrix4f mat = poseStack.last().pose();

        addVertex(bufferbuilder, mat, backRightBottom, color, alpha, forward);
        addVertex(bufferbuilder, mat, backLeftBottom, color, alpha, forward);
        addVertex(bufferbuilder, mat, backLeftTop, color, alpha, forward);
        addVertex(bufferbuilder, mat, backRightTop, color, alpha, forward);

        forward.reverse();
        addVertex(bufferbuilder, mat, frontLeftBottom, color, alpha, forward);
        addVertex(bufferbuilder, mat, frontRightBottom, color, alpha, forward);
        addVertex(bufferbuilder, mat, frontRightTop, color, alpha, forward);
        addVertex(bufferbuilder, mat, frontLeftTop, color, alpha, forward);

        addVertex(bufferbuilder, mat, frontRightBottom, color, alpha, rightNormal);
        addVertex(bufferbuilder, mat, backRightBottom, color, alpha, rightNormal);
        addVertex(bufferbuilder, mat, backRightTop, color, alpha, rightNormal);
        addVertex(bufferbuilder, mat, frontRightTop, color, alpha, rightNormal);

        rightNormal.reverse();
        addVertex(bufferbuilder, mat, backLeftBottom, color, alpha, rightNormal);
        addVertex(bufferbuilder, mat, frontLeftBottom, color, alpha, rightNormal);
        addVertex(bufferbuilder, mat, frontLeftTop, color, alpha, rightNormal);
        addVertex(bufferbuilder, mat, backLeftTop, color, alpha, rightNormal);

        addVertex(bufferbuilder, mat, backLeftTop, color, alpha, upNormal);
        addVertex(bufferbuilder, mat, frontLeftTop, color, alpha, upNormal);
        addVertex(bufferbuilder, mat, frontRightTop, color, alpha, upNormal);
        addVertex(bufferbuilder, mat, backRightTop, color, alpha, upNormal);

        upNormal.reverse();
        addVertex(bufferbuilder, mat, frontLeftBottom, color, alpha, upNormal);
        addVertex(bufferbuilder, mat, backLeftBottom, color, alpha, upNormal);
        addVertex(bufferbuilder, mat, backRightBottom, color, alpha, upNormal);
        addVertex(bufferbuilder, mat, frontRightBottom, color, alpha, upNormal);
    }

    private static void addVertex(BufferBuilder buff, Matrix4f mat, Vec3 pos, Vec3i color, int alpha, Vec3 normal) {
        buff.vertex(mat, (float) pos.x, (float) pos.y, (float) pos.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal((float) normal.x, (float) normal.y, (float) normal.z)
            .endVertex();
    }
}
