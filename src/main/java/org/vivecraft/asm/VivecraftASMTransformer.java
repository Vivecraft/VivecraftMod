package org.vivecraft.asm;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import cpw.mods.modlauncher.api.ITransformer.Target;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.objectweb.asm.tree.ClassNode;
import org.vivecraft.asm.handler.ASMHandlerContainerScreen;
import org.vivecraft.asm.handler.ASMHandlerCreativeScreen;
import org.vivecraft.asm.handler.ASMHandlerFluidState;
import org.vivecraft.asm.handler.ASMHandlerItemRayTrace;

public class VivecraftASMTransformer implements ITransformer<ClassNode>
{
    private final List<ASMClassHandler> asmHandlers = new ArrayList<>();

    public VivecraftASMTransformer()
    {
        this.asmHandlers.add(new ASMHandlerContainerScreen());
        this.asmHandlers.add(new ASMHandlerCreativeScreen());
        this.asmHandlers.add(new ASMHandlerFluidState());
        this.asmHandlers.add(new ASMHandlerItemRayTrace());
    }

    public ClassNode transform(ClassNode input, ITransformerVotingContext context)
    {
        for (ASMClassHandler asmclasshandler : this.asmHandlers)
        {
            if (asmclasshandler.shouldPatchClass())
            {
                String s = asmclasshandler.getDesiredClass();

                if (s.equals(input.name))
                {
                    System.out.println("Patching class: " + s);
                    asmclasshandler.patchClass(input);
                }
            }
        }

        return input;
    }

    @Nonnull
    public TransformerVoteResult castVote(ITransformerVotingContext iTransformerVotingContext)
    {
        return TransformerVoteResult.YES;
    }

    @Nonnull
    public Set<Target> targets()
    {
        HashSet<Target> hashset = new HashSet<>();

        for (ASMClassHandler asmclasshandler : this.asmHandlers)
        {
            if (asmclasshandler.shouldPatchClass())
            {
                Target target = Target.targetClass(asmclasshandler.getDesiredClass());
                hashset.add(target);
            }
        }

        return hashset;
    }
}
