package com.example.vivecraftfabric.mixin.client.resources.language;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.utils.LangHelper;

import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.server.packs.resources.ResourceManager;

@Mixin(ClientLanguage.class)
public class ClientLanguageMixin {

	//TODO replace
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/language/ClientLanguage;appendFrom(Ljava/util/List;Ljava/util/Map;)V", shift = Shift.AFTER), method = "loadFrom(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;)Lnet/minecraft/client/resources/language/ClientLanguage;", 
			cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private static void load(ResourceManager p_118917_, List<LanguageInfo> p_118918_, CallbackInfoReturnable<ClientLanguage> info, Map<String, String> map, boolean flag, Iterator iter, LanguageInfo languageinfo) {
		 LangHelper.loadLocaleData(languageinfo.getCode(), map);
	}
}
