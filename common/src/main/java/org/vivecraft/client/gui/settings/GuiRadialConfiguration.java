package org.vivecraft.client.gui.settings;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button.Builder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.ArrayUtils;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

import javax.annotation.Nonnull;

import static org.joml.Math.max;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

public class GuiRadialConfiguration extends org.vivecraft.client.gui.framework.GuiVROptionsBase {
    public static String vrTitle = "vivecraft.options.screen.radialmenu";
    private String[] arr;
    private boolean isShift = false;
    private int selectedIndex = -1;
    private GuiRadialItemsList list;
    private boolean isselectmode = false;

    public GuiRadialConfiguration(Screen guiScreen) {
        super(guiScreen);
    }

    public void setKey(KeyMapping key) {
        if (key != null) {
            this.arr[this.selectedIndex] = key.getName();
        } else {
            this.arr[this.selectedIndex] = "";
        }

        this.selectedIndex = -1;
        this.isselectmode = false;
        this.reinit = true;
        this.visibleList = null;

        if (!this.isShift) {
            dh.vrSettings.vrRadialItems = ArrayUtils.clone(this.arr);
        } else {
            dh.vrSettings.vrRadialItemsAlt = ArrayUtils.clone(this.arr);
        }

        dh.vrSettings.saveOptions();
    }

    @Override
    public void init() {
        this.list = new GuiRadialItemsList(this);
        this.clearWidgets();

        if (this.isselectmode) {
            this.addRenderableWidget(new Builder(Component.translatable("gui.cancel"), (p) ->
                {
                    this.isselectmode = false;
                    this.reinit = true;
                    this.visibleList = null;
                })
                    .size(150, 20)
                    .pos(this.width / 2 - 155, this.height - 25)
                    .build()
            );
            this.addRenderableWidget(new Builder(Component.translatable("vivecraft.gui.clear"), (p) ->
                this.setKey(null))
                    .size(150, 20)
                    .pos(this.width / 2 - 155, 25)
                    .build()
            );
        } else {
            super.init(VrOptions.RADIAL_MODE_HOLD);
            if (this.isShift) {
                this.addRenderableWidget(new Builder(Component.translatable("vivecraft.gui.radialmenu.mainset"), (p) ->
                    {
                        this.isShift = !this.isShift;
                        this.reinit = true;
                    })
                        .size(150, 20)
                        .pos(this.width / 2 + 2, 30)
                        .build()
                );
            } else {
                this.addRenderableWidget(new Builder(Component.translatable("vivecraft.gui.radialmenu.alternateset"), (p) ->
                    {
                        this.isShift = !this.isShift;
                        this.reinit = true;
                    })
                        .size(150, 20)
                        .pos(this.width / 2 + 2, 30)
                        .build()
                );
            }

            int i = 8;
            int j = 120;
            int k = 360 / i;
            int l = 48;
            int i1 = this.width / 2;
            int j1 = this.height / 2;
            this.arr = ArrayUtils.clone(dh.vrSettings.vrRadialItems);
            String[] astring = ArrayUtils.clone(dh.vrSettings.vrRadialItemsAlt);

            if (this.isShift) {
                this.arr = astring;
            }

            for (int k1 = 0; k1 < i; ++k1) {
                KeyMapping keymapping = null;

                for (KeyMapping keymapping1 : mc.options.keyMappings) {
                    if (keymapping1.getName().equalsIgnoreCase(this.arr[k1])) {
                        keymapping = keymapping1;
                    }
                }

                String s = "";

                if (keymapping != null) {
                    s = I18n.get(keymapping.getName());
                }

                int i2 = max(j, this.font.width(s));
                int j2 = 0;
                int k2 = 0;

                if (k1 == 0) {
                    j2 = 0;
                    k2 = -l;
                } else if (k1 == 1) {
                    j2 = i2 / 2 + 8;
                    k2 = -l / 2;
                } else if (k1 == 2) {
                    j2 = i2 / 2 + 32;
                    k2 = 0;
                } else if (k1 == 3) {
                    j2 = i2 / 2 + 8;
                    k2 = l / 2;
                } else if (k1 == 4) {
                    j2 = 0;
                    k2 = l;
                } else if (k1 == 5) {
                    j2 = -i2 / 2 - 8;
                    k2 = l / 2;
                } else if (k1 == 6) {
                    j2 = -i2 / 2 - 32;
                    k2 = 0;
                } else if (k1 == 7) {
                    j2 = -i2 / 2 - 8;
                    k2 = -l / 2;
                }

                int l1 = k1;
                this.addRenderableWidget(new Builder(Component.translatable(s), (p) ->
                    {
                        this.selectedIndex = l1;
                        this.isselectmode = true;
                        this.reinit = true;
                        this.visibleList = this.list;
                    })
                        .size(i2, 20)
                        .pos(i1 + j2 - i2 / 2, j1 + k2)
                        .build()
                );
            }
            super.addDefaultButtons();
        }
    }

    @Override
    protected void loadDefaults() {
        super.loadDefaults();
        dh.vrSettings.vrRadialItems = dh.vrSettings.getRadialItemsDefault();
        dh.vrSettings.vrRadialItemsAlt = dh.vrSettings.getRadialItemsAltDefault();
    }

    @Override
    protected boolean onDoneClicked() {
        if (this.isselectmode) {
            this.isselectmode = false;
            this.visibleList = null;
            return this.reinit = true;
        } else {
            return false;
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (this.visibleList == null) {
            guiGraphics.drawCenteredString(mc.font, Component.translatable("vivecraft.messages.radialmenubind.1"), this.width / 2, this.height - 50, 5635925);
        }

        if (this.isShift) {
            guiGraphics.drawCenteredString(mc.font, Component.translatable("vivecraft.messages.radialmenubind.2"), this.width / 2, this.height - 36, 13777015);
        }
    }
}
