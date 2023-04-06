package org.vivecraft.client.render;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public class StaticTexture extends SimpleTexture
{
    private static final Logger LOG = LogManager.getLogger();

    public StaticTexture(ResourceLocation p_118133_)
    {
        super(p_118133_);
    }

    public void load(ResourceManager pManager) throws IOException
    {
        this.releaseId();
        Resource resource = null;

        try
        {
            BufferedImage bufferedimage = null;
            boolean flag = false;
            boolean flag1 = false;

//            if (Config.isShaders())
//            {
//            }
        }
        finally
        {
            IOUtils.closeQuietly((Closeable)resource);
        }
    }
}
