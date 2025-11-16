package org.vivecraft.client_vr.render.ubos;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.MappableRingBuffer;

public class PostProcessUBO {
    public static final String UBO_NAME = "PostProcessUbo";
    private static final int POST_PROCESS_UBO_SIZE = new Std140SizeCalculator().putFloat().putFloat().putFloat()
        .putFloat().putFloat().putFloat().putFloat().putFloat().putFloat().putFloat().putInt().get();
    private final MappableRingBuffer postProcessBuffer;

    public PostProcessUBO() {
        this.postProcessBuffer = new MappableRingBuffer(() -> "PostProcess UBO",
            GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, POST_PROCESS_UBO_SIZE);
    }

    public void updateBuffer(
        float circle_radius, float circle_offset, float border, float water, float portal, float portalTime,
        float pumpkin, float redAlpha, float blueAlpha, float blackAlpha, int eye)
    {
        try (GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder()
            .mapBuffer(this.postProcessBuffer.currentBuffer(), false, true))
        {
            Std140Builder.intoBuffer(mappedView.data())
                .putFloat(circle_radius)
                .putFloat(circle_offset)
                .putFloat(border)
                .putFloat(water)
                .putFloat(pumpkin)
                .putFloat(portal)
                .putFloat(portalTime)
                .putFloat(redAlpha)
                .putFloat(blueAlpha)
                .putFloat(blackAlpha)
                .putInt(eye);
        }
    }

    public GpuBuffer getBuffer() {
        return this.postProcessBuffer.currentBuffer();
    }

    public void endFrame() {
        this.postProcessBuffer.rotate();
    }

    public void close() {
        this.postProcessBuffer.close();
    }
}
