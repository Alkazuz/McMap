package br.alkazuz.finder;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;
import br.alkazuz.utils.Util;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Finder {
    private static List<ObfuscatedField> obfuscatedFields = new ArrayList<>();
    private static List<ObfuscatedClass> obfuscatedClasses = new ArrayList<>();
    private static List<ObfuscatedMethod> obfuscatedMethods = new ArrayList<>();

    public void process(ClassNode classNode) {}

    protected void printClass(String classSring, ObfuscatedClass obfuscatedClass) {

    }

    public static void addField(ObfuscatedField obfuscatedField) {
        obfuscatedFields.add(obfuscatedField);
    }

    public static void addMethod(String className, String originalName, MethodNode methodNode) {
        obfuscatedMethods.add(ObfuscatedMethod.parse(className, originalName, methodNode));
    }

    public static void addClass(String originalName, String obfuscatedName) {
        obfuscatedClasses.add(ObfuscatedClass.parse(originalName, Util.asmDescToClassName(obfuscatedName)));
    }

    protected MethodNode findMethodNode(ClassNode classNode, String methodName) {
        for (Object object : classNode.methods) {
            if (!(object instanceof MethodNode)) continue;
            MethodNode methodNode = (MethodNode) object;
            if (methodNode.name.equals(methodName)) {
                return methodNode;
            }
        }
        return null;
    }

    protected ClassNode getObfuscatedClass(String className) {
        ObfuscatedClass obfuscatedClass = obfuscatedClasses.stream()
                .filter(obfuscatedClass1 -> obfuscatedClass1.getOriginalName().equals(className))
                .findFirst().orElse(null);
        if (obfuscatedClass == null) return null;

        for (ClassNode classNode : Util.classes) {
            if (!classNode.name.equals(obfuscatedClass.getObfuscatedName())) continue;
            return classNode;
        }
        return null;
    }

    protected void printFieldInsnNodeIndexes(MethodNode methodNode) {
        int index = 0;
        for (AbstractInsnNode object : methodNode.instructions.toArray()) {
            if (!(object instanceof FieldInsnNode)) continue;
            FieldInsnNode fieldInsnNode = (FieldInsnNode) object;
            System.out.println("Index: " + index + " Name: " + fieldInsnNode.name + " Desc: " + fieldInsnNode.desc);
            index++;
        }
    }

    protected FieldInsnNode findFieldInsnNodeIndex(MethodNode methodNode, int index) {
        int i = 0;
        for (AbstractInsnNode object : methodNode.instructions.toArray()) {
            if (!(object instanceof FieldInsnNode)) continue;
            FieldInsnNode fieldInsnNode = (FieldInsnNode) object;
            if (i == index) {
                return fieldInsnNode;
            }
            i++;
        }
        return null;
    }

    public static void output() {
        JsonObjectBuilder classesObjectBuilder = Json.createObjectBuilder();
        for (ObfuscatedClass obfuscatedClass : obfuscatedClasses) {
            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
            jsonObjectBuilder.add("obfuscated", obfuscatedClass.getObfuscatedName());

            JsonArrayBuilder fieldsArrayBuilder = Json.createArrayBuilder();
            for (ObfuscatedField obfuscatedField : obfuscatedFields) {
                if (!obfuscatedField.getClassName().equals(obfuscatedClass.getOriginalName())) {
                    continue;
                }
                JsonObjectBuilder fieldObjectBuilder = Json.createObjectBuilder();
                fieldObjectBuilder.add("name", obfuscatedField.getOriginalName());
                fieldObjectBuilder.add("obfuscated", obfuscatedField.getFieldNode().name);
                fieldObjectBuilder.add("signature", obfuscatedField.getFieldNode().desc);
                fieldObjectBuilder.add("static", obfuscatedField.getFieldNode().getOpcode() == 8);
                fieldsArrayBuilder.add(fieldObjectBuilder);
            }

            jsonObjectBuilder.add("fields", fieldsArrayBuilder);

            JsonArrayBuilder methodsArrayBuilder = Json.createArrayBuilder();
            for (ObfuscatedMethod obfuscatedMethod : obfuscatedMethods) {
                if (!obfuscatedMethod.getClassName().equals(obfuscatedClass.getOriginalName())) {
                    continue;
                }
                JsonObjectBuilder methodObjectBuilder = Json.createObjectBuilder();
                methodObjectBuilder.add("name", obfuscatedMethod.getOriginalName());
                methodObjectBuilder.add("obfuscated", obfuscatedMethod.getMethodNode().name);
                methodObjectBuilder.add("signature", obfuscatedMethod.getMethodNode().desc);
                methodObjectBuilder.add("static", obfuscatedMethod.getMethodNode().access == 8);
                methodsArrayBuilder.add(methodObjectBuilder);
            }

            jsonObjectBuilder.add("methods", methodsArrayBuilder);

            classesObjectBuilder.add(obfuscatedClass.getOriginalName(), jsonObjectBuilder);
        }
        JsonObject classesObject = classesObjectBuilder.build();

        StringWriter stringWriter = new StringWriter();
        JsonWriterFactory writerFactory = Json.createWriterFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));
        JsonWriter jsonWriter = writerFactory.createWriter(stringWriter);

        jsonWriter.writeObject(classesObject);
        jsonWriter.close();

        String formattedJsonString = stringWriter.toString();

        System.out.println(formattedJsonString);
    }

}
