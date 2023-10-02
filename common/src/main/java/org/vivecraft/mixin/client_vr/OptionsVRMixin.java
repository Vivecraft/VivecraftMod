package org.vivecraft.mixin.client_vr;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.stream.Stream;

import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.vivecraft.client.VivecraftVRMod.userKeyBindingSet;
import static org.vivecraft.client.VivecraftVRMod.vanillaBindingSet;

@Mixin(Options.class)
public abstract class OptionsVRMixin {

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            opcode = PUTFIELD,
            target = "Lnet/minecraft/client/Options;keyMappings:[Lnet/minecraft/client/KeyMapping;"
        )
    )
    void processOptionsMixin(Options instance, KeyMapping[] keyMappings, Operation<KeyMapping[]> original) {
        if (keyMappings != null) {
            if (keyMappings.length > 0) {
                // inject custom key mappings
                keyMappings = Stream.concat(Arrays.stream(keyMappings), userKeyBindingSet.stream()).toArray(KeyMapping[]::new);
                vanillaBindingSet.addAll(Arrays.asList(keyMappings));
                original.call(instance, keyMappings);
            } else {
                throw new MissingResourceException(
                    "keyMappings is empty!",
                    keyMappings.getClass().getName(),
                    Arrays.toString(keyMappings)
                );
            }
        } else {
            throw new NullPointerException("keyMappings is null!");
        }
    }
}
