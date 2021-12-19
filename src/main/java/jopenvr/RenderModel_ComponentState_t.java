package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class RenderModel_ComponentState_t extends Structure
{
    public HmdMatrix34_t mTrackingToComponentRenderModel;
    public HmdMatrix34_t mTrackingToComponentLocal;
    public int uProperties;

    public RenderModel_ComponentState_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("mTrackingToComponentRenderModel", "mTrackingToComponentLocal", "uProperties");
    }

    public RenderModel_ComponentState_t(HmdMatrix34_t mTrackingToComponentRenderModel, HmdMatrix34_t mTrackingToComponentLocal, int uProperties)
    {
        this.mTrackingToComponentRenderModel = mTrackingToComponentRenderModel;
        this.mTrackingToComponentLocal = mTrackingToComponentLocal;
        this.uProperties = uProperties;
    }

    public RenderModel_ComponentState_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends RenderModel_ComponentState_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends RenderModel_ComponentState_t implements com.sun.jna.Structure.ByValue
    {
    }
}
