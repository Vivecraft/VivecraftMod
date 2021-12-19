package org.vivecraft.asm;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public interface ASMMethodHandler
{
    MethodTuple getDesiredMethod();

    void patchMethod(MethodNode var1, ClassNode var2);
}
