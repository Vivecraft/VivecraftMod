package org.vivecraft.mixin.client.blaze3d;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ByteBufferBuilder.class)
public interface ByteBufferBuilderAccessor {
    @Accessor("capacity")
    int getCapacity();
}
