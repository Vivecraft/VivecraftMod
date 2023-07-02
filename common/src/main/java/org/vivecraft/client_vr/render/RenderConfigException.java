package org.vivecraft.client_vr.render;

public class RenderConfigException extends Exception
{
    public String title;
    public String error;

    public RenderConfigException(String title, String error)
    {
        this.title = title;
        this.error = error;
    }

    public String toString()
    {
        return this.error;
    }
}
