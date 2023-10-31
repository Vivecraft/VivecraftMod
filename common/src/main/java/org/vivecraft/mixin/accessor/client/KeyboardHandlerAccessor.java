package org.vivecraft.mixin.accessor.client;

import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(KeyboardHandler.class)
public interface KeyboardHandlerAccessor {
    // gui handling
    @Invoker
    void callCharTyped(long l, int i, int j);
}
