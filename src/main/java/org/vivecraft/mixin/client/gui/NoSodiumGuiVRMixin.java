package org.vivecraft.mixin.client.gui;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Restriction(conflict = @Condition("sodium"))
@Mixin(Gui.class)
public class NoSodiumGuiVRMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;useFancyGraphics()Z"), method = "render")
    public boolean noVignette() {
        return false;
    }
}
