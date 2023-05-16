package org.vivecraft.mod_compat_vr.sodium;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import me.jellysquid.mods.sodium.client.SodiumClientMod;
import org.lwjgl.opengl.GL32C;
import org.vivecraft.client.Xplat;

public class SodiumHelper {
    static final LongArrayFIFOQueue fences = new LongArrayFIFOQueue();

    public static void preRenderMinecraft() {
        while (fences.size() > SodiumClientMod.options().advanced.cpuRenderAheadLimit) {
            var fence = fences.dequeueLong();
            GL32C.glClientWaitSync(fence, GL32C.GL_SYNC_FLUSH_COMMANDS_BIT, Long.MAX_VALUE);
            GL32C.glDeleteSync(fence);
        }
    }

    public static void postRenderMinecraft() {
        var fence = GL32C.glFenceSync(GL32C.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);

        if (fence == 0) {
            throw new RuntimeException("Failed to create fence object");
        }

        fences.enqueue(fence);
    }

    public static boolean isLoaded() {
        return Xplat.isModLoaded("sodium") || Xplat.isModLoaded("rubidium");
    }

// NotFixed
//    public static void vignette(boolean b) {
//        SodiumClientMod.options().quality.enableVignette = b;
//    }
}
