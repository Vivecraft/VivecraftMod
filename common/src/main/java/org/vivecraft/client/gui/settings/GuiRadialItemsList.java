package org.vivecraft.client.gui.settings;

import java.util.Arrays;

import net.minecraft.client.gui.GuiGraphics;
import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class GuiRadialItemsList extends ObjectSelectionList
{
    private final GuiRadialConfiguration parent;
    private final Minecraft mc;
    private Entry[] listEntries;
    private int maxListLabelWidth = 0;

    public GuiRadialItemsList(GuiRadialConfiguration parent, Minecraft mc)
    {
        super(mc, parent.width, parent.height, 63, parent.height - 32, 20);
        this.parent = parent;
        this.mc = mc;
        this.maxListLabelWidth = 90;
        this.buildList();
    }

    public void buildList()
    {
        KeyMapping[] akeymapping = ArrayUtils.clone(this.mc.options.keyMappings);
        Arrays.sort((Object[])akeymapping);
        String s = null;

        for (KeyMapping keymapping : akeymapping)
        {
            String s1 = keymapping != null ? keymapping.getCategory() : null;

            if (s1 != null)
            {
                if (s1 != null && !s1.equals(s))
                {
                    s = s1;
                    this.addEntry(new CategoryEntry(s1));
                }

                this.addEntry(new MappingEntry(keymapping, this.parent));
            }
        }
    }

    public class CategoryEntry extends Entry
    {
        private final String labelText;
        private final int labelWidth;

        public CategoryEntry(String name)
        {
            this.labelText = I18n.get(name);
            this.labelWidth = GuiRadialItemsList.this.mc.font.width(this.labelText);
        }

        public void render(GuiGraphics guiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks)
        {
            guiGraphics.drawString(mc.font, this.labelText, (mc.screen.width / 2 - this.labelWidth / 2), (pTop + pHeight - 9 - 1), 6777215);
        }

		@Override
		public Component getNarration() {
			// TODO Auto-generated method stub
			return null;
		}
    }

    public class MappingEntry extends Entry
    {
        private final KeyMapping myKey;
        private GuiRadialConfiguration parentScreen;

        private MappingEntry(KeyMapping key, GuiRadialConfiguration parent)
        {
            this.myKey = key;
            this.parentScreen = parent;
        }

        public void render(GuiGraphics guiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks)
        {
            ChatFormatting chatformatting = ChatFormatting.WHITE;

            if (pIsMouseOver)
            {
                chatformatting = ChatFormatting.GREEN;
            }

            guiGraphics.drawString(mc.font, chatformatting + I18n.get(this.myKey.getName()), (mc.screen.width / 2 - GuiRadialItemsList.this.maxListLabelWidth / 2), (pTop + pHeight / 2 - 9 / 2), 16777215);
        }

        public boolean mouseClicked(double pMouseX, double p_94738_, int pMouseY)
        {
            this.parentScreen.setKey(this.myKey);
            return true;
        }

		@Override
		public Component getNarration() {
			// TODO Auto-generated method stub
			return null;
		}
    }
}
