package org.vivecraft.mod_compat_vr.optifine;

import com.mojang.logging.LogUtils;
import net.minecraft.client.resources.language.I18n;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OptifineHelper {

    private static boolean checkedForOptifine = false;
    private static boolean optifineLoaded = false;

    private static Class<?> optifineConfig;
    private static Method optifineConfigIsShadersMethod;

    public static boolean isOptifineLoaded() {
        if (!checkedForOptifine) {
            checkedForOptifine = true;
            optifineLoaded = !I18n.get("of.key.zoom").equals("of.key.zoom");
            if (optifineLoaded) {
                init();
            }
        }
        return optifineLoaded;
    }

    public static boolean isShaderActive() {
        try {
            return (boolean)optifineConfigIsShadersMethod.invoke(optifineConfig);
        } catch (InvocationTargetException | IllegalAccessException e) {
            return false;
        }
    }

    private static void init() {
        try {
            optifineConfig = Class.forName("net.optifine.Config");
            optifineConfigIsShadersMethod = optifineConfig.getMethod("isShaders");


        } catch (ClassNotFoundException e) {
            LogUtils.getLogger().error("Optifine detected, but couldn't load class: {}", e.getMessage());
            optifineLoaded = false;
        } catch (NoSuchMethodException e) {
            LogUtils.getLogger().error("Optifine detected, but couldn't load Method: {}", e.getMessage());
            optifineLoaded = false;
        }
    }

}
