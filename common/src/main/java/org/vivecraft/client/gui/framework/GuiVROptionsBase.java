package org.vivecraft.client.gui.framework;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.phys.Vec2;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.ScreenUtils;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.ArrayList;
import java.util.List;

public abstract class GuiVROptionsBase extends Screen {
    protected ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
    public static final int DONE_BUTTON = 200;
    public static final int DEFAULTS_BUTTON = 201;
    protected final Screen lastScreen;
    protected final VRSettings settings;
    //private VRTooltipManager tooltipManager = new VRTooltipManager(this, new TooltipProviderVROptions());
    protected boolean reinit;
    protected boolean drawDefaultButtons = true;
    protected ObjectSelectionList visibleList = null;
    private int nextButtonIndex = 0;
    public String vrTitle = "Title";
    private Button btnDone;
    private Button btnDefaults;

    public GuiVROptionsBase(Screen lastScreen) {
        super(Component.literal(""));
        this.lastScreen = lastScreen;
        this.settings = ClientDataHolderVR.getInstance().vrSettings;
    }

    protected void addDefaultButtons() {
        this.addRenderableWidget(this.btnDone = new Button.Builder(Component.translatable("gui.back"), (p) ->
        {
            if (!this.onDoneClicked()) {
                this.dataholder.vrSettings.saveOptions();
                this.minecraft.setScreen(this.lastScreen);
            }
        })
            .pos(this.width / 2 + 5, this.height - 30)
            .size(150, 20)
            .build());
        this.addRenderableWidget(this.btnDefaults = new Button.Builder(Component.translatable("vivecraft.gui.loaddefaults"), (p) ->
        {
            this.loadDefaults();
            this.dataholder.vrSettings.saveOptions();
            this.reinit = true;
        })
            .pos(this.width / 2 - 155, this.height - 30)
            .size(150, 20)
            .build());
    }

    protected boolean onDoneClicked() {
        return false;
    }

    protected void init(VROptionLayout[] settings, boolean clear) {
        if (clear) {
            this.clearWidgets();
        }

        int i = 0;

        for (final VROptionLayout vroptionlayout : settings) {
            if (vroptionlayout.getOption() != null && vroptionlayout.getOption().getEnumFloat()) {
                this.addRenderableWidget(new GuiVROptionSlider(vroptionlayout.getOrdinal(), vroptionlayout.getX(this.width), vroptionlayout.getY(this.height), vroptionlayout.getOption()) {
                    public void onClick(double pMouseX, double p_93635_) {
                        if (vroptionlayout.getCustomHandler() == null || !vroptionlayout.getCustomHandler().apply(this, new Vec2((float) pMouseX, (float) p_93635_))) {
                            super.onClick(pMouseX, p_93635_);
                        }
                    }
                });
            } else if (vroptionlayout.getOption() != null) {
                this.addRenderableWidget(new GuiVROptionButton(vroptionlayout.getOrdinal(), vroptionlayout.getX(this.width), vroptionlayout.getY(this.height), vroptionlayout.getOption(), vroptionlayout.getButtonText(), (p) ->
                {
                    if (vroptionlayout.getCustomHandler() == null || !vroptionlayout.getCustomHandler().apply((GuiVROptionButton) p, new Vec2(0.0F, 0.0F))) {
                        this.settings.setOptionValue(((GuiVROptionButton) p).getOption());
                        p.setMessage(Component.literal(vroptionlayout.getButtonText()));
                    }
                }));
            } else if (vroptionlayout.getScreen() != null) {
                this.addRenderableWidget(new GuiVROptionButton(vroptionlayout.getOrdinal(), vroptionlayout.getX(this.width), vroptionlayout.getY(this.height), vroptionlayout.getButtonText(), (p) ->
                {
                    try {
                        if (vroptionlayout.getCustomHandler() != null && vroptionlayout.getCustomHandler().apply((GuiVROptionButton) p, new Vec2(0.0F, 0.0F))) {
                            return;
                        }

                        this.settings.saveOptions();
                        this.minecraft.setScreen(vroptionlayout.getScreen().getConstructor(Screen.class).newInstance(this));
                    } catch (ReflectiveOperationException reflectiveoperationexception) {
                        reflectiveoperationexception.printStackTrace();
                    }
                }));
            } else if (vroptionlayout.getCustomHandler() != null) {
                this.addRenderableWidget(new GuiVROptionButton(vroptionlayout.getOrdinal(), vroptionlayout.getX(this.width), vroptionlayout.getY(this.height), vroptionlayout.getButtonText(), (p) ->
                {
                    vroptionlayout.getCustomHandler().apply((GuiVROptionButton) p, new Vec2(0.0F, 0.0F));
                }));
            } else {
                this.addRenderableWidget(new GuiVROptionButton(vroptionlayout.getOrdinal(), vroptionlayout.getX(this.width), vroptionlayout.getY(this.height), vroptionlayout.getButtonText(), (p) ->
                {
                }));
            }
        }

        ++i;
    }

    protected void loadDefaults() {
        for (GuiEventListener child : this.children()) {
            if (!(child instanceof GuiVROption optionButton)) {
                continue;
            }

            this.settings.loadDefault(optionButton.getOption());
        }
    }

    protected void init(VROptionEntry[] settings, boolean clear) {
        if (clear) {
            this.clearWidgets();
            this.nextButtonIndex = 0;
        }

        ArrayList<VROptionLayout> arraylist = new ArrayList<>();

        if (this.nextButtonIndex < this.children().size()) {
            this.nextButtonIndex = this.children().size();
        }

        int i = this.nextButtonIndex;

        for (int j = 0; j < settings.length; ++j) {
            VROptionLayout.Position vroptionlayout$position = settings[j].center ? VROptionLayout.Position.POS_CENTER : (i % 2 == 0 ? VROptionLayout.Position.POS_LEFT : VROptionLayout.Position.POS_RIGHT);

            if (settings[j].center && i % 2 != 0) {
                ++i;
            }

            if (settings[j].option != null) {
                if (settings[j].option != VRSettings.VrOptions.DUMMY) {
                    arraylist.add(new VROptionLayout(settings[j].option, settings[j].customHandler, vroptionlayout$position, (float) Math.floor((float) i / 2.0F), true, settings[j].title));
                }
            } else if (settings[j].customHandler != null) {
                arraylist.add(new VROptionLayout(settings[j].customHandler, vroptionlayout$position, (float) Math.floor((float) i / 2.0F), true, settings[j].title));
            }

            if (settings[j].center) {
                ++i;
            }

            ++i;
        }

        this.nextButtonIndex = i;
        this.init(arraylist.toArray(new VROptionLayout[0]), false);
    }

    protected void init(VRSettings.VrOptions[] settings, boolean clear) {
        VROptionEntry[] avroptionentry = new VROptionEntry[settings.length];

        for (int i = 0; i < settings.length; ++i) {
            avroptionentry[i] = new VROptionEntry(settings[i]);
        }

        this.init(avroptionentry, clear);
    }

    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        if (this.reinit) {
            this.reinit = false;
            VRSettings.VrOptions selected = this.getFocused() instanceof GuiVROptionButton option ? option.getOption() : null;
            this.init();
            if (selected != null) {
                List<?> items = this.children().stream().filter(listener -> listener instanceof GuiVROptionButton o && o.getOption() == selected).toList();
                if (!items.isEmpty()) {
                    this.setFocused((GuiEventListener) items.get(0));
                }
            }
        }

        super.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);

        if (this.visibleList != null) {
            this.visibleList.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
        }

        guiGraphics.drawCenteredString(this.font, Component.translatable(this.vrTitle), this.width / 2, 15, 16777215);

        if (this.btnDefaults != null) {
            this.btnDefaults.visible = this.drawDefaultButtons;
        }

        if (this.btnDone != null) {
            this.btnDone.visible = this.drawDefaultButtons;
        }

        renderTooltip(guiGraphics, pMouseX, pMouseY);
    }

    protected void actionPerformed(AbstractWidget button) {
    }

    protected void actionPerformedRightClick(AbstractWidget button) {
    }

    public boolean mouseClicked(double pMouseX, double p_94738_, int pMouseY) {
        boolean flag = super.mouseClicked(pMouseX, p_94738_, pMouseY);
        AbstractWidget abstractwidget = ScreenUtils.getSelectedButton((int) pMouseX, (int) p_94738_, ScreenUtils.getButtonList(this));

        if (abstractwidget != null) {
            if (pMouseY == 0) {
                this.actionPerformed(abstractwidget);
            } else if (pMouseY == 1) {
                this.actionPerformedRightClick(abstractwidget);
            }
        } else if (this.visibleList != null) {
            return this.visibleList.mouseClicked(pMouseX, p_94738_, pMouseY);
        }

        return flag;
    }

    public boolean mouseReleased(double pMouseX, double p_94754_, int pMouseY) {
        return this.visibleList != null ? this.visibleList.mouseReleased(pMouseX, p_94754_, pMouseY) : super.mouseReleased(pMouseX, p_94754_, pMouseY);
    }

    public boolean mouseDragged(double pMouseX, double p_94741_, int pMouseY, double p_94743_, double pButton) {
        return this.visibleList != null ? this.visibleList.mouseDragged(pMouseX, p_94741_, pMouseY, p_94743_, pButton) : super.mouseDragged(pMouseX, p_94741_, pMouseY, p_94743_, pButton);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollAmountX, double scrollAmountY) {
        if (this.visibleList != null) {
            this.visibleList.mouseScrolled(x, y, scrollAmountX, scrollAmountY);
        }

        return super.mouseScrolled(x, y, scrollAmountX, scrollAmountY);
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == 256) {
            if (!this.onDoneClicked()) {
                this.dataholder.vrSettings.saveOptions();
                this.minecraft.setScreen(this.lastScreen);
            }

            return true;
        } else {
            if (super.keyPressed(pKeyCode, pScanCode, pModifiers)) {
                if (this.getFocused() instanceof AbstractWidget widget) {
                    this.actionPerformed(widget);
                }
                return true;
            } else {
                return this.visibleList != null && this.visibleList.keyPressed(pKeyCode, pScanCode, pModifiers);
            }
        }
    }

    public boolean charTyped(char pCodePoint, int pModifiers) {
        return this.visibleList != null && this.visibleList.charTyped(pCodePoint, pModifiers) || super.charTyped(pCodePoint, pModifiers);
    }

    private void renderTooltip(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {
        GuiEventListener hover = null;

        if (this.minecraft.getLastInputType().isKeyboard()) {
            // only show focused tooltip when navigating with keyboard, so a click with the mouse removes it
            hover = this.getFocused();
        }
        // find active button
        if (hover == null) {
            for (GuiEventListener child : children()) {
                if (child instanceof AbstractWidget widget && this.isMouseOver(widget, pMouseX, pMouseY)) {
                    hover = child;
                }
            }
        }
        if (hover != null) {
            if (hover instanceof GuiVROption guiHover) {
                if (guiHover.getOption() != null) {
                    String tooltipString = "vivecraft.options." + guiHover.getOption().name() + ".tooltip";
                    String tooltip = "";
                    // check if it has a tooltip
                    if (I18n.exists(tooltipString)) {
                        tooltip = I18n.get(tooltipString, (Object) null);
                    }

                    if (dataholder.vrSettings.overrides.hasSetting(guiHover.getOption())) {
                        VRSettings.ServerOverrides.Setting setting = dataholder.vrSettings.overrides.getSetting(guiHover.getOption());
                        if (setting.isValueOverridden()) {
                            tooltip = I18n.get("vivecraft.message.overriddenbyserver") + tooltip;
                        } else if (setting.isFloat() && (setting.isValueMinOverridden() || setting.isValueMaxOverridden())) {
                            tooltip = I18n.get("vivecraft.message.limitedbyserver", setting.getValueMin(), setting.getValueMax()) + tooltip;
                        }
                    }
                    if (!tooltip.isEmpty()) {
                        // add format reset at line ends
                        tooltip = tooltip.replace("\n", "§r\n");

                        // make last line the roughly 308 wide
                        List<FormattedText> formattedText = font.getSplitter().splitLines(tooltip, 308, Style.EMPTY);
                        tooltip += " ".repeat((308 - (formattedText.size() == 0 ? 0 : font.width(formattedText.get(formattedText.size() - 1)))) / font.width(" "));

                        // if tooltip is not too low, draw below button, else above
                        if (guiHover.getY() + guiHover.getHeight() + formattedText.size() * (font.lineHeight + 1) + 14 < this.height) {
                            guiGraphics.renderTooltip(this.font, font.split(Component.literal(tooltip), 308), this.width / 2 - 166, guiHover.getY() + guiHover.getHeight() + 14);
                        } else {
                            guiGraphics.renderTooltip(this.font, font.split(Component.literal(tooltip), 308), this.width / 2 - 166, guiHover.getY() - formattedText.size() * (font.lineHeight + 1) + 9);
                        }
                    }
                }
            }
        }
    }

    private boolean isMouseOver(AbstractWidget widget, double x, double y) {
        return widget.visible && x >= widget.getX() && y >= widget.getY() && x < (widget.getX() + widget.getWidth()) && y < (widget.getY() + widget.getHeight());
    }
}
