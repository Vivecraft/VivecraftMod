package org.vivecraft.render;

import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class ShaderHelper
{
    private static int createShader(String shaderGLSL, int shaderType) throws Exception
    {
        int i = 0;

        try
        {
            i = ARBShaderObjects.glCreateShaderObjectARB(shaderType);

            if (i == 0)
            {
                return 0;
            }
            else
            {
                ARBShaderObjects.glShaderSourceARB(i, (CharSequence)shaderGLSL);
                ARBShaderObjects.glCompileShaderARB(i);

                if (ARBShaderObjects.glGetObjectParameteriARB(i, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == 0)
                {
                    throw new RuntimeException("Error creating shader: " + getLogInfo(i));
                }
                else
                {
                    return i;
                }
            }
        }
        catch (Exception exception)
        {
            ARBShaderObjects.glDeleteObjectARB(i);
            throw exception;
        }
    }

    public static int checkGLError(String par1Str)
    {
        int i = GL11.glGetError();

        if (i != 0)
        {
            String s = "";
            System.out.println("########## GL ERROR ##########");
            System.out.println("@ " + par1Str);
            System.out.println(i + ": " + s);
        }

        return i;
    }

    private static String getLogInfo(int obj)
    {
        return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, 35716));
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
        k = ARBShaderObjects.glCreateProgramObjectARB();

        if (k == 0)
        {
            return 0;
        }
        else
        {
            ARBShaderObjects.glAttachObjectARB(k, i);
            ARBShaderObjects.glAttachObjectARB(k, j);

            if (doAttribs)
            {
                GL20.glBindAttribLocation(k, 0, "in_Position");
                checkGLError("@2");
                GL20.glBindAttribLocation(k, 1, "in_Color");
                checkGLError("@2a");
                GL20.glBindAttribLocation(k, 2, "in_TextureCoord");
                checkGLError("@3");
            }

            ARBShaderObjects.glLinkProgramARB(k);
            checkGLError("Link");

            if (ARBShaderObjects.glGetObjectParameteriARB(k, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == 0)
            {
                System.out.println(getLogInfo(k));
                return 0;
            }
            else
            {
                ARBShaderObjects.glValidateProgramARB(k);

                if (ARBShaderObjects.glGetObjectParameteriARB(k, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == 0)
                {
                    System.out.println(getLogInfo(k));
                    return 0;
                }
                else
                {
                    return k;
                }
            }
        }
    }
}
