package org.vivecraft.mixin.client_vr;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(KeyMapping.class)
public abstract class KeyMappingVRMixin {

    @Final
    @Shadow
    private static Map<String, Integer> CATEGORY_SORT_ORDER;

    // inject custom controls categories
    static {
        CATEGORY_SORT_ORDER.put("vivecraft.key.category.gui", CATEGORY_SORT_ORDER.values().stream().max(Integer::compareTo).orElse(0) + 1);
        CATEGORY_SORT_ORDER.put("vivecraft.key.category.climbey", CATEGORY_SORT_ORDER.values().stream().max(Integer::compareTo).orElse(0) + 1);
        CATEGORY_SORT_ORDER.put("vivecraft.key.category.keyboard", CATEGORY_SORT_ORDER.values().stream().max(Integer::compareTo).orElse(0) + 1);
        CATEGORY_SORT_ORDER.put("vivecraft.key.category.skeletal_input", CATEGORY_SORT_ORDER.values().stream().max(Integer::compareTo).orElse(0) + 1);
    }
}
