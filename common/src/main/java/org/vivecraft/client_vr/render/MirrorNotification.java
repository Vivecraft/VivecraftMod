package org.vivecraft.client_vr.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.vivecraft.client.utils.TextUtils;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.render.helpers.GuiRenderHelper;

import java.util.ArrayList;

public class MirrorNotification {

    private static final Minecraft MC = Minecraft.getInstance();

    private static long MIRROR_NOTIFY_START;

    private static long MIRROR_NOTIFY_LEN;

    private static boolean MIRROR_NOTIFY_CLEAR;

    private static String MIRROR_NOTIFY_TEXT;

    /**
     * shows notification text on the desktop window
     *
     * @param text     text to show
     * @param clear    if the screen should be cleared to black
     * @param lengthMs how many milliseconds the text should be shown
     */
    public static void notify(String text, boolean clear, int lengthMs) {
        MIRROR_NOTIFY_START = System.currentTimeMillis();
        MIRROR_NOTIFY_TEXT = text;
        MIRROR_NOTIFY_CLEAR = clear;
        MIRROR_NOTIFY_LEN = lengthMs;
    }

    /**
     * draws the notification text
     */
    public static void render() {
        if (System.currentTimeMillis() < MIRROR_NOTIFY_START + MIRROR_NOTIFY_LEN) {
            int screenX = MC.mainRenderTarget.width;

            // override the gui scale, to be in absolute size
            int backupGuiScale = GuiHandler.GUI_SCALE_FACTOR;
            GuiHandler.GUI_SCALE_FACTOR = 1;

            GuiGraphics guiGraphics = GuiRenderHelper.getGuiGraphics();
            guiGraphics.pose().scale(3, 3);

            if (MIRROR_NOTIFY_CLEAR) {
                RenderSystem.getDevice().createCommandEncoder()
                    .clearColorTexture(MC.mainRenderTarget.getColorTexture(), 0);
            }

            final int TEXT_WORDWRAP_LEN = screenX / 22;
            ArrayList<String> wrapped = new ArrayList<>();

            if (MIRROR_NOTIFY_TEXT != null) {
                TextUtils.wordWrap(MIRROR_NOTIFY_TEXT, TEXT_WORDWRAP_LEN, wrapped);
            }

            int column = 1;
            final int COLUMN_GAP = 12;

            for (String line : wrapped) {
                guiGraphics.drawString(MC.font, line, 1, column, 0xFFFFFFFF);
                column += COLUMN_GAP;
            }

            GuiRenderHelper.finish();

            // reset gui scale
            GuiHandler.GUI_SCALE_FACTOR = backupGuiScale;
        }
    }
}
