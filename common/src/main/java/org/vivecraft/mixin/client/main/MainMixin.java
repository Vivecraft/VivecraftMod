package org.vivecraft.mixin.client.main;

import org.vivecraft.client.utils.UpdateChecker;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraft.client.main.Main;

@Mixin(Main.class)
public class MainMixin {
	
	@Inject(at = @At(value = "INVOKE", target = "Ljoptsimple/OptionParser;allowsUnrecognizedOptions()V", remap = false), method = "run", locals = LocalCapture.CAPTURE_FAILHARD)
	private static void options(String[] strings, boolean bl, CallbackInfo ci, OptionParser optionparser) {
		optionparser.accepts("kiosk");
		optionparser.accepts("viewonly");
		optionparser.accepts("katvr");
		optionparser.accepts("infinadeck");
	}

	@Redirect(at = @At(value = "INVOKE", target = "Ljoptsimple/OptionParser;parse([Ljava/lang/String;)Ljoptsimple/OptionSet;", remap = false) , method = "run")
	private static OptionSet kiosk(OptionParser optionparser, String[] p_129642_) {
		new Thread(UpdateChecker::checkForUpdates).start();
		OptionSet optionset = optionparser.parse(p_129642_);
		ClientDataHolderVR.kiosk = optionset.has("kiosk");
		
		if (ClientDataHolderVR.kiosk)
		{
			System.out.println("Setting kiosk");
		}
		
		if (ClientDataHolderVR.kiosk)
		{
			ClientDataHolderVR.viewonly = optionset.has("viewonly");
			
			if (ClientDataHolderVR.viewonly)
			{
				System.out.println("Setting viewonly");
			}
		}

		ClientDataHolderVR.katvr = optionset.has("katvr");
		ClientDataHolderVR.infinadeck = optionset.has("infinadeck");
		return optionset;
	}
}
