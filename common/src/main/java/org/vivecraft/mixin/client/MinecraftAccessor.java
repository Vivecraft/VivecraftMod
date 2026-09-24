package org.vivecraft.mixin.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * This accessor is used instead of an access widener, because of <a href="https://github.com/neoforged/FancyModLoader/issues/453">this</a> NeoForge bug, so this can be reverted to an access widener once that is fixed.
 */
@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Invoker
    void invokeStartUseItem();
}
