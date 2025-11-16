#version 330

in vec3 Position;
in vec2 UV0;

out vec2 texCoordinates;

void main() {
    gl_Position = vec4(Position, 1.0);
    texCoordinates = UV0;
}
