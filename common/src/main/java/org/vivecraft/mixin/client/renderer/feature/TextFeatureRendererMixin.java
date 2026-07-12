package org.vivecraft.mixin.client.renderer.feature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.TextFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client.extensions.SubmitNodeCollectionExtension;
import org.vivecraft.client.extensions.TextFeatureRenderExtension;

import java.util.List;

@Mixin(TextFeatureRenderer.class)
public class TextFeatureRendererMixin implements TextFeatureRenderExtension {

    @Unique
    private boolean vivecraft$renderLateText = false;

    @Unique
    @Override
    public void vivecraft$setRenderLateText(boolean renderLateText) {
        this.vivecraft$renderLateText = renderLateText;
    }

    @ModifyExpressionValue(method = "renderTranslucent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollection;getTextSubmits()Ljava/util/List;"))
    private List<SubmitNodeStorage.TextSubmit> vivecraft$renderLateText(
        List<SubmitNodeStorage.TextSubmit> original, @Local(argsOnly = true) SubmitNodeCollection nodeCollection)
    {
        return this.vivecraft$renderLateText ?
            ((SubmitNodeCollectionExtension) nodeCollection).vivecraft$getLateTextSubmits() : original;
    }
}
