package org.vivecraft.common.utils.color;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

import static org.joml.Math.*;

// TODO: further classes
//  ColorB (byte accurate),
//  ColorI (integer accurate),
//  ColorL (long accurate),
//  ColorF (float accurate),
//  ColorD (double accurate),
//  ColorA (Big/arbitrarily accurate),
//  ColorS (static/super accurate)
//  to indicate the programmer's desired accuracy.
// TODO: cache conversions
// TODO: color space conformity

/**
 * <p>
 * This {@link Color} class uses four {@code byte} values to express an RGBA (Red, Green, Blue, Alpha/Opacity) color.
 * <br>
 * However, {@code byte}, {@code int}, and {@code float} are all signed values.
 * <br>
 * To map RGBA color channels effectively to each primitive's range some definitions must be outlined.
 * </p>
 * <p>
 * Definitions:
 * <pre>
 *     color {@code byte}: {@link Byte#MIN_VALUE} to {@link Byte#MAX_VALUE} | {@code (byte) -128} to {@code (byte) 127} | {@code (byte) 0x80} to {@code (byte) 0x7F} | {@code (byte) 0b10000000} to {@code (byte) 0b01111111}
 *     color {@code int}: {@code 0} to {@code 255} | {@code 0x00} to {@code 0xFF}
 *     color {@code float}: {@code 0} to {@code 1} | {@code 0.0F} to {@code 1.0F}
 *     halves:
 *         {@code byte}: {@code (byte) -1 | (byte) 0xFF}
 *         {@code int}: {@code 127} | {@code 0x7F} | {@code 0b01111111}
 *         {@code float}: {@code 0.5F}
 * </pre>
 * </p>
 * <p>
 * It is <strong>strongly recommended</strong> to use the {@code byte} expression of RGBA channels when constructing and/or statically assigning.
 * <br>
 * Using a {@code (byte)} explicitly in your constructors allows the code to express the exact value intended for each channel without ambiguity.
 * <br>
 * However, other operations may be easier to understand when implemented using percent {@code float} scaling or literal {@code int} math.
 * <br>
 * Especially when importing and exporting results to and from packages which may only handle non-{@code byte} primatives, using the complex constructors and methods is reasonable.
 * <br>
 * Similarly, values from color pickers often provide <u>positive</u> decimal integer and <u>unsigned</u> hexadecimal expressions of RGB( rarely A ).
 * <br>
 * Remember, the java {@code byte} uses <u>signed</u> notation.
 * <br>
 * To write a signed {@code byte} using the color provided from an unsigned color picker, do the following:
 * <ul>
 *     <li>
 *         Subtract {@code 128} from each of its decimal channels {@code ||} Subtract {@code 0x80} from each of its hexadecimal channels
 *         <ul>
 *             <li>
 *                 Note: This applies to all of {@link Color#r}, {@link Color#g}, {@link Color#b}, <b>and {@link Color#a}</b>.
 *             </li>
 *         </ul>
 *     </li>
 *     <li>
 *         Use the result of those subtractions as the inputs to any of this Class' {@code byte} constructors.
 *     </li>
 * </ul>
 * </p>
 * <p>
 * Ex.
 * <pre>
 *     {@code
 *     // int constructors
 *     Color cyan_from_positive_decimal_int = new Color(0, 255, 255);
 *     Color cyan_from_hexadecimal_int = new Color(0x00, 0xFF, 0xFF);
 *     // byte constructors
 *     Color cyan_from_signed_decimal_byte = new Color((byte) -128, (byte) 127, (byte) 127);
 *     Color cyan_from_hexadecimal_byte = new Color((byte) 0x80, (byte) 0x7F, (byte) 0x7F);
 *     Color cyan_from_Byte_statics = new Color(Byte.MIN_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE);
 *     // int constructors
 *     Color magenta_from_positive_decimal_int = new Color(255, 0, 255);
 *     Color magenta_from_hexadecimal_int = new Color(0xFF, 0x00, 0xFF);
 *     // byte constructors
 *     Color magenta_from_signed_decimal_byte = new Color((byte) 127, (byte) -128, (byte) 127);
 *     Color magenta_from_hexadecimal_byte = new Color((byte) 0x7F, (byte) 0x80, (byte) 0x7F);
 *     Color magenta_from_Byte_statics = new Color(Byte.MAX_VALUE, Byte.MIN_VALUE, Byte.MAX_VALUE);
 *     System.out.printf(
 *         "Cyan Color Equality: %b\n",
 *         cyan_from_decimal_int == cyan_from_hexadecimal_int == cyan_from_decimal_byte == cyan_from_hexadecimal_byte == cyan_from_Byte_statics
 *     );
 *     System.out.println(
 *         "Magenta Color Equality: %b",
 *         magenta_from_decimal_int == magenta_from_hexadecimal_int == magenta_from_decimal_byte == magenta_from_hexadecimal_byte == magenta_from_Byte_statics
 *     );}
 * <samp>Cyan Color Equality: true
 * Magenta Color Equality: true</samp>
 * </pre>
 * </p>
 * @implNote
 * This class' conversions favor the simplest methods.
 * <br>
 * Floating point conversion is not exactly accurate with this class.
 * <br>
 * This intentionally incurred lack of precision is due to only using 256 values for each channel at the {@code int} and {@code byte} level.
 * <br>
 * Ex.
 * <pre>
 * <samp>input: 0.785
 * toInt: 200
 * toByte: 0x48
 * back toInt: 200
 * back toFloat: 0.784314
 * </samp></pre>
 * @apiNote
 * This class intentionally shadows {@link java.awt.Color} to prevent it from being used.
 * <br>
 * Using {@link java.awt.Color} or any of {@link java.awt}, may crash {@link org.lwjgl} on MacOS.
 */
@ParametersAreNonnullByDefault
public class Color
{
	/**
	 * The red channel as a color {@code byte}.
	 * @see Color
	 */
	public byte r = Byte.MAX_VALUE;

	/**
	 * The green channel as a color {@code byte}.
	 * @see Color
	 */
	public byte g = Byte.MAX_VALUE;

	/**
	 * The blue channel as a color {@code byte}.
	 * @see Color
	 */
	public byte b = Byte.MAX_VALUE;

	/**
	 * The alpha channel as a color {@code byte}.
	 * @see Color
	 */
	public byte a = Byte.MAX_VALUE;

	/**
	 * A Color containing {@link Byte#MAX_VALUE} in {@link Color#r}, {@link Color#g}, {@link Color#b}, and {@link Color#a}.
	 */
	public static final Color WHITE = new Color();

	/**
	 * A Color containing {@link Byte#MIN_VALUE} in {@link Color#r}, {@link Color#g}, and {@link Color#b} and {@link Byte#MAX_VALUE} in {@link Color#a}.
	 */
	public static final Color BLACK = new Color(Byte.MIN_VALUE, Byte.MAX_VALUE);

	/**
	 * A Color containing {@link Byte#MIN_VALUE} in {@link Color#r}, {@link Color#g}, {@link Color#b}, and {@link Color#a}.
	 * <br>
	 * Alias for transparent black.
	 */
	public static final Color OFF = new Color(Byte.MIN_VALUE);

	/**
	 * A Color containing {@link Byte#MIN_VALUE} in {@link Color#g}, and {@link Color#b} and {@link Byte#MAX_VALUE} in {@link Color#r}, and {@link Color#a}.
	 */
	public static final Color RED = new Color(Byte.MAX_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE);

	/**
	 * A Color containing {@link Byte#MIN_VALUE} in {@link Color#r}, and {@link Color#b} and {@link Byte#MAX_VALUE} in {@link Color#g}, and {@link Color#a}.
	 */
	public static final Color GREEN = new Color(Byte.MIN_VALUE, Byte.MAX_VALUE, Byte.MIN_VALUE);

	/**
	 * A Color containing {@link Byte#MIN_VALUE} in {@link Color#r}, and {@link Color#g} and {@link Byte#MAX_VALUE} in {@link Color#b}, and {@link Color#a}.
	 */
	public static final Color BLUE = new Color(Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MAX_VALUE);

	/**
	 * A Color containing {@link Byte#MIN_VALUE} in {@link Color#r}, and {@link Color#g} and {@link Byte#MAX_VALUE} in {@link Color#b}, and {@link Color#a}.
	 */
	public static final Color YELLOW = new Color(Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MIN_VALUE);

	/**
	 * A Color containing {@link Byte#MIN_VALUE} in {@link Color#r} and {@link Byte#MAX_VALUE} in {@link Color#g}, {@link Color#b}, and {@link Color#a}.
	 */
	public static final Color CYAN = new Color(Byte.MIN_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE);

	/**
	 * A Color containing {@link Byte#MIN_VALUE} in {@link Color#g} and {@link Byte#MAX_VALUE} in {@link Color#r}, {@link Color#b}, and {@link Color#a}.
	 */
	public static final Color MAGENTA = new Color(Byte.MAX_VALUE, Byte.MIN_VALUE, Byte.MAX_VALUE);

	/**
	 * Construct an opaque white {@link Color}.
	 * <br>
	 * All channels default to {@link Byte#MAX_VALUE}.
	 */
	public Color(){}

	/**
	 * Constructs a new Color with the same channel values as {@code color}.
	 * @param color the Color to copy.
	 */
	public Color(Color color) {
		this.set(color);
	}

	/**
	 * Construct a new {@link Color} with the same color channel values as {@code color}
	 * @param color the {@link Color} to copy.
	 * @param alpha the byte to assign {@link Color#a} to.
	 */
	public Color(Color color, byte alpha)
	{
		this.set(color, alpha);
	}

	/**
	 * Construct a new {@link Color} with the same color channel values as {@code color}
	 * @param color the {@link Color} to copy.
	 * @param alpha the integer value to convert and assign {@link Color#a} to.
	 */
	public Color(Color color, int alpha)
	{
		this.set(color, alpha);
	}

	/**
	 * Construct a new {@link Color} with the same color channel values as {@code color}
	 * @param color the {@link Color} to copy.
	 * @param alpha the percent to convert and assign {@link Color#a} to.
	 */
	public Color(Color color, float alpha)
	{
		this.set(color, alpha);
	}

	/**
	 * Construct a new Color by converting distinct integer values for each channel.
	 * @param red the integer value to assign {@link Color#r} to.
	 * @param green the integer value to assign {@link Color#g} to.
	 * @param blue the integer value to assign {@link Color#b} to.
	 * @param alpha the integer value to assign {@link Color#a} to.
	 */
	public Color(int red, int green, int blue, int alpha){
		this.set(red, green, blue, alpha);
	}

	/**
	 * Construct a new Color by converting distinct integer values for each color channel.
	 * @param red the integer value to assign {@link Color#r} to.
	 * @param green the integer value to assign {@link Color#g} to.
	 * @param blue the integer value to assign {@link Color#b} to.
	 */
	public Color(int red, int green, int blue)
	{
		this.set(red, green, blue);
	}

	/**
	 * Construct a new Color by converting an integer to reuse for all color channels, and a distinct integer value for {@code alpha}.
	 * @param rgb the integer value to convert and assign {@link Color#r}, {@link Color#g}, and {@link Color#b} to.
	 * @param alpha the integer value to convert and assign {@link Color#a} to.
	 */
	public Color(int rgb, int alpha)
	{
		this.set(rgb, alpha);
	}

	/**
	 * Construct a new Color by converting a distinct percent {@code float} for each channel.
	 * @param red the percent value to convert and assign {@link Color#r} to.
	 * @param green the percent value to convert and assign {@link Color#g} to.
	 * @param blue the percent value to convert and assign {@link Color#b} to.
	 * @param alpha the integer value to convert and assign {@link Color#a} to.
	 */
	public Color(float red, float green, float blue, float alpha)
	{
		this.set(red, green, blue, alpha);
	}

	/**
	 * Construct a new Color by converting a distinct percent {@code float} for each color channel.
	 * @param red the percent value to convert and assign {@link Color#r} to.
	 * @param green the percent value to convert and assign {@link Color#g} to.
	 * @param blue the percent value to convert and assign {@link Color#b} to.
	 */
	public Color(float red, float green, float blue)
	{
		this.set(red, green, blue);
	}

	/**
	 * Construct a Color using a distinct byte for each channel.
	 * @param red {@link Color#r}
	 * @param green {@link Color#g}
	 * @param blue {@link Color#b}
	 * @param alpha {@link Color#a}
	 */
	public Color(byte red, byte green, byte blue, byte alpha)
	{
		this.set(red, green, blue, alpha);
	}

	/**
	 * Construct an opaque Color using a distinct byte for each color channel.
	 * @param red {@link Color#r}
	 * @param green {@link Color#g}
	 * @param blue {@link Color#b}
	 */
	public Color(byte red, byte green, byte blue)
	{
		this.set(red, green, blue, Byte.MAX_VALUE);
	}

	/**
	 * Construct a new Color by reusing a single {@code byte} for all color channels, and a distinct integer value for {@code alpha}.
	 * @param rgb the {@code byte} to assign {@link Color#r}, {@link Color#g}, and {@link Color#b} to.
	 * @param alpha the {@code byte} to assign {@link Color#a} to.
	 */
	public Color(byte rgb, byte alpha)
	{
		this.set(rgb, alpha);
	}

	/**
	 * Constructs a new Color from an argb encoded {@code int}.
	 * <br>
	 * <pre>
	 *     Ex.
	 *         {@code Color transparent_white = new Color((byte) 0x00FFFFFF);}
	 *         {@code Color opaque_black = new Color((byte) 0xFF000000);}
	 *         {@code Color opaque_lime = new Color((byte) 0xFF00FF00);}
	 * </pre>
	 * @param color the int to decode
	 */
	public Color(int color) {
		this.fromARGB(color);
	}

	/**
	 * Get {@link Color#r}.
	 * @return {@link Color#r}
	 */
	public byte r()
	{
		return this.r;
	}

	/**
	 * Set {@link Color#r}.
	 * @param red the byte to assign {@link Color#r} to.
	 * @return this
	 */
	public Color r(byte red)
	{
		this.r = red;
		return this;
	}

	/**
	 * Get {@link Color#r} as a decimal integer.
	 * @return {@link Color#r} converted to a decimal integer ({@code 0} to {@code 255})
	 */
	public int R()
	{
		return toInt(this.r);
	}

	/**
	 * Set {@link Color#r} by converting a decimal integer ({@code 0} to {@code 255}).
	 * @param red the decimal value ({@code 0} to {@code 255}) to assign {@link Color#r} to.
	 * @return this
	 */
	public Color R(int red)
	{
		return this.r(toByte(red));
	}

	/**
	 * Set {@link Color#r} by converting a percent.
	 * @param red the percent ({@code 0.0F} to {@code 1.0F}) to assign {@link Color#r} to.
	 * @return this
	 */
	public Color setR(float red)
	{
		return this.R(toInt(red));
	}

	/**
	 * Get {@link Color#r} as a percent.
	 * @return {@link Color#r} converted to a percent ({@code 0.0F} to {@code 1.0F})
	 */
	public float getR()
	{
		return toFloat(this.r);
	}


	/**
	 * Get {@link Color#g}.
	 * @return {@link Color#g}
	 */
	public byte g()
	{
		return this.g;
	}


	/**
	 * Set {@link Color#g}.
	 * @param green the byte to assign {@link Color#g} to.
	 * @return this
	 */
	public Color g(byte green)
	{
		this.g = green;
		return this;
	}

	/**
	 * Get {@link Color#g} as a decimal integer.
	 * @return {@link Color#g} converted to a decimal integer ({@code 0} to {@code 255})
	 */
	public int G()
	{
		return toInt(this.g);
	}

	/**
	 * Set {@link Color#g} by converting a decimal integer ({@code 0} to {@code 255}).
	 * @param green the decimal value ({@code 0} to {@code 255}) to assign {@link Color#g} to.
	 * @return this
	 */
	public Color G(int green)
	{
		return this.g(toByte(green));
	}

	/**
	 * Set {@link Color#g} by converting a percent ({@code 0.0F} to {@code 1.0F}).
	 * @param green the percent ({@code 0.0F} to {@code 1.0F}) to assign {@link Color#g} to.
	 * @return this
	 */
	public Color setG(float green)
	{
		return this.G(toInt(green));
	}

	/**
	 * Get {@link Color#g} as a percent ({@code 0.0F} to {@code 1.0F}).
	 * @return {@link Color#g} converted to a percent ({@code 0.0F} to {@code 1.0F})
	 */
	public float getG()
	{
		return toFloat(g);
	}

	/**
	 * Get the blue channel byte.
	 * @return {@link Color#b}
	 */
	public byte b()
	{
		return this.b;
	}

	/**
	 * Set {@link Color#b}.
	 * @param blue the byte to set {@link Color#b} to.
	 * @return this
	 */
	public Color b(byte blue)
	{
		this.b = blue;
		return this;
	}

	/**
	 * Set {@link Color#b} by converting a decimal ({@code 0} to {@code 255}).
	 * @param blue the decimal value ({@code 0} to {@code 255}) to assign {@link Color#b} to.
	 * @return this
	 */
	public Color B(int blue)
	{
		return this.b(toByte(blue));
	}

	/**
	 * Get {@link Color#b} as a decimal integer ({@code 0} to {@code 255}).
	 * @return {@link Color#b} converted to a decimal integer ({@code 0} to {@code 255})
	 */
	public int B()
	{
		return toInt(this.b);
	}

	/**
	 * Set {@link Color#b} by converting a percent ({@code 0.0F} to {@code 1.0F}).
	 * @param blue the percent ({@code 0.0F} to {@code 1.0F}) to assign {@link Color#b} to.
	 * @return this
	 */
	public Color setB(float blue)
	{
		return this.B(toInt(blue));
	}

	/**
	 * Get {@link Color#b} as a percent ({@code 0.0F} to {@code 1.0F}).
	 * @return {@link Color#b} converted to a percent ({@code 0.0F} to {@code 1.0F})
	 */
	public float getB()
	{
		return toFloat(b);
	}

	/**
	 * Get {@link Color#a}.
	 * @return {@link Color#a}
	 */
	public byte a()
	{
		return this.a;
	}

	/**
	 *
	 * @param alpha the byte to assign {@link Color#a} to.
	 * @return this
	 */
	public Color a(byte alpha)
	{
		this.a = alpha;
		return this;
	}

	/**
	 * Get {@link Color#a} as a decimal integer ({@code 0} to {@code 255}).
	 * @return {@link Color#a} converted to a decimal integer ({@code 0} to {@code 255})
	 */
	public int A()
	{
		return toInt(this.a);
	}

	/**
	 * Set {@link Color#a} by converting a decimal ({@code 0} to {@code 255}).
	 * @param alpha the decimal value ({@code 0} to {@code 255}) to assign {@link Color#a} to.
	 * @return this
	 */
	public Color A(int alpha)
	{
		return this.a(toByte(alpha));
	}

	/**
	 * Set {@link Color#a} by converting a percent ({@code 0.0F} to {@code 1.0F}).
	 * @param alpha the percent ({@code 0.0F} to {@code 1.0F}) to assign {@link Color#a} to.
	 * @return this
	 */
	public Color setA(float alpha)
	{
		return this.A(toInt(alpha));
	}

	/**
	 * Get {@link Color#a} as a percent ({@code 0.0F} to {@code 1.0F}).
	 * @return {@link Color#a} converted to a percent ({@code 0.0F} to {@code 1.0F}).
	 */
	public float getA()
	{
		return toFloat(a);
	}

	/**
	 * Set all channels of this {@link Color} to the same {@code byte} values as {@code color}.
	 * @param color the {@link Color} to copy.
	 * @return this
	 */
	public Color set(Color color){
		return this.set(color.r(), color.g(), color.b(), color.a());
	}

	/**
	 * Set all the color channels of {@code this} {@link Color} to the same byte values as {@code color}, and {@link Color#a} to {@code alpha}.
	 * @param color the {@link Color} to copy.
	 * @param alpha the byte to assign {@link Color#a} to.
	 * @return this
	 */
	public Color set(Color color, byte alpha){
		return this.set(color.r, color.g, color.b, alpha);
	}

	/**
	 * Set all the color channels of {@code this} {@link Color} to the same byte values as {@code color}, and {@link Color#a} to {@code alpha}.
	 * @param color the {@link Color} to copy.
	 * @param alpha the integer value to assign {@link Color#a} to.
	 * @return this
	 */
	public Color set(Color color, int alpha){
		return this.set(color.r, color.g, color.b, alpha);
	}

	/**
	 * Set all the color channels of {@code this} {@link Color} to the same byte values as {@code color}, and {@link Color#a} to {@code alpha}.
	 * @param color the {@link Color} to copy.
	 * @param alpha the byte to assign {@link Color#a} to.
	 * @return this
	 */
	public Color set(Color color, float alpha){
		return this.set(color.r, color.g, color.b, alpha);
	}

	public Color set(int red, int green, int blue, int alpha)
	{
		this.R(red);
		this.G(green);
		this.B(blue);
		this.A(alpha);
		return this;
	}

	/**
	 * Set all the color channels of {@code this} {@link Color} by converting distinct integer values.
	 * @param red the integer value to assign {@link Color#r} to.
	 * @param green the integer value to assign {@link Color#g} to.
	 * @param blue the integer value to assign {@link Color#b} to.
	 * @return this
	 */
	public Color set(int red, int green, int blue)
	{
		this.R(red);
		this.G(green);
		this.B(blue);
		return this;
	}

	public Color set(int rgb, int a)
	{
		return this.set(toByte(rgb), toByte(a));
	}

	public Color set(float red, float green, float blue, float alpha)
	{
		this.setR(red);
		this.setG(green);
		this.setB(blue);
		this.setA(alpha);
		return this;
	}

	public Color set(float red, float green, float blue)
	{
		this.setR(red);
		this.setG(green);
		this.setB(blue);
		return this;
	}

	public Color set(float rgb, float alpha)
	{
		return this.set(toInt(rgb), toInt(alpha));
	}

	/**
	 * Set all the channels to distinct bytes.
	 * @param red r
	 * @param green g
	 * @param blue b
	 * @param alpha a
	 * @return this
	 */
	public Color set(byte red, byte green, byte blue, byte alpha){
		this.r(red);
		this.g(green);
		this.b(blue);
		this.a(alpha);
		return this;
	}

	/**
	 * Set the color channels to distinct {@code byte} values.
	 * @param red r
	 * @param green g
	 * @param blue b
	 * @return this
	 */
	public Color set(byte red, byte green, byte blue)
	{
		this.r(red);
		this.g(green);
		this.b(blue);
		return this;
	}

	/**
	 * Set the color channels to the same {@code byte} value and alpha to a distinct byte value.
	 * @param rgb r, g, b
	 * @param alpha a
	 * @return this
	 */
	public Color set(byte rgb, byte alpha)
	{
		return this.set(rgb, rgb, rgb, alpha);
	}

	/**
	 * Set all the channels to the same {@code byte} value.
	 * @param rgba r, g, b, a
	 * @return this
	 */
	public Color set(byte rgba)
	{
		return this.set(rgba, rgba, rgba, rgba);
	}

	/**
	 * Set all the channels using an ARGB encoded int.
	 * <br>
	 * Like {@code 0xAARRGGBB}
	 * <br>
	 * <pre>
	 * Ex.
	 *     {@code
	 *     int ARGB = 0xFFAABBCC;
	 *     System.out.println(new Color(ARGB) == new Color((byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xFF));
	 *     }
	 *     <samp>true</samp>
	 * </pre>
	 * @see Color#toARGB
	 * @param color the int to decode
	 * @return this
	 */
	public Color fromARGB(int color)
	{
		return this.set(
			(byte)(color >> 16),
			(byte)(color >> 8),
			(byte)color,
			(byte)(color >> 24)
		);
	}

	/**
	 * Set all the channels using a BGRA encoded int.
	 * <br>
	 * Like {@code 0xBBGGRRAA}
	 * <br>
	 * <pre>
	 * Ex.
	 *     {@code
	 *     int ARGB = 0xFFAABBCC;
	 *     System.out.println(new Color(BGRA) == new Color((byte) 0xBB, (byte) 0xAA, (byte) 0xFF, (byte) 0xCC));
	 *     }
	 *     <samp>true</samp>
	 * </pre>
	 * @see Color#fromRGBA
	 * @param color the int to decode
	 * @return this
	 */
	public Color fromBGRA(int color)
	{
		return this.set(
			(byte)(color >> 16),
			(byte)(color >> 8),
			(byte)(color >> 24),
			(byte)color
		);
	}

	/**
	 * Set all the channels using an RGBA encoded int.
	 * <br>
	 * Like {@code 0xRRGGBBAA}
	 * <br>
	 * <pre>
	 * Ex.
	 *     {@code
	 *     int RGBA = 0xAABBCCFF;
	 *     System.out.println(new Color().fromRGBA(RGBA) == new Color((byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xFF));
	 *     }
	 *     <samp>true</samp>
	 * </pre>
	 * @see Color#fromARGB
	 * @param color the int to decode
	 * @return this
	 */
	public Color fromRGBA(int color)
	{
		return this.set(
			(byte)(color >> 24),
			(byte)(color >> 16),
			(byte)(color >> 8),
			(byte)color
		);
	}

	/**
	 * Set all the channels using an ABGR encoded int.
	 * <br>
	 * Like {@code 0xAABBGGRR}
	 * <br>
	 * <pre>
	 * Ex.
	 *     {@code
	 *     int ABGR = 0xFFAABBCC;
	 *     System.out.println(new Color(ABGR) == new Color((byte) 0xCC, (byte) 0xBB, (byte) 0xAA, (byte) 0xFF));
	 *     }
	 *     <samp>true</samp>
	 * </pre>
	 * @see Color#toARGB
	 * @param color the int to decode
	 * @return this
	 */
	public Color fromABGR(int color)
	{
		return this.set(
			(byte)color,
			(byte)(color >> 8),
			(byte)(color >> 16),
			(byte)(color >> 24)
		);
	}

	/**
	 * Encodes this color as an {@code 0xRRGGBBAA} int.
	 * @see Color#fromRGBA
	 * @return this color as an {@code 0xRRGGBBAA} int.
	 */
	public int toRGBA() {
		return (((int)this.r) << 24) | (((int)this.g) << 16) | (((int)this.b) << 8) | ((int)this.a);
	}

	/**
	 * Encodes this color as an {@code 0xBBGGRRAA} int.
	 * @see Color#fromBGRA
	 * @return this color as an {@code 0xBBGGRRAA} int.
	 */
	public int toBGRA() {
		return (((int)this.b) << 24) | (((int)this.g) << 16) | (((int)this.r) << 8) | ((int)this.a);
	}

	/**
	 * Encodes this color as an {@code 0xAARRGGBB} int.
	 * @see Color#fromRGBA
	 * @return this color as an {@code 0xAARRGGBB} int.
	 */
	public int toARGB() {
		return (((int)this.a) << 24) | (((int)this.r) << 16) | (((int)this.g) << 8) | ((int)this.b);
	}

	/**
	 * Encodes this color as an {@code 0xAABBGGRR} int.
	 * @see Color#fromABGR
	 * @return this color as an {@code 0xAABBGGRR} int.
	 */
	public int toABGR(){
		return (((int)this.a) << 24) | (((int)this.b) << 16) | (((int)this.g) << 8) | ((int)this.r);
	}

	/**
	 * Set all the channels using HSB floats.
	 * @param hue
	 * @param saturation
	 * @param brightness
	 * @return this
	 */
	public Color fromHSB(float hue, float saturation, float brightness) {
		byte byteness = toByte(brightness);

		if (saturation == 0.0F) {
			this.r = this.g = this.b = byteness;
		} else {
			float f = (hue - floor(hue)) * 6.0F;
			float f1 = f - floor(f);
			byte f2 = toByte(brightness * (1.0F - saturation));
			byte f3 = toByte(brightness * (1.0F - saturation * f1));
			byte f4 = toByte(brightness * (1.0F - saturation * (1.0F - f1)));

			switch ((int)f) {
				case 0 -> {
					this.r = byteness;
					this.g = f4;
					this.b = f2;
				}
				case 1 -> {
					this.r = f3;
					this.g = byteness;
					this.b = f2;
				}
				case 2 -> {
					this.r = f2;
					this.g = byteness;
					this.b = f4;
				}
				case 3 -> {
					this.r = f2;
					this.g = f3;
					this.b = byteness;
				}
				case 4 -> {
					this.r = f4;
					this.g = f2;
					this.b = byteness;
				}
				case 5 -> {
					this.r = byteness;
					this.g = f2;
					this.b = f3;
				}
			}
		}

		return this;
	}

	// TODO: should something else clamp for the programmer? constructors/methods?

	/**
	 * Convert a {@code float} containing the percent ({@code 0.0F} to {@code 1.0F}) of a channel's strength.
	 * @apiNote This function will <strong>NOT</strong> {@link org.joml.Math#clamp} to the valid {@code float} range.
	 * <br>
	 * This color class is not a replacement to comprehensive math libraries like {@link java.lang.Math} or {@link org.joml}.
	 * @param percent channel strength in percent ({@code 0.0F} to {@code 1.0F})
	 * @return byte representation of {@code percent}
	 */
	public static byte toByte(float percent)
	{
		return toByte(toInt(percent));
	}

	/**
	 * Convert an {@code int} containing the decimal ({@code 0} to {@code 255}) representation of a channel.
	 * @apiNote This function will <strong>NOT</strong> {@link org.joml.Math#clamp} to the valid {@code integer} range.
	 * <br>
	 * This color class is not a replacement to comprehensive math libraries like {@link java.lang.Math} or {@link org.joml}.
	 * @param decimal channel value in decimal ({@code 0} to {@code 255})
	 * @return byte representation of {@code decimal}
	 */
	public static byte toByte(int decimal)
	{
		return (byte)(decimal - 128);
	}

	/**
	 * Convert a channel {@code byte} to a decimal {@code int} ({@code 0} to {@code 255}).
	 * @param channel the channel to convert to an {@code int}
	 * @return decimal representation {@code channel}
	 */
	public static int toInt(byte channel)
	{
		return channel + 128;
	}

	/**
	 * Convert a percent {@code float} ({@code 0.0F}, {@code 1.0F}) to a decimal {@code int} ({@code 0}, {@code 255}).
	 * @apiNote This function will <strong>NOT</strong> {@link org.joml.Math#clamp} to the valid {@code percent} range.
	 * <br>
	 * This color class is not a replacement to comprehensive math libraries like {@link java.lang.Math} or {@link org.joml}.
	 * @param percent the percent to convert to a decimal
	 * @return the decimal representation of percent
	 */
	public static int toInt(float percent)
	{
		return (int)(percent * 255);
	}

	/**
	 * Convert an {@code int} ({@code 0} to {@code 255}) to a percent {@code float} ({@code 0.0F} to {@code 1.0F}).
	 * @apiNote This function will <strong>NOT</strong> {@link org.joml.Math#clamp} to the valid {@code integer} range.
	 * <br>
	 * This color class is not a replacement to comprehensive math libraries like {@link java.lang.Math} or {@link org.joml}.
	 * @param integer the integer to convert to a percent {@code float}
	 * @return the percent representation of {@code integer}
	 */
	public static float toFloat(int integer)
	{
		return integer / 255.0F;
	}

	/**
	 * Convert a channel {@code byte} to a percent {@code float} ({@code 0.0F} to {@code 1.0F}).
	 * @param channel the channel to convert to a percent {@code float}
	 * @return the percent representation of {@code channel}
	 */
	public static float toFloat(byte channel)
	{
		return toFloat(toInt(channel));
	}

	/**
	 * Test all the channels in this {@link Color} are equal to an encoded {@code int} value.
	 * @see Color#toARGB()
	 * @implNote The channels of {@code this} are combined into an integer using {@link Color#toARGB()} for the comparison.
	 * @param argb the encoded integer containing color channels to test against {@link Color#r}, {@link Color#g}, {@link Color#b}, and {@link Color#a}
	 * @return true only when {@code argb} and {@code this} have all identical channel values.
	 */
	public boolean equals(int argb){
		return argb == this.toARGB();
	}

	/**
	 * Test the color channels in this {@link Color} are equal to {@code rgb} and {@link Color#a} is equal to {@code a}.
	 * @param rgb the color channel to test against {@link Color#r}, {@link Color#g}, and {@link Color#b}
	 * @param a the color channel to test against {@link Color#a}
	 * @return true only when {@code color} and {@code this} have all identical channel values.
	 */
	public boolean equals(byte rgb, byte a){
		return this.equals(rgb, rgb, rgb, a);
	}

	/**
	 * Test all the channels in this {@link Color} are equal to the corresponding values provided.
	 * @param red the color channel to test against {@link Color#r}
	 * @param green the color channel to test against {@link Color#g}
	 * @param blue the color channel to test against {@link Color#b}
	 * @param alpha the color channel to test against {@link Color#a}
	 * @return true only when {@code color} and {@code this} have all identical channel values.
	 */
	public boolean equals(byte red, byte green, byte blue, byte alpha){
		return this.r == red && this.g == green && this.b == blue && this.a == alpha;
	}

	/**
	 * Test the color channels in this {@link Color} are equal to {@code rgb} and {@link Color#a} is equal to {@code a}.
	 * @param rgb the integer to test against {@link Color#r}, {@link Color#g}, {@link Color#b}, {@link Color#a}
	 * @param a the integer to test against {@link Color#a}
	 * @return true only when {@code color} and {@code this} have all identical channel values.
	 */
	public boolean equals(int rgb, int a){
		return this.equals(toByte(rgb), toByte(a));
	}

	/**
	 * Test all the channels in this {@link Color} are equal to the corresponding values provided.
	 * @param red the integer to test against {@link Color#r}
	 * @param green the integer to test against {@link Color#g}
	 * @param blue the integer to test against {@link Color#b}
	 * @param alpha the integer to test against {@link Color#a}
	 * @return true only when {@code color} and {@code this} have all identical channel values.
	 */
	public boolean equals(int red, int green, int blue, int alpha){
		return this.R() == red && this.G() == green && this.B() == blue && this.A() == alpha;
	}

	/**
	 * Test the color channels in this {@link Color} are equal to {@code rgb} and {@link Color#a} is equal to {@code a}.
	 * @param rgb percent to test against {@link Color#r}, {@link Color#g}, {@link Color#b}, {@link Color#a}
	 * @param a percent to test against {@link Color#a}
	 * @return true only when {@code color} and {@code this} have all identical channel values.
	 */
	public boolean equals(float rgb, float a){
		return this.equals(toInt(rgb), toInt(a));
	}

	/**
	 * Test all the channels in this {@link Color} are equal to the corresponding values provided.
	 * @param red the percent to test against {@link Color#r}
	 * @param green the percent to test against {@link Color#g}
	 * @param blue the percent to test against {@link Color#b}
	 * @param alpha the percent to test against {@link Color#a}
	 * @return true only when {@code color} and {@code this} have all identical channel values.
	 */
	public boolean equals(float red, float green, float blue, float alpha){
		return this.getR() == red && this.getG() == green && this.getB() == blue && this.getA() == alpha;
	}

	/**
	 * Test another {@link Color} is equal to {@code this}.
	 * @param color the {@link Color} to test
	 * @return true only when {@code color} and {@code this} have all identical channel values.
	 */
	public boolean equals(Color color){
		return this.equals(color.r, color.g, color.b, color.a);
	}

	@Override
	public boolean equals(Object obj)
	{
		return getClass() == obj.getClass() && this.equals((Color) obj);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(r, g, b, a);
	}

	/**
	 * Converts this {@link Color} into a string containing the hexadecimal RGB A and decimal R G B A values.
	 * @return string describing this {@link Color}.
	 */
	@Override
	public String toString()
	{
		return String.format(
			"(#%X%X%X %X | %d %d %d %d)", this.r, this.g, this.b, this.a, this.R(), this.G(), this.B(), this.A()
		);
	}
}
