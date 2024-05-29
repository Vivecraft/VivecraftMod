package org.vivecraft.client.utils;

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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.IOUtils;
import org.lwjgl.openvr.HmdMatrix44;
import org.vivecraft.client.Xplat;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_vr.utils.LoaderUtils;
import org.vivecraft.common.utils.lwjgl.*;
import org.vivecraft.common.utils.math.Quaternion;
import org.vivecraft.common.utils.math.Vector2;
import org.vivecraft.common.utils.math.Vector3;

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

public class Utils {
    private static final char[] illegalChars = new char[]{'"', '<', '>', '|', '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005', '\u0006', '\u0007', '\b', '\t', '\n', '\u000b', '\f', '\r', '\u000e', '\u000f', '\u0010', '\u0011', '\u0012', '\u0013', '\u0014', '\u0015', '\u0016', '\u0017', '\u0018', '\u0019', '\u001a', '\u001b', '\u001c', '\u001d', '\u001e', '\u001f', ':', '*', '?', '\\', '/'};
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 20000;
    private static final Random avRandomizer = new Random();

    static {
        // Needs to be sorted for binary search
        Arrays.sort(illegalChars);
    }

    public static String sanitizeFileName(String fileName) {
        StringBuilder sanitized = new StringBuilder();

        for (int i = 0; i < fileName.length(); i++) {
            char ch = fileName.charAt(i);

            if (Arrays.binarySearch(illegalChars, ch) < 0) {
                sanitized.append(ch);
            } else {
                sanitized.append('_');
            }
        }
        return sanitized.toString();
    }

    public static Vector3 convertToOVRVector(Vector3f vector) {
        return new Vector3(vector.x, vector.y, vector.z);
    }

    public static Vector3 convertToOVRVector(Vec3 vector) {
        return new Vector3((float) vector.x, (float) vector.y, (float) vector.z);
    }

    public static Matrix4f convertOVRMatrix(org.vivecraft.common.utils.math.Matrix4f matrix) {
        Matrix4f mat = new Matrix4f();
        mat.m00 = matrix.M[0][0];
        mat.m01 = matrix.M[0][1];
        mat.m02 = matrix.M[0][2];
        mat.m03 = matrix.M[0][3];
        mat.m10 = matrix.M[1][0];
        mat.m11 = matrix.M[1][1];
        mat.m12 = matrix.M[1][2];
        mat.m13 = matrix.M[1][3];
        mat.m20 = matrix.M[2][0];
        mat.m21 = matrix.M[2][1];
        mat.m22 = matrix.M[2][2];
        mat.m23 = matrix.M[2][3];
        mat.m30 = matrix.M[3][0];
        mat.m31 = matrix.M[3][1];
        mat.m32 = matrix.M[3][2];
        mat.m33 = matrix.M[3][3];
        mat.transpose(mat);
        return mat;
    }

    public static org.vivecraft.common.utils.math.Matrix4f convertToOVRMatrix(Matrix4f matrixIn) {
        Matrix4f matrix = new Matrix4f();
        matrixIn.transpose(matrix);
        org.vivecraft.common.utils.math.Matrix4f mat = new org.vivecraft.common.utils.math.Matrix4f();
        mat.M[0][0] = matrix.m00;
        mat.M[0][1] = matrix.m01;
        mat.M[0][2] = matrix.m02;
        mat.M[0][3] = matrix.m03;
        mat.M[1][0] = matrix.m10;
        mat.M[1][1] = matrix.m11;
        mat.M[1][2] = matrix.m12;
        mat.M[1][3] = matrix.m13;
        mat.M[2][0] = matrix.m20;
        mat.M[2][1] = matrix.m21;
        mat.M[2][2] = matrix.m22;
        mat.M[2][3] = matrix.m23;
        mat.M[3][0] = matrix.m30;
        mat.M[3][1] = matrix.m31;
        mat.M[3][2] = matrix.m32;
        mat.M[3][3] = matrix.m33;
        return mat;
    }

    public static double lerp(double from, double to, double percent) {
        return from + (to - from) * percent;
    }

    public static double lerpMod(double from, double to, double percent, double mod) {
        return Math.abs(to - from) < mod / 2.0D ?
            from + (to - from) * percent :
            from + (to - from - Math.signum(to - from) * mod) * percent;
    }

    public static double absLerp(double value, double target, double stepSize) {
        double step = Math.abs(stepSize);

        if (target - value > step) {
            return value + step;
        } else if (target - value < -step) {
            return value - step;
        } else {
            return target;
        }
    }

    public static float angleDiff(float a, float b) {
        float d = Math.abs(a - b) % 360.0F;
        float r = d > 180.0F ? 360.0F - d : d;

        float diff = a - b;
        int sign = (diff >= 0.0F && diff <= 180.0F) || (diff <= -180.0F && diff >= -360.0F) ? 1 : -1;

        return r * sign;
    }

    public static float angleNormalize(float angle) {
        angle = angle % 360.0F;

        if (angle < 0.0F) {
            angle += 360.0F;
        }

        return angle;
    }

    public static Vector3f directionFromMatrix(Matrix4f matrix, float x, float y, float z) {
        Vector4f vec = new Vector4f(x, y, z, 0.0F);
        Matrix4f.transform(matrix, vec, vec);
        vec.normalise(vec);
        return new Vector3f(vec.x, vec.y, vec.z);
    }

    /* With thanks to http://ramblingsrobert.wordpress.com/2011/04/13/java-word-wrap-algorithm/ */
    public static void wordWrap(String in, int length, ArrayList<String> wrapped) {
        // can't wrap with length 0, so return the original string
        if (length == 0) {
            wrapped.add(in);
            return;
        }
        String newLine = "\n";
        boolean quickExit = false;

        // Remove carriage return
        in = in.replace("\r", "");

        if (in.length() < length) {
            quickExit = true;
            length = in.length();
        }

        // Split on a newline if present
        if (in.substring(0, length).contains(newLine)) {
            String wrappedLine = in.substring(0, in.indexOf(newLine)).trim();
            wrapped.add(wrappedLine);
            wordWrap(in.substring(in.indexOf(newLine) + 1), length, wrapped);
        } else if (quickExit) {
            wrapped.add(in);
        } else {
            // Otherwise, split along the nearest previous space / tab / dash
            int spaceIndex = Math.max(Math.max(in.lastIndexOf(" ", length),
                in.lastIndexOf("\t", length)),
                in.lastIndexOf("-", length));

            // If no nearest space, split at length
            if (spaceIndex == -1) {
                spaceIndex = length;
            }

            // Split!
            String wrappedLine = in.substring(0, spaceIndex).trim();
            wrapped.add(wrappedLine);
            wordWrap(in.substring(spaceIndex), length, wrapped);
        }
    }

    public static Vector2f convertVector(Vector2 vector) {
        return new Vector2f(vector.getX(), vector.getY());
    }

    public static Vector2 convertVector(Vector2f vector) {
        return new Vector2(vector.getX(), vector.getY());
    }

    public static Vector3f convertVector(Vector3 vector) {
        return new Vector3f(vector.getX(), vector.getY(), vector.getZ());
    }

    public static Vector3 convertVector(Vector3f vector) {
        return new Vector3(vector.getX(), vector.getY(), vector.getZ());
    }

    public static Vector3 convertVector(Vec3 vector) {
        return new Vector3((float) vector.x, (float) vector.y, (float) vector.z);
    }

    public static Vector3f convertToVector3f(Vec3 vector) {
        return new Vector3f((float) vector.x, (float) vector.y, (float) vector.z);
    }

    public static Vec3 convertToVector3d(Vector3 vector) {
        return new Vec3(vector.getX(), vector.getY(), vector.getZ());
    }

    public static Vec3 convertToVector3d(Vector3f vector) {
        return new Vec3(vector.x, vector.y, vector.z);
    }

    public static Vector3f transformVector(Matrix4f matrix, Vector3f vector, boolean point) {
        Vector4f vec = Matrix4f.transform(matrix, new Vector4f(vector.x, vector.y, vector.z, point ? 1.0F : 0.0F), null);
        return new Vector3f(vec.x, vec.y, vec.z);
    }

    public static Quaternion quatLerp(Quaternion start, Quaternion end, float fraction) {
        Quaternion quat = new Quaternion();
        quat.w = start.w + (end.w - start.w) * fraction;
        quat.x = start.x + (end.x - start.x) * fraction;
        quat.y = start.y + (end.y - start.y) * fraction;
        quat.z = start.z + (end.z - start.z) * fraction;
        return quat;
    }

    public static Matrix4f matrix3to4(Matrix3f matrix) {
        Matrix4f mat = new Matrix4f();
        mat.m00 = matrix.m00;
        mat.m01 = matrix.m01;
        mat.m02 = matrix.m02;
        mat.m10 = matrix.m10;
        mat.m11 = matrix.m11;
        mat.m12 = matrix.m12;
        mat.m20 = matrix.m20;
        mat.m21 = matrix.m21;
        mat.m22 = matrix.m22;
        return mat;
    }

    public static InputStream getAssetAsStream(String name, boolean required) {
        InputStream is = null;

        try {
            try {
                Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation("vivecraft", name));
                if (resource.isPresent()) {
                    is = resource.get().open();
                }
            } catch (NullPointerException | FileNotFoundException fileNotFoundException) {
                // might be called super early
                is = VRShaders.class.getResourceAsStream("/assets/vivecraft/" + name);
            }

            if (is == null) {
                //uhh debugging?
                Path dir = Paths.get(System.getProperty("user.dir"));

                if (dir.getParent() != null) {
                    Path path = dir.getParent().resolve("src/resources/assets/vivecraft/" + name);

                    if (!path.toFile().exists() && dir.getParent().getParent() != null) {
                        path = dir.getParent().getParent().resolve("resources/assets/vivecraft/" + name);
                    }

                    if (path.toFile().exists()) {
                        is = new FileInputStream(path.toFile());
                    }
                }
            }
        } catch (Exception exception) {
            handleAssetException(exception, name, required);
            return null;
        }

        if (is == null) {
            handleAssetException(new FileNotFoundException(name), name, required);
        }

        return is;
    }

    public static byte[] loadAsset(String name, boolean required) {
        InputStream is = getAssetAsStream(name, required);

        if (is == null) {
            return null;
        } else {
            try {
                byte[] out = IOUtils.toByteArray(is);
                is.close();
                return out;
            } catch (Exception exception) {
                handleAssetException(exception, name, required);
                return null;
            }
        }
    }

    public static String loadAssetAsString(String name, boolean required) {
        byte[] bytes = loadAsset(name, required);
        return bytes == null ? null : new String(bytes, Charsets.UTF_8);
    }

    public static void loadAssetToFile(String name, File file, boolean required) {
        InputStream is = getAssetAsStream(name, required);

        if (is != null) {
            try {
                writeStreamToFile(is, file);
                is.close();
            } catch (Exception exception) {
                handleAssetException(exception, name, required);
            }
        }
    }

    private static void handleAssetException(Throwable e, String name, boolean required) {
        if (required) {
            throw new RuntimeException("Failed to load asset: " + name, e);
        } else {
            VRSettings.logger.error("Failed to load asset: {}", name);
            e.printStackTrace();
        }
    }

    public static void unpackNatives(String directory) {
        try {
            (new File("openvr/" + directory)).mkdirs();

            // dev environment
            try {
                Path path = Paths.get(System.getProperty("user.dir"));
                Path path1 = path.getParent().resolve("src/resources/natives/" + directory);

                if (!path1.toFile().exists()) {
                    path1 = path.getParent().getParent().resolve("resources/natives/" + directory);
                }

                if (path1.toFile().exists()) {
                    VRSettings.logger.info("Copying '{}' natives...", directory);

                    for (File file1 : path1.toFile().listFiles()) {
                        VRSettings.logger.info("copying: {}", file1.getName());
                        Files.copy(file1, new File("openvr/" + directory + "/" + file1.getName()));
                    }

                    return;
                }
            } catch (Exception exception) {
            }
            //

            //Live
            VRSettings.logger.info("Unpacking '{}' natives...", directory);

            Path jarPath = Xplat.getJarPath();
            boolean didExtractSomething = false;
            try (Stream<Path> natives = java.nio.file.Files.list(jarPath.resolve("natives/" + directory))) {
                for (Path file : natives.collect(Collectors.toCollection(ArrayList::new))) {
                    didExtractSomething = true;
                    VRSettings.logger.info("extracting: {}", file);
                    java.nio.file.Files.copy(file, new File("openvr/" + directory + "/" + file.getFileName()).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                VRSettings.logger.error("Failed to unpack natives from jar");
            }
            if (!didExtractSomething) {
                ZipFile zipfile = LoaderUtils.getVivecraftZip();
                Enumeration<? extends ZipEntry> enumeration = zipfile.entries();

                while (enumeration.hasMoreElements()) {
                    ZipEntry zipentry = enumeration.nextElement();

                    if (zipentry.getName().startsWith("natives/" + directory)) {
                        String s = Paths.get(zipentry.getName()).getFileName().toString();
                        VRSettings.logger.info("extracting: {}", s);
                        writeStreamToFile(zipfile.getInputStream(zipentry), new File("openvr/" + directory + "/" + s));
                    }
                }

                zipfile.close();
            }
            //
        } catch (Exception exception1) {
            System.out.println("Failed to unpack natives");
            exception1.printStackTrace();
        }
    }


    public static void writeStreamToFile(InputStream is, File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buffer = new byte[4096];
        int count;

        while ((count = is.read(buffer, 0, buffer.length)) != -1) {
            fos.write(buffer, 0, count);
        }

        fos.flush();
        fos.close();
        is.close();
    }

    public static String httpReadLine(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setUseCaches(false);
        conn.setDoInput(true);
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = br.readLine();
        br.close();
        conn.disconnect();
        return line;
    }

    public static List<String> httpReadAllLines(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setUseCaches(false);
        conn.setDoInput(true);
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        List<String> list = new ArrayList<>();
        String line;

        while ((line = br.readLine()) != null) {
            list.add(line);
        }

        br.close();
        conn.disconnect();
        return list;
    }

    public static byte[] httpReadAll(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setUseCaches(false);
        conn.setDoInput(true);
        InputStream is = conn.getInputStream();
        ByteArrayOutputStream bout = new ByteArrayOutputStream(conn.getContentLength());
        byte[] bytes = new byte[4096];
        int count;

        while ((count = is.read(bytes, 0, bytes.length)) != -1) {
            bout.write(bytes, 0, count);
        }

        is.close();
        conn.disconnect();
        return bout.toByteArray();
    }

    public static String httpReadAllString(String url) throws IOException {
        return new String(httpReadAll(url), StandardCharsets.UTF_8);
    }

    public static void httpReadToFile(String url, File file, boolean writeWhenComplete) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(20000);
        conn.setUseCaches(false);
        conn.setDoInput(true);
        InputStream is = conn.getInputStream();

        if (writeWhenComplete) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(conn.getContentLength());
            byte[] bytes = new byte[4096];
            int count;

            while ((count = is.read(bytes, 0, bytes.length)) != -1) {
                bout.write(bytes, 0, count);
            }

            OutputStream out = new FileOutputStream(file);
            out.write(bout.toByteArray());
            out.flush();
            out.close();

            is.close();
        } else {
            // this already closes the InputStream
            writeStreamToFile(is, file);
        }
        conn.disconnect();
    }

    public static void httpReadToFile(String url, File file) throws IOException {
        httpReadToFile(url, file, false);
    }

    public static String getFileChecksum(File file, String algorithm) throws IOException, NoSuchAlgorithmException {
        InputStream is = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        is.read(bytes);
        is.close();
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        messageDigest.update(bytes);
        Formatter formatter = new Formatter();

        for (byte b : messageDigest.digest()) {
            formatter.format("%02x", b);
        }

        String str = formatter.toString();
        formatter.close();
        return str;
    }

    public static byte[] readFile(File file) throws IOException {
        FileInputStream fileinputstream = new FileInputStream(file);
        return readFully(fileinputstream);
    }

    public static String readFileString(File file) throws IOException {
        return new String(readFile(file), StandardCharsets.UTF_8);
    }

    public static byte[] readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] bytes = new byte[4096];
        int count;

        while ((count = in.read(bytes, 0, bytes.length)) != -1) {
            out.write(bytes, 0, count);
        }

        in.close();
        return out.toByteArray();
    }

    public static String readWinRegistry(String key) {
        try {
            Process process = Runtime.getRuntime().exec("reg query \"" + key.substring(0, key.lastIndexOf('\\')) + "\" /v \"" + key.substring(key.lastIndexOf('\\') + 1) + "\"");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    String[] split = line.split("REG_SZ|REG_DWORD");
                    if (split.length > 1) {
                        return split[1].trim();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Quaternion slerp(Quaternion start, Quaternion end, float alpha) {
        float d = start.x * end.x + start.y * end.y + start.z * end.z + start.w * end.w;
        float absDot = d < 0.0F ? -d : d;

        // Set the first and second scale for the interpolation
        float scale0 = 1.0F - alpha;
        float scale1 = alpha;

        // Check if the angle between the 2 quaternions was big enough to
        // warrant such calculations
        if ((1.0F - absDot) > 0.1F) {
            // Get the angle between the 2 quaternions,
            // and then store the sin() of that angle
            float angle = (float) Math.acos(absDot);
            float invSinTheta = 1.0F / (float) Math.sin(angle);

            // Calculate the scale for q1 and q2, according to the angle and
            // its sine value
            scale0 = (float) Math.sin((1.0F - alpha) * angle) * invSinTheta;
            scale1 = (float) Math.sin(alpha * angle) * invSinTheta;
        }

        if (d < 0.0F) {
            scale1 = -scale1;
        }

        // Calculate the x, y, z and w values for the quaternion by using a
        // special form of linear interpolation for quaternions.
        float x = (scale0 * start.x) + (scale1 * end.x);
        float y = (scale0 * start.y) + (scale1 * end.y);
        float z = (scale0 * start.z) + (scale1 * end.z);
        float w = (scale0 * start.w) + (scale1 * end.w);

        // Return the interpolated quaternion
        return new Quaternion(w, x, y, z);
    }

    public static Vec3 vecLerp(Vec3 start, Vec3 end, double fraction) {
        double x = start.x + (end.x - start.x) * fraction;
        double y = start.y + (end.y - start.y) * fraction;
        double z = start.z + (end.z - start.z) * fraction;
        return new Vec3(x, y, z);
    }

    public static float applyDeadzone(float axis, float deadzone) {
        float scalar = 1.0F / (1.0F - deadzone);
        float newAxis = 0.0F;

        if (Math.abs(axis) > deadzone) {
            newAxis = (Math.abs(axis) - deadzone) * scalar * Math.signum(axis);
        }

        return newAxis;
    }

    public static void spawnParticles(ParticleOptions type, int count, Vec3 position, Vec3 size, double speed) {
        Minecraft minecraft = Minecraft.getInstance();

        for (int k = 0; k < count; k++) {
            double d0 = avRandomizer.nextGaussian() * size.x;
            double d1 = avRandomizer.nextGaussian() * size.y;
            double d2 = avRandomizer.nextGaussian() * size.z;
            double d3 = avRandomizer.nextGaussian() * speed;
            double d4 = avRandomizer.nextGaussian() * speed;
            double d5 = avRandomizer.nextGaussian() * speed;

            try {
                minecraft.level.addParticle(type, position.x + d0, position.y + d1, position.z + d2, d3, d4, d5);
            } catch (Throwable throwable) {
                VRSettings.logger.warn("Could not spawn particle effect {}", type);
                return;
            }
        }
    }

    public static int getCombinedLightWithMin(BlockAndTintGetter lightReader, BlockPos pos, int minLight) {
        int light = LevelRenderer.getLightColor(lightReader, pos);
        int blockLight = (light >> 4) & 0xF;

        if (blockLight < minLight) {
            light &= 0xFFFFFF00;
            light |= minLight << 4;
        }

        return light;
    }

    public static void takeScreenshot(RenderTarget fb) {
        Minecraft minecraft = Minecraft.getInstance();
        Screenshot.grab(minecraft.gameDirectory, fb, text ->
            minecraft.execute(() -> minecraft.gui.getChat().addMessage(text)));
    }

    public static List<FormattedText> wrapText(FormattedText text, int width, Font fontRenderer, @Nullable FormattedText linePrefix) {
        ComponentCollector componentcollector = new ComponentCollector();
        text.visit((style, str) -> {
            componentcollector.append(FormattedText.of(str, style));
            return Optional.empty();
        }, Style.EMPTY);

        List<FormattedText> list = Lists.newArrayList();
        fontRenderer.getSplitter()
            .splitLines(componentcollector.getResultOrEmpty(), width, Style.EMPTY, (lineText, sameLine) -> {
                list.add(sameLine && linePrefix != null ? FormattedText.composite(linePrefix, lineText) : lineText);
            });
        return list.isEmpty() ? Lists.newArrayList(FormattedText.EMPTY) : list;
    }

    public static List<ChatFormatting> styleToFormats(Style style) {
        if (style.isEmpty()) {
            return new ArrayList<>();
        } else {
            ArrayList<ChatFormatting> arraylist = new ArrayList<>();

            if (style.getColor() != null) {
                arraylist.add(ChatFormatting.getByName(style.getColor().serialize()));
            }

            if (style.isBold()) {
                arraylist.add(ChatFormatting.BOLD);
            }

            if (style.isItalic()) {
                arraylist.add(ChatFormatting.ITALIC);
            }

            if (style.isStrikethrough()) {
                arraylist.add(ChatFormatting.STRIKETHROUGH);
            }

            if (style.isUnderlined()) {
                arraylist.add(ChatFormatting.UNDERLINE);
            }

            if (style.isObfuscated()) {
                arraylist.add(ChatFormatting.OBFUSCATED);
            }

            return arraylist;
        }
    }

    public static String formatsToString(List<ChatFormatting> formats) {
        if (formats.size() == 0) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder();
            formats.forEach(builder::append);
            return builder.toString();
        }
    }

    public static String styleToFormatString(Style style) {
        return formatsToString(styleToFormats(style));
    }

    public static Component throwableToComponent(Throwable throwable) {
        MutableComponent result = Component.literal(throwable.getClass().getName() +
            (throwable.getMessage() == null ? "" : ": " + throwable.getMessage()));

        for (StackTraceElement element : throwable.getStackTrace()) {
            result.append(Component.literal("\n" + element.toString()));
        }
        return result;
    }

    public static long microTime() {
        return System.nanoTime() / 1000L;
    }

    public static long milliTime() {
        return System.nanoTime() / 1000000L;
    }

    public static void printStackIfContainsClass(String className) {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        boolean print = false;

        for (StackTraceElement stackEl : stack) {
            if (stackEl.getClassName().equals(className)) {
                print = true;
                break;
            }
        }

        if (print) {
            Thread.dumpStack();
        }
    }

    public static org.joml.Matrix4f Matrix4fFromOpenVR(HmdMatrix44 in) {
        return new org.joml.Matrix4f(in.m(0), in.m(4), in.m(8), in.m(12),
            in.m(1), in.m(5), in.m(9), in.m(13),
            in.m(2), in.m(6), in.m(10), in.m(14),
            in.m(3), in.m(7), in.m(11), in.m(15));
    }

    public static Quaternion convertMatrix4ftoRotationQuat(
        float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22)
    {
        double lengthSquared = m00 * m00 + m10 * m10 + m20 * m20;

        if (lengthSquared != 1.0D && lengthSquared != 0.0D) {
            lengthSquared = 1.0D / Math.sqrt(lengthSquared);
            m00 = (float) (m00 * lengthSquared);
            m10 = (float) (m10 * lengthSquared);
            m20 = (float) (m20 * lengthSquared);
        }

        lengthSquared = m01 * m01 + m11 * m11 + m21 * m21;

        if (lengthSquared != 1.0D && lengthSquared != 0.0D) {
            lengthSquared = 1.0D / Math.sqrt(lengthSquared);
            m01 = (float) (m01 * lengthSquared);
            m11 = (float) (m11 * lengthSquared);
            m21 = (float) (m21 * lengthSquared);
        }

        lengthSquared = m02 * m02 + m12 * m12 + m22 * m22;

        if (lengthSquared != 1.0D && lengthSquared != 0.0D) {
            lengthSquared = 1.0D / Math.sqrt(lengthSquared);
            m02 = (float) (m02 * lengthSquared);
            m12 = (float) (m12 * lengthSquared);
            m22 = (float) (m22 * lengthSquared);
        }

        // Use the Graphics Gems code, from
        // ftp://ftp.cis.upenn.edu/pub/graphics/shoemake/quatut.ps.Z
        // *NOT* the "Matrix and Quaternions FAQ", which has errors!

        // the trace is the sum of the diagonal elements; see
        // http://mathworld.wolfram.com/MatrixTrace.html
        float t = m00 + m11 + m22;

        // we protect the division by s by ensuring that s>=1
        Quaternion quat = new Quaternion();

        if (t >= 0.0F) { // |w| >= .5
            double s = Math.sqrt(t + 1.0F); // |s|>=1 ...
            quat.w = (float) (0.5D * s);
            s = 0.5D / s; // so this division isn't bad
            quat.x = (float) ((m21 - m12) * s);
            quat.y = (float) ((m02 - m20) * s);
            quat.z = (float) ((m10 - m01) * s);
        } else if (m00 > m11 && m00 > m22) {
            double s = Math.sqrt(1.0D + m00 - m11 - m22); // |s|>=1
            quat.x = (float) (s * 0.5D); // |x| >= .5
            s = 0.5D / s;
            quat.y = (float) ((m10 + m01) * s);
            quat.z = (float) ((m02 + m20) * s);
            quat.w = (float) ((m21 - m12) * s);
        } else if (m11 > m22) {
            double s = Math.sqrt(1.0D + m11 - m00 - m22); // |s|>=1
            quat.y = (float) (s * 0.5D); // |y| >= .5
            s = 0.5D / s;
            quat.x = (float) ((m10 + m01) * s);
            quat.z = (float) ((m21 + m12) * s);
            quat.w = (float) ((m02 - m20) * s);
        } else {
            double s = Math.sqrt(1.0D + m22 - m00 - m11); // |s|>=1
            quat.z = (float) (s * 0.5D); // |z| >= .5
            s = 0.5D / s;
            quat.x = (float) ((m02 + m20) * s);
            quat.y = (float) ((m21 + m12) * s);
            quat.w = (float) ((m10 - m01) * s);
        }

        return quat;
    }

    public static org.vivecraft.common.utils.math.Matrix4f rotationXMatrix(float angle) {
        float sina = (float) Math.sin(angle);
        float cosa = (float) Math.cos(angle);
        return new org.vivecraft.common.utils.math.Matrix4f(1.0F, 0.0F, 0.0F,
            0.0F, cosa, -sina,
            0.0F, sina, cosa);
    }

    public static org.vivecraft.common.utils.math.Matrix4f rotationZMatrix(float angle) {
        float sina = (float) Math.sin(angle);
        float cosa = (float) Math.cos(angle);
        return new org.vivecraft.common.utils.math.Matrix4f(cosa, -sina, 0.0F,
            sina, cosa, 0.0F,
            0.0F, 0.0F, 1.0F);
    }

    public static Vector3 convertMatrix4ftoTranslationVector(org.vivecraft.common.utils.math.Matrix4f mat) {
        return new Vector3(mat.M[0][3], mat.M[1][3], mat.M[2][3]);
    }

    public static void Matrix4fSet(org.vivecraft.common.utils.math.Matrix4f mat,
        float m11, float m12, float m13, float m14,
        float m21, float m22, float m23, float m24,
        float m31, float m32, float m33, float m34,
        float m41, float m42, float m43, float m44)
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

    public static void Matrix4fCopy(
        org.vivecraft.common.utils.math.Matrix4f source, org.vivecraft.common.utils.math.Matrix4f dest)
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
}
