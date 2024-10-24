package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.BlockTags;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.common.network.packet.c2s.ClimbingPayloadC2S;
import org.vivecraft.server.config.ClimbeyBlockmode;

import java.util.*;

public class ClimbTracker extends Tracker {
    public static final ModelResourceLocation clawsModel = new ModelResourceLocation("vivecraft", "climb_claws", "inventory");
    public Set<Block> blocklist = new HashSet<>();
    public ClimbeyBlockmode serverBlockmode = ClimbeyBlockmode.DISABLED;
    public boolean forceActivate = false;
    public int latchStartController = -1;
    public Vec3[] latchStart = new Vec3[]{new Vec3(0.0D, 0.0D, 0.0D), new Vec3(0.0D, 0.0D, 0.0D)};
    public Vec3[] latchStart_room = new Vec3[]{new Vec3(0.0D, 0.0D, 0.0D), new Vec3(0.0D, 0.0D, 0.0D)};
    public Vec3[] latchStartBody = new Vec3[]{new Vec3(0.0D, 0.0D, 0.0D), new Vec3(0.0D, 0.0D, 0.0D)};

    private boolean gravityOverride = false;
    private boolean wantJump = false;
    private final boolean[] latched = new boolean[2];
    private final boolean[] wasInBlock = new boolean[2];
    private final boolean[] wasButton = new boolean[2];
    private final boolean[] wasLatched = new boolean[2];
    private final AABB[] box = new AABB[2];
    private final AABB[] latchBox = new AABB[2];
    private final boolean[] inBlock = new boolean[2];

    /**
     * 2: Ladder facing north
     * 3: Ladder facing south
     * 4: Ladder facing west
     * 5: Ladder facing east
     */
    private final Direction[] grabDirection = new Direction[2];
    private final AABB northBB = new AABB(0.1D, 0.0D, 0.9D, 0.9D, 1.0D, 1.1D);
    private final AABB southBB = new AABB(0.1D, 0.0D, -0.1D, 0.9D, 1.0D, 0.1D);
    private final AABB westBB = new AABB(0.9D, 0.0D, 0.1D, 1.1D, 1.0D, 0.9D);
    private final AABB eastBB = new AABB(-0.1D, 0.0D, 0.1D, 0.1D, 1.0D, 0.9D);
    private final AABB upBB = new AABB(0.0D, 0.9D, 0.0D, 1.0D, 1.1D, 1.0D);
    private final AABB fullBB = new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    private final Random rand = new Random();
    boolean unsetFlag;

    public ClimbTracker(Minecraft mc, ClientDataHolderVR dh) {
        super(mc, dh);
    }

    public boolean isGrabbingLadder() {
        return this.latched[0] || this.latched[1];
    }

    public boolean wasGrabbingLadder() {
        return this.wasLatched[0] || this.latched[1];
    }

    public boolean isGrabbingLadder(int controller) {
        return this.latched[controller];
    }

    public boolean wasGrabbingLadder(int controller) {
        return this.wasLatched[controller];
    }

    /**
     * @return if the main Minecraft Player can use climbing claws
     */
    public boolean isClimbeyClimb() {
        return this.isActive(this.mc.player) && hasClimbeyClimbEquipped(this.mc.player);
    }

    /**
     * @param player Player to check
     * @return if the given {@code player} has a climbing claw item in either hand
     */
    public static boolean hasClimbeyClimbEquipped(Player player) {
        return ClientNetworking.serverAllowsClimbey &&
            (isClaws(player.getMainHandItem()) || isClaws(player.getOffhandItem()));
    }

    /**
     * @param itemStack ItemStack to check
     * @return if the given {@code itemStack} is a climbing claw item
     */
    public static boolean isClaws(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        } else if (!itemStack.hasCustomHoverName()) {
            return false;
        } else if (itemStack.getItem() != Items.SHEARS) {
            return false;
        } else if (!itemStack.hasTag() || !itemStack.getTag().getBoolean("Unbreakable")) {
            return false;
        } else {
            return itemStack.getHoverName().getString().equals("Climb Claws") ||
                (itemStack.getHoverName().getContents() instanceof TranslatableContents translatableContent &&
                    translatableContent.getKey().equals("vivecraft.item.climbclaws")
                );
        }
    }

    private static boolean canStand(BlockPos blockPos, LocalPlayer player) {
        VoxelShape blockShape = player.level().getBlockState(blockPos).getCollisionShape(player.level(), blockPos);
        if (blockShape.isEmpty() || blockShape.bounds().maxY != 0.0D) {
            BlockPos above = blockPos.above();
            VoxelShape aboveBlockShape = player.level().getBlockState(above).getCollisionShape(player.level(), above);
            if (aboveBlockShape.isEmpty() || aboveBlockShape.bounds().maxY > 0.0D) {
                return false;
            } else {
                BlockPos above2 = above.above();
                VoxelShape above2BlockShape = player.level().getBlockState(above2).getCollisionShape(player.level(), above2);
                return above2BlockShape.isEmpty() || above2BlockShape.bounds().maxY <= 0.0D;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean isActive(LocalPlayer player) {
        if (this.dh.vrSettings.seated) {
            return false;
        } else if (!this.dh.vrPlayer.getFreeMove() && !this.dh.vrSettings.simulateFalling) {
            return false;
        } else if (!this.dh.vrSettings.realisticClimbEnabled) {
            return false;
        } else if (player == null && !player.isAlive()) {
            return false;
        } else if (this.mc.gameMode == null) {
            return false;
        } else if (player.isPassenger()) {
            return false;
        } else if (!hasClimbeyClimbEquipped(player) && (player.zza != 0 || player.xxa != 0)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void idleTick(LocalPlayer player) {
        if (!this.isActive(player)) {
            this.wasLatched[0] = false;
            this.wasLatched[1] = false;
        }

        if (this.wasGrabbingLadder() && !this.isGrabbingLadder()) {
            this.forceActivate = true;
        } else if (player.onGround() || player.getAbilities().flying) {
            this.forceActivate = false;
        }

        this.dh.vr.getInputAction(VivecraftVRMod.INSTANCE.keyClimbeyGrab)
            .setEnabled(ControllerType.RIGHT, this.isClimbeyClimb() &&
                (this.isGrabbingLadder() || this.inBlock[0] || this.forceActivate));
        this.dh.vr.getInputAction(VivecraftVRMod.INSTANCE.keyClimbeyGrab)
            .setEnabled(ControllerType.LEFT, this.isClimbeyClimb() &&
                (this.isGrabbingLadder() || this.inBlock[1] || this.forceActivate));
    }

    @Override
    public void reset(LocalPlayer player) {
        this.latchStartController = -1;
        this.latched[0] = false;
        this.latched[1] = false;
        player.setNoGravity(false);
    }

    @Override
    public void doProcess(LocalPlayer player) {
        boolean[] button = new boolean[2];
        boolean[] allowed = new boolean[2];
        Vec3[] controllerPos = new Vec3[2];

        boolean nope = false; // only one hand can grab at the same time
        boolean grabbed = false;
        boolean jump = false;
        boolean ladder = false;

        for (int c = 0; c < 2; c++) {
            controllerPos[c] = this.dh.vrPlayer.vrdata_world_pre.getController(c).getPosition();
            Vec3 controllerDir = this.dh.vrPlayer.vrdata_world_pre.getController(c).getDirection();
            this.inBlock[c] = false;

            BlockPos blockPos = BlockPos.containing(controllerPos[c]);
            BlockState blockState = this.mc.level.getBlockState(blockPos);
            Block block = blockState.getBlock();
            VoxelShape voxelShape = blockState.getCollisionShape(this.mc.level, blockPos);

            if (voxelShape.isEmpty()) {
                this.box[c] = null;
            } else {
                this.box[c] = voxelShape.bounds();
            }

            if (!this.dh.climbTracker.isClimbeyClimb()) {
                // roomscale climbable
                Vec3 controllerPosNear = this.dh.vrPlayer.vrdata_world_pre.getController(c).getPosition().subtract(controllerDir.scale(0.2D));
                AABB controllerBB = new AABB(controllerPos[c], controllerPosNear);
                ladder = true;
                boolean ok = block instanceof LadderBlock ||
                    block instanceof VineBlock ||
                    blockState.is(BlockTags.VIVECRAFT_CLIMBABLE);

                if (!ok) { //check other end of controllerBB.
                    BlockPos blockPos2 = BlockPos.containing(controllerPosNear);
                    BlockState blockState2 = this.mc.level.getBlockState(blockPos2);
                    Block block2 = blockState2.getBlock();

                    if (block2 instanceof LadderBlock || block2 instanceof VineBlock || blockState2.is(BlockTags.VIVECRAFT_CLIMBABLE)) {
                        blockPos = blockPos2;
                        blockState = blockState2;
                        block = block2;
                        controllerPos[c] = controllerPosNear;
                        VoxelShape voxelShape2 = blockState2.getCollisionShape(this.mc.level, blockPos2);

                        if (voxelShape2.isEmpty()) {
                            this.box[c] = null;
                        } else {
                            ok = true;
                            this.box[c] = voxelShape2.bounds();
                        }
                    }
                }

                if (ok) {
                    List<AABB> BBs = new ArrayList<>();

                    if (block instanceof LadderBlock) {
                        switch (blockState.getValue(LadderBlock.FACING)) {
                            case DOWN -> ok = false; //Marilyn??
                            case EAST -> BBs.add(this.eastBB);
                            case NORTH -> BBs.add(this.northBB);
                            case SOUTH -> BBs.add(this.southBB);
                            case UP -> BBs.add(this.upBB); //Where the shit did you find a jungle gym?
                            case WEST -> BBs.add(this.westBB);
                            default -> ok = false;
                        }
                    }

                    if (block instanceof VineBlock) {
                        this.box[c] = new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

                        // Not vanilla-y to allow climbing on top vines.
                        //if (blockState.getValue(VineBlock.UP) && this.mc.level.getBlockState(blockPos.above()).canOcclude())
                        //	bbs.add(upBB);

                        if (blockState.getValue(VineBlock.NORTH) && this.mc.level.getBlockState(blockPos.north()).canOcclude()) {
                            BBs.add(this.southBB);
                        }

                        if (blockState.getValue(VineBlock.EAST) && this.mc.level.getBlockState(blockPos.east()).canOcclude()) {
                            BBs.add(this.westBB);
                        }

                        if (blockState.getValue(VineBlock.SOUTH) && this.mc.level.getBlockState(blockPos.south()).canOcclude()) {
                            BBs.add(this.northBB);
                        }

                        if (blockState.getValue(VineBlock.WEST) && this.mc.level.getBlockState(blockPos.west()).canOcclude()) {
                            BBs.add(this.eastBB);
                        }
                    }

                    this.inBlock[c] = false;

                    if (ok) {
                        for (AABB aabb2 : BBs) {
                            if (controllerBB.intersects(aabb2.move(blockPos))) {
                                this.inBlock[c] = true;
                                if (aabb2 == this.northBB) {
                                    this.grabDirection[c] = Direction.NORTH;
                                } else if (aabb2 == this.southBB) {
                                    this.grabDirection[c] = Direction.SOUTH;
                                } else if (aabb2 == this.eastBB) {
                                    this.grabDirection[c] = Direction.EAST;
                                } else if (aabb2 == this.westBB) {
                                    this.grabDirection[c] = Direction.WEST;
                                }
                                break;
                            }
                        }
                    }
                } else {
                    Vec3 handToLatch = this.latchStart[c].subtract(controllerPos[c]);
                    if (handToLatch.length() > 0.5D) {
                        this.inBlock[c] = false;
                    } else {
                        BlockPos latchBlockPos = BlockPos.containing(this.latchStart[c]);
                        BlockState latchBlockState = this.mc.level.getBlockState(latchBlockPos);
                        this.inBlock[c] = this.wasInBlock[c] &&
                            latchBlockState.getBlock() instanceof LadderBlock ||
                            latchBlockState.getBlock() instanceof VineBlock ||
                            latchBlockState.is(BlockTags.VIVECRAFT_CLIMBABLE);
                    }
                }

                button[c] = this.inBlock[c];
                allowed[c] = this.inBlock[c];
            } else {
                // Climbey
                if (this.mc.player.onGround()) {
                    this.mc.player.setOnGround(!this.latched[0] && !this.latched[1]);
                }

                if (c == 0) {
                    button[c] = VivecraftVRMod.INSTANCE.keyClimbeyGrab.isDown(ControllerType.RIGHT);
                } else {
                    button[c] = VivecraftVRMod.INSTANCE.keyClimbeyGrab.isDown(ControllerType.LEFT);
                }

                this.inBlock[c] = this.box[c] != null && this.box[c].move(blockPos).contains(controllerPos[c]);

                if (!this.inBlock[c]) {
                    Vec3 handToLatch = this.latchStart[c].subtract(controllerPos[c]);
                    if (handToLatch.length() > 0.5D) {
                        button[c] = false;
                    }
                }

                allowed[c] = this.allowed(blockState);
            }

            this.wasLatched[c] = this.latched[c];

            if (!button[c] && this.latched[c]) {
                // let go
                this.latched[c] = false;

                if (c == 0) {
                    VivecraftVRMod.INSTANCE.keyClimbeyGrab.unpressKey(ControllerType.RIGHT);
                } else {
                    VivecraftVRMod.INSTANCE.keyClimbeyGrab.unpressKey(ControllerType.LEFT);
                }

                jump = true;
            }

            if (!this.latched[c] && !nope && allowed[c]) {
                // grab
                if (!this.wasInBlock[c] && this.inBlock[c]) {
                    // indicate can grab.
                    this.dh.vr.triggerHapticPulse(c, 750);
                }

                if ((!this.wasInBlock[c] && this.inBlock[c] && button[c]) ||
                    (!this.wasButton[c] && button[c] && this.inBlock[c])) {
                    // Grab
                    grabbed = true;
                    this.wantJump = false;
                    this.latchStart[c] = controllerPos[c];
                    this.latchStart_room[c] = this.dh.vrPlayer.vrdata_room_pre.getController(c).getPosition();
                    this.latchStartBody[c] = player.position();
                    this.latchStartController = c;
                    this.latchBox[c] = this.box[c];
                    this.latched[c] = true;

                    if (c == 0) {
                        this.latched[1] = false;
                        nope = true;
                    } else {
                        this.latched[0] = false;
                    }

                    this.dh.vr.triggerHapticPulse(c, 2000);
                    ((PlayerExtension) this.mc.player).vivecraft$stepSound(blockPos, this.latchStart[c]);

                    if (!ladder) { // dust when climbey grabbing
                        this.dh.vrPlayer.blockDust(this.latchStart[c].x, this.latchStart[c].y, this.latchStart[c].z, 5, blockPos, blockState, 0.1F, 0.2F);
                    }
                }
            }

            this.wasButton[c] = button[c];
            this.wasInBlock[c] = this.inBlock[c];
        }

        if (!this.latched[0] && !this.latched[1]) {
            // check in case they let go with one hand, and other hand should take over.
            for (int c = 0; c < 2; c++) {
                if (this.inBlock[c] && button[c] && allowed[c]) {
                    grabbed = true;
                    this.latchStart[c] = controllerPos[c];
                    this.latchStart_room[c] = this.dh.vrPlayer.vrdata_room_pre.getController(c).getPosition();
                    this.latchStartBody[c] = player.position();
                    this.latchStartController = c;
                    this.latched[c] = true;
                    this.latchBox[c] = this.box[c];
                    this.wantJump = false;
                    this.dh.vr.triggerHapticPulse(c, 2000);

                    if (!ladder) {
                        BlockPos blockPos = BlockPos.containing(this.latchStart[c]);
                        BlockState blockState = this.mc.level.getBlockState(blockPos);
                        this.dh.vrPlayer.blockDust(this.latchStart[c].x, this.latchStart[c].y, this.latchStart[c].z, 5, blockPos, blockState, 0.1F, 0.2F);
                    }
                }
            }
        }

        if (!this.wantJump && !ladder) {
            this.wantJump = VivecraftVRMod.INSTANCE.keyClimbeyJump.isDown() && JumpTracker.hasClimbeyJumpEquipped(player);
        }

        jump &= this.wantJump;

        if ((this.latched[0] || this.latched[1]) && !this.gravityOverride) {
            this.unsetFlag = true;
            player.setNoGravity(true);
            this.gravityOverride = true;
        }

        if (!this.latched[0] && !this.latched[1] && this.gravityOverride) {
            player.setNoGravity(false);
            this.gravityOverride = false;
        }

        if (!this.latched[0] && !this.latched[1] && !jump) {
            if (player.onGround() && this.unsetFlag) {
                this.unsetFlag = false;
                VivecraftVRMod.INSTANCE.keyClimbeyGrab.unpressKey(ControllerType.RIGHT);
                VivecraftVRMod.INSTANCE.keyClimbeyGrab.unpressKey(ControllerType.LEFT);
            }
            this.latchStartController = -1;
            return; //fly u fools
        }

        if ((this.latched[0] || this.latched[1]) && this.rand.nextInt(20) == 10) {
            this.mc.player.causeFoodExhaustion(0.1F);

            if (!ladder) {
                BlockPos blockPos = BlockPos.containing(this.latchStart[this.latchStartController]);
                BlockState blockState = this.mc.level.getBlockState(blockPos);
                this.dh.vrPlayer.blockDust(this.latchStart[this.latchStartController].x, this.latchStart[this.latchStartController].y, this.latchStart[this.latchStartController].z, 1, blockPos, blockState, 0.1F, 0.2F);
            }
        }

        Vec3 now = this.dh.vrPlayer.vrdata_world_pre.getController(this.latchStartController).getPosition();
        Vec3 start = VRPlayer.room_to_world_pos(this.latchStart_room[this.latchStartController], this.dh.vrPlayer.vrdata_world_pre);

        Vec3 delta = now.subtract(start);

        this.latchStart_room[this.latchStartController] = this.dh.vrPlayer.vrdata_room_pre.getController(this.latchStartController).getPosition();

        if (this.wantJump) {
            // bzzzzzz
            this.dh.vr.triggerHapticPulse(this.latchStartController, 200);
        }

        if (!jump) {
            if (grabbed) {
                player.setDeltaMovement(0.0D, 0.0D, 0.0D);
            } else {
                player.setDeltaMovement(player.getDeltaMovement().x, 0.0D, player.getDeltaMovement().z);
            }

            player.fallDistance = 0.0F;

            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();

            double newX = x;
            double newY = y - delta.y;
            double newZ = z;

            BlockPos blockPos = BlockPos.containing(this.latchStart[this.latchStartController]);

            if (!ladder) {
                newX = x - delta.x;
                newZ = z - delta.z;
            } else {
                Direction dir = this.grabDirection[this.latchStartController];

                if (dir == Direction.NORTH || dir == Direction.SOUTH) {
                    // allow sideways
                    newX = x - delta.x;
                    newZ = (float) blockPos.getZ() + 0.5F;
                    // move player closer to wall, with small world scale
                    newZ += (1.0 - Math.min(this.dh.vrPlayer.worldScale, 1.0)) * (dir == Direction.NORTH ? 0.5 : -0.5);
                } else if (dir == Direction.EAST || dir == Direction.WEST) {
                    // allow sideways
                    newZ = z - delta.z;
                    newX = (float) blockPos.getX() + 0.5F;
                    // move player closer to wall, with small world scale
                    newX += (1.0 - Math.min(this.dh.vrPlayer.worldScale, 1.0)) * (dir == Direction.WEST ? 0.5 : -0.5);
                }
            }

            double hmd = this.dh.vrPlayer.vrdata_room_pre.getHeadPivot().y;
            double controller = this.dh.vrPlayer.vrdata_room_pre.getController(this.latchStartController).getPosition().y;

            // check for getting off on top
            if (!this.wantJump && // not jumping
                this.latchBox[this.latchStartController] != null && // uhh why?
                controller <= hmd / 2.0D && // hands down below waist
                this.latchStart[this.latchStartController].y > this.latchBox[this.latchStartController].maxY * 0.8D + blockPos.getY() // latched onto top 20% of block
            ) {
                Vec3 dir = this.dh.vrPlayer.vrdata_world_pre.hmd.getDirection().scale(0.1F);
                Vec3 horizontalDir = (new Vec3(dir.x, 0.0D, dir.z)).normalize().scale(0.1D); // check if free spot

                boolean ok = this.mc.level.noCollision(player, player.getBoundingBox().move(horizontalDir.x, this.latchBox[this.latchStartController].maxY + (double) blockPos.getY() - player.getY(), horizontalDir.z));

                if (ok) {
                    newX = player.getX() + horizontalDir.x;
                    newY = this.latchBox[this.latchStartController].maxY + (double) blockPos.getY();
                    newZ = player.getZ() + horizontalDir.z;
                    this.latchStartController = -1;
                    this.latched[0] = false;
                    this.latched[1] = false;
                    this.wasInBlock[0] = false;
                    this.wasInBlock[1] = false;
                    player.setNoGravity(false);
                }
            }

            boolean free = false;

            for (int i = 0; i < 8; i++) {
                double ax = newX;
                double ay = newY;
                double az = newZ;

                switch (i) {
                    case 2 -> ay = y;
                    case 3 -> az = z;
                    case 4 -> ax = x;
                    case 5 -> {
                        ax = x;
                        az = z;
                    }
                    case 6 -> {
                        ax = x;
                        ay = y;
                    }
                    case 7 -> {
                        ay = y;
                        az = z;
                    }
                    default -> {} // 0 and 1 do the same?
                }

                player.setPos(ax, ay, az);
                AABB bb = player.getBoundingBox();
                free = this.mc.level.noCollision(player, bb);

                if (free) {
                    if (i > 1) {
                        // ouch!
                        this.dh.vr.triggerHapticPulse(0, 100);
                        this.dh.vr.triggerHapticPulse(1, 100);
                    }

                    break;
                }
            }

            if (!free) {
                player.setPos(x, y, z);
                // ouch!
                this.dh.vr.triggerHapticPulse(0, 100);
                this.dh.vr.triggerHapticPulse(1, 100);
            }

            if (this.mc.isLocalServer()) {
                // handle server falling.
                for (ServerPlayer serverplayer : this.mc.getSingleplayerServer().getPlayerList().getPlayers()) {
                    if (serverplayer.getId() == this.mc.player.getId()) {
                        serverplayer.fallDistance = 0.0F;
                    }
                }
            } else {
                if (this.mc.getConnection() != null) {
                    this.mc.getConnection().send(ClientNetworking.createServerPacket(new ClimbingPayloadC2S()));
                }
            }
        } else {
            // jump!
            this.wantJump = false;
            Vec3 p1 = player.position().subtract(delta);
            Vec3 movement = this.dh.vr.controllerHistory[this.latchStartController].netMovement(0.3D);
            double speed = this.dh.vr.controllerHistory[this.latchStartController].averageSpeed(0.3F);
            movement = movement.scale(0.66D * speed);

            final float limit = 0.66F;

            if (movement.length() > limit) {
                movement = movement.scale(limit / movement.length());
            }

            if (player.hasEffect(MobEffects.JUMP)) {
                movement = movement.scale(player.getEffect(MobEffects.JUMP).getAmplifier() + 1.5D);
            }

            movement = movement.yRot(this.dh.vrPlayer.vrdata_world_pre.rotation_radians);
            player.setDeltaMovement(movement.multiply(-1.0D, -1.0D, -1.0D));
            player.xOld = p1.x;
            player.yOld = p1.y;
            player.zOld = p1.z;
            p1 = p1.add(player.getDeltaMovement().x, player.getDeltaMovement().y, player.getDeltaMovement().z);
            player.setPos(p1.x, p1.y, p1.z);
            this.dh.vrPlayer.snapRoomOriginToPlayerEntity(player, false, false);
            this.mc.player.causeFoodExhaustion(0.3F);
        }
    }

    private boolean allowed(BlockState bs) {
        return switch(this.serverBlockmode) {
            case DISABLED -> true;
            case WHITELIST -> this.blocklist.contains(bs.getBlock());
            case BLACKLIST -> !this.blocklist.contains(bs.getBlock());
        };
    }
}
