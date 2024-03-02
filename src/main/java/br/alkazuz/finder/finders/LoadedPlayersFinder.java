package br.alkazuz.finder.finders;

import br.alkazuz.finder.Finder;
import br.alkazuz.finder.ObfuscatedField;
import br.alkazuz.utils.Util;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LoadedPlayersFinder extends Finder {

    @Override
    public void process(ClassNode entityClassNode) {
        if (!entityClassNode.interfaces.contains("java/util/concurrent/Callable")) return;
        for (Object o : entityClassNode.methods) {
            MethodNode methodNode = (MethodNode) o;
            if (methodNode.desc.equals("()Ljava/lang/String;") && methodNode.instructions.size() > 0) {
                List<AbstractInsnNode> instructions = Arrays.stream(methodNode.instructions.toArray()).collect(Collectors.toList());
                for (AbstractInsnNode insn : instructions) {
                    if (insn instanceof MethodInsnNode) {
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;
                        if (methodInsn.getOpcode() == Opcodes.INVOKEINTERFACE &&
                                methodInsn.name.equals("size") &&
                                methodInsn.desc.equals("()I") && methodInsn.owner.equals("java/util/List")) {
                            AbstractInsnNode prevInsn = instructions.get(instructions.indexOf(methodInsn) - 1);
                            if (prevInsn instanceof FieldInsnNode) {
                                FieldInsnNode fieldInsnNode = (FieldInsnNode) prevInsn;
                                addClass("net/minecraft/world/World", fieldInsnNode.owner);
                                addField(ObfuscatedField.parse("net/minecraft/world/World", "loadedPlayers", fieldInsnNode));
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
}
