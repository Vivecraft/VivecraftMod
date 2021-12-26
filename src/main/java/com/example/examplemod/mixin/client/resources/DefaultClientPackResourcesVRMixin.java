package com.example.examplemod.mixin.client.resources;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.client.resources.DefaultClientPackResources;

@Mixin(DefaultClientPackResources.class)
public class DefaultClientPackResourcesVRMixin {

	@ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/VanillaPackResources;<init>(Lnet/minecraft/server/packs/metadata/pack/PackMetadataSection;[Ljava/lang/String;)V"), method = "<init>(Lnet/minecraft/server/packs/metadata/pack/PackMetadataSection;Lnet/minecraft/client/resources/AssetIndex;)V")
	private static String[] init(String[] s) {
		String[] n = new String[] {
				"minecraft", "realms", "vivecraft"
		};
		return n;
	}
}
