package org.vivecraft.client_vr.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
import org.vivecraft.client.utils.TextUtils;
import org.vivecraft.client_vr.extensions.WindowExtension;

import java.util.ArrayList;

public class MirrorNotification {

    private static final Minecraft mc = Minecraft.getInstance();

    private static long mirrorNotifyStart;

    private static long mirrorNotifyLen;

    private static boolean mirrorNotifyClear;

    private static String mirrorNotifyText;

    /**
     * shows notification text on the desktop window
     * @param text text to show
     * @param clear if the screen should be cleared to black
     * @param lengthMs how many milliseconds the text should be shown
     */
    public static void notify(String text, boolean clear, int lengthMs) {
        mirrorNotifyStart = System.currentTimeMillis();
        mirrorNotifyText = text;
        mirrorNotifyClear = clear;
        mirrorNotifyLen = lengthMs;
    }

    /**
     * draws the notification text
     */
    public static void render() {
        if (System.currentTimeMillis() < mirrorNotifyStart + mirrorNotifyLen) {
            int screenX = ((WindowExtension) (Object) mc.getWindow()).vivecraft$getActualScreenWidth();
            int screenY = ((WindowExtension) (Object) mc.getWindow()).vivecraft$getActualScreenHeight();

            RenderSystem.viewport(0, 0, screenX, screenY);
            Matrix4f projection = new Matrix4f().setOrtho(0.0F, screenX,
                screenY, 0.0F, 1000.0F, 21000.0F);
            RenderSystem.setProjectionMatrix(projection, VertexSorting.ORTHOGRAPHIC_Z);

            RenderSystem.getModelViewStack().pushPose();
            RenderSystem.getModelViewStack().setIdentity();
            RenderSystem.getModelViewStack().translate(0, 0, -11000);
            RenderSystem.applyModelViewMatrix();

            RenderSystem.setShaderFogStart(Float.MAX_VALUE);

            GuiGraphics guiGraphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
            guiGraphics.pose().scale(3, 3, 3);

            if (mirrorNotifyClear) {
                RenderSystem.clearColor(0, 0, 0, 0);
                RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
            } else {
                RenderSystem.clear(GL11C.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
            }

            final int TEXT_WORDWRAP_LEN = screenX / 22;
            ArrayList<String> wrapped = new ArrayList<>();

            if (mirrorNotifyText != null) {
                TextUtils.wordWrap(mirrorNotifyText, TEXT_WORDWRAP_LEN, wrapped);
            }

            int column = 1;
            final int COLUMN_GAP = 12;

            for (String line : wrapped) {
                guiGraphics.drawString(mc.font, line, 1, column, 0xFFFFFF);
                column += COLUMN_GAP;
            }
            guiGraphics.flush();

            RenderSystem.getModelViewStack().popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }
}
