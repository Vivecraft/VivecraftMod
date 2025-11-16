package org.vivecraft.mixin.client_vr.renderer;

import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Accessor
    FogRenderer getFogRenderer();

    @Accessor
    ScreenEffectRenderer getScreenEffectRenderer();

    @Accessor
    GuiRenderState getGuiRenderState();

    @Accessor
    GuiRenderer getGuiRenderer();
}
