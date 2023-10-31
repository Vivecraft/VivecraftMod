package org.vivecraft.mixin.accessor.world.level.block.state;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.StateHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StateHolder.class)
public interface StateHolderAccessor {
    @SuppressWarnings("rawtypes")
    @Accessor
    MapCodec getPropertiesCodec();
}
