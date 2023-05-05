package org.vivecraft.client_vr.provider;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class HapticScheduler
{
    protected ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public abstract void queueHapticPulse(ControllerType var1, float var2, float var3, float var4, float var5);
}
