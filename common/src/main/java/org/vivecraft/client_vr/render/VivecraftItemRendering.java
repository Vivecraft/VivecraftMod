package org.vivecraft.client_vr.render;

import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ItemTags;
import org.vivecraft.client_vr.gameplay.trackers.SwingTracker;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.client_vr.provider.ControllerType;

import org.joml.Quaternionf;

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

import javax.annotation.Nonnull;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

import static org.joml.Math.*;

public class VivecraftItemRendering
{
    public static VivecraftItemTransformType getTransformType(ItemStack pStack, AbstractClientPlayer pPlayer, ItemRenderer itemRenderer) {
        VivecraftItemTransformType rendertype = VivecraftItemTransformType.Item;
        Item item = pStack.getItem();

        if (pStack.getUseAnimation() != UseAnim.EAT && pStack.getUseAnimation() != UseAnim.DRINK)
        {
            if (item instanceof BlockItem)
            {
                Block block = ((BlockItem)item).getBlock();

                if (block instanceof TorchBlock)
                {
                    rendertype = VivecraftItemTransformType.Block_Stick;
                }
                else
                {
                    BakedModel bakedmodel = itemRenderer.getModel(pStack, mc.level, mc.player, 0);

                    if (bakedmodel.isGui3d())
                    {
                        rendertype = VivecraftItemTransformType.Block_3D;
                    }
                    else
                    {
                        rendertype = VivecraftItemTransformType.Block_Item;
                    }
                }
            }
            else if (item instanceof MapItem || pStack.is(ItemTags.VIVECRAFT_MAPS))
            {
                rendertype = VivecraftItemTransformType.Map;
            }
            else if (pStack.getUseAnimation() == UseAnim.BOW)
            {
                rendertype = VivecraftItemTransformType.Bow_Seated;

                if (dh.bowTracker.isActive())
                {
                    if (dh.bowTracker.isDrawing)
                    {
                        rendertype = VivecraftItemTransformType.Bow_Roomscale_Drawing;
                    }
                    else
                    {
                        rendertype = VivecraftItemTransformType.Bow_Roomscale;
                    }
                }
            }
            else if (pStack.getUseAnimation() == UseAnim.TOOT_HORN)
            {
                rendertype = VivecraftItemTransformType.Horn;
            }
            else if (item instanceof SwordItem || pStack.is(ItemTags.VIVECRAFT_SWORDS))
            {
                rendertype = VivecraftItemTransformType.Sword;
            }
            else if (item instanceof ShieldItem || pStack.is(ItemTags.VIVECRAFT_SHIELDS))
            {
                rendertype = VivecraftItemTransformType.Shield;
            }
            else if (item instanceof TridentItem || pStack.is(ItemTags.VIVECRAFT_SPEARS))
            {
                rendertype = VivecraftItemTransformType.Spear;
            }
            else if (item instanceof CrossbowItem || pStack.is(ItemTags.VIVECRAFT_CROSSBOWS))
            {
                rendertype = VivecraftItemTransformType.Crossbow;
            }
            else if (item instanceof CompassItem || item == Items.CLOCK || pStack.is(ItemTags.VIVECRAFT_COMPASSES)) {
                rendertype = VivecraftItemTransformType.Compass;
            }
            else
            {
                if (SwingTracker.isTool(item))
                {
                    rendertype = VivecraftItemTransformType.Tool;

                    if (item instanceof FoodOnAStickItem || item instanceof FishingRodItem || pStack.is(ItemTags.VIVECRAFT_FISHING_RODS))
                    {
                        rendertype = VivecraftItemTransformType.Tool_Rod;
                    }
                }
                else if (TelescopeTracker.isTelescope(pStack))
                {
                    rendertype = VivecraftItemTransformType.Telescope;
                }
            }
        }
        else
        {
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
        int k = mainHand ? 1 : -1;
        float scale = 0.7F;
        float translateX = -0.05F;
        float translateY = 0.005F;
        float translateZ = 0.0F;
        boolean useLeftHandModelinLeftHand = false;

        double gunAngle = dh.vr.getGunAngle();
        Quaternionf rotation = new Quaternionf().rotationY(0.0F).rotateX(toRadians((float)(-110.0D + gunAngle)));
        Quaternionf preRotation = new Quaternionf().rotationY(0.0F);

        if (rendertype == VivecraftItemTransformType.Bow_Seated)
        {
            translateY -= 0.1D;
            translateZ += 0.1D;
            rotation.rotateX(toRadians((float)(90.0D - gunAngle)));
        }
        else if (rendertype == VivecraftItemTransformType.Bow_Roomscale)
        {
            rotation.rotationX(0.0F);
            float ang = (float)toRadians((-110.0D + gunAngle));
            pMatrixStack.last().pose().rotateX(ang);
            pMatrixStack.last().normal().rotateX(ang);
            translateY -= 0.25D;
            translateZ += (double)0.025F + 0.03D * gunAngle / 40.0D;
            translateX -= 0.0225D;
            scale = 1.0F;
        }
        else if (rendertype == VivecraftItemTransformType.Bow_Roomscale_Drawing)
        {
            rotation.rotationY(0.0F);
            scale = 1.0F;
            int i = dh.vrSettings.reverseShootingEye ? 1 : 0;

            Vec3 vec3 = dh.bowTracker.getAimVector();
            Vec3 vec31 = new Vec3(vec3.x, vec3.y, vec3.z);
            Vec3 vec32 = dh.vrPlayer.vrdata_world_render.getHand(1).getCustomVector(new Vec3(0.0D, -1.0D, 0.0D));
            Vec3 vec33 = dh.vrPlayer.vrdata_world_render.getHand(1).getCustomVector(new Vec3(0.0D, 0.0D, -1.0D));
            vec31.cross(vec32);
            float f = (float)toDegrees(asin(vec31.y / vec31.length()));
            float f1 = (float)toDegrees(atan2(vec31.x, vec31.z));
            Vec3 vec34 = new Vec3(0.0D, 1.0D, 0.0D);
            Vec3 vec35 = new Vec3(vec31.x, 0.0D, vec31.z);
            Vec3 vec36 = Vec3.ZERO;
            double d5 = vec33.dot(vec35);

            if (d5 != 0.0D)
            {
                vec36 = vec35.scale(d5);
            }

            Vec3 vec37 = vec33.subtract(vec36).normalize();
            double d6 = vec37.dot(vec34);
            double d7 = vec35.dot(vec37.cross(vec34));
            float f2;

            if (d7 < 0.0D)
            {
                f2 = -((float)acos(d6));
            }
            else
            {
                f2 = (float)acos(d6);
            }

            float f3 = (float)((180D / PI) * (double)f2);

            if (dh.bowTracker.isCharged())
            {
                long j = Util.getMillis() - dh.bowTracker.startDrawTime;
                translateX += 0.003D * sin(j);
            }

            pMatrixStack.last().pose()
                .translate(0.0F, 0.0F, 0.1F)
                .mul(dh.vrPlayer.vrdata_world_render.getController(1).getMatrix());
            rotation.rotateY(toRadians(f1));
            rotation.rotateX(toRadians(-f));
            rotation.rotateZ(toRadians(-f3));
            rotation.rotateZ(toRadians(180.0F));
            pMatrixStack.last().pose().rotate(rotation);
            rotation.rotationY(0.0F);
            rotation.rotateY(toRadians(180.0F));
            rotation.rotateX(toRadians(160.0F));
            translateY += 0.1225D;
            translateX += 0.125D;
            translateZ += 0.16D;
        }
        else if (rendertype == VivecraftItemTransformType.Crossbow)
        {
            translateX += 0.01F;
            translateZ -= 0.02F;
            translateY -= 0.02F;
            scale = 0.5F;
            rotation.rotationX(0.0F);
            rotation.rotateY(toRadians(10.0F));
        }
        else if (rendertype == VivecraftItemTransformType.Map)
        {
            rotation.rotationX(toRadians(-45.0F));
            translateX = 0.0F;
            translateY = 0.16F;
            translateZ = -0.075F;
            scale = 0.75F;
        }
        else if (rendertype == VivecraftItemTransformType.Noms)
        {
            long l = mc.player.getUseItemRemainingTicks();
            rotation.rotationZ(toRadians(180.0F));
            rotation.rotateX(toRadians(-135.0F));
            translateZ += 0.006F * sin(l);
            translateZ += 0.02F;
            translateX += 0.08F;
            scale = 0.4F;
        }
        else if (rendertype != VivecraftItemTransformType.Item && rendertype != VivecraftItemTransformType.Block_Item)
        {
            if (rendertype == VivecraftItemTransformType.Compass)
            {
                rotation.rotationY(toRadians(90.0F));
                rotation.rotateX(toRadians(25.0F));
                scale = 0.4F;
            }
            else if (rendertype == VivecraftItemTransformType.Block_3D)
            {
                scale = 0.3F;
                translateZ -= 0.1F;
                translateX += 0.05F;
            }
            else if (rendertype == VivecraftItemTransformType.Block_Stick)
            {
                rotation.rotationX(0.0F);
                translateY += -0.105D + 0.06D * gunAngle / 40.0D;
                translateZ -= 0.1F;
                rotation.rotateX(toRadians(-45.0F));
                rotation.rotateX(toRadians((float)gunAngle));
            }
            else if (rendertype == VivecraftItemTransformType.Horn)
            {
                scale = 0.3F;
                rotation = new Quaternionf().rotationX(0.0F);
                translateY += -0.105F + 0.06F * gunAngle / 40.0F;
                translateZ -= 0.1F;
                rotation.rotateX(toRadians(-45.0F));
                rotation.rotateX(toRadians((float)gunAngle));
            }
            else if (rendertype == VivecraftItemTransformType.Shield)
            {
                boolean reverse = dh.vrSettings.reverseHands && !dh.vrSettings.seated;
                if(reverse)k*=-1;
                scale = 0.4F;

                translateY += 0.18F;
                if (k==1)
                {
                    rotation.rotateX(toRadians((float)(105.0D - gunAngle)));
                    translateX += 0.11F;
                }
                else
                {
                    rotation.rotateX(toRadians((float)(115.0D - gunAngle)));
                    translateX -= 0.015F;
                }
                translateZ += 0.1F;

                if (pPlayer.isUsingItem() && pPlayer.getUseItemRemainingTicks() > 0 && pPlayer.getUsedItemHand() == pHand)
                {
                    rotation.rotateX(toRadians(k*5F));
                    rotation.rotateZ(toRadians(-5F));

                    translateY -= 0.12F;
                    if (k==1)
                    {
                        translateZ -= .1F;
                        translateX += .04F;
                    }
                    else
                    {
                        translateZ -= .11F;
                        translateX += 0.19F;
                    }


                    if (pPlayer.isBlocking())
                    {
                        rotation.rotateY(toRadians((float)k * 90.0F));
                    }
                    else
                    {
                        rotation.rotateY(toRadians((1.0F - pEquippedProgress)) * (float)k * 90.0F);
                    }
                }
                rotation.rotateY(toRadians((float)k * -90.0F));
            }
            else if (rendertype == VivecraftItemTransformType.Spear)
            {
                rotation.rotationX(0.0F);
                translateX -= 0.135F;
                translateZ += 0.575F;
                scale = 0.6F;
                float f4 = 0.0F;
                boolean flag5 = false;
                int i1;

                if (pPlayer.isUsingItem() && pPlayer.getUseItemRemainingTicks() > 0 && pPlayer.getUsedItemHand() == pHand)
                {
                    flag5 = true;
                    i1 = EnchantmentHelper.getRiptide(pStack);

                    if (i1 <= 0 || i1 > 0 && pPlayer.isInWaterOrRain())
                    {
                        f4 = (float)pStack.getUseDuration() - ((float)mc.player.getUseItemRemainingTicks() - pPartialTicks + 1.0F);

                        if (f4 > 10.0F)
                        {
                            f4 = 10.0F;

                            if (i1 > 0 && pPlayer.isInWaterOrRain())
                            {
                                float ang = toRadians((float)(-dh.tickCounter * 10 * i1 % 360) - pPartialTicks * 10.0F * (float)i1);
                                pMatrixStack.last().pose().rotateZ(ang);
                                pMatrixStack.last().normal().rotateZ(ang);
                            }

                            if (dh.frameIndex % 4L == 0L)
                            {
                                dh.vr.triggerHapticPulse(mainHand ? 0 : 1, 200);
                            }

                            long j1 = Util.getMillis() - dh.bowTracker.startDrawTime;
                            translateX += 0.003D * sin(j1);
                        }
                    }
                }

                if (pPlayer.isAutoSpinAttack())
                {
                    i1 = 5;
                    translateZ -= 0.15F;
                    float ang = toRadians((float)(-dh.tickCounter * 10 * i1 % 360) - pPartialTicks * 10.0F * (float)i1);
                    pMatrixStack.last().pose().rotateZ(ang);
                    pMatrixStack.last().normal().rotateZ(ang);
                    flag5 = true;
                }

                if (!flag5)
                {
                    translateY += 0.0F + 0.2F * gunAngle / 40.0F;
                    rotation.rotateX(toRadians((float)gunAngle));
                }

                rotation.rotateX(toRadians(-65.0F));
                translateZ += (-0.75F + f4 / 10.0F * 0.25F);
            }
            else if (rendertype != VivecraftItemTransformType.Sword)
            {
                if (rendertype == VivecraftItemTransformType.Tool_Rod)
                {
                    translateZ -= 0.15F;
                    translateY += -0.02D + gunAngle / 40.0D * 0.1D;
                    translateX += 0.05F;
                    rotation.rotateX(toRadians(40.0F));
                    scale = 0.8F;
                }
                else if (rendertype == VivecraftItemTransformType.Tool)
                {
                    boolean isClaws = dh.climbTracker.isClaws(pStack) && dh.climbTracker.isClimbeyClimb();

                    if (isClaws)
                    {
                        rotation.rotateX(toRadians((float)(-gunAngle)));
                        scale = 0.3F;
                        translateZ += 0.075F;
                        translateY += 0.02F;
                        translateX += 0.03F;

                        if (VivecraftVRMod.keyClimbeyGrab.isDown(ControllerType.RIGHT) && mainHand || VivecraftVRMod.keyClimbeyGrab.isDown(ControllerType.LEFT) && !mainHand)
                        {
                            translateZ -= 0.2F;
                        }
                    }

                    if (pStack.getItem() instanceof ArrowItem || pStack.is(ItemTags.VIVECRAFT_ARROWS))
                    {
                        preRotation.rotationZ(toRadians(-180.0F));
                        rotation.rotateX(toRadians((float)(-gunAngle)));
                    }
                }
                else if (rendertype == VivecraftItemTransformType.Telescope)
                {
                    preRotation.rotationX(0.0F);
                    rotation.rotationX(0.0F);
                    translateZ = 0.0F;
                    translateY = 0.0F;
                    translateX = 0.0F;
                }
            }
        }
        else
        {
            rotation.rotationZ(toRadians(180.0F));
            rotation.rotateX(toRadians(-135.0F));
            scale = 0.4F;
            translateX += 0.08F;
            translateZ -= 0.08F;
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

    public enum VivecraftItemTransformType
    {
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