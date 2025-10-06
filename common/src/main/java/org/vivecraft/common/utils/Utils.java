package org.vivecraft.common.utils;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

public class Utils {

    /**
     * estimate the head hitbox of an entity
     *
     * @param entity  Entity to get the head hitbox for
     * @param inflate by how much the hitbox should be enlarged
     * @return AABB describing the head hit box, or null the entity is unsupported
     */
    public static AABB getEntityHeadHitbox(Entity entity, double inflate) {
        // swimming players hitbox is just a box around their butt
        if ((entity instanceof Player player && !player.isSwimming()) ||
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
            entity instanceof Strider)
        {

            Vec3 headPos = entity.getEyePosition();
            double headsize = entity.getBbWidth() * 0.5;
            if (((LivingEntity) entity).isBaby()) {
                // babies have big heads
                headsize *= 1.20;
            }
            return new AABB(
                headPos.subtract(headsize, headsize - inflate, headsize),
                headPos.add(headsize, headsize + inflate, headsize))
                .inflate(inflate);
        } else if (!(entity instanceof EnderDragon) && // no ender dragon, the code doesn't work for it
            entity instanceof LivingEntity livingEntity)
        {

            float yRot = -livingEntity.yBodyRot * Mth.DEG_TO_RAD;
            // offset head in entity rotation
            Vec3 headPos = entity.getEyePosition()
                .add(new Vec3(Mth.sin(yRot), 0, Mth.cos(yRot))
                    .scale(livingEntity.getBbWidth() * 0.5F));

            double headsize = livingEntity.getBbWidth() * 0.25;
            if (livingEntity.isBaby()) {
                // babies have big heads
                headsize *= 1.5;
            }
            return new AABB(
                headPos.subtract(headsize, headsize, headsize),
                headPos.add(headsize, headsize, headsize))
                .inflate(inflate * 0.25)
                .expandTowards(headPos.subtract(entity.position()).scale(inflate));
        }
        return null;
    }

    /**
     * creates a new AABB, so that the given {@code point} is also inside
     *
     * @param aabb  AABB to modify
     * @param point point to include
     * @return extended AABB do include the original {@code aabb} and the given {@code point}
     */
    public static AABB includePoint(AABB aabb, Vec3 point) {
        return new AABB(
            Math.min(aabb.minX, point.x), Math.min(aabb.minY, point.y), Math.min(aabb.minZ, point.z),
            Math.max(aabb.maxX, point.x), Math.max(aabb.maxY, point.y), Math.max(aabb.maxZ, point.z));
    }

    /**
     * calculates the direction the {@code source} DamageSource hit the {@code target} Entity from
     *
     * @param source DamageSource to get the direction from
     * @param target Entity that received the damage
     * @return the direction the damage came from
     */
    public static Vector3fc getDirFromDamageSource(DamageSource source, Entity target) {
        if (source.getEntity() instanceof FallingBlockEntity) {
            // damage from above
            return MathUtils.UP;
        } else if (source.getSourcePosition() != null) {
            return MathUtils.subtractToVector3f(source.getSourcePosition(), target.getBoundingBox().getCenter())
                .normalize();
        } else if (source.getEntity() != null) {
            return MathUtils.subtractToVector3f(source.getEntity().position(), target.getBoundingBox().getCenter())
                .normalize();
        } else if (source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypeTags.BURN_FROM_STEPPING)) {
            // damage from the below
            return MathUtils.DOWN;
        } else {
            // no direction
            return MathUtils.ZERO;
        }
    }
}
