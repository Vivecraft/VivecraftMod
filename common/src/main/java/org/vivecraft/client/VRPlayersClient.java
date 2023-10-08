package org.vivecraft.client;

import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.common.network.VRPlayerState;

import java.util.*;

import static org.joml.Math.*;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.*;

public class VRPlayersClient {
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

    public boolean isVRPlayer(Player player) {
        return this.vivePlayers.containsKey(player.getUUID());
    }

    public void disableVR(UUID player) {
        this.vivePlayers.remove(player);
        this.vivePlayersLast.remove(player);
        this.vivePlayersReceived.remove(player);
    }

    public void Update(UUID uuid, VRPlayerState vrPlayerState, float worldScale, float heightScale, boolean localPlayer) {
        if (localPlayer || !mc.player.getUUID().equals(uuid)) {
            RotInfo rotInfo = new RotInfo();

            if (this.donors.containsKey(uuid)) {
                rotInfo.hmd = this.donors.get(uuid);
            }

            rotInfo.reverse = vrPlayerState.reverseHands();
            rotInfo.seated = vrPlayerState.seated();
            vrPlayerState.controller1().orientation().transformUnit(rotInfo.leftArmRot.set(0.0F, 0.0F, -1.0F));
            vrPlayerState.controller0().orientation().transformUnit(rotInfo.rightArmRot.set(0.0F, 0.0F, -1.0F));
            vrPlayerState.hmd().orientation().transformUnit(rotInfo.headRot.set(0.0F, 0.0F, -1.0F));
            convertToVector3f(vrPlayerState.hmd().position(), rotInfo.Headpos);
            convertToVector3f(vrPlayerState.controller1().position(), rotInfo.leftArmPos);
            convertToVector3f(vrPlayerState.controller0().position(), rotInfo.rightArmPos);
            rotInfo.leftArmQuat.set(vrPlayerState.controller1().orientation());
            rotInfo.rightArmQuat.set(vrPlayerState.controller0().orientation());
            rotInfo.headQuat.set(vrPlayerState.hmd().orientation());
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

    public void Update(UUID uuid, VRPlayerState vrPlayerState, float worldscale, float heightscale) {
        this.Update(uuid, vrPlayerState, worldscale, heightscale, false);
    }

    public void tick() {
        this.vivePlayersLast.putAll(this.vivePlayers);

        this.vivePlayers.putAll(this.vivePlayersReceived);

        if (mc.level != null) {
            Iterator<UUID> iterator = this.vivePlayers.keySet().iterator();

            while (iterator.hasNext()) {
                UUID uuid = iterator.next();

                if (mc.level.getPlayerByUUID(uuid) == null) {
                    iterator.remove();
                    this.vivePlayersLast.remove(uuid);
                    this.vivePlayersReceived.remove(uuid);
                }
            }

            if (!mc.isPaused()) {
                for (Player player : mc.level.players()) {
                    if (this.donors.getOrDefault(player.getUUID(), 0) > 3 && this.rand.nextInt(10) < 4) {
                        RotInfo playermodelcontroller$rotinfo = this.vivePlayers.get(player.getUUID());
                        Vector3f vec3 = new Vector3f();

                        if (playermodelcontroller$rotinfo != null) {
                            playermodelcontroller$rotinfo.leftArmPos.sub(playermodelcontroller$rotinfo.rightArmPos, vec3).rotateY((-(float) PI / 2F));

                            if (playermodelcontroller$rotinfo.reverse) {
                                vec3.negate();
                            } else if (playermodelcontroller$rotinfo.seated) {
                                vec3.set(playermodelcontroller$rotinfo.rightArmRot);
                            }

                            if (vec3.length() < (double) 1.0E-4F) {
                                vec3.set(playermodelcontroller$rotinfo.headRot);
                            }
                        } else {
                            convertToVector3f(player.getLookAngle(), vec3);
                        }

                        vec3.mul(0.1F);
                        Vector3f vec31 = new Vector3f();
                        if (playermodelcontroller$rotinfo != null && player == mc.player) {
                            var player_pos = player.position();
                            playermodelcontroller$rotinfo.Headpos.add(
                                (float) player_pos.x(), (float) player_pos.y(), (float) player_pos.z(), vec31
                            );
                        } else {
                            convertToVector3f(player.getEyePosition(1.0F), vec31);
                        }
                        Particle particle = mc.particleEngine.createParticle(ParticleTypes.FIREWORK, vec31.x + (player.isShiftKeyDown() ? -vec3.x * 3.0D : 0.0D) + ((double) this.rand.nextFloat() - 0.5D) * (double) 0.02F, vec31.y - (double) (player.isShiftKeyDown() ? 1.0F : 0.8F) + ((double) this.rand.nextFloat() - 0.5D) * (double) 0.02F, vec31.z + (player.isShiftKeyDown() ? -vec3.z * 3.0D : 0.0D) + ((double) this.rand.nextFloat() - 0.5D) * (double) 0.02F, -vec3.x + ((double) this.rand.nextFloat() - 0.5D) * (double) 0.01F, ((double) this.rand.nextFloat() - (double) 0.05F) * (double) 0.05F, -vec3.z + ((double) this.rand.nextFloat() - 0.5D) * (double) 0.01F);

                        if (particle != null) {
                            particle.setColor(
                                0.5F + this.rand.nextFloat() / 2.0F,
                                0.5F + this.rand.nextFloat() / 2.0F,
                                0.5F + this.rand.nextFloat() / 2.0F
                            );
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
            uuid = mc.player.getUUID();
        }

        RotInfo playermodelcontroller$rotinfo = this.vivePlayers.get(uuid);

        if (playermodelcontroller$rotinfo != null && this.vivePlayersLast.containsKey(uuid)) {
            RotInfo playermodelcontroller$rotinfo1 = this.vivePlayersLast.get(uuid);
            RotInfo playermodelcontroller$rotinfo2 = new RotInfo();
            float f = mc.getFrameTime();
            playermodelcontroller$rotinfo2.reverse = playermodelcontroller$rotinfo.reverse;
            playermodelcontroller$rotinfo2.seated = playermodelcontroller$rotinfo.seated;
            playermodelcontroller$rotinfo2.hmd = playermodelcontroller$rotinfo.hmd;
            playermodelcontroller$rotinfo1.leftArmPos.lerp(
                playermodelcontroller$rotinfo.leftArmPos, f, playermodelcontroller$rotinfo2.leftArmPos
            );
            playermodelcontroller$rotinfo1.rightArmPos.lerp(
                playermodelcontroller$rotinfo.rightArmPos, f, playermodelcontroller$rotinfo2.rightArmPos
            );
            playermodelcontroller$rotinfo1.Headpos.lerp(
                playermodelcontroller$rotinfo.Headpos, f, playermodelcontroller$rotinfo2.Headpos
            );
            playermodelcontroller$rotinfo2.leftArmQuat.set(playermodelcontroller$rotinfo.leftArmQuat);
            playermodelcontroller$rotinfo2.rightArmQuat.set(playermodelcontroller$rotinfo.rightArmQuat);
            playermodelcontroller$rotinfo2.headQuat.set(playermodelcontroller$rotinfo.headQuat);
            playermodelcontroller$rotinfo1.leftArmRot.lerp(
                playermodelcontroller$rotinfo2.leftArmQuat.transformUnit(new Vector3f(forward)),
                f,
                playermodelcontroller$rotinfo2.leftArmRot
            );
            playermodelcontroller$rotinfo1.rightArmRot.lerp(
                playermodelcontroller$rotinfo2.rightArmQuat.transformUnit(new Vector3f(forward)),
                f,
                playermodelcontroller$rotinfo2.rightArmRot
            );
            playermodelcontroller$rotinfo1.headRot.lerp(
                playermodelcontroller$rotinfo2.headQuat.transformUnit(new Vector3f(forward)),
                f,
                playermodelcontroller$rotinfo2.headRot
            );
            playermodelcontroller$rotinfo2.heightScale = playermodelcontroller$rotinfo.heightScale;
            playermodelcontroller$rotinfo2.worldScale = playermodelcontroller$rotinfo.worldScale;
            return playermodelcontroller$rotinfo2;
        } else {
            return playermodelcontroller$rotinfo;
        }
    }

    public static RotInfo getMainPlayerRotInfo(VRData data) {
        RotInfo playermodelcontroller$rotinfo = new RotInfo();
        playermodelcontroller$rotinfo.headQuat.setFromNormalized(data.hmd.getMatrix());
        playermodelcontroller$rotinfo.leftArmQuat.setFromNormalized(data.getController(1).getMatrix());
        playermodelcontroller$rotinfo.rightArmQuat.setFromNormalized(data.getController(0).getMatrix());
        playermodelcontroller$rotinfo.seated = dh.vrSettings.seated;
        data.getController(1).getPosition(playermodelcontroller$rotinfo.leftArmPos);
        data.getController(0).getPosition(playermodelcontroller$rotinfo.rightArmPos);
        data.hmd.getPosition(playermodelcontroller$rotinfo.Headpos);
        return playermodelcontroller$rotinfo;
    }

    public boolean isTracked(UUID uuid) {
        this.debug = false;
        return this.debug || this.vivePlayers.containsKey(uuid);
    }

    public static float getFacingYaw(RotInfo rotInfo) {
        var vec3 = getOrientVec(rotInfo.headQuat);
        return (float) toDegrees(atan2(vec3.x, vec3.z));
    }

    public static Vector3f getOrientVec(Quaternionf quat) {
        return new Vector3f(up).cross(
            quat.transformUnit(new Vector3f(forward)).cross(
                quat.transformUnit(new Vector3f(up))).normalize()
        ).normalize();
    }

    public static class RotInfo {
        public boolean seated;
        public boolean reverse;
        public int hmd = 0;
        public Quaternionf leftArmQuat = new Quaternionf();
        public Quaternionf rightArmQuat = new Quaternionf();
        public Quaternionf headQuat = new Quaternionf();
        public Vector3f leftArmRot = new Vector3f();
        public Vector3f rightArmRot = new Vector3f();
        public Vector3f headRot = new Vector3f();
        public Vector3f leftArmPos = new Vector3f();
        public Vector3f rightArmPos = new Vector3f();
        public Vector3f Headpos = new Vector3f();
        public float worldScale;
        public float heightScale;

        public double getBodyYawRadians() {
            Vector3f vec3;

            if (this.seated) {
                vec3 = new Vector3f(this.rightArmRot);
            } else {
                vec3 = new Vector3f(this.leftArmPos).sub(this.rightArmPos).rotateY((-(float) PI / 2.0F));
                if (this.reverse) {
                    vec3.negate();
                }
            }

            vec3.lerp(this.headRot, 0.5F);
            return atan2(-vec3.x, vec3.z);
        }
    }
}
