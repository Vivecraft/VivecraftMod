package org.vivecraft.client_vr.gameplay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.common.VRServerPerms;
import org.vivecraft.client_vr.gameplay.trackers.Tracker;
import org.vivecraft.mod_compat_vr.pehkui.PehkuiHelper;
import org.vivecraft.client.Xplat;
import org.vivecraft.client_vr.ItemTags;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_vr.gameplay.trackers.VehicleTracker;

import java.util.ArrayList;
import java.util.Random;

public class VRPlayer
{
    Minecraft mc = Minecraft.getInstance();
    ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
    public VRData vrdata_room_pre;
    public VRData vrdata_world_pre;
    public VRData vrdata_room_post;
    public VRData vrdata_world_post;
    public VRData vrdata_world_render;
    ArrayList<Tracker> trackers = new ArrayList<>();
    public float worldScale = ClientDataHolderVR.getInstance().vrSettings.overrides.getSetting(VRSettings.VrOptions.WORLD_SCALE).getFloat();
    private float rawWorldScale = ClientDataHolderVR.getInstance().vrSettings.overrides.getSetting(VRSettings.VrOptions.WORLD_SCALE).getFloat();
    private boolean teleportOverride = false;
    public boolean teleportWarning = false;
    public boolean vrSwitchWarning = false;
    public int chatWarningTimer = -1;
    public Vec3 roomOrigin = new Vec3(0.0D, 0.0D, 0.0D);
    private boolean isFreeMoveCurrent = true;
    public double wfMode = 0.0D;
    public int wfCount = 0;
    public int roomScaleMovementDelay = 0;
    boolean initdone = false;
    public boolean onTick;

    public void registerTracker(Tracker tracker)
    {
        this.trackers.add(tracker);
    }

    public VRPlayer()
    {
        this.vrdata_room_pre = new VRData(new Vec3(0.0D, 0.0D, 0.0D), this.dh.vrSettings.walkMultiplier, 1.0F, 0.0F);
        this.vrdata_room_post = new VRData(new Vec3(0.0D, 0.0D, 0.0D), this.dh.vrSettings.walkMultiplier, 1.0F, 0.0F);
        this.vrdata_world_post = new VRData(new Vec3(0.0D, 0.0D, 0.0D), this.dh.vrSettings.walkMultiplier, 1.0F, 0.0F);
        this.vrdata_world_pre = new VRData(new Vec3(0.0D, 0.0D, 0.0D), this.dh.vrSettings.walkMultiplier, 1.0F, 0.0F);
    }

    public VRData getVRDataWorld()
    {
        return this.vrdata_world_render != null ? this.vrdata_world_render : this.vrdata_world_pre;
    }

    public static VRPlayer get()
    {
        return ClientDataHolderVR.getInstance().vrPlayer;
    }

    public static Vec3 room_to_world_pos(Vec3 pos, VRData data)
    {
        Vec3 vec3 = new Vec3(pos.x * (double)data.worldScale, pos.y * (double)data.worldScale, pos.z * (double)data.worldScale);
        vec3 = vec3.yRot(data.rotation_radians);
        return vec3.add(data.origin.x, data.origin.y, data.origin.z);
    }

    public static Vec3 world_to_room_pos(Vec3 pos, VRData data)
    {
        Vec3 vec3 = pos.add(-data.origin.x, -data.origin.y, -data.origin.z);
        vec3 = new Vec3(vec3.x / (double)data.worldScale, vec3.y / (double)data.worldScale, vec3.z / (double)data.worldScale);
        return vec3.yRot(-data.rotation_radians);
    }

    public void postPoll()
    {
        this.vrdata_room_pre = new VRData(new Vec3(0.0D, 0.0D, 0.0D), this.dh.vrSettings.walkMultiplier, 1.0F, 0.0F);
        GuiHandler.processGui();
        KeyboardHandler.processGui();
        RadialHandler.processGui();
    }

    public void preTick()
    {
        this.onTick = true;
        this.vrdata_world_pre = new VRData(this.roomOrigin, this.dh.vrSettings.walkMultiplier, this.worldScale, (float)Math.toRadians((double)this.dh.vrSettings.worldRotation));
        float f = this.dh.vrSettings.overrides.getSetting(VRSettings.VrOptions.WORLD_SCALE).getFloat();

        if (((GameRendererExtension) this.mc.gameRenderer).isInMenuRoom())
        {
            this.worldScale = 1.0F;
        }
        else {
            if (this.wfCount > 0 && !this.mc.isPaused()) {
                if (this.wfCount < 40) {
                    this.rawWorldScale = (float) ((double) this.rawWorldScale - this.wfMode);

                    if (this.wfMode > 0.0D) {
                        if (this.rawWorldScale < f) {
                            this.rawWorldScale = f;
                        }
                    } else if (this.wfMode < 0.0D && this.rawWorldScale > f) {
                        this.rawWorldScale = f;
                    }
                } else {
                    this.rawWorldScale = (float) ((double) this.rawWorldScale + this.wfMode);

                    if (this.wfMode > 0.0D) {
                        if (this.rawWorldScale > 20.0F) {
                            this.rawWorldScale = 20.0F;
                        }
                    } else if (this.wfMode < 0.0D && this.rawWorldScale < 0.1F) {
                        this.rawWorldScale = 0.1F;
                    }
                }

                --this.wfCount;
            } else {
                this.rawWorldScale = f;
            }

            this.worldScale = rawWorldScale;

            if (Xplat.isModLoaded("pehkui")) {
                // scale world with player size
                this.worldScale *= PehkuiHelper.getPlayerScale(mc.player, mc.getFrameTime());
                // limit scale
                if (this.worldScale > 100F)
                    this.worldScale = 100F;
                else if (this.worldScale < 0.025F) //minClip + player position indicator offset
                    this.worldScale = 0.025F;
            }
        }

        if (this.dh.vrSettings.seated && !((GameRendererExtension) this.mc.gameRenderer).isInMenuRoom())
        {
            this.dh.vrSettings.worldRotation = this.dh.vr.seatedRot;
        }
    }

    public void postTick()
    {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        VRData vrdata = new VRData(this.vrdata_world_pre.origin, dataholder.vrSettings.walkMultiplier, this.vrdata_world_pre.worldScale, this.vrdata_world_pre.rotation_radians);
        VRData vrdata1 = new VRData(this.vrdata_world_pre.origin, dataholder.vrSettings.walkMultiplier, this.worldScale, this.vrdata_world_pre.rotation_radians);
        Vec3 vec3 = vrdata1.hmd.getPosition().subtract(vrdata.hmd.getPosition());
        this.roomOrigin = this.roomOrigin.subtract(vec3);
        VRData vrdata2 = new VRData(this.roomOrigin, dataholder.vrSettings.walkMultiplier, this.worldScale, this.vrdata_world_pre.rotation_radians);
        float f = dataholder.vrSettings.worldRotation;
        float f1 = (float)Math.toDegrees((double)this.vrdata_world_pre.rotation_radians);
        this.rotateOriginAround(-f + f1, vrdata2.getHeadPivot());
        this.vrdata_room_post = new VRData(new Vec3(0.0D, 0.0D, 0.0D), dataholder.vrSettings.walkMultiplier, 1.0F, 0.0F);
        this.vrdata_world_post = new VRData(this.roomOrigin, dataholder.vrSettings.walkMultiplier, this.worldScale, (float)Math.toRadians((double)dataholder.vrSettings.worldRotation));
        this.doPermanantLookOverride(minecraft.player, this.vrdata_world_post);
        ClientNetworking.sendVRPlayerPositions(this);
        this.onTick = false;
    }

    public void preRender(float par1)
    {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        float f = this.vrdata_world_post.worldScale * par1 + this.vrdata_world_pre.worldScale * (1.0F - par1);
        float f1 = this.vrdata_world_post.rotation_radians;
        float f2 = this.vrdata_world_pre.rotation_radians;
        float f3 = Math.abs(f1 - f2);

        if ((double)f3 > Math.PI)
        {
            if (f1 > f2)
            {
                f2 = (float)((double)f2 + (Math.PI * 2D));
            }
            else
            {
                f1 = (float)((double)f1 + (Math.PI * 2D));
            }
        }

        float f4 = f1 * par1 + f2 * (1.0F - par1);
        Vec3 vec3 = new Vec3(this.vrdata_world_pre.origin.x + (this.vrdata_world_post.origin.x - this.vrdata_world_pre.origin.x) * (double)par1, this.vrdata_world_pre.origin.y + (this.vrdata_world_post.origin.y - this.vrdata_world_pre.origin.y) * (double)par1, this.vrdata_world_pre.origin.z + (this.vrdata_world_post.origin.z - this.vrdata_world_pre.origin.z) * (double)par1);
        this.vrdata_world_render = new VRData(vec3, dataholder.vrSettings.walkMultiplier, f, f4);

        for (Tracker tracker : this.trackers)
        {
            if (tracker.getEntryPoint() == Tracker.EntryPoint.SPECIAL_ITEMS)
            {
                tracker.idleTick(minecraft.player);

                if (tracker.isActive(minecraft.player))
                {
                    tracker.doProcess(minecraft.player);
                }
                else
                {
                    tracker.reset(minecraft.player);
                }
            }
        }
    }

    public void postRender(float par1)
    {
    }

    public void setRoomOrigin(double x, double y, double z, boolean reset)
    {
        if (reset && this.vrdata_world_pre != null)
        {
            this.vrdata_world_pre.origin = new Vec3(x, y, z);
        }

        this.roomOrigin = new Vec3(x, y, z);
    }

    public void snapRoomOriginToPlayerEntity(LocalPlayer player, boolean reset, boolean instant)
    {
        if (!Thread.currentThread().getName().equals("Server thread"))
        {
            if (player != null && player.position() != Vec3.ZERO)
            {
                Minecraft minecraft = Minecraft.getInstance();
                ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();

                if (dataholder.sneakTracker.sneakCounter <= 0)
                {
                    VRData vrdata = this.vrdata_world_pre;

                    if (instant)
                    {
                        vrdata = new VRData(this.roomOrigin, dataholder.vrSettings.walkMultiplier, this.worldScale, (float)Math.toRadians((double)dataholder.vrSettings.worldRotation));
                    }

                    Vec3 vec3 = vrdata.getHeadPivot().subtract(vrdata.origin);
                    double d0 = player.getX() - vec3.x;
                    double d2 = player.getZ() - vec3.z;
                    double d1 = player.getY() + ((PlayerExtension) player).getRoomYOffsetFromPose();
                    this.setRoomOrigin(d0, d1, d2, reset);
                }
            }
        }
    }

    public float rotDiff_Degrees(float start, float end)
    {
        double d0 = Math.toRadians((double)end);
        double d1 = Math.toRadians((double)start);
        return (float)Math.toDegrees(Math.atan2(Math.sin(d0 - d1), Math.cos(d0 - d1)));
    }

    public void rotateOriginAround(float degrees, Vec3 o)
    {
        Vec3 vec3 = this.roomOrigin;
        float f = (float)Math.toRadians((double)degrees);

        if (f != 0.0F)
        {
            this.setRoomOrigin(Math.cos((double)f) * (vec3.x - o.x) - Math.sin((double)f) * (vec3.z - o.z) + o.x, vec3.y, Math.sin((double)f) * (vec3.x - o.x) + Math.cos((double)f) * (vec3.z - o.z) + o.z, false);
        }
    }

    public void tick(LocalPlayer player, Minecraft mc, RandomSource rand)
    {
        if (((PlayerExtension) player).getInitFromServer())
        {
            if (!this.initdone)
            {
                System.out.println("<Debug info start>");
                System.out.println("Room object: " + this);
                System.out.println("Room origin: " + this.vrdata_world_pre.origin);
                System.out.println("Hmd position room: " + this.vrdata_room_pre.hmd.getPosition());
                System.out.println("Hmd position world: " + this.vrdata_world_pre.hmd.getPosition());
                System.out.println("Hmd Projection Left: " + dh.vrRenderer.eyeproj[0]);
                System.out.println("Hmd Projection Right: " + dh.vrRenderer.eyeproj[1]);
                System.out.println("<Debug info end>");
                this.initdone = true;
            }

            this.doPlayerMoveInRoom(player);

            for (Tracker tracker : this.trackers)
            {
                if (tracker.getEntryPoint() == Tracker.EntryPoint.LIVING_UPDATE)
                {
                    tracker.idleTick(mc.player);

                    if (tracker.isActive(mc.player))
                    {
                        tracker.doProcess(mc.player);
                    }
                    else
                    {
                        tracker.reset(mc.player);
                    }
                }
            }

            if (player.isPassenger())
            {
                Entity entity = mc.player.getVehicle();

                if (entity instanceof AbstractHorse)
                {
                    AbstractHorse abstracthorse = (AbstractHorse)entity;

                    if (abstracthorse.isControlledByLocalInstance() && abstracthorse.isSaddled() && !dh.horseTracker.isActive(mc.player))
                    {
                        abstracthorse.yBodyRot = this.vrdata_world_pre.getBodyYaw();
                        dh.vehicleTracker.rotationCooldown = 10;
                    }
                }
                else if (entity instanceof Mob)
                {
                    Mob mob = (Mob)entity;

                    if (mob.isControlledByLocalInstance())
                    {
                        mob.yBodyRot = this.vrdata_world_pre.getBodyYaw();
                        dh.vehicleTracker.rotationCooldown = 10;
                    }
                }
            }
        }
    }

    public void doPlayerMoveInRoom(LocalPlayer player)
    {
        if (this.roomScaleMovementDelay > 0)
        {
            --this.roomScaleMovementDelay;
        }
        else
        {
            Minecraft minecraft = Minecraft.getInstance();
            ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();

            if (player != null)
            {
                if (!player.isShiftKeyDown())
                {
                    if (!player.isSleeping())
                    {
                        if (!dataholder.jumpTracker.isjumping())
                        {
                            if (!dataholder.climbTracker.isGrabbingLadder())
                            {
                                if (player.isAlive())
                                {
                                    VRData vrdata = new VRData(this.roomOrigin, dataholder.vrSettings.walkMultiplier, this.worldScale, this.vrdata_world_pre.rotation_radians);

                                    if (dataholder.vehicleTracker.canRoomscaleDismount(minecraft.player))
                                    {
                                        Vec3 vec35 = minecraft.player.getVehicle().position();
                                        Vec3 vec36 = vrdata.getHeadPivot();
                                        double d6 = Math.sqrt((vec36.x - vec35.x) * (vec36.x - vec35.x) + (vec36.z - vec35.z) * (vec36.z - vec35.z));

                                        if (d6 > 1.0D)
                                        {
                                            dataholder.sneakTracker.sneakCounter = 5;
                                        }
                                    }
                                    else
                                    {
                                        float f = player.getBbWidth() / 2.0F;
                                        float f1 = player.getBbHeight();
                                        Vec3 vec3 = vrdata.getHeadPivot();
                                        double d0 = vec3.x;
                                        double d1 = player.getY();
                                        double d2 = vec3.z;
                                        AABB aabb = new AABB(d0 - (double)f, d1, d2 - (double)f, d0 + (double)f, d1 + (double)f1, d2 + (double)f);
                                        Vec3 vec31 = null;
                                        float f2 = 0.0625F;
                                        boolean flag = minecraft.level.noCollision(player, aabb);

                                        if (flag)
                                        {
                                            player.setPosRaw(d0, !dataholder.vrSettings.simulateFalling ? d1 : player.getY(), d2);
                                            player.setBoundingBox(new AABB(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.minY + (double)f1, aabb.maxZ));
                                            player.fallDistance = 0.0F;
                                            this.getEstimatedTorsoPosition(d0, d1, d2);
                                        }
                                        else if ((dataholder.vrSettings.walkUpBlocks && ((PlayerExtension) player).getMuhJumpFactor() == 1.0F || dataholder.climbTracker.isGrabbingLadder() && dataholder.vrSettings.realisticClimbEnabled) && player.fallDistance == 0.0F)
                                        {
                                            if (vec31 == null)
                                            {
                                                vec31 = this.getEstimatedTorsoPosition(d0, d1, d2);
                                            }

                                            float f3 = player.getDimensions(player.getPose()).width * 0.45F;
                                            double d3 = (double)(f - f3);
                                            AABB aabb1 = new AABB(vec31.x - d3, aabb.minY, vec31.z - d3, vec31.x + d3, aabb.maxY, vec31.z + d3);
                                            boolean flag1 = !minecraft.level.noCollision(player, aabb1);

                                            if (flag1)
                                            {
                                                double d4 = vec31.x - d0;
                                                double d5 = vec31.z - d2;
                                                aabb = aabb.move(d4, 0.0D, d5);
                                                int i = 0;

                                                if (player.onClimbable() && dataholder.vrSettings.realisticClimbEnabled)
                                                {
                                                    i = 6;
                                                }

                                                for (int j = 0; j <= 10 + i; ++j)
                                                {
                                                    aabb = aabb.move(0.0D, 0.1D, 0.0D);
                                                    flag = minecraft.level.noCollision(player, aabb);

                                                    if (flag)
                                                    {
                                                        d0 = d0 + d4;
                                                        d2 = d2 + d5;
                                                        d1 = d1 + (double)(0.1F * (float)j);
                                                        player.setPosRaw(d0, d1, d2);
                                                        player.setBoundingBox(new AABB(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ));
                                                        Vec3 vec32 = this.roomOrigin.add(d4, (double)(0.1F * (float)j), d5);
                                                        this.setRoomOrigin(vec32.x, vec32.y, vec32.z, false);
                                                        Vec3 vec33 = player.getLookAngle();
                                                        Vec3 vec34 = (new Vec3(vec33.x, 0.0D, vec33.z)).normalize();
                                                        player.fallDistance = 0.0F;
                                                        ((PlayerExtension) minecraft.player).stepSound(new BlockPos(player.position()), player.position());
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public Vec3 getEstimatedTorsoPosition(double x, double y, double z)
    {
        Entity entity = Minecraft.getInstance().player;
        Vec3 vec3 = entity.getLookAngle();
        Vec3 vec31 = (new Vec3(vec3.x, 0.0D, vec3.z)).normalize();
        float f = (float)vec3.y * 0.25F;
        return new Vec3(x + vec31.x * (double)f, y + vec31.y * (double)f, z + vec31.z * (double)f);
    }

    public void blockDust(double x, double y, double z, int count, BlockPos bp, BlockState bs, float scale, float velscale)
    {
        new Random();
        Minecraft minecraft = Minecraft.getInstance();

        for (int i = 0; i < count; ++i)
        {
            TerrainParticle terrainparticle = new TerrainParticle(minecraft.level, x, y, z, 0.0D, 0.0D, 0.0D, bs);
            terrainparticle.setPower(velscale);
            //TODO: check
           // minecraft.particleEngine.add(terrainparticle.init(bp).scale(scale));
            minecraft.particleEngine.add(terrainparticle.scale(scale));
        }
    }

    public void updateFreeMove()
    {
        if (this.dh.teleportTracker.isAiming())
        {
            this.isFreeMoveCurrent = false;
        }

        if (this.mc.player.input.forwardImpulse != 0.0F || this.mc.player.input.leftImpulse != 0.0F)
        {
            this.isFreeMoveCurrent = true;
        }

        this.updateTeleportKeys();
    }

    public boolean getFreeMove()
    {
        if (this.dh.vrSettings.seated)
        {
            return this.dh.vrSettings.seatedFreeMove || !this.isTeleportEnabled();
        }
        else
        {
            return this.isFreeMoveCurrent || this.dh.vrSettings.forceStandingFreeMove;
        }
    }

    public String toString()
    {
        return "VRPlayer: \r\n \t origin: " + this.roomOrigin + "\r\n \t rotation: " + String.format("%.3f", ClientDataHolderVR.getInstance().vrSettings.worldRotation) + "\r\n \t scale: " + String.format("%.3f", this.worldScale) + "\r\n \t room_pre " + this.vrdata_room_pre + "\r\n \t world_pre " + this.vrdata_world_pre + "\r\n \t world_post " + this.vrdata_world_post + "\r\n \t world_render " + this.vrdata_world_render;
    }

    public Vec3 getRightClickLookOverride(Player entity, int c)
    {
        Vec3 vec3 = entity.getLookAngle();

        if (((GameRendererExtension) this.mc.gameRenderer).getCrossVec() != null)
        {
            vec3 = entity.getEyePosition(1.0F).subtract(((GameRendererExtension) this.mc.gameRenderer).getCrossVec()).normalize().reverse();
        }

        ItemStack itemstack;
        label54:
        {
            itemstack = c == 0 ? entity.getMainHandItem() : entity.getOffhandItem();

            if (!(itemstack.getItem() instanceof SnowballItem) && !(itemstack.getItem() instanceof EggItem) && !(itemstack.getItem() instanceof SpawnEggItem) && !(itemstack.getItem() instanceof PotionItem) && !(itemstack.getItem() instanceof BowItem) && !itemstack.is(ItemTags.VIVECRAFT_THROW_ITEMS))
            {
                if (!(itemstack.getItem() instanceof CrossbowItem))
                {
                    break label54;
                }

                CrossbowItem crossbowitem = (CrossbowItem)itemstack.getItem();

                if (!CrossbowItem.isCharged(itemstack))
                {
                    break label54;
                }
            }

            VRData vrdata = this.dh.vrPlayer.vrdata_world_pre;
            vec3 = vrdata.getController(c).getDirection();
            Vec3 vec31 = this.dh.bowTracker.getAimVector();

            if (this.dh.bowTracker.isNotched() && vec31 != null && vec31.lengthSqr() > 0.0D)
            {
                vec3 = vec31.reverse();
            }

            return vec3;
        }

        if (itemstack.getItem() == Items.BUCKET && this.dh.interactTracker.bukkit[c])
        {
            vec3 = entity.getEyePosition(1.0F).subtract(this.dh.vrPlayer.vrdata_world_pre.getController(c).getPosition()).normalize().reverse();
        }

        return vec3;
    }

    public void doPermanantLookOverride(LocalPlayer entity, VRData data)
    {
    	if (entity == null)
    		return;


    	if (entity.isPassenger())
        {
    		//Server-side movement
    		Vec3 vec3 = VehicleTracker.getSteeringDirection(entity);

    		if (vec3 != null)
    		{
    			entity.setXRot((float)Math.toDegrees(Math.asin(-vec3.y / vec3.length())));
    			entity.setYRot((float)Math.toDegrees(Math.atan2(-vec3.x, vec3.z)));
                entity.setYHeadRot(entity.getYRot());
    		}
    	} else if(entity.isBlocking()) {
    		//block direction
    		if (entity.getUsedItemHand() == InteractionHand.MAIN_HAND) {
    			entity.setYRot(data.getController(0).getYaw());
    			entity.setYHeadRot(entity.getYRot());
    			entity.setXRot(-data.getController(0).getPitch());
        	} else {
    			entity.setYRot(data.getController(1).getYaw());
    			entity.setYHeadRot(entity.getYRot());
    			entity.setXRot(-data.getController(1).getPitch());
        		}	
        	}
    	else  if (entity.isSprinting() && (entity.input.jumping || mc.options.keyJump.isDown()) || entity.isFallFlying() || entity.isSwimming() && entity.zza > 0.0F)
            {
    		//Server-side movement
            VRSettings.FreeMove freeMoveType = entity.isFallFlying() && this.dh.vrSettings.vrFreeMoveFlyMode != VRSettings.FreeMove.AUTO ? this.dh.vrSettings.vrFreeMoveFlyMode : this.dh.vrSettings.vrFreeMoveMode;


    		if (freeMoveType == VRSettings.FreeMove.CONTROLLER)
                {
                    entity.setYRot(data.getController(1).getYaw());
                    entity.setYHeadRot(entity.getYRot());
                    entity.setXRot(-data.getController(1).getPitch());
                }
                else
                {
                    entity.setYRot(data.hmd.getYaw());
                    entity.setYHeadRot(entity.getYRot());
                    entity.setXRot(-data.hmd.getPitch());
                }
            }
    	else if (((GameRendererExtension)mc.gameRenderer).getCrossVec() != null){
    		//Look AT the crosshair by default, most compatible with mods.
    		Vec3 playerToCrosshair = entity.getEyePosition(1).subtract(((GameRendererExtension)mc.gameRenderer).getCrossVec()); //backwards
    		double what = playerToCrosshair.y/playerToCrosshair.length();
    		if(what > 1) what = 1;
    		if(what < -1) what = -1;
    		float pitch = (float)Math.toDegrees(Math.asin(what));
    		float yaw = (float)Math.toDegrees(Math.atan2(playerToCrosshair.x, -playerToCrosshair.z));    
    		entity.setXRot(pitch);
    		entity.setYRot(yaw);
    		entity.setYHeadRot(yaw);
    	} else {
    		//use HMD only if no crosshair hit.
    		entity.setYRot(data.hmd.getYaw());
                    entity.setYHeadRot(entity.getYRot());
    		entity.setXRot(-data.hmd.getPitch());	
        }
    }

    public Vec3 AimedPointAtDistance(VRData source, int controller, double distance)
    {
        Vec3 vec3 = source.getController(controller).getPosition();
        Vec3 vec31 = source.getController(controller).getDirection();
        return vec3.add(vec31.x * distance, vec31.y * distance, vec31.z * distance);
    }

    public HitResult rayTraceBlocksVR(VRData source, int controller, double blockReachDistance, boolean p_174822_4_)
    {
        Vec3 vec3 = source.getController(controller).getPosition();
        Vec3 vec31 = this.AimedPointAtDistance(source, controller, blockReachDistance);
        return this.mc.level.clip(new ClipContext(vec3, vec31, ClipContext.Block.OUTLINE, p_174822_4_ ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, this.mc.player));
    }

    public boolean isTeleportSupported()
    {
        return !VRServerPerms.INSTANCE.noTeleportClient;
    }

    public boolean isTeleportOverridden()
    {
        return this.teleportOverride;
    }

    public boolean isTeleportEnabled()
    {
        boolean flag = !VRServerPerms.INSTANCE.noTeleportClient || this.teleportOverride;

        if (this.dh.vrSettings.seated)
        {
            return flag;
        }
        else
        {
            return flag && !this.dh.vrSettings.forceStandingFreeMove;
        }
    }

    public void setTeleportOverride(boolean override)
    {
        this.teleportOverride = override;
        this.updateTeleportKeys();
    }

    public void updateTeleportKeys()
    {
        this.dh.vr.getInputAction(VivecraftVRMod.INSTANCE.keyTeleport).setEnabled(this.isTeleportEnabled());
        this.dh.vr.getInputAction(VivecraftVRMod.INSTANCE.keyTeleportFallback).setEnabled(!this.isTeleportEnabled());
    }
}
