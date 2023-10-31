package org.vivecraft.mixin.accessor.client;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {
    // gui handling
    @Invoker
    void callOnPress(long l, int i, int j, int k);
    @Invoker
    void callOnMove(long l, double d, double e);
    @Invoker
    void callOnScroll(long l, double d, double e);
}
