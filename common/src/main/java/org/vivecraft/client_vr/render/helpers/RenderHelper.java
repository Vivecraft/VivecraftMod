package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
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
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43C;
import org.vivecraft.client.extensions.RenderTargetExtension;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.mixin.client.blaze3d.RenderSystemAccessor;
import org.vivecraft.mod_compat_vr.ShadersHelper;

import java.util.function.Supplier;

public class RenderHelper {

    private static int polyBlendSrcA;
    private static int polyBlendDstA;
    private static int polyBlendSrcRGB;
    private static int polyBlendDstRGB;
    private static boolean polyBlend;
    private static boolean polyTex;
    private static boolean polyLight;
    private static boolean polyCull;

    private static final ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
    private static final Minecraft mc = Minecraft.getInstance();

    public static void applyVRModelView(RenderPass currentPass, PoseStack poseStack) {
        Matrix4f modelView;
        if (currentPass == RenderPass.CENTER && dataHolder.vrSettings.displayMirrorCenterSmooth > 0.0F) {
            modelView = new Matrix4f().rotation(MCVR.get().hmdRotHistory
                .averageRotation(dataHolder.vrSettings.displayMirrorCenterSmooth));
        } else {
            modelView = dataHolder.vrPlayer.vrdata_world_render.getEye(currentPass)
                .getMatrix().transposed().toMCMatrix();
        }
        poseStack.last().pose().mul(modelView);
        poseStack.last().normal().mul(new Matrix3f(modelView));
    }

    public static void applyVRModelView(RenderPass currentPass, Matrix4f matrix) {
        Matrix4f modelView;
        if (currentPass == RenderPass.CENTER && dataHolder.vrSettings.displayMirrorCenterSmooth > 0.0F) {
            modelView = new Matrix4f().rotation(MCVR.get().hmdRotHistory
                .averageRotation(dataHolder.vrSettings.displayMirrorCenterSmooth));
        } else {
            modelView = dataHolder.vrPlayer.vrdata_world_render.getEye(currentPass)
                .getMatrix().transposed().toMCMatrix();
        }
        matrix.mul(modelView);
    }

    public static Vec3 getSmoothCameraPosition(RenderPass renderpass, VRData vrData) {
        if (dataHolder.currentPass == RenderPass.CENTER && dataHolder.vrSettings.displayMirrorCenterSmooth > 0.0F) {
            return MCVR.get().hmdHistory.averagePosition(dataHolder.vrSettings.displayMirrorCenterSmooth)
                .scale(vrData.worldScale)
                .yRot(vrData.rotation_radians)
                .add(vrData.origin);
        } else {
            return vrData.getEye(renderpass).getPosition();
        }
    }

    public static void applyStereo(RenderPass currentPass, PoseStack matrix) {
        if (currentPass == RenderPass.LEFT || currentPass == RenderPass.RIGHT) {
            Vec3 eye = dataHolder.vrPlayer.vrdata_world_render.getEye(currentPass).getPosition()
                .subtract(dataHolder.vrPlayer.vrdata_world_render.getEye(RenderPass.CENTER)
                    .getPosition());
            matrix.translate(-eye.x, -eye.y, -eye.z);
        }
    }

    public static Vec3 getControllerRenderPos(int c) {
        if (!dataHolder.vrSettings.seated) {
            return dataHolder.vrPlayer.vrdata_world_render.getController(c).getPosition();
        } else {
            Vec3 out = null;
            int mainHand = InteractionHand.MAIN_HAND.ordinal();
            if (dataHolder.vrSettings.reverseHands) {
                c = 1 - c;
                mainHand = InteractionHand.OFF_HAND.ordinal();
            }

            if (mc.getCameraEntity() != null && mc.level != null) {
                Vec3 dir = dataHolder.vrPlayer.vrdata_world_render.hmd.getDirection();
                dir = dir.yRot((float) Math.toRadians(c == 0 ? -35.0D : 35.0D));
                dir = new Vec3(dir.x, 0.0D, dir.z);
                dir = dir.normalize();
                if (TelescopeTracker.isTelescope(mc.player.getUseItem()) && TelescopeTracker.isTelescope(c == mainHand ? mc.player.getMainHandItem() : mc.player.getOffhandItem())) {
                    // move the controller in front of the eye when using the spyglass
                    VRData.VRDevicePose eye = c == 0 ? dataHolder.vrPlayer.vrdata_world_render.eye0 :
                                              dataHolder.vrPlayer.vrdata_world_render.eye1;

                    out = eye.getPosition()
                        .add(dataHolder.vrPlayer.vrdata_world_render.hmd.getDirection()
                            .scale(0.2 * dataHolder.vrPlayer.vrdata_world_render.worldScale));
                }
                if (out == null) {
                    out = dataHolder.vrPlayer.vrdata_world_render.getEye(RenderPass.CENTER).getPosition().add(
                        dir.x * 0.3D * dataHolder.vrPlayer.vrdata_world_render.worldScale,
                        -0.4D * dataHolder.vrPlayer.vrdata_world_render.worldScale,
                        dir.z * 0.3D * dataHolder.vrPlayer.vrdata_world_render.worldScale);
                }
            } else { //main menu
                Vec3 dir = dataHolder.vrPlayer.vrdata_world_render.hmd.getDirection();
                dir = dir.yRot((float) Math.toRadians(c == 0 ? -35.0D : 35.0D));
                dir = new Vec3(dir.x, 0.0D, dir.z);
                dir = dir.normalize();
                out = dataHolder.vrPlayer.vrdata_world_render.hmd.getPosition().add(dir.x * 0.3D, -0.4D,
                    dir.z * 0.3D);
            }
            return out;
        }
    }

    public static void setupRenderingAtController(int controller, Matrix4f matrix) {
        Vec3 aimSource = getControllerRenderPos(controller);
        aimSource = aimSource.subtract(
            getSmoothCameraPosition(dataHolder.currentPass, dataHolder.vrPlayer.getVRDataWorld()));
        matrix.translate((float) aimSource.x, (float) aimSource.y, (float) aimSource.z);
        float sc = dataHolder.vrPlayer.vrdata_world_render.worldScale;
        if (dataHolder.vrSettings.seated && mc.level != null && TelescopeTracker.isTelescope(mc.player.getUseItem()) && TelescopeTracker.isTelescope(controller == 0 ? mc.player.getMainHandItem() : mc.player.getOffhandItem())) {
            matrix.mul(dataHolder.vrPlayer.vrdata_world_render.hmd.getMatrix().inverted()
                .transposed().toMCMatrix());
            matrix.rotate(Axis.XP.rotationDegrees(90));
            matrix.translate(controller == (dataHolder.vrSettings.reverseHands ? 1 : 0) ? 0.075F * sc : -0.075F * sc, -0.025F * sc, 0.0325F * sc);
        } else {
            matrix.mul(dataHolder.vrPlayer.vrdata_world_render.getController(controller)
                .getMatrix().inverted().transposed().toMCMatrix());
        }

        matrix.scale(sc, sc, sc);
    }

    public static void renderDebugAxes(int r, int g, int b, float radius) {
        setupPolyRendering(true);
        RenderSystem.setShaderTexture(0, ResourceLocation.parse("vivecraft:textures/white.png"));
        renderCircle(new Vec3(0.0D, 0.0D, 0.0D), radius, 32, r, g, b, 255, 0);
        renderCircle(new Vec3(0.0D, 0.01D, 0.0D), radius * 0.75F, 32, r, g, b, 255, 0);
        renderCircle(new Vec3(0.0D, 0.02D, 0.0D), radius * 0.25F, 32, r, g, b, 255, 0);
        renderCircle(new Vec3(0.0D, 0.0D, 0.15D), radius * 0.5F, 32, r, g, b, 255, 2);
        setupPolyRendering(false);
    }

    public static void renderCircle(Vec3 pos, float radius, int edges, int r, int g, int b, int a, int side) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.addVertex((float) pos.x, (float) pos.y, (float) pos.z).setColor(r, g, b, a);

        for (int i = 0; i < edges + 1; i++) {
            float startAngle = (float) i / (float) edges * (float) Math.PI * 2.0F;
            if (side == 0 || side == 1) { //y
                float x = (float) pos.x + (float) Math.cos(startAngle) * radius;
                float y = (float) pos.y;
                float z = (float) pos.z + (float) Math.sin(startAngle) * radius;
                bufferBuilder.addVertex(x, y, z).setColor(r, g, b, a);
            } else if (side == 2 || side == 3) { //z
                float x = (float) pos.x + (float) Math.cos(startAngle) * radius;
                float y = (float) pos.y + (float) Math.sin(startAngle) * radius;
                float z = (float) pos.z;
                bufferBuilder.addVertex(x, y, z).setColor(r, g, b, a);
            } else if (side == 4 || side == 5) { //x
                float x = (float) pos.x;
                float y = (float) pos.y + (float) Math.cos(startAngle) * radius;
                float z = (float) pos.z + (float) Math.sin(startAngle) * radius;
                bufferBuilder.addVertex(x, y, z).setColor(r, g, b, a);
            }
        }
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }

    public static void setupPolyRendering(boolean enable) {
//		boolean flag = Config.isShaders(); TODO
        boolean flag = false;

        if (enable) {
            polyBlendSrcA = GlStateManager.BLEND.srcAlpha;
            polyBlendDstA = GlStateManager.BLEND.dstAlpha;
            polyBlendSrcRGB = GlStateManager.BLEND.srcRgb;
            polyBlendDstRGB = GlStateManager.BLEND.dstRgb;
            polyBlend = GL43C.glIsEnabled(GL11.GL_BLEND);
            polyTex = true;
            polyLight = false;
            polyCull = true;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            // GlStateManager._disableLighting();
            RenderSystem.disableCull();

            if (flag) {
//				this.prog = Shaders.activeProgram; TODO
//				Shaders.useProgram(Shaders.ProgramTexturedLit);
            }
        } else {
            RenderSystem.blendFuncSeparate(polyBlendSrcRGB, polyBlendDstRGB, polyBlendSrcA,
                polyBlendDstA);

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

//			if (flag && this.polytex) {
//				Shaders.useProgram(this.prog); TODO
//			}
        }
    }

    public static void drawScreen(DeltaTracker.Timer timer, Screen screen, GuiGraphics guiGraphics) {
        Matrix4fStack posestack = RenderSystem.getModelViewStack();
        posestack.pushMatrix();
        posestack.identity();
        posestack.translate(0.0F, 0.0F, -11000.0F);
        RenderSystem.applyModelViewMatrix();

        Matrix4f guiProjection = (new Matrix4f()).setOrtho(
            0.0F, (float) (mc.getWindow().getWidth() / mc.getWindow().getGuiScale()),
                (float) (mc.getWindow().getHeight() / mc.getWindow().getGuiScale()), 0.0F,
                1000.0F, 21000.0F);
        RenderSystem.setProjectionMatrix(guiProjection, VertexSorting.ORTHOGRAPHIC_Z);

        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ONE);

        screen.render(guiGraphics, 0, 0, timer.getRealtimeDeltaTicks());

        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ONE);

        posestack.popMatrix();
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

        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        bufferbuilder.addVertex((-(size / 2.0F)), (-(size * aspect) / 2.0F), 0.0F)
            .setUv(0.0F, 0.0F)
            .setColor(color[0], color[1], color[2], color[3])
            .setNormal(0.0F, 0.0F, 1.0F);
        bufferbuilder.addVertex((size / 2.0F), (-(size * aspect) / 2.0F), 0.0F)
            .setUv(1.0F, 0.0F)
            .setColor(color[0], color[1], color[2], color[3])
            .setNormal(0.0F, 0.0F, 1.0F);
        bufferbuilder.addVertex((size / 2.0F), (size * aspect / 2.0F), 0.0F)
            .setUv(1.0F, 1.0F)
            .setColor(color[0], color[1], color[2], color[3])
            .setNormal(0.0F, 0.0F, 1.0F);
        bufferbuilder.addVertex((-(size / 2.0F)), (size * aspect / 2.0F), 0.0F)
            .setUv(0.0F, 1.0F)
            .setColor(color[0], color[1], color[2], color[3])
            .setNormal(0.0F, 0.0F, 1.0F);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }

    public static void drawSizedQuad(float displayWidth, float displayHeight, float size, float[] color, Matrix4f pMatrix) {
        float aspect = displayHeight / displayWidth;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder
            .addVertex(pMatrix, (-(size / 2.0F)), (-(size * aspect) / 2.0F), 0)
            .setUv(0.0F, 0.0F);
        bufferbuilder
            .addVertex(pMatrix, (size / 2.0F), (-(size * aspect) / 2.0F), 0)
            .setUv(1.0F, 0.0F);
        bufferbuilder
            .addVertex(pMatrix, (size / 2.0F), (size * aspect / 2.0F), 0)
            .setUv(1.0F, 1.0F);
        bufferbuilder
            .addVertex(pMatrix, (-(size / 2.0F)), (size * aspect / 2.0F), 0)
            .setUv(0.0F, 1.0F);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void drawSizedQuadWithLightmapCutout(float displayWidth, float displayHeight, float size, int lighti, Matrix4f pMatrix, boolean flipY) {
        drawSizedQuadWithLightmapCutout(displayWidth, displayHeight, size, lighti, new float[]{1, 1, 1, 1}, pMatrix, flipY);
    }

    public static void drawSizedQuadWithLightmapCutout(float displayWidth, float displayHeight, float size, int lighti,
        float[] color, Matrix4f pMatrix, boolean flipY) {
        drawSizedQuadWithLightmap(displayWidth, displayHeight, size, lighti, color, pMatrix, GameRenderer::getRendertypeEntityCutoutNoCullShader, flipY);
    }

    public static void drawSizedQuadSolid(float displayWidth, float displayHeight, float size,
        float[] color, Matrix4f pMatrix) {
        drawSizedQuadWithLightmap(displayWidth, displayHeight, size, LightTexture.pack(15, 15), color, pMatrix, GameRenderer::getRendertypeEntitySolidShader, false);
    }

    public static void drawSizedQuadWithLightmap(float displayWidth, float displayHeight, float size, int lighti,
        float[] color, Matrix4f pMatrix, Supplier<ShaderInstance> shader, boolean flipY) {
        float aspect = displayHeight / displayWidth;
        RenderSystem.setShader(shader);
        mc.gameRenderer.lightTexture().turnOnLightLayer();
        mc.gameRenderer.overlayTexture().setupOverlayColor();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);

        // store old lights
        Vector3f light0Old = RenderSystemAccessor.getShaderLightDirections()[0];
        Vector3f light1Old = RenderSystemAccessor.getShaderLightDirections()[1];

        Vector3f normal = new Vector3f(0, 0, 1);

        // weird iris behaviour
        if (ShadersHelper.isShaderActive()) {
            normal = new Matrix3f(pMatrix).transform(normal);
        }

        // set lights to front
        RenderSystem.setShaderLights(normal, normal);
        RenderSystem.setupShaderLights(RenderSystem.getShader());

        bufferbuilder.addVertex(pMatrix, (-(size / 2.0F)), (-(size * aspect) / 2.0F), 0)
            .setColor(color[0], color[1], color[2], color[3])
            .setUv(0.0F, flipY ? 1.0F : 0.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(lighti)
            .setNormal(0, 0, 1);
        bufferbuilder.addVertex(pMatrix, (size / 2.0F), (-(size * aspect) / 2.0F), 0)
            .setColor(color[0], color[1], color[2], color[3])
            .setUv(1.0F, flipY ? 1.0F : 0.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(lighti)
            .setNormal(0, 0, 1);
        bufferbuilder.addVertex(pMatrix, (size / 2.0F), (size * aspect / 2.0F), 0)
            .setColor(color[0], color[1], color[2], color[3])
            .setUv(1.0F, flipY ? 0.0F : 1.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(lighti)
            .setNormal(0, 0, 1);
        bufferbuilder.addVertex(pMatrix, (-(size / 2.0F)), (size * aspect / 2.0F), 0)
            .setColor(color[0], color[1], color[2], color[3])
            .setUv(0.0F, flipY ? 0.0F : 1.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(lighti)
            .setNormal(0, 0, 1);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());

        mc.gameRenderer.lightTexture().turnOffLightLayer();

        // reset lights
        if (light0Old != null && light1Old != null) {
            RenderSystem.setShaderLights(light0Old, light1Old);
            RenderSystem.setupShaderLights(RenderSystem.getShader());
        }
    }

    public static void renderFlatQuad(Vec3 pos, float width, float height, float yaw, int r, int g, int b, int a, Matrix4f mat) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);

        Vec3 offset = (new Vec3((width / 2.0F), 0.0, height / 2.0F))
            .yRot((float) Math.toRadians(-yaw));

        bufferbuilder.addVertex(mat, (float) (pos.x + offset.x), (float) pos.y, (float) (pos.z + offset.z))
            .setColor(r, g, b, a).setNormal(0.0F, 1.0F, 0.0F);
        bufferbuilder.addVertex(mat, (float) (pos.x + offset.x), (float) pos.y, (float) (pos.z - offset.z))
            .setColor(r, g, b, a).setNormal(0.0F, 1.0F, 0.0F);
        bufferbuilder.addVertex(mat, (float) (pos.x - offset.x), (float) pos.y, (float) (pos.z - offset.z))
            .setColor(r, g, b, a).setNormal(0.0F, 1.0F, 0.0F);
        bufferbuilder.addVertex(mat, (float) (pos.x - offset.x), (float) pos.y, (float) (pos.z + offset.z))
            .setColor(r, g, b, a).setNormal(0.0F, 1.0F, 0.0F);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }

    public static void renderBox(BufferBuilder bufferbuilder, Vec3 start, Vec3 end, float minX, float maxX, float minY, float maxY, Vec3i color, byte alpha, Matrix4f mat) {
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
        buff.addVertex(mat, (float) pos.x, (float) pos.y, (float) pos.z)
            .setColor(color.getX(), color.getY(), color.getZ(), alpha)
            .setNormal((float) normal.x, (float) normal.y, (float) normal.z);
    }
}
