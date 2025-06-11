package org.vivecraft.client_vr;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.client_xr.render_pass.WorldRenderPass;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A TextureTarget that holds multiple TextureTargets for each RenderPass, and delegates calls to the current active one
 * this assumes that the TextureTarget gets at least cleared/bound or something, before its width/height gets accessed
 */
public class MultiPassTextureTarget extends TextureTarget {

    // when either of those is set, methods will use their corresponding TextureTarget, instead of the current pass one
    private boolean isVanilla = false;
    private RenderPass passOverride = null;

    private RenderTarget last = null;

    // gets set after super call, so when it is null, it can be assumed to not be ready yet
    @Nullable
    private final EnumMap<RenderPass, TextureTarget> vrTargets;

    private final TextureTarget vanilla;

    public MultiPassTextureTarget(int width, int height, boolean useDepth) {
        super(width, height, useDepth);
        super.destroyBuffers();

        this.vrTargets = new EnumMap<>(RenderPass.class);

        this.isVanilla = true;
        this.vanilla = new TextureTarget(width, height, useDepth);
        this.isVanilla = false;

        for (RenderPass pass : RenderPass.values()) {
            // create one TextureTarget for each active render pass
            WorldRenderPass worldPass = WorldRenderPass.getByRenderPass(pass);
            if (worldPass == null) continue;
            RenderTarget original = worldPass.target;
            this.vrTargets.put(pass, new TextureTarget(original.width, original.height, useDepth));
        }
        // set vanilla as default
        setLast(this.vanilla);
    }

    @Override
    public void resize(int width, int height) {
        if (this.vrTargets == null) {
            super.resize(width, height);
            return;
        }

        // resize all targets to their main counterpart
        this.isVanilla = true;
        this.vanilla.resize(width, height);
        this.isVanilla = false;
        for (Map.Entry<RenderPass, TextureTarget> entry : this.vrTargets.entrySet()) {
            WorldRenderPass pass = WorldRenderPass.getByRenderPass(entry.getKey());
            if (pass != null) {
                this.passOverride = entry.getKey();
                entry.getValue().resize(pass.target.width, pass.target.height);
                this.passOverride = null;
            }
        }
    }

    @Override
    public void destroyBuffers() {
        if (this.vrTargets == null) {
            super.destroyBuffers();
            return;
        }
        // this one should be called on all TextureTargets
        callOnAllTarget(TextureTarget::destroyBuffers);
    }

    @Override
    public void copyDepthFrom(RenderTarget otherTarget) {
        if (this.vrTargets == null) {
            super.copyDepthFrom(otherTarget);
            return;
        }
        callOnTarget(r -> r.copyDepthFrom(otherTarget));
    }

    @Override
    public void createBuffers(int width, int height) {
        if (this.vrTargets == null) {
            super.createBuffers(width, height);
            return;
        }
        callOnTarget(r -> r.createBuffers(width, height));
    }

    @Override
    public void setFilterMode(int filterMode) {
        if (this.vrTargets == null) {
            super.setFilterMode(filterMode);
            return;
        }
        callOnTarget(r -> r.setFilterMode(filterMode));
    }

    @Override
    public void checkStatus() {
        if (this.vrTargets == null) {
            super.checkStatus();
            return;
        }
        callOnTarget(TextureTarget::checkStatus);
    }

    @Override
    public void bindRead() {
        if (this.vrTargets == null) {
            super.bindRead();
            return;
        }
        callOnTarget(TextureTarget::bindRead);
    }

    @Override
    public void unbindRead() {
        if (this.vrTargets == null) {
            super.unbindRead();
            return;
        }
        callOnTarget(TextureTarget::unbindRead);
    }

    @Override
    public void bindWrite(boolean setViewport) {
        if (this.vrTargets == null) {
            super.bindWrite(setViewport);
            return;
        }
        callOnTarget(r -> r.bindWrite(setViewport));
    }

    @Override
    public void unbindWrite() {
        if (this.vrTargets == null) {
            super.unbindWrite();
            return;
        }
        callOnTarget(TextureTarget::unbindWrite);
    }

    @Override
    public void setClearColor(float red, float green, float blue, float alpha) {
        if (this.vrTargets == null) {
            super.setClearColor(red, green, blue, alpha);
            return;
        }
        // this one should be called on all TextureTargets
        callOnAllTarget(r -> r.setClearColor(red, green, blue, alpha));
    }

    @Override
    public void blitToScreen(int width, int height) {
        if (this.vrTargets == null) {
            super.blitToScreen(width, height);
            return;
        }
        callOnTarget(r -> r.blitToScreen(width, height));
    }

    @Override
    public void blitAndBlendToScreen(int width, int height) {
        if (this.vrTargets == null) {
            super.blitAndBlendToScreen(width, height);
            return;
        }
        callOnTarget(r -> r.blitAndBlendToScreen(width, height));
    }

    @Override
    public void clear() {
        if (this.vrTargets == null) {
            super.clear();
            return;
        }
        callOnTarget(TextureTarget::clear);
    }

    @Override
    public int getColorTextureId() {
        if (this.vrTargets == null) {
            return super.getColorTextureId();
        }
        return callOnTargetInt(TextureTarget::getColorTextureId);
    }

    @Override
    public int getDepthTextureId() {
        if (this.vrTargets == null) {
            return super.getDepthTextureId();
        }
        return callOnTargetInt(TextureTarget::getDepthTextureId);
    }

    private void callOnAllTarget(Consumer<TextureTarget> consumer) {
        this.isVanilla = true;
        consumer.accept(this.vanilla);
        this.isVanilla = false;
        for (Map.Entry<RenderPass, TextureTarget> entry : this.vrTargets.entrySet()) {
            this.passOverride = entry.getKey();
            consumer.accept(entry.getValue());
            this.passOverride = null;
        }
    }

    private void callOnTarget(Consumer<TextureTarget> consumer) {
        TextureTarget current = getCurrent();
        if (current != this.last) {
            setLast(current);
        }
        consumer.accept(current);
    }

    private int callOnTargetInt(Function<TextureTarget, Integer> function) {
        TextureTarget current = getCurrent();
        if (current != this.last) {
            setLast(current);
        }
        return function.apply(current);
    }

    /**
     * @return the TextureTarget that should be rendered to now
     */
    private TextureTarget getCurrent() {
        if (this.isVanilla || RenderPassType.isVanilla()) {
            return this.vanilla;
        } else {
            return this.vrTargets.get(
                this.passOverride != null ? this.passOverride : ClientDataHolderVR.getInstance().currentPass);
        }
    }

    /**
     * sets the public fields to the ones of the provided TextureTarget
     *
     * @param current TextureTarget to set
     */
    private void setLast(TextureTarget current) {
        this.last = current;
        this.width = current.width;
        this.height = current.height;
        this.viewWidth = current.viewWidth;
        this.viewHeight = current.viewHeight;
        this.frameBufferId = current.frameBufferId;
        this.filterMode = current.filterMode;
    }
}
