package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static org.joml.Math.abs;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.*;

public class BackpackTracker extends Tracker {
    public boolean[] wasIn = new boolean[2];
    public int previousSlot = 0;

    @Override
    public boolean isActive() {

        if (dh.vrSettings.seated) {
            return false;
        } else if (!dh.vrSettings.backpackSwitching) {
            return false;
        } else if (mc.player == null) {
            return false;
        } else if (mc.gameMode == null) {
            return false;
        } else if (!mc.player.isAlive()) {
            return false;
        } else if (mc.player.isSleeping()) {
            return false;
        } else {
            return !dh.bowTracker.isDrawing;
        }
    }

    @Override
    public void doProcess() {
        Vector3fc hmdPos = convertToVector3f(dh.vrPlayer.vrdata_room_pre.getHeadRear(), new Vector3f());

        for (int hand = 0; hand < 2; ++hand) {
            Vector3f controllerPos = dh.vrPlayer.vrdata_room_pre.getController(hand).getPosition(new Vector3f());
            boolean below = abs(hmdPos.y() - controllerPos.y()) < 0.25D;
            hmdPos.sub(controllerPos, controllerPos); // controllerPos -> delta
            double dot = dh.vrPlayer.vrdata_room_pre.getHand(hand).getDirection(new Vector3f()).dot(0.0F, -1.0F, 0.0F);
            double dotDelta = controllerPos.dot(dh.vrPlayer.vrdata_room_pre.hmd.getDirection(new Vector3f()));
            boolean behind = dotDelta > 0.0D && controllerPos.length() > 0.05D;
            boolean aimdown = dot > 0.6D;
            boolean infront = dotDelta < 0.0D && controllerPos.length() > 0.25D;
            boolean aimup = dot < 0.0D;
            boolean zone = below && behind && aimdown;

            if (zone) {
                if (!this.wasIn[hand]) {
                    if (hand == 0) {
                        if (!dh.climbTracker.isGrabbingLadder() || !dh.climbTracker.isClaws(mc.player.getMainHandItem())) {
                            if (mc.player.getInventory().selected != 0) {
                                this.previousSlot = mc.player.getInventory().selected;
                                mc.player.getInventory().selected = 0;
                            } else {
                                mc.player.getInventory().selected = this.previousSlot;
                                this.previousSlot = 0;
                            }
                        }
                    } else if (!dh.climbTracker.isGrabbingLadder() || !dh.climbTracker.isClaws(mc.player.getOffhandItem())) {
//                        if (dh.vrSettings.physicalGuiEnabled) {
//                            minecraft.physicalGuiManager.toggleInventoryBag();
//                        } else {
                            mc.player.connection.send(new ServerboundPlayerActionPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
//                        }
                    }

                    dh.vr.triggerHapticPulse(hand, 1500);
                    this.wasIn[hand] = true;
                }
            } else if (infront || aimup) {
                this.wasIn[hand] = false;
            }
        }
    }
}
