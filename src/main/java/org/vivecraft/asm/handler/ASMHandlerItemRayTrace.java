package org.vivecraft.asm.handler;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.vivecraft.asm.ASMClassHandler;
import org.vivecraft.asm.ASMMethodHandler;
import org.vivecraft.asm.ASMUtil;
import org.vivecraft.asm.MethodTuple;

public class ASMHandlerItemRayTrace extends ASMClassHandler
{
    public String getDesiredClass()
    {
        return "net/minecraft/item/Item";
    }

    public ASMMethodHandler[] getMethodHandlers()
    {
        return new ASMMethodHandler[] {new ASMHandlerItemRayTrace.RayTraceMethodHandler()};
    }

    public static class RayTraceMethodHandler implements ASMMethodHandler
    {
        public MethodTuple getDesiredMethod()
        {
            return new MethodTuple("func_219968_a", "(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/RayTraceContext$FluidMode;)Lnet/minecraft/util/math/BlockRayTraceResult;");
        }

        public void patchMethod(MethodNode methodNode, ClassNode classNode)
        {
            AbstractInsnNode abstractinsnnode = ASMUtil.findFirstInstruction(methodNode, 58, 5);
            InsnList insnlist = new InsnList();
            insnlist.add(new VarInsnNode(25, 1));
            insnlist.add(new VarInsnNode(23, 3));
            insnlist.add(new MethodInsnNode(184, "org/vivecraft/asm/ASMDelegator", "itemRayTracePitch", "(Lnet/minecraft/entity/player/PlayerEntity;F)F", false));
            insnlist.add(new VarInsnNode(56, 3));
            insnlist.add(new VarInsnNode(25, 1));
            insnlist.add(new VarInsnNode(23, 4));
            insnlist.add(new MethodInsnNode(184, "org/vivecraft/asm/ASMDelegator", "itemRayTraceYaw", "(Lnet/minecraft/entity/player/PlayerEntity;F)F", false));
            insnlist.add(new VarInsnNode(56, 4));
            insnlist.add(new VarInsnNode(25, 1));
            insnlist.add(new VarInsnNode(25, 5));
            insnlist.add(new MethodInsnNode(184, "org/vivecraft/asm/ASMDelegator", "itemRayTracePos", "(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/vector/Vector3d;)Lnet/minecraft/util/math/vector/Vector3d;", false));
            insnlist.add(new VarInsnNode(58, 5));
            methodNode.instructions.insert(abstractinsnnode, insnlist);
            System.out.println("Inserted raytrace override");
        }
    }
}
