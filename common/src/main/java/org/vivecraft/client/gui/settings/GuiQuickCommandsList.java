package org.vivecraft.client.gui.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import org.vivecraft.client_vr.ClientDataHolderVR;

public class GuiQuickCommandsList extends ObjectSelectionList<GuiQuickCommandsList.CommandEntry> {
    protected ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();

    public GuiQuickCommandsList(GuiQuickCommandEditor parent, Minecraft mc) {
        super(mc, parent.width, parent.height - 64, 32, 20);

        for (String command : this.dataholder.vrSettings.vrQuickCommands) {
            this.minecraft.font.width(command);
            this.addEntry(new CommandEntry(command, this));
        }
    }

    @Override
    protected void renderSelection(GuiGraphics guiGraphics, int top, int width, int height, int outerColor, int innerColor) {
    }

    public class CommandEntry extends Entry<CommandEntry> {
        private final Button btnDelete;
        public final EditBox txt;

        private CommandEntry(String command, GuiQuickCommandsList parent) {
            this.txt = new EditBox(GuiQuickCommandsList.this.minecraft.font, parent.width / 2 - 100, 60, 200, 20,
                Component.literal(""));
            this.txt.setMaxLength(256);
            this.txt.setValue(command);
            this.btnDelete = new Button.Builder(Component.literal("X"), (p) ->
            {
                this.txt.setValue("");
                this.txt.setFocused(true);
            })
                .size(18, 18)
                .pos(0, 0)
                .build();
        }

        @Override
        public void setFocused(boolean focused) {
            this.txt.setFocused(focused);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.btnDelete.mouseClicked(mouseX, mouseY, button)) {
                return true;
            } else {
                return this.txt.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
            }
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (this.btnDelete.isMouseOver(mouseX, mouseY) && this.btnDelete.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                return true;
            } else {
                return (this.txt.isMouseOver(mouseX, mouseY) && this.txt.mouseDragged(mouseX, mouseY, button, dragX, dragY)) || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (this.btnDelete.mouseReleased(mouseX, mouseY, button)) {
                return true;
            } else {
                return this.txt.mouseReleased(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button);
            }
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            if (this.btnDelete.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                return true;
            } else {
                return this.txt.mouseScrolled(mouseX, mouseY, scrollX, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
            }
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            return this.txt.isFocused() ? this.txt.charTyped(codePoint, modifiers) : super.charTyped(codePoint, modifiers);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return this.txt.isFocused() ? this.txt.keyPressed(keyCode, scanCode, modifiers) : super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            this.txt.setX(left);
            this.txt.setY(top);
            this.txt.render(guiGraphics, mouseX, mouseY, partialTick);
            this.btnDelete.setX(this.txt.getX() + this.txt.getWidth() + 2);
            this.btnDelete.setY(this.txt.getY());
            this.btnDelete.visible = true;
            this.btnDelete.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public Component getNarration() {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
