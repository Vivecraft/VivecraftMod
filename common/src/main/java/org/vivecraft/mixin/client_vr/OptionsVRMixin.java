package org.vivecraft.mixin.client_vr;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.VRState;

import java.util.function.IntFunction;
import java.util.stream.Stream;

@Mixin(Options.class)
public abstract class OptionsVRMixin {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;toArray(Ljava/util/function/IntFunction;)[Ljava/lang/Object;"))
    private Object[] vivecraft$processKeyMappings(
        Stream instance, IntFunction<Object[]> intFunction, Operation<Object[]> original)
    {
        return VivecraftVRMod.INSTANCE.initializeBindings((KeyMapping[]) original.call(instance, intFunction));
    }

    @ModifyExpressionValue(method = "buildPlayerInformation", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 3))
    private Object vivecraft$alwaysRightMain(Object arm) {
        return VRState.VR_RUNNING ? HumanoidArm.RIGHT : arm;
    }
}
