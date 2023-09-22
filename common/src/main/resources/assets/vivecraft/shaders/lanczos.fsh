#version 150 core
uniform sampler2D inputImageTexture;
uniform sampler2D inputDepthTexture;

in vec2 centerTextureCoordinate;
in vec2 oneStepLeftTextureCoordinate;
in vec2 twoStepsLeftTextureCoordinate;
in vec2 threeStepsLeftTextureCoordinate;
in vec2 fourStepsLeftTextureCoordinate;
in vec2 oneStepRightTextureCoordinate;
in vec2 twoStepsRightTextureCoordinate;
in vec2 threeStepsRightTextureCoordinate;
in vec2 fourStepsRightTextureCoordinate;
out vec4 fragColor;
// sinc(x) * sinc(x/a) = (a * sin(pi * x) * sin(pi * x / a)) / (pi^2 * x^2)
// Assuming a Lanczos constant of 2.0, and scaling values to max out at x = +/- 1.5

void main()
{
    vec4 fragmentColor = texture2D(inputImageTexture, centerTextureCoordinate) * 0.38026;

    fragmentColor += texture2D(inputImageTexture, oneStepLeftTextureCoordinate) * 0.27667;
    fragmentColor += texture2D(inputImageTexture, oneStepRightTextureCoordinate) * 0.27667;

    fragmentColor += texture2D(inputImageTexture, twoStepsLeftTextureCoordinate) * 0.08074;
    fragmentColor += texture2D(inputImageTexture, twoStepsRightTextureCoordinate) * 0.08074;

    fragmentColor += texture2D(inputImageTexture, threeStepsLeftTextureCoordinate) * -0.02612;
    fragmentColor += texture2D(inputImageTexture, threeStepsRightTextureCoordinate) * -0.02612;

    fragmentColor += texture2D(inputImageTexture, fourStepsLeftTextureCoordinate) * -0.02143;
    fragmentColor += texture2D(inputImageTexture, fourStepsRightTextureCoordinate) * -0.02143;

    fragColor = fragmentColor;

    float depth = texture2D(inputDepthTexture, centerTextureCoordinate).r * 0.38026;

    depth += texture2D(inputDepthTexture, oneStepLeftTextureCoordinate).r * 0.27667;
    depth += texture2D(inputDepthTexture, oneStepRightTextureCoordinate).r * 0.27667;

    depth += texture2D(inputDepthTexture, twoStepsLeftTextureCoordinate).r * 0.08074;
    depth += texture2D(inputDepthTexture, twoStepsRightTextureCoordinate).r * 0.08074;

    depth += texture2D(inputDepthTexture, threeStepsLeftTextureCoordinate).r * -0.02612;
    depth += texture2D(inputDepthTexture, threeStepsRightTextureCoordinate).r * -0.02612;

    depth += texture2D(inputDepthTexture, fourStepsLeftTextureCoordinate).r * -0.02143;
    depth += texture2D(inputDepthTexture, fourStepsRightTextureCoordinate).r * -0.02143;

    gl_FragDepth = depth;

}
