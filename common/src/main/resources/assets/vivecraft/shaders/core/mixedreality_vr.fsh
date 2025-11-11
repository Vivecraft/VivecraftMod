#version 330

#define GUI_FIRST 1
#define GUI_THIRD 2
#define GUI_SEPARATE 4

uniform sampler2D firstPersonColor;

uniform sampler2D thirdPersonColor;
uniform sampler2D thirdPersonDepth;

uniform sampler2D guiColor;

layout(std140) uniform MixedRealityUbo {
    mat4 projectionMatrix;
    mat4 viewMatrix;

// these are vec4s beacuse of ubo shenanigans
    vec4 keyColor;
    vec4 hmdViewPosition;
    vec4 hmdPlaneNormal;

    int alphaMode;
    int firstPersonPass;
    int guiMask;
};

in vec2 texCoordinates;

out vec4 out_Color;

vec3 getFragmentPosition(in vec2 coord) {
    vec4 posScreen = vec4(coord * 2.0 - 1.0, texture(thirdPersonDepth, coord).x * 2.0 - 1.0, 1);
    vec4 posView = inverse(projectionMatrix * viewMatrix) * posScreen;
    return posView.xyz / posView.w;
}

vec3 avoidKeyColor(in vec3 color) {
    // make sure colors don't match keyColor
    if (all(lessThan(color - keyColor.rgb, vec3(0.004)))) {
        if (all(lessThan(keyColor.rgb, vec3(0.004)))) {
            // if key is black add
            return color + 0.004;
        } else {
            return color - 0.004;
        }
    } else {
        return color;
    }
}

vec4 sampleTexture(sampler2D colorSampler, vec2 coord, int gui) {
    vec4 color = vec4(texture(colorSampler, coord).rgb, 0.0);
    if ((guiMask & gui) != 0) {
        vec4 guiColor = texture(guiColor, coord);
        color.rgb = mix(color.rgb, guiColor.rgb, guiColor.a);
        color.a = guiColor.a;
    }
    return color;
}

void main(void) {

    out_Color = vec4(keyColor.rgb, 1.0);
    if (firstPersonPass == 1) {
        // unity like
        vec2 sampleTexCoord = fract(texCoordinates * 2.0);
        if (texCoordinates.x >= 0.5 && texCoordinates.y < 0.5) {
            // first person
            out_Color.rgb = sampleTexture(firstPersonColor, sampleTexCoord, GUI_FIRST).rgb;
        } else {
            vec4 thirdColor = sampleTexture(thirdPersonColor, sampleTexCoord, GUI_THIRD);
            if (texCoordinates.x < 0.5 && texCoordinates.y < 0.5) {
                // third person all
                out_Color.rgb = thirdColor.rgb;
            } else if (texCoordinates.y >= 0.5) {
                // third person front
                vec3 fragPos = getFragmentPosition(sampleTexCoord);
                if (texCoordinates.x >= 0.5 && (guiMask & GUI_SEPARATE) != 0 && alphaMode != 1) {
                    vec4 guiCol = texture(guiColor, sampleTexCoord);
                    out_Color.rgb = mix(out_Color.rgb, avoidKeyColor(guiCol.rgb), step(0.1, guiCol.a));
                } else if (dot(fragPos - hmdViewPosition.xyz, hmdPlaneNormal.xyz) >= 0.0 || thirdColor.a > 0.1) {
                    if (texCoordinates.x < 0.5) {
                        // color
                        out_Color.rgb = thirdColor.rgb;
                        if (alphaMode == 0) {
                            out_Color.rgb = avoidKeyColor(out_Color.rgb);
                        }
                    } else if (alphaMode == 1) {
                        // white mask
                        out_Color.rgb = vec3(1.0);
                    }
                }
            }
        }
    } else {
        // side by side
        vec2 sampleTexCoord = fract(texCoordinates * vec2(2.0, 1.0));
        vec4 thirdColor = sampleTexture(thirdPersonColor, sampleTexCoord, GUI_THIRD);
        if (texCoordinates.x >= 0.5) {
            // third person all
            out_Color.rgb = thirdColor.rgb;
        } else {
            // third person front
            vec3 fragPos = getFragmentPosition(sampleTexCoord);
            if (dot(fragPos - hmdViewPosition.xyz, hmdPlaneNormal.xyz) >= 0.0 || thirdColor.a > 0.1) {
                // color
                out_Color.rgb = avoidKeyColor(thirdColor.rgb);
            }
        }
    }
}
