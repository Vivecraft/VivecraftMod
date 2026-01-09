package org.vivecraft.client_vr.extensions;

public interface OptionInstanceExtension<T> {
    /**
     * set the OptionInstance to a new value, without calling {@link net.minecraft.client.OptionInstance#onValueUpdate}
     */
    void vivecraft$setWithoutUpdate(T newValue);
}
