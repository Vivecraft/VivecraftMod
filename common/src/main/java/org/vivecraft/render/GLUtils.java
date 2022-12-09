package org.vivecraft.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import org.lwjgl.opengl.GL12;

public class GLUtils
{
    private static FloatBuffer matrixBuffer = MemoryTracker.create(16).asFloatBuffer();

    public static synchronized ByteBuffer createByteBuffer(int size)
    {
        return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }

    public static FloatBuffer createFloatBuffer(int size)
    {
        return createByteBuffer(size << 2).asFloatBuffer();
    }

    public static synchronized int generateDisplayLists(int range)
    {
        int i = GL12.glGenLists(range);

        if (i == 0)
        {
            int j = GlStateManager._getError();
            String s = "No error code reported";

            if (j != 0)
            {
                s = "dunno";// GLX.getErrorString(j);
            }

            throw new IllegalStateException("glGenLists returned an ID of 0 for a count of " + range + ", GL error (" + j + "): " + s);
        }
        else
        {
            return i;
        }
    }
}
