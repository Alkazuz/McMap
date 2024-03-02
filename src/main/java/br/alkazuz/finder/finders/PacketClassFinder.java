package br.alkazuz.finder.finders;

import br.alkazuz.finder.Finder;
import br.alkazuz.finder.ObfuscatedField;
import br.alkazuz.utils.Util;
import jdk.internal.org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

public class PacketClassFinder extends Finder {

    @Override
    public void process(ClassNode classNode) {
        if (!classNode.name.equals("net/eq2online/macros/compatibility/hooks/Packet22CollectHookable")) return;
        String superName = classNode.superName;
        ClassNode superClass22Packet = Util.getClassNode(superName);

        addClass("net/minecraft/packet/Packet", superClass22Packet.superName);
        processPacketEntityInteract(Util.getClassNode(superClass22Packet.superName));
    }

    private void processPacketEntityInteract(ClassNode classNode) {
        for (Object method : classNode.methods) {
            MethodNode methodNode = (MethodNode) method;
            if (methodNode.name.equals("<clinit>")) {
                for (int i = 0; i < methodNode.instructions.size(); i++) {
                    AbstractInsnNode insnNode = methodNode.instructions.get(i);

                    if (insnNode instanceof IntInsnNode && i > 2 && methodNode.instructions.get(i - 1) instanceof MethodInsnNode) {
                        IntInsnNode intInsnNode = (IntInsnNode) insnNode;
                        if (intInsnNode.operand == 7) {
                            LdcInsnNode ldcInsnNode = (LdcInsnNode) methodNode.instructions.get(i + 3);
                            addClass("net/minecraft/packet/PacketEntityInteract", Util.cleanAsmLcst(ldcInsnNode.cst.toString()));
                        }
                    }
                }
            }

        }
    }
}
