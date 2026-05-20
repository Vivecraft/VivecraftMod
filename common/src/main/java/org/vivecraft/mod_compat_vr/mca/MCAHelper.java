package org.vivecraft.mod_compat_vr.mca;

import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;
import org.vivecraft.Xloader;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class MCAHelper {

    private static boolean INITIALIZED = false;
    private static boolean INIT_FAILED = false;

    private static Field MCAClient_playerData;

    private static Method VillagerLike_getRawScaleFactor;
    private static Method VillagerLike_getHorizontalScaleFactor;

    public static boolean isLoaded() {
        return Xloader.INSTANCE.isModLoaded("mca");
    }

    public static void undoPlayerScale(LivingEntity player, Vector3f pos) {
        if (init()) {
            try {
                Object villagerData = ((Map) MCAClient_playerData.get(null)).get(player.getUUID());
                if (villagerData != null) {
                    float heightScale = (float) VillagerLike_getRawScaleFactor.invoke(villagerData);
                    float widthScale = (float) VillagerLike_getHorizontalScaleFactor.invoke(villagerData);
                    pos.x /= widthScale;
                    pos.z /= widthScale;
                    pos.y /= heightScale;
                }
            } catch (IllegalAccessException | InvocationTargetException ignore) {}
        }
    }

    public static void applyPlayerScale(LivingEntity player, Vector3f pos) {
        if (init()) {
            try {
                Object villagerData = ((Map) MCAClient_playerData.get(null)).get(player.getUUID());
                if (villagerData != null) {
                    float heightScale = (float) VillagerLike_getRawScaleFactor.invoke(villagerData);
                    float widthScale = (float) VillagerLike_getHorizontalScaleFactor.invoke(villagerData);
                    pos.x *= widthScale;
                    pos.z *= widthScale;
                    pos.y *= heightScale;
                }
            } catch (IllegalAccessException | InvocationTargetException ignore) {}
        }
    }

    private static boolean init() {
        if (INITIALIZED) {
            return !INIT_FAILED;
        } else {
            try {
                MCAClient_playerData = ClassUtils.getClassWithAlternative(
                        Xloader.INSTANCE.getModloader().name + ".net.mca.MCAClient", "net.conczin.mca.MCAClient")
                    .getField("playerData");

                Class<?> VillagerLike = ClassUtils.getClassWithAlternative(
                    Xloader.INSTANCE.getModloader().name + ".net.mca.entity.VillagerLike",
                    "net.conczin.mca.entity.VillagerLike");

                VillagerLike_getRawScaleFactor = ClassUtils.getMethodWithAlternative(VillagerLike,
                    "getRawScaleFactor", "getRawVerticalScaleFactor");
                VillagerLike_getHorizontalScaleFactor = VillagerLike.getMethod("getHorizontalScaleFactor");
            } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException e) {
                INIT_FAILED = true;
                VRSettings.LOGGER.error("Vivecraft: Failed to initialize MCA compat", e);
            }
        }

        INITIALIZED = true;
        return !INIT_FAILED;
    }
}
