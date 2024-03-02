package br.alkazuz.finder.finders;

import br.alkazuz.finder.Finder;
import br.alkazuz.finder.ObfuscatedField;
import br.alkazuz.utils.Util;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static br.alkazuz.utils.Util.getClassNode;

public class ItemStackFinder extends Finder {

    @Override
    public void process(ClassNode classNode) {
        if (classNode.name.equals("net/eq2online/macros/scripting/crafting/AutoCraftingManager")) {
            processHelperContainerSlots(classNode);
        }
            if (!classNode.name.equals("net/eq2online/macros/struct/ItemStackInfo")) return;
        processItemStackConstructor(classNode);
    }

    private void processHelperContainerSlots(ClassNode classNode) {
        for (Object o : classNode.methods) {
            MethodNode methodNode = (MethodNode) o;
            if (methodNode.name.equals("getCraftingItemId")) {
                List<AbstractInsnNode> instructions = Arrays.stream(methodNode.instructions.toArray()).collect(Collectors.toList());
                for (AbstractInsnNode insn : instructions) {
                    if (insn instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) insn;
                        if (fieldInsnNode.getOpcode() == Opcodes.GETFIELD) {
                            addField(ObfuscatedField.parse("net/minecraft/item/ItemStack", "itemId", fieldInsnNode));
                        }
                    }
                }
            }
        }
    }
    //

    private ClassNode processItemStackConstructor(ClassNode classNode) {
        for (Object method : classNode.methods) {
            MethodNode methodNode = (MethodNode) method;
            if (methodNode.name.equals("<init>")) {
                for (AbstractInsnNode node : methodNode.instructions.toArray()) {
                    if (node instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) node;
                        if (fieldInsnNode.getOpcode() == Opcodes.PUTFIELD) {
                            if (fieldInsnNode.name.equals("itemStack")) {
                                addClass("net/minecraft/item/ItemStack", Util.convertDesc(fieldInsnNode.desc));
                                return getClassNode(Util.convertDesc(fieldInsnNode.desc));
                            }
                        }
                    }
                }
            }

        }
        return null;
    }
}
