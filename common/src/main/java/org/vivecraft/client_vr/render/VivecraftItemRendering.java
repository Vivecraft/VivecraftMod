package org.vivecraft.client_vr.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.ItemTags;
import org.vivecraft.client_vr.gameplay.trackers.SwingTracker;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;

public class VivecraftItemRendering {
    private static final ClientDataHolderVR dh = ClientDataHolderVR.getInstance();

    public static VivecraftItemTransformType getTransformType(ItemStack pStack, AbstractClientPlayer pPlayer, ItemRenderer itemRenderer) {
        VivecraftItemTransformType rendertype = VivecraftItemTransformType.Item;
        Item item = pStack.getItem();
        Minecraft minecraft = Minecraft.getInstance();

        if (pStack.getUseAnimation() != UseAnim.EAT && pStack.getUseAnimation() != UseAnim.DRINK) {
            if (item instanceof BlockItem) {
                Block block = ((BlockItem) item).getBlock();

                if (block instanceof TorchBlock) {
                    rendertype = VivecraftItemTransformType.Block_Stick;
                } else {
                    BakedModel bakedmodel = itemRenderer.getModel(pStack, minecraft.level, minecraft.player, 0);

                    if (bakedmodel.isGui3d()) {
                        rendertype = VivecraftItemTransformType.Block_3D;
                    } else {
                        rendertype = VivecraftItemTransformType.Block_Item;
                    }
                }
            } else if (item instanceof MapItem || pStack.is(ItemTags.VIVECRAFT_MAPS)) {
                rendertype = VivecraftItemTransformType.Map;
            } else if (pStack.getUseAnimation() == UseAnim.BOW && !pStack.is(ItemTags.VIVECRAFT_BOW_EXCLUSION)) {
                rendertype = VivecraftItemTransformType.Bow_Seated;

                if (dh.bowTracker.isActive((LocalPlayer) pPlayer)) {
                    if (dh.bowTracker.isDrawing) {
                        rendertype = VivecraftItemTransformType.Bow_Roomscale_Drawing;
                    } else {
                        rendertype = VivecraftItemTransformType.Bow_Roomscale;
                    }
                }
            } else if (pStack.getUseAnimation() == UseAnim.TOOT_HORN) {
                rendertype = VivecraftItemTransformType.Horn;
            } else if (item instanceof MaceItem || pStack.is(ItemTags.VIVECRAFT_MACES)) {
                rendertype = VivecraftItemTransformType.Mace;
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

    public static void applyThirdPersonItemTransforms(PoseStack pMatrixStack, VivecraftItemTransformType rendertype, boolean mainHand, AbstractClientPlayer pPlayer, float pEquippedProgress, float pPartialTicks, ItemStack pStack, InteractionHand pHand) {
        //all TODO
        int k = mainHand ? 1 : -1;
        Minecraft minecraft = Minecraft.getInstance();
        double scale = 0.525D;
        double translateX = 0;
        double translateY = 0.05;
        double translateZ = 0;
        boolean useLeftHandModelinLeftHand = false;

        if (dh.climbTracker.isClaws(pStack)) {
            scale = 0.4D;
        }

//        pMatrixStack.mulPose(preRotation);
        pMatrixStack.translate(translateX, translateY, translateZ);
//        pMatrixStack.mulPose(rotation);
        pMatrixStack.scale((float) scale, (float) scale, (float) scale);
    }


    public static void applyFirstPersonItemTransforms(PoseStack pMatrixStack, VivecraftItemTransformType rendertype, boolean mainHand, AbstractClientPlayer pPlayer, float pEquippedProgress, float pPartialTicks, ItemStack pStack, InteractionHand pHand) {
        int k = mainHand ? 1 : -1;
        Minecraft minecraft = Minecraft.getInstance();
        double scale = 0.7D;
        double translateX = -0.05D;
        double translateY = 0.005D;
        double translateZ = 0.0D;
        boolean useLeftHandModelinLeftHand = false;

        double gunAngle = dh.vr.getGunAngle();
        Quaternionf rotation = Axis.YP.rotationDegrees(0.0F);
        Quaternionf preRotation = Axis.YP.rotationDegrees(0.0F);
        rotation.mul(Axis.XP.rotationDegrees((float) (-110.0D + gunAngle)));

        if (rendertype == VivecraftItemTransformType.Bow_Seated) {
            translateY += -0.1D;
            translateZ += 0.1D;
            rotation.mul(Axis.XP.rotationDegrees((float) (90.0D - gunAngle)));
            scale = 0.7F;
        } else if (rendertype == VivecraftItemTransformType.Bow_Roomscale) {
            rotation = Axis.XP.rotationDegrees(0.0F);
            pMatrixStack.mulPose(Axis.XP.rotationDegrees((float) (-110.0D + gunAngle)));
            translateY -= 0.25D;
            translateZ += (double) 0.025F + 0.03D * gunAngle / 40.0D;
            translateX += -0.0225D;
            scale = 1.0D;
        } else if (rendertype == VivecraftItemTransformType.Bow_Roomscale_Drawing) {
            rotation = Axis.YP.rotationDegrees(0.0F);
            scale = 1.0D;
            int i = 0;

            if (dh.vrSettings.reverseShootingEye) {
                i = 1;
            }

            Vec3 vec3 = dh.bowTracker.getAimVector();
            Vec3 vec31 = new Vec3(vec3.x, vec3.y, vec3.z);
            Vec3 vec32 = dh.vrPlayer.vrdata_world_render.getHand(1).getCustomVector(new Vec3(0.0D, -1.0D, 0.0D));
            Vec3 vec33 = dh.vrPlayer.vrdata_world_render.getHand(1).getCustomVector(new Vec3(0.0D, 0.0D, -1.0D));
            vec31.cross(vec32);
            double d4 = (180D / Math.PI) * Math.acos(vec31.dot(vec32));
            float f = (float) Math.toDegrees(Math.asin(vec31.y / vec31.length()));
            float f1 = (float) Math.toDegrees(Math.atan2(vec31.x, vec31.z));
            Vec3 vec34 = new Vec3(0.0D, 1.0D, 0.0D);
            Vec3 vec35 = new Vec3(vec31.x, 0.0D, vec31.z);
            Vec3 vec36 = Vec3.ZERO;
            double d5 = vec33.dot(vec35);

            if (d5 != 0.0D) {
                vec36 = vec35.scale(d5);
            }

            double d6 = 0.0D;
            Vec3 vec37 = vec33.subtract(vec36).normalize();
            d6 = vec37.dot(vec34);
            double d7 = vec35.dot(vec37.cross(vec34));
            float f2;

            if (d7 < 0.0D) {
                f2 = -((float) Math.acos(d6));
            } else {
                f2 = (float) Math.acos(d6);
            }

            float f3 = (float) ((180D / Math.PI) * (double) f2);

            if (dh.bowTracker.isCharged()) {
                long j = Util.getMillis() - dh.bowTracker.startDrawTime;
                translateX += 0.003D * Math.sin((double) j);
            }

            pMatrixStack.translate(0.0D, 0.0D, 0.1D);
            pMatrixStack.last().pose().mul(dh.vrPlayer.vrdata_world_render.getController(1).getMatrix().transposed().toMCMatrix());
            rotation.mul(Axis.YP.rotationDegrees(f1));
            rotation.mul(Axis.XP.rotationDegrees(-f));
            rotation.mul(Axis.ZP.rotationDegrees(-f3));
            rotation.mul(Axis.ZP.rotationDegrees(180.0F));
            pMatrixStack.last().pose().rotate(rotation);
            rotation = Axis.YP.rotationDegrees(0.0F);
            rotation.mul(Axis.YP.rotationDegrees(180.0F));
            rotation.mul(Axis.XP.rotationDegrees(160.0F));
            translateY += 0.1225D;
            translateX += 0.125D;
            translateZ += 0.16D;
        } else if (rendertype == VivecraftItemTransformType.Crossbow) {
            translateX += 0.01F;
            translateZ += -0.02F;
            translateY += -0.02F;
            scale = 0.5D;
            rotation = Axis.XP.rotationDegrees(0.0F);
            rotation.mul(Axis.YP.rotationDegrees(10.0F));
        } else if (rendertype == VivecraftItemTransformType.Map) {
            rotation = Axis.XP.rotationDegrees(-45.0F);
            translateX = 0.0D;
            translateY = 0.16D;
            translateZ = -0.075D;
            scale = 0.75D;
        } else if (rendertype == VivecraftItemTransformType.Noms) {
            long l = minecraft.player.getUseItemRemainingTicks();
            rotation = Axis.ZP.rotationDegrees(180.0F);
            rotation.mul(Axis.XP.rotationDegrees(-135.0F));
            translateZ = translateZ + 0.006D * Math.sin((double) l);
            translateZ = translateZ + (double) 0.02F;
            translateX += 0.08F;
            scale = 0.4F;
        } else if (rendertype != VivecraftItemTransformType.Item && rendertype != VivecraftItemTransformType.Block_Item) {
            if (rendertype == VivecraftItemTransformType.Compass) {
                rotation = Axis.YP.rotationDegrees(90.0F);
                rotation.mul(Axis.XP.rotationDegrees(25.0F));
                scale = 0.4F;
            } else if (rendertype == VivecraftItemTransformType.Block_3D) {
                scale = 0.3F;
                translateZ += -0.1F;
                translateX += 0.05F;
            } else if (rendertype == VivecraftItemTransformType.Block_Stick) {
                rotation = Axis.XP.rotationDegrees(0.0F);
                translateY += -0.105D + 0.06D * gunAngle / 40.0D;
                translateZ += -0.1F;
                rotation.mul(Axis.XP.rotationDegrees(-45.0F));
                rotation.mul(Axis.XP.rotationDegrees((float) gunAngle));
            } else if (rendertype == VivecraftItemTransformType.Horn) {
                scale = 0.3F;
                rotation = Axis.XP.rotationDegrees(0.0F);
                translateY += -0.105D + 0.06D * gunAngle / 40.0D;
                translateZ += -0.1F;
                rotation.mul(Axis.XP.rotationDegrees(-45.0F));
                rotation.mul(Axis.XP.rotationDegrees((float) gunAngle));
            } else if (rendertype == VivecraftItemTransformType.Shield) {
                boolean reverse = dh.vrSettings.reverseHands;
                if (reverse) {
                    k *= -1;
                }
                scale = 0.4F;

                translateY += 0.18F;
                //
                if (k == 1) {
                    rotation.mul(Axis.XP.rotationDegrees((float) (105.0D - gunAngle)));
                    translateX += 0.11F;
                } else {
                    rotation.mul(Axis.XP.rotationDegrees((float) (115.0D - gunAngle)));
                    translateX += -0.015D;
                }
                ////
                translateZ += 0.1F;

                if (pPlayer.isUsingItem() && pPlayer.getUseItemRemainingTicks() > 0 && pPlayer.getUsedItemHand() == pHand) {
                    rotation.mul(Axis.XP.rotationDegrees(k * 5F));
                    rotation.mul(Axis.ZP.rotationDegrees(-5F));

                    if (k == 1) {
                        translateY += -0.12F;
                        translateZ += -.1F;
                        translateX += .04F;
                    } else {
                        translateY += -0.12F;
                        translateZ += -.11F;
                        translateX += 0.19F;
                    }


                    ////
                    if (pPlayer.isBlocking()) {
                        rotation.mul(Axis.YP.rotationDegrees((float) k * 90.0F));
                    } else {
                        rotation.mul(Axis.YP.rotationDegrees((1.0F - pEquippedProgress) * (float) k * 90.0F));
                    }
                    ////
                }
                ////
                rotation.mul(Axis.YP.rotationDegrees((float) k * -90.0F));
            } else if (rendertype == VivecraftItemTransformType.Spear) {
                rotation = Axis.XP.rotationDegrees(0.0F);
                translateX += -0.135F;
                translateZ = translateZ + (double) 0.575F;
                scale = 0.6F;
                float f4 = 0.0F;
                boolean flag5 = false;
                float i1 = 0;

                if (pPlayer.isUsingItem() && pPlayer.getUseItemRemainingTicks() > 0 && pPlayer.getUsedItemHand() == pHand) {
                    flag5 = true;
                    i1 = EnchantmentHelper.getTridentSpinAttackStrength(pStack, pPlayer);

                    if (i1 <= 0 || i1 > 0 && pPlayer.isInWaterOrRain()) {
                        f4 = (float) pStack.getUseDuration(pPlayer) - ((float) minecraft.player.getUseItemRemainingTicks() - pPartialTicks + 1.0F);

                        if (f4 > 10.0F) {
                            f4 = 10.0F;

                            if (i1 > 0 && pPlayer.isInWaterOrRain()) {
                                pMatrixStack.mulPose(Axis.ZP.rotationDegrees((float) (-dh.tickCounter * 10 * i1 % 360) - pPartialTicks * 10.0F * (float) i1));
                            }

                            if (dh.frameIndex % 4L == 0L) {
                                dh.vr.triggerHapticPulse(mainHand ? 0 : 1, 200);
                            }

                            long j1 = Util.getMillis() - dh.bowTracker.startDrawTime;
                            translateX += 0.003D * Math.sin((double) j1);
                        }
                    }
                }

                if (pPlayer.isAutoSpinAttack()) {
                    i1 = 5;
                    translateZ += -0.15F;
                    pMatrixStack.mulPose(Axis.ZP.rotationDegrees((float) (-dh.tickCounter * 10 * i1 % 360) - pPartialTicks * 10.0F * (float) i1));
                    flag5 = true;
                }

                if (!flag5) {
                    translateY += 0.0D + 0.2D * gunAngle / 40.0D;
                    rotation.mul(Axis.XP.rotationDegrees((float) gunAngle));
                }

                rotation.mul(Axis.XP.rotationDegrees(-65.0F));
                translateZ = translateZ + (double) (-0.75F + f4 / 10.0F * 0.25F);
            } else if (rendertype != VivecraftItemTransformType.Sword) {
                if (rendertype == VivecraftItemTransformType.Tool_Rod) {
                    translateZ += -0.15F;
                    translateY += -0.02D + gunAngle / 40.0D * 0.1D;
                    translateX += 0.05F;
                    rotation.mul(Axis.XP.rotationDegrees(40.0F));
                    scale = 0.8F;
                } else if (rendertype == VivecraftItemTransformType.Tool) {
                    if (pStack.getItem() instanceof ArrowItem || pStack.is(ItemTags.VIVECRAFT_ARROWS)) {
                        preRotation = Axis.ZP.rotationDegrees(-180.0F);
                        rotation.mul(Axis.XP.rotationDegrees((float) (-gunAngle)));
                    }
                } else if (rendertype == VivecraftItemTransformType.Telescope) {
                    preRotation = Axis.XP.rotationDegrees(0.0F);
                    rotation = Axis.XP.rotationDegrees(0.0F);
                    translateZ = 0.0D;
                    translateY = 0.0D;
                    translateX = 0.0D;
                } else if (rendertype == VivecraftItemTransformType.Mace) {
                    translateX = 0.00D;
                    translateY = 0.02D;
                    translateZ = -0.07D;
                }
            }
        } else {
            rotation = Axis.ZP.rotationDegrees(180.0F);
            rotation.mul(Axis.XP.rotationDegrees(-135.0F));
            scale = 0.4F;
            translateX += 0.08F;
            translateZ += -0.08F;
        }

        pMatrixStack.mulPose(preRotation);
        pMatrixStack.translate(translateX, translateY, translateZ);
        pMatrixStack.mulPose(rotation);
        pMatrixStack.scale((float) scale, (float) scale, (float) scale);
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
        Horn,
        Mace
    }
}
