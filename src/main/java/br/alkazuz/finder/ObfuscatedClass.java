package br.alkazuz.finder;

import org.objectweb.asm.tree.ClassNode;

public class ObfuscatedClass {
    private final String originalName;
    private final String obfuscatedName;

    public ObfuscatedClass(String originalName, String obfuscatedName) {
        this.originalName = originalName;
        this.obfuscatedName = obfuscatedName;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getObfuscatedName() {
        return obfuscatedName;
    }

    public static ObfuscatedClass parse(String originalName, String obfuscatedName) {
        return new ObfuscatedClass(originalName, obfuscatedName);
    }

}
