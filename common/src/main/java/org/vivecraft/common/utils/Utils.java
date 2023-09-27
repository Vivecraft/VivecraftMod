package org.vivecraft.common.utils;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Utils {

    public static AABB getEntityHeadHitbox(Entity entity, double inflate) {
        if ((entity instanceof Player player && !player.isSwimming()) || // swimming players hitbox is just a box around their butt
            entity instanceof Zombie ||
            entity instanceof AbstractPiglin ||
            entity instanceof AbstractSkeleton ||
            entity instanceof Witch ||
            entity instanceof AbstractIllager ||
            entity instanceof Blaze ||
            entity instanceof Creeper ||
            entity instanceof EnderMan ||
            entity instanceof AbstractVillager ||
            entity instanceof SnowGolem ||
            entity instanceof Vex ||
            entity instanceof Strider) {

            Vec3 headpos = entity.getEyePosition();
            double headsize = entity.getBbWidth() * 0.5;
            if (((LivingEntity) entity).isBaby()) {
                // babies have big heads
                headsize *= 1.20;
            }
            return new AABB(headpos.subtract(headsize, headsize - inflate, headsize), headpos.add(headsize, headsize + inflate, headsize)).inflate(inflate);
        } else if (!(entity instanceof EnderDragon) // no ender dragon, the code doesn't work for it
            && entity instanceof LivingEntity livingEntity) {

            float yrot = -(livingEntity.yBodyRot) * 0.017453292F;
            // offset head in entity rotation
            Vec3 headpos = entity.getEyePosition()
                .add(new Vec3(Mth.sin(yrot), 0, Mth.cos(yrot))
                    .scale(livingEntity.getBbWidth() * 0.5F));

            double headsize = livingEntity.getBbWidth() * 0.25;
            if (livingEntity.isBaby()) {
                // babies have big heads
                headsize *= 1.5;
            }
            return new AABB(headpos.subtract(headsize, headsize, headsize), headpos.add(headsize, headsize, headsize)).inflate(inflate * 0.25).expandTowards(headpos.subtract(entity.position()).scale(inflate));
        }
        return null;
    }
}
