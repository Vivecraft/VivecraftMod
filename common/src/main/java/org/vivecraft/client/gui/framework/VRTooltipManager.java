package org.vivecraft.client.gui.framework;//Optifine
//package org.vivecraft.gui.framework;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import java.awt.Rectangle;
//import java.util.Arrays;
//import java.util.List;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.Font;
//import net.minecraft.client.gui.GuiComponent;
//import net.minecraft.client.gui.components.AbstractWidget;
//import net.minecraft.client.gui.components.Widget;
//import net.minecraft.client.gui.screens.Screen;
//import net.optifine.gui.GuiScreenOF;
//import net.optifine.gui.TooltipProvider;
//
//public class VRTooltipManager
//{
//    public final Screen guiScreen;
//    public final TooltipProvider tooltipProvider;
//    protected int lastMouseX = 0;
//    protected int lastMouseY = 0;
//    protected long mouseStillTime = 0L;
//
//    public VRTooltipManager(Screen guiScreen, TooltipProvider tooltipProvider)
//    {
//        this.guiScreen = guiScreen;
//        this.tooltipProvider = tooltipProvider;
//    }
//
//    public void drawTooltips(PoseStack matrixStackIn, int x, int y, List<AbstractWidget> buttonList)
//    {
//        if (Math.abs(x - this.lastMouseX) <= 15 && Math.abs(y - this.lastMouseY) <= 15)
//        {
//            int i = 700;
//
//            if (System.currentTimeMillis() >= this.mouseStillTime + (long)i)
//            {
//                AbstractWidget abstractwidget = Screen.getSelectedButton((int)x, (int)y, buttonList);
//
//                if (abstractwidget != null)
//                {
//                    Rectangle rectangle = this.tooltipProvider.getTooltipBounds(this.guiScreen, x, y);
//                    String[] astring = this.tooltipProvider.getTooltipLines(abstractwidget, rectangle.width - 10);
//
//                    if (astring != null)
//                    {
//                        if (astring.length > 8)
//                        {
//                            astring = Arrays.copyOf(astring, 8);
//                            astring[astring.length - 1] = astring[astring.length - 1] + " ...";
//                        }
//
//                        if (this.tooltipProvider.isRenderBorder())
//                        {
//                            int j = -528449408;
//                            this.drawRectBorder(matrixStackIn, rectangle.x, rectangle.y, rectangle.x + rectangle.width, rectangle.y + rectangle.height, j);
//                        }
//
//                        GuiComponent.fill(matrixStackIn, rectangle.x, rectangle.y, rectangle.x + rectangle.width, rectangle.y + rectangle.height, -536870912);
//
//                        for (int l = 0; l < astring.length; ++l)
//                        {
//                            String s = astring[l];
//                            int k = 14540253;
//                            Font font = Minecraft.getInstance().font;
//                            font.drawShadow(matrixStackIn, s, (float)(rectangle.x + 5), (float)(rectangle.y + 5 + l * 11), k);
//                        }
//                    }
//                }
//            }
//        }
//        else
//        {
//            this.lastMouseX = x;
//            this.lastMouseY = y;
//            this.mouseStillTime = System.currentTimeMillis();
//        }
//    }
//
//    private void drawRectBorder(PoseStack matrixStackIn, int x1, int y1, int x2, int y2, int col)
//    {
//        GuiComponent.fill(matrixStackIn, x1, y1 - 1, x2, y1, col);
//        GuiComponent.fill(matrixStackIn, x1, y2, x2, y2 + 1, col);
//        GuiComponent.fill(matrixStackIn, x1 - 1, y1, x1, y2, col);
//        GuiComponent.fill(matrixStackIn, x2, y1, x2 + 1, y2, col);
//    }
//}
