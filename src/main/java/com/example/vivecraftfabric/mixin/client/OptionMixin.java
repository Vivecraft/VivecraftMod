package com.example.vivecraftfabric.mixin.client;

import com.example.vivecraftfabric.DataHolder;
import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.CycleOption;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Mixin(Option.class)
public abstract class OptionMixin {

    @Final
    @Shadow
    private static Component GRAPHICS_TOOLTIP_FAST;

    @Final
    @Shadow
    private static Component GRAPHICS_TOOLTIP_FANCY;

    @Final
    @Shadow
    private static Component GRAPHICS_TOOLTIP_FABULOUS;


    @Final
    @Mutable
    @Shadow
    public static CycleOption<GraphicsStatus> GRAPHICS = CycleOption.create("options.graphics", Arrays.asList(GraphicsStatus.values()), Stream.of(GraphicsStatus.values()).filter(graphicsStatus -> graphicsStatus != GraphicsStatus.FABULOUS).collect(Collectors.toList()), () -> Minecraft.getInstance().getGpuWarnlistManager().isSkippingFabulous(), graphicsStatus -> {
        TranslatableComponent mutableComponent = new TranslatableComponent(graphicsStatus.getKey());
        if (graphicsStatus == GraphicsStatus.FABULOUS) {
            return mutableComponent.withStyle(ChatFormatting.ITALIC);
        }
        return mutableComponent;
    }, options -> options.graphicsMode, (options, option, graphicsStatus) -> {
        Minecraft minecraft = Minecraft.getInstance();
        GpuWarnlistManager gpuWarnlistManager = minecraft.getGpuWarnlistManager();
        if (graphicsStatus == GraphicsStatus.FABULOUS && gpuWarnlistManager.willShowWarning()) {
            gpuWarnlistManager.showWarning();
            return;
        }
        options.graphicsMode = graphicsStatus;
        minecraft.levelRenderer.allChanged();
        DataHolder.getInstance().vrRenderer.reinitFrameBuffers("gfx setting change");
    }).setTooltip(minecraft -> {
        List<FormattedCharSequence> list = minecraft.font.split(GRAPHICS_TOOLTIP_FAST, 200);
        List<FormattedCharSequence> list2 = minecraft.font.split(GRAPHICS_TOOLTIP_FANCY, 200);
        List<FormattedCharSequence> list3 = minecraft.font.split(GRAPHICS_TOOLTIP_FABULOUS, 200);
        return graphicsStatus -> {
            switch (graphicsStatus) {
                case FANCY: {
                    return list2;
                }
                case FAST: {
                    return list;
                }
                case FABULOUS: {
                    return list3;
                }
            }
            return ImmutableList.of();
        };
    });
}
