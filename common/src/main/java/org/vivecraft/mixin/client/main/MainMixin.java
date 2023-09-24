package org.vivecraft.mixin.client.main;

import org.vivecraft.client.utils.UpdateChecker;

import com.google.common.base.Stopwatch;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.common.utils.Utils.logger;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(net.minecraft.client.main.Main.class)
public class MainMixin {
	
	@Inject(at = @At(value = "INVOKE", target = "Ljoptsimple/OptionParser;allowsUnrecognizedOptions()V"), method = "main", locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
	private static void options(String[] strings, CallbackInfo ci, Stopwatch stopwatch, Stopwatch stopwatch2, OptionParser optionparser) {
		optionparser.accepts("kiosk");
		optionparser.accepts("viewonly");
		optionparser.accepts("katvr");
		optionparser.accepts("infinadeck");
	}

	@Redirect(at = @At(value = "INVOKE", target = "Ljoptsimple/OptionParser;parse([Ljava/lang/String;)Ljoptsimple/OptionSet;", remap = false) , method = "main", remap = false)
	private static OptionSet kiosk(OptionParser optionparser, String[] cmdOptions) {
		new Thread(UpdateChecker::checkForUpdates).start();
		OptionSet optionset = optionparser.parse(cmdOptions);
		dh.kiosk = optionset.has("kiosk");
		
		if (dh.kiosk)
		{
			logger.info("Setting kiosk");
		}
		
		if (dh.kiosk)
		{
			dh.viewonly = optionset.has("viewonly");
			
			if (dh.viewonly)
			{
				logger.info("Setting viewonly");
			}
		}

		dh.katvr = optionset.has("katvr");
		dh.infinadeck = optionset.has("infinadeck");
		return optionset;
	}
}
