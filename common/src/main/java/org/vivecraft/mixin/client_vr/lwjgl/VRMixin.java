package org.vivecraft.mixin.client_vr.lwjgl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.lwjgl.openvr.VR;
import org.lwjgl.system.Configuration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

@Mixin(VR.class)
public class VRMixin {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @WrapOperation(method = "<clinit>", at = @At(value = "FIELD", target = "Lorg/lwjgl/system/Configuration;OPENVR_LIBRARY_NAME:Lorg/lwjgl/system/Configuration;"))
    private static Configuration<String> vivecraft$openVRLib(Operation<Configuration<String>> original) {
        try {
            Class<?> StateInit = Class.forName("org.lwjgl.system.Configuration$StateInit");
            Constructor<Configuration> c = Configuration.class.getDeclaredConstructor(String.class, StateInit);
            c.setAccessible(true);
            Field StateInit_STRING = StateInit.getDeclaredField("STRING");
            StateInit_STRING.setAccessible(true);
            return (Configuration<String>) c.newInstance("org.lwjgl.openvr.libname", StateInit_STRING.get(null));
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
