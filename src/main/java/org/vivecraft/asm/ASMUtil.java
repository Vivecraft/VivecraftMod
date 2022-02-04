package org.vivecraft.asm;

import java.util.Iterator;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class ASMUtil {
	private ASMUtil() {
	}
	
	public static MethodNode findMethod(ClassNode node, MethodTuple tuple) {
		for (Iterator<MethodNode> it = node.methods.iterator(); it.hasNext(); ) {
			MethodNode method = it.next();
			if (method.name.equals(tuple.methodName) && method.desc.equals(tuple.methodDesc)) {
				return method;
			}
		}
		return null;
	}
	
	public static boolean deleteMethod(ClassNode node, MethodTuple tuple) {
		for (Iterator<MethodNode> it = node.methods.iterator(); it.hasNext(); ) {
			MethodNode method = it.next();
			if (method.name.equals(tuple.methodName) && method.desc.equals(tuple.methodDesc)) {
				it.remove();
				return true;
			}
		}
		return false;
	}
	
	public static void deleteInstructions(MethodNode node, int index, int count) {
		for (int i = 0; i < count; i++) {
			node.instructions.remove(node.instructions.get(index));
		}
	}

	public static void insertInstructionsRelative(MethodNode node, AbstractInsnNode insn, int offset, InsnList list) {
		node.instructions.insert(node.instructions.get(node.instructions.indexOf(insn) + offset), list);
	}
	
	public static AbstractInsnNode findFirstOpcode(MethodNode node, int opcode) {
		return findNthOpcode(node, 0, opcode);
	}
	
	public static AbstractInsnNode findLastOpcode(MethodNode node, int opcode) {
		AbstractInsnNode ret = null;
		for (Iterator<AbstractInsnNode> it = node.instructions.iterator(); it.hasNext(); ) {
			AbstractInsnNode instruction = it.next();
			if (instruction.getOpcode() == opcode) {
				ret = instruction;
			}
		}
		return ret;
	}
	
	public static AbstractInsnNode findNthOpcode(MethodNode node, int n, int opcode) {
		int i = 0;
		for (Iterator<AbstractInsnNode> it = node.instructions.iterator(); it.hasNext(); ) {
			AbstractInsnNode instruction = it.next();
			if (instruction.getOpcode() == opcode) {
				if (i++ == n) return instruction;
			}
		}
		return null;
	}
	
	public static AbstractInsnNode findFirstInstruction(MethodNode node, int opcode, Object... args) {
		return findNthInstruction(node, 0, opcode, args);
	}
	
	public static AbstractInsnNode findLastInstruction(MethodNode node, int opcode, Object... args) {
		AbstractInsnNode ret = null;
		for (Iterator<AbstractInsnNode> it = node.instructions.iterator(); it.hasNext(); ) {
			AbstractInsnNode instruction = it.next();
			if (matchInstruction(instruction, opcode, args)) {
				ret = instruction;
			}
		}
		return ret;
	}
	
	public static AbstractInsnNode findNthInstruction(MethodNode node, int n, int opcode, Object... args) {
		int i = 0;
		for (Iterator<AbstractInsnNode> it = node.instructions.iterator(); it.hasNext(); ) {
			AbstractInsnNode instruction = it.next();
			if (matchInstruction(instruction, opcode, args)) {
				if (i++ == n) return instruction;
			}
		}
		return null;
	}
	
	public static AbstractInsnNode findFirstInstructionPattern(MethodNode node, Object[]... instructions) {
		return findNthInstructionPattern(node, 0, instructions);
	}
	
	public static boolean addMethod(ClassNode node, MethodTuple tuple, MethodNode method) {
	
		for (Iterator<MethodNode> it = node.methods.iterator(); it.hasNext(); ) {
			if (method.name.equals(tuple.methodName) && method.desc.equals(tuple.methodDesc)) {
				//exists.
				return false;
			}
		}
	
		//node.methods.add(node); TODO no clue
		
		return true;
	}
	
	public static AbstractInsnNode findLastInstructionPattern(MethodNode node, int n, Object[]... instructions) {
		AbstractInsnNode ret = null;
		int j = 0;
		for (Iterator<AbstractInsnNode> it = node.instructions.iterator(); it.hasNext(); ) {
			AbstractInsnNode instruction = it.next();
			Object[] args = new Object[instructions[j].length - 1];
			System.arraycopy(instructions[j], 1, args, 0, args.length);
			if (matchInstruction(instruction, (Integer)instructions[j][0], args)) {
				if (++j == instructions.length) {
					ret = instruction;
					j = 0;
				}
			} else {
				j = 0;
			}
		}
		return ret;
	}
	
	public static AbstractInsnNode findNthInstructionPattern(MethodNode node, int n, Object[]... instructions) {
		int i = 0, j = 0;
		for (Iterator<AbstractInsnNode> it = node.instructions.iterator(); it.hasNext(); ) {
			AbstractInsnNode instruction = it.next();
			Object[] args = new Object[instructions[j].length - 1];
			System.arraycopy(instructions[j], 1, args, 0, args.length);
			if (matchInstruction(instruction, (Integer)instructions[j][0], args)) {
				if (++j == instructions.length) {
					if (i++ == n) return instruction;
					j = 0;
				}
			} else {
				j = 0;
			}
		}
		return null;
	}

	public static boolean matchInstruction(AbstractInsnNode ain, AbstractInsnNode ain2) {
		if (ain.getOpcode() != ain2.getOpcode()) return false;
		if (ain instanceof InsnNode) {
			return true;
		} else if (ain instanceof VarInsnNode) {
			return ((VarInsnNode)ain).var == ((VarInsnNode)ain2).var;
		} else if (ain instanceof LdcInsnNode) {
			return ((LdcInsnNode)ain).cst.equals(((LdcInsnNode)ain2).cst);
		} else if (ain instanceof IntInsnNode) {
			return ((IntInsnNode)ain).operand == ((IntInsnNode)ain2).operand;
		} else if (ain instanceof TypeInsnNode) {
			return ((TypeInsnNode)ain).desc.equals(((TypeInsnNode)ain2).desc);
		} else if (ain instanceof FieldInsnNode) {
			FieldInsnNode fin = (FieldInsnNode)ain;
			FieldInsnNode fin2 = (FieldInsnNode)ain2;
			return fin.owner.equals(fin2.owner) && fin.name.equals(fin2.name) && fin.desc.equals(fin2.desc);
		} else if (ain instanceof MethodInsnNode) {
			MethodInsnNode min = (MethodInsnNode)ain;
			MethodInsnNode min2 = (MethodInsnNode)ain2;
			return min.owner.equals(min2.owner) && min.name.equals(min2.name) && min.desc.equals(min2.desc) && min.itf == min2.itf;
		} else if (ain instanceof JumpInsnNode) {
			return ((JumpInsnNode)ain).label == ((JumpInsnNode)ain2).label;
		} else if (ain instanceof IincInsnNode) {
			IincInsnNode iin = (IincInsnNode)ain;
			IincInsnNode iin2 = (IincInsnNode)ain2;
			return iin.var == iin2.var && iin.incr == iin2.incr;
		}
		return false;
	}

	public static boolean matchInstruction(AbstractInsnNode ain, int opcode, Object... args) {
		if (ain.getOpcode() != opcode) return false;
		if (ain instanceof InsnNode) {
			return true;
		} else if (ain instanceof VarInsnNode) {
			return args[0] instanceof Integer && ((VarInsnNode)ain).var == (Integer)args[0];
		} else if (ain instanceof LdcInsnNode) {
			return args[0].equals(((LdcInsnNode)ain).cst);
		} else if (ain instanceof IntInsnNode) {
			return args[0] instanceof Integer && ((IntInsnNode)ain).operand == (Integer)args[0];
		} else if (ain instanceof TypeInsnNode) {
			return args[0] instanceof String && ((TypeInsnNode)ain).desc.equals(args[0]);
		} else if (ain instanceof FieldInsnNode) {
			if (args.length != 3 || !(args[0] instanceof String) || !(args[1] instanceof String) || !(args[2] instanceof String))
				return false;
			FieldInsnNode fin = (FieldInsnNode)ain;
			return fin.owner.equals(args[0]) && fin.name.equals(args[1]) && fin.desc.equals(args[2]);
		} else if (ain instanceof MethodInsnNode) {
			if (args.length != 4 || !(args[0] instanceof String) || !(args[1] instanceof String) || !(args[2] instanceof String) || !(args[3] instanceof Boolean))
				return false;
			MethodInsnNode min = (MethodInsnNode)ain;
			return min.owner.equals(args[0]) && min.name.equals(args[1]) && min.desc.equals(args[2]) && min.itf == (Boolean)args[3];
		} else if (ain instanceof JumpInsnNode) {
			return args[0] instanceof LabelNode && ((JumpInsnNode)ain).label == args[0];
		} else if (ain instanceof IincInsnNode) {
			if (args.length != 2 || !(args[0] instanceof Integer) || !(args[1] instanceof Integer))
				return false;
			IincInsnNode iin = (IincInsnNode)ain;
			return iin.var == (Integer)args[0] && iin.incr == (Integer)args[1];
		}
		return false;
	}
}
