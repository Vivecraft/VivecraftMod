#version 330

uniform sampler2D DiffuseSampler;

in vec2 texCoordinates;

out vec4 fragColor;

void main(){
    fragColor = texture(DiffuseSampler, texCoordinates.st);

    // srgb is liner in the lower, and pow in the upper
    bvec3 cutoff = lessThanEqual(fragColor.rgb, vec3(0.04045));
    vec3 higher = pow(fragColor.rgb * 0.9478672986 + 0.0521327014, vec3(2.4));
    vec3 lower = fragColor.rgb * 0.0773993808;
    fragColor.rgb = mix(higher, lower, vec3(cutoff));
}
