package org.vivecraft.client_vr.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gui.keyboard.EasterEggTheme;
import org.vivecraft.client_vr.gui.keyboard.KeyboardKeys;
import org.vivecraft.client_vr.gui.keyboard.KeyboardTheme;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.utils.RGBAColor;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PhysicalKeyboard {
    private static final float SPACING = 0.0064F;
    private static final float KEY_WIDTH = 0.04F;
    private static final float KEY_HEIGHT = 0.04F;
    private static final float KEY_WIDTH_SPECIAL = KEY_WIDTH * 2 + SPACING;

    private final Minecraft mc = Minecraft.getInstance();
    private final ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
    private boolean reinit;
    private boolean shift;
    private boolean shiftSticky;
    private final List<KeyButton> keys;
    private int rows;
    private int columns;
    private float spacing;
    private float keyWidth;
    private float keyHeight;
    private float keyWidthSpecial;
    private float scale = 1.0F;
    private final KeyButton[] pressedKey = new KeyButton[2];
    private final long[] pressTime = new long[2];
    private final long[] pressRepeatTime = new long[2];
    private long shiftPressTime;
    private boolean lastPressedShift;
    private Supplier<String> easterEggText = () -> {
        int[] data = {0xbc, 0xa1, 0xb7, 0xaf, 0xa2, 0xee, 0xbc, 0xaf, 0xa7, 0xa0, 0xac, 0xa1, 0xb9};
        byte[] bytes = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            bytes[i] = (byte) (data[i] ^ 0xce);
        }
        String str = new String(bytes, StandardCharsets.UTF_8);
        this.easterEggText = () -> str; // poor man's lazy init
        return str;
    };
    private int easterEggIndex = 0;
    private boolean easterEggActive;
    private final Map<Integer, RGBAColor> customTheme = new HashMap<>();

    public PhysicalKeyboard() {
        this.keys = new ArrayList<>();
    }

    public void init() {
        this.unpressAllKeys();
        this.keys.clear();

        this.spacing = SPACING * this.scale;
        this.keyWidth = KEY_WIDTH * this.scale;
        this.keyHeight = KEY_HEIGHT * this.scale;
        this.keyWidthSpecial = KEY_WIDTH_SPECIAL * this.scale;

        KeyboardKeys.Layout layout = KeyboardKeys.getRegularKeys(this.shift, () -> {
            if (!PhysicalKeyboard.this.shiftSticky) {
                setShift(false, false);
            }
        });

        this.rows = layout.rows();
        this.columns = layout.columns();

        for (KeyboardKeys.Key key : layout.keys()) {
            int y = key.y() < 0 ? this.rows - key.y() : key.y();
            this.addKey(new KeyButton(
                key.x() * (this.keyWidth + this.spacing), (y - 1) * (this.keyHeight + this.spacing),
                key.width() * this.keyWidth + (key.width() - 1) * this.spacing,
                key.height() * this.keyHeight + (key.height() - 1) * this.spacing, key));
        }

        List<KeyboardKeys.Key> specialKeys = KeyboardKeys.getSpecialKeys(() -> {
            if (this.shift && !this.shiftSticky && ClientUtils.milliTime() - this.shiftPressTime < 400L) {
                setShift(true, true);
            } else {
                setShift(!this.shift, false);
            }
            this.shiftPressTime = ClientUtils.milliTime();
        });
        for (KeyboardKeys.Key key : specialKeys) {
            int y = key.y() < 0 ? this.rows - key.y() : key.y();
            this.addKey(new KeyButton(
                key.x() * (this.keyWidth + this.spacing),
                (y - 1) * (this.keyHeight + this.spacing),
                key.width() * this.keyWidth + (key.width() - 1) * this.spacing,
                key.height() * this.keyHeight, key)
            {

                @Override
                public RGBAColor getRenderColor() {
                    if (this.key.isShift() && PhysicalKeyboard.this.shift) {
                        RGBAColor color = new RGBAColor(this.pressed ? 1.0F : 0.5F, this.pressed ? 1.0F : 0.5F,
                            0.0F, 0.5F);
                        if (!PhysicalKeyboard.this.shiftSticky) {
                            color.r = 0.0F;
                        }
                        return color;
                    }
                    return super.getRenderColor();
                }
            });
        }

        // Set pressed keys to the new objects
        for (int c = 0; c < 2; c++) {
            if (this.pressedKey[c] != null) {
                for (KeyButton key : this.keys) {
                    if (key.key.id() == this.pressedKey[c].key.id()) {
                        this.pressedKey[c] = key;
                        key.pressed = true;
                        break;
                    }
                }
            }
        }

        this.dh.vrSettings.physicalKeyboardTheme.theme.reload();

        this.reinit = false;
    }

    public void process() {
        if (this.reinit) {
            this.init();
        }

        for (int c = 0; c < 2; ++c) {
            ControllerType controller = ControllerType.values()[c];
            KeyButton key = this.findTouchedKey(controller);

            long milliTime = ClientUtils.milliTime();
            if (key != null) {
                if (key != this.pressedKey[c] && milliTime - this.pressTime[c] >= 150L) {
                    if (this.pressedKey[c] != null) {
                        this.pressedKey[c].unpress();
                        this.pressedKey[c] = null;
                    }

                    key.press(controller, false);
                    this.pressedKey[c] = key;
                    this.pressTime[c] = milliTime;
                    this.pressRepeatTime[c] = milliTime;
                } else if (key == this.pressedKey[c] && milliTime - this.pressTime[c] >= 500L &&
                    milliTime - this.pressRepeatTime[c] >= 100L)
                {
                    key.press(controller, true);
                    this.pressRepeatTime[c] = milliTime;
                }
            } else if (this.pressedKey[c] != null) {
                this.pressedKey[c].unpress();
                this.pressedKey[c] = null;
                this.pressTime[c] = milliTime;
            }
        }
    }

    public void processBindings() {
        if (GuiHandler.KEY_KEYBOARD_SHIFT.consumeClick()) {
            this.setShift(true, true);
            this.lastPressedShift = true;
        }

        if (!GuiHandler.KEY_KEYBOARD_SHIFT.isDown() && this.lastPressedShift) {
            this.setShift(false, false);
            this.lastPressedShift = false;
        }
    }

    private Vector3f getCenterPos() {
        return new Vector3f(
            ((this.keyWidth + this.spacing) * (this.columns + this.columns % 2.0F / 2.0F) +
                (this.keyWidthSpecial + this.spacing) * 2.0F
            ) / 2.0F,
            (this.keyHeight + this.spacing) * (this.rows + 1),
            0.0F);
    }

    private KeyButton findTouchedKey(ControllerType controller) {
        // Transform the controller into keyboard space
        Matrix4f matrix = new Matrix4f();
        matrix.translate(this.getCenterPos());
        matrix.mul(new Matrix4f(KeyboardHandler.ROTATION_ROOM).invert());
        matrix.translate(-KeyboardHandler.POS_ROOM.x, -KeyboardHandler.POS_ROOM.y, -KeyboardHandler.POS_ROOM.z);

        Vector3f pos = matrix.transformPosition(
            this.dh.vrPlayer.vrdata_room_pre.getController(controller.ordinal()).getPositionF());

        // Do intersection checks
        for (KeyButton key : this.keys) {
            if (key.getCollisionBoundingBox().contains(pos.x, pos.y, pos.z)) {
                return key;
            }
        }

        return null;
    }

    private void updateEasterEgg(String label) {
        String text = this.easterEggText.get();
        if (this.easterEggIndex < text.length()) {
            if (label.toLowerCase().equals(String.valueOf(text.charAt(this.easterEggIndex)))) {
                this.easterEggIndex++;
            } else {
                this.easterEggIndex = 0;
            }
        } else if (label.equals("Enter")) {
            this.easterEggActive = !this.easterEggActive;
        } else {
            this.easterEggIndex = 0;
        }
    }

    private void drawBox(BufferBuilder buf, AABB box, RGBAColor color, PoseStack poseStack) {
        // Alright let's draw a box
        Matrix4f matrix = poseStack.last().pose();
        float minX = (float) box.minX, minY = (float) box.minY, minZ = (float) box.minZ;
        float maxX = (float) box.maxX, maxY = (float) box.maxY, maxZ = (float) box.maxZ;

        // front
        buf.vertex(matrix, minX, minY, minZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();
        buf.vertex(matrix, minX, maxY, minZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();
        buf.vertex(matrix, maxX, maxY, minZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();
        buf.vertex(matrix, maxX, minY, minZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();

        // top
        buf.vertex(matrix, minX, minY, minZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();
        buf.vertex(matrix, maxX, minY, minZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();
        buf.vertex(matrix, maxX, minY, maxZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();
        buf.vertex(matrix, minX, minY, maxZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();

        // left
        buf.vertex(matrix, minX, minY, minZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();
        buf.vertex(matrix, minX, minY, maxZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();
        buf.vertex(matrix, minX, maxY, maxZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();
        buf.vertex(matrix, minX, maxY, minZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();

        // back
        buf.vertex(matrix, maxX, maxY, maxZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();
        buf.vertex(matrix, minX, maxY, maxZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();
        buf.vertex(matrix, minX, minY, maxZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();
        buf.vertex(matrix, maxX, minY, maxZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();

        // bottom
        buf.vertex(matrix, maxX, maxY, maxZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();
        buf.vertex(matrix, maxX, maxY, minZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();
        buf.vertex(matrix, minX, maxY, minZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();
        buf.vertex(matrix, minX, maxY, maxZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();

        // right
        buf.vertex(matrix, maxX, maxY, maxZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();
        buf.vertex(matrix, maxX, minY, maxZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();
        buf.vertex(matrix, maxX, minY, minZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();
        buf.vertex(matrix, maxX, maxY, minZ).uv(0, 0)
            .color(color.r, color.g, color.b, color.a)
            .endVertex();
        // Woo that was fun
    }

    public void render(PoseStack poseStack) {
        // no keys, don't render
        if (this.keys.isEmpty()) return;
        poseStack.pushPose();
        Vector3f center = this.getCenterPos();
        poseStack.translate(-center.x, -center.y, -center.z);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();


        KeyboardTheme.Theme theme =
            this.easterEggActive ? EasterEggTheme.INSTANCE : this.dh.vrSettings.physicalKeyboardTheme.theme;
        for (KeyButton button : this.keys) {
            theme.updateColor(button.color, button.key.id(), button.key.x(), button.key.y());
        }

        // We need to ignore depth so we can see the back faces and text
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        // Stuff for drawing labels
        Font font = this.mc.font;
        ArrayList<Tuple<Component, Vector3f>> labels = new ArrayList<>();
        float textScale = 0.002F * this.scale;

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        ShadersHelper.bindTexture(RenderHelper.WHITE_TEXTURE);

        // Start building vertices for key boxes
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        for (KeyButton key : this.keys) {
            AABB box = key.getRenderBoundingBox();
            RGBAColor color = key.getRenderColor();

            // Draw the key itself
            this.drawBox(buf, box, color, poseStack);

            // Calculate text position
            float stringWidth = (float) font.width(key.key.label()) * textScale;
            float stringHeight = font.lineHeight * textScale;
            float textX = (float) box.minX + ((float) box.maxX - (float) box.minX) / 2.0F - stringWidth / 2.0F;
            float textY = (float) box.minY + ((float) box.maxY - (float) box.minY) / 2.0F - stringHeight / 2.0F;
            float textZ = (float) box.minZ + ((float) box.maxZ - (float) box.minZ) / 2.0F;

            // Put label in the list
            labels.add(new Tuple<>(key.key.label(), new Vector3f(textX, textY, textZ)));
        }

        // Draw all the key boxes
        BufferUploader.drawWithShader(buf.end());

        RenderSystem.depthFunc(GL11.GL_LEQUAL);

        // Start building vertices for text
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(buf);

        // Build all the text
        for (Tuple<Component, Vector3f> label : labels) {
            poseStack.pushPose();
            poseStack.translate(label.getB().x, label.getB().y, label.getB().z);
            poseStack.scale(textScale, textScale, 1.0F);
            font.drawInBatch(label.getA(), 0.0F, 0.0F, 0xFFFFFFFF, false, poseStack.last().pose(), bufferSource,
                Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
        }

        // Draw all the labels
        bufferSource.endBatch();

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        poseStack.popPose();
    }

    public void show() {
        if (!this.shiftSticky) {
            this.shift = false;
        }

        this.scale = this.dh.vrSettings.physicalKeyboardScale;
        this.reinit = true;
    }

    public void unpressAllKeys() {
        for (KeyButton key : this.keys) {
            if (key.pressed) {
                key.unpress();
            }
        }
    }

    private KeyButton addKey(KeyButton key) {
        this.keys.add(key);
        return key;
    }

    public boolean isShift() {
        return this.shift;
    }

    public boolean isShiftSticky() {
        return this.shiftSticky;
    }

    public void setShift(boolean shift, boolean sticky) {
        if (shift != this.shift || sticky != this.shiftSticky) {
            this.shift = shift;
            this.shiftSticky = shift && sticky;
            this.reinit = true;
        }
    }

    public float getScale() {
        return this.scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
        this.reinit = true;
    }

    private class KeyButton {
        public final AABB boundingBox;
        public final KeyboardKeys.Key key;
        public RGBAColor color = new RGBAColor(1.0F, 1.0F, 1.0F, 0.5F);
        public boolean pressed;

        public KeyButton(float x, float y, float width, float height, KeyboardKeys.Key key) {
            this.boundingBox = new AABB(x, y, 0.0D, x + width, y + height, 0.028D * PhysicalKeyboard.this.scale);
            this.key = key;
        }

        public AABB getRenderBoundingBox() {
            return this.pressed ? this.boundingBox.move(0.0D, 0.0D, 0.012D * PhysicalKeyboard.this.scale) :
                this.boundingBox;
        }

        public AABB getCollisionBoundingBox() {
            return this.pressed ? this.boundingBox.expandTowards(0.0D, 0.0D, 0.08D) : this.boundingBox;
        }

        public RGBAColor getRenderColor() {
            RGBAColor color = this.color.copy();

            if (!this.pressed) {
                color.r *= 0.5F;
                color.g *= 0.5F;
                color.b *= 0.5F;
            }

            return color;
        }

        public final void press(ControllerType controller, boolean isRepeat) {
            if (!isRepeat) {
                PhysicalKeyboard.this.mc.getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }

            MCVR.get().triggerHapticPulse(controller, isRepeat ? 300 : 600);
            this.pressed = true;
            this.key.onPress().run();
            updateEasterEgg(this.key.label().getString());
        }

        public final void unpress() {
            this.pressed = false;
            this.key.onRelease().run();
        }
    }
}
