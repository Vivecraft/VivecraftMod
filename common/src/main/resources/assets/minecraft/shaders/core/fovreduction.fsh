#version 150 core

uniform sampler2D Sampler0;
uniform float circle_radius;
uniform float circle_offset = 0.1;
uniform float border;

uniform float water;
uniform float portal;
uniform float pumpkin;

uniform float portaltime;
uniform float redalpha;
uniform float bluealpha;
uniform float blackalpha;

in vec2 texCoordinates;

out vec4 fragColor;

const vec4 black = vec4(0, 0, 0, 1.0);
const vec4 orange = vec4(.25, .125, 0, 1.0);
const float pi = 3.14159265;

uniform int eye = 0;

void main(){

	vec4 bkg_color = texture(Sampler0,texCoordinates.st); 
    
	if(portal > 0){ //swirly whirly
		float ts = texCoordinates.s;
		vec2 mod_texcoord = texCoordinates.st + vec2(portal*.005*cos(portaltime + 20*ts*pi), portal*.005*sin(portaltime + 30*ts*pi));
		bkg_color = texture(Sampler0, mod_texcoord);
	}

	if(water > 0){ //goobly woobly
		float ts = texCoordinates.s;
		vec2 mod_texcoord = texCoordinates.st + vec2(0, water*.0010*sin(portaltime + 10*ts*pi));
		bkg_color = texture(Sampler0, mod_texcoord);
		vec4 blue = vec4(0, 0, bkg_color.b, 1.0);
		bkg_color  = mix(bkg_color, blue, 0.1);

	}
	
	if(redalpha > 0){ //ouchy wouchy
		vec4 red = vec4(bkg_color.r, 0, 0, 1.0);
		bkg_color  = mix(bkg_color, red, redalpha);
	}
	
	if(bluealpha > 0){ //chilly willy
		vec4 blue = vec4(0, bkg_color.g/2.0, bkg_color.b, 1.0);
		bkg_color  = mix(bkg_color, blue, bluealpha);
	}

	if(blackalpha > 0){ //spooky wooky
		bkg_color  = mix(bkg_color, black, blackalpha);
	}

	if(circle_radius < 0.8){ //arfy barfy
		vec2 circle_center = vec2(0.5 + eye*circle_offset, 0.5);
		vec2 uv = texCoordinates.xy; 
		uv -= circle_center; 
		float dist =  sqrt(dot(uv, uv)); 
		float t = 1.0 + smoothstep(circle_radius, circle_radius+10, dist) - smoothstep(circle_radius-border, circle_radius, dist);
		if(pumpkin>0){
			bkg_color  = mix(orange, bkg_color,t);
		} else{
			bkg_color  = mix(black, bkg_color,t);
		}
	}

	fragColor = bkg_color;
	
}