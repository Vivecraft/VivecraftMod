package org.vivecraft.gui.framework;

import org.vivecraft.settings.VRSettings;

import com.example.examplemod.DataHolder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;

public class GuiVROptionSlider extends GuiVROptionButton
{
    private double sliderValue = 1.0D;
    public boolean dragging;
    private final double minValue;
    private final double maxValue;

    public GuiVROptionSlider(int id, int x, int y, int width, int height, VRSettings.VrOptions option, double min, double max)
    {
        super(id, x, y, width, height, option, "", (p) ->
        {
        });
        this.minValue = min;
        this.maxValue = max;
        Minecraft minecraft = Minecraft.getInstance();
        DataHolder dataHolder = DataHolder.getInstance();
        this.sliderValue = this.enumOptions.normalizeValue(dataHolder.vrSettings.getOptionFloatValue(this.enumOptions));
        this.setMessage(new TextComponent(dataHolder.vrSettings.getButtonDisplayString(this.enumOptions)));
    }

    public GuiVROptionSlider(int id, int x, int y, VRSettings.VrOptions option, double min, double max)
    {
        this(id, x, y, 150, 20, option, min, max);
    }

    protected int getHoverState(boolean mouseOver)
    {
        return 0;
    }

    protected void onDrag(double pMouseX, double p_93637_, double pMouseY, double p_93639_)
    {
        this.setValueFromMouse(pMouseX);
        super.onDrag(pMouseX, p_93637_, pMouseY, p_93639_);
    }

    private void setValueFromMouse(double p_setValueFromMouse_1_)
    {
        Minecraft minecraft = Minecraft.getInstance();
        DataHolder dataHolder = DataHolder.getInstance();
        this.sliderValue = (double)((float)(p_setValueFromMouse_1_ - (double)(this.x + 4)) / (float)(this.width - 8));
        this.sliderValue = Mth.clamp(this.sliderValue, 0.0D, 1.0D);
        double d0 = this.enumOptions.denormalizeValue((float)this.sliderValue);
        dataHolder.vrSettings.setOptionFloatValue(this.enumOptions, (float)d0);
        this.sliderValue = this.enumOptions.normalizeValue((float)d0);
        this.setMessage(new TextComponent(dataHolder.vrSettings.getButtonDisplayString(this.enumOptions)));
    }

    protected void renderBg(PoseStack pMatrixStack, Minecraft pMinecraft, int pMouseX, int pMouseY)
    {
        if (this.visible)
        {
            pMinecraft.getTextureManager().bindForSetup(WIDGETS_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int i = (this.isHoveredOrFocused() ? 2 : 1) * 20;
            this.blit(pMatrixStack, this.x + (int)(this.sliderValue * (double)(this.width - 8)), this.y, 0, 46 + i, 4, 20);
            this.blit(pMatrixStack, this.x + (int)(this.sliderValue * (double)(this.width - 8)) + 4, this.y, 196, 46 + i, 4, 20);
        }
    }

    public void onClick(double pMouseX, double p_93635_)
    {
        this.sliderValue = (pMouseX - (double)(this.x + 4)) / (double)(this.width - 8);
        this.sliderValue = Mth.clamp(this.sliderValue, 0.0D, 1.0D);
        Minecraft minecraft = Minecraft.getInstance();
        DataHolder dataHolder = DataHolder.getInstance();
        dataHolder.vrSettings.setOptionFloatValue(this.enumOptions, (float)this.enumOptions.denormalizeValue((float)this.sliderValue));
        this.setMessage(new TextComponent(dataHolder.vrSettings.getButtonDisplayString(this.enumOptions)));
        this.dragging = true;
    }

    protected int getYImage(boolean pIsHovered)
    {
        return 0;
    }

    public void onRelease(double pMouseX, double p_93670_)
    {
        this.dragging = false;
    }
}
