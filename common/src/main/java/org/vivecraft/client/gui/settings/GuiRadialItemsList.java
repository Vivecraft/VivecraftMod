package org.vivecraft.client.gui.settings;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public class GuiRadialItemsList extends net.minecraft.client.gui.components.ObjectSelectionList {
    private final GuiRadialConfiguration parent;
    private final int maxListLabelWidth = 90;

    public GuiRadialItemsList(GuiRadialConfiguration parent) {
        super(Minecraft.getInstance(), parent.width, parent.height, 63, parent.height - 32, 20);
        this.parent = parent;
        this.buildList();
    }

    public void buildList() {
        KeyMapping[] akeymapping = ArrayUtils.clone(Minecraft.getInstance().options.keyMappings);
        Arrays.sort(akeymapping);
        String s = null;

        for (KeyMapping keymapping : akeymapping) {
            String s1 = keymapping != null ? keymapping.getCategory() : null;

            if (s1 != null) {
                if (s1 != null && !s1.equals(s)) {
                    s = s1;
                    this.addEntry(new CategoryEntry(s1));
                }

                this.addEntry(new MappingEntry(keymapping, this.parent));
            }
        }
    }

    public static class CategoryEntry extends Entry {
        private final String labelText;
        private final int labelWidth;

        public CategoryEntry(String name) {
            this.labelText = I18n.get(name);
            this.labelWidth = Minecraft.getInstance().font.width(this.labelText);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
            guiGraphics.drawString(Minecraft.getInstance().font, this.labelText, (Minecraft.getInstance().screen.width / 2 - this.labelWidth / 2), (pTop + pHeight - 9 - 1), 6777215);
        }

        @Override
        public Component getNarration() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    public class MappingEntry extends Entry {
        private final KeyMapping myKey;
        private final GuiRadialConfiguration parentScreen;

        private MappingEntry(KeyMapping key, GuiRadialConfiguration parent) {
            this.myKey = key;
            this.parentScreen = parent;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
            final ChatFormatting chatformatting;

            if (pIsMouseOver) {
                chatformatting = ChatFormatting.GREEN;
            } else {
                chatformatting = ChatFormatting.WHITE;
            }

            guiGraphics.drawString(Minecraft.getInstance().font, chatformatting + I18n.get(this.myKey.getName()), (Minecraft.getInstance().screen.width / 2 - GuiRadialItemsList.this.maxListLabelWidth / 2), (pTop + pHeight / 2 - 9 / 2), 16777215);
        }

        @Override
        public boolean mouseClicked(double pMouseX, double p_94738_, int pMouseY) {
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
