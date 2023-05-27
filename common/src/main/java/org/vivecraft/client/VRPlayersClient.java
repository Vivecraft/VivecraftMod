package org.vivecraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.common.network.VrPlayerState;
import org.vivecraft.common.utils.math.Quaternion;
import org.vivecraft.common.utils.math.Vector3;

import java.util.*;
import java.util.Map.Entry;

public class VRPlayersClient {
    private final Minecraft mc;
    private final Map<UUID, RotInfo> vivePlayers = new HashMap<>();
    private final Map<UUID, RotInfo> vivePlayersLast = new HashMap<>();
    private final Map<UUID, RotInfo> vivePlayersReceived = Collections.synchronizedMap(new HashMap<>());
    private final Map<UUID, Integer> donors = new HashMap<>();
    static VRPlayersClient instance;
    private final Random rand = new Random();
    public boolean debug = false;

    public static VRPlayersClient getInstance() {
        if (instance == null) {
            instance = new VRPlayersClient();
        }

        return instance;
    }

    public static void clear() {
        instance = null;
    }

    private VRPlayersClient() {
        this.mc = Minecraft.getInstance();
    }

    public boolean isVRPlayer(Player player) {
        return vivePlayers.containsKey(player.getUUID());
    }

    public void disableVR(UUID player) {
        this.vivePlayers.remove(player);
        this.vivePlayersLast.remove(player);
        this.vivePlayersReceived.remove(player);
    }

    public void Update(UUID uuid, VrPlayerState vrPlayerState, float worldScale, float heightScale, boolean localPlayer) {
        if (localPlayer || !this.mc.player.getUUID().equals(uuid)) {
            Vector3 forward = new Vector3(0.0F, 0.0F, -1.0F);
            Vector3 hmdDir = vrPlayerState.hmd().orientation().multiply(forward);
            Vector3 controller0Dir = vrPlayerState.controller0().orientation().multiply(forward);
            Vector3 controller1Dir = vrPlayerState.controller1().orientation().multiply(forward);
            var rotInfo = new RotInfo();
            rotInfo.reverse = vrPlayerState.reverseHands();
            rotInfo.seated = vrPlayerState.seated();

            if (this.donors.containsKey(uuid)) {
                rotInfo.hmd = this.donors.get(uuid);
            }

            rotInfo.leftArmRot = new Vec3(controller1Dir.getX(), controller1Dir.getY(), controller1Dir.getZ());
            rotInfo.rightArmRot = new Vec3(controller0Dir.getX(), controller0Dir.getY(), controller0Dir.getZ());
            rotInfo.headRot = new Vec3(hmdDir.getX(), hmdDir.getY(), hmdDir.getZ());
            rotInfo.Headpos = vrPlayerState.hmd().position();
            rotInfo.leftArmPos = vrPlayerState.controller1().position();
            rotInfo.rightArmPos = vrPlayerState.controller0().position();
            rotInfo.leftArmQuat = vrPlayerState.controller1().orientation();
            rotInfo.rightArmQuat = vrPlayerState.controller0().orientation();
            rotInfo.headQuat = vrPlayerState.hmd().orientation();
            rotInfo.worldScale = worldScale;

            if (heightScale < 0.5F) {
                heightScale = 0.5F;
            }

            if (heightScale > 1.5F) {
                heightScale = 1.5F;
            }

            rotInfo.heightScale = heightScale;

            if (rotInfo.seated) {
                rotInfo.heightScale = 1.0F;
            }

            this.vivePlayersReceived.put(uuid, rotInfo);
        }
    }

    public void Update(UUID uuid, VrPlayerState vrPlayerState, float worldscale, float heightscale) {
        this.Update(uuid, vrPlayerState, worldscale, heightscale, false);
    }

    public void tick() {
        for (Entry<UUID, RotInfo> entry : this.vivePlayers.entrySet()) {
            this.vivePlayersLast.put(entry.getKey(), entry.getValue());
        }

        for (Entry<UUID, RotInfo> entry1 : this.vivePlayersReceived.entrySet()) {
            this.vivePlayers.put(entry1.getKey(), entry1.getValue());
        }

        Level level = Minecraft.getInstance().level;

        if (level != null) {
            Iterator<UUID> iterator = this.vivePlayers.keySet().iterator();

            while (iterator.hasNext()) {
                UUID uuid = iterator.next();

                if (level.getPlayerByUUID(uuid) == null) {
                    iterator.remove();
                    this.vivePlayersLast.remove(uuid);
                    this.vivePlayersReceived.remove(uuid);
                }
            }

            if (!this.mc.isPaused()) {
                for (Player player : level.players()) {
                    if (this.donors.getOrDefault(player.getUUID(), 0) > 3 && this.rand.nextInt(10) < 4) {
                        RotInfo playermodelcontroller$rotinfo = this.vivePlayers.get(player.getUUID());
                        Vec3 vec3 = player.getLookAngle();

                        if (playermodelcontroller$rotinfo != null) {
                            vec3 = playermodelcontroller$rotinfo.leftArmPos.subtract(playermodelcontroller$rotinfo.rightArmPos).yRot((-(float) Math.PI / 2F));

                            if (playermodelcontroller$rotinfo.reverse) {
                                vec3 = vec3.scale(-1.0D);
                            } else if (playermodelcontroller$rotinfo.seated) {
                                vec3 = playermodelcontroller$rotinfo.rightArmRot;
                            }

                            if (vec3.length() < (double) 1.0E-4F) {
                                vec3 = playermodelcontroller$rotinfo.headRot;
                            }
                        }

                        vec3 = vec3.scale(0.1F);
                        Vec3 vec31 = playermodelcontroller$rotinfo != null && player == this.mc.player ? playermodelcontroller$rotinfo.Headpos : player.getEyePosition(1.0F);
                        Particle particle = this.mc.particleEngine.createParticle(ParticleTypes.FIREWORK, vec31.x + (player.isShiftKeyDown() ? -vec3.x * 3.0D : 0.0D) + ((double) this.rand.nextFloat() - 0.5D) * (double) 0.02F, vec31.y - (double) (player.isShiftKeyDown() ? 1.0F : 0.8F) + ((double) this.rand.nextFloat() - 0.5D) * (double) 0.02F, vec31.z + (player.isShiftKeyDown() ? -vec3.z * 3.0D : 0.0D) + ((double) this.rand.nextFloat() - 0.5D) * (double) 0.02F, -vec3.x + ((double) this.rand.nextFloat() - 0.5D) * (double) 0.01F, ((double) this.rand.nextFloat() - (double) 0.05F) * (double) 0.05F, -vec3.z + ((double) this.rand.nextFloat() - 0.5D) * (double) 0.01F);

                        if (particle != null) {
                            particle.setColor(0.5F + this.rand.nextFloat() / 2.0F, 0.5F + this.rand.nextFloat() / 2.0F, 0.5F + this.rand.nextFloat() / 2.0F);
                        }
                    }
                }
            }
        }
    }

    public void setHMD(UUID uuid, int level) {
        this.donors.put(uuid, level);
    }

    public boolean HMDCHecked(UUID uuid) {
        return this.donors.containsKey(uuid);
    }

    public RotInfo getRotationsForPlayer(UUID uuid) {
        if (this.debug) {
            uuid = this.mc.player.getUUID();
        }

        RotInfo playermodelcontroller$rotinfo = this.vivePlayers.get(uuid);

        if (playermodelcontroller$rotinfo != null && this.vivePlayersLast.containsKey(uuid)) {
            RotInfo playermodelcontroller$rotinfo1 = this.vivePlayersLast.get(uuid);
            RotInfo playermodelcontroller$rotinfo2 = new RotInfo();
            float f = Minecraft.getInstance().getFrameTime();
            playermodelcontroller$rotinfo2.reverse = playermodelcontroller$rotinfo.reverse;
            playermodelcontroller$rotinfo2.seated = playermodelcontroller$rotinfo.seated;
            playermodelcontroller$rotinfo2.hmd = playermodelcontroller$rotinfo.hmd;
            playermodelcontroller$rotinfo2.leftArmPos = Utils.vecLerp(playermodelcontroller$rotinfo1.leftArmPos, playermodelcontroller$rotinfo.leftArmPos, f);
            playermodelcontroller$rotinfo2.rightArmPos = Utils.vecLerp(playermodelcontroller$rotinfo1.rightArmPos, playermodelcontroller$rotinfo.rightArmPos, f);
            playermodelcontroller$rotinfo2.Headpos = Utils.vecLerp(playermodelcontroller$rotinfo1.Headpos, playermodelcontroller$rotinfo.Headpos, f);
            playermodelcontroller$rotinfo2.leftArmQuat = playermodelcontroller$rotinfo.leftArmQuat;
            playermodelcontroller$rotinfo2.rightArmQuat = playermodelcontroller$rotinfo.rightArmQuat;
            playermodelcontroller$rotinfo2.headQuat = playermodelcontroller$rotinfo.headQuat;
            Vector3 vector3 = new Vector3(0.0F, 0.0F, -1.0F);
            playermodelcontroller$rotinfo2.leftArmRot = Utils.vecLerp(playermodelcontroller$rotinfo1.leftArmRot, Utils.convertToVector3d(playermodelcontroller$rotinfo2.leftArmQuat.multiply(vector3)), f);
            playermodelcontroller$rotinfo2.rightArmRot = Utils.vecLerp(playermodelcontroller$rotinfo1.rightArmRot, Utils.convertToVector3d(playermodelcontroller$rotinfo2.rightArmQuat.multiply(vector3)), f);
            playermodelcontroller$rotinfo2.headRot = Utils.vecLerp(playermodelcontroller$rotinfo1.headRot, Utils.convertToVector3d(playermodelcontroller$rotinfo2.headQuat.multiply(vector3)), f);
            playermodelcontroller$rotinfo2.heightScale = playermodelcontroller$rotinfo.heightScale;
            playermodelcontroller$rotinfo2.worldScale = playermodelcontroller$rotinfo.worldScale;
            return playermodelcontroller$rotinfo2;
        } else {
            return playermodelcontroller$rotinfo;
        }
    }

    public static RotInfo getMainPlayerRotInfo(VRData data) {
        RotInfo playermodelcontroller$rotinfo = new RotInfo();
        Quaternion quaternion = new Quaternion(data.getController(1).getMatrix());
        Quaternion quaternion1 = new Quaternion(data.getController(0).getMatrix());
        Quaternion quaternion2 = new Quaternion(data.hmd.getMatrix());
        playermodelcontroller$rotinfo.headQuat = quaternion2;
        playermodelcontroller$rotinfo.leftArmQuat = quaternion;
        playermodelcontroller$rotinfo.rightArmQuat = quaternion1;
        playermodelcontroller$rotinfo.seated = ClientDataHolderVR.getInstance().vrSettings.seated;
        playermodelcontroller$rotinfo.leftArmPos = data.getController(1).getPosition();
        playermodelcontroller$rotinfo.rightArmPos = data.getController(0).getPosition();
        playermodelcontroller$rotinfo.Headpos = data.hmd.getPosition();
        return playermodelcontroller$rotinfo;
    }

    public boolean isTracked(UUID uuid) {
        this.debug = false;
        return this.debug ? true : this.vivePlayers.containsKey(uuid);
    }

    public static float getFacingYaw(RotInfo rotInfo) {
        Vec3 vec3 = getOrientVec(rotInfo.headQuat);
        return (float) Math.toDegrees(Math.atan2(vec3.x, vec3.z));
    }

    public static Vec3 getOrientVec(Quaternion quat) {
        Vec3 vec3 = quat.multiply(new Vec3(0.0D, 0.0D, -1.0D)).cross(quat.multiply(new Vec3(0.0D, 1.0D, 0.0D))).normalize();
        return (new Vec3(0.0D, 1.0D, 0.0D)).cross(vec3).normalize();
    }

    public static class RotInfo {
        public boolean seated;
        public boolean reverse;
        public int hmd = 0;
        public Quaternion leftArmQuat;
        public Quaternion rightArmQuat;
        public Quaternion headQuat;
        public Vec3 leftArmRot;
        public Vec3 rightArmRot;
        public Vec3 headRot;
        public Vec3 leftArmPos;
        public Vec3 rightArmPos;
        public Vec3 Headpos;
        public float worldScale;
        public float heightScale;

        public double getBodyYawRadians() {
            Vec3 vec3 = this.leftArmPos.subtract(this.rightArmPos).yRot((-(float) Math.PI / 2F));

            if (this.reverse) {
                vec3 = vec3.scale(-1.0D);
            }

            if (this.seated) {
                vec3 = this.rightArmRot;
            }

            Vec3 vec31 = Utils.vecLerp(vec3, this.headRot, 0.5D);
            return Math.atan2(-vec31.x, vec31.z);
        }
    }
}
