package org.vivecraft.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.Pose;
import org.vivecraft.common.network.VRPlayerState;

import javax.annotation.Nullable;

import static org.joml.Math.toRadians;
import static org.vivecraft.common.utils.Utils.convertToVec3;
import static org.vivecraft.common.utils.Utils.forward;

public class ServerVivePlayer {
    @Nullable
    public VRPlayerState vrPlayerState;
    public float draw;
    public float worldScale = 1.0F;
    public float heightScale = 1.0F;
    public byte activeHand = 0;
    public boolean crawling;
    private boolean isVR = false;
    public Vec3 offset = new Vec3(0.0D, 0.0D, 0.0D);
    public ServerPlayer player;
    public int networkVersion = CommonNetworkHelper.MAX_SUPPORTED_NETWORK_VERSION;

    public ServerVivePlayer(ServerPlayer player) {
        this.player = player;
    }

    public float getDraw() {
        return this.draw;
    }

    public Vec3 getControllerVectorCustom(int controller, Vector3f direction) {
        Pose controllerPose = (controller == 0 || this.isSeated() ?
                               this.vrPlayerState.controller0() :
                               this.vrPlayerState.controller1()
        );

        return (controllerPose != null ?
                convertToVec3(controllerPose.orientation().transformUnit(direction, new Vector3f())) :
                this.player.getLookAngle()
        );
    }

    public Vec3 getControllerDir(int controller) {
        return this.getControllerVectorCustom(controller, new Vector3f(forward));
    }

    public Vec3 getHMDDir() {
        if (this.vrPlayerState != null) {
            Vector3f vector3 = this.vrPlayerState.hmd().orientation().transformUnit(new Vector3f(forward), new Vector3f());
            return new Vec3(vector3.x, vector3.y, vector3.z);
        }
        return this.player.getLookAngle();
    }

    public Vec3 getHMDPos(Player player) {
        if (this.vrPlayerState != null) {
            return this.vrPlayerState.hmd().position().add(player.position()).add(this.offset);
        }
        return player.position().add(0.0D, 1.62D, 0.0D);
    }

    public Vec3 getControllerPos(int c, Player player, boolean realPosition) {
        if (this.vrPlayerState != null) {

            // TODO: What the fuck is this nonsense?
            if (this.isSeated() && !realPosition) {
                Vec3 vec3 = this.getHMDDir();
                vec3 = vec3.yRot(toRadians(c == 0 ? -35.0F : 35.0F));
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

    public boolean isVR() {
        return this.isVR;
    }

    public void setVR(boolean vr) {
        this.isVR = vr;
    }

    public boolean isSeated() {
        return this.vrPlayerState != null && this.vrPlayerState.seated();
    }
}
