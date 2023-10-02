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
        CATEGORY_SORT_ORDER.put("vivecraft.key.category.gui", 8);
        CATEGORY_SORT_ORDER.put("vivecraft.key.category.climbey", 9);
        CATEGORY_SORT_ORDER.put("vivecraft.key.category.keyboard", 10);
    }
}
