package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.FoodOnAStickItem;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client_vr.ItemTags;
import org.vivecraft.client_vr.VRData.VRDevicePose;
import org.vivecraft.client_vr.settings.VRSettings.FreeMove;

import static org.joml.Math.*;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

public class VehicleTracker extends Tracker {
    public Vec3 Premount_Pos_Room = new Vec3(0.0D, 0.0D, 0.0D);
    public float vehicleInitialRotation = 0.0F;
    public int rotationCooldown = 0;
    private double rotationTarget = 0.0D;
    private int minecartStupidityCounter;
    public int dismountCooldown = 0;

    @Override
    public boolean isActive() {

        if (mc.player == null) {
            return false;
        } else if (mc.gameMode == null) {
            return false;
        } else {
            return mc.player.isAlive();
        }
    }

    @Override
    public void reset() {
        this.minecartStupidityCounter = 2;
        super.reset();
    }

    public double getVehicleFloor(Entity vehicle, double original) {
        return original;
    }

    public static Vec3 getSteeringDirection(LocalPlayer player) {
        Vec3 vec3 = null;
        Entity entity = player.getVehicle();

        if (!(entity instanceof AbstractHorse) && !(entity instanceof Boat)) {
            if (entity instanceof Mob mob) {

                if (mob.isControlledByLocalInstance()) {
                    int i = (player.getMainHandItem().getItem() instanceof FoodOnAStickItem || player.getMainHandItem().is(ItemTags.VIVECRAFT_FOOD_STICKS)) ? 0 : 1;
                    VRDevicePose vrdata$vrdevicepose = dh.vrPlayer.vrdata_world_pre.getController(i);
                    return vrdata$vrdevicepose.getPosition().add(vrdata$vrdevicepose.getDirection().scale(0.3D)).subtract(entity.position()).normalize();
                }
            }
        } else if (player.zza > 0.0F) {
            if (dh.vrSettings.vrFreeMoveMode == FreeMove.HMD) {
                return dh.vrPlayer.vrdata_world_pre.hmd.getDirection();
            }

            return dh.vrPlayer.vrdata_world_pre.getController(0).getDirection();
        }

        return vec3;
    }

    @Override
    public void doProcess() {
        if (!mc.isPaused()) {
            if (this.dismountCooldown > 0) {
                --this.dismountCooldown;
            }

            if (this.rotationCooldown > 0) {
                --this.rotationCooldown;
            }

            if (dh.vrSettings.vehicleRotation && mc.player.isPassenger() && this.rotationCooldown == 0) {
                Entity entity = mc.player.getVehicle();
                this.rotationTarget = entity.getYRot();

                if (entity instanceof AbstractHorse abstracthorse && !dh.horseTracker.isActive()) {

                    if (abstracthorse.isControlledByLocalInstance() && abstracthorse.isSaddled()) {
                        return;
                    }

                    this.rotationTarget = abstracthorse.yBodyRot;
                } else if (entity instanceof Mob mob) {

                    if (mob.isControlledByLocalInstance()) {
                        return;
                    }

                    this.rotationTarget = mob.yBodyRot;
                }

                boolean flag = true;
                float smoothed = 10.0F;

                if (entity instanceof Minecart) {
                    if (this.shouldMinecartTurnView((Minecart) entity)) {
                        if (this.minecartStupidityCounter > 0) {
                            --this.minecartStupidityCounter;
                        }
                    } else {
                        this.minecartStupidityCounter = 3;
                    }

                    this.rotationTarget = this.getMinecartRenderYaw((Minecart) entity);

                    if (this.minecartStupidityCounter > 0) {
                        this.vehicleInitialRotation = (float) this.rotationTarget;
                    }

                    double d0 = this.mineCartSpeed((Minecart) entity);
                    smoothed = 200.0F * (float) (d0 * d0);

                    if (smoothed < 10.0F) {
                        smoothed = 10.0F;
                    }
                }

                float f0 = toRadians(this.vehicleInitialRotation);
                float f11 = toRadians((float) this.rotationTarget);
                float f1 = (float) toDegrees(atan2(sin(f0 - f11), cos(f0 - f11)));

                if (flag) {
                    if (f1 > smoothed) {
                        f1 = smoothed;
                    }

                    if (f1 < -smoothed) {
                        f1 = -smoothed;
                    }
                }

                dh.vrSettings.worldRotation += f1;
                dh.vrSettings.worldRotation %= 360.0F;
                dh.vr.seatedRot = dh.vrSettings.worldRotation;
                this.vehicleInitialRotation -= f1;
                this.vehicleInitialRotation %= 360.0F;
            } else {
                this.minecartStupidityCounter = 3;

                if (mc.player.isPassenger()) {
                    this.vehicleInitialRotation = mc.player.getVehicle().getYRot();
                }
            }
        }
    }

    public void onStartRiding(Entity vehicle, LocalPlayer player) {
        Vec3 vec3 = dh.vrPlayer.vrdata_room_pre.getHeadPivot();
        this.Premount_Pos_Room = new Vec3(vec3.x, 0.0D, vec3.z);
        this.dismountCooldown = 5;

        if (dh.vrSettings.vehicleRotation) {
            float f = dh.vrPlayer.vrdata_world_pre.hmd.getYaw();
            float f1 = vehicle.getYRot() % 360.0F;
            this.vehicleInitialRotation = dh.vrSettings.worldRotation;
            this.rotationCooldown = 2;

            if (vehicle instanceof Minecart) {
                return;
            }

            float f0 = toRadians(f);
            float f11 = toRadians(f1);
            float f2 = (float) toDegrees(atan2(sin(f0 - f11), cos(f0 - f11)));
            dh.vrSettings.worldRotation = (float) (toDegrees(dh.vrPlayer.vrdata_world_pre.rotation_radians) + f2);
            dh.vrSettings.worldRotation %= 360.0F;
            dh.vr.seatedRot = dh.vrSettings.worldRotation;
        }
    }

    // TODO: remove onStopRiding?
    public void onStopRiding(LocalPlayer player) {
        dh.swingTracker.disableSwing = 10;
        dh.sneakTracker.sneakCounter = 0;

        if (dh.vrSettings.vehicleRotation) {
            //I dont wanna do this anymore.
            //I think its more confusing to get off the thing and not know where you're looking
            //	mc.vrSettings.vrWorldRotation = playerRotation_PreMount;
            //	mc.vr.seatedRot = playerRotation_PreMount;
        }
    }

    private float getMinecartRenderYaw(Minecart entity) {
        Vec3 vec3 = new Vec3(entity.getX() - entity.xOld, entity.getY() - entity.yOld, entity.getZ() - entity.zOld);
        float f = (float) toDegrees(atan2(-vec3.x, vec3.z));
        return this.shouldMinecartTurnView(entity) ? -180.0F + f : this.vehicleInitialRotation;
    }

    private double mineCartSpeed(Minecart entity) {
        Vec3 vec3 = new Vec3(entity.getDeltaMovement().x, 0.0D, entity.getDeltaMovement().z);
        return vec3.length();
    }

    private boolean shouldMinecartTurnView(Minecart entity) {
        Vec3 vec3 = new Vec3(entity.getX() - entity.xOld, entity.getY() - entity.yOld, entity.getZ() - entity.zOld);
        return vec3.length() > 0.001D;
    }

    public boolean canRoomscaleDismount() {
        return mc.player.zza == 0.0F && mc.player.xxa == 0.0F && mc.player.isPassenger() && mc.player.getVehicle().onGround() && this.dismountCooldown == 0;
    }
}
