package org.vivecraft.client_vr.gameplay.trackers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.network.chat.contents.TranslatableContents;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.BlockTags;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.provider.ControllerType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ClimbTracker extends Tracker
{
    private boolean[] latched = new boolean[2];
    private boolean[] wasinblock = new boolean[2];
    private boolean[] wasbutton = new boolean[2];
    private boolean[] waslatched = new boolean[2];
    public Set<Block> blocklist = new HashSet<>();
    public byte serverblockmode = 0;
    private boolean gravityOverride = false;
    public boolean forceActivate = false;
    public Vec3[] latchStart = new Vec3[] {new Vec3(0.0D, 0.0D, 0.0D), new Vec3(0.0D, 0.0D, 0.0D)};
    public Vec3[] latchStart_room = new Vec3[] {new Vec3(0.0D, 0.0D, 0.0D), new Vec3(0.0D, 0.0D, 0.0D)};
    public Vec3[] latchStartBody = new Vec3[] {new Vec3(0.0D, 0.0D, 0.0D), new Vec3(0.0D, 0.0D, 0.0D)};
    public int latchStartController = -1;
    boolean wantjump = false;
    AABB[] box = new AABB[2];
    AABB[] latchbox = new AABB[2];
    boolean[] inblock = new boolean[2];
    int[] meta = new int[2];
    private AABB northbb = new AABB(0.1D, 0.0D, 0.9D, 0.9D, 1.0D, 1.1D);
    private AABB southBB = new AABB(0.1D, 0.0D, -0.1D, 0.9D, 1.0D, 0.1D);
    private AABB westBB = new AABB(0.9D, 0.0D, 0.1D, 1.1D, 1.0D, 0.9D);
    private AABB eastBB = new AABB(-0.1D, 0.0D, 0.1D, 0.1D, 1.0D, 0.9D);
    private AABB upBB = new AABB(0.0D, 0.9D, 0.0D, 1.0D, 1.1D, 1.0D);
    private AABB fullBB = new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    private Random rand = new Random();
    boolean unsetflag;

    public ClimbTracker(Minecraft mc, ClientDataHolderVR dh)
    {
        super(mc, dh);
    }

    public boolean isGrabbingLadder()
    {
        return this.latched[0] || this.latched[1];
    }

    public boolean wasGrabbingLadder()
    {
        return this.waslatched[0] || this.latched[1];
    }

    public boolean isGrabbingLadder(int controller)
    {
        return this.latched[controller];
    }

    public boolean wasGrabbingLadder(int controller)
    {
        return this.waslatched[controller];
    }

    public boolean isClaws(ItemStack i)
    {
        if (i.isEmpty())
        {
            return false;
        }
        else if (!i.hasCustomHoverName())
        {
            return false;
        }
        else if (i.getItem() != Items.SHEARS)
        {
            return false;
        }
        else if (!i.hasTag() || !i.getTag().getBoolean("Unbreakable"))
        {
            return false;
        }
        else
        {
            return i.getHoverName().getContents() instanceof TranslatableContents && ((TranslatableContents)i.getHoverName().getContents()).getKey().equals("vivecraft.item.climbclaws") || i.getHoverName().getString().equals("Climb Claws");
        }
    }

    public boolean isActive(LocalPlayer p)
    {
        if (this.dh.vrSettings.seated)
        {
            return false;
        }
        else if (!this.dh.vrPlayer.getFreeMove() && !ClientDataHolderVR.getInstance().vrSettings.simulateFalling)
        {
            return false;
        }
        else if (!this.dh.vrSettings.realisticClimbEnabled)
        {
            return false;
        }
        else if (p != null && p.isAlive())
        {
            if (this.mc.gameMode == null)
            {
                return false;
            }
            else if (p.isPassenger())
            {
                return false;
            }
            else
            {
                return this.isClimbeyClimbEquipped() || p.zza == 0.0F && p.xxa == 0.0F;
            }
        }
        else
        {
            return false;
        }
    }

    public boolean isClimbeyClimb()
    {
        return !this.isActive(this.mc.player) ? false : this.isClimbeyClimbEquipped();
    }

    public boolean isClimbeyClimbEquipped()
    {
        return ClientNetworking.serverAllowsClimbey && ((PlayerExtension) this.mc.player).isClimbeyClimbEquipped();
    }

    private boolean canstand(BlockPos bp, LocalPlayer p)
    {
        AABB aabb = p.level().getBlockState(bp).getCollisionShape(p.level(), bp).bounds();

        if (aabb != null && aabb.maxY != 0.0D)
        {
            BlockPos blockpos = bp.above();
            AABB aabb1 = p.level().getBlockState(blockpos).getCollisionShape(p.level(), blockpos).bounds();

            if (aabb1 != null && aabb1.maxY > 0.0D)
            {
                return false;
            }
            else
            {
                BlockPos blockpos1 = blockpos.above();
                AABB aabb2 = p.level().getBlockState(blockpos1).getCollisionShape(p.level(), blockpos1).bounds();
                return aabb2 == null || !(aabb2.maxY > 0.0D);
            }
        }
        else
        {
            return false;
        }
    }

    public void idleTick(LocalPlayer player)
    {
        if (!this.isActive(player))
        {
            this.waslatched[0] = false;
            this.waslatched[1] = false;
        }

        if (this.wasGrabbingLadder() && !this.isGrabbingLadder())
        {
            this.forceActivate = true;
        }
        else if (this.mc.player.onGround() || this.mc.player.getAbilities().flying)
        {
            this.forceActivate = false;
        }

        this.dh.vr.getInputAction(VivecraftVRMod.INSTANCE.keyClimbeyGrab).setEnabled(ControllerType.RIGHT, this.isClimbeyClimb() && (this.isGrabbingLadder() || this.inblock[0] || this.forceActivate));
        this.dh.vr.getInputAction(VivecraftVRMod.INSTANCE.keyClimbeyGrab).setEnabled(ControllerType.LEFT, this.isClimbeyClimb() && (this.isGrabbingLadder() || this.inblock[1] || this.forceActivate));
    }

    public void reset(LocalPlayer player)
    {
        this.latchStartController = -1;
        this.latched[0] = false;
        this.latched[1] = false;
        player.setNoGravity(false);
    }

    public void doProcess(LocalPlayer player)
    {
        boolean[] aboolean = new boolean[2];
        boolean[] aboolean1 = new boolean[2];
        Vec3[] avec3 = new Vec3[2];
        boolean flag = false;
        boolean flag1 = false;
        boolean flag2 = false;
        boolean flag3 = false;

        for (int i = 0; i < 2; ++i)
        {
            avec3[i] = this.dh.vrPlayer.vrdata_world_pre.getController(i).getPosition();
            Vec3 vec3 = this.dh.vrPlayer.vrdata_world_pre.getController(i).getDirection();
            this.inblock[i] = false;
            BlockPos blockpos = BlockPos.containing(avec3[i]);
            BlockState blockstate = this.mc.level.getBlockState(blockpos);
            Block block = blockstate.getBlock();
            VoxelShape voxelshape = blockstate.getCollisionShape(this.mc.level, blockpos);

            if (voxelshape.isEmpty())
            {
                this.box[i] = null;
            }
            else
            {
                this.box[i] = voxelshape.bounds();
            }

            if (!this.dh.climbTracker.isClimbeyClimb())
            {
                Vec3 vec31 = this.dh.vrPlayer.vrdata_world_pre.getController(i).getPosition().subtract(vec3.scale(0.2D));
                AABB aabb = new AABB(avec3[i], vec31);
                flag3 = true;
                boolean flag4 = block instanceof LadderBlock || block instanceof VineBlock || blockstate.is(BlockTags.VIVECRAFT_CLIMBABLE);

                if (!flag4)
                {
                    BlockPos blockpos1 = BlockPos.containing(vec31);
                    BlockState blockstate1 = this.mc.level.getBlockState(blockpos1);
                    Block block1 = blockstate1.getBlock();

                    if (block1 instanceof LadderBlock || block1 instanceof VineBlock || blockstate1.is(BlockTags.VIVECRAFT_CLIMBABLE))
                    {
                        blockpos = blockpos1;
                        blockstate = blockstate1;
                        block = blockstate1.getBlock();
                        avec3[i] = vec31;
                        VoxelShape voxelshape1 = blockstate1.getCollisionShape(this.mc.level, blockpos1);

                        if (voxelshape1.isEmpty())
                        {
                            this.box[i] = null;
                            flag4 = false;
                        }
                        else
                        {
                            flag4 = true;
                            this.box[i] = voxelshape1.bounds();
                        }
                    }
                }

                if (flag4)
                {
                    List<AABB> list = new ArrayList<>();

                    if (block instanceof LadderBlock)
                    {
                        switch ((Direction)blockstate.getValue(LadderBlock.FACING))
                        {
                            case DOWN:
                                flag4 = false;
                                break;

                            case EAST:
                                list.add(this.eastBB);
                                break;

                            case NORTH:
                                list.add(this.northbb);
                                break;

                            case SOUTH:
                                list.add(this.southBB);
                                break;

                            case UP:
                                list.add(this.upBB);
                                break;

                            case WEST:
                                list.add(this.westBB);
                                break;

                            default:
                                flag4 = false;
                        }
                    }

                    if (block instanceof VineBlock)
                    {
                        flag4 = true;
                        this.box[i] = new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

                        if (blockstate.getValue(VineBlock.NORTH) && this.mc.level.getBlockState(blockpos.north()).canOcclude())
                        {
                            list.add(this.southBB);
                        }

                        if (blockstate.getValue(VineBlock.EAST) && this.mc.level.getBlockState(blockpos.east()).canOcclude())
                        {
                            list.add(this.westBB);
                        }

                        if (blockstate.getValue(VineBlock.SOUTH) && this.mc.level.getBlockState(blockpos.south()).canOcclude())
                        {
                            list.add(this.northbb);
                        }

                        if (blockstate.getValue(VineBlock.WEST) && this.mc.level.getBlockState(blockpos.west()).canOcclude())
                        {
                            list.add(this.eastBB);
                        }
                    }

                    this.inblock[i] = false;

                    if (flag4)
                    {
                        for (AABB aabb2 : list)
                        {
                            if (aabb.intersects(aabb2.move(blockpos)))
                            {
                                this.inblock[i] = true;

                                if (aabb2 == this.northbb)
                                {
                                    this.meta[i] = 2;
                                }
                                else if (aabb2 == this.southBB)
                                {
                                    this.meta[i] = 3;
                                }
                                else if (aabb2 == this.eastBB)
                                {
                                    this.meta[i] = 5;
                                }
                                else if (aabb2 == this.westBB)
                                {
                                    this.meta[i] = 4;
                                }

                                break;
                            }
                        }
                    }
                }
                else
                {
                    Vec3 vec311 = this.latchStart[i].subtract(avec3[i]);
                    double d9 = vec311.length();

                    if (d9 > 0.5D)
                    {
                        this.inblock[i] = false;
                    }
                    else
                    {
                        BlockPos blockpos5 = BlockPos.containing(this.latchStart[i]);
                        BlockState blockstate2 = this.mc.level.getBlockState(blockpos5);
                        this.inblock[i] = this.wasinblock[i] && blockstate2.getBlock() instanceof LadderBlock || blockstate2.getBlock() instanceof VineBlock || blockstate2.is(BlockTags.VIVECRAFT_CLIMBABLE);
                    }
                }

                aboolean[i] = this.inblock[i];
                aboolean1[i] = this.inblock[i];
            }
            else
            {
                if (this.mc.player.onGround())
                {
                    this.mc.player.setOnGround(!this.latched[0] && !this.latched[1]);
                }

                if (i == 0)
                {
                    aboolean[i] = VivecraftVRMod.INSTANCE.keyClimbeyGrab.isDown(ControllerType.RIGHT);
                }
                else
                {
                    aboolean[i] = VivecraftVRMod.INSTANCE.keyClimbeyGrab.isDown(ControllerType.LEFT);
                }

                this.inblock[i] = this.box[i] != null && this.box[i].move(blockpos).contains(avec3[i]);

                if (!this.inblock[i])
                {
                    Vec3 vec310 = this.latchStart[i].subtract(avec3[i]);
                    double d7 = vec310.length();

                    if (d7 > 0.5D)
                    {
                        aboolean[i] = false;
                    }
                }

                aboolean1[i] = this.allowed(blockstate);
            }

            this.waslatched[i] = this.latched[i];

            if (!aboolean[i] && this.latched[i])
            {
                this.latched[i] = false;

                if (i == 0)
                {
                    VivecraftVRMod.INSTANCE.keyClimbeyGrab.unpressKey(ControllerType.RIGHT);
                }
                else
                {
                    VivecraftVRMod.INSTANCE.keyClimbeyGrab.unpressKey(ControllerType.LEFT);
                }

                flag2 = true;
            }

            if (!this.latched[i] && !flag && aboolean1[i])
            {
                if (!this.wasinblock[i] && this.inblock[i])
                {
                    this.dh.vr.triggerHapticPulse(i, 750);
                }

                if (!this.wasinblock[i] && this.inblock[i] && aboolean[i] || !this.wasbutton[i] && aboolean[i] && this.inblock[i])
                {
                    flag1 = true;
                    this.wantjump = false;
                    this.latchStart[i] = avec3[i];
                    this.latchStart_room[i] = this.dh.vrPlayer.vrdata_room_pre.getController(i).getPosition();
                    this.latchStartBody[i] = player.position();
                    this.latchStartController = i;
                    this.latchbox[i] = this.box[i];
                    this.latched[i] = true;

                    if (i == 0)
                    {
                        this.latched[1] = false;
                        flag = true;
                    }
                    else
                    {
                        this.latched[0] = false;
                    }

                    this.dh.vr.triggerHapticPulse(i, 2000);
                    ((PlayerExtension) this.mc.player).stepSound(blockpos, this.latchStart[i]);

                    if (!flag3)
                    {
                        this.dh.vrPlayer.blockDust(this.latchStart[i].x, this.latchStart[i].y, this.latchStart[i].z, 5, blockpos, blockstate, 0.1F, 0.2F);
                    }
                }
            }

            this.wasbutton[i] = aboolean[i];
            this.wasinblock[i] = this.inblock[i];
        }

        if (!this.latched[0] && !this.latched[1])
        {
            for (int k = 0; k < 2; ++k)
            {
                if (this.inblock[k] && aboolean[k] && aboolean1[k])
                {
                    flag1 = true;
                    this.latchStart[k] = avec3[k];
                    this.latchStart_room[k] = this.dh.vrPlayer.vrdata_room_pre.getController(k).getPosition();
                    this.latchStartBody[k] = player.position();
                    this.latchStartController = k;
                    this.latched[k] = true;
                    this.latchbox[k] = this.box[k];
                    this.wantjump = false;
                    this.dh.vr.triggerHapticPulse(k, 2000);
                    BlockPos blockpos4 = BlockPos.containing(this.latchStart[k]);
                    BlockState blockstate4 = this.mc.level.getBlockState(blockpos4);

                    if (!flag3)
                    {
                        this.dh.vrPlayer.blockDust(this.latchStart[k].x, this.latchStart[k].y, this.latchStart[k].z, 5, blockpos4, blockstate4, 0.1F, 0.2F);
                    }
                }
            }
        }

        if (!this.wantjump && !flag3)
        {
            this.wantjump = VivecraftVRMod.INSTANCE.keyClimbeyJump.isDown() && this.dh.jumpTracker.isClimbeyJumpEquipped();
        }

        flag2 = flag2 & this.wantjump;

        if ((this.latched[0] || this.latched[1]) && !this.gravityOverride)
        {
            this.unsetflag = true;
            player.setNoGravity(true);
            this.gravityOverride = true;
        }

        if (!this.latched[0] && !this.latched[1] && this.gravityOverride)
        {
            player.setNoGravity(false);
            this.gravityOverride = false;
        }

        if (!this.latched[0] && !this.latched[1] && !flag2)
        {
            if (player.onGround() && this.unsetflag)
            {
                this.unsetflag = false;
                VivecraftVRMod.INSTANCE.keyClimbeyGrab.unpressKey(ControllerType.RIGHT);
                VivecraftVRMod.INSTANCE.keyClimbeyGrab.unpressKey(ControllerType.LEFT);
            }

            this.latchStartController = -1;
        }
        else
        {
            if ((this.latched[0] || this.latched[1]) && this.rand.nextInt(20) == 10)
            {
                this.mc.player.causeFoodExhaustion(0.1F);
                BlockPos blockpos3 = BlockPos.containing(this.latchStart[this.latchStartController]);
                BlockState blockstate3 = this.mc.level.getBlockState(blockpos3);

                if (!flag3)
                {
                    this.dh.vrPlayer.blockDust(this.latchStart[this.latchStartController].x, this.latchStart[this.latchStartController].y, this.latchStart[this.latchStartController].z, 1, blockpos3, blockstate3, 0.1F, 0.2F);
                }
            }

            Vec3 vec34 = this.dh.vrPlayer.vrdata_world_pre.getController(this.latchStartController).getPosition();
            VRPlayer vrplayer = this.dh.vrPlayer;
            Vec3 vec35 = VRPlayer.room_to_world_pos(this.latchStart_room[this.latchStartController], this.dh.vrPlayer.vrdata_world_pre);
            Vec3 vec36 = vec34.subtract(vec35);
            this.latchStart_room[this.latchStartController] = this.dh.vrPlayer.vrdata_room_pre.getController(this.latchStartController).getPosition();

            if (this.wantjump)
            {
                this.dh.vr.triggerHapticPulse(this.latchStartController, 200);
            }

            if (!flag2)
            {
                Vec3 vec37 = this.latchStart[this.latchStartController];

                if (flag1)
                {
                    player.setDeltaMovement(0.0D, 0.0D, 0.0D);
                }
                else
                {
                    player.setDeltaMovement(player.getDeltaMovement().x, 0.0D, player.getDeltaMovement().z);
                }

                player.fallDistance = 0.0F;
                double d4 = player.getX();
                double d6 = player.getY();
                double d8 = player.getZ();
                double d10 = d4;
                double d0 = d8;
                double d11 = d6 - vec36.y;
                BlockPos blockpos2 = BlockPos.containing(vec37);

                if (!flag3)
                {
                    d10 = d4 - vec36.x;
                    d0 = d8 - vec36.z;
                }
                else
                {
                    int j = this.meta[this.latchStartController];

                    if (j != 2 && j != 3)
                    {
                        if (j == 4 || j == 5)
                        {
                            d0 = d8 - vec36.z;
                            d10 = (double)((float)blockpos2.getX() + 0.5F);
                        }
                    }
                    else
                    {
                        d10 = d4 - vec36.x;
                        d0 = (double)((float)blockpos2.getZ() + 0.5F);
                    }
                }

                double d12 = this.dh.vrPlayer.vrdata_room_pre.getHeadPivot().y;
                double d1 = this.dh.vrPlayer.vrdata_room_pre.getController(this.latchStartController).getPosition().y;

                if (!this.wantjump && this.latchbox[this.latchStartController] != null && d1 <= d12 / 2.0D && this.latchStart[this.latchStartController].y > this.latchbox[this.latchStartController].maxY * 0.8D + (double)blockpos2.getY())
                {
                    Vec3 vec32 = this.dh.vrPlayer.vrdata_world_pre.hmd.getDirection().scale((double)0.1F);
                    Vec3 vec33 = (new Vec3(vec32.x, 0.0D, vec32.z)).normalize().scale(0.1D);
                    boolean flag5 = this.mc.level.noCollision(player, player.getBoundingBox().move(vec33.x, this.latchbox[this.latchStartController].maxY + (double)blockpos2.getY() - player.getY(), vec33.z));

                    if (flag5)
                    {
                        d10 = player.getX() + vec33.x;
                        d11 = this.latchbox[this.latchStartController].maxY + (double)blockpos2.getY();
                        d0 = player.getZ() + vec33.z;
                        this.latchStartController = -1;
                        this.latched[0] = false;
                        this.latched[1] = false;
                        this.wasinblock[0] = false;
                        this.wasinblock[1] = false;
                        player.setNoGravity(false);
                    }
                }

                boolean flag6 = false;

                for (int l = 0; l < 8; ++l)
                {
                    double d13 = d10;
                    double d2 = d11;
                    double d3 = d0;

                    switch (l)
                    {
                        case 1:
                        default:
                            break;

                        case 2:
                            d2 = d6;
                            break;

                        case 3:
                            d3 = d8;
                            break;

                        case 4:
                            d13 = d4;
                            break;

                        case 5:
                            d13 = d4;
                            d3 = d8;
                            break;

                        case 6:
                            d13 = d4;
                            d2 = d6;
                            break;

                        case 7:
                            d2 = d6;
                            d3 = d8;
                    }

                    player.setPos(d13, d2, d3);
                    AABB aabb1 = player.getBoundingBox();
                    flag6 = this.mc.level.noCollision(player, aabb1);

                    if (flag6)
                    {
                        if (l > 1)
                        {
                            this.dh.vr.triggerHapticPulse(0, 100);
                            this.dh.vr.triggerHapticPulse(1, 100);
                        }

                        break;
                    }
                }

                if (!flag6)
                {
                    player.setPos(d4, d6, d8);
                    this.dh.vr.triggerHapticPulse(0, 100);
                    this.dh.vr.triggerHapticPulse(1, 100);
                }

                if (this.mc.isLocalServer())
                {
                    for (ServerPlayer serverplayer : this.mc.getSingleplayerServer().getPlayerList().getPlayers())
                    {
                        if (serverplayer.getId() == this.mc.player.getId())
                        {
                            serverplayer.fallDistance = 0.0F;
                        }
                    }
                }
                else
                {
                    ServerboundCustomPayloadPacket serverboundcustompayloadpacket = ClientNetworking.getVivecraftClientPacket(CommonNetworkHelper.PacketDiscriminators.CLIMBING, new byte[0]);

                    if (this.mc.getConnection() != null)
                    {
                        this.mc.getConnection().send(serverboundcustompayloadpacket);
                    }
                }
            }
            else
            {
                this.wantjump = false;
                Vec3 vec38 = player.position().subtract(vec36);
                Vec3 vec39 = this.dh.vr.controllerHistory[this.latchStartController].netMovement(0.3D);
                double d5 = this.dh.vr.controllerHistory[this.latchStartController].averageSpeed((double)0.3F);
                vec39 = vec39.scale(0.66D * d5);
                float f = 0.66F;

                if (vec39.length() > (double)f)
                {
                    vec39 = vec39.scale((double)f / vec39.length());
                }

                if (player.hasEffect(MobEffects.JUMP))
                {
                    vec39 = vec39.scale((double)player.getEffect(MobEffects.JUMP).getAmplifier() + 1.5D);
                }

                vec39 = vec39.yRot(this.dh.vrPlayer.vrdata_world_pre.rotation_radians);
                player.setDeltaMovement(vec39.multiply(-1.0D, -1.0D, -1.0D));
                player.xOld = vec38.x;
                player.yOld = vec38.y;
                player.zOld = vec38.z;
                vec38 = vec38.add(player.getDeltaMovement().x, player.getDeltaMovement().y, player.getDeltaMovement().z);
                player.setPos(vec38.x, vec38.y, vec38.z);
                this.dh.vrPlayer.snapRoomOriginToPlayerEntity(player, false, false);
                this.mc.player.causeFoodExhaustion(0.3F);
            }
        }
    }

    private boolean allowed(BlockState bs)
    {
        if (this.serverblockmode == 0)
        {
            return true;
        }
        else if (this.serverblockmode == 1)
        {
            return this.blocklist.contains(bs.getBlock());
        }
        else if (this.serverblockmode == 2)
        {
            return !this.blocklist.contains(bs.getBlock());
        }
        else
        {
            return false;
        }
    }
}
