package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client.extensions.SubmitNodeCollectionExtension;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_vr.render.helpers.opengl.OpenGLHelper;
import org.vivecraft.client_vr.render.rendertypes.VRRenderTypes;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.MathUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenderHelper {

    private static final ClientDataHolderVR DATA_HOLDER = ClientDataHolderVR.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();

    public static final Identifier DEBUG_CAPE = Identifier.parse("vivecraft:textures/cape.png");
    public static final Identifier WHITE_TEXTURE = Identifier.parse("vivecraft:textures/white.png");
    public static final Identifier BLACK_TEXTURE = Identifier.parse("vivecraft:textures/black.png");

    public static GpuTextureView getGpuTexture(Identifier identifier) {
        return MC.getTextureManager().getTexture(identifier).getTextureView();
    }

    /**
     * gets the rotation matrix for the given RenderPass
     *
     * @param renderPass RenderPass to get the rotation matrix for
     */
    public static Matrix4f getVRModelView(RenderPass renderPass) {
        return DATA_HOLDER.vrPlayer.getVRDataWorld().getEye(renderPass).getMatrix().transpose();
    }

    /**
     * Applies the rotation from the given RenderPass to the given PoseStack
     *
     * @param renderPass RenderPass rotation to use
     * @param poseStack  PoseStack to apply the rotation to
     */
    public static void applyVRModelView(RenderPass renderPass, PoseStack poseStack) {
        Matrix4f modelView = getVRModelView(renderPass);
        poseStack.mulPose(modelView);
    }

    /**
     * Applies the rotation from the given RenderPass to the given matrix
     *
     * @param renderPass RenderPass rotation to use
     * @param matrix     Matrix4f to apply the rotation to
     */
    public static void applyVRModelView(RenderPass renderPass, Matrix4f matrix) {
        Matrix4f modelView = getVRModelView(renderPass);
        matrix.mul(modelView);
    }

    /**
     * Applies the offset for the LEFT and RIGHT RenderPass from the headset position
     * Other RenderPasses do nothing
     *
     * @param renderPass RenderPass to apply the offset for
     * @param poseStack  PoseStack to apply the offset to
     */
    public static void applyStereo(RenderPass renderPass, PoseStack poseStack) {
        if (renderPass == RenderPass.LEFT || renderPass == RenderPass.RIGHT) {
            Vec3 eye = DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(renderPass).getPosition()
                .subtract(DATA_HOLDER.vrPlayer.vrdata_world_render.hmd.getPosition());
            poseStack.translate(-eye.x, -eye.y, -eye.z);
        }
    }

    /**
     * Gets the position of the given controller/tracker in world space.
     * For controllers (0, 1), this positions the seated controllers.
     * Other stuff is just forwarded to the world_render vrData
     *
     * @param c controller/tracker to get the position for
     * @return position of the given controller
     */
    public static Vec3 getControllerRenderPos(int c) {
        if (DATA_HOLDER.vrSettings.seated && c < 2) {
            // only do the seated override for the controllers, not trackers

            int mainHand = InteractionHand.MAIN_HAND.ordinal();
            if (DATA_HOLDER.vrSettings.reverseHands) {
                c = 1 - c;
                mainHand = InteractionHand.OFF_HAND.ordinal();
            }

            // handle telescopes, allow for double scoping
            if (MC.player != null && MC.level != null &&
                TelescopeTracker.isTelescope(MC.player.getUseItem()) &&
                TelescopeTracker.isTelescope(c == mainHand ? MC.player.getMainHandItem() : MC.player.getOffhandItem()))
            {
                // move the controller in front of the eye when using the spyglass
                VRData.VRDevicePose eye = c == 0 ? DATA_HOLDER.vrPlayer.vrdata_world_render.eye0 :
                    DATA_HOLDER.vrPlayer.vrdata_world_render.eye1;

                Vector3f dir = DATA_HOLDER.vrPlayer.vrdata_world_render.hmd.getDirection()
                    .mul(0.2F * DATA_HOLDER.vrPlayer.vrdata_world_render.worldScale);

                return eye.getPosition().add(dir.x, dir.y, dir.z);
            } else {
                // general case
                // no worldScale in the main menu
                float worldScale = MC.player != null && MC.level != null ?
                    DATA_HOLDER.vrPlayer.vrdata_world_render.worldScale : 1.0F;

                Vector3f dir = DATA_HOLDER.vrPlayer.vrdata_world_render.hmd.getDirection();
                dir.rotateY(Mth.DEG_TO_RAD * (c == 0 ? -35.0F : 35.0F));
                dir.y = 0F;
                dir.normalize();
                return DATA_HOLDER.vrPlayer.vrdata_world_render.hmd.getPosition().add(
                    dir.x * 0.3D * worldScale,
                    -0.4D * worldScale,
                    dir.z * 0.3D * worldScale);
            }
        } else {
            return DATA_HOLDER.vrPlayer.vrdata_world_render.getController(c).getPosition();
        }
    }

    /**
     * sets up the matrix to render at the given controller/tracker
     *
     * @param c      controller/tracker to render at
     * @param matrix Matrix4f to apply the position to
     */
    public static void setupRenderingAtController(int c, Matrix4f matrix) {
        setupRenderingAtController(c, matrix, true);
    }

    /**
     * sets up the matrix to render at the given controller/tracker
     *
     * @param c      controller/tracker to render at
     * @param matrix Matrix4f to apply the position to
     */
    public static Vec3 setupRenderingAtController(int c, Matrix4f matrix, boolean combine) {
        // TODO separate position from rotation
        Vec3 aimSource = getControllerRenderPos(c);
        if (combine) {
            aimSource = aimSource.subtract(
                DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(DATA_HOLDER.currentPass).getPosition());
            // move from head to hand origin.
            matrix.translate((float) aimSource.x, (float) aimSource.y, (float) aimSource.z);
        }

        float sc = DATA_HOLDER.vrPlayer.vrdata_world_render.worldScale;

        // handle telescopes in seated, allow for double scoping
        if (DATA_HOLDER.vrSettings.seated && MC.player != null && MC.level != null &&
            TelescopeTracker.isTelescope(MC.player.getUseItem()) &&
            TelescopeTracker.isTelescope(c == 0 ? MC.player.getMainHandItem() : MC.player.getOffhandItem()))
        {
            matrix.mul(DATA_HOLDER.vrPlayer.vrdata_world_render.hmd.getMatrix().invert().transpose());
            matrix.rotate(Axis.XP.rotationDegrees(90F));
            // move to the eye center, seems to be magic numbers that work for the vive at least
            matrix.translate((c == (DATA_HOLDER.vrSettings.reverseHands ? 1 : 0) ? 0.075F : -0.075F) * sc,
                -0.025F * sc,
                0.0325F * sc);
        } else {
            matrix.mul(DATA_HOLDER.vrPlayer.vrdata_world_render.getController(c).getMatrix().invert().transpose());
        }

        matrix.scale(sc, sc, sc);
        return aimSource;
    }

    /**
     * renders the given screen to the current main target and generates mipmaps for it
     *
     * @param screen      the Screen to render
     * @param maxGuiScale if set, renders the screen at max gui scale
     */
    public static void drawScreen(Screen screen, boolean maxGuiScale) {
        double guiScale = maxGuiScale ? GuiHandler.GUI_SCALE_FACTOR_MAX : MC.getWindow().getGuiScale();

        // set gui scale to make the scissor work, that checks the window gui scale
        int backupGuiScale = GuiHandler.GUI_SCALE_FACTOR;
        GuiHandler.GUI_SCALE_FACTOR = (int) guiScale;

        GuiRenderHelper.renderScreen(screen);

        // reset gui scale
        GuiHandler.GUI_SCALE_FACTOR = backupGuiScale;

        if (DATA_HOLDER.vrSettings.guiMipmaps) {
            // update mipmaps for Gui layer
            OpenGLHelper.genMipmaps(MC.mainRenderTarget.getColorTexture());
        }
    }

    /**
     * draws the crosshair at the specified location on the screen
     *
     * @param graphics GuiGraphicsExtractor to render with, is not flushed after rendering
     * @param mouseX   x coordinate in screen pixel coordinates
     * @param mouseY   y coordinate in screen pixel coordinates
     */
    public static void drawMouseMenuQuad(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        float size = 15.0F * Math.max(ClientDataHolderVR.getInstance().vrSettings.menuCrosshairScale,
            1.0F / (float) MC.getWindow().getGuiScale());

        graphics.blitSprite(VRShaders.CROSSHAIR_MENU, Gui.CROSSHAIR_SPRITE, (int) (mouseX - size * 0.5F + 1),
            (int) (mouseY - size * 0.5F + 1), (int) size, (int) size);
    }

    /**
     * draws the "connecting to vr runtime" message to the main rendertarget screen
     */
    public static void drawVRConnectingMessage() {
        // clear depth, because text that was already there would be over ours
        RenderSystem.getDevice().createCommandEncoder()
            .clearDepthTexture(MC.getMainRenderTarget().getDepthTexture(), 1.0);

        GuiGraphicsExtractor graphics = GuiRenderHelper.getGuiGraphics();

        int width = 200;
        List<FormattedCharSequence> formattedChars = MC.font.split(
            Component.translatable("vivecraft.messages.connectingtoruntime"), width - 10);
        int height = formattedChars.size() * 8 + Math.max(formattedChars.size() - 1, 0) * 4 + 10;

        int x = graphics.guiWidth() / 2 - width / 2;
        int y = graphics.guiHeight() / 2 - height / 2;

        // transparent background to dim the game
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), 0x40000000);

        // black background with border
        graphics.fill(x, y, x + width, y + height, 0xFF000000);
        graphics.outline(x, y, width, height, 0xFFFFFFFF);

        for (int line = 0; line < formattedChars.size(); line++) {
            graphics.centeredText(MC.font, formattedChars.get(line), graphics.guiWidth() / 2,
                y + 5 + line * 12, 0xFFFFFFFF);
        }

        GuiRenderHelper.finish();
    }

    /**
     * draws a quad with the PositionTex shader, ignoring depth, to be used when <b>not</b> in a world
     *
     * @param displayWidth  texture width
     * @param displayHeight texture height
     * @param size          size of the quad
     * @param color         color of the quad, expects an array of length 4 for: r, g, b, a
     * @param poseStack     PoseStack to position the screen with
     * @param source        TenderTarget to render
     * @param depthAlways   if the quad should use depth testing or not
     * @param output        SubmitNodeCollector to output to
     * @param order         order to render at
     * @return order to render the next thing at
     */
    public static int drawSizedQuad(
        float displayWidth, float displayHeight, float size, float[] color, PoseStack poseStack, RenderTarget source,
        boolean depthAlways, SubmitNodeCollector output, int order)
    {
        float sizeX = size * 0.5F;
        float sizeY = sizeX * displayHeight / displayWidth;

        RenderType renderType = VRRenderTypes.guiTextured(source.getColorTextureView(), depthAlways);

        RenderHelper.submitLateCustomGeometry(output.order(order++), poseStack, renderType,
            (pose, consumer) -> {
                consumer
                    .addVertex(pose, -sizeX, -sizeY, 0)
                    .setUv(0.0F, 0.0F)
                    .setColor(color[0], color[1], color[2], color[3]);
                consumer
                    .addVertex(pose, sizeX, -sizeY, 0)
                    .setUv(1.0F, 0.0F)
                    .setColor(color[0], color[1], color[2], color[3]);
                consumer
                    .addVertex(pose, sizeX, sizeY, 0)
                    .setUv(1.0F, 1.0F)
                    .setColor(color[0], color[1], color[2], color[3]);
                consumer
                    .addVertex(pose, -sizeX, sizeY, 0)
                    .setUv(0.0F, 1.0F)
                    .setColor(color[0], color[1], color[2], color[3]);
            });
        return order;
    }

    /**
     * draws a quad with the given entity RenderType and no color modifier, to be used when <b>in</b> a world
     *
     * @param displayWidth  texture width
     * @param displayHeight texture height
     * @param size          size of the quad
     * @param packedLight   block and sky light packed into an int
     * @param poseStack     PoseStack to use to
     * @param renderType    entity RenderType to use
     * @param flipY         if the texture should be flipped vertically
     * @param output        SubmitNodeCollector to output to
     * @param order         order to render at
     * @return order to render the next thing at
     */
    public static int submitSizedQuadWithLightmap(
        float displayWidth, float displayHeight, float size, int packedLight, PoseStack poseStack,
        RenderType renderType, boolean flipY, SubmitNodeCollector output, int order)
    {
        return submitSizedQuadWithLightmap(displayWidth, displayHeight, size, packedLight, new float[]{1, 1, 1, 1},
            poseStack, renderType, flipY, output, order);
    }

    /**
     * draws a quad with the given entity RenderType at full brightness, to be used when <b>in</b> a world
     *
     * @param displayWidth  texture width
     * @param displayHeight texture height
     * @param size          size of the quad
     * @param color         color of the quad, expects an array of length 4 for: r, g, b, a
     * @param poseStack     PoseStack to use to
     * @param renderType    entity RenderType to use
     * @param output        SubmitNodeCollector to output to
     * @param order         order to render at
     * @return order to render the next thing at
     */
    public static int submitSizedQuadFullbright(
        float displayWidth, float displayHeight, float size, float[] color, PoseStack poseStack, RenderType renderType,
        SubmitNodeCollector output, int order)
    {
        return submitSizedQuadWithLightmap(displayWidth, displayHeight, size, LightCoordsUtil.FULL_BRIGHT, color,
            poseStack, renderType, false, output, order);
    }

    /**
     * draws a quad with the given entity RenderType, to be used when <b>in</b> a world
     *
     * @param displayWidth  texture width
     * @param displayHeight texture height
     * @param size          size of the quad
     * @param packedLight   block and sky light packed into an int
     * @param color         color of the quad, expects an array of length 4 for: r, g, b, a
     * @param poseStack     PoseStack to use to for positioning
     * @param renderType    RenderType to render as, needs to be one of the entity types
     * @param flipY         if the texture should be flipped vertically
     * @param output        SubmitNodeCollector to output to
     * @param order         order to render at
     * @return order to render the next thing at
     */
    public static int submitSizedQuadWithLightmap(
        float displayWidth, float displayHeight, float size, int packedLight, float[] color, PoseStack poseStack,
        RenderType renderType, boolean flipY, SubmitNodeCollector output, int order)
    {
        float sizeX = size * 0.5F;
        float sizeY = sizeX * displayHeight / displayWidth;

        Vector3f normal = poseStack.last().transformNormal(0, 0, 1, new Vector3f());

        RenderHelper.submitLateCustomGeometry(output.order(order++), poseStack, renderType,
            (pose, consumer) -> {
                consumer.addVertex(pose, -sizeX, -sizeY, 0)
                    .setColor(color[0], color[1], color[2], color[3])
                    .setUv(0.0F, flipY ? 1.0F : 0.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                    .setNormal(normal.x, normal.y, normal.z);
                consumer.addVertex(pose, sizeX, -sizeY, 0)
                    .setColor(color[0], color[1], color[2], color[3])
                    .setUv(1.0F, flipY ? 1.0F : 0.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                    .setNormal(normal.x, normal.y, normal.z);
                consumer.addVertex(pose, sizeX, sizeY, 0)
                    .setColor(color[0], color[1], color[2], color[3])
                    .setUv(1.0F, flipY ? 0.0F : 1.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                    .setNormal(normal.x, normal.y, normal.z);
                consumer.addVertex(pose, -sizeX, sizeY, 0)
                    .setColor(color[0], color[1], color[2], color[3])
                    .setUv(0.0F, flipY ? 0.0F : 1.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                    .setNormal(normal.x, normal.y, normal.z);
            });
        return order;
    }

    /**
     * draws a colored quad
     *
     * @param pos         center position of the quad
     * @param width       width of the quad
     * @param height      height of the quad
     * @param yaw         y rotation of the quad
     * @param r           red 0-255
     * @param g           green 0-255
     * @param b           blue 0-255
     * @param a           alpha 0-255
     * @param poseStack   PoseStack to use for positioning
     * @param depthAlways ignores depth and always draws
     * @param output      SubmitNodeCollector to output to
     * @param order       order to render at
     * @return order to render the next thing at
     */
    public static int renderFlatQuad(
        Vec3 pos, float width, float height, float yaw, int r, int g, int b, int a, PoseStack poseStack,
        boolean depthAlways, SubmitNodeCollector output, int order)
    {
        Vec3 offset = (new Vec3(width * 0.5F, 0.0, height * 0.5F))
            .yRot(Mth.DEG_TO_RAD * -yaw);

        RenderHelper.submitLateCustomGeometry(output.order(order++), poseStack, VRRenderTypes.quads(depthAlways),
            (pose, consumer) -> {
                consumer.addVertex(pose, (float) (pos.x + offset.x), (float) pos.y, (float) (pos.z + offset.z))
                    .setColor(r, g, b, a);
                consumer.addVertex(pose, (float) (pos.x + offset.x), (float) pos.y, (float) (pos.z - offset.z))
                    .setColor(r, g, b, a);
                consumer.addVertex(pose, (float) (pos.x - offset.x), (float) pos.y, (float) (pos.z - offset.z))
                    .setColor(r, g, b, a);
                consumer.addVertex(pose, (float) (pos.x - offset.x), (float) pos.y, (float) (pos.z + offset.z))
                    .setColor(r, g, b, a);
            });
        return order;
    }

    /**
     * adds a box to the given Tesselator
     *
     * @param consumer VertexConsumer to use
     * @param start    start of the box, combined with end gives the axis the box is on
     * @param end      end of the box, combined with start gives the axis the box is on
     * @param xSize    X size of the box
     * @param ySize    Y size of the box
     * @param color    color of the box 0-255 per component
     * @param alpha    transparency of the box 0-255
     * @param pose     Pose to use for positioning
     */
    public static void renderBox(
        VertexConsumer consumer, Vec3 start, Vec3 end, float xSize, float ySize, Vec3i color, byte alpha,
        PoseStack.Pose pose)
    {
        renderBox(consumer, start, end, -xSize * 0.5F, xSize * 0.5F, -ySize * 0.5F, ySize * 0.5F, color, alpha, pose);
    }

    /**
     * adds a box to the given Tesselator
     *
     * @param consumer VertexConsumer to use
     * @param start    start of the box, combined with end gives the axis the box is on
     * @param end      end of the box, combined with start gives the axis the box is on
     * @param minX     X- size of the box
     * @param maxX     X+ size of the box
     * @param minY     Y- size of the box
     * @param maxY     Y+ size of the box
     * @param color    color of the box 0-255 per component
     * @param alpha    transparency of the box 0-255
     * @param pose     Pose to use for positioning
     */
    public static void renderBox(
        VertexConsumer consumer, Vec3 start, Vec3 end, float minX, float maxX, float minY, float maxY, Vec3i color,
        byte alpha, PoseStack.Pose pose)
    {
        Vec3 forward = start.subtract(end).normalize();
        Vec3 right = forward.cross(MathUtils.UP_D);
        if (right.lengthSqr() == 0) {
            right = MathUtils.LEFT_D;
        } else {
            right = right.normalize();
        }
        Vec3 up = right.cross(forward);

        Vec3 left = right.scale(minX);
        right = right.scale(maxX);

        Vec3 down = up.scale(minY);
        up = up.scale(maxY);

        Vec3 backRightBottom = start.add(right.x + down.x, right.y + down.y, right.z + down.z);
        Vec3 backRightTop = start.add(right.x + up.x, right.y + up.y, right.z + up.z);
        Vec3 backLeftBottom = start.add(left.x + down.x, left.y + down.y, left.z + down.z);
        Vec3 backLeftTop = start.add(left.x + up.x, left.y + up.y, left.z + up.z);

        Vec3 frontRightBottom = end.add(right.x + down.x, right.y + down.y, right.z + down.z);
        Vec3 frontRightTop = end.add(right.x + up.x, right.y + up.y, right.z + up.z);
        Vec3 frontLeftBottom = end.add(left.x + down.x, left.y + down.y, left.z + down.z);
        Vec3 frontLeftTop = end.add(left.x + up.x, left.y + up.y, left.z + up.z);

        addVertex(consumer, pose, backRightBottom, color, alpha);
        addVertex(consumer, pose, backLeftBottom, color, alpha);
        addVertex(consumer, pose, backLeftTop, color, alpha);
        addVertex(consumer, pose, backRightTop, color, alpha);

        addVertex(consumer, pose, frontLeftBottom, color, alpha);
        addVertex(consumer, pose, frontRightBottom, color, alpha);
        addVertex(consumer, pose, frontRightTop, color, alpha);
        addVertex(consumer, pose, frontLeftTop, color, alpha);

        addVertex(consumer, pose, frontRightBottom, color, alpha);
        addVertex(consumer, pose, backRightBottom, color, alpha);
        addVertex(consumer, pose, backRightTop, color, alpha);
        addVertex(consumer, pose, frontRightTop, color, alpha);

        addVertex(consumer, pose, backLeftBottom, color, alpha);
        addVertex(consumer, pose, frontLeftBottom, color, alpha);
        addVertex(consumer, pose, frontLeftTop, color, alpha);
        addVertex(consumer, pose, backLeftTop, color, alpha);

        addVertex(consumer, pose, backLeftTop, color, alpha);
        addVertex(consumer, pose, frontLeftTop, color, alpha);
        addVertex(consumer, pose, frontRightTop, color, alpha);
        addVertex(consumer, pose, backRightTop, color, alpha);

        addVertex(consumer, pose, frontLeftBottom, color, alpha);
        addVertex(consumer, pose, backLeftBottom, color, alpha);
        addVertex(consumer, pose, backRightBottom, color, alpha);
        addVertex(consumer, pose, frontRightBottom, color, alpha);
    }

    /**
     * adds a Vertex with the DefaultVertexFormat.POSITION_COLOR_NORMAL format to the buffer builder
     *
     * @param consumer BufferBuilder to add the vertex to
     * @param pose     Pose to use for positioning the vertex
     * @param pos      position of the vertex
     * @param color    color of the vertex 0-255
     * @param alpha    transparency of the vertex 0-255
     */
    private static void addVertex(
        VertexConsumer consumer, PoseStack.Pose pose, Vec3 pos, Vec3i color, int alpha)
    {
        consumer.addVertex(pose, (float) pos.x, (float) pos.y, (float) pos.z)
            .setColor(color.getX(), color.getY(), color.getZ(), alpha);
    }

    /**
     * adds the given CustomGeometryRenderer to render after Translucnets
     *
     * @param output                 order to render at
     * @param poseStack              PoseStack to use for the submit
     * @param renderType             rendertype to submit as
     * @param customGeometryRenderer renderer to add
     */
    public static void submitLateCustomGeometry(
        OrderedSubmitNodeCollector output, PoseStack poseStack, RenderType renderType,
        SubmitNodeCollector.CustomGeometryRenderer customGeometryRenderer)
    {
        ((SubmitNodeCollectionExtension) output).vivecraft$submitLateCustomGeometry(poseStack, renderType,
            customGeometryRenderer);
    }

    private static final Map<String, Pair<Integer, Integer>> GL_ERRORS = new HashMap<>();

    /**
     * checks if there were any opengl errors since this was last called
     *
     * @param errorSection name of the section that is checked, this gets logged if there are any errors
     * @return error string if there was one
     */
    public static String checkGLError(String errorSection) {
        int error = GlStateManager._getError();
        int count = 0;
        Pair<Integer, Integer> oldError = GL_ERRORS.get(errorSection);
        if (error != 0 && oldError != null && oldError.getLeft() == error) {
            count = oldError.getRight() + 1;
        }
        GL_ERRORS.put(errorSection, Pair.of(error, count));
        if (error != 0 && count < 5) {
            String errorString = switch (error) {
                case GL11C.GL_INVALID_ENUM -> "invalid enum";
                case GL11C.GL_INVALID_VALUE -> "invalid value";
                case GL11C.GL_INVALID_OPERATION -> "invalid operation";
                case GL11C.GL_STACK_OVERFLOW -> "stack overflow";
                case GL11C.GL_STACK_UNDERFLOW -> "stack underflow";
                case GL11C.GL_OUT_OF_MEMORY -> "out of memory";
                case GL30C.GL_INVALID_FRAMEBUFFER_OPERATION -> "framebuffer is not complete";
                default -> "unknown error";
            };
            VRSettings.LOGGER.error("Vivecraft: ########## GL ERROR ##########");
            VRSettings.LOGGER.error("Vivecraft: @ {}", errorSection);
            VRSettings.LOGGER.error("Vivecraft: {}: {}", error, errorString);
            return errorString;
        } else if (count == 5) {
            VRSettings.LOGGER.error("Vivecraft: repeated gl errors for {}, not logging anymore", errorSection);
        }
        return "";
    }
}
