package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.PiercingWeapon;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.api.client.ItemInUseTracker;
import org.vivecraft.api.data.FBTMode;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.Vector3fHistory;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.render.helpers.DebugRenderHelper;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.MathUtils;
import org.vivecraft.common.utils.Utils;
import org.vivecraft.data.ViveBlockTags;
import org.vivecraft.data.ViveItemTags;
import org.vivecraft.mod_compat_vr.epicfight.EpicFightHelper;

import java.util.*;

public class SwingTracker implements ItemInUseTracker, DebugRenderTracker {
    private static final int[] CONTROLLER_AND_FEET = new int[]{MCVR.MAIN_CONTROLLER, MCVR.OFFHAND_CONTROLLER, MCVR.RIGHT_FOOT_TRACKER, MCVR.LEFT_FOOT_TRACKER};
    private static final VRBodyPart[] BODYPARTS = new VRBodyPart[]{VRBodyPart.MAIN_HAND, VRBodyPart.OFF_HAND, VRBodyPart.RIGHT_FOOT, VRBodyPart.LEFT_FOOT};
    // at a 0.3m offset on index controllers a speed of 2.5m/s is an intended smack, 7 m/s is about as high as your arm can go.
    private static final float SPEED_THRESH = 2.5F;

    public int disableSwing = 3;

    private final Quaternionf[] lastHandRot = new Quaternionf[4];
    private final Vec3[] lastHandPos = new Vec3[4];
    private final List<Vec3>[] miningPoints = new List[4];

    private final boolean[] lastWeaponSolid = new boolean[4];

    private final List<Entity>[] lastHitEntities = new List[]{Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList()};

    private final Vec3[] miningPoint = new Vec3[4];
    private final Vec3[] attackingPoint = new Vec3[4];
    private final Vec3[] weaponTip = new Vec3[4];
    private final Vector3fHistory[] tipHistory = new Vector3fHistory[]{new Vector3fHistory(), new Vector3fHistory(), new Vector3fHistory(), new Vector3fHistory()};
    private final Vector3fHistory[] tipDirHistory = new Vector3fHistory[]{new Vector3fHistory(), new Vector3fHistory(), new Vector3fHistory(), new Vector3fHistory()};
    private final Vector3fHistory[] baseHistory = new Vector3fHistory[]{new Vector3fHistory(), new Vector3fHistory(), new Vector3fHistory(), new Vector3fHistory()};
    private final boolean[] canAct = new boolean[4];

    // brushes need to be held 5 ticks, to trigger the server side
    private final int[] useHoldTicks = new int[4];

    // debug render stuff
    private final AABB[] lastAttackAABB = new AABB[4];
    private final Vec3[] lastBlockHit = new Vec3[4];
    private final int[] lastMiningPointHit = new int[4];
    private final List<Pair<Vec3, Integer>>[] previousMiningPoints = new List[]{new LinkedList<>(), new LinkedList<>(), new LinkedList<>(), new LinkedList<>()};

    private final Minecraft mc;
    private final ClientDataHolderVR dh;

    public SwingTracker(Minecraft mc, ClientDataHolderVR dh) {
        this.mc = mc;
        this.dh = dh;
    }

    @Override
    public boolean isActive(LocalPlayer player) {
        if (this.disableSwing > 0) {
            this.disableSwing--;
            return false;
        } else if (this.mc.gameMode == null) {
            return false;
        } else if (player == null) {
            return false;
        } else if (!player.isAlive()) {
            return false;
        } else if (player.isSleeping()) {
            return false;
        } else if (this.mc.screen != null) {
            return false;
        } else if (this.dh.vrSettings.weaponCollision == VRSettings.WeaponCollision.OFF) {
            return false;
        } else if (this.dh.vrSettings.weaponCollision == VRSettings.WeaponCollision.AUTO && player.isCreative()) {
            return false;
        } else if (this.dh.vrSettings.seated) {
            return false;
        } else if (this.dh.vrSettings.getVrFreeMoveMode(false) == VRSettings.FreeMove.RUN_IN_PLACE &&
            player.zza > 0.0F)
        {
            return false; // don't hit things while RIPing.
        } else if (player.isBlocking() && !ClientNetworking.SERVER_ALLOWS_ATTACKING_WHILE_BLOCKING) {
            return false; // don't hit things while blocking.
        } else {
            return !this.dh.jumpTracker.isjumping();
        }
    }

    /**
     * @param itemStack ItemStack to check
     * @return if the given {@code itemStack} is a Tool
     */
    public static boolean isTool(ItemStack itemStack) {
        return isToolItem(itemStack.getItem()) ||
            itemStack.is(ViveItemTags.VIVECRAFT_TOOLS) ||
            // also check the vanilla tags, when on a server without vivecraft
            itemStack.is(ItemTags.PICKAXES) ||
            itemStack.is(ItemTags.AXES) ||
            itemStack.is(ItemTags.SHOVELS) ||
            itemStack.is(ItemTags.HOES);
    }

    private static boolean isToolItem(Item item) {
        return item instanceof ShovelItem ||
            item instanceof HoeItem ||
            item instanceof AxeItem ||
            item instanceof ArrowItem ||
            item instanceof FishingRodItem ||
            item instanceof FoodOnAStickItem ||
            item instanceof ShearsItem ||
            item == Items.BONE ||
            item == Items.BLAZE_ROD ||
            item == Items.BAMBOO ||
            item == Items.TORCH ||
            item == Items.REDSTONE_TORCH ||
            item == Items.STICK ||
            item == Items.DEBUG_STICK ||
            item instanceof FlintAndSteelItem ||
            item instanceof BrushItem;
    }

    @Override
    public ProcessType processType() {
        return ProcessType.PER_TICK;
    }

    @Override
    public void inactiveProcess(LocalPlayer player) {
        for (int i = 0; i < 4; i++) {
            this.lastHandPos[i] = null;
            this.attackingPoint[i] = null;
            this.weaponTip[i] = null;
            this.miningPoints[i] = null;
            this.useHoldTicks[i] = 0;
        }
    }

    @Override
    public boolean itemInUse(LocalPlayer player) {
        return Arrays.stream(this.useHoldTicks).anyMatch(useHoldTick -> useHoldTick > 0);
    }

    @Override
    public void activeProcess(LocalPlayer player) {
        float speedTreshhold = SPEED_THRESH;

        if (player.isCreative()) {
            speedTreshhold *= 1.5F;
        }

        Profiler.get().push("updateSwingAttack");

        int trackers = 2;

        if (this.dh.vrSettings.feetCollision && this.dh.vrPlayer.vrdata_world_pre.fbtMode != FBTMode.ARMS_ONLY) {
            trackers = 4;
        }

        for (int i = 0; i < trackers; i++) {
            if (this.useHoldTicks[i] > 0) {
                this.useHoldTicks[i]--;
            }
            int c = CONTROLLER_AND_FEET[i];
            boolean isHand = i < 2;
            if (!isHand || !this.dh.climbTracker.isGrabbingLadder(c)) {
                Vec3 handPos = this.dh.vrPlayer.vrdata_world_pre.getDevice(c).getPosition();
                Vector3f handDirection = this.dh.vrPlayer.vrdata_world_pre.getHand(c).getCustomVector(MathUtils.BACK);
                ItemStack itemstack = player.getItemInHand(
                    c == MCVR.OFFHAND_CONTROLLER ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
                Item item = itemstack.getItem();
                boolean isTool = false;
                boolean isSword = false;
                boolean isLance = false;

                if (this.dh.vrSettings.onlySwordCollision &&
                    !(itemstack.is(ItemTags.SWORDS) || itemstack.is(ViveItemTags.VIVECRAFT_SWORDS)))
                {
                    // only swords can hit
                    continue;
                }

                if (itemstack.is(ItemTags.SWORDS) || itemstack.is(ViveItemTags.VIVECRAFT_SWORDS) ||
                    item instanceof TridentItem || itemstack.is(ViveItemTags.VIVECRAFT_SPEARS))
                {
                    isSword = true;
                    isTool = true;
                } else if (itemstack.is(ItemTags.SPEARS) || itemstack.is(ViveItemTags.VIVECRAFT_LANCES)) {
                    isLance = true;
                    isTool = true;
                } else if (isTool(itemstack)) {
                    isTool = true;
                }

                float weaponLength = 0.0F;
                float entityReachAdd = 0.3F;

                AttackRange range = itemstack.get(DataComponents.ATTACK_RANGE);

                if (isHand) {
                    double playerEntityReach =
                        range != null ? range.effectiveMaxRange(player) : player.entityInteractionRange();

                    // subtract arm length and clamp it to 6 meters
                    playerEntityReach = Math.min(playerEntityReach, 6.0) - 0.5;

                    if (isSword || isLance) {
                        weaponLength = 0.6F;
                        // in default situations a total reach of 2.5
                        entityReachAdd = (float) playerEntityReach - weaponLength;
                    } else if (isTool) {
                        weaponLength = 0.35F;
                        // in default situations a total reach of 1.55
                        entityReachAdd = (float) playerEntityReach * 0.62F - weaponLength;
                    } else if (!itemstack.isEmpty()) {
                        weaponLength = 0.1F;
                        // in default situations a total reach of 0.4
                        entityReachAdd = (float) playerEntityReach * 0.16F - weaponLength;
                    }
                    entityReachAdd += range != null ? range.hitboxMargin() : 0.0F;
                }

                weaponLength *= this.dh.vrPlayer.vrdata_world_pre.worldScale;

                // remember previous pos
                Vec3 prevMiningPoint = this.miningPoint[i];

                Vector3f weaponEnd = handDirection.mul(weaponLength, new Vector3f());
                this.miningPoint[i] = handPos.add(weaponEnd.x, weaponEnd.y, weaponEnd.z);

                // do speed calc in actual room coords
                Vector3f base = this.dh.vrPlayer.vrdata_room_pre.getDevice(c).getPositionF();
                Vector3f tip = this.dh.vrPlayer.vrdata_room_pre.getHand(c).getCustomVector(MathUtils.BACK).mul(0.3F)
                    .add(base);
                this.baseHistory[i].add(base);
                this.tipHistory[i].add(tip);
                this.tipDirHistory[i].add(isTool ? this.dh.vrPlayer.vrdata_room_pre.getHand(c).getDirection() :
                    this.dh.vrPlayer.vrdata_room_pre.getDevice(c).getDirection());

                float speed = this.tipHistory[i].averageSpeed(0.33D);

                boolean inAnEntity = false;
                this.canAct[i] =
                    speed > speedTreshhold && !this.lastWeaponSolid[i] && !player.cannotAttackWithItem(itemstack, 0);

                boolean canLunge = false;
                Vector3f deviceDirection = this.tipDirHistory[i].averagePosition(0.33D).normalize();
                // roomscale spear lunge
                if (this.canAct[i] && isLance && this.dh.vrSettings.roomscaleSpearLunge) {
                    // this is on purpose not checking averageSpeed, to get the linear speed
                    Vector3f baseTravelDirection = this.baseHistory[i].netMovement(0.33D);
                    // divide by time to make it m/s
                    baseTravelDirection.div(0.33F);
                    // speed in the pointing direction
                    float dotSpeed = Math.max(baseTravelDirection.dot(deviceDirection), 0F);
                    // check if the item has a lunge that could be triggered
                    canLunge = dotSpeed > speedTreshhold && isLance && player.hasEnoughFoodToDoExhaustiveManoeuvres() &&
                        EnchantmentHelper.getItemEnchantmentLevel(
                            player.level().registryAccess().getOrThrow(Enchantments.LUNGE), itemstack) > 0 &&
                        !player.isPassenger() && !player.isFallFlying() && !player.isInWater();
                }

                // Check EntityCollisions first
                boolean entityAct = this.canAct[i];

                // no hitting around corners, to not trigger anticheat
                if (entityAct) {
                    BlockHitResult blockhitresult = this.mc.level.clip(
                        new ClipContext(this.dh.vrPlayer.vrdata_world_pre.hmd.getPosition(), handPos,
                            ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this.mc.player));

                    if (blockhitresult.getType() != HitResult.Type.MISS) {
                        entityAct = false;
                    }
                }

                Vec3 lastAttackPoint = this.attackingPoint[i];
                Vec3 lastWeaponTip = this.weaponTip[i];
                this.attackingPoint[i] = this.constrain(handPos, this.miningPoint[i]);

                Vector3f weaponEntityEnd = handDirection.mul(weaponLength + entityReachAdd, new Vector3f());
                this.weaponTip[i] = handPos.add(weaponEntityEnd.x, weaponEntityEnd.y, weaponEntityEnd.z);
                // no hitting through blocks
                this.weaponTip[i] = this.constrain(handPos, this.weaponTip[i]);

                // -0.5 to subtract arm length
                float weaponStartOffset = range != null ?
                    Math.max(0.0F, range.effectiveMinRange(player) - range.hitboxMargin() - 0.5F) :
                    0.0F;
                Vector3fc weaponEntityStart = handDirection.mul(weaponStartOffset, new Vector3f());
                Vec3 entityStartPos = this.constrain(handPos,
                    handPos.add(weaponEntityStart.x(), weaponEntityStart.y(), weaponEntityStart.z()));

                AABB weaponBB = new AABB(entityStartPos, this.attackingPoint[i]);
                AABB weaponTipBB = new AABB(entityStartPos, this.weaponTip[i]);

                // make sure the last attack point is also in, for better collision on fast swings
                if (lastAttackPoint != null) {
                    weaponBB = Utils.includePoint(weaponBB, lastAttackPoint);
                }
                if (lastWeaponTip != null) {
                    weaponTipBB = Utils.includePoint(weaponTipBB, lastWeaponTip);
                }
                this.lastAttackAABB[i] = weaponTipBB;

                List<Entity> mobs = this.mc.level.getEntities(this.mc.player, weaponTipBB);
                if (this.dh.vrSettings.reducedPlayerReach) {
                    // shorter range for players to try to prevent accidental hits
                    mobs.removeIf((e) -> e instanceof Player);

                    List<Entity> players = this.mc.level.getEntities(this.mc.player, weaponBB);
                    players.removeIf((e) -> !(e instanceof Player));
                    mobs.addAll(players);
                }

                // piercing attacks
                PiercingWeapon piercingWeapon = itemstack.get(DataComponents.PIERCING_WEAPON);
                Vec3 averageTargetPosition = Vec3.ZERO;
                int targetCount = 0;

                for (Entity entity : mobs) {
                    if (entity.isPickable() &&
                        entity != this.mc.getCameraEntity().getVehicle() && // don't hit ridden entity
                        !this.lastHitEntities[i].contains(entity)) // don't hit entities multiple times per swing
                    {
                        if (entityAct) {
                            // this.mc.physicalGuiManager.preClickAction();
                            if (piercingWeapon != null) {
                                averageTargetPosition = averageTargetPosition.add(entity.getBoundingBox().getCenter());
                                targetCount++;
                            } else if (!EpicFightHelper.isLoaded() || !EpicFightHelper.attack()) {
                                ClientNetworking.sendActiveBodyPart(BODYPARTS[i], true);
                                // only attack if epic fight didn't trigger
                                this.mc.gameMode.attack(player, entity);
                            } else {
                                // only attack once with epic fight
                                entityAct = false;
                            }
                            this.dh.vr.triggerHapticPulse(c, 1000);
                            this.lastWeaponSolid[i] = true;
                        }
                        inAnEntity = true;
                    }
                }

                if (speed > speedTreshhold) {
                    this.lastHitEntities[i] = mobs;
                } else {
                    // since we couldn't act, we also didn't hit anything
                    this.lastHitEntities[i] = Collections.emptyList();
                }

                if (piercingWeapon != null && this.canAct[i] && (inAnEntity || canLunge)) {
                    // piercing attacks have their own logic
                    ClientNetworking.sendActiveBodyPart(BODYPARTS[i], true);
                    ClientNetworking.overrideAimDir(inAnEntity ?
                        MathUtils.subtractToVector3f(averageTargetPosition.scale(1F / targetCount), handPos)
                            .normalize() :
                        deviceDirection.rotateY(this.dh.vrPlayer.vrdata_world_pre.rotation_radians, new Vector3f()));
                    this.mc.gameMode.piercingAttack(piercingWeapon);
                    ClientNetworking.resetAim(0);
                    continue;
                }

                // can't hit anything else if we hit an entity
                this.canAct[i] &= !inAnEntity;

                // no hitting while climbey climbing
                if (isHand && this.dh.climbTracker.isClimbeyClimb() && (!isTool ||
                    (c == 0 && VivecraftVRMod.INSTANCE.keyClimbeyGrab.isDown(ControllerType.RIGHT)) ||
                    (c == 1 && VivecraftVRMod.INSTANCE.keyClimbeyGrab.isDown(ControllerType.LEFT))
                ))
                {
                    // reset these since those are not valid for the next tick then
                    this.lastHandPos[i] = null;
                    this.lastHandRot[i] = null;
                    continue;
                }

                // trace from the last known air position to the current position, check if we hit any block in our path
                // and damage the block it collides with...
                BlockHitResult blockHit = null;
                BlockState blockstate = null;
                this.lastMiningPointHit[i] = 0;

                Quaternionf weaponRotation = new Quaternionf().setFromNormalized(
                    this.dh.vrPlayer.vrdata_world_pre.getHand(c).getMatrix());

                // don't need to check, if we can't hit anything anyway
                if (this.canAct[i]) {
                    this.miningPoints[i] = new ArrayList<>();

                    // only interpolate if the last point was valid
                    if (this.lastHandPos[i] != null && prevMiningPoint != null) {
                        float dot = Math.abs(weaponRotation.dot(this.lastHandRot[i]));
                        float angle = 2.0F * (float) Math.acos(dot);

                        // have at max 22.5° sample steps, adds up to 8 points
                        float subdivisions = Mth.floor(angle / Mth.PI * 8F);

                        Quaternionf temp = new Quaternionf();
                        Vector3f lerpHandDir = new Vector3f();

                        // add previous position
                        this.miningPoints[i].add(prevMiningPoint);

                        for (int s = 1; s < subdivisions; s++) {
                            float lerp = s / subdivisions;
                            this.lastHandRot[i].slerp(weaponRotation, lerp, temp);
                            Vec3 lerpHand = MathUtils.vecDLerp(this.lastHandPos[i], handPos, lerp);
                            temp.transform(0, 0, -weaponLength, lerpHandDir);
                            this.miningPoints[i].add(lerpHand.add(lerpHandDir.x, lerpHandDir.y, lerpHandDir.z));
                        }
                    } else {
                        // use the current point as the start
                        this.miningPoints[i].add(this.miningPoint[i]);
                    }

                    // add current position
                    this.miningPoints[i].add(this.miningPoint[i]);

                    for (int p = 1; p < this.miningPoints[i].size(); p++) {
                        Vec3 startPos = this.miningPoints[i].get(p - 1);
                        Vec3 endPos = this.miningPoints[i].get(p);

                        if (startPos.subtract(endPos).lengthSqr() < 1.0E-7) {
                            // mc short circuits to a miss if start and end are too close together
                            endPos = endPos.add(0.001);
                        }

                        blockHit = this.mc.level.clip(
                            new ClipContext(startPos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE,
                                player));
                        blockstate = this.mc.level.getBlockState(blockHit.getBlockPos());

                        // check if block is breakable with a sword
                        // override is needed, because the `getDestroyProgress` check only checks the main hand item
                        ClientNetworking.BODY_PART_CLIENT_OVERRIDE = BODYPARTS[i];
                        // if this tool is right for the block
                        boolean mineableByItem = this.dh.vrSettings.swordBlockCollision &&
                            (itemstack.isCorrectToolForDrops(blockstate) ||
                                blockstate.getDestroyProgress(player, player.level(), blockHit.getBlockPos()) == 1F
                            );
                        ClientNetworking.BODY_PART_CLIENT_OVERRIDE = null;

                        // don't break climbable blocks
                        // if this block shouldn't be breakable with roomscale mining
                        boolean protectedBlock = this.dh.vrSettings.realisticClimbEnabled &&
                            (!player.isShiftKeyDown() || !this.dh.vrSettings.allowBreakingClimbable) &&
                            (blockstate.getBlock() instanceof LadderBlock ||
                                blockstate.getBlock() instanceof VineBlock ||
                                blockstate.is(ViveBlockTags.VIVECRAFT_CLIMBABLE)
                            );

                        // cancel if we can't hit anything further
                        this.lastMiningPointHit[i] = p;
                        if ((isSword && !mineableByItem) || protectedBlock) {
                            // need to reset these, or further code will falsely use these as a valid hit
                            blockHit = null;
                            blockstate = null;
                        } else if (blockHit.getType() == HitResult.Type.BLOCK) {
                            // found the first hit
                            break;
                        }
                    }
                }

                // store for next tick
                this.lastHandPos[i] = handPos;
                this.lastHandRot[i] = weaponRotation;

                // if the blockHit is inside, it didn't hit the block from the outside
                if (blockHit != null && blockHit.getType() == HitResult.Type.BLOCK && !blockHit.isInside() &&
                    this.canAct[i])
                {
                    this.lastWeaponSolid[i] = true;
                    this.lastBlockHit[i] = blockHit.getLocation();
                    int totalHits = 3;
                    // roomscale door punching
                    if (this.dh.vrSettings.doorHitting && isOpenable(blockstate, blockHit.getDirection()) &&
                        this.mc.gameMode.useItemOn(player,
                            c == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, blockHit) !=
                            InteractionResult.PASS)
                    {
                        // the useItem is already in the if check, so nothing to do here
                    }
                    // roomscale hoe interaction
                    else if (isHand && (item instanceof HoeItem || itemstack.is(ViveItemTags.VIVECRAFT_HOES) ||
                        itemstack.is(ViveItemTags.VIVECRAFT_SCYTHES)
                    ) && (blockstate.getBlock() instanceof CropBlock ||
                        blockstate.getBlock() instanceof StemBlock ||
                        blockstate.getBlock() instanceof AttachedStemBlock ||
                        blockstate.is(ViveBlockTags.VIVECRAFT_CROPS) ||
                        // check if the item can use the block
                        (item.useOn(new UseOnContext(player,
                            c == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                            blockHit)) instanceof InteractionResult.Success success &&
                            success.swingSource() == InteractionResult.SwingSource.CLIENT
                        )
                    ))
                    {
                        // don't try to break crops with hoes
                        // actually use the item on the block
                        boolean useSuccessful = this.mc.gameMode.useItemOn(player,
                            i == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                            blockHit) instanceof InteractionResult.Success success &&
                            success.swingSource() == InteractionResult.SwingSource.CLIENT;
                        if (itemstack.is(ViveItemTags.VIVECRAFT_SCYTHES) && !useSuccessful) {
                            // some scythes just need to be used
                            this.mc.gameMode.useItem(player,
                                c == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
                        }
                    }
                    // roomscale brushes
                    else if (isHand && (item instanceof BrushItem || itemstack.is(ViveItemTags.VIVECRAFT_BRUSHES))) {
                        // local visuals/sound
                        if (item instanceof BrushItem brush) {
                            brush.spawnDustParticles(player.level(), blockHit, blockstate,
                                player.getViewVector(0.0F),
                                c == 0 ? player.getMainArm() : player.getMainArm().getOpposite());
                        }
                        player.level().playSound(player, blockHit.getBlockPos(),
                            blockstate.getBlock() instanceof BrushableBlock ?
                                ((BrushableBlock) blockstate.getBlock()).getBrushSound() :
                                SoundEvents.BRUSH_GENERIC, SoundSource.BLOCKS);

                        // server logic
                        // need to look at it for longer, this will modify the player look direction
                        // needed for vanilla and spigot
                        ClientDataHolderVR.getInstance().vrPlayer.setLookAtPos(blockHit.getLocation(), 5);

                        ClientNetworking.sendActiveBodyPart(BODYPARTS[i], true);
                        ClientNetworking.overrideAimDir(MathUtils.subtractToVector3f(blockHit.getLocation(),
                            ClientNetworking.SERVER_WANTS_DATA ? handPos :
                                Minecraft.getInstance().player.getEyePosition()));
                        // for modded servers, make sure the aim stays consistent until it triggers
                        ClientNetworking.overrideAimPos(handPos);

                        this.mc.gameMode.useItemOn(player,
                            c == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, blockHit);

                        ClientNetworking.resetAim(5);
                        this.useHoldTicks[i] = 5;
                    }
                    // roomscale noteblocks
                    else if (blockstate.getBlock() instanceof NoteBlock ||
                        blockstate.is(ViveBlockTags.VIVECRAFT_MUSIC_BLOCKS))
                    {
                        this.mc.gameMode.continueDestroyBlock(blockHit.getBlockPos(), blockHit.getDirection());
                    }
                    // roomscale mining
                    else {
                        // faster swings do more damage
                        totalHits = (int) (totalHits + Math.min(speed - speedTreshhold, 4.0F));
                        // this.mc.physicalGuiManager.preClickAction();

                        // send hitting hand
                        ClientNetworking.sendActiveBodyPart(BODYPARTS[i], true);

                        // this will either destroy the block if in creative or set it as the current block.
                        // does nothing in survival if you are already hitting this block.
                        this.mc.gameMode.startDestroyBlock(blockHit.getBlockPos(), blockHit.getDirection());

                        // seems to be the only way to tell it didn't instabreak.
                        if (this.getIsHittingBlock()) {
                            for (int hit = 0; hit < totalHits; hit++) {
                                // send multiple ticks worth of 'holding left click' to it.
                                if (this.mc.gameMode.continueDestroyBlock(blockHit.getBlockPos(),
                                    blockHit.getDirection()))
                                {
                                    this.mc.level.addBreakingBlockEffect(blockHit.getBlockPos(),
                                        blockHit.getDirection());
                                }

                                this.clearBlockHitDelay();

                                if (!this.getIsHittingBlock()) {
                                    // seems to be the only way to tell if it broke.
                                    break;
                                }
                            }

                            this.mc.gameMode.destroyDelay = 0;
                        }

                        this.dh.vrPlayer.blockDust(blockHit.getLocation().x, blockHit.getLocation().y,
                            blockHit.getLocation().z, 3 * totalHits, blockHit.getBlockPos(), blockstate, 0.6F, 1.0F);
                    }

                    this.dh.vr.triggerHapticPulse(c, 250 * totalHits);
                } else {
                    // reset
                    this.lastWeaponSolid[i] = false;
                }
            }
        }

        // reset hitting hand
        ClientNetworking.resetActiveBodyPart();

        Profiler.get().pop();
    }

    private boolean getIsHittingBlock() {
        return this.mc.gameMode.isDestroying();
    }

    private void clearBlockHitDelay() {
        // TODO set destroyTicks to 1 to cancel multiple sound events per hit
        // MCReflection.PlayerController_blockHitDelay.set(this.mc.gameMode, 0);
        // this.mc.gameMode.blockBreakingCooldown = 1;
    }

    /**
     * does a raytrace to find the closest block
     *
     * @param start start of raytrace
     * @param end   end of raytrace
     * @return hit position, if a Block was hit, or {@code end} otherwise
     */
    private Vec3 constrain(Vec3 start, Vec3 end) {
        BlockHitResult blockhitresult = this.mc.level.clip(
            new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this.mc.player));
        return blockhitresult.getType() == HitResult.Type.BLOCK ? blockhitresult.getLocation() : end;
    }

    /**
     * checks if the given block can be opened by hitting it in the give direction
     *
     * @param state     block state to check
     * @param direction worldDirection the hit came from
     * @return if the block can be opened
     */
    private boolean isOpenable(BlockState state, Direction direction) {

        if (state.is(net.minecraft.tags.BlockTags.DOORS) || state.getBlock() instanceof DoorBlock) {
            Direction facing = state.getValue(DoorBlock.FACING);
            boolean open = state.getValue(DoorBlock.OPEN);
            DoorHingeSide hinge = state.getValue(DoorBlock.HINGE);

            if (!open) {
                return facing == direction.getOpposite();
            } else {
                return switch (direction) {
                    case SOUTH -> (facing == Direction.WEST && hinge == DoorHingeSide.LEFT) ||
                        (facing == Direction.EAST && hinge == DoorHingeSide.RIGHT);
                    case NORTH -> (facing == Direction.EAST && hinge == DoorHingeSide.LEFT) ||
                        (facing == Direction.WEST && hinge == DoorHingeSide.RIGHT);
                    case EAST -> (facing == Direction.SOUTH && hinge == DoorHingeSide.LEFT) ||
                        (facing == Direction.NORTH && hinge == DoorHingeSide.RIGHT);
                    case WEST -> (facing == Direction.NORTH && hinge == DoorHingeSide.LEFT) ||
                        (facing == Direction.SOUTH && hinge == DoorHingeSide.RIGHT);
                    default -> false;
                };
            }
        } else if (state.is(net.minecraft.tags.BlockTags.TRAPDOORS) || state.getBlock() instanceof TrapDoorBlock) {
            Direction facing = state.getValue(TrapDoorBlock.FACING);
            boolean open = state.getValue(TrapDoorBlock.OPEN);
            return (!open && direction == Direction.DOWN) || (open && direction.getOpposite() == facing);
        } else if (state.is(net.minecraft.tags.BlockTags.FENCE_GATES) || state.getBlock() instanceof FenceGateBlock) {
            Direction facing = state.getValue(FenceGateBlock.FACING);
            boolean open = state.getValue(FenceGateBlock.OPEN);
            return !open && direction.getAxis() == facing.getAxis();
        }
        return false;
    }

    /**
     * @param player    Player that is holding the item
     * @param itemStack held item
     * @return the transparency for held items to indicate attack power or sneaking.
     */
    public static float getItemFade(LocalPlayer player, ItemStack itemStack, boolean mainHand) {
        float fade = player.getAttackStrengthScale(0.0F) * 0.75F + 0.25F;

        if (player.isShiftKeyDown()) {
            fade = 0.75F;
        }

        if (ClientDataHolderVR.getInstance().swingTracker.lastWeaponSolid[mainHand ? 0 : 1]) {
            fade -= 0.25F;
        }

        if (itemStack != ItemStack.EMPTY) {
            if (player.isBlocking() && player.getUseItem() != itemStack) {
                fade -= 0.25F;
            }

            if (itemStack.getItem() == Items.SHIELD && !player.isBlocking()) {
                fade -= 0.25F;
            }
        }

        if ((double) fade < 0.1D) {
            fade = 0.1F;
        }

        if (fade > 1.0F) {
            fade = 1.0F;
        }

        return fade;
    }

    @Override
    public void renderDebug() {
        int trackers = 2;

        if (this.dh.vrSettings.feetCollision && this.dh.vrPlayer.vrdata_world_pre.fbtMode != FBTMode.ARMS_ONLY) {
            trackers = 4;
        }

        VRData world = this.dh.vrPlayer.getVRDataWorld();
        // for world relative
        Vec3 camWorld = world.getEye(this.dh.currentPass).getPosition();
        // for player relative
        Vec3 cam = camWorld.add(this.dh.vrPlayer.vrdata_world_pre.origin).subtract(world.origin);

        for (int i = 0; i < trackers; i++) {
            int failColor =
                this.tipHistory[i].averageSpeed(0.33D) > SPEED_THRESH * (this.mc.player.isCreative() ? 1.5F : 1F) ?
                    MathUtils.ORANGE_INT : MathUtils.RED_INT;
            if (this.miningPoints[i] != null || this.miningPoint[i] != null) {
                if (this.previousMiningPoints[i].isEmpty() ||
                    !this.previousMiningPoints[i].getLast().getLeft().equals(this.miningPoint[i]))
                {
                    // only updated when actable
                    if (this.miningPoints[i] != null && this.canAct[i]) {
                        // skip first, since that is the last tick point
                        for (int p = 1; p < this.miningPoints[i].size(); p++) {
                            int color =
                                p <= this.lastMiningPointHit[i] ? MathUtils.GREEN_INT : MathUtils.LIGHT_GRAY_INT;
                            if (p < this.miningPoints[i].size() - 1) {
                                color = ARGB.scaleRGB(color, 0.5F);
                            }
                            this.previousMiningPoints[i].addLast(
                                Pair.of(this.miningPoints[i].get(p), color));
                        }
                    } else {
                        this.previousMiningPoints[i].addLast(
                            Pair.of(this.miningPoint[i], this.canAct[i] ? MathUtils.GREEN_INT : failColor));
                    }
                    while (this.previousMiningPoints[i].size() > 20) {
                        this.previousMiningPoints[i].removeFirst();
                    }
                }

                DebugRenderHelper.renderCube(this.miningPoint[i], 0.025F,
                    this.canAct[i] ? MathUtils.GREEN_INT : failColor);
                if (!this.previousMiningPoints[i].isEmpty()) {
                    Pair<Vec3, Integer> prev = null;
                    for (Pair<Vec3, Integer> p : this.previousMiningPoints[i]) {
                        DebugRenderHelper.renderCube(p.getLeft(), 0.0125F,
                            p.getRight());
                        if (prev != null) {
                            DebugRenderHelper.renderLine(p.getRight(), prev.getLeft(), p.getLeft());
                        }
                        prev = p;
                    }
                }
            }
            if (this.lastBlockHit[i] != null) {
                DebugRenderHelper.renderCube(this.lastBlockHit[i], 0.025F, MathUtils.GREEN_INT);
            }

            if (this.lastAttackAABB[i] != null) {
                DebugRenderHelper.renderAABB(this.lastAttackAABB[i],
                    this.lastHitEntities[i].isEmpty() ? failColor : MathUtils.GREEN_INT);
            }
            if (this.weaponTip[i] != null) {
                DebugRenderHelper.renderCube(this.weaponTip[i], 0.025F,
                    this.lastHitEntities[i].isEmpty() ? failColor : MathUtils.GREEN_INT);
            }
            for (Entity entity : this.lastHitEntities[i]) {
                DebugRenderHelper.renderCube(
                    entity.getBoundingBox().getCenter(),
                    (float) entity.getBoundingBox().getSize() / 2F, MathUtils.GREEN_INT);
            }
        }
    }
}
