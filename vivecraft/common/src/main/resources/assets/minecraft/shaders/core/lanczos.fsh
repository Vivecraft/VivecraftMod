#version 150 core
 uniform sampler2D Sampler0;
 uniform sampler2D Sampler1;

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