package org.vivecraft.mixin.accessor.client.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Gui.class)
public interface GuiAccessor {
    @NotNull
    @Accessor
    static ResourceLocation getGUI_ICONS_LOCATION() {
        return null;
    }
}
