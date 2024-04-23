package org.vivecraft.client_vr.render;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL43C;

import java.util.List;

public class ShaderHelper {
    private static int createShader(String shaderGLSL, int shaderType) throws Exception {
        int i = 0;

        try {
            i = GlStateManager.glCreateShader(shaderType);

            if (i == 0) {
                return 0;
            } else {
                GlStateManager.glShaderSource(i, List.of(shaderGLSL));
                GlStateManager.glCompileShader(i);

                String log = GL20C.glGetShaderInfoLog(i);
                if (!log.isEmpty()) {
                    throw new RuntimeException("Error creating shader: " + log);
                } else {
                    return i;
                }
            }
        } catch (Exception exception) {
            GlStateManager.glDeleteShader(i);
            throw exception;
        }
    }

    public static int checkGLError(String par1Str) {
        int error = GlStateManager._getError();

        if (error != 0) {
            String errorString = switch (error) {
                case GL11.GL_INVALID_ENUM -> "invalid enum";
                case GL11.GL_INVALID_VALUE -> "invalid value";
                case GL11.GL_INVALID_OPERATION -> "invalid operation";
                case GL11.GL_STACK_OVERFLOW -> "stack overflow";
                case GL11.GL_STACK_UNDERFLOW -> "stack underflow";
                case GL11.GL_OUT_OF_MEMORY -> "out of memory";
                default -> "unknown error";
            };
            System.out.println("########## GL ERROR ##########");
            System.out.println("@ " + par1Str);
            System.out.println(error + ": " + errorString);
        }

        return error;
    }

    public static int initShaders(String vertexShaderGLSL, String fragmentShaderGLSL, boolean doAttribs) {
        int i = 0;
        int j = 0;
        int k = 0;
        label98:
        {
            byte b0;

            try {
                i = createShader(vertexShaderGLSL, 35633);
                j = createShader(fragmentShaderGLSL, 35632);
                break label98;
            } catch (Exception exception) {
                exception.printStackTrace();
                b0 = 0;
            } finally {
                if (i == 0 || j == 0) {
                    return 0;
                }
            }

            return b0;
        }
        k = GlStateManager.glCreateProgram();

        if (k == 0) {
            return 0;
        } else {
            GlStateManager.glAttachShader(k, i);
            GlStateManager.glAttachShader(k, j);

            if (doAttribs) {
                GlStateManager._glBindAttribLocation(k, 0, "in_Position");
                checkGLError("@2");
                GlStateManager._glBindAttribLocation(k, 1, "in_Color");
                checkGLError("@2a");
                GlStateManager._glBindAttribLocation(k, 2, "in_TextureCoord");
                checkGLError("@3");
            }

            GL43C.glLinkProgram(k);
            checkGLError("Link");

            String log = GL20C.glGetShaderInfoLog(i);
            if (!log.isEmpty()) {
                System.out.println("Shader compilation log: " + log);
                return 0;
            }
            String log2 = GL20C.glGetShaderInfoLog(j);
            if (!log2.isEmpty()) {
                System.out.println("Shader compilation log: " + log2);
                return 0;
            } else {
                return k;
            }
        }
    }
}
