package com.example.examplemod.mixin.client.main;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraft.client.main.Main;

@Mixin(Main.class)
public class MainMixin {
	
	@Unique
	private static boolean kiosk;
	@Unique
	private static boolean viewonly;
	@Unique
	private static boolean katvr;
	@Unique
	private static boolean infinadeck;
	
	@Inject(at = @At(value = "INVOKE", target = "Ljoptsimple/OptionParser;allowsUnrecognizedOptions()V", remap = false), method = "main([Ljava/lang/String;)V", locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
	private static void options(String[] p_129642_, CallbackInfo callback, OptionParser optionparser) {
		optionparser.accepts("kiosk");
		optionparser.accepts("viewonly");
		optionparser.accepts("katvr");
		optionparser.accepts("infinadeck");
	}
	
	@ModifyConstant(method = "main", constant = @Constant(intValue = 854), remap = false)
	private static int width(int i) {
		return 1280;
	}
	
	@ModifyConstant(method = "main", constant = @Constant(intValue = 480), remap = false)
	private static int height(int i) {
		return 720;
	}
		
	@Redirect(at = @At(value = "INVOKE", target = "Ljoptsimple/OptionParseroptionparse;([Ljava/lang/String;)Ljoptsimple/OptionSet;", remap = false) , method = "main([Ljava/lang/String;)V", remap = false)
	private static OptionSet kiosk(OptionParser optionparser, String[] p_129642_) {
		OptionSet optionset = optionparser.parse(p_129642_);
		kiosk = optionset.has("kiosk");
		
		if (kiosk)
		{
			System.out.println("Setting kiosk");
		}
		
		if (kiosk)
		{
			viewonly = optionset.has("viewonly");
			
			if (viewonly)
			{
				System.out.println("Setting viewonly");
			}
		}
		
		katvr = optionset.has("katvr");
		infinadeck = optionset.has("infinadeck");
		return optionset;
	}
//	
//	@Redirect(at = @At("INVOKE"))
//	public static void headless(System system) {
//		return;
//	}
}
