package org.vivecraft.client_vr.gui;

import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.InputSimulator;
import org.vivecraft.client_vr.settings.OptionEnum;
import org.vivecraft.common.utils.color.Color;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.AABB;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.*;

import static org.joml.Math.*;
import static org.joml.RoundingMode.CEILING;
import static org.lwjgl.glfw.GLFW.*;

public class PhysicalKeyboard
{
    private boolean reinit;
    private boolean shift;
    private boolean shiftSticky;
    private final List<KeyButton> keys = new ArrayList<>();
    private static final int ROWS = 4;
    private static final int COLUMNS = 13;
    private static final float SPACING = 0.0064F;
    private static final float KEY_WIDTH = 0.04F;
    private static final float KEY_HEIGHT = 0.04F;
    private static final float KEY_WIDTH_SPECIAL = KEY_WIDTH * 2 + SPACING;
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
        for (int i = 0; i < data.length; i++)
            bytes[i] = (byte)(data[i] ^ 0xce);
        String str = new String(bytes, StandardCharsets.UTF_8);
        this.easterEggText = () -> str; // poor man's lazy init
        return str;
    };
    private int easterEggIndex = 0;
    private boolean easterEggActive;
    private Map<Integer, Color> customTheme = new HashMap<>();

    public void init()
    {
        this.keys.clear();
        this.rows = ROWS;
        this.columns = COLUMNS;
        this.spacing = SPACING * this.scale;
        this.keyWidth = KEY_WIDTH * this.scale;
        this.keyHeight = KEY_HEIGHT * this.scale;
        this.keyWidthSpecial = KEY_WIDTH_SPECIAL * this.scale;

        String chars = this.shift ? dh.vrSettings.keyboardKeysShift : dh.vrSettings.keyboardKeys;

        float calcRows = (float)chars.length() / (float)this.columns;
        if (abs((float)this.rows - calcRows) > 0.01F)
        {
            this.rows = roundUsing(calcRows, CEILING);
        }

        for (int i = 0; i < this.rows; ++i)
        {
            for (int j = 0; j < this.columns; ++j)
            {
                int k = i * this.columns + j;
                char c0 = ' ';

                if (k < chars.length())
                {
                    c0 = chars.charAt(k);
                }

                final char c1 = c0;
                this.addKey(new KeyButton(k, String.valueOf(c0), this.keyWidthSpecial + this.spacing + j * (this.keyWidth + this.spacing), i * (this.keyHeight + this.spacing), this.keyWidth, this.keyHeight)
                {
                    @Override
                    public void onPressed()
                    {
                        InputSimulator.typeChar(c1);

                        if (!PhysicalKeyboard.this.shiftSticky)
                        {
                            PhysicalKeyboard.this.setShift(false, false);
                        }

                        if (c1 == '/' && mc.screen == null)
                        {
                            InputSimulator.pressKey(GLFW_KEY_SLASH);
                            InputSimulator.releaseKey(GLFW_KEY_SLASH);
                        }
                    }
                });
            }
        }

        for (int l = 0; l < 2; ++l)
        {
            this.addKey(new KeyButton(1000 + l, "Shift", l == 1 ? this.keyWidthSpecial + this.spacing + this.columns * (this.keyWidth + this.spacing) : 0.0F, 3.0F * (this.keyHeight + this.spacing), this.keyWidthSpecial, this.keyHeight)
            {
                @Override
                public void onPressed()
                {
                    if (PhysicalKeyboard.this.shift && !PhysicalKeyboard.this.shiftSticky && milliTime() - PhysicalKeyboard.this.shiftPressTime < 400L)
                    {
                        PhysicalKeyboard.this.setShift(true, true);
                    }
                    else
                    {
                        PhysicalKeyboard.this.setShift(!PhysicalKeyboard.this.shift, false);
                    }

                    PhysicalKeyboard.this.shiftPressTime = milliTime();
                }

                @Override
                public Color getRenderColor() {
                    return (PhysicalKeyboard.this.shift ?
                        new Color(
                            (!shiftSticky ? Byte.MIN_VALUE : this.pressed ? Byte.MAX_VALUE : (byte) -1),
                            this.pressed ? Byte.MAX_VALUE : (byte) -1
                        ) :
                        super.getRenderColor()
                    );
                }
            });
        }

        this.addKey(new KeyButton(1002, " ", this.keyWidthSpecial + this.spacing + (float)(this.columns - 5) / 2.0F * (this.keyWidth + this.spacing), (float)this.rows * (this.keyHeight + this.spacing), 5.0F * (this.keyWidth + this.spacing) - this.spacing, this.keyHeight)
        {
            @Override
            public void onPressed()
            {
                InputSimulator.typeChar(' ');
            }
        });
        this.addKey(new KeyButton(1003, "Tab", 0.0F, this.keyHeight + this.spacing, this.keyWidthSpecial, this.keyHeight)
        {
            @Override
            public void onPressed()
            {
                InputSimulator.pressKey(GLFW_KEY_TAB);
                InputSimulator.releaseKey(GLFW_KEY_TAB);
            }
        });
        this.addKey(new KeyButton(1004, "Esc", 0.0F, 0.0F, this.keyWidthSpecial, this.keyHeight)
        {
            @Override
            public void onPressed()
            {
                InputSimulator.pressKey(GLFW_KEY_ESCAPE);
                InputSimulator.releaseKey(GLFW_KEY_ESCAPE);
            }
        });
        this.addKey(new KeyButton(1005, "Bksp", this.keyWidthSpecial + this.spacing + this.columns * (this.keyWidth + this.spacing), 0.0F, this.keyWidthSpecial, this.keyHeight)
        {
            @Override
            public void onPressed()
            {
                InputSimulator.pressKey(GLFW_KEY_BACKSPACE);
                InputSimulator.releaseKey(GLFW_KEY_BACKSPACE);
            }
        });
        this.addKey(new KeyButton(1006, "Enter", this.keyWidthSpecial + this.spacing + this.columns * (this.keyWidth + this.spacing), 2.0F * (this.keyHeight + this.spacing), this.keyWidthSpecial, this.keyHeight)
        {
            @Override
            public void onPressed()
            {
                InputSimulator.pressKey(GLFW_KEY_ENTER);
                InputSimulator.releaseKey(GLFW_KEY_ENTER);
            }
        });
        this.addKey(new KeyButton(1007, "↑", this.keyWidthSpecial + this.spacing + (this.columns + 1) * (this.keyWidth + this.spacing), 4.0F * (this.keyHeight + this.spacing), this.keyWidth, this.keyHeight)
        {
            @Override
            public void onPressed()
            {
                InputSimulator.pressKey(GLFW_KEY_UP);
                InputSimulator.releaseKey(GLFW_KEY_UP);
            }
        });
        this.addKey(new KeyButton(1008, "↓", this.keyWidthSpecial + this.spacing + (this.columns + 1) * (this.keyWidth + this.spacing), 5.0F * (this.keyHeight + this.spacing), this.keyWidth, this.keyHeight)
        {
            @Override
            public void onPressed()
            {
                InputSimulator.pressKey(GLFW_KEY_DOWN);
                InputSimulator.releaseKey(GLFW_KEY_DOWN);
            }
        });
        this.addKey(new KeyButton(1009, "←", this.keyWidthSpecial + this.spacing + this.columns * (this.keyWidth + this.spacing), 5.0F * (this.keyHeight + this.spacing), this.keyWidth, this.keyHeight)
        {
            @Override
            public void onPressed()
            {
                InputSimulator.pressKey(GLFW_KEY_LEFT);
                InputSimulator.releaseKey(GLFW_KEY_LEFT);
            }
        });
        this.addKey(new KeyButton(1010, "→", this.keyWidthSpecial + this.spacing + (this.columns + 2) * (this.keyWidth + this.spacing), 5.0F * (this.keyHeight + this.spacing), this.keyWidth, this.keyHeight)
        {
            @Override
            public void onPressed()
            {
                InputSimulator.pressKey(GLFW_KEY_RIGHT);
                InputSimulator.releaseKey(GLFW_KEY_RIGHT);
            }
        });
        this.addKey(new KeyButton(1011, "Cut", (this.keyWidthSpecial + this.spacing), -1.0F * (this.keyHeight + this.spacing), this.keyWidthSpecial, this.keyHeight)
        {
            @Override
            public void onPressed()
            {
                InputSimulator.pressKey(GLFW_KEY_LEFT_CONTROL);
                InputSimulator.pressKey(GLFW_KEY_X);
                InputSimulator.releaseKey(GLFW_KEY_X);
                InputSimulator.releaseKey(GLFW_KEY_LEFT_CONTROL);
            }
        });
        this.addKey(new KeyButton(1012, "Copy", 2.0F * (this.keyWidthSpecial + this.spacing), -1.0F * (this.keyHeight + this.spacing), this.keyWidthSpecial, this.keyHeight)
        {
            @Override
            public void onPressed()
            {
                InputSimulator.pressKey(GLFW_KEY_LEFT_CONTROL);
                InputSimulator.pressKey(GLFW_KEY_C);
                InputSimulator.releaseKey(GLFW_KEY_C);
                InputSimulator.releaseKey(GLFW_KEY_LEFT_CONTROL);
            }
        });
        this.addKey(new KeyButton(1013, "Paste", 3.0F * (this.keyWidthSpecial + this.spacing), -1.0F * (this.keyHeight + this.spacing), this.keyWidthSpecial, this.keyHeight)
        {
            @Override
            public void onPressed()
            {
                InputSimulator.pressKey(GLFW_KEY_LEFT_CONTROL);
                InputSimulator.pressKey(GLFW_KEY_V);
                InputSimulator.releaseKey(GLFW_KEY_V);
                InputSimulator.releaseKey(GLFW_KEY_LEFT_CONTROL);
            }
        });

        for (int i1 = 0; i1 < 2; ++i1)
        {
            if (this.pressedKey[i1] != null)
            {
                for (KeyButton physicalkeyboard$keybutton1 : this.keys)
                {
                    if (physicalkeyboard$keybutton1.id == this.pressedKey[i1].id)
                    {
                        this.pressedKey[i1] = physicalkeyboard$keybutton1;
                        physicalkeyboard$keybutton1.pressed = true;
                        break;
                    }
                }
            }
        }

        if (dh.vrSettings.physicalKeyboardTheme == KeyboardTheme.CUSTOM) {
            this.customTheme.clear();
            File themeFile = new File(mc.gameDirectory, "keyboardtheme.txt");
            if (!themeFile.exists()) {
                // Write template theme file
                try (PrintWriter pw = new PrintWriter(new FileWriter(themeFile, StandardCharsets.UTF_8))) {
                    char[] normalChars = dh.vrSettings.keyboardKeys.toCharArray();
                    for (int i = 0; i < normalChars.length; i++) {
                        pw.println("# " + normalChars[i] + " (Normal)");
                        pw.println(i + "=255,255,255");
                    }
                    char[] shiftChars = dh.vrSettings.keyboardKeysShift.toCharArray();
                    for (int i = 0; i < shiftChars.length; i++) {
                        pw.println("# " + shiftChars[i] + " (Shifted)");
                        pw.println((i + 500) + "=255,255,255");
                    }
                    this.keys.forEach(button -> {
                        if (button.id >= 1000) {
                            pw.println("# " + button.label);
                            pw.println(button.id + "=255,255,255");
                        }
                    });
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                // Load theme file
                try (Stream<String> lines = Files.lines(Paths.get(themeFile.toURI()), StandardCharsets.UTF_8)) {
                    lines.forEach(line -> {
                        if (line.length() == 0 || line.charAt(0) == '#') return;
                        try {
                            String[] split = line.split("=", 2);
                            int id = Integer.parseInt(split[0]);
                            String[] colorSplit = split[1].split(",");
                            Color color = new Color(Integer.parseInt(colorSplit[0]), Integer.parseInt(colorSplit[1]), Integer.parseInt(colorSplit[2]));
                            this.customTheme.put(id, color);
                        } catch (Exception ex) {
                            logger.error("Bad theme line: {}", line);
                            ex.printStackTrace();
                        }
                    });
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        this.reinit = false;
    }

    public void process()
    {
        if (this.reinit)
        {
            this.init();
        }

        for (int i = 0; i < 2; ++i)
        {
            ControllerType controllertype = ControllerType.values()[i];
            KeyButton physicalkeyboard$keybutton = this.findTouchedKey(controllertype);

            if (physicalkeyboard$keybutton != null)
            {
                if (physicalkeyboard$keybutton != this.pressedKey[i] && milliTime() - this.pressTime[i] >= 150L)
                {
                    if (this.pressedKey[i] != null)
                    {
                        this.pressedKey[i].unpress(controllertype);
                        this.pressedKey[i] = null;
                    }

                    physicalkeyboard$keybutton.press(controllertype, false);
                    this.pressedKey[i] = physicalkeyboard$keybutton;
                    this.pressTime[i] = milliTime();
                    this.pressRepeatTime[i] = milliTime();
                }
                else if (physicalkeyboard$keybutton == this.pressedKey[i] && milliTime() - this.pressTime[i] >= 500L && milliTime() - this.pressRepeatTime[i] >= 100L)
                {
                    physicalkeyboard$keybutton.press(controllertype, true);
                    this.pressRepeatTime[i] = milliTime();
                }
            }
            else if (this.pressedKey[i] != null)
            {
                this.pressedKey[i].unpress(controllertype);
                this.pressedKey[i] = null;
                this.pressTime[i] = milliTime();
            }
        }
    }

    public void processBindings()
    {
        if (GuiHandler.keyKeyboardShift.consumeClick())
        {
            this.setShift(true, true);
            this.lastPressedShift = true;
        }

        if (!GuiHandler.keyKeyboardShift.isDown() && this.lastPressedShift)
        {
            this.setShift(false, false);
            this.lastPressedShift = false;
        }
    }

    private Vector3f getCenterPos()
    {
        return new Vector3f(((this.keyWidth + this.spacing) * ((float)this.columns + (float)this.columns % 2.0F / 2.0F) + (this.keyWidthSpecial + this.spacing) * 2.0F) / 2.0F, (this.keyHeight + this.spacing) * (float)(this.rows + 1), 0.0F);
    }

    private KeyButton findTouchedKey(ControllerType controller)
    {
        Matrix4f matrix4f = new Matrix4f().translate(this.getCenterPos())
            .mul(KeyboardHandler.Rotation_room.invertAffine(new Matrix4f()))
            .translate(convertToVector3f(KeyboardHandler.Pos_room).negate());
        Vector3f vector = convertToVector3f(dh.vrPlayer.vrdata_room_pre.getController(controller.ordinal()).getPosition());
        matrix4f.transformPosition(vector, vector);

        for (KeyButton physicalkeyboard$keybutton : this.keys)
        {
            if (physicalkeyboard$keybutton.getCollisionBoundingBox().contains(vector.x(), vector.y(), vector.z()))
            {
                return physicalkeyboard$keybutton;
            }
        }

        return null;
    }

    private void updateEasterEgg(String label)
    {
        String text = this.easterEggText.get();
        if (this.easterEggIndex < text.length())
        {
            if (label.toLowerCase().equals(String.valueOf(text.charAt(this.easterEggIndex))))
            {
                ++this.easterEggIndex;
            }
            else
            {
                this.easterEggIndex = 0;
            }
        }
        else if ("Enter".equals(label))
        {
            this.easterEggActive = !this.easterEggActive;
        }
        else
        {
            this.easterEggIndex = 0;
        }
    }

    private void drawBox(BufferBuilder buf, AABB box, Color color, PoseStack poseStack)
    {
        Matrix4f matrix = poseStack.last().pose();
        float minX = (float)box.minX, minY = (float)box.minY, minZ = (float)box.minZ;
        float maxX = (float)box.maxX, maxY = (float)box.maxY, maxZ = (float)box.maxZ;
        int r = color.R();
        int g = color.G();
        int b = color.B();
        int a = color.A();
        buf.vertex(matrix, minX, minY, minZ).uv(0, 0).color(r, g, b, a).normal(0.0F, 0.0F, -1.0F).endVertex();
        buf.vertex(matrix, minX, maxY, minZ).uv(0, 0).color(r, g, b, a).normal(0.0F, 0.0F, -1.0F).endVertex();
        buf.vertex(matrix, maxX, maxY, minZ).uv(0, 0).color(r, g, b, a).normal(0.0F, 0.0F, -1.0F).endVertex();
        buf.vertex(matrix, maxX, minY, minZ).uv(0, 0).color(r, g, b, a).normal(0.0F, 0.0F, -1.0F).endVertex();
        buf.vertex(matrix, minX, minY, minZ).uv(0, 0).color(r, g, b, a).normal(0.0F, -1.0F, 0.0F).endVertex();
        buf.vertex(matrix, maxX, minY, minZ).uv(0, 0).color(r, g, b, a).normal(0.0F, -1.0F, 0.0F).endVertex();
        buf.vertex(matrix, maxX, minY, maxZ).uv(0, 0).color(r, g, b, a).normal(0.0F, -1.0F, 0.0F).endVertex();
        buf.vertex(matrix, minX, minY, maxZ).uv(0, 0).color(r, g, b, a).normal(0.0F, -1.0F, 0.0F).endVertex();
        buf.vertex(matrix, minX, minY, minZ).uv(0, 0).color(r, g, b, a).normal(-1.0F, 0.0F, 0.0F).endVertex();
        buf.vertex(matrix, minX, minY, maxZ).uv(0, 0).color(r, g, b, a).normal(-1.0F, 0.0F, 0.0F).endVertex();
        buf.vertex(matrix, minX, maxY, maxZ).uv(0, 0).color(r, g, b, a).normal(-1.0F, 0.0F, 0.0F).endVertex();
        buf.vertex(matrix, minX, maxY, minZ).uv(0, 0).color(r, g, b, a).normal(-1.0F, 0.0F, 0.0F).endVertex();
        buf.vertex(matrix, maxX, maxY, maxZ).uv(0, 0).color(r, g, b, a).normal(0.0F, 0.0F, 1.0F).endVertex();
        buf.vertex(matrix, minX, maxY, maxZ).uv(0, 0).color(r, g, b, a).normal(0.0F, 0.0F, 1.0F).endVertex();
        buf.vertex(matrix, minX, minY, maxZ).uv(0, 0).color(r, g, b, a).normal(0.0F, 0.0F, 1.0F).endVertex();
        buf.vertex(matrix, maxX, minY, maxZ).uv(0, 0).color(r, g, b, a).normal(0.0F, 0.0F, 1.0F).endVertex();
        buf.vertex(matrix, maxX, maxY, maxZ).uv(0, 0).color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
        buf.vertex(matrix, maxX, maxY, minZ).uv(0, 0).color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
        buf.vertex(matrix, minX, maxY, minZ).uv(0, 0).color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
        buf.vertex(matrix, minX, maxY, maxZ).uv(0, 0).color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
        buf.vertex(matrix, maxX, maxY, maxZ).uv(0, 0).color(r, g, b, a).normal(1.0F, 0.0F, 0.0F).endVertex();
        buf.vertex(matrix, maxX, minY, maxZ).uv(0, 0).color(r, g, b, a).normal(1.0F, 0.0F, 0.0F).endVertex();
        buf.vertex(matrix, maxX, minY, minZ).uv(0, 0).color(r, g, b, a).normal(1.0F, 0.0F, 0.0F).endVertex();
        buf.vertex(matrix, maxX, maxY, minZ).uv(0, 0).color(r, g, b, a).normal(1.0F, 0.0F, 0.0F).endVertex();
    }

    public void render(PoseStack poseStack)
    {
        poseStack.pushPose();
        Vector3f center = this.getCenterPos();
        poseStack.last().pose().translate(-center.x, -center.y, -center.z);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();

        if (this.easterEggActive) {
            // https://qimg.techjargaming.com/i/UkG1cWAh.png
            for (KeyButton button : this.keys) {
                button.color.fromHSB(((float)dh.tickCounter + mc.getFrameTime()) / 100.0F + (float)(button.boundingBox.minX + (button.boundingBox.maxX - button.boundingBox.minX) / 2.0D) / 2.0F, 1.0F, 1.0F);
            }
        } else {
            this.keys.forEach(button -> {
                if (dh.vrSettings.physicalKeyboardTheme == KeyboardTheme.CUSTOM) {
                    Color color = this.customTheme.get(this.shift && button.id < 1000 ? button.id + 500 : button.id);
                    if (color != null) {
                        button.color.set(color.r, color.g, color.b);
                    }
                } else {
                    dh.vrSettings.physicalKeyboardTheme.assignColor(button);
                }
            });
        }

        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);

        // TODO: does this still do the right thing for shaders?
        mc.getTextureManager().bindForSetup(new ResourceLocation("vivecraft:textures/white.png"));
        RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));

        // We need to ignore depth so we can see the back faces and text
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);

        // Stuff for drawing labels
        ArrayList<Tuple<String, Vector3f>> labels = new ArrayList<>();
        float textScale = 0.002F * this.scale;

        // Start building vertices for key boxes
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);

        for (KeyButton button : this.keys)
        {
            AABB box = button.getRenderBoundingBox();

            // Draw the key itself
            this.drawBox(bufferbuilder, box, button.getRenderColor(), poseStack);

            // Calculate text position
            float stringWidth = (float)mc.font.width(button.label) * textScale;
            float stringHeight = mc.font.lineHeight * textScale;
            float textX = (float)box.minX + ((float)box.maxX - (float)box.minX) / 2.0F - stringWidth / 2.0F;
            float textY = (float)box.minY + ((float)box.maxY - (float)box.minY) / 2.0F - stringHeight / 2.0F;
            float textZ = (float)box.minZ + ((float)box.maxZ - (float)box.minZ) / 2.0F;

            // Put label in the list
            labels.add(new Tuple<>(button.label, new Vector3f(textX, textY, textZ)));
        }

        // Draw all the key boxes
        tesselator.end();

        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        //GlStateManager._disableLighting();

        // Start building vertices for text
        BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(tesselator.getBuilder());

        // Build all the text
        for (Tuple<String, Vector3f> label : labels)
        {
            Vector3f textPos = label.getB();
            poseStack.pushPose();
            poseStack.last().pose().translate(textPos.x, textPos.y, textPos.z);
            poseStack.scale(textScale, textScale, 1.0F);
            mc.font.drawInBatch(label.getA(), 0.0F, 0.0F, 0xFFFFFFFF, false, poseStack.last().pose(), multibuffersource$buffersource, DisplayMode.NORMAL, 0, 15728880, mc.font.isBidirectional());
            poseStack.popPose();
        }

        // Draw all the labels
        multibuffersource$buffersource.endBatch();
        //GlStateManager._enableLighting();

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        poseStack.popPose();
    }

    public void show()
    {
        if (!this.shiftSticky)
        {
            this.shift = false;
        }

        this.scale = dh.vrSettings.physicalKeyboardScale;
        this.reinit = true;
    }

    private KeyButton addKey(KeyButton key)
    {
        this.keys.add(key);
        return key;
    }

    public boolean isShift()
    {
        return this.shift;
    }

    public boolean isShiftSticky()
    {
        return this.shiftSticky;
    }

    public void setShift(boolean shift, boolean sticky)
    {
        if (shift != this.shift || sticky != this.shiftSticky)
        {
            this.shift = shift;
            this.shiftSticky = shift && sticky;
            this.reinit = true;
        }
    }

    public float getScale()
    {
        return this.scale;
    }

    public void setScale(float scale)
    {
        this.scale = scale;
        this.reinit = true;
    }

    private abstract class KeyButton
    {
        public final int id;
        public final String label;
        public final AABB boundingBox;
        public Color color = new Color(Color.WHITE, (byte) -1);
        public boolean pressed;

        public KeyButton(int id, String label, float x, float y, float width, float height)
        {
            this.id = id;
            this.label = label;
            this.boundingBox = new AABB(x, y, 0.0D, x + width, y + height, 0.028D * PhysicalKeyboard.this.scale);
        }

        public AABB getRenderBoundingBox()
        {
            return this.pressed ? this.boundingBox.move(0.0D, 0.0D, 0.012D * (double)PhysicalKeyboard.this.scale) : this.boundingBox;
        }

        public AABB getCollisionBoundingBox()
        {
            return this.pressed ? this.boundingBox.expandTowards(0.0D, 0.0D, 0.08D) : this.boundingBox;
        }

        public Color getRenderColor()
        {
            Color color = new Color(this.color);

            if (!this.pressed)
            {
                color.r >>= 1;
                color.g >>= 1;
                color.b >>= 1;
            }

            return color;
        }

        public final void press(ControllerType controller, boolean isRepeat)
        {
            if (!isRepeat)
            {
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }

            dh.vr.triggerHapticPulse(controller, isRepeat ? 300 : 600);
            this.pressed = true;
            this.onPressed();
            PhysicalKeyboard.this.updateEasterEgg(this.label);
        }

        public final void unpress(ControllerType controller)
        {
            this.pressed = false;
        }

        public abstract void onPressed();
    }

    public enum KeyboardTheme implements OptionEnum<KeyboardTheme> {
        DEFAULT {
            @Override
            public void assignColor(KeyButton button) {
                button.color.set(Color.WHITE);
            }
        },
        RED {
            @Override
            public void assignColor(KeyButton button) {
                button.color.set(Color.RED);
            }
        },
        GREEN {
            @Override
            public void assignColor(KeyButton button) {
                button.color.set(Color.GREEN);
            }
        },
        BLUE {
            @Override
            public void assignColor(KeyButton button) {
                button.color.set(Color.BLUE);
            }
        },
        BLACK {
            @Override
            public void assignColor(KeyButton button) {
                button.color.set(Color.BLACK);
            }
        },
        GRASS {
            @Override
            public void assignColor(KeyButton button) {
                if (button.boundingBox.maxY < 0.07D) {
                    // button.color.set(0.321F, 0.584F, 0.184F);
                    button.color.set((byte)0xD1, (byte)0x14, (byte)0xAE);
                } else {
                    // button.color.set(0.607F, 0.462F, 0.325F);
                    button.color.set((byte)0x1A, (byte)0xF5, (byte)0xD2);
                }
            }
        },
        BEES {
            @Override
            public void assignColor(KeyButton button) {
                if (button.boundingBox.maxX % 0.2D < 0.1D){
                    button.color.r(Byte.MAX_VALUE);
                    button.color.g(Byte.MAX_VALUE);
                }
                else
                {
                    button.color.r(Byte.MIN_VALUE);
                    button.color.g(Byte.MIN_VALUE);
                }
                button.color.b(Byte.MIN_VALUE);
            }
        },
        AESTHETIC {
            @Override
            public void assignColor(KeyButton button) {
                if (button.id >= 1000) {
                    button.color.set(Color.CYAN);
                } else {
                    button.color.set(Color.MAGENTA);
                }
            }
        },
        DOSE {
            @Override
            public void assignColor(KeyButton button) {
                if (button.id % 2 == 0)
                {
                    button.color.set((byte) -1, Byte.MIN_VALUE, Byte.MAX_VALUE);
                }
                else
                {
                    button.color.set(Byte.MIN_VALUE, Byte.MAX_VALUE, Byte.MIN_VALUE);
                }
            }
        },
        CUSTOM {
            @Override
            public void assignColor(KeyButton button) {
                // Handled elsewhere
            }
        };

        public abstract void assignColor(KeyButton button);
    }
}
