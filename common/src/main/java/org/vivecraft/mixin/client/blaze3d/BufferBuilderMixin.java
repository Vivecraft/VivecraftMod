package org.vivecraft.mixin.client.blaze3d;

import com.mojang.blaze3d.vertex.BufferBuilder;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.vivecraft.client.extensions.BufferBuilderExtension;

import java.nio.ByteBuffer;

@Mixin(BufferBuilder.class)
public class BufferBuilderMixin implements BufferBuilderExtension {
    @Shadow
    private ByteBuffer buffer;

    @Override
    public void vivecraft$freeBuffer() {
        MemoryTrackerAccessor.getAllocator().free(MemoryUtil.memAddress0(buffer));
    }

    @Override
    public int vivecraft$getBufferSize() {
        return buffer.capacity();
    }
}
