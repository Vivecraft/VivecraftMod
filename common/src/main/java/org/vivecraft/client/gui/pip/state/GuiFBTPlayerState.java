package org.vivecraft.client.gui.pip.state;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.joml.Vector3fc;

import javax.annotation.Nullable;

public record GuiFBTPlayerState(boolean rightReady, boolean leftReady, Vector3fc right, Vector3fc left, float yRot,
                                int x0, int y0, int x1, int y1, float scale,
                                ScreenRectangle bounds) implements PictureInPictureRenderState
{
    public GuiFBTPlayerState(
        boolean rightReady, boolean leftReady, Vector3fc right, Vector3fc left, float yRot, int x0, int y0, int x1,
        int y1)
    {
        this(rightReady, leftReady, right, left, yRot, x0, y0, x1, y1, 1,
            new ScreenRectangle(x0, y0, x1 - x0, y1 - y0));
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return this.bounds;
    }
}
