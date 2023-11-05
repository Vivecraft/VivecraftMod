package org.vivecraft.common.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivecraft.client.Xplat;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_vr.utils.LoaderUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.FloatBuffer;
import java.nio.file.Files;
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

    public static final Vector3fc PITCH = new Vector3f(1.0F, 0.0F, 0.0F);
    public static final Vector3fc YAW = new Vector3f(0.0F, 1.0F, 0.0F);
    public static final Vector3fc ROLL = new Vector3f(0.0F, 0.0F, 1.0F);

    public static void wordWrap(String in, int length, ArrayList<String> wrapped) {
        // can't wrap with length 0, so return the original string
        if (length == 0) {
            wrapped.add(in);
            return;
        }
        String s = "\n";
        boolean flag = false;
        in = in.replace("\r", "");

        if (in.length() < length) {
            flag = true;
            length = in.length();
        }

        if (in.substring(0, length).contains(s)) {
            String s2 = in.substring(0, in.indexOf(s)).trim();
            wrapped.add(s2);
            wordWrap(in.substring(in.indexOf(s) + 1), length, wrapped);
        } else if (flag) {
            wrapped.add(in);
        } else {
            int i = Math.max(Math.max(in.lastIndexOf(" ", length), in.lastIndexOf("\t", length)), in.lastIndexOf("-", length));

            if (i == -1) {
                i = length;
            }

            String s1 = in.substring(0, i).trim();
            wrapped.add(s1);
            wordWrap(in.substring(i), length, wrapped);
        }
    }

    public static org.joml.Vector3f convertVector(Vec3 vector, Vector3f dest) {
        return dest.set((float) vector.x(), (float) vector.y(), (float) vector.z());
    }

    public static Vec3 convertToVector3d(Vector3f vector) {
        return new Vec3(vector.x(), vector.y(), vector.z());
    }

    static {
        Arrays.sort(illegalChars);
    }

    public static InputStream getAssetAsStream(String name, boolean required) {
        InputStream inputstream = null;

        try {
            try {
                Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation("vivecraft", name));
                if (resource.isPresent()) {
                    inputstream = resource.get().open();
                }
            } catch (NullPointerException | FileNotFoundException filenotfoundexception) {
                inputstream = VRShaders.class.getResourceAsStream("/assets/vivecraft/" + name);
            }

            if (inputstream == null) {
                Path path1 = Paths.get(System.getProperty("user.dir"));

                if (path1.getParent() != null) {
                    Path path = path1.getParent().resolve("src/resources/assets/vivecraft/" + name);

                    if (!path.toFile().exists() && path1.getParent().getParent() != null) {
                        path = path1.getParent().getParent().resolve("resources/assets/vivecraft/" + name);
                    }

                    if (path.toFile().exists()) {
                        inputstream = new FileInputStream(path.toFile());
                    }
                }
            }
        } catch (Exception exception) {
            handleAssetException(exception, name, required);
            return null;
        }

        if (inputstream == null) {
            handleAssetException(new FileNotFoundException(name), name, required);
        }

        return inputstream;
    }

    public static void loadAssetToFile(String name, File file, boolean required) {
        InputStream inputstream = getAssetAsStream(name, required);

        if (inputstream != null) {
            try {
                writeStreamToFile(inputstream, file);
                inputstream.close();
            } catch (Exception exception) {
                handleAssetException(exception, name, required);
            }
        }
    }

    private static void handleAssetException(Throwable e, String name, boolean required) {
        if (required) {
            throw new RuntimeException("Failed to load asset: " + name, e);
        } else {
            System.out.println("Failed to load asset: " + name);
            e.printStackTrace();
        }
    }

    public static void unpackNatives(String directory) {
        try {
            (new File("openvr/" + directory)).mkdirs();

            try {
                Path path = Paths.get(System.getProperty("user.dir"));
                Path path1 = path.getParent().resolve("src/resources/natives/" + directory);

                if (!path1.toFile().exists()) {
                    path1 = path.getParent().getParent().resolve("resources/natives/" + directory);
                }

                if (path1.toFile().exists()) {
                    System.out.println("Copying " + directory + " natives...");

                    for (File file1 : path1.toFile().listFiles()) {
                        System.out.println(file1.getName());
                        Files.copy(file1.toPath(), new File("openvr/" + directory + "/" + file1.getName()).toPath());
                    }

                    return;
                }
            } catch (Exception exception) {
            }

            System.out.println("Unpacking " + directory + " natives...");

            Path jarPath = Xplat.getJarPath();
            boolean didExtractSomething = false;
            try (Stream<Path> natives = java.nio.file.Files.list(jarPath.resolve("natives/" + directory))) {
                for (Path file : natives.collect(Collectors.toCollection(ArrayList::new))) {
                    didExtractSomething = true;
                    System.out.println(file);
                    java.nio.file.Files.copy(file, new File("openvr/" + directory + "/" + file.getFileName()).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                System.out.println("Failed to unpack natives from jar");
            }
            if (!didExtractSomething) {
                ZipFile zipfile = LoaderUtils.getVivecraftZip();
                Enumeration<? extends ZipEntry> enumeration = zipfile.entries();

                while (enumeration.hasMoreElements()) {
                    ZipEntry zipentry = enumeration.nextElement();

                    if (zipentry.getName().startsWith("natives/" + directory)) {
                        String s = Paths.get(zipentry.getName()).getFileName().toString();
                        System.out.println(s);
                        writeStreamToFile(zipfile.getInputStream(zipentry), new File("openvr/" + directory + "/" + s));
                    }
                }

                zipfile.close();
            }
        } catch (Exception exception1) {
            System.out.println("Failed to unpack natives");
            exception1.printStackTrace();
        }
    }

    public static void writeStreamToFile(InputStream is, File file) throws IOException {
        FileOutputStream fileoutputstream = new FileOutputStream(file);
        byte[] abyte = new byte[4096];
        int i;

        while ((i = is.read(abyte, 0, abyte.length)) != -1) {
            fileoutputstream.write(abyte, 0, i);
        }

        fileoutputstream.flush();
        fileoutputstream.close();
        is.close();
    }

    public static String httpReadLine(String url) throws IOException {
        HttpURLConnection httpurlconnection = (HttpURLConnection) (new URL(url)).openConnection();
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

    public static List<String> httpReadAllLines(String url) throws IOException {
        HttpURLConnection httpurlconnection = (HttpURLConnection) (new URL(url)).openConnection();
        httpurlconnection.setConnectTimeout(5000);
        httpurlconnection.setReadTimeout(20000);
        httpurlconnection.setUseCaches(false);
        httpurlconnection.setDoInput(true);
        BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(httpurlconnection.getInputStream()));
        ArrayList<String> arraylist = new ArrayList<>();
        String s;

        while ((s = bufferedreader.readLine()) != null) {
            arraylist.add(s);
        }

        bufferedreader.close();
        httpurlconnection.disconnect();
        return arraylist;
    }

    public static void httpReadToFile(String url, File file, boolean writeWhenComplete) throws IOException {
        HttpURLConnection httpurlconnection = (HttpURLConnection) (new URL(url)).openConnection();
        httpurlconnection.setConnectTimeout(5000);
        httpurlconnection.setReadTimeout(20000);
        httpurlconnection.setUseCaches(false);
        httpurlconnection.setDoInput(true);
        InputStream inputstream = httpurlconnection.getInputStream();

        if (writeWhenComplete) {
            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream(httpurlconnection.getContentLength());
            byte[] abyte = new byte[4096];
            int i;

            while ((i = inputstream.read(abyte, 0, abyte.length)) != -1) {
                bytearrayoutputstream.write(abyte, 0, i);
            }

            OutputStream outputstream = new FileOutputStream(file);
            outputstream.write(bytearrayoutputstream.toByteArray());
            outputstream.flush();
            outputstream.close();
        } else {
            OutputStream outputstream1 = new FileOutputStream(file);
            byte[] abyte1 = new byte[4096];
            int j;

            while ((j = inputstream.read(abyte1, 0, abyte1.length)) != -1) {
                outputstream1.write(abyte1, 0, j);
            }

            outputstream1.flush();
            outputstream1.close();
        }

        inputstream.close();
        httpurlconnection.disconnect();
    }

    public static String getFileChecksum(File file, String algorithm) throws IOException, NoSuchAlgorithmException {
        InputStream inputstream = new FileInputStream(file);
        byte[] abyte = new byte[(int) file.length()];
        inputstream.read(abyte);
        inputstream.close();
        MessageDigest messagedigest = MessageDigest.getInstance(algorithm);
        messagedigest.update(abyte);
        Formatter formatter = new Formatter();

        for (byte b0 : messagedigest.digest()) {
            formatter.format("%02x", b0);
        }

        String s = formatter.toString();
        formatter.close();
        return s;
    }

    public static Vec3 vecLerp(Vec3 start, Vec3 end, double fraction) {
        double d0 = start.x + (end.x - start.x) * fraction;
        double d1 = start.y + (end.y - start.y) * fraction;
        double d2 = start.z + (end.z - start.z) * fraction;
        return new Vec3(d0, d1, d2);
    }

    public static AABB getEntityHeadHitbox(Entity entity, double inflate) {
        if ((entity instanceof Player player && !player.isSwimming()) || // swimming players hitbox is just a box around their butt
            entity instanceof Zombie ||
            entity instanceof AbstractPiglin ||
            entity instanceof AbstractSkeleton ||
            entity instanceof Witch ||
            entity instanceof AbstractIllager ||
            entity instanceof Blaze ||
            entity instanceof Creeper ||
            entity instanceof EnderMan ||
            entity instanceof AbstractVillager ||
            entity instanceof SnowGolem ||
            entity instanceof Vex ||
            entity instanceof Strider) {

            Vec3 headpos = entity.getEyePosition();
            double headsize = entity.getBbWidth() * 0.5;
            if (((LivingEntity) entity).isBaby()) {
                // babies have big heads
                headsize *= 1.20;
            }
            return new AABB(headpos.subtract(headsize, headsize - inflate, headsize), headpos.add(headsize, headsize + inflate, headsize)).inflate(inflate);
        } else if (!(entity instanceof EnderDragon) // no ender dragon, the code doesn't work for it
            && entity instanceof LivingEntity livingEntity) {

            float yrot = -(livingEntity.yBodyRot) * 0.017453292F;
            // offset head in entity rotation
            Vec3 headpos = entity.getEyePosition()
                .add(new Vec3(Mth.sin(yrot), 0, Mth.cos(yrot))
                    .scale(livingEntity.getBbWidth() * 0.5F));

            double headsize = livingEntity.getBbWidth() * 0.25;
            if (livingEntity.isBaby()) {
                // babies have big heads
                headsize *= 1.5;
            }
            return new AABB(headpos.subtract(headsize, headsize, headsize), headpos.add(headsize, headsize, headsize)).inflate(inflate * 0.25).expandTowards(headpos.subtract(entity.position()).scale(inflate));
        }
        return null;
    }

    /**
     * Vivecraft's logger for printing to console.
     */
    public static final Logger logger = LoggerFactory.getLogger("Vivecraft");

    public static void printStackIfContainsClass(String className) {
        StackTraceElement[] astacktraceelement = Thread.currentThread().getStackTrace();
        boolean flag = false;

        for (StackTraceElement stacktraceelement : astacktraceelement) {
            if (stacktraceelement.getClassName().equals(className)) {
                flag = true;
                break;
            }
        }

        if (flag) {
            Thread.dumpStack();
        }
    }

    public static long microTime(){
        return System.nanoTime() / 1000L;
    }

    public static long milliTime() {
        return System.nanoTime() / 1000000L;
    }

    public static Vector3fc forward() {
        return new Vector3f(0.0F, 0.0F, -1.0F);
    }

    public static Vec3 toVec3(Vector3fc vector3fc) {
        return new Vec3(vector3fc.x(), vector3fc.y(), vector3fc.z());
    }

    public static Matrix4f convertSteamVRMatrix3ToMatrix4f(FloatBuffer floatBuffer, Matrix4f mat) {
        return mat.setTransposed(new org.joml.Matrix4f(
            floatBuffer.get(0), floatBuffer.get(4), floatBuffer.get(8), 0.0F,
            floatBuffer.get(1), floatBuffer.get(5), floatBuffer.get(9), 0.0F,
            floatBuffer.get(2), floatBuffer.get(6), floatBuffer.get(10), 0.0F,
            floatBuffer.get(3), floatBuffer.get(7), floatBuffer.get(11), 1.0F
        ));
    }
}
