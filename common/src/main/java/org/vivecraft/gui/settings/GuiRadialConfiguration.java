package org.vivecraft.gui.settings;

import org.apache.commons.lang3.ArrayUtils;
import org.vivecraft.gui.framework.GuiVROptionsBase;
import org.vivecraft.gui.framework.VROptionLayout;
import org.vivecraft.settings.VRSettings;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class GuiRadialConfiguration extends GuiVROptionsBase
{
    static VROptionLayout[] options = new VROptionLayout[] {
            new VROptionLayout(VRSettings.VrOptions.RADIAL_MODE_HOLD, VROptionLayout.Position.POS_LEFT, 0.0F, true, "")
    };
    private String[] arr;
    private boolean isShift = false;
    private int selectedIndex = -1;
    private GuiRadialItemsList list;
    private boolean isselectmode = false;

    public GuiRadialConfiguration(Screen guiScreen)
    {
        super(guiScreen);
    }

    public void setKey(KeyMapping key)
    {
        if (key != null)
        {
            this.arr[this.selectedIndex] = key.getName();
        }
        else
        {
            this.arr[this.selectedIndex] = "";
        }

        this.selectedIndex = -1;
        this.isselectmode = false;
        this.reinit = true;
        this.visibleList = null;

        if (!this.isShift)
        {
            this.dataholder.vrSettings.vrRadialItems = ArrayUtils.clone(this.arr);
        }
        else
        {
            this.dataholder.vrSettings.vrRadialItemsAlt = ArrayUtils.clone(this.arr);
        }

        this.dataholder.vrSettings.saveOptions();
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.radialmenu";
        this.list = new GuiRadialItemsList(this, this.minecraft);
        this.clearWidgets();

        if (this.isselectmode)
        {
            this.addRenderableWidget(new Button.Builder( Component.translatable("gui.cancel"),  (p) ->
                {
                    this.isselectmode = false;
                    this.reinit = true;
                    this.visibleList = null;
                })
                .size( 150,  20)
                .pos(this.width / 2 - 155,  this.height - 25)
                .build());
            this.addRenderableWidget(new Button.Builder( Component.translatable("vivecraft.gui.clear"),  (p) ->
                {
                    this.setKey((KeyMapping)null);
                })
                .size( 150,  20)
                .pos(this.width / 2 - 155,  25)
                .build());
        }
        else
        {
            if (this.isShift)
            {
                this.addRenderableWidget(new Button.Builder( Component.translatable("vivecraft.gui.radialmenu.mainset"),  (p) ->
                    {
                        this.isShift = !this.isShift;
                        this.reinit = true;
                    })
                    .size( 150,  20)
                    .pos(this.width / 2 + 2,  30)
                    .build());
            }
            else
            {
                this.addRenderableWidget(new Button.Builder( Component.translatable("vivecraft.gui.radialmenu.alternateset"),  (p) ->
                    {
                        this.isShift = !this.isShift;
                        this.reinit = true;
                    })
                    .size( 150,  20)
                    .pos(this.width / 2 + 2,  30)
                    .build());
            }

            super.init(options, false);
            int i = 8;
            int j = 120;
            int k = 360 / i;
            int l = 48;
            int i1 = this.width / 2;
            int j1 = this.height / 2;
            this.arr = ArrayUtils.clone(this.dataholder.vrSettings.vrRadialItems);
            String[] astring = ArrayUtils.clone(this.dataholder.vrSettings.vrRadialItemsAlt);

            if (this.isShift)
            {
                this.arr = astring;
            }

            for (int k1 = 0; k1 < i; ++k1)
            {
                KeyMapping keymapping = null;

                for (KeyMapping keymapping1 : this.minecraft.options.keyMappings)
                {
                    if (keymapping1.getName().equalsIgnoreCase(this.arr[k1]))
                    {
                        keymapping = keymapping1;
                    }
                }

                String s = "";

                if (keymapping != null)
                {
                    s = I18n.get(keymapping.getName());
                }

                int i2 = Math.max(j, this.font.width(s));
                int j2 = 0;
                int k2 = 0;

                if (k1 == 0)
                {
                    j2 = 0;
                    k2 = -l;
                }
                else if (k1 == 1)
                {
                    j2 = i2 / 2 + 8;
                    k2 = -l / 2;
                }
                else if (k1 == 2)
                {
                    j2 = i2 / 2 + 32;
                    k2 = 0;
                }
                else if (k1 == 3)
                {
                    j2 = i2 / 2 + 8;
                    k2 = l / 2;
                }
                else if (k1 == 4)
                {
                    j2 = 0;
                    k2 = l;
                }
                else if (k1 == 5)
                {
                    j2 = -i2 / 2 - 8;
                    k2 = l / 2;
                }
                else if (k1 == 6)
                {
                    j2 = -i2 / 2 - 32;
                    k2 = 0;
                }
                else if (k1 == 7)
                {
                    j2 = -i2 / 2 - 8;
                    k2 = -l / 2;
                }

                int l1 = k1;
                this.addRenderableWidget(new Button.Builder(  Component.translatable(s),  (p) ->
                    {
                        this.selectedIndex = l1;
                        this.isselectmode = true;
                        this.reinit = true;
                        this.visibleList = this.list;
                    })
                    .size( i2,  20)
                    .pos(i1 + j2 - i2 / 2,  j1 + k2)
                    .build());
                super.addDefaultButtons();
            }
        }
    }

    protected void loadDefaults()
    {
        super.loadDefaults();
        this.settings.vrRadialItems = this.settings.getRadialItemsDefault();
        this.settings.vrRadialItemsAlt = this.settings.getRadialItemsAltDefault();
    }

    protected boolean onDoneClicked()
    {
        if (this.isselectmode)
        {
            this.isselectmode = false;
            this.reinit = true;
            this.visibleList = null;
            return true;
        }
        else
        {
            return false;
        }
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);

        if (this.visibleList == null)
        {
            drawCenteredString(pMatrixStack, this.minecraft.font,  Component.translatable("vivecraft.messages.radialmenubind.1"), this.width / 2, this.height - 50, 5635925);
        }

        if (this.isShift)
        {
            drawCenteredString(pMatrixStack, this.minecraft.font,  Component.translatable("vivecraft.messages.radialmenubind.2"), this.width / 2, this.height - 36, 13777015);
        }
    }
}
