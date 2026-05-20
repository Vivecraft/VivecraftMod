package org.vivecraft.mixin.client_vr.renderer.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.cuboid.ItemModelGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.extensions.BlockModelWrapperExtension;

@Mixin(CuboidItemModelWrapper.Unbaked.class)
public class BlockModelWrapper$UnbakedVRMixin {
    @ModifyReturnValue(method = "bake", at = @At("RETURN"))
    private ItemModel vivecraft$setGenerated(ItemModel itemModel, @Local ResolvedModel resolvedModel) {
        if (itemModel instanceof BlockModelWrapperExtension blockModel) {
            while (resolvedModel != null &&
                !resolvedModel.debugName().equals(ItemModelGenerator.GENERATED_ITEM_MODEL_ID.toString())) {
                resolvedModel = resolvedModel.parent();
            }
            if (resolvedModel != null) {
                blockModel.vivecraft$setGenerated(true);
            }
        }
        return itemModel;
    }
}
