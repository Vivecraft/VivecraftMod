package org.vivecraft.mod_compat_vr.pehkui;

import net.minecraft.world.entity.Entity;
import org.vivecraft.Xloader;
import org.vivecraft.client_vr.settings.VRSettings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PehkuiHelper {

    private static boolean INITIALIZED = false;
    private static boolean INIT_FAILED = false;

    private static Method ScaleUtils_getEyeHeightScale;
    private static Method ScaleUtils_getBoundingBoxHeightScale;

    public static boolean isLoaded() {
        return Xloader.INSTANCE.isModLoaded("pehkui");
    }

    /**
     * gets the current eye height scale of the give Entity
     *
     * @param entity      Entity to get the eye height scale for
     * @param partialTick current partial tick
     * @return scale of the entities eye height
     */
    public static float getEntityEyeHeightScale(Entity entity, float partialTick) {
        if (init()) {
            try {
                return (float) ScaleUtils_getEyeHeightScale.invoke(null, entity, partialTick);
            } catch (InvocationTargetException | IllegalAccessException e) {
                VRSettings.LOGGER.error("Vivecraft: couldn't get pehkui entity eye height scale:", e);
            }
        }
        return 1F;
    }

    /**
     * gets the current bounding box scale of the give Entity
     *
     * @param entity      Entity to get the bounding box scale for
     * @param partialTick current partial tick
     * @return scale of the entities bounding box
     */
    public static float getEntityBbScale(Entity entity, float partialTick) {
        if (init()) {
            try {
                return (float) ScaleUtils_getBoundingBoxHeightScale.invoke(null, entity, partialTick);
            } catch (InvocationTargetException | IllegalAccessException e) {
                VRSettings.LOGGER.error("Vivecraft: couldn't get pehkui entity bb scale:", e);
            }
        }
        return 1F;
    }

    /**
     * initializes all Reflections
     *
     * @return if init was successful
     */
    private static boolean init() {
        if (INITIALIZED) {
            // try to softly fail when something went wrong
            return !INIT_FAILED;
        }
        try {
            Class<?> ScaleUtils = Class.forName("virtuoel.pehkui.util.ScaleUtils");
            ScaleUtils_getEyeHeightScale = ScaleUtils.getDeclaredMethod("getEyeHeightScale", Entity.class, float.class);
            ScaleUtils_getBoundingBoxHeightScale = ScaleUtils.getDeclaredMethod("getBoundingBoxHeightScale",
                Entity.class, float.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            INIT_FAILED = true;
            VRSettings.LOGGER.error("Vivecraft: Failed to initialize Pehkui compat:", e);
        }
        INITIALIZED = true;
        return !INIT_FAILED;
    }
}
