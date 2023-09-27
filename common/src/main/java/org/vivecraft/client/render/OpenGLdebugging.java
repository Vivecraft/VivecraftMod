package org.vivecraft.client.render;

import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

public class OpenGLdebugging {
    private static final Map<String, Boolean> dumpedComplete = new HashMap<>();
    private static final Map<String, Boolean> dumped = new HashMap<>();
    private static final Map<String, Boolean> dumpedType = new HashMap<>();
    public static OpenGLdebugging instance = new OpenGLdebugging();
    public GLproperty[] propertyList = new GLproperty[]{new GLproperty(2816, "GL_CURRENT_COLOR", "Current color", "current", "glGetFloatv()"), new GLproperty(2817, "GL_CURRENT_INDEX", "Current color index", "current", "glGetFloatv()"), new GLproperty(2819, "GL_CURRENT_TEXTURE_COORDS", "Current texture coordinates", "current", "glGetFloatv()"), new GLproperty(2818, "GL_CURRENT_NORMAL", "Current normal", "current", "glGetFloatv()"), new GLproperty(2823, "GL_CURRENT_RASTER_POSITION", "Current raster position", "current", "glGetFloatv()"), new GLproperty(2825, "GL_CURRENT_RASTER_DISTANCE", "Current raster distance", "current", "glGetFloatv()"), new GLproperty(2820, "GL_CURRENT_RASTER_COLOR", "Color associated with raster position", "current", "glGetFloatv()"), new GLproperty(2821, "GL_CURRENT_RASTER_INDEX", "Color index associated with raster position", "current", "glGetFloatv()"), new GLproperty(2822, "GL_CURRENT_RASTER_TEXTURE_COORDS", "Texture coordinates associated with raster position", "current", "glGetFloatv()"), new GLproperty(2824, "GL_CURRENT_RASTER_POSITION_VALID", "Raster position valid bit", "current", "glGetBooleanv()"), new GLproperty(2883, "GL_EDGE_FLAG", "Edge flag", "current", "glGetBooleanv()"), new GLproperty(32884, "GL_VERTEX_ARRAY", "Vertex array enable", "vertex-array", "glIsEnabled()"), new GLproperty(32890, "GL_VERTEX_ARRAY_SIZE", "Coordinates per vertex", "vertex-array", "glGetIntegerv()"), new GLproperty(32891, "GL_VERTEX_ARRAY_TYPE", "Type of vertex coordinates", "vertex-array", "glGetIntegerv()"), new GLproperty(32892, "GL_VERTEX_ARRAY_STRIDE", "Stride between vertices", "vertex-array", "glGetIntegerv()"), new GLproperty(32910, "GL_VERTEX_ARRAY_POINTER", "Pointer to the vertex array", "vertex-array", "glGetPointerv()"), new GLproperty(32885, "GL_NORMAL_ARRAY", "Normal array enable", "vertex-array", "glIsEnabled()"), new GLproperty(32894, "GL_NORMAL_ARRAY_TYPE", "Type of normal coordinates", "vertex-array", "glGetIntegerv()"), new GLproperty(32895, "GL_NORMAL_ARRAY_STRIDE", "Stride between normals", "vertex-array", "glGetIntegerv()"), new GLproperty(32911, "GL_NORMAL_ARRAY_POINTER", "Pointer to the normal array", "vertex-array", "glGetPointerv()"), new GLproperty(32886, "GL_COLOR_ARRAY", "RGBA color array enable", "vertex-array", "glIsEnabled()"), new GLproperty(32897, "GL_COLOR_ARRAY_SIZE", "Colors per vertex", "vertex-array", "glGetIntegerv()"), new GLproperty(32898, "GL_COLOR_ARRAY_TYPE", "Type of color components", "vertex-array", "glGetIntegerv()"), new GLproperty(32899, "GL_COLOR_ARRAY_STRIDE", "Stride between colors", "vertex-array", "glGetIntegerv()"), new GLproperty(32912, "GL_COLOR_ARRAY_POINTER", "Pointer to the color array", "vertex-array", "glGetPointerv()"), new GLproperty(32887, "GL_INDEX_ARRAY", "Color-index array enable", "vertex-array", "glIsEnabled()"), new GLproperty(32901, "GL_INDEX_ARRAY_TYPE", "Type of color indices", "vertex-array", "glGetIntegerv()"), new GLproperty(32902, "GL_INDEX_ARRAY_STRIDE", "Stride between color indices", "vertex-array", "glGetIntegerv()"), new GLproperty(32913, "GL_INDEX_ARRAY_POINTER", "Pointer to the index array", "vertex-array", "glGetPointerv()"), new GLproperty(32888, "GL_TEXTURE_COORD_ARRAY", "Texture coordinate array enable", "vertex-array", "glIsEnabled()"), new GLproperty(32904, "GL_TEXTURE_COORD_ARRAY_SIZE", "Texture coordinates per element", "vertex-array", "glGetIntegerv()"), new GLproperty(32905, "GL_TEXTURE_COORD_ARRAY_TYPE", "Type of texture coordinates", "vertex-array", "glGetIntegerv()"), new GLproperty(32906, "GL_TEXTURE_COORD_ARRAY_STRIDE", "Stride between texture coordinates", "vertex-array", "glGetIntegerv()"), new GLproperty(32914, "GL_TEXTURE_COORD_ARRAY_POINTER", "Pointer to the texture coordinate array", "vertex-array", "glGetPointerv()"), new GLproperty(32889, "GL_EDGE_FLAG_ARRAY", "Edge flag array enable", "vertex-array", "glIsEnabled()"), new GLproperty(32908, "GL_EDGE_FLAG_ARRAY_STRIDE", "Stride between edge flags", "vertex-array", "glGetIntegerv()"), new GLproperty(32915, "GL_EDGE_FLAG_ARRAY_POINTER", "Pointer to the edge flag array", "vertex-array", "glGetPointerv()"), new GLproperty(2982, "GL_MODELVIEW_MATRIX", "Modelview matrix stack", "matrix", "glGetFloatv()"), new GLproperty(2983, "GL_PROJECTION_MATRIX", "Projection matrix stack", "matrix", "glGetFloatv()"), new GLproperty(2984, "GL_TEXTURE_MATRIX", "Texture matrix stack", "matrix", "glGetFloatv()"), new GLproperty(2978, "GL_VIEWPORT", "Viewport origin and extent", "viewport", "glGetIntegerv()"), new GLproperty(2928, "GL_DEPTH_RANGE", "Depth range near and far", "viewport", "glGetFloatv()"), new GLproperty(2979, "GL_MODELVIEW_STACK_DEPTH", "Modelview matrix stack pointer", "matrix", "glGetIntegerv()"), new GLproperty(2980, "GL_PROJECTION_STACK_DEPTH", "Projection matrix stack pointer", "matrix", "glGetIntegerv()"), new GLproperty(2981, "GL_TEXTURE_STACK_DEPTH", "Texture matrix stack pointer", "matrix", "glGetIntegerv()"), new GLproperty(2976, "GL_MATRIX_MODE", "Current matrix mode", "transform", "glGetIntegerv()"), new GLproperty(2977, "GL_NORMALIZE", "Current normal normalization on/off", "transform/ enable", "glIsEnabled()"), new GLproperty(2918, "GL_FOG_COLOR", "Fog color", "fog", "glGetFloatv()"), new GLproperty(2913, "GL_FOG_INDEX", "Fog index", "fog", "glGetFloatv()"), new GLproperty(2914, "GL_FOG_DENSITY", "Exponential fog density", "fog", "glGetFloatv()"), new GLproperty(2915, "GL_FOG_START", "Linear fog start", "fog", "glGetFloatv()"), new GLproperty(2916, "GL_FOG_END", "Linear fog end", "fog", "glGetFloatv()"), new GLproperty(2917, "GL_FOG_MODE", "Fog mode", "fog", "glGetIntegerv()"), new GLproperty(2912, "GL_FOG", "True if fog enabled", "fog/enable", "glIsEnabled()"), new GLproperty(2900, "GL_SHADE_MODEL", "glShadeModel() setting", "lighting", "glGetIntegerv()"), new GLproperty(2896, "GL_LIGHTING", "True if lighting is enabled", "lighting/e nable", "glIsEnabled()"), new GLproperty(2903, "GL_COLOR_MATERIAL", "True if color tracking is enabled", "lighting", "glIsEnabled()"), new GLproperty(2902, "GL_COLOR_MATERIAL_PARAMETER", "Material properties tracking current color", "lighting", "glGetIntegerv()"), new GLproperty(2901, "GL_COLOR_MATERIAL_FACE", "Face(s) affected by color tracking", "lighting", "glGetIntegerv()"), new GLproperty(4608, "GL_AMBIENT", "Ambient material color", "lighting", "glGetMaterialfv()"), new GLproperty(4609, "GL_DIFFUSE", "Diffuse material color", "lighting", "glGetMaterialfv()"), new GLproperty(4610, "GL_SPECULAR", "Specular material color", "lighting", "glGetMaterialfv()"), new GLproperty(5632, "GL_EMISSION", "Emissive material color", "lighting", "glGetMaterialfv()"), new GLproperty(5633, "GL_SHININESS", "Specular exponent of material", "lighting", "glGetMaterialfv()"), new GLproperty(2899, "GL_LIGHT_MODEL_AMBIENT", "Ambient scene color", "lighting", "glGetFloatv()"), new GLproperty(2897, "GL_LIGHT_MODEL_LOCAL_VIEWER", "Viewer is local", "lighting", "glGetBooleanv()"), new GLproperty(2898, "GL_LIGHT_MODEL_TWO_SIDE", "Use two-sided lighting", "lighting", "glGetBooleanv()"), new GLproperty(4608, "GL_AMBIENT", "Ambient intensity of light i", "lighting", "glGetLightfv()"), new GLproperty(4609, "GL_DIFFUSE", "Diffuse intensity of light i", "lighting", "glGetLightfv()"), new GLproperty(4610, "GL_SPECULAR", "Specular intensity of light i", "lighting", "glGetLightfv()"), new GLproperty(4611, "GL_POSITION", "Position of light i", "lighting", "glGetLightfv()"), new GLproperty(4615, "GL_CONSTANT_ATTENUATION", "Constant attenuation factor", "lighting", "glGetLightfv()"), new GLproperty(4616, "GL_LINEAR_ATTENUATION", "Linear attenuation factor", "lighting", "glGetLightfv()"), new GLproperty(4617, "GL_QUADRATIC_ATTENUATION", "Quadratic attenuation factor", "lighting", "glGetLightfv()"), new GLproperty(4612, "GL_SPOT_DIRECTION", "Spotlight direction of light i", "lighting", "glGetLightfv()"), new GLproperty(4613, "GL_SPOT_EXPONENT", "Spotlight exponent of light i", "lighting", "glGetLightfv()"), new GLproperty(4614, "GL_SPOT_CUTOFF", "Spotlight angle of light i", "lighting", "glGetLightfv()"), new GLproperty(16384, "GL_LIGHT0", "True if light 0 enabled", "lighting/enable", "glIsEnabled()"), new GLproperty(16385, "GL_LIGHT1", "True if light 1 enabled", "lighting/enable", "glIsEnabled()"), new GLproperty(16386, "GL_LIGHT2", "True if light 2 enabled", "lighting/enable", "glIsEnabled()"), new GLproperty(16387, "GL_LIGHT3", "True if light 3 enabled", "lighting/enable", "glIsEnabled()"), new GLproperty(16388, "GL_LIGHT4", "True if light 4 enabled", "lighting/enable", "glIsEnabled()"), new GLproperty(16389, "GL_LIGHT5", "True if light 5 enabled", "lighting/enable", "glIsEnabled()"), new GLproperty(16390, "GL_LIGHT6", "True if light 6 enabled", "lighting/enable", "glIsEnabled()"), new GLproperty(16391, "GL_LIGHT7", "True if light 7 enabled", "lighting/enable", "glIsEnabled()"), new GLproperty(5635, "GL_COLOR_INDEXES", "ca, cd, and cs for color-index lighting", "lighting/e nable", "glGetMaterialfv()"), new GLproperty(2833, "GL_POINT_SIZE", "Point size", "point", "glGetFloatv()"), new GLproperty(2832, "GL_POINT_SMOOTH", "Point antialiasing on", "point/enable", "glIsEnabled()"), new GLproperty(2849, "GL_LINE_WIDTH", "Line width", "line", "glGetFloatv()"), new GLproperty(2848, "GL_LINE_SMOOTH", "Line antialiasing on", "line/enable", "glIsEnabled()"), new GLproperty(2853, "GL_LINE_STIPPLE_PATTERN", "Line stipple", "line", "glGetIntegerv()"), new GLproperty(2854, "GL_LINE_STIPPLE_REPEAT", "Line stipple repeat", "line", "glGetIntegerv()"), new GLproperty(2852, "GL_LINE_STIPPLE", "Line stipple enable", "line/enable", "glIsEnabled()"), new GLproperty(2884, "GL_CULL_FACE", "Polygon culling enabled", "polygon/enable", "glIsEnabled()"), new GLproperty(2885, "GL_CULL_FACE_MODE", "Cull front-/back-facing polygons", "polygon", "glGetIntegerv()"), new GLproperty(2886, "GL_FRONT_FACE", "Polygon front-face CW/CCW indicator", "polygon", "glGetIntegerv()"), new GLproperty(2881, "GL_POLYGON_SMOOTH", "Polygon antialiasing on", "polygon/enable", "glIsEnabled()"), new GLproperty(2880, "GL_POLYGON_MODE", "Polygon rasterization mode (front and back)", "polygon", "glGetIntegerv()"), new GLproperty(32824, "GL_POLYGON_OFFSET_FACTOR", "Polygon offset factor", "polygon", "glGetFloatv()"), new GLproperty(10753, "GL_POLYGON_OFFSET_POINT", "Polygon offset enable for GL_POINT mode rasterization", "polygon/enable", "glIsEnabled()"), new GLproperty(10754, "GL_POLYGON_OFFSET_LINE", "Polygon offset enable for GL_LINE mode rasterization", "polygon/enable", "glIsEnabled()"), new GLproperty(32823, "GL_POLYGON_OFFSET_FILL", "Polygon offset enable for GL_FILL mode rasterization", "polygon/enable", "glIsEnabled()"), new GLproperty(2882, "GL_POLYGON_STIPPLE", "Polygon stipple enable", "polygon/enable", "glIsEnabled()"), new GLproperty(3552, "GL_TEXTURE_1D", "True if 1-D texturing enabled ", "texture/enable", "glIsEnabled()"), new GLproperty(3553, "GL_TEXTURE_2D", "True if 2-D texturing enabled ", "texture/enable", "glIsEnabled()"), new GLproperty(32872, "GL_TEXTURE_BINDING_1D", "Texture object bound to GL_TEXTURE_1D", "texture", "glGetIntegerv()"), new GLproperty(32873, "GL_TEXTURE_BINDING_2D", "Texture object bound to GL_TEXTURE_2D", "texture", "glGetIntegerv()"), new GLproperty(5890, "GL_TEXTURE", "x-D texture image at level of detail i", "UNUSED", "glGetTexImage()"), new GLproperty(4096, "GL_TEXTURE_WIDTH", "x-D texture image i's width", "UNUSED", "glGetTexLevelParameter*()"), new GLproperty(4097, "GL_TEXTURE_HEIGHT", "x-D texture image i's height", "UNUSED", "glGetTexLevelParameter*()"), new GLproperty(4101, "GL_TEXTURE_BORDER", "x-D texture image i's border width", "UNUSED", "glGetTexLevelParameter*()"), new GLproperty(32860, "GL_TEXTURE_RED_SIZE", "x-D texture image i's red resolution", "UNUSED", "glGetTexLevelParameter*()"), new GLproperty(32861, "GL_TEXTURE_GREEN_SIZE", "x-D texture image i's green resolution", "UNUSED", "glGetTexLevelParameter*()"), new GLproperty(32862, "GL_TEXTURE_BLUE_SIZE", "x-D texture image i's blue resolution", "UNUSED", "glGetTexLevelParameter*()"), new GLproperty(32863, "GL_TEXTURE_ALPHA_SIZE", "x-D texture image i's alpha resolution", "UNUSED", "glGetTexLevelParameter*()"), new GLproperty(32864, "GL_TEXTURE_LUMINANCE_SIZE", "x-D texture image i's luminance resolution", "UNUSED", "glGetTexLevelParameter*()"), new GLproperty(32865, "GL_TEXTURE_INTENSITY_SIZE", "x-D texture image i's intensity resolution", "UNUSED", "glGetTexLevelParameter*()"), new GLproperty(4100, "GL_TEXTURE_BORDER_COLOR", "Texture border color", "texture", "glGetTexParameter*()"), new GLproperty(10241, "GL_TEXTURE_MIN_FILTER", "Texture minification function", "texture", "glGetTexParameter*()"), new GLproperty(10240, "GL_TEXTURE_MAG_FILTER", "Texture magnification function", "texture", "glGetTexParameter*()"), new GLproperty(10242, "GL_TEXTURE_WRAP_S", "Texture wrap mode (x is S or T)", "texture", "glGetTexParameter*()"), new GLproperty(10243, "GL_TEXTURE_WRAP_T", "Texture wrap mode (x is S or T)", "texture", "glGetTexParameter*()"), new GLproperty(32870, "GL_TEXTURE_PRIORITY", "Texture object priority", "texture", "glGetTexParameter*()"), new GLproperty(8704, "GL_TEXTURE_ENV_MODE", "Texture application function", "texture", "glGetTexEnviv()"), new GLproperty(8705, "GL_TEXTURE_ENV_COLOR", "Texture environment color", "texture", "glGetTexEnvfv()"), new GLproperty(3168, "GL_TEXTURE_GEN_S", "Texgen enabled (x is S, T, R, or Q)", "texture/enable", "glIsEnabled()"), new GLproperty(3169, "GL_TEXTURE_GEN_T", "Texgen enabled (x is S, T, R, or Q)", "texture/enable", "glIsEnabled()"), new GLproperty(3170, "GL_TEXTURE_GEN_R", "Texgen enabled (x is S, T, R, or Q)", "texture/enable", "glIsEnabled()"), new GLproperty(3171, "GL_TEXTURE_GEN_Q", "Texgen enabled (x is S, T, R, or Q)", "texture/enable", "glIsEnabled()"), new GLproperty(9474, "GL_EYE_PLANE", "Texgen plane equation coefficients", "texture", "glGetTexGenfv()"), new GLproperty(9473, "GL_OBJECT_PLANE", "Texgen object linear coefficients", "texture", "glGetTexGenfv()"), new GLproperty(9472, "GL_TEXTURE_GEN_MODE", "Function used for texgen", "texture", "glGetTexGeniv()"), new GLproperty(3089, "GL_SCISSOR_TEST", "Scissoring enabled", "scissor/enable", "glIsEnabled()"), new GLproperty(3088, "GL_SCISSOR_BOX", "Scissor box", "scissor", "glGetIntegerv()"), new GLproperty(3008, "GL_ALPHA_TEST", "Alpha test enabled", "color-buffer/enable", "glIsEnabled()"), new GLproperty(3009, "GL_ALPHA_TEST_FUNC", "Alpha test function", "color-buffer", "glGetIntegerv()"), new GLproperty(3010, "GL_ALPHA_TEST_REF", "Alpha test reference value", "color-buffer", "glGetIntegerv()"), new GLproperty(2960, "GL_STENCIL_TEST", "Stenciling enabled", "stencil-buffer/enable", "glIsEnabled()"), new GLproperty(2962, "GL_STENCIL_FUNC", "Stencil function", "stencil-buffer", "glGetIntegerv()"), new GLproperty(2963, "GL_STENCIL_VALUE_MASK", "Stencil mask", "stencil-buffer", "glGetIntegerv()"), new GLproperty(2967, "GL_STENCIL_REF", "Stencil reference value", "stencil-buffer", "glGetIntegerv()"), new GLproperty(2964, "GL_STENCIL_FAIL", "Stencil fail action", "stencil-buffer", "glGetIntegerv()"), new GLproperty(2965, "GL_STENCIL_PASS_DEPTH_FAIL", "Stencil depth buffer fail action", "stencil-buffer", "glGetIntegerv()"), new GLproperty(2966, "GL_STENCIL_PASS_DEPTH_PASS", "Stencil depth buffer pass action", "stencil-buffer", "glGetIntegerv()"), new GLproperty(2929, "GL_DEPTH_TEST", "Depth buffer enabled", "depth-buffer/ena ble", "glIsEnabled()"), new GLproperty(2932, "GL_DEPTH_FUNC", "Depth buffer test function", "depth-buffer", "glGetIntegerv()"), new GLproperty(3042, "GL_BLEND", "Blending enabled", "color-buffer/enable", "glIsEnabled()"), new GLproperty(3041, "GL_BLEND_SRC", "Blending source function", "color-buffer", "glGetIntegerv()"), new GLproperty(3040, "GL_BLEND_DST", "Blending destination function", "color-buffer", "glGetIntegerv()"), new GLproperty(3024, "GL_DITHER", "Dithering enabled", "color-buffer/enable", "glIsEnabled()"), new GLproperty(3057, "GL_INDEX_LOGIC_OP", "Color index logical operation enabled", "color-buffer/enable", "glIsEnabled()"), new GLproperty(3058, "GL_COLOR_LOGIC_OP", "RGBA color logical operation enabled", "color-buffer/enable", "glIsEnabled()"), new GLproperty(3056, "GL_LOGIC_OP_MODE", "Logical operation function", "color-buffer", "glGetIntegerv()"), new GLproperty(3073, "GL_DRAW_BUFFER", "Buffers selected for drawing", "color-buffer", "glGetIntegerv()"), new GLproperty(3105, "GL_INDEX_WRITEMASK", "Color-index writemask", "color-buffer", "glGetIntegerv()"), new GLproperty(3107, "GL_COLOR_WRITEMASK", "Color write enables; R, G, B, or A", "color-buffer", "glGetBooleanv()"), new GLproperty(2930, "GL_DEPTH_WRITEMASK", "Depth buffer enabled for writing", "depth-buffer", "glGetBooleanv()"), new GLproperty(2968, "GL_STENCIL_WRITEMASK", "Stencil-buffer writemask", "stencil-buffer", "glGetIntegerv()"), new GLproperty(3106, "GL_COLOR_CLEAR_VALUE", "Color-buffer clear value (RGBA mode)", "color-buffer", "glGetFloatv()"), new GLproperty(3104, "GL_INDEX_CLEAR_VALUE", "Color-buffer clear value (color-index mode)", "color-buffer", "glGetFloatv()"), new GLproperty(2931, "GL_DEPTH_CLEAR_VALUE", "Depth-buffer clear value", "depth-buffer", "glGetIntegerv()"), new GLproperty(2961, "GL_STENCIL_CLEAR_VALUE", "Stencil-buffer clear value", "stencil-buffer", "glGetIntegerv()"), new GLproperty(2944, "GL_ACCUM_CLEAR_VALUE", "Accumulation-buffer clear value", "accum-buffer", "glGetFloatv()"), new GLproperty(3312, "GL_UNPACK_SWAP_BYTES", "Value of GL_UNPACK_SWAP_BYTES", "pixel-store", "glGetBooleanv()"), new GLproperty(3313, "GL_UNPACK_LSB_FIRST", "Value of GL_UNPACK_LSB_FIRST", "pixel-store", "glGetBooleanv()"), new GLproperty(3314, "GL_UNPACK_ROW_LENGTH", "Value of GL_UNPACK_ROW_LENGTH", "pixel-store", "glGetIntegerv()"), new GLproperty(3315, "GL_UNPACK_SKIP_ROWS", "Value of GL_UNPACK_SKIP_ROWS", "pixel-store", "glGetIntegerv()"), new GLproperty(3316, "GL_UNPACK_SKIP_PIXELS", "Value of GL_UNPACK_SKIP_PIXELS", "pixel-store", "glGetIntegerv()"), new GLproperty(3317, "GL_UNPACK_ALIGNMENT", "Value of GL_UNPACK_ALIGNMENT", "pixel-store", "glGetIntegerv()"), new GLproperty(3328, "GL_PACK_SWAP_BYTES", "Value of GL_PACK_SWAP_BYTES", "pixel-store", "glGetBooleanv()"), new GLproperty(3329, "GL_PACK_LSB_FIRST", "Value of GL_PACK_LSB_FIRST", "pixel-store", "glGetBooleanv()"), new GLproperty(3330, "GL_PACK_ROW_LENGTH", "Value of GL_PACK_ROW_LENGTH", "pixel-store", "glGetIntegerv()"), new GLproperty(3331, "GL_PACK_SKIP_ROWS", "Value of GL_PACK_SKIP_ROWS", "pixel-store", "glGetIntegerv()"), new GLproperty(3332, "GL_PACK_SKIP_PIXELS", "Value of GL_PACK_SKIP_PIXELS", "pixel-store", "glGetIntegerv()"), new GLproperty(3333, "GL_PACK_ALIGNMENT", "Value of GL_PACK_ALIGNMENT", "pixel-store", "glGetIntegerv()"), new GLproperty(3344, "GL_MAP_COLOR", "True if colors are mapped", "pixel", "glGetBooleanv()"), new GLproperty(3345, "GL_MAP_STENCIL", "True if stencil values are mapped", "pixel", "glGetBooleanv()"), new GLproperty(3346, "GL_INDEX_SHIFT", "Value of GL_INDEX_SHIFT", "pixel", "glGetIntegerv()"), new GLproperty(3347, "GL_INDEX_OFFSET", "Value of GL_INDEX_OFFSET", "pixel", "glGetIntegerv()"), new GLproperty(3350, "GL_ZOOM_X", "x zoom factor", "pixel", "glGetFloatv()"), new GLproperty(3351, "GL_ZOOM_Y", "y zoom factor", "pixel", "glGetFloatv()"), new GLproperty(3074, "GL_READ_BUFFER", "Read source buffer", "pixel", "glGetIntegerv()"), new GLproperty(2561, "GL_ORDER", "1D map order", "capability", "glGetMapiv()"), new GLproperty(2561, "GL_ORDER", "2D map orders", "capability", "glGetMapiv()"), new GLproperty(2560, "GL_COEFF", "1D control points", "capability", "glGetMapfv()"), new GLproperty(2560, "GL_COEFF", "2D control points", "capability", "glGetMapfv()"), new GLproperty(2562, "GL_DOMAIN", "1D domain endpoints", "capability", "glGetMapfv()"), new GLproperty(2562, "GL_DOMAIN", "2D domain endpoints", "capability", "glGetMapfv()"), new GLproperty(3536, "GL_MAP1_GRID_DOMAIN", "1D grid endpoints", "eval", "glGetFloatv()"), new GLproperty(3538, "GL_MAP2_GRID_DOMAIN", "2D grid endpoints", "eval", "glGetFloatv()"), new GLproperty(3537, "GL_MAP1_GRID_SEGMENTS", "1D grid divisions", "eval", "glGetFloatv()"), new GLproperty(3539, "GL_MAP2_GRID_SEGMENTS", "2D grid divisions", "eval", "glGetFloatv()"), new GLproperty(3456, "GL_AUTO_NORMAL", "True if automatic normal generation enabled", "eval", "glIsEnabled()"), new GLproperty(3152, "GL_PERSPECTIVE_CORRECTION_HINT", "Perspective correction hint", "hint", "glGetIntegerv()"), new GLproperty(3153, "GL_POINT_SMOOTH_HINT", "Point smooth hint", "hint", "glGetIntegerv()"), new GLproperty(3154, "GL_LINE_SMOOTH_HINT", "Line smooth hint", "hint", "glGetIntegerv()"), new GLproperty(3155, "GL_POLYGON_SMOOTH_HINT", "Polygon smooth hint", "hint", "glGetIntegerv()"), new GLproperty(3156, "GL_FOG_HINT", "Fog hint", "hint", "glGetIntegerv()"), new GLproperty(3377, "GL_MAX_LIGHTS", "Maximum number of lights", "capability", "glGetIntegerv()"), new GLproperty(3378, "GL_MAX_CLIP_PLANES", "Maximum number of user clipping planes", "capability", "glGetIntegerv()"), new GLproperty(3382, "GL_MAX_MODELVIEW_STACK_DEPTH", "Maximum modelview-matrix stack depth", "capability", "glGetIntegerv()"), new GLproperty(3384, "GL_MAX_PROJECTION_STACK_DEPTH", "Maximum projection-matrix stack depth", "capability", "glGetIntegerv()"), new GLproperty(3385, "GL_MAX_TEXTURE_STACK_DEPTH", "Maximum depth of texture matrix stack", "capability", "glGetIntegerv()"), new GLproperty(3408, "GL_SUBPIXEL_BITS", "Number of bits of subpixel precision in x and y", "capability", "glGetIntegerv()"), new GLproperty(3379, "GL_MAX_TEXTURE_SIZE", "See discussion in Texture Proxy in Chapter 9", "capability", "glGetIntegerv()"), new GLproperty(3380, "GL_MAX_PIXEL_MAP_TABLE", "Maximum size of a glPixelMap() translation table", "capability", "glGetIntegerv()"), new GLproperty(3383, "GL_MAX_NAME_STACK_DEPTH", "Maximum selection-name stack depth", "capability", "glGetIntegerv()"), new GLproperty(2865, "GL_MAX_LIST_NESTING", "Maximum display-list call nesting", "capability", "glGetIntegerv()"), new GLproperty(3376, "GL_MAX_EVAL_ORDER", "Maximum evaluator polynomial order", "capability", "glGetIntegerv()"), new GLproperty(3386, "GL_MAX_VIEWPORT_DIMS", "Maximum viewport dimensions", "capability", "glGetIntegerv()"), new GLproperty(3381, "GL_MAX_ATTRIB_STACK_DEPTH", "Maximum depth of the attribute stack", "capability", "glGetIntegerv()"), new GLproperty(3387, "GL_MAX_CLIENT_ATTRIB_STACK_DEPTH", "Maximum depth of the client attribute stack", "capability", "glGetIntegerv()"), new GLproperty(3072, "GL_AUX_BUFFERS", "Number of auxiliary buffers", "capability", "glGetBooleanv()"), new GLproperty(3121, "GL_RGBA_MODE", "True if color buffers store RGBA", "capability", "glGetBooleanv()"), new GLproperty(3120, "GL_INDEX_MODE", "True if color buffers store indices", "capability", "glGetBooleanv()"), new GLproperty(3122, "GL_DOUBLEBUFFER", "True if front and back buffers exist", "capability", "glGetBooleanv()"), new GLproperty(3123, "GL_STEREO", "True if left and right buffers exist", "capability", "glGetBooleanv()"), new GLproperty(2834, "GL_POINT_SIZE_RANGE", "Range (low to high) of antialiased point sizes", "capability", "glGetFloatv()"), new GLproperty(2835, "GL_POINT_SIZE_GRANULARITY", "Antialiased point-size granularity", "capability", "glGetFloatv()"), new GLproperty(2850, "GL_LINE_WIDTH_RANGE", "Range (low to high) of antialiased line widths", "capability", "glGetFloatv()"), new GLproperty(2851, "GL_LINE_WIDTH_GRANULARITY", "Antialiased line-width granularity", "capability", "glGetFloatv()"), new GLproperty(3410, "GL_RED_BITS", "Number of bits per red component in color buffers", "capability", "glGetIntegerv()"), new GLproperty(3411, "GL_GREEN_BITS", "Number of bits per green component in color buffers", "capability", "glGetIntegerv()"), new GLproperty(3412, "GL_BLUE_BITS", "Number of bits per blue component in color buffers", "capability", "glGetIntegerv()"), new GLproperty(3413, "GL_ALPHA_BITS", "Number of bits per alpha component in color buffers", "capability", "glGetIntegerv()"), new GLproperty(3409, "GL_INDEX_BITS", "Number of bits per index in color buffers", "capability", "glGetIntegerv()"), new GLproperty(3414, "GL_DEPTH_BITS", "Number of depth-buffer bitplanes", "capability", "glGetIntegerv()"), new GLproperty(3415, "GL_STENCIL_BITS", "Number of stencil bitplanes", "capability", "glGetIntegerv()"), new GLproperty(3416, "GL_ACCUM_RED_BITS", "Number of bits per red component in the accumulation buffer", "capability", "glGetIntegerv()"), new GLproperty(3417, "GL_ACCUM_GREEN_BITS", "Number of bits per green component in the accumulation buffer", "capability", "glGetIntegerv()"), new GLproperty(3418, "GL_ACCUM_BLUE_BITS", "Number of bits per blue component in the accumulation buffer", "capability", "glGetIntegerv()"), new GLproperty(3419, "GL_ACCUM_ALPHA_BITS", "Number of bits per alpha component in the accumulation buffer", "capability", "glGetIntegerv()"), new GLproperty(2866, "GL_LIST_BASE", "Setting of glListBase()", "list", "glGetIntegerv()"), new GLproperty(2867, "GL_LIST_INDEX", "Number of display list under construction; 0 if none", "current", "glGetIntegerv()"), new GLproperty(2864, "GL_LIST_MODE", "Mode of display list under construction; undefined if none", "current", "glGetIntegerv()"), new GLproperty(2992, "GL_ATTRIB_STACK_DEPTH", "Attribute stack pointer", "current", "glGetIntegerv()"), new GLproperty(2993, "GL_CLIENT_ATTRIB_STACK_DEPTH", "Client attribute stack pointer", "current", "glGetIntegerv()"), new GLproperty(3440, "GL_NAME_STACK_DEPTH", "Name stack depth", "current", "glGetIntegerv()"), new GLproperty(3136, "GL_RENDER_MODE", "glRenderMode() setting", "current", "glGetIntegerv()"), new GLproperty(3571, "GL_SELECTION_BUFFER_POINTER", "Pointer to selection buffer", "select", "glGetPointerv()"), new GLproperty(3572, "GL_SELECTION_BUFFER_SIZE", "Size of selection buffer", "select", "glGetIntegerv()"), new GLproperty(3568, "GL_FEEDBACK_BUFFER_POINTER", "Pointer to feedback buffer", "feedback", "glGetPointerv()"), new GLproperty(3569, "GL_FEEDBACK_BUFFER_SIZE", "Size of feedback buffer", "feedback", "glGetIntegerv()"), new GLproperty(3570, "GL_FEEDBACK_BUFFER_TYPE", "Type of feedback buffer", "feedback", "glGetIntegerv()")};

    public static String dumpOpenGLstate() {
        StringBuilder stringbuilder = new StringBuilder();

        for (int i = 0; i < instance.propertyList.length; ++i) {
            stringbuilder.append(instance.propertyList[i].name + ":");
            stringbuilder.append(GL11.glIsEnabled(instance.propertyList[i].gLconstant) + ":");
            stringbuilder.append(getPropertyAsString(i));
            stringbuilder.append(" (" + instance.propertyList[i].description + ")\n");
        }

        return stringbuilder.toString();
    }

    public static void dumpOpenGLstateToFile(String filename) {
        String s = dumpOpenGLstate();

        try {
            FileOutputStream fileoutputstream = new FileOutputStream(new File(filename));
            IOUtils.write(s.getBytes(), fileoutputstream);
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
    }

    public static void dumpOpenGLstateToFileOnce(String filename) {
        if (dumpedComplete.get(filename) == null) {
            dumpOpenGLstateToFile(filename);
            dumpedComplete.put(filename, true);
        }
    }

    public static String dumpAllIsEnabled() {
        StringBuilder stringbuilder = new StringBuilder();

        for (int i = 0; i < instance.propertyList.length; ++i) {
            if (instance.propertyList[i].fetchCommand == "glIsEnabled()") {
                stringbuilder.append(instance.propertyList[i].name + ":");
                stringbuilder.append(GL11.glIsEnabled(instance.propertyList[i].gLconstant));
                stringbuilder.append(" (" + instance.propertyList[i].description + ")\n");
            }
        }

        return stringbuilder.toString();
    }

    public static void dumpAllIsEnabledToFile(String filename) {
        String s = dumpAllIsEnabled();

        try {
            FileOutputStream fileoutputstream = new FileOutputStream(new File(filename));
            IOUtils.write(s.getBytes(), fileoutputstream);
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
    }

    public static void dumpAllIsEnabledToFileOnce(String filename) {
        if (dumped.get(filename) == null) {
            dumpAllIsEnabledToFile(filename);
            dumped.put(filename, true);
        }
    }

    public static String dumpAllType(String type) {
        StringBuilder stringbuilder = new StringBuilder();

        for (int i = 0; i < instance.propertyList.length; ++i) {
            if (instance.propertyList[i].category.equals(type)) {
                stringbuilder.append(instance.propertyList[i].name + ":");
                stringbuilder.append(getPropertyAsString(i));
                stringbuilder.append(" (" + instance.propertyList[i].description + ")\n");
            }
        }

        return stringbuilder.toString();
    }

    public static void dumpAllTypeToFile(String filename, String type) {
        String s = dumpAllType(type);

        try {
            FileOutputStream fileoutputstream = new FileOutputStream(new File(filename));
            IOUtils.write(s.getBytes(), fileoutputstream);
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
    }

    public static void dumpAllTypeToFileOnce(String filename, String type) {
        if (dumpedType.get(filename) == null) {
            dumpAllTypeToFile(filename, type);
            dumpedType.put(filename, true);
        }
    }

    private static String getPropertyAsString(int propertyListIndex) {
        int i = instance.propertyList[propertyListIndex].gLconstant;

        if (instance.propertyList[propertyListIndex].fetchCommand.equals("glIsEnabled()")) {
            return "" + GL11.glIsEnabled(i);
        } else if (instance.propertyList[propertyListIndex].fetchCommand == "glGetBooleanv()") {
            ByteBuffer bytebuffer = BufferUtils.createByteBuffer(16);
            GL11.glGetBooleanv(i, bytebuffer);
            String s2 = "";

            for (int l = 0; l < bytebuffer.capacity(); ++l) {
                s2 = s2 + (l == 0 ? "" : ", ") + bytebuffer.get(l);
            }

            return s2;
        } else if (instance.propertyList[propertyListIndex].fetchCommand == "glGetIntegerv()") {
            IntBuffer intbuffer = BufferUtils.createIntBuffer(16);
            GL11.glGetIntegerv(i, intbuffer);
            String s1 = "";

            for (int k = 0; k < intbuffer.capacity(); ++k) {
                s1 = s1 + (k == 0 ? "" : ", ") + intbuffer.get(k);
            }

            return s1;
        } else if (instance.propertyList[propertyListIndex].fetchCommand == "glGetFloatv()") {
            FloatBuffer floatbuffer = BufferUtils.createFloatBuffer(16);
            GL11.glGetFloatv(i, floatbuffer);
            String s = "";

            for (int j = 0; j < floatbuffer.capacity(); ++j) {
                s = s + (j == 0 ? "" : ", ") + floatbuffer.get(j);
            }

            return s;
        } else {
            return "";
        }
    }

    public class GLproperty {
        public int gLconstant;
        public String name;
        public String description;
        public String category;
        public String fetchCommand;

        public GLproperty(int init_gLconstant, String init_name, String init_description, String init_category, String init_fetchCommand) {
            this.gLconstant = init_gLconstant;
            this.name = init_name;
            this.description = init_description;
            this.category = init_category;
            this.fetchCommand = init_fetchCommand;
        }
    }
}
