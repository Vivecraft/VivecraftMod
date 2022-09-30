package org.vivecraft;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class NonVRMixinConfig implements IMixinConfigPlugin {
    private static boolean classLoadIgnore;

    public static void classLoad() {
        classLoadIgnore = true;
    }

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!Xplat.isModLoadedSuccess()) {
            LogManager.getLogger().log(Level.WARN, "not loading '" + mixinClassName + "' because mod failed to load completely");
            return false;
        }
        if (mixinClassName.contains("ClientPacketListenerMixin")) {
            return !VRState.checkVR();
        } else {
            return true;
        }
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
}
