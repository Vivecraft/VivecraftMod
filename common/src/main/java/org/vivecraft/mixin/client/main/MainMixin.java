package org.vivecraft.mixin.client.main;

import org.vivecraft.ClientDataHolder;
import org.spongepowered.asm.mixin.Mixin;
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

//Done
@Mixin(Main.class)
public class MainMixin {
	
	@Inject(at = @At(value = "INVOKE", target = "Ljoptsimple/OptionParser;allowsUnrecognizedOptions()V", remap = false), method = "run", locals = LocalCapture.CAPTURE_FAILHARD)
	private static void options(String[] strings, boolean bl, CallbackInfo ci, OptionParser optionparser) {
		optionparser.accepts("kiosk");
		optionparser.accepts("viewonly");
		optionparser.accepts("katvr");
		optionparser.accepts("infinadeck");
	}
	
	@ModifyConstant(method = "run", constant = @Constant(intValue = 854))
	private static int width(int i) {
		return 1280;
	}
	
	@ModifyConstant(method = "run", constant = @Constant(intValue = 480))
	private static int height(int i) {
		return 720;
	}
		
	@Redirect(at = @At(value = "INVOKE", target = "Ljoptsimple/OptionParser;parse([Ljava/lang/String;)Ljoptsimple/OptionSet;", remap = false) , method = "run")
	private static OptionSet kiosk(OptionParser optionparser, String[] p_129642_) {
		OptionSet optionset = optionparser.parse(p_129642_);
		ClientDataHolder.kiosk = optionset.has("kiosk");
		
		if (ClientDataHolder.kiosk)
		{
			System.out.println("Setting kiosk");
		}
		
		if (ClientDataHolder.kiosk)
		{
			ClientDataHolder.viewonly = optionset.has("viewonly");
			
			if (ClientDataHolder.viewonly)
			{
				System.out.println("Setting viewonly");
			}
		}

		ClientDataHolder.katvr = optionset.has("katvr");
		ClientDataHolder.infinadeck = optionset.has("infinadeck");
		return optionset;
	}
//	
//	@Redirect(at = @At("INVOKE"))
//	public static void headless(System system) {
//		return;
//	}
}
