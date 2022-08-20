package org.vivecraft;

import me.fallenbreath.conditionalmixin.api.mixin.RestrictiveMixinConfigPlugin;

import java.util.List;
import java.util.Set;

public class NonVRMixinConfig extends RestrictiveMixinConfigPlugin {
    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (targetClassName.contains("ClientPacketListener")) {
            return !VRState.isVR;
        } else {
            return true;
        }
    }

    @Override
    public List<String> getMixins() {
        return null;
    }
}
