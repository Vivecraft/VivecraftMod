package org.vivecraft.client.utils;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client_vr.settings.VRSettings;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Random;

public class Utils {

    private static final Random avRandomizer = new Random();

    /**
     * tries to read the give registry key from the Windows registry
     * @param key registry key to look up
     * @return value of the registry key, or {@code null} on error
     */
    public static String readWinRegistry(String key) {
        try {
            Process process = Runtime.getRuntime().exec(
                "reg query \"" + key.substring(0, key.lastIndexOf('\\')) + "\" /v \"" +
                    key.substring(key.lastIndexOf('\\') + 1) + "\"");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    String[] split = line.split("REG_SZ|REG_DWORD");
                    if (split.length > 1) {
                        return split[1].trim();
                    }
                }
            }
        } catch (Exception e) {
            VRSettings.logger.error("Vivecraft: error reading registry key: ", e);
        }
        return null;
    }

    /**
     * spawns {@code count} particles at the given {@code position} in a {@code size} sized area
     * @param type particle type to spawn
     * @param count how many particles to spawn
     * @param position position to spawn the particles at
     * @param size size of an area the particles should spawn in
     * @param speed speed of particles in random directions
     */
    public static void spawnParticles(ParticleOptions type, int count, Vec3 position, Vec3 size, double speed) {
        Minecraft minecraft = Minecraft.getInstance();

        for (int k = 0; k < count; k++) {
            double offX = avRandomizer.nextGaussian() * size.x;
            double offY = avRandomizer.nextGaussian() * size.y;
            double offZ = avRandomizer.nextGaussian() * size.z;
            double dirX = avRandomizer.nextGaussian() * speed;
            double dirY = avRandomizer.nextGaussian() * speed;
            double dirZ = avRandomizer.nextGaussian() * speed;

            try {
                minecraft.level.addParticle(type,
                    position.x + offX, position.y + offY, position.z + offZ,
                    dirX, dirY, dirZ);
            } catch (Throwable throwable) {
                VRSettings.logger.warn("Vivecraft: Could not spawn particle effect {}", type);
                return;
            }
        }
    }

    /**
     * gives the combined sky/block light at the given {@code pos} position, with block light clamped to {@code minLight}
     * @param lightReader level to get the light from
     * @param pos position to get the light at
     * @param minLight minimum block light value
     * @return combined sky/block light
     */
    public static int getCombinedLightWithMin(BlockAndTintGetter lightReader, BlockPos pos, int minLight) {
        int light = LevelRenderer.getLightColor(lightReader, pos);
        int blockLight = (light >> 4) & 0xF;

        if (blockLight < minLight) {
            light &= 0xFFFFFF00;
            light |= minLight << 4;
        }

        return light;
    }

    /**
     * triggers the minecraft takes screenshot method with the given RenderTarget
     * @param fb RenderTarget to capture the screenshot from
     */
    public static void takeScreenshot(RenderTarget fb) {
        Minecraft minecraft = Minecraft.getInstance();
        Screenshot.grab(minecraft.gameDirectory, fb, text ->
            minecraft.execute(() -> minecraft.gui.getChat().addMessage(text)));
    }

    public static long microTime() {
        return System.nanoTime() / 1000L;
    }

    public static long milliTime() {
        return System.nanoTime() / 1000000L;
    }
}
