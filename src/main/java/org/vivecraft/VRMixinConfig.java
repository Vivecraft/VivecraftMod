package org.vivecraft;

import com.sun.jna.NativeLibrary;
import jopenvr.JOpenVRLibrary;
import me.fallenbreath.conditionalmixin.api.mixin.RestrictiveMixinConfigPlugin;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.vivecraft.utils.Utils;
import org.vivecraft.xplat.XplatImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class VRMixinConfig implements IMixinConfigPlugin {
    private static boolean asked = false;

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
            Path file = XplatImpl.getInstance().getConfigPath("vivecraft-config.properties");
            if (!Files.exists(file)) {
                Files.createFile(file);
            }
            properties.load(Files.newInputStream(file));
            if (properties.containsKey("vrStatus")) {
                VRState.isVR = Boolean.parseBoolean(properties.getProperty("vrStatus"));
            } else if (!asked && !JOpenVRLibrary.isErrored()) {
                unpackPlatformNatives();
                VRState.isVR = TinyFileDialogs.tinyfd_messageBox("VR", "Would you like to use VR?", "yesno", "info", false);
                asked = true;

                properties.setProperty("vrStatus", String.valueOf(VRState.isVR));
                properties.store(Files.newOutputStream(file), "This file stores if VR should be enabled.");
                TinyFileDialogs.tinyfd_messageBox("VR", "Your choice has been saved. To edit it, please go to config/vivecraft-config.properties.", "ok", "info", false);
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
        if (mixinClassName.contains("NoSodium") && XplatImpl.getInstance().isModLoaded("sodium")) {
            return false;
        }
        return VRState.isVR;
    }
}
