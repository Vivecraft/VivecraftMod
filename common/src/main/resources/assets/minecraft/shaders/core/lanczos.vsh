#version 150 core

uniform float texelWidthOffset;
uniform float texelHeightOffset;

in vec3 Position;
in vec2 UV0;

out vec2 centerTextureCoordinate;
out vec2 oneStepLeftTextureCoordinate;
out vec2 twoStepsLeftTextureCoordinate;
out vec2 threeStepsLeftTextureCoordinate;
out vec2 fourStepsLeftTextureCoordinate;
out vec2 oneStepRightTextureCoordinate;
out vec2 twoStepsRightTextureCoordinate;
out vec2 threeStepsRightTextureCoordinate;
out vec2 fourStepsRightTextureCoordinate;

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
