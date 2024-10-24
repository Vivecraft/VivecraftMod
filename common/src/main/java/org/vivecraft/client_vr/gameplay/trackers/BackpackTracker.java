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

    @Override
    public boolean isActive(LocalPlayer player) {
        if (this.dh.vrSettings.seated) {
            return false;
        } else if (!this.dh.vrSettings.backpackSwitching) {
            return false;
        } else if (player == null) {
            return false;
        } else if (this.mc.gameMode == null) {
            return false;
        } else if (!player.isAlive()) {
            return false;
        } else if (player.isSleeping()) {
            return false;
        } else {
            return !this.dh.bowTracker.isDrawing;
        }
    }

    @Override
    public void doProcess(LocalPlayer player) {
        VRPlayer provider = this.dh.vrPlayer;
        Vec3 hmdPos = provider.vrdata_room_pre.getHeadRear();

        for (int c = 0; c < 2; c++) {
            Vec3 controllerPos = provider.vrdata_room_pre.getController(c).getPosition();
            Vec3 controllerDir = provider.vrdata_room_pre.getHand(c).getDirection();
            Vec3 hmdDir = provider.vrdata_room_pre.hmd.getDirection();
            Vec3 delta = hmdPos.subtract(controllerPos);

            double dot = controllerDir.dot(this.down);
            double dotDelta = delta.dot(hmdDir);

            boolean below = Math.abs(hmdPos.y - controllerPos.y) < 0.25D;
            boolean behind = dotDelta > 0.0D && delta.length() > 0.05D;
            boolean aimDown = dot > 0.6D;
            boolean infront = dotDelta < 0.0D && delta.length() > 0.25D;
            boolean aimUp = dot < 0.0D;

            boolean zone = below && behind && aimDown;

            if (zone) {
                if (!this.wasIn[c]) {
                    if (c == 0) {
                        //mainhand
                        if (!this.dh.climbTracker.isGrabbingLadder() || !ClimbTracker.isClaws(this.mc.player.getMainHandItem())) {
                            if (player.getInventory().selected != 0) {
                                this.previousSlot = player.getInventory().selected;
                                player.getInventory().selected = 0;
                            } else {
                                player.getInventory().selected = this.previousSlot;
                                this.previousSlot = 0;
                            }
                        }
                    } else {
                        //offhand
                        if (!this.dh.climbTracker.isGrabbingLadder() || !ClimbTracker.isClaws(this.mc.player.getOffhandItem())) {
                            if (this.dh.vrSettings.physicalGuiEnabled) {
                                //minecraft.physicalGuiManager.toggleInventoryBag();
                            } else {
                                player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
                            }
                        }
                    }

                    this.dh.vr.triggerHapticPulse(c, 1500);
                    this.wasIn[c] = true;
                }
            } else if (infront || aimUp) {
                this.wasIn[c] = false;
            }
        }
    }
}
