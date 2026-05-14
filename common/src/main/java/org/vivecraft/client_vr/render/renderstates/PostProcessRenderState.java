package org.vivecraft.client_vr.render.renderstates;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.mod_compat_vr.iris.IrisHelper;

public class PostProcessRenderState {
    private boolean wasInWater;

    public float fovReduction = 1.0F;
    public float waterEffect;
    public float pumpkinEffect;
    public float portalEffect;
    public float red;
    public float black;
    public float blue;
    public float time;

    public void extract(float partialTick, boolean isInWater) {
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
        Minecraft mc = Minecraft.getInstance();

        // status effects
        this.red = 0.0F;
        this.black = 0.0F;
        this.blue = 0.0F;
        this.time = (float) Util.getMillis() / 1000.0F;

        this.pumpkinEffect = 0.0F;
        this.portalEffect = 0.0F;

        if (mc.player != null && mc.level != null) {
            if (dataHolder.vrSettings.waterEffect && this.wasInWater != isInWater) {
                // water state changed, start effect
                this.waterEffect = 2.3F;
            } else {
                if (isInWater) {
                    // slow falloff in water
                    this.waterEffect -= 1F / 120F;
                } else {
                    // fast falloff outside water
                    this.waterEffect -= 1F / 60F;
                }

                if (this.waterEffect < 0.0F) {
                    this.waterEffect = 0.0F;
                }
            }

            this.wasInWater = isInWater;

            if (IrisHelper.isLoaded() && !IrisHelper.hasWaterEffect()) {
                this.waterEffect = 0.0F;
            }

            float portalTime = Mth.lerp(partialTick, mc.player.oPortalEffectIntensity, mc.player.portalEffectIntensity);
            if (dataHolder.vrSettings.portalEffect &&
                // vanilla check for portal overlay
                portalTime > 0.0F)
            {
                this.portalEffect = portalTime;
            } else {
                this.portalEffect = 0.0F;
            }

            ItemStack itemstack = mc.player.getItemBySlot(EquipmentSlot.HEAD);

            if (dataHolder.vrSettings.pumpkinEffect && itemstack.getItem() == Blocks.CARVED_PUMPKIN.asItem() &&
                (!itemstack.has(DataComponents.CUSTOM_MODEL_DATA)))
            {
                this.pumpkinEffect = 1.0F;
            } else {
                this.pumpkinEffect = 0.0F;
            }

            float hurtTimer = (float) mc.player.hurtTime - partialTick;
            float healthPercent = 1.0F - mc.player.getHealth() / mc.player.getMaxHealth();
            healthPercent = (healthPercent - 0.5F) * 0.75F;

            if (dataHolder.vrSettings.hitIndicator && hurtTimer > 0.0F) { // hurt flash
                hurtTimer = hurtTimer / (float) mc.player.hurtDuration;
                hurtTimer = healthPercent +
                    Mth.sin(hurtTimer * hurtTimer * hurtTimer * hurtTimer * Mth.PI) * 0.5F;
                this.red = hurtTimer;
            } else if (dataHolder.vrSettings.lowHealthIndicator) { // red due to low health
                this.red = healthPercent * Mth.abs(Mth.sin((2.5F * this.time) / (1.0F - healthPercent + 0.1F)));

                if (mc.player.isCreative()) {
                    this.red = 0.0F;
                }
            }

            float freeze = mc.player.getPercentFrozen();
            if (dataHolder.vrSettings.freezeEffect && freeze > 0) {
                this.blue = this.red;
                this.blue = Math.max(freeze / 2, this.blue);
                this.red = 0;
            }

            if (dataHolder.vrSettings.sleepEffect && mc.player.isSleeping()) {
                this.black = 0.5F + 0.3F * mc.player.getSleepTimer() * 0.01F;
            }

            if (dataHolder.vr.isWalkingAbout && this.black < 0.8F) {
                this.black = 0.5F;
            }

            // fov reduction when moving
            if (dataHolder.vrSettings.useFOVReduction && dataHolder.vrPlayer.getFreeMove()) {
                if (Math.abs(mc.player.zza) > 0.0F || Math.abs(mc.player.xxa) > 0.0F) {
                    this.fovReduction = this.fovReduction - 0.05F;
                } else {
                    this.fovReduction = this.fovReduction + 0.01F;
                }
                this.fovReduction = Mth.clamp(this.fovReduction, dataHolder.vrSettings.fovReductionMin, 0.8F);
            } else {
                this.fovReduction = 1.0F;
            }
        } else {
            this.waterEffect = 0.0F;
            this.fovReduction = 1.0F;
        }
    }
}
