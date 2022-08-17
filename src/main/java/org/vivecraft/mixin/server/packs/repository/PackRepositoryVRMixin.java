package org.vivecraft.mixin.server.packs.repository;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.vivecraft.DataHolder;
import com.google.common.collect.Lists;

import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;

@Mixin(PackRepository.class)
public class PackRepositoryVRMixin {
	
	@Shadow
	@Final
	private Set<RepositorySource> sources;
	
	@Shadow
	private List<Pack> selected;

	@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/server/packs/repository/Pack$PackConstructor;[Lnet/minecraft/server/packs/repository/RepositorySource;)V")
	public void sources(Pack.PackConstructor packConstructor, RepositorySource[] repositorySources, CallbackInfo info) {
		 this.sources = new HashSet<>(Arrays.asList(repositorySources));
	}
	
	@Inject(at = @At("TAIL"), method = "reload()V")
	public void reload(CallbackInfo info) {
		if (!this.selected.equals(Lists.newLinkedList(this.selected))) {
			DataHolder.getInstance().resourcePacksChanged = true;
		}
	}
	
	@Inject(at = @At("TAIL"), method = "setSelected(Ljava/util/Collection;)V")
	public void selected(Collection<String> c, CallbackInfo info) {
		if (!this.selected.equals(Lists.newLinkedList(this.selected))) {
			DataHolder.getInstance().resourcePacksChanged = true;
		}
	}

}
