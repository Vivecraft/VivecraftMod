package org.vivecraft.client.gui.framework.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client.gui.framework.TooltipRenderer;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client.gui.framework.VROptionLayout;
import org.vivecraft.client.gui.framework.widgets.GuiVROption;
import org.vivecraft.client.gui.framework.widgets.GuiVROptionButton;
import org.vivecraft.client.gui.framework.widgets.GuiVROptionSlider;
import org.vivecraft.client.gui.settings.GuiAllSettings;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.helpers.GuiHelper;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.TooltipUtil;

import java.util.ArrayList;

public abstract class GuiVROptionsBase extends Screen {
    public static final ResourceLocation VIVE_WIDGETS_LOCATION = new ResourceLocation("vivecraft",
        "textures/gui/widgets.png");

    protected ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
    protected final Screen lastScreen;
    protected final VRSettings vrSettings;
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
        this.vrSettings = ClientDataHolderVR.getInstance().vrSettings;
    }

    protected void addDefaultButtons() {
        int searchX, searchY;
        if (this.width > 360) {
            // but the search to the side
            searchX = this.width / 2 - 180;
            searchY = (int) Math.ceil((float) (this.height / 6) - 10.0F);
        } else {
            // but the search above
            searchX = this.width / 2 - 155;
            searchY = (int) Math.ceil((float) (this.height / 6) - 31.0F);
        }
        Button search = new ImageButton(
            searchX, searchY,
            20, 20, 0, 0, 20,
            VIVE_WIDGETS_LOCATION, 64, 64,
            (p) -> this.minecraft.setScreen(new GuiAllSettings(this)),
            (button, poseStack, x, y) -> GuiHelper.renderOnTooltip(button, poseStack, x, y,
                Component.translatable("vivecraft.options.screen.search")),
            Component.translatable("vivecraft.options.screen.search"));
        this.addRenderableWidget(search);

        this.addRenderableWidget(
            this.btnDone = new Button(this.width / 2 + 5, this.height - 30, 150, 20,
                Component.translatable("gui.back"), (p) -> {
                if (!this.onDoneClicked()) {
                    this.dataHolder.vrSettings.saveOptions();
                    this.minecraft.setScreen(this.lastScreen);
                }
            }));

        this.addRenderableWidget(
            this.btnDefaults = new Button(this.width / 2 - 155, this.height - 30, 150, 20,
                Component.translatable("vivecraft.gui.loaddefaults"), (p) -> {
                this.loadDefaults();
                this.dataHolder.vrSettings.saveOptions();
                this.reinit = true;
            }));

        // sort children from top left to bottom right, to fix tab navigation
        this.children().sort((a, b) -> {
            if (a instanceof AbstractWidget wA && b instanceof AbstractWidget wB) {
                if (wA.y < wB.y || (wA.y == wB.y && wA.x < wB.x)) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                return 0;
            }
        });
    }

    protected boolean onDoneClicked() {
        return false;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    protected void init(VROptionLayout[] settings, boolean clear) {
        // init with a complete layout
        if (clear) {
            this.clearWidgets();
        }

        for (final VROptionLayout layout : settings) {
            if (layout.getOption() != null && layout.getOption().getType() == VRSettings.OptionType.LIMITED_FLOAT) {
                // Option Slider
                this.addRenderableWidget(
                    new GuiVROptionSlider(layout.getOrdinal(), layout.getX(this.width), layout.getY(this.height),
                        layout.getOption())
                    {
                        @Override
                        public void onClick(double mouseX, double mouseY) {
                            if (layout.getCustomHandler() == null ||
                                !layout.getCustomHandler().apply(this, new Vec2((float) mouseX, (float) mouseY)))
                            {
                                super.onClick(mouseX, mouseY);
                            }
                        }
                    });
            } else if (layout.getOption() != null) {
                // Option Button
                this.addRenderableWidget(
                    new GuiVROptionButton(layout.getOrdinal(), layout.getX(this.width), layout.getY(this.height),
                        layout.getOption(), layout.getButtonText(), (p) -> {
                        if (layout.getCustomHandler() == null ||
                            !layout.getCustomHandler().apply((GuiVROptionButton) p, new Vec2(0.0F, 0.0F)))
                        {
                            this.vrSettings.setOptionValue(((GuiVROptionButton) p).getOption());
                            p.setMessage(Component.literal(layout.getButtonText()));
                        }
                    }));
            } else if (layout.getScreen() != null) {
                // Screen button
                this.addRenderableWidget(
                    new GuiVROptionButton(layout.getOrdinal(), layout.getX(this.width), layout.getY(this.height),
                        layout.getButtonText(), (p) -> {
                        try {
                            if (layout.getCustomHandler() != null &&
                                layout.getCustomHandler().apply((GuiVROptionButton) p, new Vec2(0.0F, 0.0F)))
                            {
                                return;
                            }

                            this.vrSettings.saveOptions();
                            this.minecraft.setScreen(layout.getScreen().getConstructor(Screen.class).newInstance(this));
                        } catch (ReflectiveOperationException e) {
                            VRSettings.LOGGER.error("Vivecraft: error setting screen: ", e);
                        }
                    }));
            } else if (layout.getCustomHandler() != null) {
                // Custom click handler button
                this.addRenderableWidget(
                    new GuiVROptionButton(layout.getOrdinal(), layout.getX(this.width), layout.getY(this.height),
                        layout.getButtonText(),
                        (p) -> layout.getCustomHandler().apply((GuiVROptionButton) p, new Vec2(0.0F, 0.0F))));
            } else {
                // just a button, do something with it on your own time.
                this.addRenderableWidget(
                    new GuiVROptionButton(layout.getOrdinal(), layout.getX(this.width), layout.getY(this.height),
                        layout.getButtonText(), (p) -> {}));
            }
        }
    }

    protected void loadDefaults() {
        for (GuiEventListener child : this.children()) {
            if (child instanceof GuiVROption optionButton) {
                this.vrSettings.loadDefault(optionButton.getOption());
            }
        }
    }

    protected void init(VROptionEntry[] settings, boolean clear) {
        // init with a generated layout from entries
        if (clear) {
            this.clearWidgets();
            this.nextButtonIndex = 0;
        }

        ArrayList<VROptionLayout> layouts = new ArrayList<>();

        if (this.nextButtonIndex < this.children().size()) {
            this.nextButtonIndex = this.children().size();
        }

        int nextIndex = this.nextButtonIndex;

        for (VROptionEntry setting : settings) {
            VROptionLayout.Position pos = setting.center ? VROptionLayout.Position.POS_CENTER :
                (nextIndex % 2 == 0 ? VROptionLayout.Position.POS_LEFT : VROptionLayout.Position.POS_RIGHT);

            if (setting.center && nextIndex % 2 != 0) {
                nextIndex++;
            }

            if (setting.option != null) {
                if (setting.option != VRSettings.VrOptions.DUMMY) {
                    layouts.add(new VROptionLayout(setting.option, setting.customHandler, pos,
                        (float) Math.floor(nextIndex / 2.0F), true, setting.title));
                }
            } else if (setting.customHandler != null) {
                layouts.add(new VROptionLayout(setting.customHandler, pos, (float) Math.floor(nextIndex / 2.0F), true,
                    setting.title));
            }

            if (setting.center) {
                nextIndex++;
            }

            nextIndex++;
        }

        this.nextButtonIndex = nextIndex;
        this.init(layouts.toArray(new VROptionLayout[0]), false);
    }

    protected void init(VRSettings.VrOptions[] settings, boolean clear) {
        // init with just a list and no special handlers
        VROptionEntry[] entries = new VROptionEntry[settings.length];

        for (int i = 0; i < settings.length; i++) {
            entries[i] = new VROptionEntry(settings[i]);
        }

        this.init(entries, clear);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (this.reinit) {
            this.reinit = false;
            // remember selected option
            VRSettings.VrOptions selected = this.getFocused() instanceof GuiVROption option ? option.getOption() : null;
            this.init();
            if (selected != null) {
                GuiEventListener newButton = this.children().stream()
                    .filter(listener -> listener instanceof GuiVROption option && option.getOption() == selected)
                    .findFirst().orElse(null);
                // refocus the new button, or clear it if it's not there
                this.setFocused(newButton);
            }
        }

        this.renderBackground(poseStack);

        if (this.visibleList != null) {
            this.visibleList.render(poseStack, mouseX, mouseY, partialTick);
        }

        super.render(poseStack, mouseX, mouseY, partialTick);

        drawCenteredString(poseStack, this.font, Component.translatable(this.vrTitle), this.width / 2, 15, 0xFFFFFFFF);

        if (this.btnDefaults != null) {
            this.btnDefaults.visible = this.drawDefaultButtons;
        }

        if (this.btnDone != null) {
            this.btnDone.visible = this.drawDefaultButtons;
        }

        renderTooltip(poseStack, mouseX, mouseY);
    }

    protected void actionPerformed(AbstractWidget widget) {}

    protected void actionPerformedRightClick(AbstractWidget widget) {}

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean success = super.mouseClicked(mouseX, mouseY, button);

        if (success && getFocused() instanceof AbstractWidget widget) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                this.actionPerformed(widget);
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                this.actionPerformedRightClick(widget);
            }
        } else if (this.visibleList != null) {
            return this.visibleList.mouseClicked(mouseX, mouseY, button);
        }

        return success;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.visibleList != null ? this.visibleList.mouseReleased(mouseX, mouseY, button) :
            super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return this.visibleList != null ? this.visibleList.mouseDragged(mouseX, mouseY, button, dragX, dragY) :
            super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (this.visibleList != null) {
            this.visibleList.mouseScrolled(mouseX, mouseY, scrollY);
        }

        return super.mouseScrolled(mouseX, mouseY, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (!this.onDoneClicked()) {
                this.dataHolder.vrSettings.saveOptions();
                this.minecraft.setScreen(this.lastScreen);
            }

            return true;
        } else {
            if (super.keyPressed(keyCode, scanCode, modifiers)) {
                if (this.getFocused() instanceof AbstractWidget widget) {
                    this.actionPerformed(widget);
                }
                return true;
            } else {
                return this.visibleList != null && this.visibleList.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return this.visibleList != null && this.visibleList.charTyped(codePoint, modifiers) ||
            super.charTyped(codePoint, modifiers);
    }

    private void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        GuiEventListener hover = null;

        // find active button
        if (hover == null) {
            for (GuiEventListener child : children()) {
                if (child instanceof AbstractWidget widget && this.isMouseOver(widget, mouseX, mouseY)) {
                    hover = child;
                }
            }
        }
        if (hover instanceof GuiVROption guiHover && guiHover.getOption() != null) {
            TooltipRenderer.renderTooltip(poseStack, TooltipUtil.getClientConfigTooltip(guiHover.getOption()),
                this.width / 2, guiHover.getY(), guiHover.getHeight());
        }
    }

    private boolean isMouseOver(AbstractWidget widget, double mouseX, double mouseY) {
        return widget.visible && mouseX >= widget.x && mouseY >= widget.y &&
            mouseX < (widget.x + widget.getWidth()) && mouseY < (widget.y + widget.getHeight());
    }
}
