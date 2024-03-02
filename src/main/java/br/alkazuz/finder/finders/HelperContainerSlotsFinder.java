package br.alkazuz.finder.finders;

import br.alkazuz.finder.Finder;
import br.alkazuz.finder.ObfuscatedField;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HelperContainerSlotsFinder extends Finder {

    @Override
    public void process(ClassNode classNode) {
        if (!classNode.name.equals("net/eq2online/macros/gui/helpers/HelperContainerSlots")) return;
        for (Object o : classNode.methods) {
            MethodNode methodNode = (MethodNode) o;
            if (methodNode.name.equals("currentScreenIsInventory")) {
                List<AbstractInsnNode> instructions = Arrays.stream(methodNode.instructions.toArray()).collect(Collectors.toList());
                for (AbstractInsnNode insn : instructions) {
                    if (insn instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) insn;
                        if (fieldInsnNode.getOpcode() == Opcodes.GETFIELD) {
                            addField(ObfuscatedField.parse("net/minecraft/client/Minecraft", "currentScreen", fieldInsnNode));
                            return;
                        }
                    }
                }
            }
        }
    }
}
