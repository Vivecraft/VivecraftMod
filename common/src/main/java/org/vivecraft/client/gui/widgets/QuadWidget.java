package org.vivecraft.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaterniond;
import org.vivecraft.common.ConfigBuilder;

import java.util.function.Consumer;

public class QuadWidget extends AbstractWidget {

    private final NumberEditBox xBox;
    private final NumberEditBox yBox;
    private final NumberEditBox zBox;
    private final NumberEditBox wBox;

    public QuadWidget(int i, int j, int k, int l, Component component, ConfigBuilder.QuatValue value) {
        super(i, j, k, l, component);
        Quaterniond quaterniond = value.get();
        this.xBox = new NumberEditBox(Minecraft.getInstance().font, i , j, (k-4) / 4, l , Component.literal(quaterniond.x +""), quaterniond.x, d -> value.set(new Quaterniond(d, quaterniond.y, quaterniond.z, quaterniond.w)));
        this.yBox = new NumberEditBox(Minecraft.getInstance().font, i + ((k - 4) / 4) + 1, j, (k-4) / 4, l , Component.literal(quaterniond.y +""), quaterniond.y, d -> value.set(new Quaterniond(quaterniond.x, d, quaterniond.z, quaterniond.w)));
        this.zBox = new NumberEditBox(Minecraft.getInstance().font, i + ((k-4) / 4)*2 + 2, j, (k-4) / 4, l , Component.literal(quaterniond.z +""), quaterniond.z, d -> value.set(new Quaterniond(quaterniond.x, quaterniond.y, d, quaterniond.w)));
        this.wBox = new NumberEditBox(Minecraft.getInstance().font, i + ((k-4) / 4)*3 + 3, j, (k-4) / 4, l , Component.literal(quaterniond.w +""), quaterniond.w, d -> value.set(new Quaterniond(quaterniond.x, quaterniond.y, quaterniond.z, d)));

    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        this.xBox.render(guiGraphics, i, j, f);
        this.yBox.render(guiGraphics, i, j, f);
        this.zBox.render(guiGraphics, i, j, f);
        this.wBox.render(guiGraphics, i, j, f);

    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        this.xBox.renderWidget(guiGraphics, i, j, f);
        this.yBox.renderWidget(guiGraphics, i, j, f);
        this.zBox.renderWidget(guiGraphics, i, j, f);
        this.wBox.renderWidget(guiGraphics, i, j, f);
    }

    @Override
    public void renderTexture(GuiGraphics guiGraphics, ResourceLocation resourceLocation, int i, int j, int k, int l, int m, int n, int o, int p, int q) {
        this.xBox.renderTexture(guiGraphics, resourceLocation, i, j, k, l, m, n, o, p, q);
        this.yBox.renderTexture(guiGraphics, resourceLocation, i, j, k, l, m, n, o, p, q);
        this.zBox.renderTexture(guiGraphics, resourceLocation, i, j, k, l, m, n, o, p, q);
        this.wBox.renderTexture(guiGraphics, resourceLocation, i, j, k, l, m, n, o, p, q);
    }

    @Override
    public void setX(int i) {
        super.setX(i);
        this.xBox.setX(i);
        this.yBox.setX(i + (width-4)/4 + 1);
        this.zBox.setX(i + ((width-4)/4)*2 + 2);
        this.wBox.setX(i + ((width-4)/4)*3 + 3);
    }

    @Override
    public void setY(int i) {
        super.setY(i);
        this.xBox.setY(i);
        this.yBox.setY(i);
        this.zBox.setY(i);
        this.wBox.setY(i);
    }

    @Override
    public void setFocused(boolean bl) {
        super.setFocused(bl);
        this.xBox.setFocused(this.xBox.isHovered());
        this.yBox.setFocused(this.yBox.isHovered());
        this.zBox.setFocused(this.zBox.isHovered());
        this.wBox.setFocused(this.wBox.isHovered());


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
        if (wBox.isMouseOver(d, e)) {
            this.wBox.setFocused(true);
            return this.wBox.mouseClicked(d,e,i);
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
        if (this.wBox.isFocused()) {
            return this.wBox.keyPressed(i, j, k);
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
        if (this.wBox.isFocused()) {
            return this.wBox.charTyped(c, i);
        }
        return super.charTyped(c, i);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public static class NumberEditBox extends EditBox {

        private final Consumer<Double> doubleConsumer;

        public NumberEditBox(Font font, int i, int j, int k, int l, Component component, double doubleValue, Consumer<Double> doubleConsumer) {
            super(font, i, j, k, l, component);
            this.doubleConsumer = doubleConsumer;
            this.setValue(doubleValue + "");
        }

        @Override
        public boolean charTyped(char c, int i) {
            if (c != 46 && (c < 48 || c > 57)) {
                return false;
            }
            boolean ret = super.charTyped(c, i);
            doubleConsumer.accept(Double.valueOf(this.getValue()));
            return ret;
        }

        @Override
        public boolean keyPressed(int i, int j, int k) {
            boolean ret = super.keyPressed(i, j, k);
            doubleConsumer.accept(Double.valueOf(this.getValue()));
            return ret;
        }
    }
}
