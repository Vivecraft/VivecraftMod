package org.vivecraft.mixin.client.main;

import com.google.common.base.Stopwatch;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.client_vr.ClientDataHolderVR;

@Mixin(Main.class)
public class MainMixin {

    @Inject(at = @At(value = "INVOKE", target = "Ljoptsimple/OptionParser;allowsUnrecognizedOptions()V"), method = "main", locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
    private static void vivecraft$options(String[] strings, CallbackInfo ci, Stopwatch stopwatch, Stopwatch stopwatch2, OptionParser optionparser) {
        optionparser.accepts("kiosk");
        optionparser.accepts("viewonly");
        optionparser.accepts("katvr");
        optionparser.accepts("infinadeck");
    }

    @Redirect(at = @At(value = "INVOKE", target = "Ljoptsimple/OptionParser;parse([Ljava/lang/String;)Ljoptsimple/OptionSet;", remap = false), method = "main", remap = false)
    private static OptionSet vivecraft$kiosk(OptionParser optionparser, String[] p_129642_) {
        OptionSet optionset = optionparser.parse(p_129642_);
        ClientDataHolderVR.kiosk = optionset.has("kiosk");

        if (ClientDataHolderVR.kiosk) {
            System.out.println("Setting kiosk");
        }

        if (ClientDataHolderVR.kiosk) {
            ClientDataHolderVR.viewonly = optionset.has("viewonly");

            if (ClientDataHolderVR.viewonly) {
                System.out.println("Setting viewonly");
            }
        }

        ClientDataHolderVR.katvr = optionset.has("katvr");
        ClientDataHolderVR.infinadeck = optionset.has("infinadeck");
        return optionset;
    }
}
