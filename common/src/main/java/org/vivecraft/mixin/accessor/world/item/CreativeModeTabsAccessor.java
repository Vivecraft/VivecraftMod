package org.vivecraft.mixin.accessor.world.item;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreativeModeTabs.class)
public interface CreativeModeTabsAccessor {

    @NotNull
    @Accessor
    static ResourceKey<CreativeModeTab> getFOOD_AND_DRINKS(){
        return null;
    }

    @NotNull
    @Accessor
    static ResourceKey<CreativeModeTab> getTOOLS_AND_UTILITIES(){
        return null;
    }
}
