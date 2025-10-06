package org.vivecraft.mod_compat_vr.shaders.patches;

import java.util.regex.Pattern;

/**
 * holder of a regex pattern and a replacement string to patch shaders
 */
public abstract class Patch {

    // characters that should be padded with whitespaces
    private final static Pattern GLSL_REGEX_SEARCH = Pattern.compile(
        "(?<!^)(" + // not at the start of the line
            "\\\\\\[|" + // opening bracket: \[
            "\\\\]|" + // closing bracket: \]
            "\\\\\\(|" + // opening bracket: \(
            "\\\\\\)|" + // closing bracket: \)
            "(?<!])\\[|" + // bracket expression: [, but not if it is directly after a closing one
            "(?<![(?\\\\])\\(|" + // capturing group: (, not preceded by other group, '?' or a \
            "\\\\\\.|" + // literal . : \.
            "\\\\\\+|" + // literal + : \+
            "\\\\\\*|" + // literal * : \*
            "\\\\\\?|" + // literal ? : \?
            "(?<!\\?):|" + // literal ':', not preceded by a '?'
            "[-;=/,]|" + // literal '-', ';', '=', '/' and ','
            "\\\\\\d+|" + // capturing group references: \1, \2
            "(?<!\\\\|\\||((?<!\\\\)[\\[(])|\\?:|\\w)\\w+" + // words not preceded by regex keywords
            ")");

    private final static Pattern GLSL_SWIZZLE_SEARCH = Pattern.compile("\\\\\\.[xyzw]{1,4}(?!\\w)");

    private final static Pattern GLSL_NUMBER_SEARCH = Pattern.compile("\\d+\\\\\\.0");

    private final Pattern[] patterns;
    private final String replacement;
    private final String testString;

    public Patch(String testString, String replacement, Pattern... patterns) {
        this.testString = testString;
        this.replacement = replacement;
        this.patterns = patterns;
    }

    public Patch(String testString, String replacement, String... stringPatterns) {
        this.testString = testString;
        this.replacement = replacement;
        this.patterns = new Pattern[stringPatterns.length];
        for (int i = 0; i < stringPatterns.length; i++) {
            this.patterns[i] = Pattern.compile(padRegex(stringPatterns[i]), Pattern.CASE_INSENSITIVE);
        }
    }

    /**
     * applies this patches to the given shader
     *
     * @param shader shader to patch
     * @return patched shader
     */
    public String patch(String shader) {
        for (Pattern pattern : this.patterns) {
            shader = pattern.matcher(shader).replaceAll(this.replacement);
        }
        return shader;
    }

    /**
     * pads glsl/regex expressions with whitespaces
     *
     * @param regex regex patter to pad
     * @return padded regex pattern
     */
    protected static String padRegex(String regex) {
        regex = GLSL_SWIZZLE_SEARCH.matcher(regex).replaceAll(matchResult -> swizzle(matchResult.group()));
        regex = GLSL_NUMBER_SEARCH.matcher(regex).replaceAll(matchResult -> number(matchResult.group()));
        return GLSL_REGEX_SEARCH.matcher(regex).replaceAll("\\\\s*$1").replaceAll(" ", "\\\\s*");
    }

    private static String swizzle(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            sb.append(switch (c) {
                case 'x' -> "[xrs]";
                case 'y' -> "[ygt]";
                case 'z' -> "[zbp]";
                case 'w' -> "[waq]";
                case '\\' -> "\\\\";
                default -> c;
            });
        }
        return sb.toString();
    }

    private static String number(String input) {
        String number = input.substring(0, input.length() - 3);
        return "(?:%s|%s\\\\.|%s\\\\.0)".formatted(number, number, number);
    }

    /**
     * @return the test string this patcher is supposed to patch correctly
     */
    public String getTestString() {
        return this.testString;
    }

    /**
     * @return the patterns used for patching
     */
    public Pattern[] getPatterns() {
        return this.patterns;
    }
}
