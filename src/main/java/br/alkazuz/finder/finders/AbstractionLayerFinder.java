package br.alkazuz.finder.finders;

import br.alkazuz.finder.Finder;
import br.alkazuz.finder.ObfuscatedField;
import br.alkazuz.utils.Util;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AbstractionLayerFinder extends Finder {

    @Override
    public void process(ClassNode classNode) {
        if (!classNode.name.equals("net/eq2online/macros/compatibility/AbstractionLayer")) return;
        processPlayer(classNode);
        processPlayerController(classNode);
        processWorld(classNode);
        // processCurrentScreen(classNode);
    }

    private void processCurrentScreen(ClassNode classNode) {
        MethodNode getCurrentScreen = findMethodNode(classNode, "getIngameGui");
        FieldInsnNode currentScreenField = findFieldInsnNodeIndex(getCurrentScreen, 0);
        addField(ObfuscatedField.parse("net/minecraft/client/Minecraft", "currentScreen", currentScreenField));
    }

    private void processWorld(ClassNode classNode) {
        MethodNode getWorld = findMethodNode(classNode, "getWorld");
        addClass("net/minecraft/world/WorldClient", getWorld.desc);
        FieldInsnNode theWorldField = findFieldInsnNodeIndex(getWorld, 0);
        addField(ObfuscatedField.parse("net/minecraft/client/Minecraft", "theWorld", theWorldField));
    }
    private void processPlayer(ClassNode classNode) {
        MethodNode getPlayer = findMethodNode(classNode, "getPlayer");
        addClass("net/minecraft/client/entity/EntityPlayerMP", getPlayer.desc);
        FieldInsnNode thePlayerField = findFieldInsnNodeIndex(getPlayer, 0);
        addField(ObfuscatedField.parse("net/minecraft/client/Minecraft", "thePlayer", thePlayerField));
        String entityPlayerSPstr = Util.getClassNode(Util.asmDescToClassName(getPlayer.desc)).superName;
        ClassNode EntityPlayerSP = Util.getClassNode(entityPlayerSPstr);
        addClass("net/minecraft/client/entity/EntityPlayerSP", Util.asmDescToClassName(getPlayer.desc));

        String entityPlayerStr = EntityPlayerSP.superName;
        ClassNode entityPlayer = Util.getClassNode(entityPlayerStr);
        addClass("net/minecraft/client/entity/EntityPlayer", entityPlayerStr);

        String entityLivingStr = entityPlayer.superName;
        ClassNode livingBase = Util.getClassNode(entityLivingStr);
        addClass("net/minecraft/client/entity/EntityLiving", entityLivingStr);

        processLivingEntity(livingBase, livingBase.superName);
        String superEntity = livingBase.superName;
        addClass("net/minecraft/entity/Entity", superEntity);
        new EntityFinder().process(Util.getClassNode(superEntity));
        processMotionUpdate();
    }

    private void processLivingEntity(ClassNode classNode, String entityClass) {
        for (Object object : classNode.methods) {
            if (!(object instanceof MethodNode)) continue;
            MethodNode methodNode = (MethodNode) object;
            if (methodNode.desc.equals("(L" + entityClass + ";)Z") && Util.getOpcodeCount(methodNode, Opcodes.GETFIELD) > 0) {
                boolean hasComparationWithNull = false;
                for (AbstractInsnNode abstractInsnNode : methodNode.instructions.toArray()) {
                    if (abstractInsnNode.getOpcode() == Opcodes.IRETURN) {
                        hasComparationWithNull = true;
                        break;
                    }
                }
                if (hasComparationWithNull) {
                    addMethod("net/minecraft/client/entity/EntityLiving", "canEntityBeSeen", methodNode);
                }
            }
        }
    }

    private void processMotionUpdate() {
        ClassNode entityPlayerMP = getObfuscatedClass("net/minecraft/client/entity/EntityPlayerMP");
        for (Object object : entityPlayerMP.methods) {
            MethodNode methodNode = (MethodNode) object;
            if (Util.getDoublesDefinitionCount(methodNode) >= 5 && Util.getBooleansDefinitionCount(methodNode) >= 4) {
                addMethod("net/minecraft/client/entity/EntityPlayerMP", "sendMotionUpdates", methodNode);
                processAddToSendQueue(methodNode);
            }
        }
    }

    private void processAddToSendQueue(MethodNode motionUpdateNode) {
        List<MethodInsnNode> methodInsnNodesInvokeVirtual = Arrays.stream( motionUpdateNode.instructions.toArray() )
                .filter(abstractInsnNode -> abstractInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL)
                .map(abstractInsnNode -> (MethodInsnNode) abstractInsnNode)
                .collect(Collectors.toList());
        MethodInsnNode methodInsnNode = methodInsnNodesInvokeVirtual.get(1);
        ClassNode netHandlerPlayClient = Util.getClassNode(methodInsnNode.owner);
        addClass("net/minecraft/network/NetHandlerPlayClient", netHandlerPlayClient.name);
        processVelocityHandler(netHandlerPlayClient);
        addClass("net/minecraft/network/NetworkManager", netHandlerPlayClient.superName);
        MethodNode addToSendQueue = findMethodNode(netHandlerPlayClient, methodInsnNode.name);
        addMethod("net/minecraft/network/NetHandlerPlayClient", "addToSendQueue", addToSendQueue);
    }

    private void processVelocityHandler(ClassNode classNode) {
        for (Object object : classNode.methods) {
            MethodNode methodNode = (MethodNode) object;
            if (methodNode.desc.contains(";)V") && methodNode.desc.contains("(L")){
                int getFieldsCount = Util.getOpcodeCount(methodNode, Opcodes.GETFIELD);
                int putFieldsCount = Util.getOpcodeCount(methodNode, Opcodes.PUTFIELD);
                int doublesCount = Util.getDoublesDefinitionCount(methodNode);
                int floatsCount = Util.getFloatsDefinitionCount(methodNode);
                int invokespecialCount = Util.getOpcodeCount(methodNode, Opcodes.INVOKESPECIAL);
                if (getFieldsCount >= 4 && putFieldsCount == 0 && doublesCount == 0 && floatsCount >= 0 && invokespecialCount >= 1) {
                    for (Object object1 : methodNode.instructions.toArray()) {
                        if (!(object1 instanceof MethodInsnNode)) continue;
                        MethodInsnNode methodInsnNode = (MethodInsnNode) object1;
                        if (methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                            if (!methodInsnNode.desc.equals("(DDD)V")) continue;
                            String packetEntityVelocity = methodNode.desc.substring(2, methodNode.desc.indexOf(";)"));
                            if (processClassVelocity(Util.getClassNode(packetEntityVelocity))) {
                                addMethod("net/minecraft/network/NetHandlerPlayClient", "handleEntityVelocity", methodNode);
                                addClass("net/minecraft/network/PacketEntityVelocity", packetEntityVelocity);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean processClassVelocity(ClassNode classNode) {
        for (Object object : classNode.methods) {
            MethodNode methodNode = (MethodNode) object;
            if (!methodNode.desc.equals("(Ljava/io/DataInputStream;)V")) continue;
            int count = 0;
            int countFieldPutfieldCount = Arrays.stream(methodNode.instructions.toArray())
                    .filter(abstractInsnNode -> abstractInsnNode instanceof FieldInsnNode)
                    .filter(abstractInsnNode -> abstractInsnNode.getOpcode() == Opcodes.PUTFIELD)
                    .map(abstractInsnNode -> (FieldInsnNode) abstractInsnNode)
                    .collect(Collectors.toList())
                    .size();
            if (countFieldPutfieldCount != 4) return false;
            for (Object object1 : methodNode.instructions.toArray()) {
                if (!(object1 instanceof FieldInsnNode)) continue;
                FieldInsnNode fieldInsnNode = (FieldInsnNode) object1;
                if (fieldInsnNode.getOpcode() == Opcodes.PUTFIELD) {
                    if (count == 0) {
                        addField(ObfuscatedField.parse("net/minecraft/network/PacketEntityVelocity", "velocityPacketEntityId", fieldInsnNode));
                    } else if (count == 1) {
                        addField(ObfuscatedField.parse("net/minecraft/network/PacketEntityVelocity", "velocityPacketMotionX", fieldInsnNode));
                    } else if (count == 2) {
                        addField(ObfuscatedField.parse("net/minecraft/network/PacketEntityVelocity", "velocityPacketMotionY", fieldInsnNode));
                    } else if (count == 3) {
                        addField(ObfuscatedField.parse("net/minecraft/network/PacketEntityVelocity", "velocityPacketMotionZ", fieldInsnNode));
                        return true;
                    }
                    count++;
                }
            }
        }
        return false;
    }

    private void processPlayerController(ClassNode classNode) {
        MethodNode getPlayerController = findMethodNode(classNode, "getPlayerController");
        FieldInsnNode fieldInsnNode = findFieldInsnNodeIndex(getPlayerController, 0);
        addField(ObfuscatedField.parse("net/minecraft/client/Minecraft", "playerController", fieldInsnNode));
        addClass("net/minecraft/client/PlayerControllerMP", Util.asmDescToClassName(fieldInsnNode.desc).substring(1, fieldInsnNode.desc.length() - 1));
        findAttackEntityMethos(Util.getClassNode(Util.asmDescToClassName(fieldInsnNode.desc).substring(1, fieldInsnNode.desc.length() - 1)));
    }

    private void findAttackEntityMethos(ClassNode classNode) {
        for(Object object : classNode.methods) {
            MethodNode methodNode = (MethodNode) object;
            if (methodNode.name.equals("<init>")) continue;
            if(!methodNode.desc.contains("(L")) continue;
            if(!methodNode.desc.contains(";L")) continue;
            if(!methodNode.desc.contains(";)V")) continue;
            addMethod("net/minecraft/client/PlayerControllerMP", "attackEntity", methodNode);
            int count = 0;
            for (Object object1 : methodNode.instructions.toArray()) {
                if (!(object1 instanceof MethodInsnNode)) continue;
                MethodInsnNode methodInsnNode = (MethodInsnNode) object1;
                if (methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                    MethodNode attackTargetEntityWithCurrentItem = findMethodNode(Util.getClassNode(methodInsnNode.owner), methodInsnNode.name);
                    if (count == 1)
                        addMethod("net/minecraft/client/entity/EntityPlayerSP", "attackTargetEntityWithCurrentItem", attackTargetEntityWithCurrentItem);
                    count++;
                }
            }
        }
    }
}
