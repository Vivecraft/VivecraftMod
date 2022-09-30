package org.vivecraft;

import com.sun.jna.NativeLibrary;
import jopenvr.JOpenVRLibrary;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.vivecraft.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class VRMixinConfig implements IMixinConfigPlugin {
    protected static boolean asked = false;
    private static boolean unpackedNatives = false;

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    static {
        Properties properties = new Properties();
        try {
            Path file = Xplat.getConfigPath("vivecraft-config.properties");
            if (!Files.exists(file)) {
                Files.createFile(file);
            }
            properties.load(Files.newInputStream(file));
            if (properties.containsKey("vrStatus")) {
                VRState.isVR = Boolean.parseBoolean(properties.getProperty("vrStatus"));
            } else if (Xplat.isDedicatedServer()) {
                VRState.isVR = false;
                properties.setProperty("vrStatus", String.valueOf(VRState.isVR)); //set dedicated server to nonVR
            } else if (!asked) {
                VRMixinConfigPopup.askVR(properties, file);
            }
            if (!unpackedNatives && VRState.isVR) {
                unpackPlatformNatives();
                // disable VR if natives failed
                VRState.isVR = !JOpenVRLibrary.isErrored();
                unpackedNatives = true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean unpackPlatformNatives()
    {
        String s = System.getProperty("os.name").toLowerCase();
        String s1 = System.getProperty("os.arch").toLowerCase();
        String s2 = "win";

        if (s.contains("linux"))
        {
            s2 = "linux";
        }
        else if (s.contains("mac"))
        {
            s2 = "osx";
        }

        if (!s.contains("mac"))
        {
            if (s1.contains("64"))
            {
                s2 = s2 + "64";
            }
            else
            {
                s2 = s2 + "32";
            }
        }

        try {
            Utils.unpackNatives(s2);
        } catch (Exception e) {
            System.out.println("Native path not found");
            return false;
        }

        if (!(new File("openvr/" + s2)).exists()) {
            System.out.println("Path does not exist, skipping VR!");
            return false;
        }
        String s3 = (new File("openvr/" + s2)).getAbsolutePath();
        System.out.println("Adding OpenVR search path: " + s3);
        NativeLibrary.addSearchPath("openvr_api", s3);
        return true;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!Xplat.isModLoadedSuccess()) {
            LogManager.getLogger().log(Level.WARN, "not loading '" + mixinClassName + "' because mod failed to load completely");
            return false;
        }
        if (mixinClassName.contains("NoSodium") && (Xplat.isModLoaded("sodium") || Xplat.isModLoaded("rubidium"))) {
            return false;
        }
        return VRState.isVR;
    }
}
