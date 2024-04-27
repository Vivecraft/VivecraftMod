package org.vivecraft;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;
import org.vivecraft.client.Xplat;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.mod_compat_vr.iris.mixin.irisshaders.IrisChunkProgramOverridesMixin;
import org.vivecraft.mod_compat_vr.iris.mixin.irisshaders.IrisChunkProgramOverridesMixinSodium_0_5_8;
import org.vivecraft.mod_compat_vr.iris.mixin.irisshaders.IrisChunkProgramOverridesMixinSodium_0_6;
import org.vivecraft.mod_compat_vr.sodium.SodiumHelper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MixinConfig implements IMixinConfigPlugin {

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

    private final Set<String> appliedModFixes = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!Xplat.isModLoadedSuccess()) {
            VRSettings.logger.info("not loading '{}' because mod failed to load completely", mixinClassName);
            return false;
        }

        // only try to apply mod mixins if the target class was found
        if (mixinClassName.startsWith("org.vivecraft.mod_compat_vr")) {
            try {
                MixinService.getService().getBytecodeProvider().getClassNode(targetClassName);
            } catch (ClassNotFoundException | IOException e) {
                return false;
            }
            String mod = mixinClassName.split("\\.")[3];
            if (appliedModFixes.add(mod)) {
                VRSettings.logger.info("Vivecraft: applying '{}' fixes", mod);
            }
        }

        String neededClass = "";
        // apply iris sodium version specific mixins only when the right class is there
        if (mixinClassName.equals(IrisChunkProgramOverridesMixinSodium_0_5_8.class.getName())
        ) {
            neededClass = "me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType";
        } else if (mixinClassName.equals(IrisChunkProgramOverridesMixinSodium_0_6.class.getName())) {
            neededClass = "net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType";
        }else if (mixinClassName.equals(IrisChunkProgramOverridesMixin.class.getName())) {
            neededClass = "net.caffeinemc.mods.sodium.client.gl.shader.GlProgram";
        }

        if (!neededClass.isEmpty()) {
            try {
                MixinService.getService().getBytecodeProvider().getClassNode(neededClass);
                return true;
            } catch (ClassNotFoundException | IOException e) {
                VRSettings.logger.info("Vivecraft: skipping mixin '{}'", mixinClassName);
                return false;
            }
        }

        return !mixinClassName.contains("NoSodium") || !SodiumHelper.isLoaded();
    }
}
