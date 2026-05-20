package org.vivecraft.client.gui.framework;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

import java.util.List;

public class TooltipRenderer {

    private static final int TOOLTIP_WIDTH = 308;
    private static final int TOOLTIP_HALF_WIDTH = TOOLTIP_WIDTH / 2;

    /**
     * renders a big centered tooltip below a widget, or above if there isn't enough space
     *
     * @param graphics      GuiGraphicsExtractor used to render stuff
     * @param tooltip       text to render
     * @param tooltipCenter center of where the tooltip should be
     * @param widgetY       upper edge of the widget
     * @param widgetHeight  height of the widget to get the lower edge
     */
    public static void renderTooltip(
        GuiGraphicsExtractor graphics, String tooltip, int tooltipCenter, int widgetY, int widgetHeight)
    {
        if (!tooltip.isEmpty()) {
            // add format reset at line ends
            tooltip = tooltip.replace("\n", "§r\n");

            Minecraft mc = Minecraft.getInstance();

            // make last line roughly 308 wide
            List<FormattedText> formattedText = mc.font.getSplitter().splitLines(tooltip, TOOLTIP_WIDTH, Style.EMPTY);
            tooltip += " ".repeat((TOOLTIP_WIDTH -
                (formattedText.isEmpty() ? 0 : mc.font.width(formattedText.get(formattedText.size() - 1)))
            ) / mc.font.width(" "));

            // if tooltip is not too low, draw below button, else above
            if (widgetY + widgetHeight + formattedText.size() * (mc.font.lineHeight + 1) + 14 < mc.screen.height) {
                graphics.tooltip(mc.font, mc.font.split(Component.literal(tooltip), TOOLTIP_WIDTH).stream()
                        .map(ClientTooltipComponent::create).toList(), tooltipCenter - TOOLTIP_HALF_WIDTH - 12,
                    widgetY + widgetHeight + 14, DefaultTooltipPositioner.INSTANCE, null);
            } else {
                graphics.tooltip(mc.font, mc.font.split(Component.literal(tooltip), TOOLTIP_WIDTH).stream()
                        .map(ClientTooltipComponent::create).toList(), tooltipCenter - TOOLTIP_HALF_WIDTH - 12,
                    widgetY - formattedText.size() * (mc.font.lineHeight + 1) + 9, DefaultTooltipPositioner.INSTANCE,
                    null);
            }
        }
    }
}
