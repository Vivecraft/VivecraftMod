package org.vivecraft.client_vr;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_xr.render_pass.RenderPassType;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A RenderTarget that holds multiple RenderTargets for each RenderPass, and delegates calls to the current active one
 */
public class MultiPassRenderTarget extends RenderTarget {

    private final RenderTarget mainTarget;
    private final Function<RenderPass, RenderTarget> vrTargets;

    public MultiPassRenderTarget(String name, RenderTarget mainTarget, Function<RenderPass, RenderTarget> vrTargets) {
        super(name, mainTarget.useDepth);
        this.mainTarget = mainTarget;
        this.vrTargets = vrTargets;

        // use the default vanilla target for those
        this.width = mainTarget.width;
        this.height = mainTarget.height;
    }

    @Override
    public void resize(int width, int height) {
        callOnTarget(r -> r.resize(width, height));
    }

    @Override
    public void destroyBuffers() {
        // this one should be called on all RenderTargets
        callOnAllTargets(RenderTarget::destroyBuffers);
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
    public void blitToScreen() {
        callOnTarget(RenderTarget::blitToScreen);
    }

    @Override
    public void blitAndBlendToTexture(GpuTextureView gpuTextureView) {
        callOnTarget(r -> r.blitAndBlendToTexture(gpuTextureView));
    }

    @Override
    public GpuTexture getColorTexture() {
        return callOnTargetRet(RenderTarget::getColorTexture);
    }

    @Override
    public GpuTextureView getColorTextureView() {
        return callOnTargetRet(RenderTarget::getColorTextureView);
    }

    @Override
    public GpuTexture getDepthTexture() {
        return
            callOnTargetRet(RenderTarget::getDepthTexture);
    }

    @Override
    public GpuTextureView getDepthTextureView() {
        return callOnTargetRet(RenderTarget::getDepthTextureView);
    }

    private void callOnTarget(Consumer<RenderTarget> consumer) {
        consumer.accept(getCurrent());
    }

    private <T> T callOnTargetRet(Function<RenderTarget, T> function) {
        return function.apply(getCurrent());
    }

    private void callOnAllTargets(Consumer<RenderTarget> consumer) {
        consumer.accept(this.mainTarget);
        for (RenderPass pass : RenderPass.values()) {
            RenderTarget target = this.vrTargets.apply(pass);
            if (target != null) {
                consumer.accept(target);
            }
        }
    }

    /**
     * @return the RenderTarget that should be rendered to now
     */
    private RenderTarget getCurrent() {
        if (RenderPassType.isVanilla()) {
            return this.mainTarget;
        } else {
            // return the vanilla target if the pass one is null
            RenderTarget target = this.vrTargets.apply(ClientDataHolderVR.getInstance().currentPass);
            return target != null ? target : this.mainTarget;
        }
    }
}
