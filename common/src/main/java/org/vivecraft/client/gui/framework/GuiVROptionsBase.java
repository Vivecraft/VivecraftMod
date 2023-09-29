package org.vivecraft.client.gui.framework;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.Builder;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.phys.Vec2;
import org.joml.RoundingMode;
import org.vivecraft.client_vr.ScreenUtils;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;

import static org.joml.Math.floor;
import static org.joml.Math.roundUsing;
import static org.lwjgl.glfw.GLFW.*;
import static org.vivecraft.client.gui.framework.VROptionPosition.*;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

public abstract class GuiVROptionsBase extends Screen {
    public static final int DONE_BUTTON = 200;
    public static final int DEFAULTS_BUTTON = 201;
    protected final Screen lastScreen;
    //private VRTooltipManager tooltipManager = new VRTooltipManager(this, new TooltipProviderVROptions());
    protected boolean reinit;
    protected boolean drawDefaultButtons = true;
    protected ObjectSelectionList visibleList;
    private int nextButtonIndex = 0;
    public static String vrTitle = "Title";
    private Button btnDone;
    private Button btnDefaults;

    public GuiVROptionsBase(Screen lastScreen) {
        super(Component.literal(""));
        this.lastScreen = lastScreen;
    }

    protected void addDefaultButtons() {
        this.addRenderableWidget(this.btnDone = new Builder(Component.translatable("gui.back"), (p) ->
        {
            if (!this.onDoneClicked()) {
                dh.vrSettings.saveOptions();
                mc.setScreen(this.lastScreen);
            }
        })
            .pos(this.width / 2 + 5, this.height - 30)
            .size(150, 20)
            .build());
        this.addRenderableWidget(this.btnDefaults = new Builder(Component.translatable("vivecraft.gui.loaddefaults"), (p) ->
        {
            this.loadDefaults();
            dh.vrSettings.saveOptions();
            this.reinit = true;
        })
            .pos(this.width / 2 - 155, this.height - 30)
            .size(150, 20)
            .build());
    }

    @Override
    protected void clearWidgets() {
        super.clearWidgets();
        this.nextButtonIndex = 0;
    }

    protected boolean onDoneClicked() {
        return false;
    }

    protected void init(
        @Nullable Class<? extends Screen> subScreen,
        @Nullable VrOptions option,
        @Nullable BiFunction<GuiVROption, Vec2, Boolean> customHandler,
        @Nonnull VROptionPosition pos,
        float row,
        @Nonnull String buttonText
    ) {
        if (this.nextButtonIndex < this.children().size()) {
            this.nextButtonIndex = this.children().size();
        }

        int x = switch (pos) {
            case POS_LEFT -> {
                yield this.width / 2 - 155;
            }
            case POS_RIGHT -> {
                yield this.width / 2 - 155 + 160;
            }
            default -> {
                yield this.width / 2 - 155 + 80;
            }
        };

        int y = roundUsing((float) (this.height / 6) + 21.0F * row - 10.0F, RoundingMode.CEILING);

        if (option != null && option.getEnumFloat()) {
            this.addRenderableWidget(new GuiVROptionSlider(x, y, option) {
                @Override
                public void onClick(double mouseX, double mouseY) {
                    if (customHandler == null || !customHandler.apply(this, new Vec2((float) mouseX, (float) mouseY))) {
                        super.onClick(mouseX, mouseY);
                    }
                }
            });
        } else if (option != null) {
            this.addRenderableWidget(new GuiVROptionButton(x, y, option, dh.vrSettings.getButtonDisplayString(option), (p) ->
            {
                if (customHandler == null || !customHandler.apply((GuiVROptionButton) p, new Vec2(0.0F, 0.0F))) {
                    dh.vrSettings.setOptionValue(((GuiVROptionButton) p).getOption());
                    p.setMessage(Component.literal(dh.vrSettings.getButtonDisplayString(option)));
                }
            }));
        } else if (subScreen != null) {
            this.addRenderableWidget(new GuiVROptionButton(x, y, buttonText, (p) ->
            {
                try {
                    if (customHandler != null && customHandler.apply((GuiVROptionButton) p, new Vec2(0.0F, 0.0F))) {
                        return;
                    }

                    dh.vrSettings.saveOptions();
                    mc.setScreen(subScreen.getConstructor(Screen.class).newInstance(this));
                } catch (ReflectiveOperationException reflectiveoperationexception) {
                    reflectiveoperationexception.printStackTrace();
                }
            }));
        } else if (customHandler != null) {
            this.addRenderableWidget(new GuiVROptionButton(x, y, buttonText, (p) ->
            {
                customHandler.apply((GuiVROptionButton) p, new Vec2(0.0F, 0.0F));
            }));
        } else {
            this.addRenderableWidget(new GuiVROptionButton(x, y, buttonText, (p) -> {
            }));
        }
    }

    protected void loadDefaults() {
        for (GuiEventListener child : this.children()) {
            if (child instanceof GuiVROption optionButton) {
                dh.vrSettings.loadDefault(optionButton.getOption());
            }
        }
    }

    protected final void init(
        @Nonnull BiFunction<GuiVROption, Vec2, Boolean> customHandler,
        @Nonnull VROptionPosition pos,
        float row,
        @Nonnull String buttonText
    ) {
        this.init(null, null, customHandler, pos, row, buttonText);
    }

    @SafeVarargs
    protected final void init(@Nonnull Class<? extends GuiVROptionsBase>... subScreens) {
        for (Class<? extends GuiVROptionsBase> subScreen : subScreens) {
            this.init(subScreen);
        }
    }

    protected void init(@Nonnull Class<? extends GuiVROptionsBase> subScreen) {
        this.init(subScreen, null, null);
    }

    protected void init(@Nonnull Class<? extends GuiVROptionsBase> subScreen, @Nullable String buttonText) {
        this.init(subScreen, buttonText, null);
    }

    protected void init(@Nonnull Class<? extends GuiVROptionsBase> subScreen, @Nullable VROptionPosition pos) {
        this.init(subScreen, null, pos);
    }

    protected void init(@Nonnull Class<? extends GuiVROptionsBase> subScreen, @Nullable String buttonText, @Nullable VROptionPosition pos) {
        this.init(subScreen, buttonText, null, null, pos);
    }

    protected void init(@Nonnull final VrOptions... options) {
        for (final VrOptions setting : options) {
            this.init(setting);
        }
    }

    protected void init(@Nonnull final VrOptions option) {
        this.init(option, (VROptionPosition) null);
    }

    protected void init(@Nonnull final VrOptions option, @Nullable final VROptionPosition pos) {
        this.init(option, null, pos);
    }

    protected void init(@Nonnull final VrOptions option, @Nullable final BiFunction<GuiVROption, Vec2, Boolean> customHandler) {
        this.init(option, customHandler, null);
    }

    protected void init(@Nonnull final VrOptions option, @Nullable final BiFunction<GuiVROption, Vec2, Boolean> customHandler, @Nullable final VROptionPosition pos) {
        this.init(null, option, customHandler, pos);
    }

    protected void init(@Nullable final String buttonText) {
        this.init(buttonText, null);
    }

    protected void init(@Nullable final String buttonText, @Nullable final BiFunction<GuiVROption, Vec2, Boolean> customHandler) {
        this.init(buttonText, customHandler, null);
    }

    protected void init(@Nullable final String buttonText, @Nullable final BiFunction<GuiVROption, Vec2, Boolean> customHandler, @Nullable final VROptionPosition pos) {
        this.init(buttonText, null, customHandler, pos);
    }

    protected void init(@Nullable final String buttonText, @Nullable final VrOptions option, @Nullable final BiFunction<GuiVROption, Vec2, Boolean> customHandler, @Nullable VROptionPosition pos) {
        this.init(null, buttonText, option, customHandler, pos);
    }

    protected void init(@Nullable Class<? extends GuiVROptionsBase> subScreen, @Nullable String buttonText, @Nullable final VrOptions option, @Nullable final BiFunction<GuiVROption, Vec2, Boolean> customHandler, @Nullable VROptionPosition pos) {
        if (this.nextButtonIndex < this.children().size()) {
            this.nextButtonIndex = this.children().size();
        }

        if (pos == null) {
            pos = (this.nextButtonIndex % 2) == 0 ? POS_LEFT : POS_RIGHT;
        } else {
            if (pos == POS_CENTER || (this.nextButtonIndex % 2) == (pos == POS_LEFT ? 1 : 0)) {
                ++this.nextButtonIndex;
            }
        }

        if (buttonText == null || buttonText.isEmpty()) {
            try {
                buttonText = (String) subScreen.getDeclaredField("vrTitle").get(null);
            } catch (Exception ignored) {
                buttonText = "";
            }
        }

        if (subScreen != null) {
            this.init(
                subScreen,
                option,
                customHandler,
                pos,
                floor(this.nextButtonIndex / 2.0F),
                buttonText
            );
        } else if (option != null) {
            if (option != VrOptions.DUMMY) {
                this.init(subScreen, option, customHandler, pos, floor(this.nextButtonIndex / 2.0F), buttonText);
            }
        } else if (customHandler != null) {
            this.init(subScreen, option, customHandler, pos, floor(this.nextButtonIndex / 2.0F), buttonText);
        }

        ++this.nextButtonIndex;
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.reinit) {
            this.reinit = false;
            this.init();
        }

        this.renderBackground(guiGraphics);

        if (this.visibleList != null) {
            this.visibleList.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        try {
            guiGraphics.drawCenteredString(this.font, Component.translatable((String) this.getClass().getDeclaredField("vrTitle").get(null)), this.width / 2, 15, 16777215);
        } catch (Exception ignored) {
            guiGraphics.drawCenteredString(this.font, Component.translatable(vrTitle), this.width / 2, 15, 16777215);
        }

        if (this.btnDefaults != null) {
            this.btnDefaults.visible = this.drawDefaultButtons;
        }

        if (this.btnDone != null) {
            this.btnDone.visible = this.drawDefaultButtons;
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    protected void actionPerformed(AbstractWidget button) {
    }

    protected void actionPerformedRightClick(AbstractWidget button) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean flag = super.mouseClicked(mouseX, mouseY, button);
        AbstractWidget abstractwidget = ScreenUtils.getSelectedButton((int) mouseX, (int) mouseY, ScreenUtils.getButtonList(this));

        if (abstractwidget != null) {
            if (button == GLFW_MOUSE_BUTTON_1) {
                this.actionPerformed(abstractwidget);
            } else if (button == GLFW_MOUSE_BUTTON_2) {
                this.actionPerformedRightClick(abstractwidget);
            }
        } else if (this.visibleList != null) {
            return this.visibleList.mouseClicked(mouseX, mouseY, button);
        }

        return flag;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.visibleList != null ? this.visibleList.mouseReleased(mouseX, mouseY, button) : super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return this.visibleList != null ? this.visibleList.mouseDragged(mouseX, mouseY, button, dragX, dragY) : super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.visibleList != null) {
            this.visibleList.mouseScrolled(mouseX, mouseY, delta);
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW_KEY_ESCAPE) {
            if (!this.onDoneClicked()) {
                dh.vrSettings.saveOptions();
                mc.setScreen(this.lastScreen);
            }

            return true;
        } else {
            return this.visibleList != null && this.visibleList.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return this.visibleList != null && this.visibleList.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
    }

    private void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        AbstractWidget hover = null;
        // find active button
        for (GuiEventListener child : this.children()) {
            if (child instanceof AbstractWidget renderable && renderable.isMouseOver(mouseX, mouseY)) {
                hover = renderable;
            }
        }
        if (hover != null) {
            if (hover instanceof GuiVROption guiHover) {
                VrOptions option = guiHover.getOption();
                String tooltipString = "vivecraft.options." + guiHover.getOption().name() + ".tooltip";
                // check if it has a tooltip
                String tooltip = option.getTooltipString(tooltipString);
                if (tooltip == null && I18n.exists(tooltipString)) {
                    tooltip = I18n.get(tooltipString, (Object) null);
                }
                if (tooltip != null) {
                    // add format reset at line ends
                    tooltip = tooltip.replace("\n", "§r\n");

                    // make last line the roughly 308 wide
                    List<FormattedText> formattedText = this.font.getSplitter().splitLines(tooltip, 308, Style.EMPTY);
                    tooltip += " ".repeat((308 - (formattedText.size() == 0 ? 0 : this.font.width(formattedText.get(formattedText.size() - 1)))) / this.font.width(" "));

                    // if tooltip is not too low, draw below button, else above
                    if (guiHover.getY() + guiHover.getHeight() + formattedText.size() * (font.lineHeight + 1) + 14 < this.height) {
                        guiGraphics.renderTooltip(this.font, this.font.split(Component.literal(tooltip), 308), this.width / 2 - 166, guiHover.getY() + guiHover.getHeight() + 14);
                    } else {
                        guiGraphics.renderTooltip(this.font, this.font.split(Component.literal(tooltip), 308), this.width / 2 - 166, guiHover.getY() - formattedText.size() * (this.font.lineHeight + 1) + 9);
                    }
                }
            }
        }
    }
}
