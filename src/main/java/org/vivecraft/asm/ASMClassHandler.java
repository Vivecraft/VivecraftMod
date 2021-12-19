package org.vivecraft.asm;

import java.util.ArrayList;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class ASMClassHandler
{
    public abstract String getDesiredClass();

    public abstract ASMMethodHandler[] getMethodHandlers();

    public boolean shouldPatchClass()
    {
        return true;
    }

    protected void patchClassRoot(ClassNode classNode)
    {
    }

    public final void patchClass(ClassNode classNode)
    {
        this.patchClassRoot(classNode);
        ASMMethodHandler[] aasmmethodhandler = this.getMethodHandlers();

        for (MethodNode methodnode : new ArrayList<MethodNode>(classNode.methods))
        {
            for (ASMMethodHandler asmmethodhandler : aasmmethodhandler)
            {
                MethodTuple methodtuple = asmmethodhandler.getDesiredMethod();

                if (methodnode.name.equals(methodtuple.methodName) && methodnode.desc.equals(methodtuple.methodDesc))
                {
                    System.out.println("Patching method: " + methodnode.name + methodnode.desc);
                    asmmethodhandler.patchMethod(methodnode, classNode);
                }
            }
        }
    }
}
