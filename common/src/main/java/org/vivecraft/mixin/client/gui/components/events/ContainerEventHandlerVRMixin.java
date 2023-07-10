package org.vivecraft.mixin.client.gui.components.events;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;

@Mixin(ContainerEventHandler.class)
public class ContainerEventHandlerVRMixin {

	@Inject(at = @At("HEAD"), method = "setInitialFocus(Lnet/minecraft/client/gui/components/events/GuiEventListener;)V", cancellable = true)
	public void focus(GuiEventListener pEventListener, CallbackInfo info) {
		if (VRState.vrRunning && !ClientDataHolderVR.getInstance().vrSettings.seated)
		{
			info.cancel();
		}
	}
}
