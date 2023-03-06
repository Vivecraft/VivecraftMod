package org.vivecraft.render;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.*;

import java.util.List;

public class ShaderHelper
{
/*    private static int createShader(String shaderGLSL, int shaderType) throws Exception
    {
        int i = 0;

        try
        {
            i = GL43C.glCreateShader(shaderType);

            if (i == 0)
            {
                return 0;
            }
            else
            {
                GlStateManager.glShaderSource(i, List.of(shaderGLSL));
                GlStateManager.glCompileShader(i);

                String log = GL20C.glGetShaderInfoLog(i);
                if (!log.isEmpty()) {
                    throw new RuntimeException("Error creating shader: " + log);
                }
                else
                {
                    return i;
                }
            }
        }
        catch (Exception exception)
        {
            GlStateManager.glDeleteShader(i);
            throw exception;
        }
    }

    public static int initShaders(String vertexShaderGLSL, String fragmentShaderGLSL, boolean doAttribs)
    {
        int i = 0;
        int j = 0;
        int k = 0;
        label98:
        {
            byte b0;

            try
            {
                i = createShader(vertexShaderGLSL, 35633);
                j = createShader(fragmentShaderGLSL, 35632);
                break label98;
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
                b0 = 0;
            }
            finally
            {
                if (i == 0 || j == 0)
                {
                    return 0;
                }
            }

            return b0;
        }
        k = GlStateManager.glCreateProgram();

        if (k == 0)
        {
            return 0;
        }
        else
        {
            GL43C.glAttachShader(k, i);
            GL43C.glAttachShader(k, j);

            if (doAttribs)
            {
                GL20.glBindAttribLocation(k, 0, "in_Position");
                GL20.glBindAttribLocation(k, 1, "in_Color");
                GL20.glBindAttribLocation(k, 2, "in_TextureCoord");
            }

            GL43C.glLinkProgram(k);

            String log = GL20C.glGetShaderInfoLog(i);
            if (!log.isEmpty()) {
                System.out.println("Shader compilation log: " + log);
                return 0;
            }
            String log2 = GL20C.glGetShaderInfoLog(j);
            if (!log2.isEmpty()) {
                System.out.println("Shader compilation log: " + log2);
                return 0;
            }
            else
                {
                    return k;
                }
            }
        }*/
}
