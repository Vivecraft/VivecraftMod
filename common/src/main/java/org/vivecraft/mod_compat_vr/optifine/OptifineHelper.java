package org.vivecraft.mod_compat_vr.optifine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client_vr.settings.VRSettings;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OptifineHelper {

    private static boolean checkedForOptifine = false;
    private static boolean optifineLoaded = false;

    private static Class<?> optifineConfig;
    private static Method optifineConfigIsShadersMethod;
    private static Method optifineConfigIsRenderRegionsMethod;
    private static Method optifineConfigIsSkyEnabledMethod;
    private static Method optifineConfigIsSunMoonEnabledMethod;
    private static Method optifineConfigIsStarsEnabledMethod;
    private static Method optifineConfigIsCustomColorsMethod;
    private static Method optifineConfigIsAntialiasingMethod;
    private static Method optifineConfigIsAntialiasingConfiguredMethod;

    private static Class<?> smartAnimations;
    private static Method smartAnimationsSpriteRenderedMethod;

    private static Class<?> customColors;
    private static Method customColorsGetSkyColorMethod;
    private static Method customColorsGetSkyColoEndMethod;
    private static Method customColorsGetUnderwaterColorMethod;
    private static Method customColorsGetUnderlavaColorMethod;
    private static Method customColorsGetFogColorMethod;
    private static Method customColorsGetFogColorEndMethod;
    private static Method customColorsGetFogColorNetherMethod;

    private static Class<?> shadersRender;
    private static Method shadersRenderBeginOutlineMethod;
    private static Method shadersRenderEndOutlineMethod;

    private static Field optionsOfRenderRegions;
    private static Field optionsOfCloudHeight;
    private static Field vertexRenderPositions;

    public static boolean isOptifineLoaded() {
        if (!checkedForOptifine) {
            checkedForOptifine = true;
            // check for optifine with a class search
            try {
                Class.forName("net.optifine.Config");
                VRSettings.logger.info("Vivecraft: Optifine detected");
                optifineLoaded = true;
            } catch (ClassNotFoundException ignore) {
                VRSettings.logger.info("Vivecraft: Optifine not detected");
                optifineLoaded = false;
            }
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

    public static void beginOutlineShader() {
        try {
            shadersRenderBeginOutlineMethod.invoke(shadersRender);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void endOutlineShader() {
        try {
            shadersRenderEndOutlineMethod.invoke(shadersRender);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static boolean isSunMoonEnabled() {
        try {
            return (boolean)optifineConfigIsSunMoonEnabledMethod.invoke(optifineConfig);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isSkyEnabled() {
        try {
            return (boolean)optifineConfigIsSkyEnabledMethod.invoke(optifineConfig);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean isStarsEnabled() {
        try {
            return (boolean)optifineConfigIsStarsEnabledMethod.invoke(optifineConfig);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isCustomColors() {
        try {
            return (boolean)optifineConfigIsCustomColorsMethod.invoke(optifineConfig);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isRenderRegions() {
        try {
            return (boolean)optifineConfigIsRenderRegionsMethod.invoke(optifineConfig);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isAntialiasing() {
        try {
            return (boolean)optifineConfigIsAntialiasingMethod.invoke(optifineConfig)
                || (boolean)optifineConfigIsAntialiasingConfiguredMethod.invoke(optifineConfig);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void setRenderRegions(boolean active) {
        try {
            optionsOfRenderRegions.set(Minecraft.getInstance().options, active);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Vec3 getCustomSkyColor(Vec3 skyColor, BlockAndTintGetter blockAccess, double x, double y, double z) {
        try {
            return (Vec3)customColorsGetSkyColorMethod.invoke(customColors, skyColor, blockAccess, x, y, z);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return skyColor;
        }
    }

    public static Vec3 getCustomSkyColorEnd(Vec3 skyColor) {
        try {
            return (Vec3)customColorsGetSkyColoEndMethod.invoke(customColors, skyColor);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return skyColor;
        }
    }


    public static Vec3 getCustomUnderwaterColor(BlockAndTintGetter blockAccess, double x, double y, double z) {
        try {
            return (Vec3)customColorsGetUnderwaterColorMethod.invoke(customColors, blockAccess, x, y, z);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Vec3 getCustomUnderlavaColor(BlockAndTintGetter blockAccess, double x, double y, double z) {
        try {
            return (Vec3)customColorsGetUnderlavaColorMethod.invoke(customColors, blockAccess, x, y, z);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Vec3 getCustomFogColor(Vec3 fogColor, BlockAndTintGetter blockAccess, double x, double y, double z) {
        try {
            return (Vec3)customColorsGetFogColorMethod.invoke(customColors, fogColor, blockAccess, x, y, z);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return fogColor;
        }
    }

    public static Vec3 getCustomFogColorEnd(Vec3 fogColor) {
        try {
            return (Vec3)customColorsGetFogColorEndMethod.invoke(customColors, fogColor);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return fogColor;
        }
    }

    public static Vec3 getCustomFogColorNether(Vec3 fogColor) {
        try {
            return (Vec3)customColorsGetFogColorNetherMethod.invoke(customColors, fogColor);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return fogColor;
        }
    }

    public static double getCloudHeight() {
        try {
            return (double)optionsOfCloudHeight.get(Minecraft.getInstance().options);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void markTextureAsActive(TextureAtlasSprite sprite) {
        try {
            smartAnimationsSpriteRenderedMethod.invoke(smartAnimations, sprite);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void copyRenderPositions(ModelPart.Vertex source, ModelPart.Vertex dest) {
        if (vertexRenderPositions != null) {
            try {
                vertexRenderPositions.set(dest, vertexRenderPositions.get(source));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static void init() {
        try {
            optifineConfig = Class.forName("net.optifine.Config");
            optifineConfigIsShadersMethod = optifineConfig.getMethod("isShaders");
            optifineConfigIsRenderRegionsMethod = optifineConfig.getMethod("isRenderRegions");
            optifineConfigIsSkyEnabledMethod = optifineConfig.getMethod("isSkyEnabled");
            optifineConfigIsSunMoonEnabledMethod = optifineConfig.getMethod("isSunMoonEnabled");
            optifineConfigIsStarsEnabledMethod = optifineConfig.getMethod("isStarsEnabled");
            optifineConfigIsCustomColorsMethod = optifineConfig.getMethod("isCustomColors");
            optifineConfigIsAntialiasingMethod = optifineConfig.getMethod("isAntialiasing");
            optifineConfigIsAntialiasingConfiguredMethod = optifineConfig.getMethod("isAntialiasingConfigured");

            smartAnimations = Class.forName("net.optifine.SmartAnimations");
            smartAnimationsSpriteRenderedMethod = smartAnimations.getMethod("spriteRendered", TextureAtlasSprite.class);

            optionsOfRenderRegions = Options.class.getField("ofRenderRegions");
            optionsOfCloudHeight = Options.class.getField("ofCloudsHeight");

            customColors = Class.forName("net.optifine.CustomColors");
            customColorsGetSkyColorMethod = customColors.getMethod("getSkyColor", Vec3.class, BlockAndTintGetter.class, double.class, double.class, double.class);

            customColorsGetUnderwaterColorMethod = customColors.getMethod("getUnderwaterColor", BlockAndTintGetter.class, double.class, double.class, double.class);
            customColorsGetUnderlavaColorMethod = customColors.getMethod("getUnderlavaColor", BlockAndTintGetter.class, double.class, double.class, double.class);

            shadersRender = Class.forName("net.optifine.shaders.ShadersRender");
            shadersRenderBeginOutlineMethod = shadersRender.getMethod("beginOutline");
            shadersRenderEndOutlineMethod = shadersRender.getMethod("endOutline");

            // private methods
            customColorsGetSkyColoEndMethod = customColors.getDeclaredMethod("getSkyColorEnd", Vec3.class);
            customColorsGetSkyColoEndMethod.setAccessible(true);
            customColorsGetFogColorMethod = customColors.getDeclaredMethod("getFogColor", Vec3.class, BlockAndTintGetter.class, double.class, double.class, double.class);
            customColorsGetFogColorMethod.setAccessible(true);
            customColorsGetFogColorEndMethod = customColors.getDeclaredMethod("getFogColorEnd", Vec3.class);
            customColorsGetFogColorEndMethod.setAccessible(true);
            customColorsGetFogColorNetherMethod = customColors.getDeclaredMethod("getFogColorNether", Vec3.class);
            customColorsGetFogColorNetherMethod.setAccessible(true);

            try {
                vertexRenderPositions = ModelPart.Vertex.class.getField("renderPositions");
            } catch (NoSuchFieldException e) {
                // this version doesn't have the entity render improvements
                vertexRenderPositions = null;
            }

        } catch (ClassNotFoundException e) {
            VRSettings.logger.error("Optifine detected, but couldn't load class: {}", e.getMessage());
            optifineLoaded = false;
        } catch (NoSuchMethodException e) {
            VRSettings.logger.error("Optifine detected, but couldn't load Method: {}", e.getMessage());
            optifineLoaded = false;
        } catch (NoSuchFieldException e) {
            VRSettings.logger.error("Optifine detected, but couldn't load Field: {}", e.getMessage());
        }
    }

}
