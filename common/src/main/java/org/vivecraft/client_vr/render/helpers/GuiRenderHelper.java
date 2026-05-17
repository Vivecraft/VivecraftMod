package org.vivecraft.client_vr.render.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.util.profiling.Profiler;
import org.vivecraft.mixin.client_vr.renderer.GameRendererAccessor;

public class GuiRenderHelper {


    public static GuiGraphics getGuiGraphics() {
        GuiRenderState guiRenderState = ((GameRendererAccessor) Minecraft.getInstance().gameRenderer).getGuiRenderState();
        guiRenderState.reset();
        return new GuiGraphics(Minecraft.getInstance(), guiRenderState);
    }

    public static void renderScreen(Screen screen) {
        GuiGraphics guiGraphics = getGuiGraphics();
        Profiler.get().push("extract");
        screen.render(guiGraphics, 0, 0, Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks());
        Profiler.get().pop();
        finish();
    }

    public static void finish() {
        Profiler.get().push("render");
        GuiRenderer guiRenderer = ((GameRendererAccessor) Minecraft.getInstance().gameRenderer).getGuiRenderer();
        guiRenderer.render(((GameRendererAccessor) Minecraft.getInstance().gameRenderer).getFogRenderer()
            .getBuffer(FogRenderer.FogMode.NONE));
        guiRenderer.incrementFrameNumber();
        Profiler.get().pop();
    }
}
