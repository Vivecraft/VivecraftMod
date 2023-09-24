package org.vivecraft.client.gui.settings;

import org.vivecraft.client.gui.settings.GuiQuickCommandsList.CommandEntry;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.Builder;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

public class GuiQuickCommandsList extends ObjectSelectionList<CommandEntry>
{
	public GuiQuickCommandsList(GuiQuickCommandEditor parent)
    {
        super(mc, parent.width, parent.height, 32, parent.height - 32, 20);

        for (String command : dh.vrSettings.vrQuickCommands)
        {
            mc.font.width(command);
            this.addEntry(new CommandEntry(command, this));
        }
    }

    @Override
    protected void renderSelection(GuiGraphics guiGraphics, int i, int j, int k, int l, int m) {
    }

    public static class CommandEntry extends Entry<CommandEntry>
    {
        private final Button btnDelete;
        public final EditBox txt;

        private CommandEntry(String command, GuiQuickCommandsList parent)
        {
            this.txt = new EditBox(mc.font, parent.width / 2 - 100, 60, 200, 20, Component.literal(""));
            this.txt.setValue(command);
            this.btnDelete = new Builder(Component.literal("X"),  (p) ->
                {
                    this.txt.setValue("");
                    this.txt.setFocused(true);
                })
                .size( 18,  18)
                .pos(0,  0)
                .build();
        }

        @Override
        public void setFocused(boolean bl) {
            txt.setFocused(bl);
        }

        @Override
        public boolean mouseClicked(double pMouseX, double p_94738_, int pMouseY)
        {
            return (this.btnDelete.mouseClicked(pMouseX, p_94738_, pMouseY) ||
                this.txt.mouseClicked(pMouseX, p_94738_, pMouseY) ||
                super.mouseClicked(pMouseX, p_94738_, pMouseY)
            );
        }

        @Override
        public boolean mouseDragged(double pMouseX, double p_94741_, int pMouseY, double p_94743_, double pButton)
        {
            return (this.btnDelete.isMouseOver(pMouseX, p_94741_) && this.btnDelete.mouseDragged(pMouseX, p_94741_, pMouseY, p_94743_, pButton)) || (this.txt.isMouseOver(pMouseX, p_94741_) && this.txt.mouseDragged(pMouseX, p_94741_, pMouseY, p_94743_, pButton)) || super.mouseDragged(pMouseX, p_94741_, pMouseY, p_94743_, pButton);
        }

        @Override
        public boolean mouseReleased(double pMouseX, double p_94754_, int pMouseY)
        {
            return (
                this.btnDelete.mouseReleased(pMouseX, p_94754_, pMouseY) ||
                this.txt.mouseReleased(pMouseX, p_94754_, pMouseY) ||
                super.mouseReleased(pMouseX, p_94754_, pMouseY)
            );
        }

        @Override
        public boolean mouseScrolled(double pMouseX, double p_94735_, double pMouseY)
        {
            return (
                this.btnDelete.mouseScrolled(pMouseX, p_94735_, pMouseY) ||
                this.txt.mouseScrolled(pMouseX, p_94735_, pMouseY) ||
                super.mouseScrolled(pMouseX, p_94735_, pMouseY)
            );
        }

        @Override
        public boolean charTyped(char pCodePoint, int pModifiers)
        {
            return (
                this.txt.isFocused() ? this.txt.charTyped(pCodePoint, pModifiers) : super.charTyped(pCodePoint, pModifiers)
            );
        }

        @Override
        public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
        {
            return (
                this.txt.isFocused() ?
                    this.txt.keyPressed(pKeyCode, pScanCode, pModifiers) :
                    super.keyPressed(pKeyCode, pScanCode, pModifiers)
            );
        }

        @Override
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
