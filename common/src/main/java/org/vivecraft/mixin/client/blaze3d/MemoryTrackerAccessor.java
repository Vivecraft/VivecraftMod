package org.vivecraft.mixin.client.blaze3d;

import com.mojang.blaze3d.platform.MemoryTracker;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * accessor to get the used ALLOCATOR, to free buffers
 */
@Mixin(MemoryTracker.class)
public interface MemoryTrackerAccessor {
    @Accessor("ALLOCATOR")
    static MemoryUtil.MemoryAllocator getAllocator() {
        throw new AssertionError();
    }
}
