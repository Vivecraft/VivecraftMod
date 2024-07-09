package org.vivecraft.common.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Interface for objects that can be written to a {@link FriendlyByteBuf}
 */
public interface BufferSerializable {
    /**
     * write this object to {@code buffer}
     * @param buffer buffer to write to
     */
    void serialize(FriendlyByteBuf buffer);
}
