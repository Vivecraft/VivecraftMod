package org.vivecraft.client_vr.provider.nullvr;


import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.HapticScheduler;

import java.util.ArrayList;
import java.util.List;

public class NullVRHapticScheduler extends HapticScheduler {

    private final List<Haptic> haptics = new ArrayList<>();

    @Override
    public void queueHapticPulse(
        ControllerType controller, float durationSeconds, float frequency, float amplitude, float delaySeconds)
    {
        if (ClientDataHolderVR.getInstance().vrSettings.nullvrHaptics) {
            this.haptics.add(new Haptic(controller.ordinal(), System.currentTimeMillis() + (long) (delaySeconds * 1000),
                (long) (durationSeconds * 1000), frequency, amplitude));
        }
    }

    public void tick() {
        for (Haptic haptic : this.haptics) {
            if (System.currentTimeMillis() > haptic.startTime) {
                Vec3 pos = ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getController(haptic.controller)
                    .getPosition();
                float freq = Math.min(haptic.frequency / 1000F, 1.0F);
                SoundInstance sound = new SimpleSoundInstance(SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.AMBIENT,
                    freq, haptic.amplitude, SoundInstance.createUnseededRandom(), pos.x, pos.y, pos.z);
                Minecraft.getInstance().getSoundManager().play(sound);
                if (Minecraft.getInstance().level != null) {
                    Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.NOTE, pos.x, pos.y, pos.z,
                        haptic.frequency, 0, 0);
                }
            }
        }
        this.haptics.removeIf(haptic -> System.currentTimeMillis() > haptic.startTime + haptic.durationMillis);
    }

    public record Haptic(int controller, long startTime, long durationMillis, float frequency, float amplitude) {}
}
