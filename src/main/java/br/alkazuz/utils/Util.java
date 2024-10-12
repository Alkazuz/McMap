package br.alkazuz.utils;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

public class Util {
    public static List<ClassNode> classes = new ArrayList<>();

    public static int getOpcodeCount(MethodNode methodNode, int opcode) {
        int count = 0;

        for (Object object : methodNode.instructions.toArray()) {
            AbstractInsnNode insn = (AbstractInsnNode) object;
            if (insn.getOpcode() == opcode) {
                count++;
            }
        }

        return count;
    }

    public static int getFloatDefinitionCount(MethodNode methodNode) {
        int count = 0;

        for (Object object : methodNode.instructions.toArray()) {
            AbstractInsnNode insn = (AbstractInsnNode) object;
            if (insn.getOpcode() == Opcodes.LDC) {
                if (((LdcInsnNode)insn).cst instanceof Float) {
                    count++;
                }
            }
        }

        return count;
    }

    public static int getBooleansDefinitionCount(MethodNode methodNode) {
        int count = 0;

        for (Object object : methodNode.instructions.toArray()) {
            AbstractInsnNode insn = (AbstractInsnNode) object;
            if (insn.getOpcode() == Opcodes.ICONST_1 || insn.getOpcode() == Opcodes.ICONST_0) {
                count++;
            }
        }

        return count;
    }

    public static int getFloatsDefinitionCount(MethodNode methodNode) {
        int count = 0;

        for (Object object : methodNode.instructions.toArray()) {
            AbstractInsnNode insn = (AbstractInsnNode) object;
            if (insn.getOpcode() == Opcodes.LDC) {
                if (((LdcInsnNode)insn).cst instanceof Float) {
                    count++;
                }
            }
        }

        return count;
    }

    public static int getDoublesDefinitionCount(MethodNode methodNode) {
        int count = 0;

        for (Object object : methodNode.instructions.toArray()) {
            AbstractInsnNode insn = (AbstractInsnNode) object;
            if (insn.getOpcode() == Opcodes.LDC) {
                if (((LdcInsnNode)insn).cst instanceof Double) {
                    count++;
                }
            }
        }

        return count;
    }

    public static int countMethodInsnNodes(MethodNode methodNode, String owner, String name) {
        int count = 0;

        for (Object object : methodNode.instructions.toArray()) {
            AbstractInsnNode insn = (AbstractInsnNode) object;
            if (insn.getOpcode() == Opcodes.INVOKESTATIC) {
                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                if (methodInsn.owner.equals(owner) && methodInsn.name.equals(name)) {
                    count++;
                }
            }
        }

        return count;
    }

    public static String cleanAsmLcst(String desc) {
        if (desc.startsWith("L") && desc.endsWith(";")) {
            return desc.substring(1, desc.length() - 1);
        }
        return desc;
    }

    public static String convertDesc(String desc) {
        if (desc.startsWith("L")) {
            return desc.replace("/", ".").replace("L", "").replace(";", "");
        }
        return desc;
    }

    public static String asmFieldDescToClassName(String desc) {
        if (desc.contains("/")) {
            return desc.substring(1, desc.length() - 1);
        }
        return desc.replace(";", "").replace("/", ".").replace("()L", "");
    }

    public static String asmDescToClassName(String desc) {
        if (desc.contains("/")) {
            return desc.substring(3, desc.length() - 1);
        }
        return desc.replace(";", "").replace("/", ".").replace("()L", "");
    }

    public static ClassNode getClassNode(String name) {
        for (ClassNode classNode : classes) {

            if (!classNode.name.equals(name)) continue;
            return classNode;
        }
        return null;
    }
}
