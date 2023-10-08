package org.vivecraft.client_vr.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ItemTags;
import org.vivecraft.client_vr.gameplay.trackers.SwingTracker;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.client_vr.provider.ControllerType;

import javax.annotation.Nonnull;

import static org.joml.Math.*;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.convertToVec3;

public class VivecraftItemRendering {
    public static VivecraftItemTransformType getTransformType(ItemStack pStack, AbstractClientPlayer pPlayer, ItemRenderer itemRenderer) {
        VivecraftItemTransformType rendertype = VivecraftItemTransformType.Item;
        Item item = pStack.getItem();

        if (pStack.getUseAnimation() != UseAnim.EAT && pStack.getUseAnimation() != UseAnim.DRINK) {
            if (item instanceof BlockItem) {
                Block block = ((BlockItem) item).getBlock();

                if (block instanceof TorchBlock) {
                    rendertype = VivecraftItemTransformType.Block_Stick;
                } else {
                    BakedModel bakedmodel = itemRenderer.getModel(pStack, mc.level, mc.player, 0);

                    if (bakedmodel.isGui3d()) {
                        rendertype = VivecraftItemTransformType.Block_3D;
                    } else {
                        rendertype = VivecraftItemTransformType.Block_Item;
                    }
                }
            } else if (item instanceof MapItem || pStack.is(ItemTags.VIVECRAFT_MAPS)) {
                rendertype = VivecraftItemTransformType.Map;
            } else if (pStack.getUseAnimation() == UseAnim.BOW) {
                rendertype = VivecraftItemTransformType.Bow_Seated;

                if (dh.bowTracker.isActive()) {
                    if (dh.bowTracker.isDrawing) {
                        rendertype = VivecraftItemTransformType.Bow_Roomscale_Drawing;
                    } else {
                        rendertype = VivecraftItemTransformType.Bow_Roomscale;
                    }
                }
            } else if (pStack.getUseAnimation() == UseAnim.TOOT_HORN) {
                rendertype = VivecraftItemTransformType.Horn;
            } else if (item instanceof SwordItem || pStack.is(ItemTags.VIVECRAFT_SWORDS)) {
                rendertype = VivecraftItemTransformType.Sword;
            } else if (item instanceof ShieldItem || pStack.is(ItemTags.VIVECRAFT_SHIELDS)) {
                rendertype = VivecraftItemTransformType.Shield;
            } else if (item instanceof TridentItem || pStack.is(ItemTags.VIVECRAFT_SPEARS)) {
                rendertype = VivecraftItemTransformType.Spear;
            } else if (item instanceof CrossbowItem || pStack.is(ItemTags.VIVECRAFT_CROSSBOWS)) {
                rendertype = VivecraftItemTransformType.Crossbow;
            } else if (item instanceof CompassItem || item == Items.CLOCK || pStack.is(ItemTags.VIVECRAFT_COMPASSES)) {
                rendertype = VivecraftItemTransformType.Compass;
            } else {
                if (SwingTracker.isTool(item)) {
                    rendertype = VivecraftItemTransformType.Tool;

                    if (item instanceof FoodOnAStickItem || item instanceof FishingRodItem || pStack.is(ItemTags.VIVECRAFT_FISHING_RODS)) {
                        rendertype = VivecraftItemTransformType.Tool_Rod;
                    }
                } else if (TelescopeTracker.isTelescope(pStack)) {
                    rendertype = VivecraftItemTransformType.Telescope;
                }
            }
        } else {
            rendertype = VivecraftItemTransformType.Noms;
        }
        return rendertype;
    }

    public static void applyThirdPersonItemTransforms(@Nonnull PoseStack pMatrixStack, VivecraftItemTransformType rendertype, boolean mainHand, AbstractClientPlayer pPlayer, float pEquippedProgress, float pPartialTicks, ItemStack pStack, InteractionHand pHand) {
        //all TODO
        float scale = 0.525F;
        float translateX = 0.0F;
        float translateY = 0.05F;
        float translateZ = 0.0F;
        boolean useLeftHandModelinLeftHand = false;

        // pMatrixStack.last().pose().rotate(preRotation);
        // pMatrixStack.last().normal().rotate(preRotation);
        pMatrixStack.last().pose().translate(translateX, translateY, translateZ);
        // pMatrixStack.last().pose().rotate(rotation);
        // pMatrixStack.last().normal().rotate(rotation);
        pMatrixStack.scale(scale, scale, scale);
    }


    public static void applyFirstPersonItemTransforms(@Nonnull PoseStack pMatrixStack, VivecraftItemTransformType rendertype, boolean mainHand, AbstractClientPlayer pPlayer, float pEquippedProgress, float pPartialTicks, ItemStack pStack, InteractionHand pHand) {
        final float scale;
        float translateX = -0.05F;
        float translateY = 0.005F;
        float translateZ = 0.0F;
        boolean useLeftHandModelinLeftHand = false;

        float gunAngle = dh.vr.getGunAngle();
        Quaternionf rotation = new Quaternionf().rotationY(0.0F).rotateX(toRadians(-110.0F + gunAngle));
        Quaternionf preRotation = new Quaternionf().rotationY(0.0F);

        switch (rendertype) {
            case Bow_Seated -> {
                scale = 0.7F;
                translateY -= 0.1F;
                translateZ += 0.1F;
                rotation.rotateX((float) PI / 2.0F - toRadians(gunAngle));
            }
            case Bow_Roomscale -> {
                rotation.rotationX(0.0F);
                float ang = toRadians((-110.0F + gunAngle));
                pMatrixStack.last().pose().rotateX(ang);
                pMatrixStack.last().normal().rotateX(ang);
                translateY -= 0.25F;
                translateZ += 0.025F + 0.03F * gunAngle / 40.0F;
                translateX -= 0.0225F;
                scale = 1.0F;
            }
            case Bow_Roomscale_Drawing -> {
                rotation.rotationY(0.0F);
                scale = 1.0F;
                int i = dh.vrSettings.reverseShootingEye ? 1 : 0;

                Vector3fc vec31 = dh.bowTracker.getAimVector();
                // Vector3f vec32 = dh.vrPlayer.vrdata_world_render.getHand(1).getCustomVector(new Vector3f(0.0F, -1.0F, 0.0F));
                Vector3f vec33 = dh.vrPlayer.vrdata_world_render.getHand(1).getCustomVector(new Vector3f(0.0F, 0.0F, -1.0F));
                // vec31.cross(vec32); // TODO: wtf
                Vector3fc vec35 = new Vector3f(vec31.x(), 0.0F, vec31.z());
                Vector3f vec36 = new Vector3f();
                float f2 = vec33.dot(vec31.x(), 0.0F, vec31.z());

                if (f2 != 0.0F) {
                    vec35.mul(f2, vec36);
                }

                vec33.sub(vec36, vec36).normalize();
                float f3 = vec36.dot(0.0F, 1.0F, 0.0F);
                float f5 = acos(f3);

                if (dh.bowTracker.isCharged()) {
                    long j = Util.getMillis() - dh.bowTracker.startDrawTime;
                    translateX += 0.003F * sin(j);
                }

                pMatrixStack.last().pose()
                    .translate(0.0F, 0.0F, 0.1F)
                    .mul(dh.vrPlayer.vrdata_world_render.getController(1).getMatrix());
                rotation.rotateY(atan2(vec31.x(), vec31.z()));
                rotation.rotateX(-asin(vec31.y() / vec31.length()));
                rotation.rotateZ(-f5 * ((vec35.dot(vec36.cross(0.0F, 1.0F, 0.0F)) < 0.0F) ? -1 : 1));
                rotation.rotateZ((float) PI);
                pMatrixStack.last().pose().rotate(rotation);
                rotation.rotationY(0.0F);
                rotation.rotateY((float) PI);
                rotation.rotateX(toRadians(160.0F));
                translateY += 0.1225F;
                translateX += 0.125F;
                translateZ += 0.16F;
            }
            case Crossbow -> {
                translateX += 0.01F;
                translateZ -= 0.02F;
                translateY -= 0.02F;
                scale = 0.5F;
                rotation.rotationX(0.0F);
                rotation.rotateY((float) PI / 18.0F);
            }
            case Map -> {
                rotation.rotationX((float) PI / -4.0F);
                translateX = 0.0F;
                translateY = 0.16F;
                translateZ = -0.075F;
                scale = 0.75F;
            }
            case Noms -> {
                long l = mc.player.getUseItemRemainingTicks();
                rotation.rotationZ((float) PI);
                rotation.rotateX(toRadians(-135.0F));
                translateZ += 0.006F * sin(l);
                translateZ += 0.02F;
                translateX += 0.08F;
                scale = 0.4F;
            }
            case Compass -> {
                rotation.rotationY((float) PI / 2.0F);
                rotation.rotateX(toRadians(25.0F));
                scale = 0.4F;
            }
            case Block_3D -> {
                scale = 0.3F;
                translateZ -= 0.1F;
                translateX += 0.05F;
            }
            case Block_Stick -> {
                scale = 0.7F;
                rotation.rotationX(0.0F);
                translateY += -0.105F + 0.06F * gunAngle / 40.0F;
                translateZ -= 0.1F;
                rotation.rotateX((float) PI / -4.0F);
                rotation.rotateX(toRadians(gunAngle));
            }
            case Horn -> {
                scale = 0.3F;
                rotation = new Quaternionf().rotationX(0.0F);
                translateY += -0.105F + 0.06F * gunAngle / 40.0F;
                translateZ -= 0.1F;
                rotation.rotateX((float) PI / -4.0F);
                rotation.rotateX(toRadians(gunAngle));
            }
            case Shield -> {
                int hand = (mainHand || dh.vrSettings.seated) && !dh.vrSettings.reverseHands ? 1 : -1;
                scale = 0.4F;

                translateY += 0.18F;
                if (hand == 1) {
                    rotation.rotateX(toRadians(105.0F - gunAngle));
                    translateX += 0.11F;
                } else {
                    rotation.rotateX(toRadians(115.0F - gunAngle));
                    translateX -= 0.015F;
                }
                translateZ += 0.1F;

                if (pPlayer.isUsingItem() && pPlayer.getUseItemRemainingTicks() > 0 && pPlayer.getUsedItemHand() == pHand) {
                    rotation.rotateX(toRadians(hand * 5F));
                    rotation.rotateZ(toRadians(-5F));

                    translateY -= 0.12F;
                    if (hand == 1) {
                        translateZ -= .1F;
                        translateX += .04F;
                    } else {
                        translateZ -= .11F;
                        translateX += 0.19F;
                    }


                    if (pPlayer.isBlocking()) {
                        rotation.rotateY(hand * (float) PI / 2.0F);
                    } else {
                        rotation.rotateY(hand * toRadians(1.0F - pEquippedProgress) * ((float) PI / 2.0F));
                    }
                }
                rotation.rotateY(hand * (float) PI / -2.0F);
            }
            case Spear -> {
                rotation.rotationX(0.0F);
                translateX -= 0.135F;
                translateZ += 0.575F;
                scale = 0.6F;
                float f4 = 0.0F;
                boolean flag5 = false;
                int i1;

                if (pPlayer.isUsingItem() && pPlayer.getUseItemRemainingTicks() > 0 && pPlayer.getUsedItemHand() == pHand) {
                    flag5 = true;
                    i1 = EnchantmentHelper.getRiptide(pStack);

                    if (i1 <= 0 || i1 > 0 && pPlayer.isInWaterOrRain()) {
                        f4 = (float) pStack.getUseDuration() - ((float) mc.player.getUseItemRemainingTicks() - pPartialTicks + 1.0F);

                        if (f4 > 10.0F) {
                            f4 = 10.0F;

                            if (i1 > 0 && pPlayer.isInWaterOrRain()) {
                                float ang = toRadians((float) (-dh.tickCounter * 10 * i1 % 360) - pPartialTicks * 10.0F * (float) i1);
                                pMatrixStack.last().pose().rotateZ(ang);
                                pMatrixStack.last().normal().rotateZ(ang);
                            }

                            if (dh.frameIndex % 4L == 0L) {
                                dh.vr.triggerHapticPulse(mainHand ? 0 : 1, 200);
                            }

                            long j1 = Util.getMillis() - dh.bowTracker.startDrawTime;
                            translateX += 0.003F * sin(j1);
                        }
                    }
                }

                if (pPlayer.isAutoSpinAttack()) {
                    i1 = 5;
                    translateZ -= 0.15F;
                    float ang = toRadians((float) (-dh.tickCounter * 10 * i1 % 360) - pPartialTicks * 10.0F * (float) i1);
                    pMatrixStack.last().pose().rotateZ(ang);
                    pMatrixStack.last().normal().rotateZ(ang);
                    flag5 = true;
                }

                if (!flag5) {
                    translateY += 0.0F + 0.2F * gunAngle / 40.0F;
                    rotation.rotateX(toRadians(gunAngle));
                }

                rotation.rotateX(toRadians(-65.0F));
                translateZ += (-0.75F + f4 / 10.0F * 0.25F);
            }
            case Tool_Rod -> {
                translateZ -= 0.15F;
                translateY += -0.02F + gunAngle / 40.0F * 0.1F;
                translateX += 0.05F;
                rotation.rotateX(toRadians(40.0F));
                scale = 0.8F;
            }
            case Tool -> {
                boolean isClaws = dh.climbTracker.isClaws(pStack) && dh.climbTracker.isClimbeyClimb();

                if (isClaws) {
                    rotation.rotateX(toRadians(-gunAngle));
                    scale = 0.3F;
                    translateZ += 0.075F;
                    translateY += 0.02F;
                    translateX += 0.03F;

                    if (VivecraftVRMod.keyClimbeyGrab.isDown(ControllerType.RIGHT) && mainHand || VivecraftVRMod.keyClimbeyGrab.isDown(ControllerType.LEFT) && !mainHand) {
                        translateZ -= 0.2F;
                    }
                } else {
                    scale = 0.7F;
                }

                if (pStack.getItem() instanceof ArrowItem || pStack.is(ItemTags.VIVECRAFT_ARROWS)) {
                    preRotation.rotationZ(-(float) PI);
                    rotation.rotateX(toRadians(-gunAngle));
                }
            }
            case Telescope -> {
                scale = 0.7F;
                preRotation.rotationX(0.0F);
                rotation.rotationX(0.0F);
                translateZ = 0.0F;
                translateY = 0.0F;
                translateX = 0.0F;
            }
            case Item, Block_Item, Sword -> {
                scale = 0.7F;
            }
            default -> {
                rotation.rotationZ((float) PI);
                rotation.rotateX(toRadians(-135.0F));
                scale = 0.4F;
                translateX += 0.08F;
                translateZ -= 0.08F;
            }
        }

        pMatrixStack.last().pose()
            .rotate(preRotation)
            .translate(translateX, translateY, translateZ)
            .rotate(rotation);

        pMatrixStack.last().normal()
            .rotate(preRotation)
            .rotate(rotation);

        pMatrixStack.scale(scale, scale, scale);
    }

    public enum VivecraftItemTransformType {
        Item,
        Block_3D,
        Block_Stick,
        Block_Item,
        Shield,
        Sword,
        Tool,
        Tool_Rod,
        Bow_Seated,
        Bow_Roomscale,
        Bow_Roomscale_Drawing,
        Spear,
        Map,
        Noms,
        Crossbow,
        Telescope,
        Compass,
        Horn
    }
}
