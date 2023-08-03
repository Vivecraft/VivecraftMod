package org.vivecraft.client.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.openvr.HmdMatrix44;
import org.vivecraft.client.Xplat;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_vr.utils.LoaderUtils;
import org.vivecraft.common.utils.math.Quaternion;
import org.vivecraft.common.utils.math.Vector2;
import org.vivecraft.common.utils.math.Vector3;
import org.vivecraft.common.utils.lwjgl.Matrix3f;
import org.vivecraft.common.utils.lwjgl.Matrix4f;
import org.vivecraft.common.utils.lwjgl.Vector2f;
import org.vivecraft.common.utils.lwjgl.Vector3f;
import org.vivecraft.common.utils.lwjgl.Vector4f;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.mojang.blaze3d.pipeline.RenderTarget;

import net.minecraft.ChatFormatting;
import net.minecraft.client.ComponentCollector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.phys.Vec3;

public class Utils
{
    private static final char[] illegalChars = new char[] {'"', '<', '>', '|', '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005', '\u0006', '\u0007', '\b', '\t', '\n', '\u000b', '\f', '\r', '\u000e', '\u000f', '\u0010', '\u0011', '\u0012', '\u0013', '\u0014', '\u0015', '\u0016', '\u0017', '\u0018', '\u0019', '\u001a', '\u001b', '\u001c', '\u001d', '\u001e', '\u001f', ':', '*', '?', '\\', '/'};
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 20000;
    private static final Random avRandomizer = new Random();

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

    public static Vector3 convertToOVRVector(Vector3f vector)
    {
        return new Vector3(vector.x, vector.y, vector.z);
    }

    public static Vector3 convertToOVRVector(Vec3 vector)
    {
        return new Vector3((float)vector.x, (float)vector.y, (float)vector.z);
    }

    public static Matrix4f convertOVRMatrix(org.vivecraft.common.utils.math.Matrix4f matrix)
    {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.m00 = matrix.M[0][0];
        matrix4f.m01 = matrix.M[0][1];
        matrix4f.m02 = matrix.M[0][2];
        matrix4f.m03 = matrix.M[0][3];
        matrix4f.m10 = matrix.M[1][0];
        matrix4f.m11 = matrix.M[1][1];
        matrix4f.m12 = matrix.M[1][2];
        matrix4f.m13 = matrix.M[1][3];
        matrix4f.m20 = matrix.M[2][0];
        matrix4f.m21 = matrix.M[2][1];
        matrix4f.m22 = matrix.M[2][2];
        matrix4f.m23 = matrix.M[2][3];
        matrix4f.m30 = matrix.M[3][0];
        matrix4f.m31 = matrix.M[3][1];
        matrix4f.m32 = matrix.M[3][2];
        matrix4f.m33 = matrix.M[3][3];
        matrix4f.transpose(matrix4f);
        return matrix4f;
    }

    public static org.vivecraft.common.utils.math.Matrix4f convertToOVRMatrix(Matrix4f matrixIn)
    {
        Matrix4f matrix4f = new Matrix4f();
        matrixIn.transpose(matrix4f);
        org.vivecraft.common.utils.math.Matrix4f matrix4f1 = new org.vivecraft.common.utils.math.Matrix4f();
        matrix4f1.M[0][0] = matrix4f.m00;
        matrix4f1.M[0][1] = matrix4f.m01;
        matrix4f1.M[0][2] = matrix4f.m02;
        matrix4f1.M[0][3] = matrix4f.m03;
        matrix4f1.M[1][0] = matrix4f.m10;
        matrix4f1.M[1][1] = matrix4f.m11;
        matrix4f1.M[1][2] = matrix4f.m12;
        matrix4f1.M[1][3] = matrix4f.m13;
        matrix4f1.M[2][0] = matrix4f.m20;
        matrix4f1.M[2][1] = matrix4f.m21;
        matrix4f1.M[2][2] = matrix4f.m22;
        matrix4f1.M[2][3] = matrix4f.m23;
        matrix4f1.M[3][0] = matrix4f.m30;
        matrix4f1.M[3][1] = matrix4f.m31;
        matrix4f1.M[3][2] = matrix4f.m32;
        matrix4f1.M[3][3] = matrix4f.m33;
        return matrix4f1;
    }

    public static double lerp(double from, double to, double percent)
    {
        return from + (to - from) * percent;
    }

    public static double lerpMod(double from, double to, double percent, double mod)
    {
        return Math.abs(to - from) < mod / 2.0D ? from + (to - from) * percent : from + (to - from - Math.signum(to - from) * mod) * percent;
    }

    public static double absLerp(double value, double target, double stepSize)
    {
        double d0 = Math.abs(stepSize);

        if (target - value > d0)
        {
            return value + d0;
        }
        else
        {
            return target - value < -d0 ? value - d0 : target;
        }
    }

    public static float angleDiff(float a, float b)
    {
        float f = Math.abs(a - b) % 360.0F;
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

    public static Vector3f directionFromMatrix(Matrix4f matrix, float x, float y, float z)
    {
        Vector4f vector4f = new Vector4f(x, y, z, 0.0F);
        Matrix4f.transform(matrix, vector4f, vector4f);
        vector4f.normalise(vector4f);
        return new Vector3f(vector4f.x, vector4f.y, vector4f.z);
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
            int i = Math.max(Math.max(in.lastIndexOf(" ", length), in.lastIndexOf("\t", length)), in.lastIndexOf("-", length));

            if (i == -1)
            {
                i = length;
            }

            String s1 = in.substring(0, i).trim();
            wrapped.add(s1);
            wordWrap(in.substring(i), length, wrapped);
        }
    }

    public static Vector2f convertVector(Vector2 vector)
    {
        return new Vector2f(vector.getX(), vector.getY());
    }

    public static Vector2 convertVector(Vector2f vector)
    {
        return new Vector2(vector.getX(), vector.getY());
    }

    public static Vector3f convertVector(Vector3 vector)
    {
        return new Vector3f(vector.getX(), vector.getY(), vector.getZ());
    }

    public static Vector3 convertVector(Vector3f vector)
    {
        return new Vector3(vector.getX(), vector.getY(), vector.getZ());
    }

    public static Vector3 convertVector(Vec3 vector)
    {
        return new Vector3((float)vector.x, (float)vector.y, (float)vector.z);
    }

    public static Vector3f convertToVector3f(Vec3 vector)
    {
        return new Vector3f((float)vector.x, (float)vector.y, (float)vector.z);
    }

    public static Vec3 convertToVector3d(Vector3 vector)
    {
        return new Vec3((double)vector.getX(), (double)vector.getY(), (double)vector.getZ());
    }

    public static Vec3 convertToVector3d(Vector3f vector)
    {
        return new Vec3((double)vector.x, (double)vector.y, (double)vector.z);
    }

    public static Vector3f transformVector(Matrix4f matrix, Vector3f vector, boolean point)
    {
        Vector4f vector4f = Matrix4f.transform(matrix, new Vector4f(vector.x, vector.y, vector.z, point ? 1.0F : 0.0F), (Vector4f)null);
        return new Vector3f(vector4f.x, vector4f.y, vector4f.z);
    }

    public static Quaternion quatLerp(Quaternion start, Quaternion end, float fraction)
    {
        Quaternion quaternion = new Quaternion();
        quaternion.w = start.w + (end.w - start.w) * fraction;
        quaternion.x = start.x + (end.x - start.x) * fraction;
        quaternion.y = start.y + (end.y - start.y) * fraction;
        quaternion.z = start.z + (end.z - start.z) * fraction;
        return quaternion;
    }

    public static Matrix4f matrix3to4(Matrix3f matrix)
    {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.m00 = matrix.m00;
        matrix4f.m01 = matrix.m01;
        matrix4f.m02 = matrix.m02;
        matrix4f.m10 = matrix.m10;
        matrix4f.m11 = matrix.m11;
        matrix4f.m12 = matrix.m12;
        matrix4f.m20 = matrix.m20;
        matrix4f.m21 = matrix.m21;
        matrix4f.m22 = matrix.m22;
        return matrix4f;
    }

    public static InputStream getAssetAsStream(String name, boolean required)
    {
        InputStream inputstream = null;

        try
        {
            try
            {
                Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation("vivecraft", name));
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
        return abyte == null ? null : new String(abyte, Charsets.UTF_8);
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
            System.out.println("Failed to load asset: " + name);
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
                    System.out.println("Copying " + directory + " natives...");

                    for (File file1 : path1.toFile().listFiles())
                    {
                        System.out.println(file1.getName());
                        Files.copy(file1, new File("openvr/" + directory + "/" + file1.getName()));
                    }

                    return;
                }
            }
            catch (Exception exception)
            {
            }

            System.out.println("Unpacking " + directory + " natives...");

            Path jarPath = Xplat.getJarPath();
            boolean didExtractSomething = false;
            try (Stream<Path> natives = java.nio.file.Files.list(jarPath.resolve("natives/" + directory)))
            {
                for (Path file : natives.collect(Collectors.toCollection(ArrayList::new)))
                {
                    didExtractSomething = true;
                    System.out.println(file);
                    java.nio.file.Files.copy(file, new File("openvr/" + directory + "/" + file.getFileName()).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e)
            {
                System.out.println("Failed to unpack natives from jar");
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
                        System.out.println(s);
                        writeStreamToFile(zipfile.getInputStream(zipentry), new File("openvr/" + directory + "/" + s));
                    }
                }

                zipfile.close();
            }
        }
        catch (Exception exception1)
        {
            System.out.println("Failed to unpack natives");
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

    public static void httpReadToFile(String url, File file, boolean writeWhenComplete) throws MalformedURLException, IOException
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

    public static String getFileChecksum(File file, String algorithm) throws FileNotFoundException, IOException, NoSuchAlgorithmException
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

    public static byte[] readFile(File file) throws FileNotFoundException, IOException
    {
        FileInputStream fileinputstream = new FileInputStream(file);
        return readFully(fileinputstream);
    }

    public static String readFileString(File file) throws FileNotFoundException, IOException
    {
        return new String(readFile(file), "UTF-8");
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

    public static Quaternion slerp(Quaternion start, Quaternion end, float alpha)
    {
        float f = start.x * end.x + start.y * end.y + start.z * end.z + start.w * end.w;
        float f1 = f < 0.0F ? -f : f;
        float f2 = 1.0F - alpha;
        float f3 = alpha;

        if ((double)(1.0F - f1) > 0.1D)
        {
            float f4 = (float)Math.acos((double)f1);
            float f5 = 1.0F / (float)Math.sin((double)f4);
            f2 = (float)Math.sin((double)((1.0F - alpha) * f4)) * f5;
            f3 = (float)Math.sin((double)(alpha * f4)) * f5;
        }

        if (f < 0.0F)
        {
            f3 = -f3;
        }

        float f8 = f2 * start.x + f3 * end.x;
        float f9 = f2 * start.y + f3 * end.y;
        float f6 = f2 * start.z + f3 * end.z;
        float f7 = f2 * start.w + f3 * end.w;
        return new Quaternion(f7, f8, f9, f6);
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

        if (Math.abs(axis) > deadzone)
        {
            f1 = (Math.abs(axis) - deadzone) * f * Math.signum(axis);
        }

        return f1;
    }

    public static void spawnParticles(ParticleOptions type, int count, Vec3 position, Vec3 size, double speed)
    {
        Minecraft minecraft = Minecraft.getInstance();

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
                minecraft.level.addParticle(type, position.x + d0, position.y + d1, position.z + d2, d3, d4, d5);
            }
            catch (Throwable throwable)
            {
                LogManager.getLogger().warn("Could not spawn particle effect {}", (Object)type);
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
        Minecraft minecraft = Minecraft.getInstance();
        Screenshot.grab(minecraft.gameDirectory, fb, (text) ->
        {
            minecraft.execute(() -> {
                minecraft.gui.getChat().addMessage(text);
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
        return (List<FormattedText>)(list.isEmpty() ? Lists.newArrayList(FormattedText.EMPTY) : list);
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

    public static long microTime()
    {
        return System.nanoTime() / 1000L;
    }

    public static long milliTime()
    {
        return System.nanoTime() / 1000000L;
    }

    public static void printStackIfContainsClass(String className)
    {
        StackTraceElement[] astacktraceelement = Thread.currentThread().getStackTrace();
        boolean flag = false;

        for (StackTraceElement stacktraceelement : astacktraceelement)
        {
            if (stacktraceelement.getClassName().equals(className))
            {
                flag = true;
                break;
            }
        }

        if (flag)
        {
            Thread.dumpStack();
        }
    }

    public static org.joml.Matrix4f Matrix4fFromOpenVR(HmdMatrix44 in)
    {
        return new org.joml.Matrix4f(in.m(0), in.m(4), in.m(8), in.m(12),
                in.m(1), in.m(5),  in.m(9), in.m(13),
                in.m(2), in.m(6), in.m(10), in.m(14),
                in.m(3), in.m(7), in.m(11), in.m(15));
    }

    public static Quaternion convertMatrix4ftoRotationQuat(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22)
    {
        double d0 = (double)(m00 * m00 + m10 * m10 + m20 * m20);

        if (d0 != 1.0D && d0 != 0.0D)
        {
            d0 = 1.0D / Math.sqrt(d0);
            m00 = (float)((double)m00 * d0);
            m10 = (float)((double)m10 * d0);
            m20 = (float)((double)m20 * d0);
        }

        d0 = (double)(m01 * m01 + m11 * m11 + m21 * m21);

        if (d0 != 1.0D && d0 != 0.0D)
        {
            d0 = 1.0D / Math.sqrt(d0);
            m01 = (float)((double)m01 * d0);
            m11 = (float)((double)m11 * d0);
            m21 = (float)((double)m21 * d0);
        }

        d0 = (double)(m02 * m02 + m12 * m12 + m22 * m22);

        if (d0 != 1.0D && d0 != 0.0D)
        {
            d0 = 1.0D / Math.sqrt(d0);
            m02 = (float)((double)m02 * d0);
            m12 = (float)((double)m12 * d0);
            m22 = (float)((double)m22 * d0);
        }

        float f = m00 + m11 + m22;
        Quaternion quaternion = new Quaternion();

        if (f >= 0.0F)
        {
            double d1 = Math.sqrt((double)(f + 1.0F));
            quaternion.w = (float)(0.5D * d1);
            d1 = 0.5D / d1;
            quaternion.x = (float)((double)(m21 - m12) * d1);
            quaternion.y = (float)((double)(m02 - m20) * d1);
            quaternion.z = (float)((double)(m10 - m01) * d1);
        }
        else if (m00 > m11 && m00 > m22)
        {
            double d4 = Math.sqrt(1.0D + (double)m00 - (double)m11 - (double)m22);
            quaternion.x = (float)(d4 * 0.5D);
            d4 = 0.5D / d4;
            quaternion.y = (float)((double)(m10 + m01) * d4);
            quaternion.z = (float)((double)(m02 + m20) * d4);
            quaternion.w = (float)((double)(m21 - m12) * d4);
        }
        else if (m11 > m22)
        {
            double d2 = Math.sqrt(1.0D + (double)m11 - (double)m00 - (double)m22);
            quaternion.y = (float)(d2 * 0.5D);
            d2 = 0.5D / d2;
            quaternion.x = (float)((double)(m10 + m01) * d2);
            quaternion.z = (float)((double)(m21 + m12) * d2);
            quaternion.w = (float)((double)(m02 - m20) * d2);
        }
        else
        {
            double d3 = Math.sqrt(1.0D + (double)m22 - (double)m00 - (double)m11);
            quaternion.z = (float)(d3 * 0.5D);
            d3 = 0.5D / d3;
            quaternion.x = (float)((double)(m02 + m20) * d3);
            quaternion.y = (float)((double)(m21 + m12) * d3);
            quaternion.w = (float)((double)(m10 - m01) * d3);
        }

        return quaternion;
    }

    public static org.vivecraft.common.utils.math.Matrix4f rotationXMatrix(float angle)
    {
        float f = (float)Math.sin((double)angle);
        float f1 = (float)Math.cos((double)angle);
        return new org.vivecraft.common.utils.math.Matrix4f(1.0F, 0.0F, 0.0F, 0.0F, f1, -f, 0.0F, f, f1);
    }

    public static org.vivecraft.common.utils.math.Matrix4f rotationZMatrix(float angle)
    {
        float f = (float)Math.sin((double)angle);
        float f1 = (float)Math.cos((double)angle);
        return new org.vivecraft.common.utils.math.Matrix4f(f1, -f, 0.0F, f, f1, 0.0F, 0.0F, 0.0F, 1.0F);
    }

    public static Vector3 convertMatrix4ftoTranslationVector(org.vivecraft.common.utils.math.Matrix4f mat)
    {
        return new Vector3(mat.M[0][3], mat.M[1][3], mat.M[2][3]);
    }

    public static void Matrix4fSet(org.vivecraft.common.utils.math.Matrix4f mat, float m11, float m12, float m13, float m14, float m21, float m22, float m23, float m24, float m31, float m32, float m33, float m34, float m41, float m42, float m43, float m44)
    {
        mat.M[0][0] = m11;
        mat.M[0][1] = m12;
        mat.M[0][2] = m13;
        mat.M[0][3] = m14;
        mat.M[1][0] = m21;
        mat.M[1][1] = m22;
        mat.M[1][2] = m23;
        mat.M[1][3] = m24;
        mat.M[2][0] = m31;
        mat.M[2][1] = m32;
        mat.M[2][2] = m33;
        mat.M[2][3] = m34;
        mat.M[3][0] = m41;
        mat.M[3][1] = m42;
        mat.M[3][2] = m43;
        mat.M[3][3] = m44;
    }

    public static void Matrix4fCopy(org.vivecraft.common.utils.math.Matrix4f source, org.vivecraft.common.utils.math.Matrix4f dest)
    {
        dest.M[0][0] = source.M[0][0];
        dest.M[0][1] = source.M[0][1];
        dest.M[0][2] = source.M[0][2];
        dest.M[0][3] = source.M[0][3];
        dest.M[1][0] = source.M[1][0];
        dest.M[1][1] = source.M[1][1];
        dest.M[1][2] = source.M[1][2];
        dest.M[1][3] = source.M[1][3];
        dest.M[2][0] = source.M[2][0];
        dest.M[2][1] = source.M[2][1];
        dest.M[2][2] = source.M[2][2];
        dest.M[2][3] = source.M[2][3];
        dest.M[3][0] = source.M[3][0];
        dest.M[3][1] = source.M[3][1];
        dest.M[3][2] = source.M[3][2];
        dest.M[3][3] = source.M[3][3];
    }

    public static org.vivecraft.common.utils.math.Matrix4f Matrix4fSetIdentity(org.vivecraft.common.utils.math.Matrix4f mat)
    {
        mat.M[0][0] = mat.M[1][1] = mat.M[2][2] = mat.M[3][3] = 1.0F;
        mat.M[0][1] = mat.M[1][0] = mat.M[2][3] = mat.M[3][1] = 0.0F;
        mat.M[0][2] = mat.M[1][2] = mat.M[2][0] = mat.M[3][2] = 0.0F;
        mat.M[0][3] = mat.M[1][3] = mat.M[2][1] = mat.M[3][0] = 0.0F;
        return mat;
    }

    static
    {
        Arrays.sort(illegalChars);
    }
}
