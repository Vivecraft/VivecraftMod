package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.vivecraft.api.client.Tracker;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;
import org.vivecraft.common.utils.MathUtils;

import java.util.Arrays;

public class BackpackTracker implements Tracker {
    public boolean[] wasIn = new boolean[2];
    private final Minecraft mc;
    private final ClientDataHolderVR dh;

    public BackpackTracker(Minecraft mc, ClientDataHolderVR dh) {
        this.mc = mc;
        this.dh = dh;
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
        } else if (this.mc.options.keyAttack.isDown() || VivecraftVRMod.INSTANCE.keyVRInteract.isDown()) {
            // don't swap while doing an action
            return false;
        } else {
            return !this.dh.bowTracker.isDrawing();
        }
    }

    @Override
    public ProcessType processType() {
        return ProcessType.PER_TICK;
    }

    @Override
    public void activeProcess(LocalPlayer player) {
        VRPlayer provider = this.dh.vrPlayer;
        Vec3 hmdPos = provider.vrdata_room_pre.getHeadRear();

        for (int c = 0; c < 2; c++) {
            Vec3 controllerPos = provider.vrdata_room_pre.getController(c).getPosition();
            Vector3f controllerDir = provider.vrdata_room_pre.getHand(c).getDirection();
            Vector3f hmdDir = provider.vrdata_room_pre.hmd.getDirection();
            Vector3f delta = MathUtils.subtractToVector3f(hmdPos, controllerPos);

            double dot = controllerDir.dot(MathUtils.DOWN);
            double dotDelta = delta.dot(hmdDir);

            boolean below = Math.abs(hmdPos.y - controllerPos.y) < 0.25D;
            boolean behind = dotDelta > 0.0D && delta.length() > 0.05D;
            boolean aimDown = dot > 0.6D;
            boolean infront = dotDelta < 0.0D && delta.length() > 0.25D;
            boolean aimUp = dot < 0.0D;

            boolean zone = below && behind && aimDown;

            if (zone) {
                if (!this.wasIn[c]) {
                    pressKeybind(c);

                    this.dh.vr.triggerHapticPulse(c, 1500);
                    this.wasIn[c] = true;
                }
            } else if (infront || aimUp) {
                this.wasIn[c] = false;
            }
        }
    }

    private void pressKeybind(int c) {
        String keybind =
            c == 0 ? this.dh.vrSettings.backpackMainHandKeybind : this.dh.vrSettings.backpackOffhandKeybind;
        Arrays.stream(this.mc.options.keyMappings)
            .filter(keymapping -> keymapping.getName().equalsIgnoreCase(keybind))
            .findFirst()
            .ifPresent(keymapping -> {
                // if the offhand keybind is set to swap item, don't swap, if we are climbing
                if (keymapping != this.mc.options.keySwapOffhand ||
                    !this.dh.climbTracker.isClimbingWith(InteractionHand.OFF_HAND))
                {
                    VRInputAction vrinputaction = MCVR.get().getInputAction(keymapping);
                    if (vrinputaction != null) {
                        vrinputaction.pressBinding();
                        // hold for 2 ticks, since this tracker runs in the middle of the tick,
                        // and unpress would happen at start of next tick
                        vrinputaction.unpressBinding(2);
                    }
                }
            });
    }
}
