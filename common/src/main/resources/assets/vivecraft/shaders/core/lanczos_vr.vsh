#version 330
#extension GL_ARB_separate_shader_objects : require

layout(std140) uniform LanczosUbo {
    float texelWidthOffset;
    float texelHeightOffset;
};

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;

layout(location = 0) out vec2 centerTextureCoordinate;
layout(location = 1) out vec2 oneStepLeftTextureCoordinate;
layout(location = 2) out vec2 twoStepsLeftTextureCoordinate;
layout(location = 3) out vec2 threeStepsLeftTextureCoordinate;
layout(location = 4) out vec2 fourStepsLeftTextureCoordinate;
layout(location = 5) out vec2 oneStepRightTextureCoordinate;
layout(location = 6) out vec2 twoStepsRightTextureCoordinate;
layout(location = 7) out vec2 threeStepsRightTextureCoordinate;
layout(location = 8) out vec2 fourStepsRightTextureCoordinate;

void main()
{
    gl_Position = vec4(Position, 1.0);

    vec2 firstOffset = vec2(texelWidthOffset, texelHeightOffset);
    vec2 secondOffset = vec2(2.0 * texelWidthOffset, 2.0 * texelHeightOffset);
    vec2 thirdOffset = vec2(3.0 * texelWidthOffset, 3.0 * texelHeightOffset);
    vec2 fourthOffset = vec2(4.0 * texelWidthOffset, 4.0 * texelHeightOffset);

    vec2 textCoord = UV0;
    centerTextureCoordinate = textCoord;
    oneStepLeftTextureCoordinate = textCoord - firstOffset;
    twoStepsLeftTextureCoordinate = textCoord - secondOffset;
    threeStepsLeftTextureCoordinate = textCoord - thirdOffset;
    fourStepsLeftTextureCoordinate = textCoord - fourthOffset;

    oneStepRightTextureCoordinate = textCoord + firstOffset;
    twoStepsRightTextureCoordinate = textCoord + secondOffset;
    threeStepsRightTextureCoordinate = textCoord + thirdOffset;
    fourStepsRightTextureCoordinate = textCoord + fourthOffset;
}
