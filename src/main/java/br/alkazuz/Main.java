package br.alkazuz;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import br.alkazuz.finder.Finder;
import br.alkazuz.finder.finders.*;
import br.alkazuz.utils.Util;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

public class Main {
    public static void main(String[] args) {
        try {

            String jarFile = "C:\\Users\\marco\\IdeaProjects\\Deobfuscator\\test\\AntiHack-deobf.jar";
            HashMap<String, ClassNode> classes = new HashMap<String, ClassNode>();
            JarFile jar;
            (jar = new JarFile(new File(jarFile))).stream().forEach(z -> mapClass(jar, z, classes));
            jar.close();

            Util.classes.addAll(classes.values());

            Finder.addClass("net/minecraft/client/Minecraft", "net/minecraft/client/Minecraft");
            classes.values().forEach(cn -> new AbstractionLayerFinder().process(cn));
            classes.values().forEach(cn -> new ItemStackFinder().process(cn));

            for (ClassNode cn : classes.values()) {
                new PacketClassFinder().process(cn);
                new MinecraftFinder().process(cn);
                new LoadedPlayersFinder().process(cn);
                new HelperContainerSlotsFinder().process(cn);
            }
            Finder.output();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static Map<String, ClassNode> mapClass(JarFile jar, JarEntry entry, Map<String, ClassNode> classes) {
        String name = entry.getName();
        try {
            InputStream jis = jar.getInputStream(entry);
            try {
                if (name.endsWith(".class")) {
                    byte[] bytes = IOUtils.toByteArray(jis);
                    String cafebabe = String.format("%02X%02X%02X%02X", bytes[0], bytes[1], bytes[2], bytes[3]);
                    if (!cafebabe.toLowerCase().equals("cafebabe")) {
                        return classes;
                    }
                    try {
                        ClassNode cn = getNode(bytes);
                        classes.put(cn.name, cn);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                if (jis != null) {
                    jis.close();
                }
            }
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        return classes;
    }

    private static ClassNode getNode(byte[] bytes) {
        ClassReader cr = new ClassReader(bytes);
        ClassNode cn = new ClassNode();
        try {
            cr.accept((ClassVisitor) cn, 8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cr = null;
        return cn;
    }
}