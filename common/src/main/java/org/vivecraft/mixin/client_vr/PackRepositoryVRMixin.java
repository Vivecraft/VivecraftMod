package org.vivecraft.mixin.client_vr;

import java.util.Collection;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Lists;

import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import org.vivecraft.common.CommonDataHolder;

@Mixin(PackRepository.class)
public class PackRepositoryVRMixin {
	
	@Shadow
	private List<Pack> selected;
	
	@Inject(at = @At("TAIL"), method = "reload()V")
	public void reload(CallbackInfo info) {
		if (!this.selected.equals(Lists.newLinkedList(this.selected))) {
			CommonDataHolder.getInstance().resourcePacksChanged = true;
		}
	}
	
	@Inject(at = @At("TAIL"), method = "setSelected(Ljava/util/Collection;)V")
	public void selected(Collection<String> c, CallbackInfo info) {
		if (!this.selected.equals(Lists.newLinkedList(this.selected))) {
			CommonDataHolder.getInstance().resourcePacksChanged = true;
		}
	}

}
