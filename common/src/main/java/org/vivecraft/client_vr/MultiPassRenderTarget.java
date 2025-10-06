package org.vivecraft.client_vr;

import com.mojang.blaze3d.pipeline.RenderTarget;
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

    public MultiPassRenderTarget(RenderTarget mainTarget, Function<RenderPass, RenderTarget> vrTargets) {
        super(mainTarget.useDepth);
        this.mainTarget = mainTarget;
        this.vrTargets = vrTargets;

        // use the default vanilla target for those
        this.width = mainTarget.width;
        this.height = mainTarget.height;
        this.viewWidth = mainTarget.viewWidth;
        this.viewHeight = mainTarget.viewHeight;
        this.frameBufferId = mainTarget.frameBufferId;
        this.filterMode = mainTarget.filterMode;
    }

    @Override
    public void resize(int width, int height, boolean clearError) {
        callOnTarget(r -> r.resize(width, height, clearError));
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
    public void createBuffers(int width, int height, boolean clearError) {
        callOnTarget(r -> r.createBuffers(width, height, clearError));
    }

    @Override
    public void setFilterMode(int filterMode) {
        callOnTarget(r -> r.setFilterMode(filterMode));
    }

    @Override
    public void checkStatus() {
        callOnTarget(RenderTarget::checkStatus);
    }

    @Override
    public void bindRead() {
        callOnTarget(RenderTarget::bindRead);
    }

    @Override
    public void unbindRead() {
        callOnTarget(RenderTarget::unbindRead);
    }

    @Override
    public void bindWrite(boolean setViewport) {
        callOnTarget(r -> r.bindWrite(setViewport));
    }

    @Override
    public void unbindWrite() {
        callOnTarget(RenderTarget::unbindWrite);
    }

    @Override
    public void setClearColor(float red, float green, float blue, float alpha) {
        callOnTarget(r -> r.setClearColor(red, green, blue, alpha));
    }

    @Override
    public void blitToScreen(int width, int height) {
        callOnTarget(r -> r.blitToScreen(width, height));
    }

    @Override
    public void blitToScreen(int width, int height, boolean disableBlend) {
        callOnTarget(r -> r.blitToScreen(width, height, disableBlend));
    }

    @Override
    public void clear(boolean clearError) {
        callOnTarget(r -> r.clear(clearError));
    }

    @Override
    public int getColorTextureId() {
        return callOnTargetRet(RenderTarget::getColorTextureId);
    }

    @Override
    public int getDepthTextureId() {
        return callOnTargetRet(RenderTarget::getDepthTextureId);
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
