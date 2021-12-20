package org.vivecraft.asm.handler;

import org.objectweb.asm.tree.ClassNode;
import org.vivecraft.asm.ASMClassHandler;
import org.vivecraft.asm.ASMMethodHandler;

public class ASMHandlerFluidState extends ASMClassHandler
{
    public String getDesiredClass()
    {
        return "net/minecraft/fluid/FluidState";
    }

    protected void patchClassRoot(ClassNode classNode)
    {
        classNode.access &= -17;
        System.out.println("Made class non-final");
    }

    public ASMMethodHandler[] getMethodHandlers()
    {
        return new ASMMethodHandler[0];
    }
}
