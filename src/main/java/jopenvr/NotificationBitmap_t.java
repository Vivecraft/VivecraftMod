package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class NotificationBitmap_t extends Structure
{
    public Pointer m_pImageData;
    public int m_nWidth;
    public int m_nHeight;
    public int m_nBytesPerPixel;

    public NotificationBitmap_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("m_pImageData", "m_nWidth", "m_nHeight", "m_nBytesPerPixel");
    }

    public NotificationBitmap_t(Pointer m_pImageData, int m_nWidth, int m_nHeight, int m_nBytesPerPixel)
    {
        this.m_pImageData = m_pImageData;
        this.m_nWidth = m_nWidth;
        this.m_nHeight = m_nHeight;
        this.m_nBytesPerPixel = m_nBytesPerPixel;
    }

    public NotificationBitmap_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends NotificationBitmap_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends NotificationBitmap_t implements com.sun.jna.Structure.ByValue
    {
    }
}
