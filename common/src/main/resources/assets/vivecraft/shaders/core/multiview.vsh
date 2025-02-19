#version 330 core
#extension GL_OVR_multiview : enable
layout(num_views = 2) in;

in vec3 Position;
in vec2 UV0;
uniform mat4 ModelViewProj[2];

out vec2 texCoordinates;

void main() {
    gl_Position = ModelViewProj[gl_ViewID_OVR] * vec4(Position, 1.0);
    texCoordinates = UV0;
}
