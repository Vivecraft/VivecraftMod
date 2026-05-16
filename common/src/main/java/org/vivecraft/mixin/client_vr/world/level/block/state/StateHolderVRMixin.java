package org.vivecraft.mixin.client_vr.world.level.block.state;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.vivecraft.client_vr.extensions.StateHolderExtension;

import java.util.Map;

// low priority, because we want to apply before FerriteCore
@Mixin(value = StateHolder.class, priority = 500)
public class StateHolderVRMixin implements StateHolderExtension {

    @Final
    @Mutable
    @Shadow
    private Reference2ObjectArrayMap<Property<?>, Comparable<?>> values;

    @Override
    public void vivecraft$setValues(Map<Property<?>, Comparable<?>> values) {
        // this cast is fine, because the supplied map always comes from a StateHolder
        // this cast is also fine with FerriteCore, because it changes all references of Reference2ObjectArrayMap to the type they use
        this.values = (Reference2ObjectArrayMap<Property<?>, Comparable<?>>) values;
    }
}
