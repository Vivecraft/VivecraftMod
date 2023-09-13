package org.vivecraft.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.vivecraft.common.ConfigBuilder;

public class VectorWidget extends AbstractWidget {
    private final QuadWidget.NumberEditBox xBox;
    private final QuadWidget.NumberEditBox yBox;
    private final QuadWidget.NumberEditBox zBox;

    public VectorWidget(int i, int j, int k, int l, Component component, ConfigBuilder.VectorValue value) {
        super(i, j, k, l, component);
        Vector3f vector = value.get();
        this.xBox = new QuadWidget.NumberEditBox(Minecraft.getInstance().font, i , j, (k-3) / 3, l , Component.literal(vector.x +""), vector.x, d -> value.set(new Vector3f(d, vector.y, vector.z)));
        this.yBox = new QuadWidget.NumberEditBox(Minecraft.getInstance().font, i + ((k - 3) / 3) + 1, j, (k-3) / 3, l , Component.literal(vector.y +""), vector.y, d -> value.set(new Vector3f(vector.x, d, vector.z)));
        this.zBox = new QuadWidget.NumberEditBox(Minecraft.getInstance().font, i + ((k-3) / 3)*2 + 2, j, (k-4) / 3, l , Component.literal(vector.z +""), vector.z, d -> value.set(new Vector3f(vector.x, vector.y, d)));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        this.xBox.render(guiGraphics, i, j, f);
        this.yBox.render(guiGraphics, i, j, f);
        this.zBox.render(guiGraphics, i, j, f);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        this.xBox.renderWidget(guiGraphics, i, j, f);
        this.yBox.renderWidget(guiGraphics, i, j, f);
        this.zBox.renderWidget(guiGraphics, i, j, f);
    }

    @Override
    public void renderTexture(GuiGraphics guiGraphics, ResourceLocation resourceLocation, int i, int j, int k, int l, int m, int n, int o, int p, int q) {
        this.xBox.renderTexture(guiGraphics, resourceLocation, i, j, k, l, m, n, o, p, q);
        this.yBox.renderTexture(guiGraphics, resourceLocation, i, j, k, l, m, n, o, p, q);
        this.zBox.renderTexture(guiGraphics, resourceLocation, i, j, k, l, m, n, o, p, q);
    }

    @Override
    public void setX(int i) {
        super.setX(i);
        this.xBox.setX(i);
        this.yBox.setX(i + (width-3)/3 + 1);
        this.zBox.setX(i + ((width-3)/3)*2 + 2);
    }

    @Override
    public void setY(int i) {
        super.setY(i);
        this.xBox.setY(i);
        this.yBox.setY(i);
        this.zBox.setY(i);
    }

    @Override
    public void setFocused(boolean bl) {
        super.setFocused(bl);
        this.xBox.setFocused(this.xBox.isHovered());
        this.yBox.setFocused(this.yBox.isHovered());
        this.zBox.setFocused(this.zBox.isHovered());
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (xBox.isMouseOver(d, e)) {
            this.xBox.setFocused(true);
            return this.xBox.mouseClicked(d,e,i);
        }
        if (yBox.isMouseOver(d, e)) {
            this.yBox.setFocused(true);
            return this.yBox.mouseClicked(d,e,i);
        }
        if (zBox.isMouseOver(d, e)) {
            this.zBox.setFocused(true);
            return this.zBox.mouseClicked(d,e,i);
        }
        return super.mouseClicked(d, e, i);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (this.xBox.isFocused()) {
            return this.xBox.keyPressed(i, j, k);
        }
        if (this.yBox.isFocused()) {
            return this.yBox.keyPressed(i, j, k);
        }
        if (this.zBox.isFocused()) {
            return this.zBox.keyPressed(i, j, k);
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public boolean charTyped(char c, int i) {
        if (this.xBox.isFocused()) {
            return this.xBox.charTyped(c, i);
        }
        if (this.yBox.isFocused()) {
            return this.yBox.charTyped(c, i);
        }
        if (this.zBox.isFocused()) {
            return this.zBox.charTyped(c, i);
        }
        return super.charTyped(c, i);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
