package org.vivecraft.fabric;

import org.spongepowered.asm.mixin.Mixins;

public class VivecraftEarlyEntryForOptifabric implements Runnable {

    // add the mixins here, because optifabric loads the optifine classes before regular mixin loading
    @Override
    public void run() {
        Mixins.addConfiguration("vivecraft.optifine.mixins.json");
    }
}
