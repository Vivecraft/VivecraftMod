package org.vivecraft.client.render;

import net.minecraft.world.entity.HumanoidArm;

public record VRPlayerRenderData(boolean isMainPlayer, float bodyYaw, float xRot,
                                 boolean laying, float layAmount, boolean swimming, boolean noLowerBodyAnimation,
                                 HumanoidArm attackArm, HumanoidArm mainArm,
                                 float bodyScale, float armScale, float legScale)
{}
