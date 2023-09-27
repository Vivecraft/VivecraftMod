package org.vivecraft.mixin.client.gui.components.events;

import net.minecraft.client.gui.components.events.ContainerEventHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ContainerEventHandler.class)
public class ContainerEventHandlerVRMixin {

	/*@Inject(at = @At("HEAD"), method = "setInitialFocus(Lnet/minecraft/client/gui/components/events/GuiEventListener;)V")
	public void vivecraft$focus(GuiEventListener pEventListener, CallbackInfo info) {
		if (!ClientDataHolder.getInstance().vrSettings.seated)
		{
			info.cancel();
		}
	}*/
}
