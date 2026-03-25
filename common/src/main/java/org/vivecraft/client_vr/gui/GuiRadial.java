package org.vivecraft.client_vr.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.framework.screens.TwoHandedScreen;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;

import java.util.Arrays;

public class GuiRadial extends TwoHandedScreen {
    private boolean isShift = false;
    String[] arr;

    @Override
    public void init() {
        this.arr = this.dh.vrSettings.vrRadialItems;
        String[] altSet = this.dh.vrSettings.vrRadialItemsAlt;
        this.clearWidgets();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        if (this.isShift) {
            this.arr = altSet;
        }

        for (int i = 0; i < this.dh.vrSettings.vrRadialButtons; i++) {
            // not all buttons need to be set
            if (i >= this.arr.length) break;

            String current = this.arr[i];
            int index = i;
            Arrays.stream(this.minecraft.options.keyMappings)
                .filter(keymapping -> keymapping.getName().equalsIgnoreCase(current))
                .findFirst()
                .ifPresent(keymapping -> {
                    VRInputAction vrinputaction = MCVR.get().getInputAction(this.arr[index]);
                    String label = I18n.get(keymapping.getName());
                    if (vrinputaction != null &&
                        (vrinputaction.keyBinding.isDown() || MethodHolder.isKeyDown(vrinputaction.keyBinding.key)))
                    {
                        label = "§a" + label;
                    }
                    this.addRenderableWidget(createButton(label, (p) -> {
                        if (vrinputaction != null) {
                            if (vrinputaction.keyBinding.isDown() ||
                                MethodHolder.isKeyDown(vrinputaction.keyBinding.key))
                            {
                                vrinputaction.stopHoldingBinding(2);
                            } else {
                                vrinputaction.holdBinding();
                            }
                        }
                    }, index, centerX, centerY));
                });
        }
    }

    public void setShift(boolean shift) {
        if (shift != this.isShift) {
            this.isShift = shift;
            this.init();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.extractBackground(graphics, mouseX, mouseY, partialTick);
        super.extractRenderState(graphics, 0, 0, partialTick);
    }

    /**
     * creates a Button withte given {@code action} and positions it in the radial circle
     *
     * @param label   label of the button
     * @param action  action to call when pressing the button
     * @param index   button index in the circle
     * @param centerX center of the circle on the screen, x coordinate
     * @param centerY center of the circle on the screen, y coordinate
     * @return Button positioned athte right spot
     */
    public static Button createButton(
        String label, Button.OnPress action, int index, int centerX, int centerY)
    {
        int buttonWidthMin = 120;
        int numButtons = ClientDataHolderVR.getInstance().vrSettings.vrRadialButtons;

        // distance from the center, with 14 buttons, move them closer together
        float dist = numButtons * (numButtons >= 14 ? 5F : 5.5F);

        int buttonWidth = Math.max(buttonWidthMin, Minecraft.getInstance().font.width(label) + 4);
        // coords of the button, button 0 is at the top with x = 0, y = -dist
        float distX = numButtons * 4 + buttonWidth * 0.5F;

        // position buttons on equal y spacing
        float btnIndex = (index < numButtons / 2 ? index : numButtons - index) / (float) (numButtons / 2);
        int y = (int) (2.0F * dist * btnIndex - dist);

        // position x so the buttons produce an ellipse
        int x = (int) (distX * (Math.sqrt(1.0F - (y * y) / (dist * dist))));

        // move in between buttons closer to the middle
        if (Math.abs(y) > 20) {
            x = (int) (x * 0.87F);
        }

        // don't go over the center
        if (index != 0 && index != numButtons / 2) {
            x = Math.max(buttonWidth / 2, x);
        }

        // second half of buttons should be on the left side
        x *= index > numButtons / 2 ? -1 : 1;

        return new Button.Builder(Component.translatable(label), action)
            .size(buttonWidth, 20)
            .pos(centerX + x - buttonWidth / 2, centerY + y - 10)
            .build();
    }
}
