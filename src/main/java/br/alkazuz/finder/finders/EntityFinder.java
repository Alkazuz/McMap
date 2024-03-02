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

public class EntityFinder extends Finder {

    @Override
    public void process(ClassNode entityClassNode) {
        findForDistanceToEntity(entityClassNode);
        for (Object object : entityClassNode.methods) {
            MethodNode method = (MethodNode) object;
            if (method.name.equals("<init>")) {
                int count = 0;
                for (int i = 0; i < method.instructions.size(); i++) {
                    AbstractInsnNode instruction = method.instructions.get(i);
                    if (instruction.getOpcode() == Opcodes.PUTFIELD) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) instruction;
                        if (count == 0) {
                           addField(ObfuscatedField.parse("net/minecraft/entity/Entity", "entityId", fieldInsnNode));
                        } else if (count == 4) {
                            addField(ObfuscatedField.parse("net/minecraft/entity/Entity", "onGround", fieldInsnNode));
                        } else if (count == 10) {
                            addField(ObfuscatedField.parse("net/minecraft/entity/Entity", "width", fieldInsnNode));
                        } else if (count == 11) {
                            addField(ObfuscatedField.parse("net/minecraft/entity/Entity", "height", fieldInsnNode));
                        } else if (count == 22) {
                            addField(ObfuscatedField.parse("net/minecraft/entity/Entity", "ticksExisted", fieldInsnNode));
                        } else if (count == 26) {
                            addField(ObfuscatedField.parse("net/minecraft/entity/Entity", "hurtResistantTime", fieldInsnNode));
                        }
                        count++;
                    } else if (instruction instanceof MethodInsnNode) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
                        if (methodInsnNode.desc.equals("(DDD)V")) {
                            MethodNode setPosition = (MethodNode) entityClassNode.methods.stream()
                                    .filter(m -> {
                                        MethodNode mn = (MethodNode) m;
                                        return mn.desc.equals("(DDD)V") && mn.name.equals(methodInsnNode.name);
                                    }).findFirst().orElse(null);
                            addMethod("net/minecraft/entity/Entity", "setPosition", setPosition);
                            processSetPosition(setPosition);
                            break;
                        }
                    }
                }
            }
            else if (hasThrowableTryCatch(method) && Util.countMethodInsnNodes(method, "java/lang/Math", "abs") >= 3) { // readFromNBT method
                addMethod("net/minecraft/entity/Entity", "readFromNBT", method);
                int countPut = 0;
                for (Object object1 : method.instructions.toArray()) {
                    AbstractInsnNode insn = (AbstractInsnNode) object1;
                    if (insn.getOpcode() == Opcodes.PUTFIELD) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) insn;
                        if (countPut == 0) { // motion x
                            addField(ObfuscatedField.parse("net/minecraft/entity/Entity", "motionX", fieldInsnNode));
                        } else if (countPut == 1) { // motion y
                            addField(ObfuscatedField.parse("net/minecraft/entity/Entity", "motionY", fieldInsnNode));
                        } else if (countPut == 2) { // motion z
                            addField(ObfuscatedField.parse("net/minecraft/entity/Entity", "motionZ", fieldInsnNode));
                        }
                        countPut++;
                    }
                }
            }
            else if (method.desc.equals("(FF)V")) {
                int getFieldCount = Util.getOpcodeCount(method, Opcodes.GETFIELD);
                int putFieldCount = Util.getOpcodeCount(method, Opcodes.PUTFIELD);
                if (getFieldCount == 0 && putFieldCount >= 2) {
                    addMethod("net/minecraft/entity/Entity", "setRotation", method);
                    int c = 0;
                    for (Object object1 : method.instructions.toArray()) {
                        AbstractInsnNode insn = (AbstractInsnNode) object1;
                        if (insn.getOpcode() == Opcodes.PUTFIELD) {
                            FieldInsnNode fieldInsnNode = (FieldInsnNode) insn;
                            if (fieldInsnNode.desc.equals("F")) {
                                if (c == 0) {
                                    addField(ObfuscatedField.parse("net/minecraft/entity/Entity", "rotationYaw", fieldInsnNode));
                                } else if (c == 1) {
                                    addField(ObfuscatedField.parse("net/minecraft/entity/Entity", "rotationPitch", fieldInsnNode));
                                }
                                c++;
                            }
                        }
                    }
                }
            }
        }
    }

    private void findForDistanceToEntity(ClassNode entityClassNode) {
        for (Object object : entityClassNode.methods) {
            MethodNode method = (MethodNode) object;
            if (method.desc.equals("(L"+entityClassNode.name+";)F")) {
                if (Util.getOpcodeCount(method, Opcodes.GETFIELD) >= 6 &&
                        Util.getOpcodeCount(method, Opcodes.INVOKESTATIC) >= 1) {
                    addMethod("net/minecraft/entity/Entity", "getDistanceToEntity", method);
                    break;
                }
            }
        }
    }

    public static boolean hasThrowableTryCatch(MethodNode method) {
        for (Object tcb : method.tryCatchBlocks) {
            TryCatchBlockNode tryCatchBlock = (TryCatchBlockNode) tcb;
            if (tryCatchBlock.type.equals("java/lang/Throwable")) {
                return true;
            }
        }
        return false;
    }

    private void processSetPosition(MethodNode methodNode) {
        List<AbstractInsnNode> instructions = Arrays.stream(methodNode.instructions.toArray())
                .filter(i -> i.getOpcode() == Opcodes.PUTFIELD)
                .collect(Collectors.toList());
        int count = 0;
        for (AbstractInsnNode instruction : instructions) {
            FieldInsnNode fieldInsnNode = (FieldInsnNode) instruction;
            if (fieldInsnNode.desc.equals("D") && count == 0) {
                addField(ObfuscatedField.parse("net/minecraft/entity/Entity", "posX", fieldInsnNode));
                count++;
            } else if (fieldInsnNode.desc.equals("D") && count == 1) {
                addField(ObfuscatedField.parse("net/minecraft/entity/Entity", "posY", fieldInsnNode));
                count++;
            } else if (fieldInsnNode.desc.equals("D") && count == 2) {
                addField(ObfuscatedField.parse("net/minecraft/entity/Entity", "posZ", fieldInsnNode));
                count++;
            }
        }
    }
}
