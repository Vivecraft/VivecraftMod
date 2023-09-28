package org.vivecraft.client_vr.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;

public class LoaderUtils {
    public static URL ZipFileUrl;
    public static File vivecraftFile;
    private static java.util.zip.ZipFile ZipFile;

    public static void init() {
        try {
            ZipFileUrl = getVivecraftZipLocation().toURL();
            vivecraftFile = toFile(ZipFileUrl.toURI());
            ZipFile = new java.util.zip.ZipFile(vivecraftFile);
        } catch (Exception e) {
            System.out.print("Error getting Vivecraft library: " + e.getLocalizedMessage());
        }
    }

    public static URI getVivecraftZipLocation() throws URISyntaxException {
        if (ZipFileUrl != null) {
            return ZipFileUrl.toURI();
        } else {
            ZipFileUrl = LoaderUtils.class.getProtectionDomain().getCodeSource().getLocation();

            if (ZipFileUrl == null) {
                throw new RuntimeException("Could not find Vivecraft zip");
            } else {
                return ZipFileUrl.toURI();
            }
        }
    }

    public static java.util.zip.ZipFile getVivecraftZip() throws URISyntaxException, IOException {
        if (vivecraftFile == null) {
            init();
        }
        return new java.util.zip.ZipFile(vivecraftFile);
    }

    public static File toFile(URI uri) {
        if (!"union".equals(uri.getScheme())) {
            return new File(uri);
        } else {
            try {
                String s = uri.getPath();

                if (s.contains("#")) {
                    s = s.substring(0, s.lastIndexOf("#"));
                }

                File file1 = new File(s);
                ZipFileUrl = file1.toURI().toURL();
                Map<String, String> map = new HashMap<>();
                map.put("create", "true");
                FileSystems.newFileSystem(URI.create("jar:" + ZipFileUrl + "!/"), map);
                return file1;
            } catch (Exception exception) {
                exception.printStackTrace();
                return null;
            }
        }
    }
}
