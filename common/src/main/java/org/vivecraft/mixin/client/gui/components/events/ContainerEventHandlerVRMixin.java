package org.vivecraft.mixin.client.gui.components.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.vivecraft.ClientDataHolder;

import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;

@Mixin(ContainerEventHandler.class)
public class ContainerEventHandlerVRMixin {

	/*@Inject(at = @At("HEAD"), method = "setInitialFocus(Lnet/minecraft/client/gui/components/events/GuiEventListener;)V")
	public void focus(GuiEventListener pEventListener, CallbackInfo info) {
		if (!ClientDataHolder.getInstance().vrSettings.seated)
		{
			info.cancel();
		}
	}*/
}
