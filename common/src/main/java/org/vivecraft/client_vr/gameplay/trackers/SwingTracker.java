package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.BlockTags;
import org.vivecraft.client_vr.ItemTags;
import org.vivecraft.client_vr.Vec3History;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.settings.VRSettings.FreeMove;
import org.vivecraft.client_vr.settings.VRSettings.WeaponCollision;

import java.util.List;

import static org.joml.Math.min;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.*;

public class SwingTracker extends Tracker {
    private final Vector3f lastWeaponEndAirc0 = new Vector3f();
    private final Vector3f lastWeaponEndAirc1 = new Vector3f();
    private final boolean[] lastWeaponSolid = new boolean[2];
    public final Vector3f miningPointc0 = new Vector3f();
    public final Vector3f miningPointc1 = new Vector3f();
    public final Vector3f attackingPointc0 = new Vector3f();
    public final Vector3f attackingPointc1 = new Vector3f();

    public final Vec3History tipHistoryc0 = new Vec3History();
    public final Vec3History tipHistoryc1 = new Vec3History();
    public final boolean[] canact = new boolean[2];
    public int disableSwing = 3;
    double speedthresh = 3.0D;

    @Override
    public boolean isActive() {
        if (this.disableSwing > 0) {
            --this.disableSwing;
            return false;
        } else if (mc.gameMode == null) {
            return false;
        } else if (mc.player == null) {
            return false;
        } else if (!mc.player.isAlive()) {
            return false;
        } else if (mc.player.isSleeping()) {
            return false;
        } else {
            if (mc.screen != null) {
                return false;
            } else if (dh.vrSettings.weaponCollision == WeaponCollision.OFF) {
                return false;
            } else if (dh.vrSettings.weaponCollision == WeaponCollision.AUTO) {
                return !mc.player.isCreative();
            } else if (dh.vrSettings.seated) {
                return false;
            } else {
                if (dh.vrSettings.vrFreeMoveMode == FreeMove.RUN_IN_PLACE && mc.player.zza > 0.0F) {
                    return false;
                } else if (mc.player.isBlocking()) {
                    return false;
                } else {
                    return !dh.jumpTracker.isjumping();
                }
            }
        }
    }

    public static boolean isTool(Item item) {
        return item instanceof DiggerItem || item instanceof ArrowItem || item instanceof FishingRodItem || item instanceof FoodOnAStickItem || item instanceof ShearsItem || item == Items.BONE || item == Items.BLAZE_ROD || item == Items.BAMBOO || item == Items.TORCH || item == Items.REDSTONE_TORCH || item == Items.STICK || item == Items.DEBUG_STICK || item instanceof FlintAndSteelItem || item instanceof BrushItem || item.getDefaultInstance().is(ItemTags.VIVECRAFT_TOOLS);
    }

    @Override
    public void doProcess() {
        this.speedthresh = 3.0D;

        if (mc.player.isCreative()) {
            this.speedthresh *= 1.5D;
        }

        mc.getProfiler().push("updateSwingAttack");

        for (int i = 0; i < 2; ++i) {
            final Vector3f miningPoint;
            final Vector3f lastWeaponEndAir;
            final Vector3f attackingPoint;
            final Vec3History tipHistory;
            if (i == 0){
                miningPoint = miningPointc0;
                lastWeaponEndAir = lastWeaponEndAirc0;
                attackingPoint = attackingPointc0;
                tipHistory = tipHistoryc0;
            } else {
                miningPoint = miningPointc1;
                lastWeaponEndAir = lastWeaponEndAirc1;
                attackingPoint = attackingPointc1;
                tipHistory = tipHistoryc1;
            }
            if (!dh.climbTracker.isGrabbingLadder(i)) {
                Vector3fc vec3 = dh.vrPlayer.vrdata_world_pre.getController(i).getPosition(new Vector3f());
                Vector3fc vec31 = dh.vrPlayer.vrdata_world_pre.getHand(i).getCustomVector(new Vector3f(forward));
                ItemStack itemstack = mc.player.getItemInHand(i == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
                Item item = itemstack.getItem();
                boolean flag = false;
                boolean flag1 = false;

                if (!(item instanceof SwordItem || itemstack.is(ItemTags.VIVECRAFT_SWORDS)) && !(item instanceof TridentItem || itemstack.is(ItemTags.VIVECRAFT_SPEARS))) {
                    if (isTool(item)) {
                        flag = true;
                    }
                } else {
                    flag1 = true;
                    flag = true;
                }

                float f;
                float f1;

                if (flag1) {
                    f1 = 1.9F;
                    f = 0.6F;
                    flag = true;
                } else if (flag) {
                    f1 = 1.2F;
                    f = 0.35F;
                    flag = true;
                } else if (!itemstack.isEmpty()) {
                    f = 0.1F;
                    f1 = 0.3F;
                } else {
                    f = 0.0F;
                    f1 = 0.3F;
                }

                f *= dh.vrPlayer.vrdata_world_pre.worldScale;
                vec3.add(vec31.mul(f, new Vector3f()), miningPoint);
                tipHistory.add(
                    dh.vrPlayer.vrdata_room_pre.getController(i).getPosition(new Vector3f()).add(
                        dh.vrPlayer.vrdata_room_pre.getHand(i).getCustomVector(new Vector3f(forward)).mul(0.3F)
                    )
                );
                float f2 = tipHistory.averageSpeed(0.33F);
                boolean flag2 = false;
                this.canact[i] = (double) f2 > this.speedthresh && !this.lastWeaponSolid[i];
                boolean flag3 = this.canact[i];

                if (flag3) {
                    BlockHitResult blockhitresult = mc.level.clip(new ClipContext(convertToVec3(dh.vrPlayer.vrdata_world_pre.hmd.getPosition(new Vector3f())), convertToVec3(vec3), Block.OUTLINE, Fluid.NONE, mc.player));

                    if (blockhitresult.getType() != Type.MISS) {
                        flag3 = false;
                    }
                }

                this.constrain(vec3, miningPoint, attackingPoint);
                Vector3f vec33 = vec31.mul(f + f1, new Vector3f()).add(vec3);
                this.constrain(vec3, vec33, vec33);
                AABB aabb = new AABB(convertToVec3(vec3), convertToVec3(attackingPoint));
                AABB aabb1 = new AABB(convertToVec3(vec3), convertToVec3(vec33));
                List<Entity> list = mc.level.getEntities(mc.player, aabb1);
                list.removeIf((e) -> e instanceof Player);
                List<Entity> list1 = mc.level.getEntities(mc.player, aabb);
                list1.removeIf((e) -> !(e instanceof Player));
                list.addAll(list1);

                for (Entity entity : list) {
                    if (entity.isPickable() && entity != mc.getCameraEntity().getVehicle()) {
                        if (flag3) {
                            //Minecraft.getInstance().physicalGuiManager.preClickAction();
                            mc.gameMode.attack(mc.player, entity);
                            dh.vr.triggerHapticPulse(i, 1000);
                            this.lastWeaponSolid[i] = true;
                        }

                        flag2 = true;
                    }
                }

                this.canact[i] = this.canact[i] && !flag1 && !flag2;

                if (!dh.climbTracker.isClimbeyClimb() || (i != 0 || !VivecraftVRMod.keyClimbeyGrab.isDown(ControllerType.RIGHT)) && flag && (i != 1 || !VivecraftVRMod.keyClimbeyGrab.isDown(ControllerType.LEFT)) && flag) {
                    BlockPos blockpos = BlockPos.containing(miningPoint.x, miningPoint.y, miningPoint.z);
                    BlockState blockstate = mc.level.getBlockState(blockpos);
                    BlockHitResult blockhitresult1 = mc.level.clip(new ClipContext(convertToVec3(lastWeaponEndAir), convertToVec3(miningPoint), Block.OUTLINE, Fluid.NONE, mc.player));

                    if (!blockstate.isAir() && blockhitresult1.getType() == Type.BLOCK && lastWeaponEndAir.length() != 0.0F) {
                        this.lastWeaponSolid[i] = true;
                        boolean flag4 = blockhitresult1.getBlockPos().equals(blockpos);
                        boolean flag5 = dh.vrSettings.realisticClimbEnabled && (blockstate.getBlock() instanceof LadderBlock || blockstate.getBlock() instanceof VineBlock || blockstate.is(BlockTags.VIVECRAFT_CLIMBABLE));

                        if (blockhitresult1.getType() == Type.BLOCK && flag4 && this.canact[i] && !flag5) {
                            int j = 3;

                            if ((item instanceof HoeItem || itemstack.is(ItemTags.VIVECRAFT_HOES) || itemstack.is(ItemTags.VIVECRAFT_SCYTHES)) && (
                                blockstate.getBlock() instanceof CropBlock
                                    || blockstate.getBlock() instanceof StemBlock
                                    || blockstate.getBlock() instanceof AttachedStemBlock
                                    || blockstate.is(BlockTags.VIVECRAFT_CROPS)
                                    // check if the item can use the block
                                    || item.useOn(new UseOnContext(mc.player, i == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, blockhitresult1)).shouldSwing())) {
                                // don't try to break crops with hoes
                                // actually use the item on the block
                                boolean useSuccessful = mc.gameMode.useItemOn(mc.player, i == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, blockhitresult1).shouldSwing();
                                if (itemstack.is(ItemTags.VIVECRAFT_SCYTHES) && !useSuccessful) {
                                    // some scythes just need to be used
                                    mc.gameMode.useItem(mc.player, i == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
                                }
                            } else if ((item instanceof BrushItem /*|| itemstack.is(ItemTags.VIVECRAFT_BRUSHES*/)) {
                                ((BrushItem) item).spawnDustParticles(mc.player.level(), blockhitresult1, blockstate, mc.player.getViewVector(0.0F), i == 0 ? mc.player.getMainArm() : mc.player.getMainArm().getOpposite());
                                mc.player.level().playSound(mc.player, blockhitresult1.getBlockPos(), blockstate.getBlock() instanceof BrushableBlock ? ((BrushableBlock) blockstate.getBlock()).getBrushSound() : SoundEvents.BRUSH_GENERIC, SoundSource.BLOCKS);
                                mc.gameMode.useItemOn(mc.player, i == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, blockhitresult1);
                            } else if (blockstate.getBlock() instanceof NoteBlock || blockstate.is(BlockTags.VIVECRAFT_MUSIC_BLOCKS)) {
                                mc.gameMode.continueDestroyBlock(blockhitresult1.getBlockPos(), blockhitresult1.getDirection());
                            } else {
                                j = (int) ((double) j + min((double) f2 - this.speedthresh, 4.0D));
                                //mc.physicalGuiManager.preClickAction();
                                mc.gameMode.startDestroyBlock(blockhitresult1.getBlockPos(), blockhitresult1.getDirection());

                                if (this.getIsHittingBlock()) {
                                    for (int k = 0; k < j; ++k) {
                                        if (mc.gameMode.continueDestroyBlock(blockhitresult1.getBlockPos(), blockhitresult1.getDirection())) {
                                            mc.particleEngine.crack(blockhitresult1.getBlockPos(), blockhitresult1.getDirection());
                                        }

                                        this.clearBlockHitDelay();

                                        if (!this.getIsHittingBlock()) {
                                            break;
                                        }
                                    }

                                    mc.gameMode.destroyDelay = 0;
                                }

                                dh.vrPlayer.blockDust(blockhitresult1.getLocation().x, blockhitresult1.getLocation().y, blockhitresult1.getLocation().z, 3 * j, blockpos, blockstate, 0.6F, 1.0F);
                            }

                            dh.vr.triggerHapticPulse(i, 250 * j);
                        }
                    } else {
                        lastWeaponEndAir.set(miningPoint);
                        this.lastWeaponSolid[i] = false;
                    }
                }
            }
        }

        mc.getProfiler().pop();
    }

    private boolean getIsHittingBlock() {
        return mc.gameMode.isDestroying();
    }

    private void clearBlockHitDelay() {
        //MCReflection.PlayerController_blockHitDelay.set(Minecraft.getInstance().gameMode, 0);
        // Minecraft.getInstance().gameMode.blockBreakingCooldown = 1;
    }

    public Vector3f constrain(Vector3fc start, Vector3fc end, Vector3f dest) {
        BlockHitResult blockhitresult = mc.level.clip(new ClipContext(convertToVec3(start), convertToVec3(end), Block.OUTLINE, Fluid.NONE, mc.player));
        return blockhitresult.getType() == Type.BLOCK ? convertToVector3f(blockhitresult.getLocation(), dest) : dest.set(end);
    }

    public static float getItemFade(ItemStack is) {
        float f = mc.player.getAttackStrengthScale(0.0F) * 0.75F + 0.25F;

        if (mc.player.isShiftKeyDown()) {
            f = 0.75F;
        }

        boolean[] aboolean = dh.swingTracker.lastWeaponSolid;
        mc.getItemRenderer();

        if (aboolean[dh.ismainhand ? 0 : 1]) {
            f -= 0.25F;
        }

        if (is != ItemStack.EMPTY) {
            if (mc.player.isBlocking() && mc.player.getUseItem() != is) {
                f -= 0.25F;
            }

            if (is.getItem() == Items.SHIELD && !mc.player.isBlocking()) {
                f -= 0.25F;
            }
        }

        if ((double) f < 0.1D) {
            f = 0.1F;
        }

        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }
}
