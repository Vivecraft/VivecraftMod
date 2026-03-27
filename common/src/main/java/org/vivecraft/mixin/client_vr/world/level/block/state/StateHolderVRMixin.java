package org.vivecraft.mixin.client_vr.world.level.block.state;

// TODO 26.1 check if needed
/*
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
}*/
