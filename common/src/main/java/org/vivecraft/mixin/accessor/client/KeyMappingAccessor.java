package org.vivecraft.mixin.accessor.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(KeyMapping.class)
public interface KeyMappingAccessor {
    // to check key inputs
    @Accessor
    void setClickCount(int clickCount);
    @Accessor
    int getClickCount();
    @Invoker
    void callRelease();
    @Accessor
    InputConstants.Key getKey();
}
