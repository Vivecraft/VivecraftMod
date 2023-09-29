package org.vivecraft.common.utils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult.Type;
import org.joml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivecraft.common.utils.color.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

import static org.joml.Math.*;

@ParametersAreNonnullByDefault
public class Utils {

    private static final Vector3dc dforward = new Vector3d(0.0F, 0.0F, -1.0F);
    public static final Vector3fc forward = new Vector3f(0.0F, 0.0F, -1.0F);
    private static final Vector3dc dbackward = new Vector3d(0.0F, 0.0F, 1.0F);
    public static final Vector3fc backward = new Vector3f(0.0F, 0.0F, 1.0F);
    private static final Vector3dc ddown = new Vector3d(0.0F, -1.0F, 0.0F);
    public static final Vector3fc down = new Vector3f(0.0F, -1.0F, 0.0F);
    private static final Vector3dc dup = new Vector3d(0.0F, 1.0F, 0.0F);
    public static final Vector3fc up = new Vector3f(0.0F, 1.0F, 0.0F);
    private static final Vector3dc dleft = new Vector3d(-1.0F, 0.0F, 0.0F);
    public static final Vector3fc left = new Vector3f(-1.0F, 0.0F, 0.0F);
    private static final Vector3dc dright = new Vector3d(1.0F, 0.0F, 0.0F);
    public static final Vector3fc right = new Vector3f(1.0F, 0.0F, 0.0F);

    public static Vector3dc forward() {
        return dforward;
    }

    public static Vector3dc backward() {
        return dbackward;
    }

    public static Vector3dc up() {
        return dup;
    }

    public static Vector3dc down() {
        return ddown;
    }

    public static Vector3dc right() {
        return dright;
    }

    public static Vector3dc left() {
        return dleft;
    }

    /**
     * Convert a {@link net.minecraft.world.phys.Vec3} to a {@link Color}.
     * <br>
     * This function is helpful for handling Optifine methods.
     *
     * @param vector the vector to convert.
     * @param dest   the {@link Color} to save to.
     * @return the {@code dest} {@link Color} containing the x, y, and z components of {@code vector} cast to floats.
     * @apiNote Avoid this function whenever reasonably possible.
     * @see Color
     */
    public static Color convertToColor(@Nullable net.minecraft.world.phys.Vec3 vector, Color dest) {
        return vector != null ? dest.set((float) vector.x, (float) vector.y, (float) vector.z) : null;
    }

    /**
     * Convert a {@link Color} to a {@link net.minecraft.world.phys.Vec3}.
     * This function is helpful for handling Optfine methods.
     *
     * @param color the color to convert.
     * @return a new {@link net.minecraft.world.phys.Vec3} whose x, y, and z components are equal to the {@link Color#r}, {@link Color#g}, and {@link Color#b} components of {@code color}.
     * @apiNote Use this function whenever reasonably possible.
     * <br>
     * {@link net.minecraft.world.phys.Vec3} is superseded by {@link Vector3d} in any color context.
     * @see Color
     */
    public static net.minecraft.world.phys.Vec3 convertToVec3(Color color) {
        return new net.minecraft.world.phys.Vec3(color.getR(), color.getG(), color.getB());
    }

    /**
     * Convert a {@link net.minecraft.world.phys.Vec3} to a {@link Vector3d}.
     *
     * @param vector the vector to copy.
     * @param dest   the {@link Vector3d} to save to.
     * @return the {@code dest} containing the same x, y, and z components of vector.
     * @apiNote Use this function whenever reasonably possible.
     * <br>
     * {@link net.minecraft.world.phys.Vec3} is superseded by {@link Vector3d} in any math context.
     * @see Utils#convertToVec3(Vector3dc)
     */
    public static Vector3d convertToVector3d(@Nullable net.minecraft.world.phys.Vec3 vector, @Nonnull Vector3d dest) {
        return vector != null ? dest.set(vector.x, vector.y, vector.z) : dest;
    }

    /**
     * Convert a {@link net.minecraft.world.phys.Vec3} to a {@link Vector3f}.
     *
     * @param vector the vector to copy.
     * @param dest   the {@link Vector3f} to save to.
     * @return the {@code dest} containing the same x, y, and z components of vector.
     * @apiNote Use this function only when float values are required!
     * <br>
     * When converting {@link net.minecraft.world.phys.Vec3}, prefer this function over
     * {@link net.minecraft.world.phys.Vec3#toVector3f()}
     * to avoid creating multiple objects.
     * @see Utils#convertToVec3(Vector3fc)
     */
    public static Vector3f convertToVector3f(@Nullable net.minecraft.world.phys.Vec3 vector, @Nonnull Vector3f dest) {
        return vector != null ? dest.set((float) vector.x, (float) vector.y, (float) vector.z) : dest;
    }

    /**
     * Convert a JOML {@link Vector3d} to a {@link net.minecraft.world.phys.Vec3}.
     *
     * @param vector the vector to copy.
     * @return a new Vec3 containing the same x, y, and z components of vector.
     * @apiNote Avoid this function whenever reasonably possible.
     * <br>
     * If there is an x, y, z signature alternative,
     * <br>
     * instead save {@code vector} and use its {@link Vector3d#x}, {@link Vector3d#y}, and {@link Vector3d#z} directly.
     * @see Utils#convertToVector3d(net.minecraft.world.phys.Vec3, Vector3d)
     */
    public static net.minecraft.world.phys.Vec3 convertToVec3(Vector3dc vector) {
        return new net.minecraft.world.phys.Vec3(vector.x(), vector.y(), vector.z());
    }

    /**
     * Convert a JOML {@link Vector3f} to a {@link net.minecraft.world.phys.Vec3}.
     *
     * @param vector the vector to copy.
     * @return a new Vec3 containing the same x, y, and z components of vector.
     * @apiNote Avoid this function whenever reasonably possible.
     * <br>
     * If there is an x, y, z signature alternative,
     * <br>
     * instead save {@code vector} and use its {@link Vector3f#x}, {@link Vector3f#y}, and {@link Vector3f#z} directly.
     * @see Utils#convertToVector3d(net.minecraft.world.phys.Vec3, Vector3d)
     */
    public static net.minecraft.world.phys.Vec3 convertToVec3(Vector3fc vector) {
        return new net.minecraft.world.phys.Vec3(vector.x(), vector.y(), vector.z());
    }

    /**
     * Convert a row-major 3x4 matrix, like {@link org.lwjgl.openvr.HmdMatrix34}, to a column-major 4x4 matrix.
     * This function is required for org.lwjgl.openvr compatibility,
     * as JOML will only read-in values using column-major layout, whereas
     * {@link org.lwjgl.openvr} <a href="https://github.com/ValveSoftware/openvr/wiki/Matrix-Usage-Example">uses row-major layout.</a>
     *
     * @see Utils#convertRM34ToCM44(DoubleBuffer, Matrix4d)
     */
    public static Matrix4f convertRM34ToCM44(FloatBuffer floatBuffer, Matrix4f dest) {
        return dest.set(
            floatBuffer.get(0), floatBuffer.get(4), floatBuffer.get(8), 0.0F,
            floatBuffer.get(1), floatBuffer.get(5), floatBuffer.get(9), 0.0F,
            floatBuffer.get(2), floatBuffer.get(6), floatBuffer.get(10), 0.0F,
            floatBuffer.get(3), floatBuffer.get(7), floatBuffer.get(11), 1.0F
        );
    }

    /**
     * Convert a row-major 3x4 matrix, like {@link org.lwjgl.openvr.HmdMatrix34}, to a column-major 4x4 matrix.
     * This function is required for org.lwjgl.openvr compatibility,
     * as JOML will only read-in values using column-major layout, whereas
     * {@link org.lwjgl.openvr} <a href="https://github.com/ValveSoftware/openvr/wiki/Matrix-Usage-Example">uses row-major layout.</a>
     *
     * @see Utils#convertRM34ToCM44(FloatBuffer, Matrix4d)
     */
    public static Matrix4d convertRM34ToCM44(FloatBuffer floatBuffer, Matrix4d dest) {
        return dest.set(
            floatBuffer.get(0), floatBuffer.get(4), floatBuffer.get(8), 0.0F,
            floatBuffer.get(1), floatBuffer.get(5), floatBuffer.get(9), 0.0F,
            floatBuffer.get(2), floatBuffer.get(6), floatBuffer.get(10), 0.0F,
            floatBuffer.get(3), floatBuffer.get(7), floatBuffer.get(11), 1.0F
        );
    }

    /**
     * Convert a row-major 3x4 matrix to a column-major 4x4 matrix.
     *
     * @see Utils#convertRM34ToCM44(FloatBuffer, Matrix4f)
     */
    public static Matrix4d convertRM34ToCM44(DoubleBuffer doubleBuffer, Matrix4d dest) {
        return dest.set(
            doubleBuffer.get(0), doubleBuffer.get(4), doubleBuffer.get(8), 0.0F,
            doubleBuffer.get(1), doubleBuffer.get(5), doubleBuffer.get(9), 0.0F,
            doubleBuffer.get(2), doubleBuffer.get(6), doubleBuffer.get(10), 0.0F,
            doubleBuffer.get(3), doubleBuffer.get(7), doubleBuffer.get(11), 1.0F
        );
    }

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
            entity instanceof Strider
        ) {
            Vector3d headpos = convertToVector3d(entity.getEyePosition(), new Vector3d());
            double headsize = entity.getBbWidth() * 0.5;
            if (((LivingEntity) entity).isBaby()) {
                // babies have big heads
                headsize *= 1.20;
            }
            Vector3d minHead = headpos.sub(headsize, headsize - inflate, headsize, new Vector3d());
            Vector3d maxHead = headpos.add(headsize, headsize + inflate, headsize, new Vector3d());

            return new AABB(
                minHead.x, minHead.y, minHead.z,
                maxHead.x, maxHead.y, maxHead.z
            ).inflate(inflate);
        }
        // ender dragon head hitbox is unsuppported since the code doesn't work for it
        else if (!(entity instanceof EnderDragon) && entity instanceof LivingEntity livingEntity) {
            float yRot = toRadians(-livingEntity.yBodyRot);
            // offset head in entity rotation
            Vector3d headpos = (convertToVector3d(entity.getEyePosition(), new Vector3d())
                .add(sin(yRot), 0.0D, cos(yRot))
                .mul(livingEntity.getBbWidth() * 0.5D)
            );
            double headsize = livingEntity.getBbWidth() * 0.25D;
            if (livingEntity.isBaby()) {
                // babies have big heads
                headsize *= 1.5D;
            }
            Vector3d minHead = headpos.sub(headsize, headsize, headsize, new Vector3d());
            Vector3d maxHead = headpos.add(headsize, headsize, headsize, new Vector3d());

            return (
                new AABB(
                    minHead.x, minHead.y, minHead.z,
                    maxHead.x, maxHead.y, maxHead.z
                )
                    .inflate(inflate * 0.25D)
                    .expandTowards(convertToVec3(headpos.sub(convertToVector3d(entity.position(), new Vector3d())).mul(inflate)))
            );
        }
        return null;
    }

    public static boolean canEntityBeSeen(Entity entity, Vector3dc playerEyePos) {
        return entity.level().clip(
            new ClipContext(convertToVec3(playerEyePos), entity.getEyePosition(), Block.COLLIDER, Fluid.NONE, entity)
        ).getType() == Type.MISS;
    }

    /**
     * Vivecraft's logger for printing to console.
     */
    public static final Logger logger = LoggerFactory.getLogger("Vivecraft");

    public static void printStackIfContainsClass(String className) {
        StackTraceElement[] astacktraceelement = Thread.currentThread().getStackTrace();
        boolean flag = false;

        for (StackTraceElement stacktraceelement : astacktraceelement) {
            if (stacktraceelement.getClassName().equals(className)) {
                flag = true;
                break;
            }
        }

        if (flag) {
            Thread.dumpStack();
        }
    }

    public static long microTime() {
        return System.nanoTime() / 1000L;
    }

    public static long milliTime() {
        return System.nanoTime() / 1000000L;
    }
}
