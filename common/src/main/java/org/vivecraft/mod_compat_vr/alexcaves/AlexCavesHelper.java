package org.vivecraft.mod_compat_vr.alexcaves;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.item.ItemStack;
import org.vivecraft.Xloader;
import org.vivecraft.client_vr.settings.VRSettings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AlexCavesHelper {
    private static boolean INITIALIZED = false;
    private static boolean INIT_FAILED = false;

    private static Class<?> CaveMapItem;
    private static Method CaveMapRenderHelper_renderCaveMap;

    public static boolean isLoaded() {
        return Xloader.INSTANCE.isModLoaded("alexscaves");
    }

    public static boolean isCaveMap(ItemStack stack) {
        return init() && CaveMapItem.isInstance(stack.getItem());
    }

    public static void renderCaveMap(
        PoseStack poseStack, SubmitNodeCollector collector, int combinedLight, ItemStack stack)
    {
        if (init()) {
            try {
                CaveMapRenderHelper_renderCaveMap.invoke(null, poseStack, collector, combinedLight, stack, true);
            } catch (InvocationTargetException | IllegalAccessException e) {
                VRSettings.LOGGER.error("Failed to render alexcaves cave map", e);
            }
        }
    }

    private static boolean init() {
        if (INITIALIZED) {
            return !INIT_FAILED;
        }
        try {
            CaveMapItem = Class.forName("com.github.alexmodguy.alexscaves.server.item.CaveMapItem");
            Class<?> CaveMapRenderHelper = Class.forName(
                "com.github.alexmodguy.alexscaves.client.render.misc.CaveMapRenderHelper");
            CaveMapRenderHelper_renderCaveMap = CaveMapRenderHelper.getDeclaredMethod("renderCaveMap", PoseStack.class,
                SubmitNodeCollector.class, int.class, ItemStack.class, boolean.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            INIT_FAILED = true;
            VRSettings.LOGGER.error("Vivecraft: Failed to initialize AlexCaves compat", e);
        }
        INITIALIZED = true;
        return !INIT_FAILED;
    }
}
