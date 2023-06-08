package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.GuiGraphics;
import org.vivecraft.client_vr.ClientDataHolderVR;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

public class GuiQuickCommandsList extends ObjectSelectionList<GuiQuickCommandsList.CommandEntry>
{
	protected ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
    private final GuiQuickCommandEditor parent;
    private final Minecraft mc;

    public GuiQuickCommandsList(GuiQuickCommandEditor parent, Minecraft mc)
    {
        super(mc, parent.width, parent.height, 32, parent.height - 32, 20);
        this.parent = parent;
        this.mc = mc;
        String[] astring = this.dataholder.vrSettings.vrQuickCommands;
        String s = null;
        int i = 0;

        for (String s1 : astring)
        {
            this.minecraft.font.width(s1);
            this.addEntry(new CommandEntry(s1, this));
        }
    }

    public class CommandEntry extends Entry<CommandEntry>
    {
        private final Button btnDelete;
        public final EditBox txt;

        private CommandEntry(String command, GuiQuickCommandsList parent)
        {
            this.txt = new EditBox(GuiQuickCommandsList.this.minecraft.font, parent.width / 2 - 100, 60, 200, 20, Component.literal(""));
            this.txt.setValue(command);
            this.btnDelete = new Button.Builder(  Component.translatable("X"),  (p) ->
                {
                    this.txt.setValue("");
                    this.txt.setFocused(true);
                })
                .size( 18,  18)
                .pos(0,  0)
                .build();
        }

        public boolean mouseClicked(double pMouseX, double p_94738_, int pMouseY)
        {
            if (this.btnDelete.mouseClicked(pMouseX, p_94738_, pMouseY))
            {
                return true;
            }
            else
            {
                return this.txt.mouseClicked(pMouseX, p_94738_, pMouseY) ? true : super.mouseClicked(pMouseX, p_94738_, pMouseY);
            }
        }

        public boolean mouseDragged(double pMouseX, double p_94741_, int pMouseY, double p_94743_, double pButton)
        {
            if (this.btnDelete.mouseDragged(pMouseX, p_94741_, pMouseY, p_94743_, pButton))
            {
                return true;
            }
            else
            {
                return this.txt.mouseDragged(pMouseX, p_94741_, pMouseY, p_94743_, pButton) ? true : super.mouseDragged(pMouseX, p_94741_, pMouseY, p_94743_, pButton);
            }
        }

        public boolean mouseReleased(double pMouseX, double p_94754_, int pMouseY)
        {
            if (this.btnDelete.mouseReleased(pMouseX, p_94754_, pMouseY))
            {
                return true;
            }
            else
            {
                return this.txt.mouseReleased(pMouseX, p_94754_, pMouseY) ? true : super.mouseReleased(pMouseX, p_94754_, pMouseY);
            }
        }

        public boolean mouseScrolled(double pMouseX, double p_94735_, double pMouseY)
        {
            if (this.btnDelete.mouseScrolled(pMouseX, p_94735_, pMouseY))
            {
                return true;
            }
            else
            {
                return this.txt.mouseScrolled(pMouseX, p_94735_, pMouseY) ? true : super.mouseScrolled(pMouseX, p_94735_, pMouseY);
            }
        }

        public boolean charTyped(char pCodePoint, int pModifiers)
        {
            return this.txt.isFocused() ? this.txt.charTyped(pCodePoint, pModifiers) : super.charTyped(pCodePoint, pModifiers);
        }

        public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
        {
            return this.txt.isFocused() ? this.txt.keyPressed(pKeyCode, pScanCode, pModifiers) : super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }

        public void render(GuiGraphics guiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks)
        {
            this.txt.setX(pLeft);
            this.txt.setY(pTop);
            this.txt.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
            this.btnDelete.setX(this.txt.getX() + this.txt.getWidth() + 2);
            this.btnDelete.setY(this.txt.getY());
            this.btnDelete.visible = true;
            this.btnDelete.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
        }

		@Override
		public Component getNarration() {
			// TODO Auto-generated method stub
			return null;
		}
    }
}
