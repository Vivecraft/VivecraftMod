package org.vivecraft.client.utils;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.MinecraftExtension;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.settings.VRSettings;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

public class ClientUtils {

    private static final Minecraft MC = Minecraft.getInstance();
    private static final ClientDataHolderVR DH = ClientDataHolderVR.getInstance();
    private static final Random AV_RANDOMIZER = new Random();

    /**
     * tries to read the give registry key from the Windows registry
     *
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
            VRSettings.LOGGER.error("Vivecraft: error reading registry key: ", e);
        }
        return null;
    }

    /**
     * spawns {@code count} particles at the given {@code position} in a {@code size} sized area
     *
     * @param type     particle type to spawn
     * @param count    how many particles to spawn
     * @param position position to spawn the particles at
     * @param size     size of an area the particles should spawn in
     * @param speed    speed of particles in random directions
     */
    public static void spawnParticles(ParticleOptions type, int count, Vec3 position, Vec3 size, double speed) {
        Minecraft minecraft = Minecraft.getInstance();

        for (int k = 0; k < count; k++) {
            double offX = AV_RANDOMIZER.nextGaussian() * size.x;
            double offY = AV_RANDOMIZER.nextGaussian() * size.y;
            double offZ = AV_RANDOMIZER.nextGaussian() * size.z;
            double dirX = AV_RANDOMIZER.nextGaussian() * speed;
            double dirY = AV_RANDOMIZER.nextGaussian() * speed;
            double dirZ = AV_RANDOMIZER.nextGaussian() * speed;

            try {
                minecraft.level.addParticle(type,
                    position.x + offX, position.y + offY, position.z + offZ,
                    dirX, dirY, dirZ);
            } catch (Throwable throwable) {
                VRSettings.LOGGER.warn("Vivecraft: Could not spawn particle effect {}", type);
                return;
            }
        }
    }

    /**
     * gives the combined sky/block light at the given {@code pos} position, with block light clamped to {@code minLight}
     *
     * @param lightReader level to get the light from
     * @param pos         position to get the light at
     * @param minLight    minimum block light value
     * @return combined sky/block light
     */
    public static int getCombinedLightWithMin(BlockAndTintGetter lightReader, BlockPos pos, int minLight) {
        int light = LevelRenderer.getLightCoords(lightReader, pos);
        int blockLight = (light >> 4) & 0xF;

        if (blockLight < minLight) {
            light &= 0xFFFFFF00;
            light |= minLight << 4;
        }

        return light;
    }

    /**
     * triggers the minecraft takes screenshot method with the given RenderTarget
     *
     * @param fb RenderTarget to capture the screenshot from
     */
    public static void takeScreenshot(RenderTarget fb) {
        Minecraft minecraft = Minecraft.getInstance();
        Screenshot.grab(minecraft.gameDirectory, fb, text ->
            minecraft.execute(() -> ClientUtils.addChatMessage(text)));
    }

    /**
     * @return current partialTick, or pausePartialTick when paused
     */
    public static float getCurrentPartialTick() {
        return ((MinecraftExtension) MC).vivecraft$getPartialTick();
    }

    public static <T extends Enum<T>> T getNextEnum(T current, int offset) {
        T[] values = (T[]) current.getClass().getEnumConstants();
        int index = (current.ordinal() + offset + values.length) % values.length;
        return values[index];
    }

    public static long microTime() {
        return System.nanoTime() / 1000L;
    }

    public static long milliTime() {
        return System.nanoTime() / 1000000L;
    }

    public static URI parseUri(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to parse Uri: " + uri, e);
        }
    }

    public static Component getNameFromSoundEvent(Identifier soundLocation) {
        String key = soundLocation.getPath();
        if (I18n.exists(key)) {
            return Component.translatable(key);
        } else if (I18n.exists("subtitles." + key)) {
            return Component.translatable("subtitles." + key);
        } else if (key.startsWith("music_disc.")) {
            String jukebox = key.replace("music_disc.", "jukebox_song.minecraft.");
            if (I18n.exists(jukebox)) {
                return Component.translatable(jukebox);
            }
        }
        return Component.literal(key);
    }

    /**
     * adds the given message to chat, and triggers the chat sound when VR is active
     *
     * @param message message to add
     */
    public static void addChatMessage(Component message) {
        // can be null, when called very early
        if (MC.gui != null) {
            MC.gui.getChat().addClientSystemMessage(message);
        }
        if (VRState.VR_RUNNING) {
            triggerChatHapticSound();
        }
    }

    /**
     * triggers a chat notification
     */
    public static void triggerChatHapticSound() {
        if (DH.vrSettings.chatNotifications != VRSettings.ChatNotifications.NONE) {
            if (!DH.vrSettings.seated && (DH.vrSettings.chatNotifications == VRSettings.ChatNotifications.HAPTIC ||
                DH.vrSettings.chatNotifications == VRSettings.ChatNotifications.BOTH
            ))
            {
                DH.vr.triggerHapticPulse(ControllerType.LEFT, 0.2F, 1000.0F, 1.0F);
            }

            if (DH.vrSettings.chatNotifications == VRSettings.ChatNotifications.SOUND ||
                DH.vrSettings.chatNotifications == VRSettings.ChatNotifications.BOTH)
            {
                Vec3 controllerPos = DH.vrPlayer.vrdata_world_pre.getController(1).getPosition();
                BuiltInRegistries.SOUND_EVENT.get(Identifier.parse(DH.vrSettings.chatNotificationSound))
                    .ifPresent(soundEvent -> {
                        if (MC.level != null) {
                            MC.level.playLocalSound(controllerPos.x(), controllerPos.y(), controllerPos.z(),
                                soundEvent.value(), SoundSource.NEUTRAL, 0.3F, 0.1F, false);
                        } else {
                            MC.getSoundManager().play(SimpleSoundInstance.forUI(soundEvent.value(), 0.1F, 0.3F));
                        }
                    });
            }
        }
    }
}
