package org.vivecraft.client.utils;

import org.vivecraft.client.Xplat;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_vr.utils.LoaderUtils;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;

import com.mojang.blaze3d.pipeline.RenderTarget;

import net.minecraft.ChatFormatting;
import net.minecraft.client.ComponentCollector;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.logger;

import static org.joml.Math.*;

public class Utils
{
    private static final char[] illegalChars = {'"', '<', '>', '|', '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005', '\u0006', '\u0007', '\b', '\t', '\n', '\u000b', '\f', '\r', '\u000e', '\u000f', '\u0010', '\u0011', '\u0012', '\u0013', '\u0014', '\u0015', '\u0016', '\u0017', '\u0018', '\u0019', '\u001a', '\u001b', '\u001c', '\u001d', '\u001e', '\u001f', ':', '*', '?', '\\', '/'};
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 20000;
    private static final Random avRandomizer = new Random();

    public static void message(final Component literal)
    {
        if (mc.level != null) {
            mc.gui.getChat().addMessage(literal);
        }
    }

    public static String sanitizeFileName(String fileName)
    {
        StringBuilder stringbuilder = new StringBuilder();

        for (int i = 0; i < fileName.length(); ++i)
        {
            char c0 = fileName.charAt(i);

            if (Arrays.binarySearch(illegalChars, c0) < 0)
            {
                stringbuilder.append(c0);
            }
            else
            {
                stringbuilder.append('_');
            }
        }

        return stringbuilder.toString();
    }

    public static float angleDiff(float a, float b)
    {
        float f = abs(a - b) % 360.0F;
        float f1 = f > 180.0F ? 360.0F - f : f;
        int i = (!(a - b >= 0.0F) || !(a - b <= 180.0F)) && (!(a - b <= -180.0F) || !(a - b >= -360.0F)) ? -1 : 1;
        return f1 * (float)i;
    }

    public static float angleNormalize(float angle)
    {
        angle = angle % 360.0F;

        if (angle < 0.0F)
        {
            angle += 360.0F;
        }

        return angle;
    }

    public static void wordWrap(String in, int length, ArrayList<String> wrapped)
    {
        // can't wrap with length 0, so return the original string
        if (length == 0) {
            wrapped.add(in);
            return;
        }
        String s = "\n";
        boolean flag = false;
        in = in.replace("\r", "");

        if (in.length() < length)
        {
            flag = true;
            length = in.length();
        }

        if (in.substring(0, length).contains(s))
        {
            String s2 = in.substring(0, in.indexOf(s)).trim();
            wrapped.add(s2);
            wordWrap(in.substring(in.indexOf(s) + 1), length, wrapped);
        }
        else if (flag)
        {
            wrapped.add(in);
        }
        else
        {
            int i = max(max(in.lastIndexOf(' ', length), in.lastIndexOf('\t', length)), in.lastIndexOf('-', length));

            if (i == -1)
            {
                i = length;
            }

            String s1 = in.substring(0, i).trim();
            wrapped.add(s1);
            wordWrap(in.substring(i), length, wrapped);
        }
    }

    public static InputStream getAssetAsStream(String name, boolean required)
    {
        InputStream inputstream = null;

        try
        {
            try
            {
                Optional<Resource> resource = mc.getResourceManager().getResource(new ResourceLocation("vivecraft", name));
                if (resource.isPresent()) {
                    inputstream = resource.get().open();
                }
            }
            catch (NullPointerException | FileNotFoundException filenotfoundexception)
            {
                inputstream = VRShaders.class.getResourceAsStream("/assets/vivecraft/" + name);
            }

            if (inputstream == null)
            {
                Path path1 = Paths.get(System.getProperty("user.dir"));

                if (path1.getParent() != null)
                {
                    Path path = path1.getParent().resolve("src/resources/assets/vivecraft/" + name);

                    if (!path.toFile().exists() && path1.getParent().getParent() != null)
                    {
                        path = path1.getParent().getParent().resolve("resources/assets/vivecraft/" + name);
                    }

                    if (path.toFile().exists())
                    {
                        inputstream = new FileInputStream(path.toFile());
                    }
                }
            }
        }
        catch (Exception exception)
        {
            handleAssetException(exception, name, required);
            return null;
        }

        if (inputstream == null)
        {
            handleAssetException(new FileNotFoundException(name), name, required);
        }

        return inputstream;
    }

    public static byte[] loadAsset(String name, boolean required)
    {
        InputStream inputstream = getAssetAsStream(name, required);

        if (inputstream == null)
        {
            return null;
        }
        else
        {
            try
            {
                byte[] abyte = IOUtils.toByteArray(inputstream);
                inputstream.close();
                return abyte;
            }
            catch (Exception exception)
            {
                handleAssetException(exception, name, required);
                return null;
            }
        }
    }

    public static String loadAssetAsString(String name, boolean required)
    {
        byte[] abyte = loadAsset(name, required);
        return abyte == null ? null : new String(abyte, StandardCharsets.UTF_8);
    }

    public static void loadAssetToFile(String name, File file, boolean required)
    {
        InputStream inputstream = getAssetAsStream(name, required);

        if (inputstream != null)
        {
            try
            {
                writeStreamToFile(inputstream, file);
                inputstream.close();
            }
            catch (Exception exception)
            {
                handleAssetException(exception, name, required);
            }
        }
    }

    private static void handleAssetException(Throwable e, String name, boolean required)
    {
        if (required)
        {
            throw new RuntimeException("Failed to load asset: " + name, e);
        }
        else
        {
            logger.error("Failed to load asset: {}", name);
            e.printStackTrace();
        }
    }

    public static void unpackNatives(String directory)
    {
        try
        {
            (new File("openvr/" + directory)).mkdirs();

            try
            {
                Path path = Paths.get(System.getProperty("user.dir"));
                Path path1 = path.getParent().resolve("src/resources/natives/" + directory);

                if (!path1.toFile().exists())
                {
                    path1 = path.getParent().getParent().resolve("resources/natives/" + directory);
                }

                if (path1.toFile().exists())
                {
                    logger.info("Copying {} natives...", directory);

                    for (File file1 : path1.toFile().listFiles())
                    {
                        logger.info(file1.getName());
                        Files.copy(file1, new File("openvr/" + directory + "/" + file1.getName()));
                    }

                    return;
                }
            }
            catch (Exception ignored)
            {
            }

            logger.info("Unpacking {} natives...", directory);

            Path jarPath = Xplat.getJarPath();
            boolean didExtractSomething = false;
            try (Stream<Path> natives = java.nio.file.Files.list(jarPath.resolve("natives/" + directory)))
            {
                for (Path file : natives.collect(Collectors.toCollection(ArrayList::new)))
                {
                    didExtractSomething = true;
                    logger.info(file.toString());
                    java.nio.file.Files.copy(file, new File("openvr/" + directory + "/" + file.getFileName()).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e)
            {
                logger.info("Failed to unpack natives from jar");
            }
            if (!didExtractSomething) {
                ZipFile zipfile = LoaderUtils.getVivecraftZip();
                Enumeration <? extends ZipEntry > enumeration = zipfile.entries();

                while (enumeration.hasMoreElements())
                {
                    ZipEntry zipentry = enumeration.nextElement();

                    if (zipentry.getName().startsWith("natives/" + directory))
                    {
                        String s = Paths.get(zipentry.getName()).getFileName().toString();
                        logger.info(s);
                        writeStreamToFile(zipfile.getInputStream(zipentry), new File("openvr/" + directory + "/" + s));
                    }
                }

                zipfile.close();
            }
        }
        catch (Exception exception1)
        {
            logger.error("Failed to unpack natives");
            exception1.printStackTrace();
        }
    }


    public static void writeStreamToFile(InputStream is, File file) throws IOException
    {
        FileOutputStream fileoutputstream = new FileOutputStream(file);
        byte[] abyte = new byte[4096];
        int i;

        while ((i = is.read(abyte, 0, abyte.length)) != -1)
        {
            fileoutputstream.write(abyte, 0, i);
        }

        fileoutputstream.flush();
        fileoutputstream.close();
        is.close();
    }

    public static String httpReadLine(String url) throws IOException
    {
        HttpURLConnection httpurlconnection = (HttpURLConnection)(new URL(url)).openConnection();
        httpurlconnection.setConnectTimeout(5000);
        httpurlconnection.setReadTimeout(20000);
        httpurlconnection.setUseCaches(false);
        httpurlconnection.setDoInput(true);
        BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(httpurlconnection.getInputStream()));
        String s = bufferedreader.readLine();
        bufferedreader.close();
        httpurlconnection.disconnect();
        return s;
    }

    public static List<String> httpReadAllLines(String url) throws IOException
    {
        HttpURLConnection httpurlconnection = (HttpURLConnection)(new URL(url)).openConnection();
        httpurlconnection.setConnectTimeout(5000);
        httpurlconnection.setReadTimeout(20000);
        httpurlconnection.setUseCaches(false);
        httpurlconnection.setDoInput(true);
        BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(httpurlconnection.getInputStream()));
        ArrayList<String> arraylist = new ArrayList<>();
        String s;

        while ((s = bufferedreader.readLine()) != null)
        {
            arraylist.add(s);
        }

        bufferedreader.close();
        httpurlconnection.disconnect();
        return arraylist;
    }

    public static byte[] httpReadAll(String url) throws IOException
    {
        HttpURLConnection httpurlconnection = (HttpURLConnection)(new URL(url)).openConnection();
        httpurlconnection.setConnectTimeout(5000);
        httpurlconnection.setReadTimeout(20000);
        httpurlconnection.setUseCaches(false);
        httpurlconnection.setDoInput(true);
        InputStream inputstream = httpurlconnection.getInputStream();
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream(httpurlconnection.getContentLength());
        byte[] abyte = new byte[4096];
        int i;

        while ((i = inputstream.read(abyte, 0, abyte.length)) != -1)
        {
            bytearrayoutputstream.write(abyte, 0, i);
        }

        inputstream.close();
        httpurlconnection.disconnect();
        return bytearrayoutputstream.toByteArray();
    }

    public static String httpReadAllString(String url) throws IOException
    {
        return new String(httpReadAll(url), StandardCharsets.UTF_8);
    }

    public static void httpReadToFile(String url, File file, boolean writeWhenComplete) throws IOException
    {
        HttpURLConnection httpurlconnection = (HttpURLConnection)(new URL(url)).openConnection();
        httpurlconnection.setConnectTimeout(5000);
        httpurlconnection.setReadTimeout(20000);
        httpurlconnection.setUseCaches(false);
        httpurlconnection.setDoInput(true);
        InputStream inputstream = httpurlconnection.getInputStream();

        if (writeWhenComplete)
        {
            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream(httpurlconnection.getContentLength());
            byte[] abyte = new byte[4096];
            int i;

            while ((i = inputstream.read(abyte, 0, abyte.length)) != -1)
            {
                bytearrayoutputstream.write(abyte, 0, i);
            }

            OutputStream outputstream = new FileOutputStream(file);
            outputstream.write(bytearrayoutputstream.toByteArray());
            outputstream.flush();
            outputstream.close();
        }
        else
        {
            OutputStream outputstream1 = new FileOutputStream(file);
            byte[] abyte1 = new byte[4096];
            int j;

            while ((j = inputstream.read(abyte1, 0, abyte1.length)) != -1)
            {
                outputstream1.write(abyte1, 0, j);
            }

            outputstream1.flush();
            outputstream1.close();
        }

        inputstream.close();
        httpurlconnection.disconnect();
    }

    public static void httpReadToFile(String url, File file) throws IOException
    {
        httpReadToFile(url, file, false);
    }

    public static List<String> httpReadList(String url) throws IOException
    {
        HttpURLConnection httpurlconnection = (HttpURLConnection)(new URL(url)).openConnection();
        httpurlconnection.setConnectTimeout(5000);
        httpurlconnection.setReadTimeout(20000);
        httpurlconnection.setUseCaches(false);
        httpurlconnection.setDoInput(true);
        BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(httpurlconnection.getInputStream()));
        List<String> list = new ArrayList<>();
        String s;

        while ((s = bufferedreader.readLine()) != null)
        {
            list.add(s);
        }

        bufferedreader.close();
        httpurlconnection.disconnect();
        return list;
    }

    public static String getFileChecksum(File file, String algorithm) throws IOException, NoSuchAlgorithmException
    {
        InputStream inputstream = new FileInputStream(file);
        byte[] abyte = new byte[(int)file.length()];
        inputstream.read(abyte);
        inputstream.close();
        MessageDigest messagedigest = MessageDigest.getInstance(algorithm);
        messagedigest.update(abyte);
        Formatter formatter = new Formatter();

        for (byte b0 : messagedigest.digest())
        {
            formatter.format("%02x", b0);
        }

        String s = formatter.toString();
        formatter.close();
        return s;
    }

    public static byte[] readFile(File file) throws IOException
    {
        FileInputStream fileinputstream = new FileInputStream(file);
        return readFully(fileinputstream);
    }

    public static String readFileString(File file) throws IOException
    {
        return new String(readFile(file), StandardCharsets.UTF_8);
    }

    public static byte[] readFully(InputStream in) throws IOException
    {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        byte[] abyte = new byte[4096];
        int i;

        while ((i = in.read(abyte, 0, abyte.length)) != -1)
        {
            bytearrayoutputstream.write(abyte, 0, i);
        }

        in.close();
        return bytearrayoutputstream.toByteArray();
    }

    public static Vec3 vecLerp(Vec3 start, Vec3 end, double fraction)
    {
        double d0 = start.x + (end.x - start.x) * fraction;
        double d1 = start.y + (end.y - start.y) * fraction;
        double d2 = start.z + (end.z - start.z) * fraction;
        return new Vec3(d0, d1, d2);
    }

    public static float applyDeadzone(float axis, float deadzone)
    {
        float f = 1.0F / (1.0F - deadzone);
        float f1 = 0.0F;

        if (abs(axis) > deadzone)
        {
            f1 = (abs(axis) - deadzone) * f * signum(axis);
        }

        return f1;
    }

    public static void spawnParticles(ParticleOptions type, int count, Vec3 position, Vec3 size, double speed)
    {
        for (int i = 0; i < count; ++i)
        {
            double d0 = avRandomizer.nextGaussian() * size.x;
            double d1 = avRandomizer.nextGaussian() * size.y;
            double d2 = avRandomizer.nextGaussian() * size.z;
            double d3 = avRandomizer.nextGaussian() * speed;
            double d4 = avRandomizer.nextGaussian() * speed;
            double d5 = avRandomizer.nextGaussian() * speed;

            try
            {
                mc.level.addParticle(type, position.x + d0, position.y + d1, position.z + d2, d3, d4, d5);
            }
            catch (Throwable throwable)
            {
                logger.warn("Could not spawn particle effect {}", type);
                return;
            }
        }
    }

    public static int getCombinedLightWithMin(BlockAndTintGetter lightReader, BlockPos pos, int minLight)
    {
        int i = LevelRenderer.getLightColor(lightReader, pos);
        int j = i >> 4 & 15;

        if (j < minLight)
        {
            i = i & -256;
            i = i | minLight << 4;
        }

        return i;
    }

    public static void takeScreenshot(RenderTarget fb)
    {
        Screenshot.grab(mc.gameDirectory, fb, (text) ->
        {
            mc.execute(() -> {
                mc.gui.getChat().addMessage(text);
            });
        });
    }

    public static List<FormattedText> wrapText(FormattedText text, int width, Font fontRenderer, @Nullable FormattedText linePrefix)
    {
        ComponentCollector componentcollector = new ComponentCollector();
        text.visit((style, str) ->
        {
            componentcollector.append(FormattedText.of(str, style));
            return Optional.empty();
        }, Style.EMPTY);
        List<FormattedText> list = Lists.newArrayList();
        fontRenderer.getSplitter().splitLines(componentcollector.getResultOrEmpty(), width, Style.EMPTY, (lineText, sameLine) ->
        {
            list.add(sameLine && linePrefix != null ? FormattedText.composite(linePrefix, lineText) : lineText);
        });
        return list.isEmpty() ? Lists.newArrayList(FormattedText.EMPTY) : list;
    }

    public static List<ChatFormatting> styleToFormats(Style style)
    {
        if (style.isEmpty())
        {
            return new ArrayList<>();
        }
        else
        {
            ArrayList<ChatFormatting> arraylist = new ArrayList<>();

            if (style.getColor() != null)
            {
                arraylist.add(ChatFormatting.getByName(style.getColor().serialize()));
            }

            if (style.isBold())
            {
                arraylist.add(ChatFormatting.BOLD);
            }

            if (style.isItalic())
            {
                arraylist.add(ChatFormatting.ITALIC);
            }

            if (style.isStrikethrough())
            {
                arraylist.add(ChatFormatting.STRIKETHROUGH);
            }

            if (style.isUnderlined())
            {
                arraylist.add(ChatFormatting.UNDERLINE);
            }

            if (style.isObfuscated())
            {
                arraylist.add(ChatFormatting.OBFUSCATED);
            }

            return arraylist;
        }
    }

    public static String formatsToString(List<ChatFormatting> formats)
    {
        if (formats.size() == 0)
        {
            return "";
        }
        else
        {
            StringBuilder stringbuilder = new StringBuilder();
            formats.forEach(stringbuilder::append);
            return stringbuilder.toString();
        }
    }

    public static String styleToFormatString(Style style)
    {
        return formatsToString(styleToFormats(style));
    }

    static
    {
        Arrays.sort(illegalChars);
    }
}
