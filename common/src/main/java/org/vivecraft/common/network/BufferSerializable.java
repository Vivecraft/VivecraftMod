package org.vivecraft.common.network;

import net.minecraft.network.FriendlyByteBuf;

public interface BufferSerializable {
    void serialize(FriendlyByteBuf buf);
}
