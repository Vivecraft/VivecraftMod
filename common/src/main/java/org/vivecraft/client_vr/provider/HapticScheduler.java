package org.vivecraft.client_vr.provider;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class HapticScheduler {
    protected ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    /**
     * schedules a haptic pulse on the give controller at the given delay
     *
     * @param controller      controller to trigger on
     * @param durationSeconds duration in seconds
     * @param frequency       frequency in Hz
     * @param amplitude       strength 0.0 - 1.0
     * @param delaySeconds    delay for when to trigger in seconds
     */
    public abstract void queueHapticPulse(
        ControllerType controller, float durationSeconds, float frequency, float amplitude, float delaySeconds);
}
