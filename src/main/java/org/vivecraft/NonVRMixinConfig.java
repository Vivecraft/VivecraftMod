package org.vivecraft;

import me.fallenbreath.conditionalmixin.api.mixin.RestrictiveMixinConfigPlugin;

import java.util.List;
import java.util.Set;

public class NonVRMixinConfig extends RestrictiveMixinConfigPlugin {
    private static boolean classLoadIgnore;

    public static void classLoad() {
        classLoadIgnore = true;
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
}
