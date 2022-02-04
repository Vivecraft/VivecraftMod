package org.vivecraft.gameplay.trackers;

import com.example.examplemod.DataHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public abstract class Tracker
{
    public Minecraft mc;
    public DataHolder dh;


    public Tracker(Minecraft mc, DataHolder dh)
    {
        this.mc = mc;
        this.dh = dh;
    }

    public abstract boolean isActive(LocalPlayer var1);

    public abstract void doProcess(LocalPlayer var1);

    public void reset(LocalPlayer player)
    {
    }

    public void idleTick(LocalPlayer player)
    {
    }

    public Tracker.EntryPoint getEntryPoint()
    {
        return Tracker.EntryPoint.LIVING_UPDATE;
    }

    public static enum EntryPoint
    {
        LIVING_UPDATE,
        SPECIAL_ITEMS;
    }
}
