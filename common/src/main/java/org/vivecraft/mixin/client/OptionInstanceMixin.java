package org.vivecraft.mixin.client;

import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.vivecraft.client_vr.extensions.OptionInstanceExtension;

@Mixin(OptionInstance.class)
public class OptionInstanceMixin<T> implements OptionInstanceExtension<T> {

    @Shadow
    private T value;

    @Override
    public void vivecraft$setWithoutUpdate(T newValue) {
        this.value = newValue;
    }
}
