package org.vivecraft.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.vivecraft.api.data.VRData;
import org.vivecraft.common.api_impl.data.VRDataImpl;
import org.vivecraft.common.api_impl.data.VRPoseImpl;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.Pose;
import org.vivecraft.common.network.VrPlayerState;
import org.vivecraft.common.utils.math.Vector3;

import javax.annotation.Nullable;

public class ServerVivePlayer {
    @Nullable
    public VrPlayerState vrPlayerState;
    public float draw;
    public float worldScale = 1.0F;
    public float heightScale = 1.0F;
    public byte activeHand = 0;
    public boolean crawling;
    private boolean isVR = false;
    public Vec3 offset = new Vec3(0.0D, 0.0D, 0.0D);
    public ServerPlayer player;
    final Vector3 forward = new Vector3(0.0F, 0.0F, -1.0F);

    public int networkVersion = CommonNetworkHelper.MAX_SUPPORTED_NETWORK_VERSION;

    public ServerVivePlayer(ServerPlayer player) {
        this.player = player;
    }

    public float getDraw() {
        return this.draw;
    }

    public Vec3 getControllerVectorCustom(int controller, Vector3 direction) {
        if (this.isSeated()) {
            controller = 0;
        }

        var controllerPose = controller == 0 ? this.vrPlayerState.controller0() : this.vrPlayerState.controller1();

        if (controllerPose != null) {
            Vector3 vector3 = controllerPose.orientation().multiply(direction);
            return new Vec3(vector3.getX(), vector3.getY(), vector3.getZ());
        } else {
            return this.player.getLookAngle();
        }
    }

    public Vec3 getControllerDir(int controller) {
        return this.getControllerVectorCustom(controller, this.forward);
    }

    public Vec3 getHMDDir() {
        if (this.vrPlayerState != null) {
            Vector3 vector3 = this.vrPlayerState.hmd().orientation().multiply(this.forward);
            return new Vec3(vector3.getX(), vector3.getY(), vector3.getZ());
        }
        return this.player.getLookAngle();
    }

    public Vec3 getHMDPos(Player player) {
        if (this.vrPlayerState != null) {
            return this.vrPlayerState.hmd().position().add(player.position()).add(this.offset);
        }
        return player.position().add(0.0D, 1.62D, 0.0D);
    }

    public Quaternionf getHMDQuaternion() {
        if (this.vrPlayerState != null) {
            return this.vrPlayerState.hmd().orientation().asJOMLQuaternion();
        }
        return null; // Should only return null if player isn't in VR.
    }

    public Vec3 getControllerPos(int c, Player player, boolean realPosition) {
        if (this.vrPlayerState != null) {

            // TODO: What the fuck is this nonsense?
            if (this.isSeated() && !realPosition) {
                Vec3 vec3 = this.getHMDDir();
                vec3 = vec3.yRot((float) Math.toRadians(c == 0 ? -35.0D : 35.0D));
                vec3 = new Vec3(vec3.x, 0.0D, vec3.z);
                vec3 = vec3.normalize();
                return this.getHMDPos(player).add(vec3.x * 0.3D * (double) this.worldScale, -0.4D * (double) this.worldScale, vec3.z * 0.3D * (double) this.worldScale);
            }

            var controllerState = c == 0 ? this.vrPlayerState.controller0() : this.vrPlayerState.controller1();

            return controllerState.position().add(player.position()).add(this.offset);
        }

        return player.position().add(0.0D, 1.62D, 0.0D);
    }

    public Vec3 getControllerPos(int c, Player player) {
        return getControllerPos(c, player, false);
    }

    public Quaternionf getControllerQuaternion(int c) {
        if (this.vrPlayerState != null) {
            Pose controllerPose = c == 0 ? this.vrPlayerState.controller0() : this.vrPlayerState.controller1();
            return controllerPose.orientation().asJOMLQuaternion();
        }
        return null; // Should only return null if player isn't in VR.
    }

    public boolean isVR() {
        return this.isVR;
    }

    public void setVR(boolean vr) {
        this.isVR = vr;
    }

    public boolean isSeated() {
        if (this.vrPlayerState == null) {
            return false;
        }
        return this.vrPlayerState.seated();
    }

    public boolean usingReversedHands() {
        if (this.vrPlayerState == null) {
            return false;
        }
        return this.vrPlayerState.reverseHands();
    }

    public VRData asVRData() {
        if (this.vrPlayerState == null) {
            return null;
        }
        return new VRDataImpl(
            new VRPoseImpl(this.getHMDPos(player), this.getHMDDir(), this.getHMDQuaternion()),
            new VRPoseImpl(this.getControllerPos(0, player), this.getControllerDir(0), this.getControllerQuaternion(0)),
            new VRPoseImpl(this.getControllerPos(1, player), this.getControllerDir(1), this.getControllerQuaternion(1)),
            this.isSeated(),
            this.usingReversedHands()
        );
    }
}
