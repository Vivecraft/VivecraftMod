package org.vivecraft.client.gui.framework;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.extensions.GuiExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.common.utils.math.Matrix4f;

public abstract class TwoHandedScreen extends Screen {
    protected ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
    public float cursorX1;
    public float cursorY1;
    public float cursorX2;
    public float cursorY2;
    private AbstractWidget lastHoveredButtonId1 = null;
    private AbstractWidget lastHoveredButtonId2 = null;
    protected boolean reinit;

    protected TwoHandedScreen() {
        super(Component.literal(""));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (this.reinit) {
            this.init();
            this.reinit = false;
        }

        double x1 = (double) this.cursorX1 * (double) this.width / (double) GuiHandler.guiWidth;
        double y1 = (double) this.cursorY1 * (double) this.height / (double) GuiHandler.guiHeight;
        double x2 = (double) this.cursorX2 * (double) this.width / (double) GuiHandler.guiWidth;
        double y2 = (double) this.cursorY2 * (double) this.height / (double) GuiHandler.guiHeight;

        AbstractWidget widget1 = null;
        AbstractWidget widget2 = null;

        for (int i = 0; i < this.children().size(); i++) {
            AbstractWidget widget = (AbstractWidget) this.children().get(i);

            boolean hover1 = widget.isMouseOver(x1, y1);
            boolean hover2 = widget.isMouseOver(x2, y2);

            if (hover1) {
                widget.render(guiGraphics, (int) x1, (int) y1, partialTicks);
            } else {
                widget.render(guiGraphics, (int) x2, (int) y2, partialTicks);
            }

            if (hover1) {
                widget1 = widget;
            }

            if (hover2) {
                widget2 = widget;
            }
        }

        if (widget1 == null) {
            this.lastHoveredButtonId1 = null;
        } else if (widget1 instanceof Button && this.lastHoveredButtonId1 != widget1) {
            MCVR.get().triggerHapticPulse(ControllerType.LEFT, 300);
            this.lastHoveredButtonId1 = widget1;
        }

        if (widget2 == null) {
            this.lastHoveredButtonId2 = null;
        } else if (widget2 instanceof Button && this.lastHoveredButtonId2 != widget2) {
            MCVR.get().triggerHapticPulse(ControllerType.RIGHT, 300);
            this.lastHoveredButtonId2 = widget2;
        }

        ((GuiExtension) this.minecraft.gui).vivecraft$drawMouseMenuQuad((int) x1, (int) y1);
        ((GuiExtension) this.minecraft.gui).vivecraft$drawMouseMenuQuad((int) x2, (int) y2);
    }

    public boolean processCursor(Vec3 Pos_room, Matrix4f Rotation_room, boolean mainCursor) {
        Vec2 tex = GuiHandler.getTexCoordsForCursor(Pos_room, Rotation_room, GuiHandler.guiScale, this.dh.vrPlayer.vrdata_room_pre.getController(mainCursor ? 1 : 0));

        // main hand
        float u = tex.x;
        float v = tex.y;

        float lastX = mainCursor ? this.cursorX1 : this.cursorX2;
        float lastY = mainCursor ? this.cursorY1 : this.cursorY2;

        float newX = (int) (u * GuiHandler.guiWidth);
        float newY = (int) (v * GuiHandler.guiHeight);

        boolean onScreen;

        if (u < 0.0F || v < 0.0F || u > 1.0F || v > 1.0F) {
            // offscreen
            newX = -1.0f;
            newY = -1.0f;
            onScreen = false;
        } else if (lastX == -1.0f) {
            // last position was not valid, so take the new one as is
            onScreen = true;
        } else {
            // apply some smoothing between mouse positions
            newX = lastX * 0.7f + newX * 0.3f;
            newY = lastY * 0.7f + newY * 0.3f;
            onScreen = true;
        }

        if (mainCursor) {
            this.cursorX1 = newX;
            this.cursorY1 = newY;
        } else {
            this.cursorX2 = newX;
            this.cursorY2 = newY;
        }

        return onScreen;
    }

}
