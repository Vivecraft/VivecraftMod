package org.vivecraft.client_vr.render.ubos;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.MappableRingBuffer;

public class LanczosUBO {
    public static final String UBO_NAME = "LanczosUbo";
    private static final int LANCZOS_UBO_SIZE = new Std140SizeCalculator().putFloat().putFloat().get();
    private final MappableRingBuffer lanczosBuffer;

    public LanczosUBO() {
        this.lanczosBuffer = new MappableRingBuffer(() -> "Lanczos UBO",
            GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, LANCZOS_UBO_SIZE);
    }

    public void updateBuffer(float texelWidthOffset, float texelHeightOffset) {
        try (GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder()
            .mapBuffer(this.lanczosBuffer.currentBuffer(), false, true))
        {
            Std140Builder.intoBuffer(mappedView.data())
                .putFloat(texelWidthOffset)
                .putFloat(texelHeightOffset);
        }
    }

    public GpuBuffer getBuffer() {
        return this.lanczosBuffer.currentBuffer();
    }

    public void endFrame() {
        this.lanczosBuffer.rotate();
    }

    public void close() {
        this.lanczosBuffer.close();
    }
}
