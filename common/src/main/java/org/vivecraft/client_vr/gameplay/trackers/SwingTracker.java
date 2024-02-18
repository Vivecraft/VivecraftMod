package org.vivecraft.client_vr.gameplay.trackers;

import java.util.List;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import org.vivecraft.api.client.Tracker;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.BlockTags;
import org.vivecraft.client_vr.ItemTags;
import org.vivecraft.client_vr.Vec3History;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.settings.VRSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SwingTracker implements Tracker {
    private final Vec3[] lastWeaponEndAir = new Vec3[]{new Vec3(0.0D, 0.0D, 0.0D), new Vec3(0.0D, 0.0D, 0.0D)};
    private final boolean[] lastWeaponSolid = new boolean[2];
    public Vec3[] miningPoint = new Vec3[2];
    public Vec3[] attackingPoint = new Vec3[2];
    public Vec3History[] tipHistory = new Vec3History[]{new Vec3History(), new Vec3History()};
    public boolean[] canact = new boolean[2];
    public int disableSwing = 3;
    Vec3 forward = new Vec3(0.0D, 0.0D, -1.0D);
    double speedthresh = 3.0D;
    protected Minecraft mc;
    protected ClientDataHolderVR dh;

    public SwingTracker(Minecraft mc, ClientDataHolderVR dh) {
        this.mc = mc;
        this.dh = dh;
    }

    public boolean isActive(LocalPlayer p) {
        if (this.disableSwing > 0) {
            --this.disableSwing;
            return false;
        } else if (this.mc.gameMode == null) {
            return false;
        } else if (p == null) {
            return false;
        } else if (!p.isAlive()) {
            return false;
        } else if (p.isSleeping()) {
            return false;
        } else {
            if (this.mc.screen != null) {
                return false;
            } else if (this.dh.vrSettings.weaponCollision == VRSettings.WeaponCollision.OFF) {
                return false;
            } else if (this.dh.vrSettings.weaponCollision == VRSettings.WeaponCollision.AUTO) {
                return !p.isCreative();
            } else if (this.dh.vrSettings.seated) {
                return false;
            } else {
                VRSettings vrsettings = this.dh.vrSettings;

                if (this.dh.vrSettings.vrFreeMoveMode == VRSettings.FreeMove.RUN_IN_PLACE && p.zza > 0.0F) {
                    return false;
                } else if (p.isBlocking()) {
                    return false;
                } else {
                    return !this.dh.jumpTracker.isjumping();
                }
            }
        }
    }

    public static boolean isTool(Item item) {
        return item instanceof DiggerItem || item instanceof ArrowItem || item instanceof FishingRodItem || item instanceof FoodOnAStickItem || item instanceof ShearsItem || item == Items.BONE || item == Items.BLAZE_ROD || item == Items.BAMBOO || item == Items.TORCH || item == Items.REDSTONE_TORCH || item == Items.STICK || item == Items.DEBUG_STICK || item instanceof FlintAndSteelItem || item instanceof BrushItem || item.getDefaultInstance().is(ItemTags.VIVECRAFT_TOOLS);
    }

    public void doProcess(LocalPlayer player) {
        this.speedthresh = 3.0D;

        if (player.isCreative()) {
            this.speedthresh *= 1.5D;
        }

        this.mc.getProfiler().push("updateSwingAttack");

        for (int i = 0; i < 2; ++i) {
            if (!this.dh.climbTracker.isGrabbingLadder(i)) {
                Vec3 vec3 = this.dh.vrPlayer.vrdata_world_pre.getController(i).getPosition();
                Vec3 vec31 = this.dh.vrPlayer.vrdata_world_pre.getHand(i).getCustomVector(this.forward);
                ItemStack itemstack = player.getItemInHand(i == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
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

                f = f * this.dh.vrPlayer.vrdata_world_pre.worldScale;
                this.miningPoint[i] = vec3.add(vec31.scale(f));
                Vec3 vec32 = this.dh.vrPlayer.vrdata_room_pre.getController(i).getPosition().add(this.dh.vrPlayer.vrdata_room_pre.getHand(i).getCustomVector(this.forward).scale(0.3D));
                this.tipHistory[i].add(vec32);
                float f2 = (float) this.tipHistory[i].averageSpeed(0.33D);
                boolean flag2 = false;
                this.canact[i] = (double) f2 > this.speedthresh && !this.lastWeaponSolid[i];
                boolean flag3 = this.canact[i];

                if (flag3) {
                    BlockHitResult blockhitresult = this.mc.level.clip(new ClipContext(this.dh.vrPlayer.vrdata_world_pre.hmd.getPosition(), vec3, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this.mc.player));

                    if (blockhitresult.getType() != HitResult.Type.MISS) {
                        flag3 = false;
                    }
                }

                this.attackingPoint[i] = this.constrain(vec3, this.miningPoint[i]);
                Vec3 vec33 = vec3.add(vec31.scale(f + f1));
                vec33 = this.constrain(vec3, vec33);
                AABB aabb = new AABB(vec3, this.attackingPoint[i]);
                AABB aabb1 = new AABB(vec3, vec33);
                List<Entity> list = this.mc.level.getEntities(this.mc.player, aabb1);
                list.removeIf((e) ->
                {
                    return e instanceof Player;
                });
                List<Entity> list1 = this.mc.level.getEntities(this.mc.player, aabb);
                list1.removeIf((e) ->
                {
                    return !(e instanceof Player);
                });
                list.addAll(list1);

                for (Entity entity : list) {
                    if (entity.isPickable() && entity != this.mc.getCameraEntity().getVehicle()) {
                        if (flag3) {
                            //this.mc.physicalGuiManager.preClickAction();
                            this.mc.gameMode.attack(player, entity);
                            this.dh.vr.triggerHapticPulse(i, 1000);
                            this.lastWeaponSolid[i] = true;
                        }

                        flag2 = true;
                    }
                }

                this.canact[i] = this.canact[i] && !flag1 && !flag2;

                if (!this.dh.climbTracker.isClimbeyClimb() || (i != 0 || !VivecraftVRMod.INSTANCE.keyClimbeyGrab.isDown(ControllerType.RIGHT)) && flag && (i != 1 || !VivecraftVRMod.INSTANCE.keyClimbeyGrab.isDown(ControllerType.LEFT)) && flag) {
                    BlockPos blockpos = BlockPos.containing(this.miningPoint[i]);
                    BlockState blockstate = this.mc.level.getBlockState(blockpos);
                    BlockHitResult blockhitresult1 = this.mc.level.clip(new ClipContext(this.lastWeaponEndAir[i], this.miningPoint[i], ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this.mc.player));

                    if (!blockstate.isAir() && blockhitresult1.getType() == HitResult.Type.BLOCK && this.lastWeaponEndAir[i].length() != 0.0D) {
                        this.lastWeaponSolid[i] = true;
                        boolean flag4 = blockhitresult1.getBlockPos().equals(blockpos);
                        boolean flag5 = this.dh.vrSettings.realisticClimbEnabled && (blockstate.getBlock() instanceof LadderBlock || blockstate.getBlock() instanceof VineBlock || blockstate.is(BlockTags.VIVECRAFT_CLIMBABLE));

                        if (blockhitresult1.getType() == HitResult.Type.BLOCK && flag4 && this.canact[i] && !flag5) {
                            int j = 3;

                            if ((item instanceof HoeItem || itemstack.is(ItemTags.VIVECRAFT_HOES) || itemstack.is(ItemTags.VIVECRAFT_SCYTHES)) && (
                                blockstate.getBlock() instanceof CropBlock
                                    || blockstate.getBlock() instanceof StemBlock
                                    || blockstate.getBlock() instanceof AttachedStemBlock
                                    || blockstate.is(BlockTags.VIVECRAFT_CROPS)
                                    // check if the item can use the block
                                    || item.useOn(new UseOnContext(player, i == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, blockhitresult1)).shouldSwing())) {
                                // don't try to break crops with hoes
                                // actually use the item on the block
                                boolean useSuccessful = this.mc.gameMode.useItemOn(player, i == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, blockhitresult1).shouldSwing();
                                if (itemstack.is(ItemTags.VIVECRAFT_SCYTHES) && !useSuccessful) {
                                    // some scythes just need to be used
                                    this.mc.gameMode.useItem(player, i == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
                                }
                            } else if ((item instanceof BrushItem /*|| itemstack.is(ItemTags.VIVECRAFT_BRUSHES*/)) {
                                ((BrushItem) item).spawnDustParticles(player.level(), blockhitresult1, blockstate, player.getViewVector(0.0F), i == 0 ? player.getMainArm() : player.getMainArm().getOpposite());
                                player.level().playSound(player, blockhitresult1.getBlockPos(), blockstate.getBlock() instanceof BrushableBlock ? ((BrushableBlock) blockstate.getBlock()).getBrushSound() : SoundEvents.BRUSH_GENERIC, SoundSource.BLOCKS);
                                this.mc.gameMode.useItemOn(player, i == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, blockhitresult1);
                            } else if (blockstate.getBlock() instanceof NoteBlock || blockstate.is(BlockTags.VIVECRAFT_MUSIC_BLOCKS)) {
                                this.mc.gameMode.continueDestroyBlock(blockhitresult1.getBlockPos(), blockhitresult1.getDirection());
                            } else {
                                j = (int) ((double) j + Math.min((double) f2 - this.speedthresh, 4.0D));
                                //this.mc.physicalGuiManager.preClickAction();
                                this.mc.gameMode.startDestroyBlock(blockhitresult1.getBlockPos(), blockhitresult1.getDirection());

                                if (this.getIsHittingBlock()) {
                                    for (int k = 0; k < j; ++k) {
                                        if (this.mc.gameMode.continueDestroyBlock(blockhitresult1.getBlockPos(), blockhitresult1.getDirection())) {
                                            this.mc.particleEngine.crack(blockhitresult1.getBlockPos(), blockhitresult1.getDirection());
                                        }

                                        this.clearBlockHitDelay();

                                        if (!this.getIsHittingBlock()) {
                                            break;
                                        }
                                    }

                                    this.mc.gameMode.destroyDelay = 0;
                                }

                                this.dh.vrPlayer.blockDust(blockhitresult1.getLocation().x, blockhitresult1.getLocation().y, blockhitresult1.getLocation().z, 3 * j, blockpos, blockstate, 0.6F, 1.0F);
                            }

                            this.dh.vr.triggerHapticPulse(i, 250 * j);
                        }
                    } else {
                        this.lastWeaponEndAir[i] = this.miningPoint[i];
                        this.lastWeaponSolid[i] = false;
                    }
                }
            }
        }

        this.mc.getProfiler().pop();
    }

    @Override
    public TrackerTickType tickType() {
        return TrackerTickType.PER_TICK;
    }

    private boolean getIsHittingBlock() {
        return this.mc.gameMode.isDestroying();
    }

    private void clearBlockHitDelay() {
        //MCReflection.PlayerController_blockHitDelay.set(this.mc.gameMode, 0);
        // this.mc.gameMode.blockBreakingCooldown = 1;
    }

    public Vec3 constrain(Vec3 start, Vec3 end) {
        BlockHitResult blockhitresult = this.mc.level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this.mc.player));
        return blockhitresult.getType() == HitResult.Type.BLOCK ? blockhitresult.getLocation() : end;
    }

    public static float getItemFade(LocalPlayer p, ItemStack is) {
        float f = p.getAttackStrengthScale(0.0F) * 0.75F + 0.25F;

        if (p.isShiftKeyDown()) {
            f = 0.75F;
        }

        boolean[] aboolean = ClientDataHolderVR.getInstance().swingTracker.lastWeaponSolid;
        Minecraft.getInstance().getItemRenderer();

        if (aboolean[ClientDataHolderVR.ismainhand ? 0 : 1]) {
            f -= 0.25F;
        }

        if (is != ItemStack.EMPTY) {
            if (p.isBlocking() && p.getUseItem() != is) {
                f -= 0.25F;
            }

            if (is.getItem() == Items.SHIELD && !p.isBlocking()) {
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
