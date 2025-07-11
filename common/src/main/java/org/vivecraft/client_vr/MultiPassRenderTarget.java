package org.vivecraft.client_vr;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_xr.render_pass.RenderPassType;

import java.util.EnumMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A RenderTarget that holds multiple RenderTargets for each RenderPass, and delegates calls to the current active one
 */
public class MultiPassRenderTarget extends RenderTarget {

    private final RenderTarget mainTarget;
    private final EnumMap<RenderPass, RenderTarget> vrTargets;

    public MultiPassRenderTarget(String name, RenderTarget mainTarget, EnumMap<RenderPass, RenderTarget> vrTargets) {
        super(name, mainTarget.useDepth);
        this.mainTarget = mainTarget;
        this.vrTargets = vrTargets;

        // use the default vanilla target for those
        this.width = mainTarget.width;
        this.height = mainTarget.height;
        this.viewWidth = mainTarget.viewWidth;
        this.viewHeight = mainTarget.viewHeight;
        this.filterMode = mainTarget.filterMode;
    }

    @Override
    public void resize(int width, int height) {
        callOnTarget(r -> r.resize(width, height));
    }

    @Override
    public void destroyBuffers() {
        // this one should be called on all RenderTargets
        this.mainTarget.destroyBuffers();
        for (RenderTarget renderTarget : this.vrTargets.values()) {
            renderTarget.destroyBuffers();
        }
    }

    @Override
    public void copyDepthFrom(RenderTarget otherTarget) {
        callOnTarget(r -> r.copyDepthFrom(otherTarget));
    }

    @Override
    public void createBuffers(int width, int height) {
        callOnTarget(r -> r.createBuffers(width, height));
    }

    @Override
    public void setFilterMode(FilterMode filterMode) {
        callOnTarget(r -> r.setFilterMode(filterMode));
    }

    @Override
    public void blitToScreen() {
        callOnTarget(RenderTarget::blitToScreen);
    }

    @Override
    public void blitAndBlendToTexture(GpuTexture gpuTexture) {
        callOnTarget(r -> r.blitAndBlendToTexture(gpuTexture));
    }

    @Override
    public GpuTexture getColorTexture() {
        return callOnTargetRet(RenderTarget::getColorTexture);
    }

    @Override
    public GpuTexture getDepthTexture() {
        return
            callOnTargetRet(RenderTarget::getDepthTexture);
    }

    private void callOnTarget(Consumer<RenderTarget> consumer) {
        if (RenderPassType.isVanilla()) {
            consumer.accept(this.mainTarget);
        } else {
            consumer.accept(this.vrTargets.get(ClientDataHolderVR.getInstance().currentPass));
        }
    }

    private <T> T callOnTargetRet(Function<RenderTarget, T> function) {
        if (RenderPassType.isVanilla()) {
            return function.apply(this.mainTarget);
        } else {
            return function.apply(this.vrTargets.get(ClientDataHolderVR.getInstance().currentPass));
        }
    }
}
