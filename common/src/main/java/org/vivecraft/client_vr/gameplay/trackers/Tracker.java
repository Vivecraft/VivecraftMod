package org.vivecraft.client_vr.gameplay.trackers;

public abstract class Tracker
{
    public boolean isActive()
    {
        return false;
    }

    public void doProcess()
    {
    }

    public void reset()
    {
    }

    public void idleTick()
    {
    }

    public EntryPoint getEntryPoint()
    {
        return EntryPoint.LIVING_UPDATE;
    }

    public enum EntryPoint
    {
        LIVING_UPDATE,
        SPECIAL_ITEMS
    }
}
