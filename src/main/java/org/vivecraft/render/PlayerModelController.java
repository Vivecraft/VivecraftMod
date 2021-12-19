package org.vivecraft.render;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.utils.Utils;
import org.vivecraft.utils.math.Quaternion;
import org.vivecraft.utils.math.Vector3;

public class PlayerModelController
{
    private final Minecraft mc;
    private Map<UUID, PlayerModelController.RotInfo> vivePlayers = new HashMap<>();
    private Map<UUID, PlayerModelController.RotInfo> vivePlayersLast = new HashMap<>();
    private Map<UUID, PlayerModelController.RotInfo> vivePlayersReceived = Collections.synchronizedMap(new HashMap<>());
    private Map<UUID, Integer> donors = new HashMap<>();
    static PlayerModelController instance;
    private Random rand = new Random();
    public boolean debug = false;

    public static PlayerModelController getInstance()
    {
        if (instance == null)
        {
            instance = new PlayerModelController();
        }

        return instance;
    }

    private PlayerModelController()
    {
        this.mc = Minecraft.getInstance();
    }

    public void Update(UUID uuid, byte[] hmddata, byte[] c0data, byte[] c1data, float worldscale, float heightscale, boolean localPlayer)
    {
        if (localPlayer || !this.mc.player.getUUID().equals(uuid))
        {
            Vec3 vec3 = null;
            Vec3 vec31 = null;
            Vec3 vec32 = null;
            Quaternion quaternion = null;
            Quaternion quaternion1 = null;
            Quaternion quaternion2 = null;
            boolean flag = false;
            boolean flag1 = false;

            for (int i = 0; i <= 2; ++i)
            {
                try
                {
                    byte[] abyte = null;

                    switch (i)
                    {
                        case 0:
                            abyte = hmddata;
                            break;

                        case 1:
                            abyte = c0data;
                            break;

                        case 2:
                            abyte = c1data;
                    }

                    ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(abyte);
                    DataInputStream datainputstream = new DataInputStream(bytearrayinputstream);
                    boolean flag2 = false;

                    if (abyte.length >= 29)
                    {
                        flag2 = datainputstream.readBoolean();
                    }

                    float f = datainputstream.readFloat();
                    float f1 = datainputstream.readFloat();
                    float f2 = datainputstream.readFloat();
                    float f3 = datainputstream.readFloat();
                    float f4 = datainputstream.readFloat();
                    float f5 = datainputstream.readFloat();
                    float f6 = datainputstream.readFloat();
                    datainputstream.close();

                    switch (i)
                    {
                        case 0:
                            if (flag2)
                            {
                                flag = true;
                            }

                            vec3 = new Vec3((double)f, (double)f1, (double)f2);
                            quaternion = new Quaternion(f3, f4, f5, f6);
                            break;

                        case 1:
                            if (flag2)
                            {
                                flag1 = true;
                            }

                            vec31 = new Vec3((double)f, (double)f1, (double)f2);
                            quaternion1 = new Quaternion(f3, f4, f5, f6);
                            break;

                        case 2:
                            if (flag2)
                            {
                                flag1 = true;
                            }

                            vec32 = new Vec3((double)f, (double)f1, (double)f2);
                            quaternion2 = new Quaternion(f3, f4, f5, f6);
                    }
                }
                catch (IOException ioexception)
                {
                }
            }

            new Vector3(0.0F, -0.0F, 0.0F);
            Vector3 vector3 = new Vector3(0.0F, 0.0F, -1.0F);
            Vector3 vector31 = quaternion.multiply(vector3);
            Vector3 vector32 = quaternion1.multiply(vector3);
            Vector3 vector33 = quaternion2.multiply(vector3);
            PlayerModelController.RotInfo playermodelcontroller$rotinfo = new PlayerModelController.RotInfo();
            playermodelcontroller$rotinfo.reverse = flag1;
            playermodelcontroller$rotinfo.seated = flag;

            if (this.donors.containsKey(uuid))
            {
                playermodelcontroller$rotinfo.hmd = this.donors.get(uuid);
            }

            playermodelcontroller$rotinfo.leftArmRot = new Vec3((double)vector33.getX(), (double)vector33.getY(), (double)vector33.getZ());
            playermodelcontroller$rotinfo.rightArmRot = new Vec3((double)vector32.getX(), (double)vector32.getY(), (double)vector32.getZ());
            playermodelcontroller$rotinfo.headRot = new Vec3((double)vector31.getX(), (double)vector31.getY(), (double)vector31.getZ());
            playermodelcontroller$rotinfo.Headpos = vec3;
            playermodelcontroller$rotinfo.leftArmPos = vec32;
            playermodelcontroller$rotinfo.rightArmPos = vec31;
            playermodelcontroller$rotinfo.leftArmQuat = quaternion2;
            playermodelcontroller$rotinfo.rightArmQuat = quaternion1;
            playermodelcontroller$rotinfo.headQuat = quaternion;
            playermodelcontroller$rotinfo.worldScale = worldscale;

            if (heightscale < 0.5F)
            {
                heightscale = 0.5F;
            }

            if (heightscale > 1.5F)
            {
                heightscale = 1.5F;
            }

            playermodelcontroller$rotinfo.heightScale = heightscale;

            if (playermodelcontroller$rotinfo.seated)
            {
                playermodelcontroller$rotinfo.heightScale = 1.0F;
            }

            this.vivePlayersReceived.put(uuid, playermodelcontroller$rotinfo);
        }
    }

    public void Update(UUID uuid, byte[] hmddata, byte[] c0data, byte[] c1data, float worldscale, float heightscale)
    {
        this.Update(uuid, hmddata, c0data, c1data, worldscale, heightscale, false);
    }

    public void tick()
    {
        for (Entry<UUID, PlayerModelController.RotInfo> entry : this.vivePlayers.entrySet())
        {
            this.vivePlayersLast.put(entry.getKey(), entry.getValue());
        }

        for (Entry<UUID, PlayerModelController.RotInfo> entry1 : this.vivePlayersReceived.entrySet())
        {
            this.vivePlayers.put(entry1.getKey(), entry1.getValue());
        }

        Level level = Minecraft.getInstance().level;

        if (level != null)
        {
            Iterator<UUID> iterator = this.vivePlayers.keySet().iterator();

            while (iterator.hasNext())
            {
                UUID uuid = iterator.next();

                if (level.getPlayerByUUID(uuid) == null)
                {
                    iterator.remove();
                    this.vivePlayersLast.remove(uuid);
                    this.vivePlayersReceived.remove(uuid);
                }
            }

            if (!this.mc.isPaused())
            {
                for (Player player : level.players())
                {
                    if (this.donors.getOrDefault(player.getUUID(), 0) > 3 && this.rand.nextInt(10) < 4)
                    {
                        PlayerModelController.RotInfo playermodelcontroller$rotinfo = this.vivePlayers.get(player.getUUID());
                        Vec3 vec3 = player.getLookAngle();

                        if (playermodelcontroller$rotinfo != null)
                        {
                            vec3 = playermodelcontroller$rotinfo.leftArmPos.subtract(playermodelcontroller$rotinfo.rightArmPos).yRot((-(float)Math.PI / 2F));

                            if (playermodelcontroller$rotinfo.reverse)
                            {
                                vec3 = vec3.scale(-1.0D);
                            }
                            else if (playermodelcontroller$rotinfo.seated)
                            {
                                vec3 = playermodelcontroller$rotinfo.rightArmRot;
                            }

                            if (vec3.length() < (double)1.0E-4F)
                            {
                                vec3 = playermodelcontroller$rotinfo.headRot;
                            }
                        }

                        vec3 = vec3.scale((double)0.1F);
                        Vec3 vec31 = playermodelcontroller$rotinfo != null && player == this.mc.player ? playermodelcontroller$rotinfo.Headpos : player.getEyePosition(1.0F);
                        Particle particle = this.mc.particleEngine.createParticle(ParticleTypes.FIREWORK, vec31.x + (player.isShiftKeyDown() ? -vec3.x * 3.0D : 0.0D) + ((double)this.rand.nextFloat() - 0.5D) * (double)0.02F, vec31.y - (double)(player.isShiftKeyDown() ? 1.0F : 0.8F) + ((double)this.rand.nextFloat() - 0.5D) * (double)0.02F, vec31.z + (player.isShiftKeyDown() ? -vec3.z * 3.0D : 0.0D) + ((double)this.rand.nextFloat() - 0.5D) * (double)0.02F, -vec3.x + ((double)this.rand.nextFloat() - 0.5D) * (double)0.01F, ((double)this.rand.nextFloat() - (double)0.05F) * (double)0.05F, -vec3.z + ((double)this.rand.nextFloat() - 0.5D) * (double)0.01F);

                        if (particle != null)
                        {
                            particle.setColor(0.5F + this.rand.nextFloat() / 2.0F, 0.5F + this.rand.nextFloat() / 2.0F, 0.5F + this.rand.nextFloat() / 2.0F);
                        }
                    }
                }
            }
        }
    }

    public void setHMD(UUID uuid, int level)
    {
        this.donors.put(uuid, level);
    }

    public boolean HMDCHecked(UUID uuid)
    {
        return this.donors.containsKey(uuid);
    }

    public PlayerModelController.RotInfo getRotationsForPlayer(UUID uuid)
    {
        if (this.debug)
        {
            uuid = this.mc.player.getUUID();
        }

        PlayerModelController.RotInfo playermodelcontroller$rotinfo = this.vivePlayers.get(uuid);

        if (playermodelcontroller$rotinfo != null && this.vivePlayersLast.containsKey(uuid))
        {
            PlayerModelController.RotInfo playermodelcontroller$rotinfo1 = this.vivePlayersLast.get(uuid);
            PlayerModelController.RotInfo playermodelcontroller$rotinfo2 = new PlayerModelController.RotInfo();
            float f = Minecraft.getInstance().getFrameTime();
            playermodelcontroller$rotinfo2.reverse = playermodelcontroller$rotinfo.reverse;
            playermodelcontroller$rotinfo2.seated = playermodelcontroller$rotinfo.seated;
            playermodelcontroller$rotinfo2.hmd = playermodelcontroller$rotinfo.hmd;
            playermodelcontroller$rotinfo2.leftArmPos = Utils.vecLerp(playermodelcontroller$rotinfo1.leftArmPos, playermodelcontroller$rotinfo.leftArmPos, (double)f);
            playermodelcontroller$rotinfo2.rightArmPos = Utils.vecLerp(playermodelcontroller$rotinfo1.rightArmPos, playermodelcontroller$rotinfo.rightArmPos, (double)f);
            playermodelcontroller$rotinfo2.Headpos = Utils.vecLerp(playermodelcontroller$rotinfo1.Headpos, playermodelcontroller$rotinfo.Headpos, (double)f);
            playermodelcontroller$rotinfo2.leftArmQuat = playermodelcontroller$rotinfo.leftArmQuat;
            playermodelcontroller$rotinfo2.rightArmQuat = playermodelcontroller$rotinfo.rightArmQuat;
            playermodelcontroller$rotinfo2.headQuat = playermodelcontroller$rotinfo.headQuat;
            Vector3 vector3 = new Vector3(0.0F, 0.0F, -1.0F);
            playermodelcontroller$rotinfo2.leftArmRot = Utils.vecLerp(playermodelcontroller$rotinfo1.leftArmRot, Utils.convertToVector3d(playermodelcontroller$rotinfo2.leftArmQuat.multiply(vector3)), (double)f);
            playermodelcontroller$rotinfo2.rightArmRot = Utils.vecLerp(playermodelcontroller$rotinfo1.rightArmRot, Utils.convertToVector3d(playermodelcontroller$rotinfo2.rightArmQuat.multiply(vector3)), (double)f);
            playermodelcontroller$rotinfo2.headRot = Utils.vecLerp(playermodelcontroller$rotinfo1.headRot, Utils.convertToVector3d(playermodelcontroller$rotinfo2.headQuat.multiply(vector3)), (double)f);
            playermodelcontroller$rotinfo2.heightScale = playermodelcontroller$rotinfo.heightScale;
            playermodelcontroller$rotinfo2.worldScale = playermodelcontroller$rotinfo.worldScale;
            return playermodelcontroller$rotinfo2;
        }
        else
        {
            return playermodelcontroller$rotinfo;
        }
    }

    public boolean isTracked(UUID uuid)
    {
        this.debug = false;
        return this.debug ? true : this.vivePlayers.containsKey(uuid);
    }

    public static float getFacingYaw(PlayerModelController.RotInfo rotInfo)
    {
        Vec3 vec3 = getOrientVec(rotInfo.headQuat);
        return (float)Math.toDegrees(Math.atan2(vec3.x, vec3.z));
    }

    public static Vec3 getOrientVec(Quaternion quat)
    {
        Vec3 vec3 = quat.multiply(new Vec3(0.0D, 0.0D, -1.0D)).cross(quat.multiply(new Vec3(0.0D, 1.0D, 0.0D))).normalize();
        return (new Vec3(0.0D, 1.0D, 0.0D)).cross(vec3).normalize();
    }

    public static class RotInfo
    {
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

        public double getBodyYawRadians()
        {
            Vec3 vec3 = this.leftArmPos.subtract(this.rightArmPos).yRot((-(float)Math.PI / 2F));

            if (this.reverse)
            {
                vec3 = vec3.scale(-1.0D);
            }

            if (this.seated)
            {
                vec3 = this.rightArmRot;
            }

            Vec3 vec31 = Utils.vecLerp(vec3, this.headRot, 0.5D);
            return Math.atan2(-vec31.x, vec31.z);
        }
    }
}
