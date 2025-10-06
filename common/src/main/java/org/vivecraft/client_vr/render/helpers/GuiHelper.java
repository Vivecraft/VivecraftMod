package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.Objects;

public class GuiHelper {
    public static void renderOutline(PoseStack poseStack, int x, int y, int width, int height, int color) {
        GuiComponent.fill(poseStack, x, y, x + width, y + 1, color);
        GuiComponent.fill(poseStack, x, y + height - 1, x + width, y + height, color);
        GuiComponent.fill(poseStack, x, y + 1, x + 1, y + height - 1, color);
        GuiComponent.fill(poseStack, x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    public static void renderScrollingString(
        PoseStack poseStack, Font font, Component text, int minX, int minY, int maxX, int maxY, int color)
    {
        int textWidth = font.width(text);
        Objects.requireNonNull(font);
        int yCenter = (minY + maxY - font.lineHeight) / 2 + 1;
        int width = maxX - minX;
        if (textWidth > width) {
            int difference = textWidth - width;
            double seconds = Util.getMillis() / 1000.0;
            double minDif = Math.max(difference * 0.5F, 3.0F);
            double offsetPosition = Math.sin(Math.PI * 0.5 * Math.cos(Math.PI * 2.0 * seconds / minDif)) / 2.0 + 0.5;
            double offset = Mth.lerp(offsetPosition, 0.0F, difference);


            enableScissor(minX, minY, maxX, maxY);
            GuiComponent.drawString(poseStack, font, text, minX - (int) offset, yCenter, color);
            RenderSystem.disableScissor();
        } else {
            GuiComponent.drawCenteredString(poseStack, font, text, (minX + maxX) / 2, yCenter, color);
        }
    }

    private static void enableScissor(int minX, int minY, int maxX, int maxY) {
        Window window = Minecraft.getInstance().getWindow();
        int windowHeight = window.getHeight();
        double scale = window.getGuiScale();
        double x = minX * scale;
        double y = windowHeight - maxY * scale;
        double width = (maxX - minX) * scale;
        double height = (maxY - minY) * scale;
        RenderSystem.enableScissor((int)x, (int)y, Math.max(0, (int)width), Math.max(0, (int)height));
    }

    public static void renderOnTooltip(Button button, PoseStack poseStack, int x, int y, Component component) {
        if (x >= button.x && x < button.x + button.getWidth() &&
            y >= button.y && y < button.y + button.getHeight())
        {
            Minecraft.getInstance().screen.renderTooltip(poseStack, component, x, y);
        } else {
            int tooltipWidth = Minecraft.getInstance().font.width(component);
            int tooltipX =
                button.x + tooltipWidth > Minecraft.getInstance().screen.width ? button.x + button.getWidth() + 14 :
                    button.x - button.getWidth();
            Minecraft.getInstance().screen.renderTooltip(poseStack, component, tooltipX,
                button.y + button.getHeight() + 15);
        }
    }
}
