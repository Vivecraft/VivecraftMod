package org.vivecraft.api;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.reflection.MCReflection;

public class AimFixHandler extends ChannelInboundHandlerAdapter
{
    private final Connection netManager;

    public AimFixHandler(Connection netManager)
    {
        this.netManager = netManager;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        ServerPlayer serverplayer = ((ServerGamePacketListenerImpl)this.netManager.getPacketListener()).player;
        boolean flag = msg instanceof ServerboundUseItemPacket || msg instanceof ServerboundUseItemOnPacket || msg instanceof ServerboundPlayerActionPacket;

        if (NetworkHelper.isVive(serverplayer) && flag && serverplayer.getServer() != null)
        {
            serverplayer.getServer().submit(() ->
            {
                Vec3 vec3 = serverplayer.position();
                Vec3 vec31 = new Vec3(serverplayer.xo, serverplayer.yo, serverplayer.zo);
                float f = serverplayer.getXRot();
                float f1 = serverplayer.getYRot();
                float f2 = serverplayer.yHeadRot;
                float f3 = serverplayer.xRotO;
                float f4 = serverplayer.yRotO;
                float f5 = serverplayer.yHeadRotO;
                float f6 = serverplayer.getEyeHeight();
                ServerVivePlayer serverviveplayer = null;

                if (NetworkHelper.isVive(serverplayer))
                {
                    serverviveplayer = NetworkHelper.vivePlayers.get(serverplayer.getGameProfile().getId());
                    Vec3 vec32 = serverviveplayer.getControllerPos(0, serverplayer);
                    Vec3 vec33 = serverviveplayer.getControllerDir(0);
                    serverplayer.setPosRaw(vec32.x, vec32.y, vec32.z);
                    serverplayer.xo = vec32.x;
                    serverplayer.yo = vec32.y;
                    serverplayer.zo = vec32.z;
                    serverplayer.setXRot((float)Math.toDegrees(Math.asin(-vec33.y)));
                    serverplayer.setYRot((float)Math.toDegrees(Math.atan2(-vec33.x, vec33.z)));
                    serverplayer.xRotO = serverplayer.getXRot();
                    serverplayer.yRotO = serverplayer.yHeadRotO = serverplayer.yHeadRot = serverplayer.getYRot();
                    MCReflection.Entity_eyeHeight.set(serverplayer, 0);
                    serverviveplayer.offset = vec3.subtract(vec32);
                }

                try {
                    if (this.netManager.isConnected())
                    {
                        try
                        {
                            ((Packet)msg).handle(this.netManager.getPacketListener());
                        }
                        catch (RunningOnDifferentThreadException runningondifferentthreadexception)
                        {
                        }
                    }
                }
                finally {
                    ReferenceCountUtil.release(msg);
                }

                serverplayer.setPosRaw(vec3.x, vec3.y, vec3.z);
                serverplayer.xo = vec31.x;
                serverplayer.yo = vec31.y;
                serverplayer.zo = vec31.z;
                serverplayer.setXRot(f);
                serverplayer.setYRot(f1);
                serverplayer.yHeadRot = f2;
                serverplayer.xRotO = f3;
                serverplayer.yRotO = f4;
                serverplayer.yHeadRotO = f5;
                MCReflection.Entity_eyeHeight.set(serverplayer, f6);

                if (serverviveplayer != null)
                {
                    serverviveplayer.offset = new Vec3(0.0D, 0.0D, 0.0D);
                }
            });
        }
        else
        {
            ctx.fireChannelRead(msg);
        }
    }
}
