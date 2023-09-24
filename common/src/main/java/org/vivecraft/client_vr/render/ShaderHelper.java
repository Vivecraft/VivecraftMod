package org.vivecraft.client_vr.render;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20C;

import java.util.List;

import static org.vivecraft.common.utils.Utils.logger;

import static com.mojang.blaze3d.platform.GlStateManager.*;

public class ShaderHelper
{
    private static int createShader(String shaderGLSL, int shaderType) throws Exception
    {
        int i = 0;

        try
        {
            i = glCreateShader(shaderType);

            if (i == 0)
            {
                return 0;
            }
            else
            {
                glShaderSource(i, List.of(shaderGLSL));
                glCompileShader(i);

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
            glDeleteShader(i);
            throw exception;
        }
    }

    public static int checkGLError(String par1Str)
    {
        int i = GL11C.glGetError();

        if (i != 0)
        {
            String s = "";
            logger.error("########## GL ERROR ##########");
            logger.error("@ " + par1Str);
            logger.error(i + ": " + s);
        }

        return i;
    }

    public static int initShaders(String vertexShaderGLSL, String fragmentShaderGLSL, boolean doAttribs)
    {
        int i = 0;
        int j = 0;
        int k;
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
                    b0 = 0;
                }
            }

            return b0;
        }
        k = glCreateProgram();

        if (k != 0)
        {
            glAttachShader(k, i);
            glAttachShader(k, j);

            if (doAttribs)
            {
                _glBindAttribLocation(k, 0, "in_Position");
                checkGLError("@2");
                _glBindAttribLocation(k, 1, "in_Color");
                checkGLError("@2a");
                _glBindAttribLocation(k, 2, "in_TextureCoord");
                checkGLError("@3");
            }

            GL20C.glLinkProgram(k);
            checkGLError("Link");

            String log = GL20C.glGetShaderInfoLog(i);
            if (!log.isEmpty())
            {
                logger.info("Shader compilation log: " + log);
                k = 0;
            }
            else
            {
                String log2 = GL20C.glGetShaderInfoLog(j);
                if (!log2.isEmpty())
                {
                    logger.info("Shader compilation log: " + log2);
                    k = 0;
                }
            }
        }
        return k;
    }
}
