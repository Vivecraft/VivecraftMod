package org.vivecraft.client_vr.provider.openxr;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.joml.Vector2f;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.common.utils.math.Matrix4f;

import java.util.List;

public class MCOpenXR extends MCVR {

    private final MCOpenXR ome;

    public MCOpenXR(Minecraft mc, ClientDataHolderVR dh) {
        super(mc, dh, VivecraftVRMod.INSTANCE);
        this.ome = this;
        this.hapticScheduler = new OpenXRHapticSchedular();
    }

    @Override
    public String getName() {
        return "OpenXR";
    }

    @Override
    public String getID() {
        return "openxr";
    }

    @Override
    public void processInputs() {

    }

    @Override
    public void destroy() {

    }

    @Override
    protected void triggerBindingHapticPulse(KeyMapping var1, int var2) {

    }

    @Override
    protected ControllerType findActiveBindingControllerType(KeyMapping var1) {
        return null;
    }

    @Override
    public void poll(long var1) {

    }

    @Override
    public Vector2f getPlayAreaSize() {
        return null;
    }

    @Override
    public boolean init() {
        return false;
    }

    @Override
    public boolean postinit() throws RenderConfigException {
        return false;
    }

    @Override
    public Matrix4f getControllerComponentTransform(int var1, String var2) {
        return null;
    }

    @Override
    public boolean hasThirdController() {
        return false;
    }

    @Override
    public List<Long> getOrigins(VRInputAction var1) {
        return null;
    }

    @Override
    public String getOriginName(long l) {
        return null;
    }

    @Override
    public VRRenderer createVRRenderer() {
        return new OpenXRStereoRenderer(this);
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
