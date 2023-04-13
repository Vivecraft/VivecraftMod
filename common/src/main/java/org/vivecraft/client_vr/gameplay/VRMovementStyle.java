package org.vivecraft.client_vr.gameplay;

import org.vivecraft.client_vr.ClientDataHolderVR;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class VRMovementStyle
{
    public String name;
    public boolean cameraSlide;
    public boolean airSparkles;
    public boolean destinationSparkles;
    public boolean showBeam;
    public boolean beamWave;
    public boolean beamArc;
    public boolean beamSpiral;
    public boolean beamGrow;
    public boolean renderVerticalStrip;
    public float beamHalfWidth;
    public float beamSegmentLength;
    public float beamSpiralRadius;
    public int beamVStrips;
    public float textureScrollSpeed;
    public ResourceLocation texture;
    public String startTeleportingSound;
    public float startTeleportingSoundVolume;
    public String endTeleportingSound;
    public float endTeleportingSoundVolume;
    public boolean teleportOnRelease;
    public boolean arcAiming;
    public ClientDataHolderVR dataholder;
    private static final ResourceLocation beamPng = new ResourceLocation("textures/entity/endercrystal/endercrystal_beam.png");

    public VRMovementStyle(ClientDataHolderVR dataholder)
    {
        this.dataholder = dataholder;
        this.setStyle("Arc");
    }

    public void setStyle(String requestedStyle)
    {
        boolean flag = true;

        if (requestedStyle == "Minimal")
        {
            this.name = requestedStyle;
            this.cameraSlide = false;
            this.airSparkles = true;
            this.destinationSparkles = true;
            this.showBeam = false;
            this.startTeleportingSound = null;
            this.endTeleportingSoundVolume = 0.8F;
            this.endTeleportingSound = "mob.endermen.portal";
            this.teleportOnRelease = false;
            this.arcAiming = false;
        }
        else if (requestedStyle == "Beam")
        {
            this.name = requestedStyle;
            this.cameraSlide = false;
            this.airSparkles = true;
            this.destinationSparkles = true;
            this.showBeam = true;
            this.beamWave = false;
            this.beamArc = false;
            this.beamSpiral = false;
            this.beamGrow = true;
            this.beamHalfWidth = 0.1F;
            this.beamSegmentLength = 0.1F;
            this.beamVStrips = 16;
            this.renderVerticalStrip = true;
            this.textureScrollSpeed = 3.0F;
            this.texture = beamPng;
            this.startTeleportingSound = null;
            this.endTeleportingSoundVolume = 0.8F;
            this.endTeleportingSound = "mob.endermen.portal";
            this.teleportOnRelease = false;
            this.arcAiming = false;
        }
        else if (requestedStyle == "Tunnel")
        {
            this.name = requestedStyle;
            this.cameraSlide = false;
            this.airSparkles = true;
            this.destinationSparkles = true;
            this.showBeam = true;
            this.beamWave = false;
            this.beamArc = false;
            this.beamSpiral = true;
            this.beamGrow = true;
            this.beamHalfWidth = 0.1F;
            this.beamSpiralRadius = 1.6F;
            this.renderVerticalStrip = true;
            this.beamVStrips = 16;
            this.textureScrollSpeed = 3.0F;
            this.texture = beamPng;
            this.startTeleportingSound = null;
            this.endTeleportingSoundVolume = 0.8F;
            this.endTeleportingSound = "mob.endermen.portal";
            this.teleportOnRelease = false;
            this.arcAiming = false;
        }
        else if (requestedStyle == "Grapple")
        {
            this.name = requestedStyle;
            this.cameraSlide = true;
            this.airSparkles = false;
            this.destinationSparkles = true;
            this.showBeam = true;
            this.beamWave = true;
            this.beamArc = false;
            this.beamSpiral = false;
            this.beamGrow = true;
            this.beamHalfWidth = 0.05F;
            this.beamSegmentLength = 0.05F;
            this.renderVerticalStrip = false;
            this.beamVStrips = 2;
            this.textureScrollSpeed = 7.0F;
            this.texture = beamPng;
            this.startTeleportingSoundVolume = 0.5F;
            this.endTeleportingSoundVolume = 0.5F;
            this.startTeleportingSound = null;
            this.endTeleportingSound = "mob.endermen.portal";
            this.teleportOnRelease = false;
            this.arcAiming = false;
        }
        else if (requestedStyle == "Arc")
        {
            this.name = requestedStyle;
            this.cameraSlide = false;
            this.airSparkles = false;
            this.destinationSparkles = false;
            this.showBeam = true;
            this.beamWave = false;
            this.beamArc = false;
            this.beamSpiral = false;
            this.beamGrow = false;
            this.beamHalfWidth = 0.1F;
            this.beamVStrips = 1;
            this.renderVerticalStrip = true;
            this.textureScrollSpeed = 3.0F;
            this.texture = beamPng;
            this.startTeleportingSound = null;
            this.endTeleportingSoundVolume = 0.7F;
            this.endTeleportingSound = null;
            this.teleportOnRelease = true;
            this.arcAiming = true;
        }
        else
        {
            flag = false;
            ClientDataHolderVR.getInstance().printChatMessage("Unknown teleport style requested: " + requestedStyle);
        }

        if (flag && Minecraft.getInstance() != null && dataholder != null)
        {
        	dataholder.printChatMessage("Teleport style (RCTRL-M): " + this.name);
        }
    }
}
