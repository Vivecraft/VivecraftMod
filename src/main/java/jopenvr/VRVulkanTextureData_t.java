package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VRVulkanTextureData_t extends Structure
{
    public long m_nImage;
    public JOpenVRLibrary.VkDevice_T m_pDevice;
    public JOpenVRLibrary.VkPhysicalDevice_T m_pPhysicalDevice;
    public JOpenVRLibrary.VkInstance_T m_pInstance;
    public JOpenVRLibrary.VkQueue_T m_pQueue;
    public int m_nQueueFamilyIndex;
    public int m_nWidth;
    public int m_nHeight;
    public int m_nFormat;
    public int m_nSampleCount;

    public VRVulkanTextureData_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("m_nImage", "m_pDevice", "m_pPhysicalDevice", "m_pInstance", "m_pQueue", "m_nQueueFamilyIndex", "m_nWidth", "m_nHeight", "m_nFormat", "m_nSampleCount");
    }

    public VRVulkanTextureData_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VRVulkanTextureData_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VRVulkanTextureData_t implements com.sun.jna.Structure.ByValue
    {
    }
}
