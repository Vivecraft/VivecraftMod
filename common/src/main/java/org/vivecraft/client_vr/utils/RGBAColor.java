package org.vivecraft.client_vr.utils;

// Pinched from GlStateManager and additional functionality added
public class RGBAColor {
    public float r;
    public float g;
    public float b;
    public float a;

    public RGBAColor() {
        this(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public RGBAColor(float red, float green, float blue, float alpha) {
        this.r = red;
        this.g = green;
        this.b = blue;
        this.a = alpha;
    }

    public RGBAColor(int red, int green, int blue, int alpha) {
        this.r = red / 255.0F;
        this.g = green / 255.0F;
        this.b = blue / 255.0F;
        this.a = alpha / 255.0F;
    }

    public RGBAColor(int value) {
        this(value & 0xFF, (value >> 8) & 0xFF, (value >> 16) & 0xFF, (value >> 24) & 0xFF);
    }

    public int toIntEncoding() {
        return (int) (this.r * 255.0F) | ((int) (this.g * 255.0F) << 8) | ((int) (this.b * 255.0F) << 16) | ((int) (this.a * 255.0F) << 24);
    }

    public RGBAColor copy() {
        return new RGBAColor(this.r, this.g, this.b, this.a);
    }

    public static RGBAColor fromHSB(float hue, float saturation, float brightness) {
        RGBAColor color = new RGBAColor();

        if (saturation == 0.0F) {
            color.r = color.g = color.b = brightness;
        } else {
            float f = (hue - (float) Math.floor(hue)) * 6.0F;
            float f1 = f - (float) Math.floor(f);
            float f2 = brightness * (1.0F - saturation);
            float f3 = brightness * (1.0F - saturation * f1);
            float f4 = brightness * (1.0F - saturation * (1.0F - f1));

            switch ((int) f) {
                case 0 -> {
                    color.r = brightness;
                    color.g = f4;
                    color.b = f2;
                }
                case 1 -> {
                    color.r = f3;
                    color.g = brightness;
                    color.b = f2;
                }
                case 2 -> {
                    color.r = f2;
                    color.g = brightness;
                    color.b = f4;
                }
                case 3 -> {
                    color.r = f2;
                    color.g = f3;
                    color.b = brightness;
                }
                case 4 -> {
                    color.r = f4;
                    color.g = f2;
                    color.b = brightness;
                }
                case 5 -> {
                    color.r = brightness;
                    color.g = f2;
                    color.b = f3;
                }
            }
        }

        return color;
    }
}
