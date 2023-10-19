package org.vivecraft.mixin.accessor.network;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Connection.class)
public interface ConnectionAccessor {
    //for aim fix
    @Accessor
    Channel getChannel();
}
