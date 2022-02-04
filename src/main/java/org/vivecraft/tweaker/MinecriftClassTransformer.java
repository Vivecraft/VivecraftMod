package org.vivecraft.tweaker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.launchwrapper.IClassTransformer;
import org.vivecraft.utils.Utils;

public class MinecriftClassTransformer implements IClassTransformer
{
    private static final boolean DEBUG = true;//Boolean.parseBoolean(System.getProperty("legacy.debugClassLoading", "false"));
    private ZipFile mcZipFile = null;
    private final MinecriftClassTransformer.Stage stage;
    private final Map<String, byte[]> cache;
    private static Set<String> myClasses = new HashSet<>();

    public MinecriftClassTransformer()
    {
        this(MinecriftClassTransformer.Stage.MAIN, (Map<String, byte[]>)null);
    }

    public MinecriftClassTransformer(MinecriftClassTransformer.Stage stage, Map<String, byte[]> cache)
    {
        this.stage = stage;
        this.cache = cache;

        if (stage == MinecriftClassTransformer.Stage.MAIN)
        {
            try
            {
                this.mcZipFile = Utils.getVivecraftZip();
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }

            if (this.mcZipFile == null)
            {
                debug("*** Can not find the Minecrift JAR in the classpath ***");
                debug("*** Minecrift will not be loaded! ***");
            }
        }
        else if (cache == null)
        {
            throw new IllegalArgumentException("Cache map required for cache/replace stage");
        }
    }

    public byte[] transform(String name, String transformedName, byte[] bytes)
    {
        switch (this.stage)
        {
            case MAIN:
                byte[] abyte = this.getMinecriftClass(name);

                if (abyte == null)
                {
                    if (DEBUG)
                    {
                        debug(String.format("Vivecraft: Passthrough %s %s", name, transformedName));
                    }
                }
                else
                {
                    myClasses.add(name);
                    abyte = this.performAsmModification(abyte, transformedName);
                    int i = bytes == null ? 0 : bytes.length;

                    if (i != abyte.length)
                    {
                        debug(String.format("Vivecraft: Overwrite %s %s (%d != %d)", name, transformedName, i, abyte.length));
                        myClasses.add(transformedName);
                    }
                }

                return abyte != null ? abyte : bytes;

            case CACHE:
                if (myClasses.contains(transformedName))
                {
                    if (DEBUG)
                    {
                        debug(String.format("Cache '%s' - '%s'", name, transformedName));
                    }

                    this.cache.put(transformedName, bytes);
                }

                return bytes;

            case REPLACE:
                if (this.cache.containsKey(transformedName))
                {
                    if (DEBUG)
                    {
                        debug(String.format("Replace '%s' - '%s'", name, transformedName));
                    }

                    return this.cache.get(transformedName);
                }

                return bytes;

            default:
                return bytes;
        }
    }

    private void writeToFile(String dir, String transformedName, String name, byte[] bytes)
    {
        FileOutputStream fileoutputstream = null;
        String s = String.format("%s/%s/%s/%s%s.%s", System.getProperty("user.home"), "minecrift_transformed_classes", dir, transformedName.replace(".", "/"), name, "class");
        File file1 = new File(s);
        debug("Writing to: " + s);

        try
        {
            File file2 = file1.getParentFile();
            file2.mkdirs();
            file1.createNewFile();
            fileoutputstream = new FileOutputStream(s);
            fileoutputstream.write(bytes);
        }
        catch (FileNotFoundException filenotfoundexception)
        {
            filenotfoundexception.printStackTrace();
        }
        catch (IOException ioexception1)
        {
            ioexception1.printStackTrace();
        }
        finally
        {
            try
            {
                if (fileoutputstream != null)
                {
                    fileoutputstream.close();
                }
            }
            catch (IOException ioexception)
            {
                ioexception.printStackTrace();
            }
        }
    }

    private byte[] getMinecriftClass(String name)
    {
        if (this.mcZipFile == null)
        {
            return null;
        }
        else
        {
            String s = name + ".class";
            ZipEntry zipentry = this.mcZipFile.getEntry(s);

            if (zipentry == null)
            {
                s = name + ".clazz";
                zipentry = this.mcZipFile.getEntry(s);
            }

            if (zipentry == null)
            {
                return null;
            }
            else
            {
                try
                {
                    InputStream inputstream = this.mcZipFile.getInputStream(zipentry);
                    byte[] abyte = readAll(inputstream);

                    if ((long)abyte.length != zipentry.getSize())
                    {
                        debug("Invalid size for " + s + ": " + abyte.length + ", should be: " + zipentry.getSize());
                        return null;
                    }
                    else
                    {
                        return abyte;
                    }
                }
                catch (IOException ioexception)
                {
                    ioexception.printStackTrace();
                    return null;
                }
            }
        }
    }

    public static byte[] readAll(InputStream is) throws IOException
    {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        byte[] abyte = new byte[1024];

        while (true)
        {
            int i = is.read(abyte);

            if (i < 0)
            {
                is.close();
                return bytearrayoutputstream.toByteArray();
            }

            bytearrayoutputstream.write(abyte, 0, i);
        }
    }

    private static void debug(String str)
    {
        System.out.println(str);
    }

    private byte[] performAsmModification(byte[] origBytecode, String className)
    {
        return origBytecode;
    }

    public static enum Stage
    {
        MAIN,
        CACHE,
        REPLACE;
    }
}
