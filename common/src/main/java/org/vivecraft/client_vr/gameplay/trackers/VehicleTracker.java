package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.item.FoodOnAStickItem;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.vivecraft.api.client.Tracker;
import org.vivecraft.api.data.FBTMode;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.common.utils.MathUtils;
import org.vivecraft.data.ViveItemTags;

public class VehicleTracker implements Tracker {
    private float PreMount_World_Rotation;
    public Vec3 Premount_Pos_Room = Vec3.ZERO;
    public float vehicleInitialRotation = 0.0F;
    public int rotationCooldown = 0;
    public int dismountCooldown = 0;

    private double rotationTarget = 0.0D;
    private int minecartStupidityCounter;
    private boolean isRiding = false;

    private final Minecraft mc;
    private final ClientDataHolderVR dh;

    public VehicleTracker(Minecraft mc, ClientDataHolderVR dh) {
        this.mc = mc;
        this.dh = dh;
    }

    @Override
    public boolean isActive(LocalPlayer player) {
        if (player == null) {
            return false;
        } else if (this.mc.gameMode == null) {
            return false;
        } else {
            return player.isAlive();
        }
    }

    @Override
    public void inactiveProcess(LocalPlayer player) {
        this.minecartStupidityCounter = 2;
        this.isRiding = false;
    }

    @Override
    public ProcessType processType() {
        return ProcessType.PER_TICK;
    }

    public double getVehicleFloor(Entity vehicle, double original) {
//        if (vehicle instanceof AbstractHorse) {
        return original; // horses are fine.
//        } else {
//            return vehicle.getY();
//        }
    }

    public static Vector3f getSteeringDirection(LocalPlayer player) {
        Entity entity = player.getVehicle();
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();

        if (entity instanceof AbstractHorse || entity instanceof AbstractBoat) {
            if (player.zza > 0) {
                return getFreeMoveDirection();
            }
        } else if (entity != null && entity.isLocalInstanceAuthoritative()) {
            int c = getControllerWithFoodStick(player);
            if (entity instanceof Mob && !(entity instanceof HappyGhast) && c != -1) {
                // pigs and striders
                VRData.VRDevicePose con = dataHolder.vrPlayer.vrdata_world_pre.getController(c);
                return MathUtils.subtractToVector3f(con.getPosition(), entity.position())
                    .add(con.getDirection().mul(0.3F))
                    .normalize();
            } else {
                // for other entities always set it, for mod compatibility
                return getFreeMoveDirection();
            }
        }

        // ignore other vehicles
        return null;
    }

    private static Vector3f getFreeMoveDirection() {
        VRData data = ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_pre;
        return switch (ClientDataHolderVR.getInstance().vrSettings.getVrFreeMoveMode(false)) {
            case HMD -> data.hmd.getDirection();
            case WAIST -> new Vector3f(0.0F, 0.0F, 1.0F)
                // use head for up/down
                .rotateX(-data.hmd.getPitchRad())
                .rotateY(data.fbtMode == FBTMode.ARMS_ONLY ? -data.getBodyYawRad() : -data.waist.getYawRad());
            default ->
                // not exactly sure why we use the main hand for riding, when we use the offhand for regular walking
                data.getController(0).getDirection();
        };
    }

    private static int getControllerWithFoodStick(LocalPlayer player) {
        if (player.getMainHandItem().getItem() instanceof FoodOnAStickItem ||
            player.getMainHandItem().is(ViveItemTags.VIVECRAFT_FOOD_STICKS))
        {
            return 0;
        } else if (player.getOffhandItem().getItem() instanceof FoodOnAStickItem ||
            player.getOffhandItem().is(ViveItemTags.VIVECRAFT_FOOD_STICKS))
        {
            return 1;
        } else {
            // doesn't hold a food stick
            return -1;
        }
    }

    @Override
    public void activeProcess(LocalPlayer player) {
        if (!this.mc.isPaused()) {
            // do vehicle rotation, which rotates around a different point.
            if (this.dismountCooldown > 0) {
                this.dismountCooldown--;
            }

            if (this.rotationCooldown > 0) {
                this.rotationCooldown--;
            }

            if (this.dh.vrSettings.vehicleRotation && this.rotationCooldown == 0 && (this.mc.player.isPassenger() ||
                (this.mc.getCameraEntity() != null && this.mc.getCameraEntity() != this.mc.player)
            ))
            {
                Entity entity = this.mc.player.getVehicle();
                if (this.mc.getCameraEntity() != null && this.mc.getCameraEntity() != this.mc.player) {
                    entity = this.mc.getCameraEntity();
                }
                this.rotationTarget = entity.getYRot();

                if (entity instanceof AbstractHorse abstracthorse && !this.dh.horseTracker.isActive(this.mc.player)) {

                    if (abstracthorse.isLocalInstanceAuthoritative() && abstracthorse.isSaddled()) {
                        return;
                    }

                    this.rotationTarget = abstracthorse.yBodyRot;
                } else if (entity instanceof Mob mob) {

                    if (mob.isLocalInstanceAuthoritative()) {
                        return;
                    }

                    this.rotationTarget = mob.yBodyRot;
                }

                boolean smooth = true;
                float smoothIncrement = 10.0F;

                if (entity instanceof Minecart minecart) {
                    // what a pain in my ass
                    if (this.shouldMinecartTurnView(minecart)) {
                        if (this.minecartStupidityCounter > 0) {
                            this.minecartStupidityCounter--;
                        }
                    } else {
                        this.minecartStupidityCounter = 3;
                    }

                    this.rotationTarget = this.getMinecartRenderYaw(minecart);

                    if (this.minecartStupidityCounter > 0) {
                        // do nothing
                        this.vehicleInitialRotation = (float) this.rotationTarget;
                    }

                    double speed = this.mineCartSpeed(minecart);
                    smoothIncrement = 200.0F * (float) (speed * speed);

                    if (smoothIncrement < 10.0F) {
                        smoothIncrement = 10.0F;
                    }
                }

                float difference = this.dh.vrPlayer.rotDiff_Degrees((float) this.rotationTarget,
                    this.vehicleInitialRotation);

                if (smooth) {
                    if (difference > smoothIncrement) {
                        difference = smoothIncrement;
                    }

                    if (difference < -smoothIncrement) {
                        difference = -smoothIncrement;
                    }
                }

                this.dh.vrSettings.worldRotation += difference;
                this.dh.vrSettings.worldRotation %= 360.0F;
                this.dh.vr.seatedRot = this.dh.vrSettings.worldRotation;

                this.vehicleInitialRotation -= difference;
                this.vehicleInitialRotation %= 360.0F;
            } else {
                this.minecartStupidityCounter = 3;

                if (this.mc.getCameraEntity() != null && this.mc.getCameraEntity() != this.mc.player) {
                    this.vehicleInitialRotation = this.mc.getCameraEntity().getYRot();
                } else if (this.mc.player.isPassenger()) {
                    this.vehicleInitialRotation = this.mc.player.getVehicle().getYRot();
                }
            }

            // vehicle movement
            // passanger movement is done in LocalPlayerVRMixin#vivecraft$wrapSetPos
            if (this.isRiding && this.mc.getCameraEntity() != null && this.mc.getCameraEntity() != this.mc.player) {
                // try to make the eye height match
                Vec3 ridingPos = this.mc.getCameraEntity().getEyePosition().subtract(0, this.mc.player.eyeHeight, 0);

                updateRiderPos(ridingPos.x, ridingPos.y, ridingPos.z, this.mc.getCameraEntity());
            }
        }
    }

    public void onStartRiding(Entity vehicle) {
        this.isRiding = true;
        this.PreMount_World_Rotation = this.dh.vrPlayer.vrdata_world_pre.rotation_radians;
        Vec3 camPos = this.dh.vrPlayer.vrdata_room_pre.getHeadPivot();
        this.Premount_Pos_Room = new Vec3(camPos.x, 0.0D, camPos.z);
        this.dismountCooldown = 5;

        if (this.dh.vrSettings.vehicleRotation) {
            float end = this.dh.vrPlayer.vrdata_world_pre.hmd.getYaw();
            float start = vehicle.getYRot() % 360.0F;
            this.vehicleInitialRotation = this.dh.vrSettings.worldRotation;
            this.rotationCooldown = 2;

            if (vehicle instanceof Minecart) {
                // don't align player with minecart, it doesn't have a 'front'
                return;
            }

            float difference = this.dh.vrPlayer.rotDiff_Degrees(start, end);
            this.dh.vrSettings.worldRotation = (float) (
                Math.toDegrees(this.dh.vrPlayer.vrdata_world_pre.rotation_radians) + (double) difference
            );
            this.dh.vrSettings.worldRotation %= 360.0F;
            this.dh.vr.seatedRot = this.dh.vrSettings.worldRotation;
        }
    }

    public void onStopRiding() {
        this.isRiding = false;
        this.dh.swingTracker.disableSwing = 10;
        this.dh.sneakTracker.sneakCounter = 0;

        /*
        if (this.dh.vrSettings.vehicleRotation) {
            // I don't wanna do this anymore.
            // I think its more confusing to get off the thing and not know where you're looking
            this.dh.vrSettings.worldRotation = PreMount_World_Rotation;
            this.dh.vr.seatedRot = PreMount_World_Rotation;
        }
        */
    }

    public boolean isRiding() {
        return this.isRiding;
    }

    public void updateRiderPos(double x, double y, double z, Entity entity) {
        Vec3 offset = this.Premount_Pos_Room.yRot(this.dh.vrPlayer.vrdata_world_pre.rotation_radians);
        x = x - offset.x;
        y = getVehicleFloor(entity, y);
        z = z - offset.z;
        this.dh.vrPlayer.setRoomOrigin(x, y, z, x + y + z == 0.0D);
    }

    private float getMinecartRenderYaw(Minecart entity) {
        Vec3 speed = new Vec3(
            entity.getX() - entity.xOld,
            entity.getY() - entity.yOld,
            entity.getZ() - entity.zOld);

        float speedYaw = (float) Math.toDegrees(Math.atan2(-speed.x, speed.z));
        return this.shouldMinecartTurnView(entity) ? -180.0F + speedYaw : this.vehicleInitialRotation;
    }

    private double mineCartSpeed(Minecart entity) {
        Vec3 speed = new Vec3(entity.getDeltaMovement().x, 0.0D, entity.getDeltaMovement().z);
        return speed.length();
    }

    private boolean shouldMinecartTurnView(Minecart entity) {
        Vec3 speed = new Vec3(
            entity.getX() - entity.xOld,
            entity.getY() - entity.yOld,
            entity.getZ() - entity.zOld);
        return speed.length() > 0.001D;
    }

    public boolean canRoomscaleDismount(LocalPlayer player) {
        return player.zza == 0.0F && player.xxa == 0.0F &&
            player.isPassenger() && player.getVehicle().onGround() &&
            this.dismountCooldown == 0;
    }
}
