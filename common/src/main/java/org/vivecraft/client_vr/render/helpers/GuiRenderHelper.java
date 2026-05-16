package org.vivecraft.client_vr.render.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.util.profiling.Profiler;
import org.vivecraft.mixin.client_vr.renderer.GameRendererAccessor;

public class GuiRenderHelper {


    public static GuiGraphicsExtractor getGuiGraphics() {
        GuiRenderState guiRenderState = ((GameRendererAccessor) Minecraft.getInstance().gameRenderer).getGameRenderState().guiRenderState;
        guiRenderState.reset();
        return new GuiGraphicsExtractor(Minecraft.getInstance(), guiRenderState, 0, 0);
    }

    public static void renderScreen(Screen screen) {
        GuiGraphicsExtractor graphics = getGuiGraphics();
        Profiler.get().push("extract");
        screen.extractRenderState(graphics, 0, 0, Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks());
        Profiler.get().pop();
        finish();
    }

    public static void finish() {
        Profiler.get().push("render");
        GuiRenderer guiRenderer = ((GameRendererAccessor) Minecraft.getInstance().gameRenderer).getGuiRenderer();
        guiRenderer.render(((GameRendererAccessor) Minecraft.getInstance().gameRenderer).getFogRenderer()
            .getBuffer(FogRenderer.FogMode.NONE));
        guiRenderer.endFrame();
        Profiler.get().pop();
    }
}
