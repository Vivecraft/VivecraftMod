package org.vivecraft.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.FoodOnAStickItem;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.VRData;
import org.vivecraft.settings.VRSettings;

public class VehicleTracker extends Tracker
{
    private float PreMount_World_Rotation;
    public Vec3 Premount_Pos_Room = new Vec3(0.0D, 0.0D, 0.0D);
    public float vehicleInitialRotation = 0.0F;
    public int rotationCooldown = 0;
    private double rotationTarget = 0.0D;
    private int minecartStupidityCounter;
    public int dismountCooldown = 0;

    public VehicleTracker(Minecraft mc)
    {
        super(mc);
    }

    public boolean isActive(LocalPlayer p)
    {
        Minecraft minecraft = Minecraft.getInstance();

        if (p == null)
        {
            return false;
        }
        else if (minecraft.gameMode == null)
        {
            return false;
        }
        else
        {
            return p.isAlive();
        }
    }

    public void reset(LocalPlayer player)
    {
        this.minecartStupidityCounter = 2;
        super.reset(player);
    }

    public double getVehicleFloor(Entity vehicle, double original)
    {
        return original;
    }

    public static Vec3 getSteeringDirection(LocalPlayer player)
    {
        Vec3 vec3 = null;
        Entity entity = player.getVehicle();
        Minecraft minecraft = Minecraft.getInstance();

        if (!(entity instanceof AbstractHorse) && !(entity instanceof Boat))
        {
            if (entity instanceof Mob)
            {
                Mob mob = (Mob)entity;

                if (mob.canBeControlledByRider())
                {
                    int i = player.getMainHandItem().getItem() instanceof FoodOnAStickItem ? 0 : 1;
                    VRData.VRDevicePose vrdata$vrdevicepose = minecraft.vrPlayer.vrdata_world_pre.getController(i);
                    return vrdata$vrdevicepose.getPosition().add(vrdata$vrdevicepose.getDirection().scale(0.3D)).subtract(entity.position()).normalize();
                }
            }
        }
        else if (player.zza > 0.0F)
        {
            VRSettings vrsettings = minecraft.vrSettings;

            if (minecraft.vrSettings.vrFreeMoveMode == VRSettings.FreeMove.HMD)
            {
                return minecraft.vrPlayer.vrdata_world_pre.hmd.getDirection();
            }

            return minecraft.vrPlayer.vrdata_world_pre.getController(0).getDirection();
        }

        return vec3;
    }

    public void doProcess(LocalPlayer player)
    {
        if (!this.mc.isPaused())
        {
            if (this.dismountCooldown > 0)
            {
                --this.dismountCooldown;
            }

            if (this.rotationCooldown > 0)
            {
                --this.rotationCooldown;
            }

            if (this.mc.vrSettings.vehicleRotation && this.mc.player.isPassenger() && this.rotationCooldown == 0)
            {
                Entity entity = this.mc.player.getVehicle();
                this.rotationTarget = (double)entity.getYRot();

                if (entity instanceof AbstractHorse && !this.mc.horseTracker.isActive(this.mc.player))
                {
                    AbstractHorse abstracthorse = (AbstractHorse)entity;

                    if (abstracthorse.canBeControlledByRider() && abstracthorse.isSaddled())
                    {
                        return;
                    }

                    this.rotationTarget = (double)abstracthorse.yBodyRot;
                }
                else if (entity instanceof Mob)
                {
                    Mob mob = (Mob)entity;

                    if (mob.canBeControlledByRider())
                    {
                        return;
                    }

                    this.rotationTarget = (double)mob.yBodyRot;
                }

                boolean flag = true;
                float smoothed = 10.0F;

                if (entity instanceof Minecart)
                {
                    if (this.shouldMinecartTurnView((Minecart)entity))
                    {
                        if (this.minecartStupidityCounter > 0)
                        {
                            --this.minecartStupidityCounter;
                        }
                    }
                    else
                    {
                        this.minecartStupidityCounter = 3;
                    }

                    this.rotationTarget = (double)this.getMinecartRenderYaw((Minecart)entity);

                    if (this.minecartStupidityCounter > 0)
                    {
                        this.vehicleInitialRotation = (float)this.rotationTarget;
                    }

                    double d0 = this.mineCartSpeed((Minecart)entity);
                    smoothed = 200.0F * (float)(d0 * d0);

                    if (smoothed < 10.0F)
                    {
                        smoothed = 10.0F;
                    }
                }

                float f1 = this.mc.vrPlayer.rotDiff_Degrees((float)this.rotationTarget, this.vehicleInitialRotation);

                if (flag)
                {
                    if (f1 > smoothed)
                    {
                        f1 = smoothed;
                    }

                    if (f1 < -smoothed)
                    {
                        f1 = -smoothed;
                    }
                }

                this.mc.vrSettings.worldRotation += f1;
                this.mc.vrSettings.worldRotation %= 360.0F;
                this.mc.vr.seatedRot = this.mc.vrSettings.worldRotation;
                this.vehicleInitialRotation -= f1;
                this.vehicleInitialRotation %= 360.0F;
            }
            else
            {
                this.minecartStupidityCounter = 3;

                if (this.mc.player.isPassenger())
                {
                    this.vehicleInitialRotation = this.mc.player.getVehicle().getYRot();
                }
            }
        }
    }

    public void onStartRiding(Entity vehicle, LocalPlayer player)
    {
        Minecraft minecraft = Minecraft.getInstance();
        this.PreMount_World_Rotation = minecraft.vrPlayer.vrdata_world_pre.rotation_radians;
        Vec3 vec3 = minecraft.vrPlayer.vrdata_room_pre.getHeadPivot();
        this.Premount_Pos_Room = new Vec3(vec3.x, 0.0D, vec3.z);
        this.dismountCooldown = 5;

        if (minecraft.vrSettings.vehicleRotation)
        {
            float f = minecraft.vrPlayer.vrdata_world_pre.hmd.getYaw();
            float f1 = vehicle.getYRot() % 360.0F;
            this.vehicleInitialRotation = minecraft.vrSettings.worldRotation;
            this.rotationCooldown = 2;

            if (vehicle instanceof Minecart)
            {
                return;
            }

            float f2 = minecraft.vrPlayer.rotDiff_Degrees(f1, f);
            minecraft.vrSettings.worldRotation = (float)(Math.toDegrees((double)minecraft.vrPlayer.vrdata_world_pre.rotation_radians) + (double)f2);
            minecraft.vrSettings.worldRotation %= 360.0F;
            minecraft.vr.seatedRot = minecraft.vrSettings.worldRotation;
        }
    }

    public void onStopRiding(LocalPlayer player)
    {
        this.mc.swingTracker.disableSwing = 10;
        this.mc.sneakTracker.sneakCounter = 0;

        if (this.mc.vrSettings.vehicleRotation)
        {
        }
    }

    private float getMinecartRenderYaw(Minecart entity)
    {
        Vec3 vec3 = new Vec3(entity.getX() - entity.xOld, entity.getY() - entity.yOld, entity.getZ() - entity.zOld);
        float f = (float)Math.toDegrees(Math.atan2(-vec3.x, vec3.z));
        return this.shouldMinecartTurnView(entity) ? -180.0F + f : this.vehicleInitialRotation;
    }

    private double mineCartSpeed(Minecart entity)
    {
        Vec3 vec3 = new Vec3(entity.getDeltaMovement().x, 0.0D, entity.getDeltaMovement().z);
        return vec3.length();
    }

    private boolean shouldMinecartTurnView(Minecart entity)
    {
        Vec3 vec3 = new Vec3(entity.getX() - entity.xOld, entity.getY() - entity.yOld, entity.getZ() - entity.zOld);
        return vec3.length() > 0.001D;
    }

    public boolean canRoomscaleDismount(LocalPlayer player)
    {
        return player.zza == 0.0F && player.xxa == 0.0F && player.isPassenger() && player.getVehicle().isOnGround() && this.dismountCooldown == 0;
    }
}
