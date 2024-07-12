package org.vivecraft.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.Pose;
import org.vivecraft.common.network.VrPlayerState;
import org.vivecraft.common.utils.math.Vector3;

import javax.annotation.Nullable;

public class ServerVivePlayer {
    // player movement state
    @Nullable
    public VrPlayerState vrPlayerState;
    // how much the player is drawing the roomscale bow
    public float draw;
    public float worldScale = 1.0F;
    public float heightScale = 1.0F;
    public byte activeHand = 0;
    public boolean crawling;
    // if the player has VR active
    private boolean isVR = false;
    // offset set during aimFix to keep the original data positions
    public Vec3 offset = new Vec3(0.0D, 0.0D, 0.0D);
    // player this data belongs to
    public ServerPlayer player;
    final Vector3 forward = new Vector3(0.0F, 0.0F, -1.0F);
    // network protocol this player is communicating with
    public int networkVersion = CommonNetworkHelper.MAX_SUPPORTED_NETWORK_VERSION;

    public ServerVivePlayer(ServerPlayer player) {
        this.player = player;
    }

    /**
     * transforms the local {@code direction} vector on controller {@code controller} into world space
     * @param controller controller to get the direction on
     * @param direction local direction to transform
     * @return direction in world space
     */
    public Vec3 getControllerVectorCustom(int controller, Vector3 direction) {
        if (this.isSeated()) {
            controller = 0;
        }

        if (this.vrPlayerState != null) {
            Pose controllerPose = controller == 0 ? this.vrPlayerState.controller0() : this.vrPlayerState.controller1();
            Vector3 out = controllerPose.orientation().multiply(direction);
            return new Vec3(out.getX(), out.getY(), out.getZ());
        } else {
            return this.player.getLookAngle();
        }
    }

    /**
     * @param controller controller to get the direction from
     * @return forward direction of the given controller
     */
    public Vec3 getControllerDir(int controller) {
        return this.getControllerVectorCustom(controller, this.forward);
    }

    /**
     * @return looking direction of the head
     */
    public Vec3 getHMDDir() {
        if (this.vrPlayerState != null) {
            Vector3 out = this.vrPlayerState.hmd().orientation().multiply(this.forward);
            return new Vec3(out.getX(), out.getY(), out.getZ());
        } else {
            return this.player.getLookAngle();
        }
    }

    /**
     * @return position of the head
     */
    public Vec3 getHMDPos() {
        if (this.vrPlayerState != null) {
            return this.vrPlayerState.hmd().position().add(this.player.position()).add(this.offset);
        } else {
            return this.player.position().add(0.0D, 1.62D, 0.0D);
        }
    }

    /**
     * @param c controller to get the position for
     * @param realPosition if true disables the seated override
     * @return controller position in world space
     */
    public Vec3 getControllerPos(int c, boolean realPosition) {
        if (this.vrPlayerState != null) {

            // in seated the realPosition is at the head,
            // so reconstruct the seated position when wanting the visual position
            if (this.isSeated() && !realPosition) {
                Vec3 dir = this.getHMDDir();
                dir = dir.yRot((float) Math.toRadians(c == 0 ? -35.0D : 35.0D));
                dir = new Vec3(dir.x, 0.0D, dir.z);
                dir = dir.normalize();
                return this.getHMDPos().add(
                    dir.x * 0.3D * this.worldScale,
                    -0.4D * this.worldScale,
                    dir.z * 0.3D * this.worldScale);
            }

            Pose controllerState = c == 0 ? this.vrPlayerState.controller0() : this.vrPlayerState.controller1();

            return controllerState.position().add(this.player.position()).add(this.offset);
        } else {
            return this.player.position().add(0.0D, 1.62D, 0.0D);
        }
    }

    /**
     * @param c controller to get the position for
     * @return controller position in world space
     */
    public Vec3 getControllerPos(int c) {
        return getControllerPos(c, false);
    }

    /**
     * @return if the player has VR active
     */
    public boolean isVR() {
        return this.isVR;
    }

    /**
     * set VR state of the player
     */
    public void setVR(boolean vr) {
        this.isVR = vr;
    }

    /**
     * @return if the player is using seated VR
     */
    public boolean isSeated() {
        return this.vrPlayerState != null && this.vrPlayerState.seated();
    }
}
