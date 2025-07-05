package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.*;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.api.data.FBTMode;
import org.vivecraft.client.ClientVRPlayers;
import org.vivecraft.client.gui.screens.FBTCalibrationScreen;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.gameplay.trackers.DebugRenderTracker;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.client_vr.provider.DeviceSource;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.common.utils.MathUtils;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class DebugRenderHelper {

    private static final ClientDataHolderVR DATA_HOLDER = ClientDataHolderVR.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();

    /**
     * renders debug stuff
     *
     * @param partialTick current partial tick
     */
    public static void renderDebug(float partialTick) {
        // make sure this is on, sometimes it is off
        RenderSystem.enableDepthTest();
        if (DATA_HOLDER.vrSettings.renderDeviceAxes) {
            renderDeviceAxes(DATA_HOLDER.vrPlayer.getVRDataWorld());
        }

        if (DATA_HOLDER.vrSettings.renderVrPlayerAxes) {
            renderPlayerAxes(partialTick);
        }

        if (DATA_HOLDER.vrSettings.renderTrackerPositions || MC.screen instanceof FBTCalibrationScreen) {
            boolean showNames = true;
            if (MC.screen instanceof FBTCalibrationScreen fbtScreen) {
                showNames = fbtScreen.isCalibrated();
            }
            renderTackerPositions(showNames);
        }

        if (DATA_HOLDER.vrSettings.renderGameplayTrackers) {
            DATA_HOLDER.getTrackers().stream()
                .filter(t -> DATA_HOLDER.vrSettings.gameplayTrackerToRender.isEmpty() ||
                    t.getClass().getName().equals(DATA_HOLDER.vrSettings.gameplayTrackerToRender))
                .forEach(t -> {
                    if (t instanceof DebugRenderTracker debugTracker && t.isActive(MC.player)) {
                        debugTracker.renderDebug();
                    }
                });
        }
    }

    /**
     * renders all available remote devices from all players
     *
     * @param partialTick current partial tick
     */
    public static void renderPlayerAxes(float partialTick) {
        if (MC.player != null) {
            BufferBuilder bufferBuilder = null;
            Vec3 camPos = MC.gameRenderer.getMainCamera().getPosition();

            for (Player p : MC.player.level().players()) {
                if (ClientVRPlayers.getInstance().isVRPlayer(p)) {
                    ClientVRPlayers.RotInfo info = ClientVRPlayers.getInstance().getRotationsForPlayer(p.getUUID());

                    if (bufferBuilder == null) {
                        RenderSystem.setShader(CoreShaders.POSITION_COLOR);
                        bufferBuilder = Tesselator.getInstance()
                            .begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
                    }

                    Vector3f playerPos = p.getPosition(partialTick).subtract(camPos).toVector3f();
                    if (p == MC.player) {
                        playerPos = ((GameRendererExtension) MC.gameRenderer).vivecraft$getRvePos(partialTick)
                            .subtract(camPos).toVector3f();
                    }

                    if (p != MC.player || DATA_HOLDER.currentPass == RenderPass.THIRD) {
                        addAxes(bufferBuilder, playerPos, info.headPos, info.headRot, info.headQuat);
                    }
                    if (!info.seated) {
                        addAxes(bufferBuilder, playerPos, info.mainHandPos, info.mainHandRot,
                            info.mainHandQuat);
                        addAxes(bufferBuilder, playerPos, info.offHandPos, info.offHandRot,
                            info.offHandQuat);
                    }
                    if (info.fbtMode != FBTMode.ARMS_ONLY) {
                        addAxes(bufferBuilder, playerPos, info.waistPos, info.waistQuat);
                        addAxes(bufferBuilder, playerPos, info.rightFootPos, info.rightFootQuat);
                        addAxes(bufferBuilder, playerPos, info.leftFootPos, info.leftFootQuat);
                    }
                    if (info.fbtMode == FBTMode.WITH_JOINTS) {
                        addAxes(bufferBuilder, playerPos, info.rightElbowPos, info.rightElbowQuat);
                        addAxes(bufferBuilder, playerPos, info.leftElbowPos, info.leftElbowQuat);
                        addAxes(bufferBuilder, playerPos, info.rightKneePos, info.rightKneeQuat);
                        addAxes(bufferBuilder, playerPos, info.leftKneePos, info.leftKneeQuat);
                    }
                }
            }
            if (bufferBuilder != null) {
                BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
            }
        }
    }

    /**
     * renders all available device axes using the provided VRData
     *
     * @param data VRData to get the devices from
     */
    public static void renderDeviceAxes(VRData data) {
        RenderSystem.setShader(CoreShaders.POSITION_COLOR);
        BufferBuilder bufferBuilder = Tesselator.getInstance()
            .begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        List<VRData.VRDevicePose> list = new ArrayList<>();

        list.add(data.c2);

        if (DATA_HOLDER.currentPass == RenderPass.THIRD) {
            list.add(data.hmd);
        }

        if (DATA_HOLDER.cameraTracker.isVisible()) {
            list.add(data.cam);
        }

        if (MC.player != null && TelescopeTracker.isTelescope(MC.player.getMainHandItem()) &&
            TelescopeTracker.isViewing(0))
        {
            list.add(data.t0);
        } else {
            list.add(MC.player != null && MC.player.isShiftKeyDown() ? data.h0 : data.c0);
        }

        if (MC.player != null && TelescopeTracker.isTelescope(MC.player.getOffhandItem()) &&
            TelescopeTracker.isViewing(0))
        {
            list.add(data.t1);
        } else {
            list.add(MC.player != null && MC.player.isShiftKeyDown() ? data.h1 : data.c1);
        }

        if (data.fbtMode != FBTMode.ARMS_ONLY) {
            list.add(data.waist);
            list.add(data.foot_left);
            list.add(data.foot_right);
        }
        if (data.fbtMode == FBTMode.WITH_JOINTS) {
            list.add(data.elbow_left);
            list.add(data.knee_left);
            list.add(data.elbow_right);
            list.add(data.knee_right);
        }

        list.forEach(p -> addAxes(bufferBuilder, data, p));

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }

    /**
     * renders cubes for all tracked devices the current VR runtime offers
     *
     * @param showNames if device names should be shown
     */
    private static void renderTackerPositions(boolean showNames) {
        VRData data = DATA_HOLDER.vrPlayer.getVRDataWorld();
        Vec3 camPos = data.getEye(DATA_HOLDER.currentPass).getPosition();
        Quaternionf orientation = data.getEye(DATA_HOLDER.currentPass).getMatrix()
            .getNormalizedRotation(new Quaternionf())
            .rotateY(Mth.PI);

        Component[] labels = new Component[]{
            Component.translatable("vivecraft.toasts.point_controller.right"),
            Component.translatable("vivecraft.toasts.point_controller.left"),
            Component.translatable("vivecraft.messages.tracker.camera"),
            Component.translatable("vivecraft.messages.tracker.waist"),
            Component.translatable("vivecraft.messages.tracker.rightFoot"),
            Component.translatable("vivecraft.messages.tracker.leftFoot"),
            Component.translatable("vivecraft.messages.tracker.rightElbow"),
            Component.translatable("vivecraft.messages.tracker.leftElbow"),
            Component.translatable("vivecraft.messages.tracker.rightKnee"),
            Component.translatable("vivecraft.messages.tracker.leftKnee")
        };

        // show all trackers
        for (Triple<DeviceSource, Integer, Matrix4fc> tracker : MCVR.get().getTrackers()) {
            Vector3f pos = tracker.getRight().getTranslation(new Vector3f());
            Vec3 trackerPos = VRPlayer.roomToWorldPos(pos, data).subtract(camPos);
            pos.set((float) trackerPos.x, (float) trackerPos.y, (float) trackerPos.z);

            if (showNames) {
                if (tracker.getMiddle() >= 0) {
                    addNamedCube(pos, orientation, Component.translatable("vivecraft.formatting.name_value",
                            Component.literal(tracker.getLeft().source.toString()), labels[tracker.getMiddle()]), 0.05F,
                        MathUtils.DARK_GRAY);
                } else {
                    addNamedCube(pos, orientation, Component.translatable("vivecraft.formatting.name_value",
                        Component.literal(tracker.getLeft().source.toString() + tracker.getLeft().deviceIndex),
                        Component.translatable("vivecraft.messages.tracker.unknown")), 0.05F, MathUtils.DARK_GRAY);
                }
            } else {
                renderCube(pos, 0.05F, MathUtils.DARK_GRAY);
            }
        }
        MC.renderBuffers().bufferSource().endLastBatch();
    }

    /**
     * renders forward, up and right axes using the {@code matrix} position and orientation
     *
     * @param matrix Matrix4f to use for positioning
     */
    public static void renderLocalAxes(Matrix4f matrix) {
        RenderSystem.getModelViewStack().pushMatrix().mul(matrix);

        RenderSystem.setShader(CoreShaders.POSITION_COLOR);
        BufferBuilder bufferBuilder = Tesselator.getInstance()
            .begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        Vector3f position = new Vector3f();

        addLine(bufferBuilder, position, MathUtils.BACK, MathUtils.BLUE);
        addLine(bufferBuilder, position, MathUtils.UP, MathUtils.GREEN);
        addLine(bufferBuilder, position, MathUtils.RIGHT, MathUtils.RED);

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.getModelViewStack().popMatrix();
    }

    /**
     * adds device axes to the {@code bufferBuilder} for the given VRDevicePose
     *
     * @param bufferBuilder BufferBuilder to use, needs to be in DEBUG_LINE_STRIP and POSITION_COLOR mode
     * @param data          VRData to get camera position from
     * @param pose          VRDevicePose to ge the orientation and position from.
     */
    private static void addAxes(
        BufferBuilder bufferBuilder, VRData data, VRData.VRDevicePose pose)
    {
        Vector3f position = pose.getPosition().subtract(data.getEye(DATA_HOLDER.currentPass).getPosition())
            .toVector3f();

        float scale = 0.25F * DATA_HOLDER.vrPlayer.worldScale;

        Vector3f forward = pose.getDirection().mul(scale);
        Vector3f up = pose.getCustomVector(MathUtils.UP).mul(scale);
        Vector3f right = pose.getCustomVector(MathUtils.RIGHT).mul(scale);

        addLine(bufferBuilder, position, forward, MathUtils.BLUE);
        addLine(bufferBuilder, position, up, MathUtils.GREEN);
        addLine(bufferBuilder, position, right, MathUtils.RED);
    }

    /**
     * adds device axes to the {@code bufferBuilder} for the given VRDevicePose, without dedicated direction vector
     *
     * @param bufferBuilder BufferBuilder to use, needs to be in DEBUG_LINE_STRIP and POSITION_COLOR mode
     * @param playerPos     player position, relative to the camera
     * @param devicePos     device position, relative to the player
     * @param rot           device rotation
     */
    private static void addAxes(
        BufferBuilder bufferBuilder, Vector3fc playerPos, Vector3fc devicePos, Quaternionfc rot)
    {
        addAxes(bufferBuilder, playerPos, devicePos, rot.transform(MathUtils.BACK, new Vector3f()), rot);
    }

    /**
     * adds device axes to the {@code bufferBuilder} for the given VRDevicePose
     *
     * @param bufferBuilder BufferBuilder to use, needs to be in DEBUG_LINE_STRIP and POSITION_COLOR mode
     * @param playerPos     player position, relative to the camera
     * @param devicePos     device position, relative to the player
     * @param dir           device forward direction
     * @param rot           device rotation
     */
    private static void addAxes(
        BufferBuilder bufferBuilder, Vector3fc playerPos, Vector3fc devicePos, Vector3fc dir,
        Quaternionfc rot)
    {
        Vector3f position = playerPos.add(devicePos, new Vector3f());

        float scale = 0.25F * DATA_HOLDER.vrPlayer.worldScale;

        Vector3f forward = dir.mul(scale, new Vector3f());
        Vector3f up = rot.transform(MathUtils.UP, new Vector3f()).mul(scale);
        Vector3f right = rot.transform(MathUtils.RIGHT, new Vector3f()).mul(scale);

        addLine(bufferBuilder, position, forward, MathUtils.BLUE);
        addLine(bufferBuilder, position, up, MathUtils.GREEN);
        addLine(bufferBuilder, position, right, MathUtils.RED);
    }

    /**
     * adds a line from {@code position} in direction {@code dir}, with the given {@code color}, to the given {@code bufferBuilder}
     *
     * @param bufferBuilder BufferBuilder to use, needs to be in DEBUG_LINE_STRIP and POSITION_COLOR mode
     * @param position      line start position
     * @param dir           line end, relative to {@code position}
     * @param color         line color
     */
    private static void addLine(BufferBuilder bufferBuilder, Vector3fc position, Vector3fc dir, Vector3fc color)
    {
        bufferBuilder.addVertex(position.x(), position.y(), position.z())
            .setColor(color.x(), color.y(), color.z(), 0.0F);
        bufferBuilder.addVertex(position.x(), position.y(), position.z())
            .setColor(color.x(), color.y(), color.z(), 1.0F);
        bufferBuilder.addVertex(position.x() + dir.x(), position.y() + dir.y(), position.z() + dir.z())
            .setColor(color.x(), color.y(), color.z(), 1.0F);
        bufferBuilder.addVertex(position.x() + dir.x(), position.y() + dir.y(), position.z() + dir.z())
            .setColor(color.x(), color.y(), color.z(), 0.0F);
    }

    /**
     * renders a camera relative line
     *
     * @param color  color of the line
     * @param points list of points the line should follow, at least 2
     */
    public static void renderLine(Vector3fc color, Vector3fc... points) {
        RenderSystem.setShader(CoreShaders.POSITION_COLOR);
        BufferBuilder bufferBuilder = Tesselator.getInstance()
            .begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (Vector3fc point : points) {
            bufferBuilder.addVertex(point.x(), point.y(), point.z())
                .setColor(color.x(), color.y(), color.z(), 1.0F);
        }

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }

    /**
     * renders a camera relative line, with a list of world space positions, and a camera position
     *
     * @param color  color of the line
     * @param camPos position of the camera
     * @param points list of points the line should follow
     */
    public static void renderLine(Vector3fc color, Vec3 camPos, Iterable<Vec3> points) {
        RenderSystem.setShader(CoreShaders.POSITION_COLOR);
        BufferBuilder bufferBuilder = Tesselator.getInstance()
            .begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (Vec3 point : points) {
            bufferBuilder.addVertex((float) (point.x() - camPos.x()), (float) (point.y() - camPos.y()),
                    (float) (point.z() - camPos.z()))
                .setColor(color.x(), color.y(), color.z(), 1.0F);
        }

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }

    /**
     * Renders a cube with text lable above it
     *
     * @param cubePos position to render the cube at, camera relative
     * @param rot     rotation facing the camera, to align the text
     * @param label   label of the cube
     * @param size    cube size
     * @param color   cube color
     */
    private static void addNamedCube(
        Vector3fc cubePos, Quaternionf rot, Component label, float size, Vector3fc color)
    {
        renderCube(cubePos, size, color);

        if (label != null) {
            renderTextAtRelativePosition(cubePos.x(), cubePos.y(), cubePos.z(), rot, label);
        }
    }

    /**
     * renders the given text above the given device, facing the camera
     *
     * @param poseStack PoseStack to use for positioning
     * @param device    device index to render at
     * @param text      text to render
     */
    public static void renderTextAtDevice(PoseStack poseStack, int device, String text) {
        renderTextAtPosition(poseStack, DATA_HOLDER.vrPlayer.getVRDataWorld().getDevice(device).getPosition(), text);
    }

    /**
     * renders the given text at the given world space position, facing the camera
     *
     * @param poseStack PoseStack to use for positioning
     * @param position  position to render at, in world space
     * @param text      text to render
     */
    public static void renderTextAtPosition(PoseStack poseStack, Vec3 position, String text) {
        VRData data = DATA_HOLDER.vrPlayer.getVRDataWorld();
        Vec3 camPos = data.getEye(DATA_HOLDER.currentPass).getPosition();
        Quaternionf rot = data.getEye(DATA_HOLDER.currentPass).getMatrix()
            .getNormalizedRotation(new Quaternionf())
            .rotateY(Mth.PI);
        Vector3f pos = MathUtils.subtractToVector3f(position, camPos);

        renderTextAtRelativePosition(pos.x, pos.y, pos.z, rot, text);
    }

    /**
     * renders the given text at the given camera relative position, with the given rotation
     *
     * @param x    x position relative to the camera
     * @param y    y position relative to the camera
     * @param z    z position relative to the camera
     * @param rot  rotation the text should look at
     * @param text text to render
     */
    public static void renderTextAtRelativePosition(
        float x, float y, float z, Quaternionf rot, String text)
    {
        renderTextAtRelativePosition(x, y, z, rot, Component.literal(text));
    }

    /**
     * renders the given text at the given camera relative position, with the given rotation
     *
     * @param x    x position relative to the camera
     * @param y    y position relative to the camera
     * @param z    z position relative to the camera
     * @param rot  rotation the text should look at
     * @param text text to render
     */
    public static void renderTextAtRelativePosition(
        float x, float y, float z, Quaternionf rot, Component text)
    {
        Matrix4f matrix = new Matrix4f();
        matrix.translate(x, y + 0.05F, z);
        matrix.rotate(rot);
        matrix.scale(-0.005F, -0.005F, 0.005F);

        MC.font.drawInBatch(text, MC.font.width(text) * -0.5F, -MC.font.lineHeight, -1, false,
            matrix, MC.renderBuffers().bufferSource(), Font.DisplayMode.NORMAL, 0,
            LightTexture.FULL_BRIGHT);
    }

    /**
     * Renders a cube
     *
     * @param position position to render the cube at, camera relative
     * @param size     cube size
     * @param color    cube color
     */
    public static void renderCube(Vector3fc position, float size, Vector3fc color) {
        RenderSystem.setShader(CoreShaders.POSITION_COLOR);
        RenderSystem.setShaderTexture(0, RenderHelper.WHITE_TEXTURE);

        BufferBuilder bufferBuilder = Tesselator.getInstance()
            .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        Vec3i iColor = new Vec3i((int) (color.x() * 255), (int) (color.y() * 255), (int) (color.z() * 255));
        Vec3 start = new Vec3(position.x(), position.y(), position.z()).add(MathUtils.FORWARD_D.scale(size * 0.5F));
        Vec3 end = new Vec3(position.x(), position.y(), position.z()).add(MathUtils.BACK_D.scale(size * 0.5F));
        RenderHelper.renderBox(bufferBuilder, start, end, size, size, iColor, (byte) 255, new Matrix4f());

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }

    /**
     * adds a circle to the given {@code bufferBuilder}
     *
     * @param bufferBuilder BufferBuilder to add the circle to
     * @param center        center to render the circle at, world camera relative
     * @param forward       world direction the circle points at
     * @param radius        circle
     * @param color         circle color
     */
    public static void addCircle(
        BufferBuilder bufferBuilder, Vector3fc center, Vector3fc forward, float radius, Vector3fc color)
    {
        Vector3f offset = MathUtils.getPerpendicularVec(forward).mul(radius);

        bufferBuilder.addVertex(center.x() + offset.x(), center.y() + offset.y(), center.z() + offset.z())
            .setColor(color.x(), color.y(), color.z(), 0.0F);

        for (int i = 0; i <= 20; i++) {
            bufferBuilder.addVertex(center.x() + offset.x(), center.y() + offset.y(), center.z() + offset.z())
                .setColor(color.x(), color.y(), color.z(), 1.0F);
            if (i != 20) {
                offset.rotateAxis(Mth.TWO_PI / 20F, forward.x(), forward.y(), forward.z());
            }
        }

        bufferBuilder.addVertex(center.x() + offset.x(), center.y() + offset.y(), center.z() + offset.z())
            .setColor(color.x(), color.y(), color.z(), 0.0F);
    }

    /**
     * Renders a camera facing circle
     *
     * @param center  center to render the circle at, world camera relative
     * @param forward direction the camera faces, world direction
     * @param radius  circle radius
     * @param color   circle color
     */
    public static void renderCircle(
        Vector3fc center, Vector3fc forward, float radius, Vector3fc color)
    {
        RenderSystem.setShader(CoreShaders.POSITION_COLOR);
        BufferBuilder bufferBuilder = Tesselator.getInstance()
            .begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        addCircle(bufferBuilder, center, forward, radius, color);

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }

    /**
     * Renders a sphere made out of 4 circles, a camera facing one and 3 axis aligned ones
     *
     * @param center center to render the sphere at, world camera relative
     * @param radius sphere radius
     * @param color  sphere color
     */
    public static void renderSphere(Vector3fc center, float radius, Vector3fc color) {
        RenderSystem.setShader(CoreShaders.POSITION_COLOR);
        BufferBuilder bufferBuilder = Tesselator.getInstance()
            .begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        addCircle(bufferBuilder, center, MathUtils.LEFT, radius, color);
        addCircle(bufferBuilder, center, MathUtils.FORWARD, radius, color);
        addCircle(bufferBuilder, center, MathUtils.UP, radius, color);

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }

    /**
     * Renders a sphere made out of 4 circles, a camera facing one and 3 axis aligned ones
     *
     * @param tip    tip position of the cone, world camera relative
     * @param dir    direction the cone base points at, world space
     * @param angle  radius of the cone
     * @param length length of the cone
     * @param color  sphere color
     */
    public static void renderCone(Vector3fc tip, Vector3fc dir, float angle, float length, Vector3fc color) {
        RenderSystem.setShader(CoreShaders.POSITION_COLOR);
        BufferBuilder bufferBuilder = Tesselator.getInstance()
            .begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        Vector3f center = dir.normalize(new Vector3f()).mul(length).add(tip);
        float radius = length * (float) Math.tan(Math.toRadians(angle));
        addCircle(bufferBuilder, center, dir, radius, color);

        Vector3f offset = MathUtils.getPerpendicularVec(dir).mul(radius);
        for (int i = 0; i < 2; i++) {
            bufferBuilder.addVertex(center.x() + offset.x(), center.y() + offset.y(), center.z() + offset.z())
                .setColor(color.x(), color.y(), color.z(), 0.0F);
            bufferBuilder.addVertex(center.x() + offset.x(), center.y() + offset.y(), center.z() + offset.z())
                .setColor(color.x(), color.y(), color.z(), 1.0F);
            bufferBuilder.addVertex(tip.x(), tip.y(), tip.z())
                .setColor(color.x(), color.y(), color.z(), 1.0F);
            bufferBuilder.addVertex(center.x() - offset.x(), center.y() - offset.y(), center.z() - offset.z())
                .setColor(color.x(), color.y(), color.z(), 1.0F);
            bufferBuilder.addVertex(center.x() - offset.x(), center.y() - offset.y(), center.z() - offset.z())
                .setColor(color.x(), color.y(), color.z(), 0.0F);
            offset.rotateAxis(Mth.HALF_PI, dir.x(), dir.y(), dir.z());
        }

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }

    public static void renderAABB(AABB aabb, Vector3fc color) {
        RenderSystem.setShader(CoreShaders.POSITION_COLOR);
        RenderSystem.setShaderTexture(0, RenderHelper.WHITE_TEXTURE);

        BufferBuilder bufferBuilder = Tesselator.getInstance()
            .begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.addVertex((float) aabb.minX, (float) aabb.minY, (float) aabb.minZ)
            .setColor(color.x(), color.y(), color.z(), 1.0F);
        bufferBuilder.addVertex((float) aabb.minX, (float) aabb.maxY, (float) aabb.minZ)
            .setColor(color.x(), color.y(), color.z(), 1.0F);
        bufferBuilder.addVertex((float) aabb.minX, (float) aabb.maxY, (float) aabb.maxZ)
            .setColor(color.x(), color.y(), color.z(), 1.0F);
        bufferBuilder.addVertex((float) aabb.minX, (float) aabb.minY, (float) aabb.maxZ)
            .setColor(color.x(), color.y(), color.z(), 1.0F);
        bufferBuilder.addVertex((float) aabb.minX, (float) aabb.minY, (float) aabb.minZ)
            .setColor(color.x(), color.y(), color.z(), 1.0F);
        bufferBuilder.addVertex((float) aabb.maxX, (float) aabb.minY, (float) aabb.minZ)
            .setColor(color.x(), color.y(), color.z(), 1.0F);
        bufferBuilder.addVertex((float) aabb.maxX, (float) aabb.maxY, (float) aabb.minZ)
            .setColor(color.x(), color.y(), color.z(), 1.0F);
        bufferBuilder.addVertex((float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ)
            .setColor(color.x(), color.y(), color.z(), 1.0F);
        bufferBuilder.addVertex((float) aabb.maxX, (float) aabb.minY, (float) aabb.maxZ)
            .setColor(color.x(), color.y(), color.z(), 1.0F);
        bufferBuilder.addVertex((float) aabb.maxX, (float) aabb.minY, (float) aabb.minZ)
            .setColor(color.x(), color.y(), color.z(), 1.0F);
        bufferBuilder.addVertex((float) aabb.maxX, (float) aabb.minY, (float) aabb.minZ)
            .setColor(color.x(), color.y(), color.z(), 0.0F);
        bufferBuilder.addVertex((float) aabb.maxX, (float) aabb.maxY, (float) aabb.minZ)
            .setColor(color.x(), color.y(), color.z(), 0.0F);
        bufferBuilder.addVertex((float) aabb.maxX, (float) aabb.maxY, (float) aabb.minZ)
            .setColor(color.x(), color.y(), color.z(), 1.0F);
        bufferBuilder.addVertex((float) aabb.minX, (float) aabb.maxY, (float) aabb.minZ)
            .setColor(color.x(), color.y(), color.z(), 1.0F);
        bufferBuilder.addVertex((float) aabb.minX, (float) aabb.maxY, (float) aabb.minZ)
            .setColor(color.x(), color.y(), color.z(), 0.0F);
        bufferBuilder.addVertex((float) aabb.minX, (float) aabb.maxY, (float) aabb.maxZ)
            .setColor(color.x(), color.y(), color.z(), 0.0F);
        bufferBuilder.addVertex((float) aabb.minX, (float) aabb.maxY, (float) aabb.maxZ)
            .setColor(color.x(), color.y(), color.z(), 1.0F);
        bufferBuilder.addVertex((float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ)
            .setColor(color.x(), color.y(), color.z(), 1.0F);
        bufferBuilder.addVertex((float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ)
            .setColor(color.x(), color.y(), color.z(), 0.0F);
        bufferBuilder.addVertex((float) aabb.maxX, (float) aabb.minY, (float) aabb.maxZ)
            .setColor(color.x(), color.y(), color.z(), 0.0F);
        bufferBuilder.addVertex((float) aabb.maxX, (float) aabb.minY, (float) aabb.maxZ)
            .setColor(color.x(), color.y(), color.z(), 1.0F);
        bufferBuilder.addVertex((float) aabb.minX, (float) aabb.minY, (float) aabb.maxZ)
            .setColor(color.x(), color.y(), color.z(), 1.0F);


        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }
}
