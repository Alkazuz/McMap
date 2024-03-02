package br.alkazuz.finder;

import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;

public class ObfuscatedField {
    private final String className;
    private final String originalName;
    private final FieldInsnNode fieldNode;

    public ObfuscatedField(String className, String originalName, FieldInsnNode fieldNode) {
        this.className = className;
        this.originalName = originalName;
        this.fieldNode = fieldNode;
    }

    public String getClassName() {
        return className;
    }

    public String getOriginalName() {
        return originalName;
    }

    public FieldInsnNode getFieldNode() {
        return fieldNode;
    }

    public static ObfuscatedField parse(String classOwner, String originalName, FieldInsnNode fieldNode) {
        return new ObfuscatedField(classOwner, originalName, fieldNode);
    }

}
