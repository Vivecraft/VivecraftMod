package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.math.Quaternion;

public class HorseTracker extends Tracker {
    private static final double boostTrigger = 1.4D;
    private static final double pullTrigger = 0.8D;
    private static final int maxSpeedLevel = 3;
    private static final long coolDownMillis = 500L;
    private static final double turnSpeed = 6.0D;
    private static final double bodyTurnSpeed = 0.2D;
    private static final double baseSpeed = 0.2D;
    private int speedLevel = 0;
    private long lastBoostMillis = -1L;
    private Horse horse = null;
    private ModelInfo info = new ModelInfo();

    public HorseTracker(Minecraft mc, ClientDataHolderVR dh) {
        super(mc, dh);
    }

    @Override
    public boolean isActive(LocalPlayer player) {
        if (true) {
            // this tracker is currently unused
            return false;
        } else if (this.dh.vrSettings.seated) {
            return false;
        } else if (player == null || !player.isAlive()) {
            return false;
        } else if (this.mc.gameMode == null) {
            return false;
        } else if (this.mc.options.keyUp.isDown()) {
            return false;
        } else if (!(player.getVehicle() instanceof AbstractHorse)) {
            return false;
        } else {
            return !this.dh.bowTracker.isNotched();
        }
    }

    @Override
    public void reset(LocalPlayer player) {
        super.reset(player);

        if (this.horse != null) {
            this.horse.setNoAi(false);
        }
    }

    @Override
    public void doProcess(LocalPlayer player) {
        this.horse = (Horse) player.getVehicle();
        this.horse.setNoAi(true);
        float absYaw = (this.horse.getYRot() + 360.0F) % 360.0F;
        float absYawOffset = (this.horse.yBodyRot + 360.0F) % 360.0F;

        Vec3 speedLeft = this.dh.vr.controllerHistory[1].netMovement(0.1D).scale(10.0D);
        Vec3 speedRight = this.dh.vr.controllerHistory[0].netMovement(0.1D).scale(10.0D);
        double speedDown = Math.min(-speedLeft.y, -speedRight.y);

        if (speedDown > boostTrigger) {
            this.doBoost();
        }

        Quaternion horseRot = new Quaternion(0.0F, -this.horse.yBodyRot, 0.0F);
        Vec3 back = horseRot.multiply(new Vec3(0.0D, 0.0D, -1.0D));
        Vec3 left = horseRot.multiply(new Vec3(1.0D, 0.0D, 0.0D));
        Vec3 right = horseRot.multiply(new Vec3(-1.0D, 0.0D, 0.0D));

        Quaternion worldRot = new Quaternion(0.0F, VRSettings.inst.worldRotation, 0.0F);

        Vec3 posL = VRPlayer.get().roomOrigin.add(worldRot.multiply(this.dh.vr.controllerHistory[1].latest()));
        Vec3 posR = VRPlayer.get().roomOrigin.add(worldRot.multiply(this.dh.vr.controllerHistory[0].latest()));

        double distanceL = posL.subtract(this.info.leftReinPos).dot(back) + posL.subtract(this.info.leftReinPos).dot(left);
        double distanceR = posR.subtract(this.info.rightReinPos).dot(back) + posR.subtract(this.info.rightReinPos).dot(right);

        if (this.speedLevel < 0) {
            this.speedLevel = 0;
        }

        if (distanceL > pullTrigger + 0.3D
            && distanceR > pullTrigger + 0.3D
            && Math.abs(distanceR - distanceL) < 0.1D) {
            if (this.speedLevel == 0 && System.currentTimeMillis() > this.lastBoostMillis + coolDownMillis) {
                this.speedLevel = -1;
            } else {
                this.doBreak();
            }
        } else {
            double pullL = 0.0D;
            double pullR = 0.0D;

            if (distanceL > pullTrigger) {
                pullL = distanceL - pullTrigger;
            }

            if (distanceR > pullTrigger) {
                pullR = distanceR - pullTrigger;
            }

            this.horse.setYRot((float) (absYaw + (pullR - pullL) * turnSpeed));
        }

        this.horse.yBodyRot = (float) Utils.lerpMod(absYawOffset, absYaw, bodyTurnSpeed, 360.0D);
        this.horse.yHeadRot = absYaw;

        Vec3 movement = horseRot.multiply(new Vec3(0.0D, 0.0D, this.speedLevel * baseSpeed));
        this.horse.setDeltaMovement(movement.x, this.horse.getDeltaMovement().y, movement.z);
    }

    private boolean doBoost() {
        if (this.speedLevel >= maxSpeedLevel) {
            return false;
        } else if (System.currentTimeMillis() < this.lastBoostMillis + coolDownMillis) {
            return false;
        } else {
            // System.out.println("Boost");
            this.speedLevel++;
            this.lastBoostMillis = System.currentTimeMillis();
            return true;
        }
    }

    private boolean doBreak() {
        if (this.speedLevel <= 0) {
            return false;
        } else if (System.currentTimeMillis() < this.lastBoostMillis + coolDownMillis) {
            return false;
        } else {
            System.out.println("Breaking");

            this.speedLevel--;
            this.lastBoostMillis = System.currentTimeMillis();
            return true;
        }
    }

    public ModelInfo getModelInfo() {
        return this.info;
    }

    public static class ModelInfo {
        public Vec3 leftReinPos = Vec3.ZERO;
        public Vec3 rightReinPos = Vec3.ZERO;
    }
}
