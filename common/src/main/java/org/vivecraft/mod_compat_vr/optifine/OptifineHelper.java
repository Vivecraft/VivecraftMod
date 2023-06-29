package org.vivecraft.mod_compat_vr.optifine;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OptifineHelper {

    private static boolean checkedForOptifine = false;
    private static boolean optifineLoaded = false;

    private static Class<?> optifineConfig;
    private static Method optifineConfigIsShadersMethod;

    private static Class<?> smartAnimations;

    private static Method smartAnimationsSpriteRenderedMethod;

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
            e.printStackTrace();
            return false;
        }
    }

    public static void markTextureAsActive(TextureAtlasSprite sprite) {
        try {
            smartAnimationsSpriteRenderedMethod.invoke(smartAnimations, sprite);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static void init() {
        try {
            optifineConfig = Class.forName("net.optifine.Config");
            optifineConfigIsShadersMethod = optifineConfig.getMethod("isShaders");

            smartAnimations = Class.forName("net.optifine.SmartAnimations");
            smartAnimationsSpriteRenderedMethod = smartAnimations.getMethod("spriteRendered", TextureAtlasSprite.class);

        } catch (ClassNotFoundException e) {
            LogUtils.getLogger().error("Optifine detected, but couldn't load class: {}", e.getMessage());
            optifineLoaded = false;
        } catch (NoSuchMethodException e) {
            LogUtils.getLogger().error("Optifine detected, but couldn't load Method: {}", e.getMessage());
            optifineLoaded = false;
        }
    }

}
