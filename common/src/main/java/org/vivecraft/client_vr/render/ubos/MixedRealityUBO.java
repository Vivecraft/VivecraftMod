package org.vivecraft.client_vr.render.ubos;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.MappableRingBuffer;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;

public class MixedRealityUBO {
    public static final String UBO_NAME = "MixedRealityUbo";
    private static final int MIXED_REALITY_UBO_SIZE = new Std140SizeCalculator()
        .putMat4f().putMat4f().putVec4().putVec4().putVec4().putInt().putInt().putInt().get();
    private final MappableRingBuffer mixedRealityBuffer;

    public MixedRealityUBO() {
        this.mixedRealityBuffer = new MappableRingBuffer(() -> "MixedReality UBO",
            GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, MIXED_REALITY_UBO_SIZE);
    }

    public void updateBuffer(
        Matrix4fc thirdProjectionMat, Matrix4fc thirdViewMat, Vector3fc hmdViewPosition,
        Vector3fc hmdPlaneNormal, boolean firstPersonPass, Vector3fc keyColor, boolean alphaMode, int guiMask)
    {
        try (GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder()
            .mapBuffer(this.mixedRealityBuffer.currentBuffer(), false, true))
        {
            Std140Builder.intoBuffer(mappedView.data())
                .putMat4f(thirdProjectionMat)
                .putMat4f(thirdViewMat)
                .putVec4(keyColor.x(), keyColor.y(), keyColor.z(), 0)
                .putVec4(hmdViewPosition.x(), hmdViewPosition.y(), hmdViewPosition.z(), 0)
                .putVec4(hmdPlaneNormal.x(), hmdPlaneNormal.y(), hmdPlaneNormal.z(), 0)
                .putInt(alphaMode ? 1 : 0)
                .putInt(firstPersonPass ? 1 : 0)
                .putInt(guiMask);
        }
    }

    public GpuBuffer getBuffer() {
        return this.mixedRealityBuffer.currentBuffer();
    }

    public void endFrame() {
        this.mixedRealityBuffer.rotate();
    }

    public void close() {
        this.mixedRealityBuffer.close();
    }
}
