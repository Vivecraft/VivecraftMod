package org.vivecraft.mixin.client_vr;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.Arrays;
import java.util.stream.Stream;

@Mixin(Options.class)
public abstract class OptionsVRMixin {

    @Shadow
    @Final
    public KeyMapping[] keyHotbarSlots;

    @ModifyArg(
        method = "<init>",
        index = 0,
        at = @At(
            value = "INVOKE",
            target = "Lorg/apache/commons/lang3/ArrayUtils;addAll([Ljava/lang/Object;[Ljava/lang/Object;)[Ljava/lang/Object;"
        )
    )
    private Object[] vivecraft$injectKeyBindingsMixin(Object[] array)
    {
        if (array instanceof KeyMapping[] mappings && mappings.length > 0) {
            // inject custom key mappings
            array = mappings = Stream.concat(Arrays.stream(mappings), VivecraftVRMod.userKeyBindingSet.stream()).toArray(KeyMapping[]::new);
            VivecraftVRMod.vanillaBindingSet.addAll(Arrays.asList(mappings));
            if (this.keyHotbarSlots != null && this.keyHotbarSlots.length > 0) {
                VivecraftVRMod.vanillaBindingSet.addAll(Arrays.asList(this.keyHotbarSlots));
            } else {
                VRSettings.logger.error("keyHotbarSlots is invalid! {}", Arrays.toString(array));
            }
        } else {
            VRSettings.logger.error("keyMappings is invalid! {}", Arrays.toString(array));
        }
        return array;
    }
}
