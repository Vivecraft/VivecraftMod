package org.vivecraft.client_vr.extensions;

import net.minecraft.world.level.block.state.properties.Property;

import java.util.Map;

public interface StateHolderExtension {
    void vivecraft$setValues(Map<Property<?>, Comparable<?>> values);
}
