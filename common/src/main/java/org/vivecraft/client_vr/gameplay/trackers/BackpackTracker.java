package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.VRPlayer;

public class BackpackTracker extends Tracker {
    public boolean[] wasIn = new boolean[2];
    public int previousSlot = 0;
    private final Vec3 down = new Vec3(0.0D, -1.0D, 0.0D);

    public BackpackTracker(Minecraft mc, ClientDataHolderVR dh) {
        super(mc, dh);
    }

    public boolean isActive(LocalPlayer p) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();

        if (dataholder.vrSettings.seated) {
            return false;
        } else if (!dataholder.vrSettings.backpackSwitching) {
            return false;
        } else if (p == null) {
            return false;
        } else if (minecraft.gameMode == null) {
            return false;
        } else if (!p.isAlive()) {
            return false;
        } else if (p.isSleeping()) {
            return false;
        } else {
            return !dataholder.bowTracker.isDrawing;
        }
    }

    public void doProcess(LocalPlayer player) {
        VRPlayer vrplayer = this.dh.vrPlayer;
        Vec3 vec3 = vrplayer.vrdata_room_pre.getHeadRear();

        for (int i = 0; i < 2; ++i) {
            Vec3 vec31 = vrplayer.vrdata_room_pre.getController(i).getPosition();
            Vec3 vec32 = vrplayer.vrdata_room_pre.getHand(i).getDirection();
            Vec3 vec33 = vrplayer.vrdata_room_pre.hmd.getDirection();
            Vec3 vec34 = vec3.subtract(vec31);
            double d0 = vec32.dot(this.down);
            double d1 = vec34.dot(vec33);
            boolean flag = Math.abs(vec3.y - vec31.y) < 0.25D;
            boolean flag1 = d1 > 0.0D && vec34.length() > 0.05D;
            boolean flag2 = d0 > 0.6D;
            boolean flag3 = d1 < 0.0D && vec34.length() > 0.25D;
            boolean flag4 = d0 < 0.0D;
            boolean flag5 = flag && flag1 && flag2;
            Minecraft minecraft = Minecraft.getInstance();
            ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();

            if (flag5) {
                if (!this.wasIn[i]) {
                    if (i == 0) {
                        if (!dataholder.climbTracker.isGrabbingLadder() || !dataholder.climbTracker.isClaws(minecraft.player.getMainHandItem())) {
                            if (player.getInventory().selected != 0) {
                                this.previousSlot = player.getInventory().selected;
                                player.getInventory().selected = 0;
                            } else {
                                player.getInventory().selected = this.previousSlot;
                                this.previousSlot = 0;
                            }
                        }
                    } else if (!dataholder.climbTracker.isGrabbingLadder() || !dataholder.climbTracker.isClaws(minecraft.player.getOffhandItem())) {
                        if (dataholder.vrSettings.physicalGuiEnabled) {
                            //minecraft.physicalGuiManager.toggleInventoryBag();
                        } else {
                            player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
                        }
                    }

                    dataholder.vr.triggerHapticPulse(i, 1500);
                    this.wasIn[i] = true;
                }
            } else if (flag3 || flag4) {
                this.wasIn[i] = false;
            }
        }
    }
}
