package org.vivecraft.mixin.client.main;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;

@Mixin(Main.class)
public class MainMixin {

    @Inject(at = @At("HEAD"), method = "main", remap = false)
    private static void vivecraft$options(String[] strings, CallbackInfo ci) {
        OptionParser vivecraftOptionParser = new OptionParser();
        vivecraftOptionParser.allowsUnrecognizedOptions();
        vivecraftOptionParser.accepts("kiosk");
        vivecraftOptionParser.accepts("viewonly");
        vivecraftOptionParser.accepts("katvr");
        vivecraftOptionParser.accepts("infinadeck");
        OptionSet optionset = vivecraftOptionParser.parse(strings);
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
    }
}
