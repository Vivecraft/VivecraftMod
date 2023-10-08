package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.BlockTags;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.common.network.CommonNetworkHelper.PacketDiscriminators;

import java.util.*;

import static org.joml.Math.min;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.convertToVector3f;

public class ClimbTracker extends Tracker {
    private boolean[] latched = new boolean[2];
    private boolean[] wasinblock = new boolean[2];
    private boolean[] wasbutton = new boolean[2];
    private boolean[] waslatched = new boolean[2];
    public Set<Block> blocklist = new HashSet<>();
    public byte serverblockmode = 0;
    private boolean gravityOverride = false;
    public boolean forceActivate = false;
    public final Vector3f latchStartc0 = new Vector3f();
    public final Vector3f latchStartc1 = new Vector3f();
    public final Vector3f latchStart_roomc0 = new Vector3f();
    public final Vector3f latchStart_roomc1 = new Vector3f();
    public final Vector3f latchStartBodyc0 = new Vector3f();
    public final Vector3f latchStartBodyc1 = new Vector3f();
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

    public boolean isGrabbingLadder() {
        return this.latched[0] || this.latched[1];
    }

    public boolean wasGrabbingLadder() {
        return this.waslatched[0] || this.latched[1];
    }

    public boolean isGrabbingLadder(int controller) {
        return this.latched[controller];
    }

    public boolean wasGrabbingLadder(int controller) {
        return this.waslatched[controller];
    }

    public boolean isClaws(ItemStack i) {
        if (i.isEmpty()) {
            return false;
        } else if (!i.hasCustomHoverName()) {
            return false;
        } else if (i.getItem() != Items.SHEARS) {
            return false;
        } else if (!i.hasTag() || !i.getTag().getBoolean("Unbreakable")) {
            return false;
        } else {
            return i.getHoverName().getContents() instanceof TranslatableContents && "vivecraft.item.climbclaws".equals(((TranslatableContents) i.getHoverName().getContents()).getKey()) || "Climb Claws".equals(i.getHoverName().getString());
        }
    }

    @Override
    public boolean isActive() {
        if (dh.vrSettings.seated) {
            return false;
        } else if (!dh.vrPlayer.getFreeMove() && !dh.vrSettings.simulateFalling) {
            return false;
        } else if (!dh.vrSettings.realisticClimbEnabled) {
            return false;
        } else if (mc.player != null && mc.player.isAlive()) {
            if (mc.gameMode == null) {
                return false;
            } else if (mc.player.isPassenger()) {
                return false;
            } else {
                return this.isClimbeyClimbEquipped() || mc.player.zza == 0.0F && mc.player.xxa == 0.0F;
            }
        } else {
            return false;
        }
    }

    public boolean isClimbeyClimb() {
        return this.isActive() && this.isClimbeyClimbEquipped();
    }

    public boolean isClimbeyClimbEquipped() {
        return ClientNetworking.serverAllowsClimbey && ((PlayerExtension) mc.player).vivecraft$isClimbeyClimbEquipped();
    }

    private boolean canstand(BlockPos bp) {
        AABB aabb = mc.player.level().getBlockState(bp).getCollisionShape(mc.player.level(), bp).bounds();

        if (aabb != null && aabb.maxY != 0.0D) {
            BlockPos blockpos = bp.above();
            AABB aabb1 = mc.player.level().getBlockState(blockpos).getCollisionShape(mc.player.level(), blockpos).bounds();

            if (aabb1 != null && aabb1.maxY > 0.0D) {
                return false;
            } else {
                BlockPos blockpos1 = blockpos.above();
                AABB aabb2 = mc.player.level().getBlockState(blockpos1).getCollisionShape(mc.player.level(), blockpos1).bounds();
                return aabb2 == null || !(aabb2.maxY > 0.0D);
            }
        } else {
            return false;
        }
    }

    @Override
    public void idleTick() {
        if (!this.isActive()) {
            this.waslatched[0] = false;
            this.waslatched[1] = false;
        }

        if (this.wasGrabbingLadder() && !this.isGrabbingLadder()) {
            this.forceActivate = true;
        } else if (mc.player.onGround() || mc.player.getAbilities().flying) {
            this.forceActivate = false;
        }

        dh.vr.getInputAction(VivecraftVRMod.keyClimbeyGrab).setEnabled(ControllerType.RIGHT, this.isClimbeyClimb() && (this.isGrabbingLadder() || this.inblock[0] || this.forceActivate));
        dh.vr.getInputAction(VivecraftVRMod.keyClimbeyGrab).setEnabled(ControllerType.LEFT, this.isClimbeyClimb() && (this.isGrabbingLadder() || this.inblock[1] || this.forceActivate));
    }

    @Override
    public void reset() {
        this.latchStartController = -1;
        this.latched[0] = false;
        this.latched[1] = false;
        mc.player.setNoGravity(false);
    }

    @Override
    public void doProcess() {
        boolean[] aboolean = new boolean[2];
        boolean[] aboolean1 = new boolean[2];
        Vector3f[] avec3 = new Vector3f[2];
        boolean flag = false;
        boolean flag1 = false;
        boolean flag2 = false;
        boolean flag3 = false;

        for (int i = 0; i < 2; ++i) {
            final Vector3f latchStart;
            final Vector3f latchStartRoom;
            final Vector3f latchStartBody;
            if (i == 0) {
                latchStart = latchStartc0;
                latchStartRoom = latchStart_roomc0;
                latchStartBody = latchStartBodyc0;
            } else {
                latchStart = latchStartc1;
                latchStartRoom = latchStart_roomc1;
                latchStartBody = latchStartBodyc1;
            }
            avec3[i] = dh.vrPlayer.vrdata_world_pre.getController(i).getPosition(new Vector3f());
            Vector3f vec3 = dh.vrPlayer.vrdata_world_pre.getController(i).getDirection(new Vector3f());
            this.inblock[i] = false;
            BlockPos blockpos = BlockPos.containing(avec3[i].x(), avec3[i].y(), avec3[i].z());
            BlockState blockstate = mc.level.getBlockState(blockpos);
            Block block = blockstate.getBlock();
            VoxelShape voxelshape = blockstate.getCollisionShape(mc.level, blockpos);

            if (voxelshape.isEmpty()) {
                this.box[i] = null;
            } else {
                this.box[i] = voxelshape.bounds();
            }

            if (!dh.climbTracker.isClimbeyClimb()) {
                Vector3f vec31 = dh.vrPlayer.vrdata_world_pre.getController(i).getPosition(new Vector3f()).sub(vec3.mul(0.2F, new Vector3f()));
                AABB aabb = new AABB(avec3[i].x(), avec3[i].y(), avec3[i].z(), vec31.x(), vec31.y(), vec31.z());
                flag3 = true;
                boolean flag4 = block instanceof LadderBlock || block instanceof VineBlock || blockstate.is(BlockTags.VIVECRAFT_CLIMBABLE);

                if (!flag4) {
                    BlockPos blockpos1 = BlockPos.containing(vec31.x(), vec31.y(), vec31.z());
                    BlockState blockstate1 = mc.level.getBlockState(blockpos1);
                    Block block1 = blockstate1.getBlock();

                    if (block1 instanceof LadderBlock || block1 instanceof VineBlock || blockstate1.is(BlockTags.VIVECRAFT_CLIMBABLE)) {
                        blockpos = blockpos1;
                        blockstate = blockstate1;
                        block = blockstate1.getBlock();
                        avec3[i] = vec31;
                        VoxelShape voxelshape1 = blockstate1.getCollisionShape(mc.level, blockpos1);

                        if (voxelshape1.isEmpty()) {
                            this.box[i] = null;
                            flag4 = false;
                        } else {
                            flag4 = true;
                            this.box[i] = voxelshape1.bounds();
                        }
                    }
                }

                if (flag4) {
                    List<AABB> list = new ArrayList<>();

                    if (block instanceof LadderBlock) {
                        switch (blockstate.getValue(LadderBlock.FACING)) {
                            case EAST -> {
                                list.add(this.eastBB);
                            }
                            case NORTH -> {
                                list.add(this.northbb);
                            }
                            case SOUTH -> {
                                list.add(this.southBB);
                            }
                            case UP -> {
                                list.add(this.upBB);
                            }
                            case WEST -> {
                                list.add(this.westBB);
                            }
                            default -> {
                                flag4 = false;
                            }
                        }
                    }

                    if (block instanceof VineBlock) {
                        flag4 = true;
                        this.box[i] = new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

                        if (blockstate.getValue(VineBlock.NORTH) && mc.level.getBlockState(blockpos.north()).canOcclude()) {
                            list.add(this.southBB);
                        }

                        if (blockstate.getValue(VineBlock.EAST) && mc.level.getBlockState(blockpos.east()).canOcclude()) {
                            list.add(this.westBB);
                        }

                        if (blockstate.getValue(VineBlock.SOUTH) && mc.level.getBlockState(blockpos.south()).canOcclude()) {
                            list.add(this.northbb);
                        }

                        if (blockstate.getValue(VineBlock.WEST) && mc.level.getBlockState(blockpos.west()).canOcclude()) {
                            list.add(this.eastBB);
                        }
                    }

                    this.inblock[i] = false;

                    if (flag4) {
                        for (AABB aabb2 : list) {
                            if (aabb.intersects(aabb2.move(blockpos))) {
                                this.inblock[i] = true;

                                if (aabb2 == this.northbb) {
                                    this.meta[i] = 2;
                                } else if (aabb2 == this.southBB) {
                                    this.meta[i] = 3;
                                } else if (aabb2 == this.eastBB) {
                                    this.meta[i] = 5;
                                } else if (aabb2 == this.westBB) {
                                    this.meta[i] = 4;
                                }

                                break;
                            }
                        }
                    }
                } else {
                    double d9 = latchStart.sub(avec3[i], new Vector3f()).length();

                    if (d9 > 0.5D) {
                        this.inblock[i] = false;
                    } else {
                        BlockPos blockpos5 = BlockPos.containing(latchStart.x, latchStart.y, latchStart.z);
                        BlockState blockstate2 = mc.level.getBlockState(blockpos5);
                        this.inblock[i] = this.wasinblock[i] && blockstate2.getBlock() instanceof LadderBlock || blockstate2.getBlock() instanceof VineBlock || blockstate2.is(BlockTags.VIVECRAFT_CLIMBABLE);
                    }
                }

                aboolean[i] = this.inblock[i];
                aboolean1[i] = this.inblock[i];
            } else {
                if (mc.player.onGround()) {
                    mc.player.setOnGround(!this.latched[0] && !this.latched[1]);
                }

                if (i == 0) {
                    aboolean[i] = VivecraftVRMod.keyClimbeyGrab.isDown(ControllerType.RIGHT);
                } else {
                    aboolean[i] = VivecraftVRMod.keyClimbeyGrab.isDown(ControllerType.LEFT);
                }

                this.inblock[i] = this.box[i] != null && this.box[i].move(blockpos).contains(avec3[i].x, avec3[i].y, avec3[i].z);

                if (!this.inblock[i]) {
                    Vector3f vec310 = latchStart.sub(avec3[i], new Vector3f());

                    if (vec310.length() > 0.5F) {
                        aboolean[i] = false;
                    }
                }

                aboolean1[i] = this.allowed(blockstate);
            }

            this.waslatched[i] = this.latched[i];

            if (!aboolean[i] && this.latched[i]) {
                this.latched[i] = false;

                if (i == 0) {
                    VivecraftVRMod.keyClimbeyGrab.unpressKey(ControllerType.RIGHT);
                } else {
                    VivecraftVRMod.keyClimbeyGrab.unpressKey(ControllerType.LEFT);
                }

                flag2 = true;
            }

            if (!this.latched[i] && !flag && aboolean1[i]) {
                if (!this.wasinblock[i] && this.inblock[i]) {
                    dh.vr.triggerHapticPulse(i, 750);
                }

                if (!this.wasinblock[i] && this.inblock[i] && aboolean[i] || !this.wasbutton[i] && aboolean[i] && this.inblock[i]) {
                    flag1 = true;
                    this.wantjump = false;
                    latchStart.set(avec3[i]);
                    dh.vrPlayer.vrdata_room_pre.getController(i).getPosition(latchStartRoom);
                    convertToVector3f(mc.player.position(), latchStartBody);
                    this.latchStartController = i;
                    this.latchbox[i] = this.box[i];
                    this.latched[i] = true;

                    if (i == 0) {
                        this.latched[1] = false;
                        flag = true;
                    } else {
                        this.latched[0] = false;
                    }

                    dh.vr.triggerHapticPulse(i, 2000);
                    ((PlayerExtension) mc.player).vivecraft$stepSound(blockpos, latchStart.x(), latchStart.y(), latchStart.z());

                    if (!flag3) {
                        dh.vrPlayer.blockDust(latchStart.x(), latchStart.y(), latchStart.z(), 5, blockpos, blockstate, 0.1F, 0.2F);
                    }
                }
            }

            this.wasbutton[i] = aboolean[i];
            this.wasinblock[i] = this.inblock[i];
        }

        if (!this.latched[0] && !this.latched[1]) {
            for (int k = 0; k < 2; ++k) {
                if (this.inblock[k] && aboolean[k] && aboolean1[k]) {
                    flag1 = true;
                    final Vector3f latchStart;
                    if (k == 0) {
                        latchStart = latchStartc0.set(avec3[k]);
                        dh.vrPlayer.vrdata_room_pre.getController(k).getPosition(latchStart_roomc0);
                        convertToVector3f(mc.player.position(), latchStartBodyc0);
                    } else {
                        latchStart = latchStartc1.set(avec3[k]);
                        dh.vrPlayer.vrdata_room_pre.getController(k).getPosition(latchStart_roomc1);
                        convertToVector3f(mc.player.position(), latchStartBodyc1);
                    }
                    this.latchStartController = k;
                    this.latched[k] = true;
                    this.latchbox[k] = this.box[k];
                    this.wantjump = false;
                    dh.vr.triggerHapticPulse(k, 2000);
                    BlockPos blockpos4 = BlockPos.containing(latchStart.x, latchStart.y, latchStart.z);
                    BlockState blockstate4 = mc.level.getBlockState(blockpos4);

                    if (!flag3) {
                        dh.vrPlayer.blockDust(latchStart.x, latchStart.y, latchStart.z, 5, blockpos4, blockstate4, 0.1F, 0.2F);
                    }
                }
            }
        }

        if (!this.wantjump && !flag3) {
            this.wantjump = VivecraftVRMod.keyClimbeyJump.isDown() && dh.jumpTracker.isClimbeyJumpEquipped();
        }

        flag2 = flag2 & this.wantjump;

        if ((this.latched[0] || this.latched[1]) && !this.gravityOverride) {
            this.unsetflag = true;
            mc.player.setNoGravity(true);
            this.gravityOverride = true;
        }

        if (!this.latched[0] && !this.latched[1] && this.gravityOverride) {
            mc.player.setNoGravity(false);
            this.gravityOverride = false;
        }

        if (!this.latched[0] && !this.latched[1] && !flag2) {
            if (mc.player.onGround() && this.unsetflag) {
                this.unsetflag = false;
                VivecraftVRMod.keyClimbeyGrab.unpressKey(ControllerType.RIGHT);
                VivecraftVRMod.keyClimbeyGrab.unpressKey(ControllerType.LEFT);
            }

            this.latchStartController = -1;
        } else {
            final Vector3f latchStart;
            final Vector3f latchStartRoom;
            final Vector3f latchStartBody;
            if (latchStartController == 0) {
                latchStart = latchStartc0;
                latchStartRoom = latchStart_roomc0;
            } else {
                latchStart = latchStartc1;
                latchStartRoom = latchStart_roomc1;
            }
            if ((this.latched[0] || this.latched[1]) && this.rand.nextInt(20) == 10) {
                mc.player.causeFoodExhaustion(0.1F);
                BlockPos blockpos3 = BlockPos.containing(latchStart.x, latchStart.y, latchStart.z);
                BlockState blockstate3 = mc.level.getBlockState(blockpos3);

                if (!flag3) {
                    dh.vrPlayer.blockDust(latchStart.x, latchStart.y, latchStart.z, 1, blockpos3, blockstate3, 0.1F, 0.2F);
                }
            }

            Vector3f vec36 = dh.vrPlayer.vrdata_world_pre.getController(this.latchStartController).getPosition(new Vector3f()).sub(VRPlayer.room_to_world_pos(latchStartRoom, dh.vrPlayer.vrdata_world_pre, new Vector3f()));
            dh.vrPlayer.vrdata_room_pre.getController(this.latchStartController).getPosition(latchStartRoom);

            if (this.wantjump) {
                dh.vr.triggerHapticPulse(this.latchStartController, 200);
            }

            if (!flag2) {

                if (flag1) {
                    mc.player.setDeltaMovement(0.0D, 0.0D, 0.0D);
                } else {
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, 0.0D, mc.player.getDeltaMovement().z);
                }

                mc.player.fallDistance = 0.0F;
                double d4 = mc.player.getX();
                double d6 = mc.player.getY();
                double d8 = mc.player.getZ();
                double d10 = d4;
                double d0 = d8;
                double d11 = d6 - vec36.y;
                BlockPos blockpos2 = BlockPos.containing(latchStart.x, latchStart.y, latchStart.z);

                if (!flag3) {
                    d10 = d4 - vec36.x;
                    d0 = d8 - vec36.z;
                } else {
                    int j = this.meta[this.latchStartController];

                    if (j != 2 && j != 3) {
                        if (j == 4 || j == 5) {
                            d0 = d8 - vec36.z;
                            d10 = (float) blockpos2.getX() + 0.5F;
                            d10 += (1.0 - min(dh.vrPlayer.worldScale, 1.0)) * (j == 4 ? 0.5 : -0.5);
                        }
                    } else {
                        d10 = d4 - vec36.x;
                        d0 = (float) blockpos2.getZ() + 0.5F;
                        d0 += (1.0 - min(dh.vrPlayer.worldScale, 1.0)) * (j == 2 ? 0.5 : -0.5);
                    }
                }

                double d12 = dh.vrPlayer.vrdata_room_pre.getHeadPivot(new Vector3f()).y;
                double d1 = dh.vrPlayer.vrdata_room_pre.getController(this.latchStartController).getPosition(new Vector3f()).y;

                if (!this.wantjump && this.latchbox[this.latchStartController] != null && d1 <= d12 / 2.0D && latchStart.y > this.latchbox[this.latchStartController].maxY * 0.8D + (double) blockpos2.getY()) {
                    Vector3f vec32 = dh.vrPlayer.vrdata_world_pre.hmd.getDirection(new Vector3f()).mul(0.1F);
                    vec32.y = 0;
                    vec32.normalize().mul(0.1F);
                    boolean flag5 = mc.level.noCollision(mc.player, mc.player.getBoundingBox().move(vec32.x, this.latchbox[this.latchStartController].maxY + (double) blockpos2.getY() - mc.player.getY(), vec32.z));

                    if (flag5) {
                        d10 = mc.player.getX() + vec32.x;
                        d11 = this.latchbox[this.latchStartController].maxY + (double) blockpos2.getY();
                        d0 = mc.player.getZ() + vec32.z;
                        this.latchStartController = -1;
                        this.latched[0] = false;
                        this.latched[1] = false;
                        this.wasinblock[0] = false;
                        this.wasinblock[1] = false;
                        mc.player.setNoGravity(false);
                    }
                }

                boolean flag6 = false;

                for (int l = 0; l < 8; ++l) {
                    double d13 = d10;
                    double d2 = d11;
                    double d3 = d0;

                    switch (l) {
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

                    mc.player.setPos(d13, d2, d3);
                    AABB aabb1 = mc.player.getBoundingBox();
                    flag6 = mc.level.noCollision(mc.player, aabb1);

                    if (flag6) {
                        if (l > 1) {
                            dh.vr.triggerHapticPulse(0, 100);
                            dh.vr.triggerHapticPulse(1, 100);
                        }

                        break;
                    }
                }

                if (!flag6) {
                    mc.player.setPos(d4, d6, d8);
                    dh.vr.triggerHapticPulse(0, 100);
                    dh.vr.triggerHapticPulse(1, 100);
                }

                if (mc.isLocalServer()) {
                    for (ServerPlayer serverplayer : mc.getSingleplayerServer().getPlayerList().getPlayers()) {
                        if (serverplayer.getId() == mc.player.getId()) {
                            serverplayer.fallDistance = 0.0F;
                        }
                    }
                } else {
                    ServerboundCustomPayloadPacket serverboundcustompayloadpacket = ClientNetworking.getVivecraftClientPacket(PacketDiscriminators.CLIMBING, new byte[0]);

                    if (mc.getConnection() != null) {
                        mc.getConnection().send(serverboundcustompayloadpacket);
                    }
                }
            } else {
                this.wantjump = false;
                Vector3f vec38 = convertToVector3f(mc.player.position(), new Vector3f()).sub(vec36);
                Vector3f vec39 = dh.vr.controllerHistory[this.latchStartController].netMovement(0.3F, new Vector3f());
                float d5 = dh.vr.controllerHistory[this.latchStartController].averageSpeed(0.3F);
                vec39.mul(0.66F * d5);
                float f = 0.66F;

                if (vec39.length() > f) {
                    vec39.mul(f / vec39.length());
                }

                if (mc.player.hasEffect(MobEffects.JUMP)) {
                    vec39.mul(mc.player.getEffect(MobEffects.JUMP).getAmplifier() + 1.5F);
                }

                vec39.rotateY(dh.vrPlayer.vrdata_world_pre.rotation_radians);
                mc.player.setDeltaMovement(-vec39.x, -vec39.y, -vec39.z);
                mc.player.xOld = vec38.x;
                mc.player.yOld = vec38.y;
                mc.player.zOld = vec38.z;
                mc.player.setPos(
                    vec38.x + mc.player.getDeltaMovement().x,
                    vec38.y + mc.player.getDeltaMovement().y,
                    vec38.z + mc.player.getDeltaMovement().z
                );
                dh.vrPlayer.snapRoomOriginToPlayerEntity(false, false);
                mc.player.causeFoodExhaustion(0.3F);
            }
        }
    }

    private boolean allowed(BlockState bs) {
        if (this.serverblockmode == 0) {
            return true;
        } else if (this.serverblockmode == 1) {
            return this.blocklist.contains(bs.getBlock());
        } else if (this.serverblockmode == 2) {
            return !this.blocklist.contains(bs.getBlock());
        } else {
            return false;
        }
    }
}
