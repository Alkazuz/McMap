package br.alkazuz.finder.finders;

import br.alkazuz.finder.Finder;
import br.alkazuz.finder.ObfuscatedField;
import br.alkazuz.utils.Util;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MinecraftFinder extends Finder {

    @Override
    public void process(ClassNode classNode) {
        if (!classNode.name.equals("net/minecraft/client/Minecraft")) return;
        findSendQueue(classNode);
        findClickMouse(classNode);
    }

    private void findClickMouse(ClassNode classNode) {
        for (Object mObject : classNode.methods.toArray()) {
            MethodNode methodNode = (MethodNode) mObject;
            if (!methodNode.desc.equals("(I)V")) continue;
            int countGetField = Util.getOpcodeCount(methodNode, Opcodes.GETFIELD);
            int countInvokeVirtual = Util.getOpcodeCount(methodNode, Opcodes.INVOKEVIRTUAL);
            if (countGetField > 50 && countInvokeVirtual > 5) {
                addMethod("net/minecraft/client/Minecraft", "clickMouse", methodNode);
                findSwingItem(methodNode);
                findObjectMouseOver(methodNode);
            }
        }
    }

    private void findObjectMouseOver(MethodNode clickMouseMethod) {
        for (int i = 0; i < clickMouseMethod.instructions.size(); i++) {
            AbstractInsnNode instruction = clickMouseMethod.instructions.get(i);
            if (instruction.getOpcode() == Opcodes.GETFIELD) {
                AbstractInsnNode nextInstruction = clickMouseMethod.instructions.get(i + 1);
                if (nextInstruction.getOpcode() == Opcodes.IFNONNULL) {
                    addField(ObfuscatedField.parse("net/minecraft/client/Minecraft", "objectMouseOver", (FieldInsnNode) instruction));
                    String cleanClassName = Util.asmDescToClassName(((FieldInsnNode) instruction).desc);
                    cleanClassName = cleanClassName.substring(1, cleanClassName.length());
                    addClass("net/minecraft/src/MovingObjectPosition", cleanClassName);
                    return;
                }
            }
        }
    }

    private void findSwingItem(MethodNode methodNode) {
        for (int i = 0; i < methodNode.instructions.size(); i++) {
            AbstractInsnNode instruction = methodNode.instructions.get(i);
            if (instruction.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
                if (methodInsnNode.desc.equals("()V")) {
                    MethodNode swingItem = (MethodNode) Util.getClassNode(methodInsnNode.owner).methods.stream()
                            .filter(m -> {
                                MethodNode mn = (MethodNode) m;
                                return mn.desc.equals("()V") && mn.name.equals(methodInsnNode.name);
                            }).findFirst().orElse(null);
                    addMethod("net/minecraft/client/entity/EntityLiving", "swingItem", swingItem);
                    return;
                }
            }
        }
    }

    private void findSendQueue(ClassNode classNode) {
        for (Object mObject : classNode.methods.toArray()) {
            MethodNode methodNode = (MethodNode) mObject;
            if (!methodNode.desc.equals("(I)V")) continue;
            int putfieldCount = Arrays.asList(methodNode.instructions.toArray())
                    .stream()
                    .filter(insnNode -> insnNode instanceof FieldInsnNode)
                    .map(insnNode -> (FieldInsnNode) insnNode)
                    .filter(fieldInsnNode -> fieldInsnNode.getOpcode() == Opcodes.PUTFIELD)
                    .collect(Collectors.toList())
                    .size();
            int fieldInsnNodeCount = Arrays.asList(methodNode.instructions.toArray())
                    .stream()
                    .filter(insnNode -> insnNode instanceof FieldInsnNode)
                    .map(insnNode -> (FieldInsnNode) insnNode)
                    .collect(Collectors.toList())
                    .size();

            if (putfieldCount == 2 && fieldInsnNodeCount > 55) {
                int count = 0;
                for(int i = methodNode.instructions.size() - 1; i >= 0; i--) {
                    AbstractInsnNode insnNode = methodNode.instructions.get(i);
                    if (insnNode instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNode;
                        if (count == 4) {
                            addField(ObfuscatedField.parse("net/minecraft/client/Minecraft", "sendQueue", fieldInsnNode));
                            return;
                        }
                        count++;
                    }
                }
            }
        }
    }
}
