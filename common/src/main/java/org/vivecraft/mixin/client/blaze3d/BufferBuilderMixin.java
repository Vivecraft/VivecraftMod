package org.vivecraft.mixin.client.blaze3d;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.vivecraft.client.extensions.BufferBuilderExtension;

import java.nio.ByteBuffer;

@Mixin(BufferBuilder.class)
public class BufferBuilderMixin implements BufferBuilderExtension {
    @Final
    @Shadow
    private ByteBufferBuilder buffer;

    @Override
    public void vivecraft$freeBuffer() {
        buffer.close();
    }

    @Override
    public int vivecraft$getBufferSize() {
        return ((ByteBufferBuilderAccessor) buffer).getCapacity();
    }
}
