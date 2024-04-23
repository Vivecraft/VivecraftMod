package org.vivecraft.client_vr.provider;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class HapticScheduler {
    protected ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public abstract void queueHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude, float delaySeconds);
}
