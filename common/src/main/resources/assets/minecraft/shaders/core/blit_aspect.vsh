#version 150 core

in vec3 Position;
in vec2 UV0;

out vec2 texCoordinates;

void main() {
    gl_Position = vec4(Position.xy * 2.0 - 1.0, 0, 1.0);
    texCoordinates = UV0;
}
