package org.vivecraft.asm.handler;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.vivecraft.asm.ASMClassHandler;
import org.vivecraft.asm.ASMMethodHandler;
import org.vivecraft.asm.ASMUtil;
import org.vivecraft.asm.MethodTuple;

public class ASMHandlerContainerScreen extends ASMClassHandler
{
    public String getDesiredClass()
    {
        return "net/minecraft/client/gui/screen/inventory/ContainerScreen";
    }

    public ASMMethodHandler[] getMethodHandlers()
    {
        return new ASMMethodHandler[] {new ASMHandlerContainerScreen.DragSplitFixMethodHandler()};
    }

    public static class DragSplitFixMethodHandler implements ASMMethodHandler
    {
        public MethodTuple getDesiredMethod()
        {
            return new MethodTuple("mouseDragged", "(DDIDD)Z");
        }

        public void patchMethod(MethodNode methodNode, ClassNode classNode)
        {
            AbstractInsnNode abstractinsnnode = ASMUtil.findFirstInstruction(methodNode, 180, "net/minecraft/client/gui/screen/inventory/ContainerScreen", "field_147007_t", "Z");
            LabelNode labelnode = ((JumpInsnNode)methodNode.instructions.get(methodNode.instructions.indexOf(abstractinsnnode) + 1)).label;
            InsnList insnlist = new InsnList();
            insnlist.add(new MethodInsnNode(184, "net/minecraft/client/gui/screen/Screen", "hasShiftDown", "()Z", false));
            insnlist.add(new JumpInsnNode(154, labelnode));
            ASMUtil.insertInstructionsRelative(methodNode, abstractinsnnode, -2, insnlist);
            System.out.println("Inserted hasShiftDown call");
        }
    }
}
