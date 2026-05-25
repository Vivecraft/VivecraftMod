package org.vivecraft.client_vr.render.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
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
import org.vivecraft.client_vr.render.helpers.gizmos.OrientedCircleGizmo;
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
    public static void extractDebug(float partialTick) {
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
            for (Player p : MC.player.level().players()) {
                if (ClientVRPlayers.getInstance().isVRPlayer(p)) {
                    ClientVRPlayers.RotInfo info = ClientVRPlayers.getInstance().getRotationsForPlayer(p.getUUID());

                    Vec3 playerPos = p.getPosition(partialTick);
                    if (p == MC.player) {
                        playerPos = ((GameRendererExtension) MC.gameRenderer).vivecraft$getRvePos(partialTick);
                    }

                    if (p != MC.player || DATA_HOLDER.currentPass == RenderPass.THIRD) {
                        addAxes(playerPos, info.headPos, info.headRot, info.headQuat);
                    }
                    if (!info.seated) {
                        addAxes(playerPos, info.mainHandPos, info.mainHandRot,
                            info.mainHandQuat);
                        addAxes(playerPos, info.offHandPos, info.offHandRot,
                            info.offHandQuat);
                    }
                    if (info.fbtMode != FBTMode.ARMS_ONLY) {
                        addAxes(playerPos, info.waistPos, info.waistQuat);
                        addAxes(playerPos, info.rightFootPos, info.rightFootQuat);
                        addAxes(playerPos, info.leftFootPos, info.leftFootQuat);
                    }
                    if (info.fbtMode == FBTMode.WITH_JOINTS) {
                        addAxes(playerPos, info.rightElbowPos, info.rightElbowQuat);
                        addAxes(playerPos, info.leftElbowPos, info.leftElbowQuat);
                        addAxes(playerPos, info.rightKneePos, info.rightKneeQuat);
                        addAxes(playerPos, info.leftKneePos, info.leftKneeQuat);
                    }
                }
            }
        }
    }

    /**
     * renders all available device axes using the provided VRData
     *
     * @param data VRData to get the devices from
     */
    public static void renderDeviceAxes(VRData data) {
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

        list.forEach(DebugRenderHelper::addAxes);
    }

    /**
     * renders cubes for all tracked devices the current VR runtime offers
     *
     * @param showNames if device names should be shown
     */
    private static void renderTackerPositions(boolean showNames) {
        VRData data = DATA_HOLDER.vrPlayer.getVRDataWorld();

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
            Vec3 trackerPos = VRPlayer.roomToWorldPos(pos, data);

            if (showNames) {
                if (tracker.getMiddle() >= 0) {
                    addNamedCube(trackerPos, Component.translatable("vivecraft.formatting.name_value",
                            Component.literal(tracker.getLeft().source.toString()), labels[tracker.getMiddle()]), 0.05F,
                        MathUtils.DARK_GRAY_INT);
                } else {
                    addNamedCube(trackerPos, Component.translatable("vivecraft.formatting.name_value",
                        Component.literal(tracker.getLeft().source.toString() + tracker.getLeft().deviceIndex),
                        Component.translatable("vivecraft.messages.tracker.unknown")), 0.05F, MathUtils.DARK_GRAY_INT);
                }
            } else {
                renderCube(trackerPos, 0.05F, MathUtils.DARK_GRAY_INT);
            }
        }
    }

    /**
     * renders forward, up and right axes using the position and orientation
     *
     * @param pos    Vec3 for world position
     * @param matrix Matrix4f to use for orientation
     */
    public static void renderLocalAxes(Vec3 pos, Matrix4f matrix) {
        addLine(pos, matrix.transformDirection(MathUtils.BACK, new Vector3f()), MathUtils.BLUE_INT);
        addLine(pos, matrix.transformDirection(MathUtils.UP, new Vector3f()), MathUtils.GREEN_INT);
        addLine(pos, matrix.transformDirection(MathUtils.RIGHT, new Vector3f()), MathUtils.RED_INT);
    }

    /**
     * adds device axes for the given VRDevicePose
     *
     * @param pose VRDevicePose to get the orientation and position from.
     */
    private static void addAxes(VRData.VRDevicePose pose) {
        Vec3 position = pose.getPosition();

        float scale = 0.25F * DATA_HOLDER.vrPlayer.worldScale;

        Vector3f forward = pose.getDirection().mul(scale);
        Vector3f up = pose.getCustomVector(MathUtils.UP).mul(scale);
        Vector3f right = pose.getCustomVector(MathUtils.RIGHT).mul(scale);

        addLine(position, forward, MathUtils.BLUE_INT);
        addLine(position, up, MathUtils.GREEN_INT);
        addLine(position, right, MathUtils.RED_INT);
    }

    /**
     * adds device axes for the given VRDevicePose, without dedicated direction vector
     *
     * @param playerPos player position, worldspace
     * @param devicePos device position, relative to the player
     * @param rot       device rotation
     */
    private static void addAxes(Vec3 playerPos, Vector3fc devicePos, Quaternionfc rot) {
        addAxes(playerPos, devicePos, rot.transform(MathUtils.BACK, new Vector3f()), rot);
    }

    /**
     * adds device axes to the {@code consumer} for the given VRDevicePose
     *
     * @param playerPos player position, worldspace
     * @param devicePos device position, relative to the player
     * @param dir       device forward direction
     * @param rot       device rotation
     */
    private static void addAxes(
        Vec3 playerPos, Vector3fc devicePos, Vector3fc dir, Quaternionfc rot)
    {
        Vec3 position = playerPos.add(devicePos.x(), devicePos.y(), devicePos.z());

        float scale = 0.25F * DATA_HOLDER.vrPlayer.worldScale;

        Vector3f forward = dir.mul(scale, new Vector3f());
        Vector3f up = rot.transform(MathUtils.UP, new Vector3f()).mul(scale);
        Vector3f right = rot.transform(MathUtils.RIGHT, new Vector3f()).mul(scale);

        addLine(position, forward, MathUtils.BLUE_INT);
        addLine(position, up, MathUtils.GREEN_INT);
        addLine(position, right, MathUtils.RED_INT);
    }

    /**
     * adds a line from {@code position} in direction {@code dir}, with the given {@code color}
     *
     * @param position line start position, in worldspace
     * @param dir      line end, relative to {@code position}
     * @param color    line color
     */
    private static void addLine(Vec3 position, Vector3fc dir, int color) {
        addSafeGizmo(new LineGizmo(position, position.add(dir.x(), dir.y(), dir.z()), color, 3.0F));
    }

    /**
     * renders a worldspace line
     *
     * @param color  color of the line
     * @param points list of points the line should follow, at least 2
     */
    public static void renderLine(int color, Vec3... points) {
        Vec3 lastPoint = null;
        for (Vec3 point : points) {
            if (lastPoint != null) {
                addSafeGizmo(new LineGizmo(lastPoint, point, color, 3.0F));
            }
            lastPoint = point;
        }
    }

    /**
     * renders a multi segment line
     *
     * @param points list of points the line should follow, at least 2, boolean of the pair indicates a line split
     * @param color  color of the line
     */
    public static void renderLine(List<Pair<Vec3, Boolean>> points, int color) {
        Pair<Vec3, Boolean> prev = null;

        for (Pair<Vec3, Boolean> point : points) {
            // don't connect on splits
            if (!point.getRight() && prev != null) {
                addSafeGizmo(new LineGizmo(prev.getLeft(), point.getLeft(), color, 3.0F));
            }
            prev = point;
        }
    }

    /**
     * renders a line, with a list of world space positions
     *
     * @param color  color of the line
     * @param points list of points the line should follow
     */
    public static void renderLine(int color, Iterable<Vec3> points) {
        Vec3 lastPoint = null;
        for (Vec3 point : points) {
            if (lastPoint != null) {
                addSafeGizmo(new LineGizmo(lastPoint, point, color, 3.0F));
            }
            lastPoint = point;
        }
    }

    /**
     * Renders a cube with text lable above it
     *
     * @param cubePos position to render the cube at, worldspace
     * @param label   label of the cube
     * @param size    cube size
     * @param color   cube color
     */
    private static void addNamedCube(
        Vec3 cubePos, Component label, float size, int color)
    {
        renderCube(cubePos, size, color);

        if (label != null) {
            renderTextAtRelativePosition(cubePos, label);
        }
    }

    /**
     * renders the given text above the given device, facing the camera
     *
     * @param device device index to render at
     * @param text   text to render
     */
    public static void renderTextAtDevice(int device, String text) {
        renderTextAtRelativePosition(DATA_HOLDER.vrPlayer.getVRDataWorld().getDevice(device).getPosition(), text);
    }

    /**
     * renders the given text at the given position, with the given rotation
     *
     * @param position position world space
     * @param text     text to render
     */
    public static void renderTextAtRelativePosition(Vec3 position, String text) {
        renderTextAtRelativePosition(position, Component.literal(text));
    }

    /**
     * renders the given text at the given position, with the given rotation
     *
     * @param position position world space
     * @param text     text to render
     */
    public static void renderTextAtRelativePosition(Vec3 position, Component text) {
        addSafeGizmo(new TextGizmo(position.add(0.0, 0.05, 0.0), text.getString(),
            TextGizmo.Style.whiteAndCentered().withScale(0.1F)));
    }

    /**
     * Renders a cube
     *
     * @param position position to render the cube at, world space
     * @param size     cube size
     * @param color    cube color
     */
    public static void renderCube(Vec3 position, float size, int color) {
        addSafeGizmo(new CuboidGizmo(new AABB(position.subtract(size * 0.5F), position.add(size * 0.5F)),
            GizmoStyle.strokeAndFill(color, 2.5F, color), false));
    }

    /**
     * adds a circle to the given {@code vertexConsumer}
     *
     * @param center  center to render the circle at, world space
     * @param forward world direction the circle points at
     * @param radius  circle
     * @param color   circle color
     */
    public static void addCircle(Vec3 center, Vector3fc forward, float radius, int color) {
        addSafeGizmo(new OrientedCircleGizmo(center, new Vector3f(forward), radius, GizmoStyle.stroke(color)));
    }

    /**
     * Renders a sphere made out of 4 circles, a camera facing one and 3 axis aligned ones
     *
     * @param center center to render the sphere at, world space
     * @param radius sphere radius
     * @param color  sphere color
     */
    public static void renderSphere(Vec3 center, float radius, int color) {
        addCircle(center, MathUtils.LEFT, radius, color);
        addCircle(center, MathUtils.FORWARD, radius, color);
        addCircle(center, MathUtils.UP, radius, color);
    }

    /**
     * Renders a cone made out of a circle and 4 lines
     *
     * @param tip    tip position of the cone, world space
     * @param dir    direction the cone base points at, world space
     * @param angle  radius of the cone
     * @param length length of the cone
     * @param color  sphere color
     */
    public static void renderCone(Vec3 tip, Vector3fc dir, float angle, float length, int color) {

        Vector3f centerOffset = dir.normalize(new Vector3f()).mul(length);
        Vec3 center = tip.add(centerOffset.x, centerOffset.y, centerOffset.z);
        float radius = length * (float) Math.tan(Math.toRadians(angle));
        addCircle(center, dir, radius, color);

        Vector3f offset = MathUtils.getPerpendicularVec(dir).mul(radius);
        for (int i = 0; i < 2; i++) {
            addSafeGizmo(new LineGizmo(center.add(offset.x, offset.y, offset.z), tip, color, 3.0F));
            addSafeGizmo(new LineGizmo(center.subtract(offset.x, offset.y, offset.z), tip, color, 3.0F));
            offset.rotateAxis(Mth.HALF_PI, dir.x(), dir.y(), dir.z());
        }
    }

    /**
     * Renders a cylinder made out of 2 circles and 4 lines
     *
     * @param bottom bottom position of the cylinder, world space
     * @param topDir vector from the bottom center to the top center, world space
     * @param radius radius of the cylinder
     * @param color  sphere color
     */
    public static void renderCylinder(Vec3 bottom, Vector3fc topDir, float radius, int color) {
        Vector3f dir = topDir.normalize(new Vector3f());

        addCircle(bottom, dir, radius, color);
        addCircle(bottom.add(topDir.x(), topDir.y(), topDir.z()), dir, radius, color);

        Vector3f offset = MathUtils.getPerpendicularVec(topDir).mul(radius);
        for (int i = 0; i < 4; i++) {
            Vec3 bot = bottom.add(offset.x, offset.y, offset.z);
            addSafeGizmo(new LineGizmo(bot, bot.add(topDir.x(), topDir.y(), topDir.z()), color, 3.0F));
            offset.rotateAxis(Mth.HALF_PI, dir.x(), dir.y(), dir.z());
        }
    }

    /**
     * renders the outline of the given world space AABB
     *
     * @param aabb  AABB to render
     * @param color color to render the AABB in
     */
    public static void renderAABB(AABB aabb, int color) {
        addSafeGizmo(new CuboidGizmo(aabb, GizmoStyle.stroke(color), false));
    }

    /**
     * catches the exeption that can be thrown when adding a Gizmo
     *
     * @param gizmo Gizmo to add
     */
    public static GizmoProperties addSafeGizmo(Gizmo gizmo) {
        try {
            return Gizmos.addGizmo(gizmo);
        } catch (IllegalStateException ignored) {
            // return a dummy properties
            return new GizmoProperties() {
                @Override
                public GizmoProperties setAlwaysOnTop() {
                    return this;
                }

                @Override
                public GizmoProperties persistForMillis(int milliseconds) {
                    return this;
                }

                @Override
                public GizmoProperties fadeOut() {
                    return this;
                }
            };
        }
    }
}
