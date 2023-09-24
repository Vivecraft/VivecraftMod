package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.phys.Vec3;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

import static org.joml.Math.*;

public class BackpackTracker extends Tracker
{
    public boolean[] wasIn = new boolean[2];
    public int previousSlot = 0;
    private final Vec3 down = new Vec3(0.0D, -1.0D, 0.0D);

    public boolean isActive()
    {

        if (dh.vrSettings.seated)
        {
            return false;
        }
        else if (!dh.vrSettings.backpackSwitching)
        {
            return false;
        }
        else if (mc.player == null)
        {
            return false;
        }
        else if (mc.gameMode == null)
        {
            return false;
        }
        else if (!mc.player.isAlive())
        {
            return false;
        }
        else if (mc.player.isSleeping())
        {
            return false;
        }
        else
        {
            return !dh.bowTracker.isDrawing;
        }
    }

    public void doProcess()
    {
        Vec3 vec3 = dh.vrPlayer.vrdata_room_pre.getHeadRear();

        for (int i = 0; i < 2; ++i)
        {
            Vec3 vec31 = dh.vrPlayer.vrdata_room_pre.getController(i).getPosition();
            Vec3 vec32 = dh.vrPlayer.vrdata_room_pre.getHand(i).getDirection();
            Vec3 vec33 = dh.vrPlayer.vrdata_room_pre.hmd.getDirection();
            Vec3 vec34 = vec3.subtract(vec31);
            double d0 = vec32.dot(this.down);
            double d1 = vec34.dot(vec33);
            boolean flag = abs(vec3.y - vec31.y) < 0.25D;
            boolean flag1 = d1 > 0.0D && vec34.length() > 0.05D;
            boolean flag2 = d0 > 0.6D;
            boolean flag3 = d1 < 0.0D && vec34.length() > 0.25D;
            boolean flag4 = d0 < 0.0D;
            boolean flag5 = flag && flag1 && flag2;

            if (flag5)
            {
                if (!this.wasIn[i])
                {
                    if (i == 0)
                    {
                        if (!dh.climbTracker.isGrabbingLadder() || !dh.climbTracker.isClaws(mc.player.getMainHandItem()))
                        {
                            if (mc.player.getInventory().selected != 0)
                            {
                                this.previousSlot = mc.player.getInventory().selected;
                                mc.player.getInventory().selected = 0;
                            }
                            else
                            {
                                mc.player.getInventory().selected = this.previousSlot;
                                this.previousSlot = 0;
                            }
                        }
                    }
                    else if (!dh.climbTracker.isGrabbingLadder() || !dh.climbTracker.isClaws(mc.player.getOffhandItem()))
                    {
                        if (dh.vrSettings.physicalGuiEnabled)
                        {
                            //minecraft.physicalGuiManager.toggleInventoryBag();
                        }
                        else
                        {
                            mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
                        }
                    }

                    dh.vr.triggerHapticPulse(i, 1500);
                    this.wasIn[i] = true;
                }
            }
            else if (flag3 || flag4)
            {
                this.wasIn[i] = false;
            }
        }
    }
}
