package org.vivecraft.fabric.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ResourceLoadStateTracker;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ReloadListener;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.render.helpers.VRPassHelper;

@Mixin(Minecraft.class)
public class FabricMinecraftVRMixin {

    @Shadow
    @Final
    private DeltaTracker.Timer deltaTracker;

    @Shadow
    @Final
    private ReloadableResourceManager resourceManager;

    @Shadow
    public boolean noRender;

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ResourceLoadStateTracker;startReload(Lnet/minecraft/client/ResourceLoadStateTracker$ReloadReason;Ljava/util/List;)V"), index = 0)
    private ResourceLoadStateTracker.ReloadReason vivecraft$registerReloadListener(
        ResourceLoadStateTracker.ReloadReason reloadReason)
    {
        this.resourceManager.registerReloadListener(new ReloadListener());
        return reloadReason;
    }

    @Inject(method = "runTick", at = @At(value = "CONSTANT", args = "stringValue=blit"))
    private void vivecraft$renderVRPassesFabric(
        boolean renderLevel, CallbackInfo ci)
    {
        if (VRState.VR_RUNNING && !this.noRender) {
            VRPassHelper.renderAndSubmit(renderLevel, this.deltaTracker);
        }
    }
}
