package org.vivecraft.asm.handler;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.vivecraft.asm.ASMClassHandler;
import org.vivecraft.asm.ASMMethodHandler;
import org.vivecraft.asm.ASMUtil;
import org.vivecraft.asm.MethodTuple;

public class ASMHandlerCreativeScreen extends ASMClassHandler
{
    public String getDesiredClass()
    {
        return "net/minecraft/client/gui/screen/inventory/CreativeScreen";
    }

    public ASMMethodHandler[] getMethodHandlers()
    {
        return new ASMMethodHandler[] {new ASMHandlerCreativeScreen.AddTabsMethodHandler(), new ASMHandlerCreativeScreen.AddSearchMethodHandler()};
    }

    public static class AddSearchMethodHandler implements ASMMethodHandler
    {
        public MethodTuple getDesiredMethod()
        {
            return new MethodTuple("func_147053_i", "()V");
        }

        public void patchMethod(MethodNode methodNode, ClassNode classNode)
        {
            InsnList insnlist = new InsnList();
            insnlist.add(new VarInsnNode(25, 0));
            insnlist.add(new FieldInsnNode(180, "net/minecraft/client/gui/screen/inventory/CreativeScreen", "field_147062_A", "Lnet/minecraft/client/gui/widget/TextFieldWidget;"));
            insnlist.add(new MethodInsnNode(182, "net/minecraft/client/gui/widget/TextFieldWidget", "func_146179_b", "()Ljava/lang/String;", false));
            insnlist.add(new VarInsnNode(25, 0));
            insnlist.add(new FieldInsnNode(180, "net/minecraft/client/gui/screen/inventory/CreativeScreen", "field_147002_h", "Lnet/minecraft/inventory/container/Container;"));
            insnlist.add(new TypeInsnNode(192, "net/minecraft/client/gui/screen/inventory/CreativeScreen$CreativeContainer"));
            insnlist.add(new FieldInsnNode(180, "net/minecraft/client/gui/screen/inventory/CreativeScreen$CreativeContainer", "field_148330_a", "Lnet/minecraft/util/NonNullList;"));
            insnlist.add(new MethodInsnNode(184, "org/vivecraft/asm/ASMDelegator", "addCreativeSearch", "(Ljava/lang/String;Lnet/minecraft/util/NonNullList;)V", false));
            AbstractInsnNode abstractinsnnode = ASMUtil.findNthInstruction(methodNode, 1, 181, "net/minecraft/client/gui/screen/inventory/CreativeScreen", "field_147067_x", "F");
            ASMUtil.insertInstructionsRelative(methodNode, abstractinsnnode, -3, insnlist);
            System.out.println("Inserted call to delegator");
        }
    }

    public static class AddTabsMethodHandler implements ASMMethodHandler
    {
        public MethodTuple getDesiredMethod()
        {
            return new MethodTuple("func_147050_b", "(Lnet/minecraft/item/ItemGroup;)V");
        }

        public void patchMethod(MethodNode methodNode, ClassNode classNode)
        {
            InsnList insnlist = new InsnList();
            insnlist.add(new VarInsnNode(25, 1));
            insnlist.add(new VarInsnNode(25, 0));
            insnlist.add(new FieldInsnNode(180, "net/minecraft/client/gui/screen/inventory/CreativeScreen", "field_147002_h", "Lnet/minecraft/inventory/container/Container;"));
            insnlist.add(new TypeInsnNode(192, "net/minecraft/client/gui/screen/inventory/CreativeScreen$CreativeContainer"));
            insnlist.add(new FieldInsnNode(180, "net/minecraft/client/gui/screen/inventory/CreativeScreen$CreativeContainer", "field_148330_a", "Lnet/minecraft/util/NonNullList;"));
            insnlist.add(new MethodInsnNode(184, "org/vivecraft/asm/ASMDelegator", "addCreativeItems", "(Lnet/minecraft/item/ItemGroup;Lnet/minecraft/util/NonNullList;)V", false));
            AbstractInsnNode abstractinsnnode = ASMUtil.findFirstInstruction(methodNode, 182, "net/minecraft/item/ItemGroup", "func_78018_a", "(Lnet/minecraft/util/NonNullList;)V", false);
            methodNode.instructions.insert(abstractinsnnode, insnlist);
            System.out.println("Inserted call to delegator");
        }
    }
}
