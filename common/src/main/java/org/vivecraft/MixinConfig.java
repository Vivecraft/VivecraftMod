package org.vivecraft;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;
import org.vivecraft.client.Xplat;

import org.vivecraft.mod_compat_vr.iris.mixin.IrisChunkProgramOverridesMixinSodium_0_4_11;
import org.vivecraft.mod_compat_vr.iris.mixin.IrisChunkProgramOverridesMixinSodium_0_4_9;
import org.vivecraft.mod_compat_vr.sodium.mixin.RenderSectionManagerVRMixin;

import java.io.IOException;
import java.util.List;
import java.util.Set;

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


    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!Xplat.isModLoadedSuccess()) {
            LogManager.getLogger().log(Level.WARN, "not loading '" + mixinClassName + "' because mod failed to load completely");
            return false;
        }

        String neededClass = "";
        // apply iris sodium version specific mixins only when the right class is there
        if (mixinClassName.equals(IrisChunkProgramOverridesMixinSodium_0_4_9.class.getName())) {
            neededClass = "me.jellysquid.mods.sodium.client.render.vertex.type.ChunkVertexType";
        }
        else if (mixinClassName.equals(IrisChunkProgramOverridesMixinSodium_0_4_11.class.getName())) {
            neededClass = "me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType";
        }
        else if (mixinClassName.equals(RenderSectionManagerVRMixin.class.getName())) {
            neededClass = "me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderList";
            try {
                MixinService.getService().getBytecodeProvider().getClassNode(neededClass);
                ClassNode node = MixinService.getService().getBytecodeProvider().getClassNode(targetClassName);
                return node.fields.stream().anyMatch(field -> field.name.equals("chunkRenderList"));
            } catch (ClassNotFoundException | IOException e) {
                return false;
            }
        }

        if (!neededClass.isEmpty()) {
            try {
                MixinService.getService().getBytecodeProvider().getClassNode(neededClass);
                return true;
            } catch (ClassNotFoundException | IOException e) {
                LogManager.getLogger().log(Level.INFO, "Vivecraft: skipping mixin '" + mixinClassName + "'");
                return false;
            }
        }

        return !mixinClassName.contains("NoSodium") || (!Xplat.isModLoaded("sodium") && !Xplat.isModLoaded("rubidium"));
    }
}
