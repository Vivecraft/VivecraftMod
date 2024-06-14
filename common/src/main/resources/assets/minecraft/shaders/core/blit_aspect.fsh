#version 150 core

uniform sampler2D DiffuseSampler;

in vec2 texCoordinates;

out vec4 fragColor;

void main(){

    fragColor = texture(DiffuseSampler, texCoordinates.st);

}
