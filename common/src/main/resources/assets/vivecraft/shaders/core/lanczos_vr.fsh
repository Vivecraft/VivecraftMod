#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

layout(location = 0) in vec2 centerTextureCoordinate;
layout(location = 1) in vec2 oneStepLeftTextureCoordinate;
layout(location = 2) in vec2 twoStepsLeftTextureCoordinate;
layout(location = 3) in vec2 threeStepsLeftTextureCoordinate;
layout(location = 4) in vec2 fourStepsLeftTextureCoordinate;
layout(location = 5) in vec2 oneStepRightTextureCoordinate;
layout(location = 6) in vec2 twoStepsRightTextureCoordinate;
layout(location = 7) in vec2 threeStepsRightTextureCoordinate;
layout(location = 8) in vec2 fourStepsRightTextureCoordinate;

layout(location = 0) out vec4 fragColor;

// sinc(x) * sinc(x/a) = (a * sin(pi * x) * sin(pi * x / a)) / (pi^2 * x^2)
// Assuming a Lanczos constant of 2.0, and scaling values to max out at x = +/- 1.5

void main()
{
    vec4 fragmentColor = texture(Sampler0, centerTextureCoordinate) * 0.38026;

    fragmentColor += texture(Sampler0, oneStepLeftTextureCoordinate) * 0.27667;
    fragmentColor += texture(Sampler0, oneStepRightTextureCoordinate) * 0.27667;

    fragmentColor += texture(Sampler0, twoStepsLeftTextureCoordinate) * 0.08074;
    fragmentColor += texture(Sampler0, twoStepsRightTextureCoordinate) * 0.08074;

    fragmentColor += texture(Sampler0, threeStepsLeftTextureCoordinate) * -0.02612;
    fragmentColor += texture(Sampler0, threeStepsRightTextureCoordinate) * -0.02612;

    fragmentColor += texture(Sampler0, fourStepsLeftTextureCoordinate) * -0.02143;
    fragmentColor += texture(Sampler0, fourStepsRightTextureCoordinate) * -0.02143;

    fragColor = fragmentColor;

    float depth = texture(Sampler1, centerTextureCoordinate).r * 0.38026;

    depth += texture(Sampler1, oneStepLeftTextureCoordinate).r * 0.27667;
    depth += texture(Sampler1, oneStepRightTextureCoordinate).r * 0.27667;

    depth += texture(Sampler1, twoStepsLeftTextureCoordinate).r * 0.08074;
    depth += texture(Sampler1, twoStepsRightTextureCoordinate).r * 0.08074;

    depth += texture(Sampler1, threeStepsLeftTextureCoordinate).r * -0.02612;
    depth += texture(Sampler1, threeStepsRightTextureCoordinate).r * -0.02612;

    depth += texture(Sampler1, fourStepsLeftTextureCoordinate).r * -0.02143;
    depth += texture(Sampler1, fourStepsRightTextureCoordinate).r * -0.02143;

    gl_FragDepth = depth;

}
