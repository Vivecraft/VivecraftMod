package org.vivecraft.client_vr.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11C;
import org.vivecraft.client.utils.TextUtils;
import org.vivecraft.client_vr.extensions.WindowExtension;

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
            int screenX = ((WindowExtension) (Object) MC.getWindow()).vivecraft$getActualScreenWidth();
            int screenY = ((WindowExtension) (Object) MC.getWindow()).vivecraft$getActualScreenHeight();

            RenderSystem.viewport(0, 0, screenX, screenY);
            Matrix4f projection = Matrix4f.orthographic(0.0F, screenX,
                0.0F, screenY, 1000.0F, 3000.0F);
            RenderSystem.setProjectionMatrix(projection);

            RenderSystem.getModelViewStack().pushPose();
            RenderSystem.getModelViewStack().setIdentity();
            RenderSystem.getModelViewStack().translate(0, 0, -2000);
            RenderSystem.applyModelViewMatrix();

            RenderSystem.setShaderFogStart(Float.MAX_VALUE);

            PoseStack poseStack = new PoseStack();
            poseStack.scale(3, 3, 3);

            if (MIRROR_NOTIFY_CLEAR) {
                RenderSystem.clearColor(0F, 0F, 0F, 1F);
                RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);
            }

            final int TEXT_WORDWRAP_LEN = screenX / 22;
            ArrayList<String> wrapped = new ArrayList<>();

            if (MIRROR_NOTIFY_TEXT != null) {
                TextUtils.wordWrap(MIRROR_NOTIFY_TEXT, TEXT_WORDWRAP_LEN, wrapped);
            }

            int column = 1;
            final int COLUMN_GAP = 12;

            for (String line : wrapped) {
                MC.font.draw(poseStack, line, 1, column, 0xFFFFFFFF);
                column += COLUMN_GAP;
            }

            RenderSystem.getModelViewStack().popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }
}
