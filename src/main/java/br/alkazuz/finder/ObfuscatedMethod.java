package br.alkazuz.finder;

import org.objectweb.asm.tree.MethodNode;

public class ObfuscatedMethod {
    private final String className;
    private final String originalName;
    private final MethodNode methodNode;

    public ObfuscatedMethod(String className, String originalName, MethodNode methodNode) {
        this.className = className;
        this.originalName = originalName;
        this.methodNode = methodNode;
    }

    public String getOriginalName() {
        return originalName;
    }

    public MethodNode getMethodNode() {
        return methodNode;
    }

    public String getClassName() {
        return className;
    }

    public static ObfuscatedMethod parse(String classOwner, String originalName, MethodNode methodNode) {
        return new ObfuscatedMethod(classOwner, originalName, methodNode);
    }

}
