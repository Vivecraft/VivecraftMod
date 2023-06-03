package org.vivecraft.client.gui.framework;//package org.vivecraft.gui.framework;
//
//import java.awt.Rectangle;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import net.minecraft.ChatFormatting;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.components.AbstractWidget;
//import net.minecraft.client.gui.screens.Screen;
//import net.minecraft.network.chat.FormattedText;
//import net.minecraft.network.chat.Style;
//import net.minecraft.network.chat.Component;
//import net.optifine.Lang;
//import net.optifine.gui.TooltipProvider;
//import org.vivecraft.settings.VRSettings;
//import org.vivecraft.utils.Utils;
//
//public class TooltipProviderVROptions implements TooltipProvider
//{
//    public Rectangle getTooltipBounds(Screen guiScreen, int x, int y)
//    {
//        int i = guiScreen.width / 2 - 150;
//        int j = guiScreen.height / 6 - 7;
//
//        if (y <= j + 98)
//        {
//            j += 105;
//        }
//
//        int k = i + 150 + 150;
//        int l = j + 84 + 10;
//        return new Rectangle(i, j, k - i, l - j);
//    }
//
//    public boolean isRenderBorder()
//    {
//        return false;
//    }
//
//    public String[] getTooltipLines(AbstractWidget btn, int width)
//    {
//        if (!(btn instanceof GuiVROptionButton))
//        {
//            return null;
//        }
//        else
//        {
//            VRSettings.VrOptions vrsettings$vroptions = ((GuiVROptionButton)btn).getOption();
//
//            if (vrsettings$vroptions == null)
//            {
//                return null;
//            }
//            else
//            {
//                String s = "vivecraft.options." + vrsettings$vroptions.name() + ".tooltip";
//                String s1 = Lang.get(s, (String)null);
//
//                if (s1 == null)
//                {
//                    return null;
//                }
//                else
//                {
//                    String[] astring = s1.split("\\r?\\n", -1);
//                    List<String> list = new ArrayList<>();
//
//                    for (String s2 : astring)
//                    {
//                        if (s2.isEmpty())
//                        {
//                            list.add(s2);
//                        }
//                        else
//                        {
//                            int i = s2.indexOf(s2.trim().charAt(0));
//                            TextComponent textcomponent = i > 0 ? Component.literal(String.join("", Collections.nCopies(i, " "))) : null;
//                            List<FormattedText> list1 = Utils.wrapText(Component.literal(s2), width, Minecraft.getInstance().font, textcomponent);
//                            Style style = Style.EMPTY;
//
//                            for (FormattedText formattedtext : list1)
//                            {
//                                list.add(Utils.styleToFormatString(style) + formattedtext.getString());
//                                String s3 = formattedtext.getString();
//
//                                for (int j = 0; j < s3.length(); ++j)
//                                {
//                                    if (s3.charAt(j) == 167)
//                                    {
//                                        if (j + 1 >= s3.length())
//                                        {
//                                            break;
//                                        }
//
//                                        char c0 = s3.charAt(j + 1);
//                                        ChatFormatting chatformatting = ChatFormatting.getByCode(c0);
//
//                                        if (chatformatting != null)
//                                        {
//                                            style = style.applyLegacyFormat(chatformatting);
//                                        }
//
//                                        ++j;
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    return list.toArray(new String[0]);
//                }
//            }
//        }
//    }
//}
