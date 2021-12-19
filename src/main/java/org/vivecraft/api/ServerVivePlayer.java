package org.vivecraft.api;

import io.netty.buffer.Unpooled;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.utils.math.Quaternion;
import org.vivecraft.utils.math.Vector3;

public class ServerVivePlayer
{
    public byte[] hmdData;
    public byte[] controller0data;
    public byte[] controller1data;
    public byte[] draw;
    public float worldScale = 1.0F;
    public float heightscale = 1.0F;
    public byte activeHand = 0;
    public boolean crawling;
    boolean isTeleportMode;
    boolean isReverseHands;
    boolean isVR = true;
    public Vec3 offset = new Vec3(0.0D, 0.0D, 0.0D);
    public ServerPlayer player;
    final Vector3 forward = new Vector3(0.0F, 0.0F, -1.0F);

    public ServerVivePlayer(ServerPlayer player)
    {
        this.player = player;
    }

    public float getDraw()
    {
        try
        {
            if (this.draw != null)
            {
                ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(this.draw);
                DataInputStream datainputstream = new DataInputStream(bytearrayinputstream);
                float f = datainputstream.readFloat();
                datainputstream.close();
                return f;
            }
        }
        catch (IOException ioexception)
        {
        }

        return 0.0F;
    }

    public Vec3 getControllerVectorCustom(int controller, Vector3 direction)
    {
        byte[] abyte = this.controller0data;

        if (controller == 1)
        {
            abyte = this.controller1data;
        }

        if (this.isSeated())
        {
            controller = 0;
        }

        if (abyte != null)
        {
            ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(abyte);
            DataInputStream datainputstream = new DataInputStream(bytearrayinputstream);

            try
            {
                boolean flag = datainputstream.readBoolean();
                float f = datainputstream.readFloat();
                float f1 = datainputstream.readFloat();
                float f2 = datainputstream.readFloat();
                float f3 = datainputstream.readFloat();
                float f4 = datainputstream.readFloat();
                float f5 = datainputstream.readFloat();
                float f6 = datainputstream.readFloat();
                Quaternion quaternion = new Quaternion(f3, f4, f5, f6);
                Vector3 vector3 = quaternion.multiply(direction);
                datainputstream.close();
                return new Vec3((double)vector3.getX(), (double)vector3.getY(), (double)vector3.getZ());
            }
            catch (IOException ioexception)
            {
                return this.player.getLookAngle();
            }
        }
        else
        {
            return this.player.getLookAngle();
        }
    }

    public Vec3 getControllerDir(int controller)
    {
        return this.getControllerVectorCustom(controller, this.forward);
    }

    public Vec3 getHMDDir()
    {
        try
        {
            if (this.hmdData != null)
            {
                ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(this.hmdData);
                DataInputStream datainputstream = new DataInputStream(bytearrayinputstream);
                boolean flag = datainputstream.readBoolean();
                float f = datainputstream.readFloat();
                float f1 = datainputstream.readFloat();
                float f2 = datainputstream.readFloat();
                float f3 = datainputstream.readFloat();
                float f4 = datainputstream.readFloat();
                float f5 = datainputstream.readFloat();
                float f6 = datainputstream.readFloat();
                Quaternion quaternion = new Quaternion(f3, f4, f5, f6);
                Vector3 vector3 = quaternion.multiply(this.forward);
                datainputstream.close();
                return new Vec3((double)vector3.getX(), (double)vector3.getY(), (double)vector3.getZ());
            }
        }
        catch (IOException ioexception)
        {
        }

        return this.player.getLookAngle();
    }

    public Vec3 getHMDPos(Player player)
    {
        try
        {
            if (this.hmdData != null)
            {
                ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(this.hmdData);
                DataInputStream datainputstream = new DataInputStream(bytearrayinputstream);
                boolean flag = datainputstream.readBoolean();
                float f = datainputstream.readFloat();
                float f1 = datainputstream.readFloat();
                float f2 = datainputstream.readFloat();
                datainputstream.close();
                return (new Vec3((double)f, (double)f1, (double)f2)).add(player.position()).add(this.offset);
            }
        }
        catch (IOException ioexception)
        {
        }

        return player.position().add(0.0D, 1.62D, 0.0D);
    }

    public Vec3 getControllerPos(int c, Player player)
    {
        try
        {
            if (this.controller0data != null && this.controller0data != null)
            {
                ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(c == 0 ? this.controller0data : this.controller1data);
                DataInputStream datainputstream = new DataInputStream(bytearrayinputstream);
                boolean flag = datainputstream.readBoolean();
                float f = datainputstream.readFloat();
                float f1 = datainputstream.readFloat();
                float f2 = datainputstream.readFloat();
                datainputstream.close();

                if (this.isSeated())
                {
                    Vec3 vec3 = this.getHMDDir();
                    vec3 = vec3.yRot((float)Math.toRadians(c == 0 ? -35.0D : 35.0D));
                    vec3 = new Vec3(vec3.x, 0.0D, vec3.z);
                    vec3 = vec3.normalize();
                    Vec3 vec31 = this.getHMDPos(player).add(vec3.x * 0.3D * (double)this.worldScale, -0.4D * (double)this.worldScale, vec3.z * 0.3D * (double)this.worldScale);
                    f = (float)vec31.x;
                    f1 = (float)vec31.y;
                    f2 = (float)vec31.z;
                    return new Vec3((double)f, (double)f1, (double)f2);
                }

                return (new Vec3((double)f, (double)f1, (double)f2)).add(player.position()).add(this.offset);
            }
        }
        catch (IOException ioexception)
        {
        }

        return player.position().add(0.0D, 1.62D, 0.0D);
    }

    public boolean isVR()
    {
        return this.isVR;
    }

    public void setVR(boolean vr)
    {
        this.isVR = vr;
    }

    public boolean isSeated()
    {
        try
        {
            if (this.hmdData == null)
            {
                return false;
            }
            else if (this.hmdData.length < 29)
            {
                return false;
            }
            else
            {
                ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(this.hmdData);
                DataInputStream datainputstream = new DataInputStream(bytearrayinputstream);
                boolean flag = datainputstream.readBoolean();
                datainputstream.close();
                return flag;
            }
        }
        catch (IOException ioexception)
        {
            return false;
        }
    }

    public byte[] getUberPacket()
    {
        FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlybytebuf.writeLong(this.player.getUUID().getMostSignificantBits());
        friendlybytebuf.writeLong(this.player.getUUID().getLeastSignificantBits());
        friendlybytebuf.writeBytes(this.hmdData);
        friendlybytebuf.writeBytes(this.controller0data);
        friendlybytebuf.writeBytes(this.controller1data);
        friendlybytebuf.writeFloat(this.worldScale);
        friendlybytebuf.writeFloat(this.heightscale);
        return friendlybytebuf.array();
    }
}
