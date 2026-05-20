package org.vivecraft.client_vr.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.effects.SpearAnimations;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Ease;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.KineticWeapon;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.BaseTorchBlock;
import net.minecraft.world.level.block.Block;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.extensions.BlockModelWrapperExtension;
import org.vivecraft.client_vr.gameplay.trackers.SwingTracker;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.common.utils.MathUtils;
import org.vivecraft.data.ViveItemTags;
import org.vivecraft.data.ViveItems;

public class VivecraftItemRendering {
    private static final ClientDataHolderVR DH = ClientDataHolderVR.getInstance();

    /**
     * determines how the given ItemStack should be rendered
     *
     * @param itemStack ItemStack to identify
     * @param player    Player holding the ItemStack
     * @return ItemTransformType that specifies how the item should be rendered
     */
    public static VivecraftItemTransformType getTransformType(ItemStack itemStack, AbstractClientPlayer player) {
        VivecraftItemTransformType itemTransformType = VivecraftItemTransformType.ITEM;
        Item item = itemStack.getItem();

        if (itemStack.getUseAnimation() == ItemUseAnimation.EAT ||
            itemStack.getUseAnimation() == ItemUseAnimation.DRINK)
        {
            itemTransformType = VivecraftItemTransformType.NOMS;
        } else if (item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();

            if (block instanceof BaseTorchBlock) {
                itemTransformType = VivecraftItemTransformType.BLOCK_STICK;
            } else {
                Identifier modelName = itemStack.get(DataComponents.ITEM_MODEL);
                if (modelName != null) {
                    ItemModel model = Minecraft.getInstance().getModelManager().getItemModel(modelName);
                    if (model instanceof BlockModelWrapperExtension blockModel && blockModel.vivecraft$isGenerated()) {
                        return VivecraftItemTransformType.BLOCK_ITEM;
                    }
                }
                itemTransformType = VivecraftItemTransformType.BLOCK_3D;
            }
        } else if (item instanceof MapItem || itemStack.is(ViveItemTags.VIVECRAFT_MAPS)) {
            itemTransformType = VivecraftItemTransformType.MAP;
        } else if (itemStack.getUseAnimation() == ItemUseAnimation.BOW &&
            !itemStack.is(ViveItemTags.VIVECRAFT_BOW_EXCLUSION))
        {
            itemTransformType = VivecraftItemTransformType.BOW_SEATED;

            if (DH.bowTracker.isActive((LocalPlayer) player)) {
                if (DH.bowTracker.isDrawing()) {
                    itemTransformType = VivecraftItemTransformType.BOW_ROOMSCALE_DRAWING;
                } else {
                    itemTransformType = VivecraftItemTransformType.BOW_ROOMSCALE;
                }
            }
        } else if (itemStack.getUseAnimation() == ItemUseAnimation.TOOT_HORN) {
            itemTransformType = VivecraftItemTransformType.HORN;
        } else if (itemStack.is(ViveItemTags.VIVECRAFT_ROTATED_TOOLS)) {
            itemTransformType = VivecraftItemTransformType.ROTATED_TOOL;
        } else if (item instanceof MaceItem || itemStack.is(ViveItemTags.VIVECRAFT_MACES)) {
            itemTransformType = VivecraftItemTransformType.MACE;
        } else if (itemStack.is(ItemTags.SWORDS) || itemStack.is(ViveItemTags.VIVECRAFT_SWORDS)) {
            itemTransformType = VivecraftItemTransformType.SWORD;
        } else if (item instanceof ShieldItem || itemStack.is(ViveItemTags.VIVECRAFT_SHIELDS)) {
            itemTransformType = VivecraftItemTransformType.SHIELD;
        } else if (item instanceof TridentItem || itemStack.is(ViveItemTags.VIVECRAFT_SPEARS)) {
            itemTransformType = VivecraftItemTransformType.SPEAR;
        } else if (item instanceof CrossbowItem || itemStack.is(ViveItemTags.VIVECRAFT_CROSSBOWS)) {
            itemTransformType = VivecraftItemTransformType.CROSSBOW;
        } else if (item instanceof CompassItem || item == Items.CLOCK ||
            itemStack.is(ViveItemTags.VIVECRAFT_COMPASSES))
        {
            itemTransformType = VivecraftItemTransformType.COMPASS;
        } else if (SwingTracker.isTool(itemStack)) {
            itemTransformType = VivecraftItemTransformType.TOOL;

            if (item instanceof FoodOnAStickItem || item instanceof FishingRodItem ||
                itemStack.is(ViveItemTags.VIVECRAFT_FISHING_RODS))
            {
                itemTransformType = VivecraftItemTransformType.TOOL_ROD;
            }
        } else if (TelescopeTracker.isTelescope(itemStack)) {
            itemTransformType = VivecraftItemTransformType.TELESCOPE;
        } else if (itemStack.is(ItemTags.SPEARS) || itemStack.is(ViveItemTags.VIVECRAFT_LANCES)) {
            itemTransformType = VivecraftItemTransformType.LANCE;
        }
        return itemTransformType;
    }

    /**
     * modifies the given poseStack, so that a third person item transform is positioned in the hand
     *
     * @param poseStack         PoseStack to modify
     * @param itemTransformType itemTransformType of the item
     * @param mainHand          if the item is in the main hand or not
     * @param player            Player holding the item
     * @param equippedProgress  equip progress of the item
     * @param partialTick       current partial tick
     * @param itemStack         ItemStack the player is holding
     * @param hand              which hand the item is held in
     */
    public static void applyThirdPersonItemTransforms(
        PoseStack poseStack, VivecraftItemTransformType itemTransformType, boolean mainHand,
        AbstractClientPlayer player, float equippedProgress, float partialTick, ItemStack itemStack,
        InteractionHand hand)
    {
        // TODO make this work with stuff by default
        int k = mainHand ? 1 : -1;
        // scale so the item is slightly bigger than the hand
        float scale = 0.525F;
        float translateX = 0.0F;
        float translateY = 0.05F;
        float translateZ = 0.0F;
        boolean useLeftHandModelinLeftHand = false;

        // claws need the actual hand size
        if (ViveItems.isClimbingClaws(itemStack)) {
            scale = 0.4F;
        }

        // poseStack.mulPose(preRotation);
        poseStack.translate(translateX, translateY, translateZ);
        // poseStack.mulPose(rotation);
        poseStack.scale(scale, scale, scale);
    }

    /**
     * modifies the given poseStack, so that a first person item transform is positioned in the hand
     *
     * @param poseStack         PoseStack to modify
     * @param itemTransformType itemTransformType of the item
     * @param mainHand          if the item is in the main hand or not
     * @param player            Player holding the item
     * @param equippedProgress  equip progress of the item
     * @param partialTick       current partial tick
     * @param itemStack         ItemStack the player is holding
     * @param hand              which hand the item is held in
     */
    public static void applyFirstPersonItemTransforms(
        PoseStack poseStack, VivecraftItemTransformType itemTransformType, boolean mainHand,
        AbstractClientPlayer player, float equippedProgress, float partialTick, ItemStack itemStack,
        InteractionHand hand)
    {

        float gunAngle = DH.vr.getGunAngle();

        // defaults
        float scale = 0.7F;
        float translateX = -0.05F;
        float translateY = 0.005F;
        float translateZ = 0.0F;

        Quaternionf rotation = new Quaternionf();
        Quaternionf preRotation = new Quaternionf();
        rotation.mul(Axis.XP.rotationDegrees(-110.0F + gunAngle));

        switch (itemTransformType) {
            case BOW_SEATED -> {
                rotation.mul(Axis.XP.rotationDegrees(90.0F - gunAngle));
                translateY += -0.1F;
                translateZ += 0.1F;
            }
            case BOW_ROOMSCALE -> {
                preRotation.set(rotation);
                rotation.identity();
                translateX -= 0.0225F;
                translateY -= 0.25F;
                translateZ += 0.025F + 0.03F * gunAngle / 40.0F;
                scale = 1.0F;
            }
            case BOW_ROOMSCALE_DRAWING -> {
                // here there be dragons
                // reset
                rotation.identity();
                scale = 1.0F;

                int bowHand = 1;

                // bow in main hand
                if (DH.vrSettings.reverseShootingEye && ClientNetworking.supportsReversedBow()) {
                    bowHand = 0;
                }

                Vector3fc aim = DH.bowTracker.getAimVector();
                Vector3f forward = DH.vrPlayer.vrdata_world_render.getHand(bowHand).getCustomVector(MathUtils.FORWARD);

                if (DH.bowTracker.isCharged()) {
                    // bow jitter
                    long j = Util.getMillis() - DH.bowTracker.startDrawTime;
                    translateX += 0.003F * Mth.sin(j);
                }

                // un-do controller tracking
                poseStack.last().pose()
                    .mul(DH.vrPlayer.vrdata_world_render.getController(bowHand).getMatrix().transpose());

                // offset the bow model to be in line with the aim vector
                Vector3f up = aim.cross(forward, new Vector3f()).cross(aim).normalize().mul(0.1F);
                poseStack.translate(up.x(), up.y(), up.z());

                // align with controller
                preRotation = new Quaternionf().lookAlong(aim, forward).conjugate();

                // bow model adjustment
                rotation = Axis.YP.rotationDegrees(180.0F);
                rotation.mul(Axis.XP.rotationDegrees(160.0F));

                translateX += 0.125F;
                translateY += 0.1225F;
                translateZ += 0.16F;
            }
            case CROSSBOW -> {
                rotation = Axis.YP.rotationDegrees(10.0F);
                translateX += 0.01F;
                translateZ -= 0.02F;
                translateY -= 0.02F;
                scale = 0.5F;
            }
            case MAP -> {
                rotation = Axis.XP.rotationDegrees(-45.0F);
                translateX = 0.0F;
                translateY = 0.16F;
                translateZ = -0.075f;
                scale = 0.75F;
            }
            case NOMS -> {
                rotation = Axis.ZP.rotationDegrees(180.0F);
                rotation.mul(Axis.XP.rotationDegrees(-135.0F));
                translateX += 0.08F;
                // eating jitter
                translateZ += 0.02F + 0.006F * Mth.sin(player.getUseItemRemainingTicks());
                scale = 0.4F;
            }
            case ITEM, BLOCK_ITEM -> {
                rotation = Axis.ZP.rotationDegrees(180.0F);
                rotation.mul(Axis.XP.rotationDegrees(-135.0F));
                translateX += 0.08F;
                translateZ += -0.08F;
                scale = 0.4F;
            }
            case COMPASS -> {
                rotation = Axis.YP.rotationDegrees(90.0F);
                rotation.mul(Axis.XP.rotationDegrees(25.0F));
                scale = 0.4F;
            }
            case BLOCK_3D -> {
                translateX += 0.05F;
                translateZ -= 0.1F;
                rotation.mul(Axis.XP.rotationDegrees(90 - gunAngle));
                scale = 0.3F;
            }
            case BLOCK_STICK -> {
                rotation = Axis.XP.rotationDegrees(-45.0F + gunAngle);
                translateY += -0.105F + 0.06F * gunAngle / 40.0F;
                translateZ -= 0.1F;
            }
            case HORN -> {
                rotation = Axis.XP.rotationDegrees(-45.0F + gunAngle);
                translateY += -0.105F + 0.06F * gunAngle / 40.0F;
                translateZ -= 0.1F;
                scale = 0.3F;
            }
            case SHIELD -> {
                int side = mainHand ? 1 : -1;
                if (DH.vrSettings.reverseHands) {
                    side *= -1;
                }

                scale = 0.4F;

                // move so that the gui is not obstructed
                translateY += 0.18F;

                if (side == 1) {
                    rotation.mul(Axis.XP.rotationDegrees(105.0F - gunAngle));
                    translateX += 0.11F;
                } else {
                    rotation.mul(Axis.XP.rotationDegrees(115.0F - gunAngle));
                    translateX -= 0.015F;
                }

                translateZ += 0.1F;

                if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand) {
                    rotation.mul(Axis.XP.rotationDegrees(side * 5F));
                    rotation.mul(Axis.ZP.rotationDegrees(-5F));

                    if (side == 1) {
                        translateX += 0.04F;
                        translateY -= 0.12F;
                        translateZ -= 0.1F;
                    } else {
                        translateX += 0.19F;
                        translateY -= 0.12F;
                        translateZ -= 0.11F;
                    }

                    // use shield animation
                    if (player.isBlocking()) {
                        rotation.mul(Axis.YP.rotationDegrees(side * 90.0F));
                    } else {
                        rotation.mul(Axis.YP.rotationDegrees((1.0F - equippedProgress) * side * 90.0F));
                    }
                }
                rotation.mul(Axis.YP.rotationDegrees(side * -90.0F));
                if (player.getCooldowns().isOnCooldown(itemStack)) {
                    rotation.mul(Axis.ZP.rotationDegrees(side * 10.0F));
                    translateY -= 0.0055F;
                    translateZ -= 0.035F;
                }
            }
            case SPEAR -> {
                rotation.identity();
                translateX -= 0.135F;
                translateZ += 0.575F;
                scale = 0.6F;

                float progress = 0.0F;
                boolean charging = false;
                float riptideLevel = 0;

                if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand) {
                    charging = true;
                    riptideLevel = EnchantmentHelper.getTridentSpinAttackStrength(itemStack, player);

                    if (riptideLevel <= 0 || player.isInWaterOrRain()) {
                        progress =
                            itemStack.getUseDuration(player) - (player.getUseItemRemainingTicks() - partialTick + 1.0F);

                        if (progress > TridentItem.THROW_THRESHOLD_TIME) {
                            float rotationProgress = progress - TridentItem.THROW_THRESHOLD_TIME;
                            progress = TridentItem.THROW_THRESHOLD_TIME;

                            if (riptideLevel > 0 && player.isInWaterOrRain()) {
                                // rotation when charged, use item use, to start at 0
                                poseStack.mulPose(Axis.ZP.rotationDegrees(-rotationProgress * 10.0F * riptideLevel));
                            }

                            // every 4 frames at 90fps equals every 44ms
                            // TODO: this should not be FPS bound. do it in tick maybe? then it would be every 50ms
                            if (DH.frameIndex % 4L == 0L) {
                                // haptics when charged
                                DH.vr.triggerHapticPulse(mainHand ? 0 : 1, 200);
                            }

                            // charge jitter
                            translateX += 0.003F * (float) Math.sin(Util.getMillis());
                        }
                    }
                }

                if (player.isAutoSpinAttack()) {
                    riptideLevel = 5;
                    translateZ -= 0.15F;
                    poseStack.mulPose(Axis.ZP.rotationDegrees(
                        (-DH.tickCounter * 10 * riptideLevel) % 360 - partialTick * 10.0F * riptideLevel));
                    charging = true;
                }

                if (!charging) {
                    translateY += 0.2F * gunAngle / 40.0F;
                    rotation.mul(Axis.XP.rotationDegrees(gunAngle));
                }

                rotation.mul(Axis.XP.rotationDegrees(-65.0F));
                translateZ += -0.75F + progress / 10.0F * 0.25F;
            }
            case TOOL_ROD -> {
                rotation.mul(Axis.XP.rotationDegrees(40.0F));
                translateY += -0.02F + gunAngle / 40.0F * 0.1F;
                translateX += 0.05F;
                translateZ -= 0.15F;
                scale = 0.8F;
            }
            case ROTATED_TOOL -> {
                rotation.mul(Axis.XP.rotationDegrees(90.0F));
                translateY += -0.125F + gunAngle / 40.0F * 0.1F;
                translateZ -= 0.1F;
            }
            case TOOL -> {
                if (itemStack.getItem() instanceof ArrowItem || itemStack.is(ViveItemTags.VIVECRAFT_ARROWS)) {
                    preRotation = Axis.ZP.rotationDegrees(-180.0F);
                    rotation.mul(Axis.XP.rotationDegrees(-gunAngle));
                }
            }
            case TELESCOPE -> {
                preRotation.identity();
                rotation.identity();
                scale *= 0.625F;
                translateX = (mainHand ? -0.03F : 0.03F) * scale;
                translateY = 0.0F;
                translateZ = -0.1F * scale;
            }
            case MACE -> {
                preRotation = Axis.XP.rotationDegrees(gunAngle);
                rotation = rotation.mul(Axis.XP.rotationDegrees(-gunAngle));
                translateX = 0.0F;
                translateY = 0.0125F;
                translateZ = -0.06F;
                scale = 0.56F;
            }
            case LANCE -> {
                KineticWeapon kineticWeapon = itemStack.get(DataComponents.KINETIC_WEAPON);
                if (kineticWeapon != null) {
                    rotation.identity();
                    translateX = 0;
                    translateY = 0;

                    // copied from SpearAnimations.firstPersonUse, without  the sway translation and adjusted for the gun angle
                    float timeHeld =
                        itemStack.getUseDuration(player) - (player.getUseItemRemainingTicks() - partialTick + 1.0F);
                    float tickSinceLastHit = player.getTicksSinceLastKineticHitFeedback(partialTick);

                    SpearAnimations.UseParams params = SpearAnimations.UseParams.fromKineticWeapon(kineticWeapon,
                        timeHeld);
                    float sideRotation =
                        SpearAnimations.progress(params.raiseProgress(), 0.5F, 0.55F) - params.swayProgress();

                    // position at hand
                    poseStack.translate(-0.1375F + sideRotation * 0.01F, 0.005F + sideRotation * 0.01F, 0);

                    // state rotation
                    poseStack.rotateAround(Axis.XP.rotationDegrees(
                            -90.0F + gunAngle *
                                (1.0F - Ease.inOutBack(params.raiseProgress()) - params.lowerProgress() * 0.333F +
                                    params.raiseBackProgress() * 1.33F
                                ) + -0.5F * params.swayScaleFast()),
                        0.0F, 0.0F, 0.0F);
                    poseStack.rotateAround(
                        Axis.YN.rotationDegrees(90.0F * sideRotation + 2.0F * params.swayScaleSlow()),
                        0.15F, 0.0F, 0.0F);

                    // cancel item rotation
                    poseStack.mulPose(Axis.XP.rotationDegrees(10));

                    // move back when hitting
                    poseStack.translate(0.0F, -SpearAnimations.hitFeedbackAmount(tickSinceLastHit) * 0.2F, 0.0F);
                } else {
                    translateX = -0.1375F;
                    rotation.mul(Axis.XP.rotationDegrees(30));
                }
            }
            // case Sword -> {}
            default -> {}
        }

        poseStack.mulPose(preRotation);
        poseStack.translate(translateX, translateY, translateZ);
        poseStack.mulPose(rotation);
        poseStack.scale(scale, scale, scale);
    }

    public enum VivecraftItemTransformType {
        ITEM,
        BLOCK_3D,
        BLOCK_STICK,
        BLOCK_ITEM,
        SHIELD,
        SWORD,
        TOOL,
        TOOL_ROD,
        BOW_SEATED,
        BOW_ROOMSCALE,
        BOW_ROOMSCALE_DRAWING,
        SPEAR,
        MAP,
        NOMS,
        CROSSBOW,
        TELESCOPE,
        COMPASS,
        HORN,
        MACE,
        ROTATED_TOOL,
        LANCE
    }
}
