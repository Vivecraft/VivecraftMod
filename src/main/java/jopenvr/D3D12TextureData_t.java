package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class D3D12TextureData_t extends Structure
{
    public JOpenVRLibrary.ID3D12Resource m_pResource;
    public JOpenVRLibrary.ID3D12CommandQueue m_pCommandQueue;
    public int m_nNodeMask;

    public D3D12TextureData_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("m_pResource", "m_pCommandQueue", "m_nNodeMask");
    }

    public D3D12TextureData_t(JOpenVRLibrary.ID3D12Resource m_pResource, JOpenVRLibrary.ID3D12CommandQueue m_pCommandQueue, int m_nNodeMask)
    {
        this.m_pResource = m_pResource;
        this.m_pCommandQueue = m_pCommandQueue;
        this.m_nNodeMask = m_nNodeMask;
    }

    public D3D12TextureData_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends D3D12TextureData_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends D3D12TextureData_t implements com.sun.jna.Structure.ByValue
    {
    }
}
