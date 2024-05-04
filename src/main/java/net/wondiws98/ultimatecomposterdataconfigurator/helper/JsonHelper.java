package net.wondiws98.ultimatecomposterdataconfigurator.helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.data.server.tag.ItemTagProvider;
import net.minecraft.data.server.tag.TagProvider;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagEntry;
import net.minecraft.registry.tag.TagFile;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class JsonHelper {
    public static boolean asBoolean(JsonElement element, String name) {
        if (element.getAsJsonPrimitive().isBoolean()) {
            return element.getAsBoolean();
        } else {
            throw createWrongTypeException(name, "Boolean", getType(element));
        }
    }

    public static Boolean getBoolean(JsonObject object, String name) {
        return getBoolean(object, name, true);
    }

    public static Boolean getBoolean(JsonObject object, String name, boolean isOptional) {
        if (object.has(name)) {
            return asBoolean(object.get(name), name);
        } else if (!isOptional) {
            throw createMissingRequiredFieldException(name);
        }
        return null;
    }

    public static int asInt(JsonElement element, String name) {
        if (element.getAsJsonPrimitive().isNumber()) {
            return element.getAsInt();
        } else {
            throw createWrongTypeException(name, "Integer", getType(element));
        }
    }

    public static Integer getInt(JsonObject object, String name) {
        return getInt(object, name, true);
    }

    public static Integer getInt(JsonObject object, String name, boolean isOptional) {
        if (object.has(name)) {
            return asInt(object.get(name), name);
        } else if (!isOptional) {
            throw createMissingRequiredFieldException(name);
        }
        return null;
    }

    public static float asFloat(JsonElement element, String name) {
        if (element.getAsJsonPrimitive().isNumber()) {
            return element.getAsFloat();
        } else {
            throw createWrongTypeException(name, "Float", getType(element));
        }
    }

    public static Float getFloat(JsonObject object, String name) {
        return getFloat(object, name, true);
    }

    public static Float getFloat(JsonObject object, String name, boolean isOptional) {
        if (object.has(name)) {
            return asFloat(object.get(name), name);
        } else if (!isOptional) {
            throw createMissingRequiredFieldException(name);
        }
        return null;
    }

    public static String asString(JsonElement element, String name) {
        if (element.getAsJsonPrimitive().isString()) {
            return element.getAsString();
        } else {
            throw createWrongTypeException(name, "String", getType(element));
        }
    }

    public static String getString(JsonObject object, String name) {
        return getString(object, name, true);
    }

    public static String getString(JsonObject object, String name, boolean isOptional) {
        if (object.has(name)) {
            return asString(object.get(name), name);
        } else if (!isOptional) {
            throw createMissingRequiredFieldException(name);
        }
        return null;
    }

    public static List<JsonElement> asList(JsonElement element, String name) {
        if (element.isJsonArray()) {
            return element.getAsJsonArray().asList();
        } else {
            throw createWrongTypeException(name, "Array", getType(element));
        }
    }

    public static List<JsonElement> getList(JsonObject object, String name) {
        return getList(object, name, true);
    }

    public static List<JsonElement> getList(JsonObject object, String name, boolean isOptional) {
        if (object.has(name)) {
            return asList(object.get(name), name);
        } else if (!isOptional) {
            throw createMissingRequiredFieldException(name);
        }
        return null;
    }

    public static Identifier asIdentifier(JsonElement element, String name) {
        String stringId = asString(element, name);
        Identifier id = Identifier.tryParse(stringId);
        if (id != null) {
            return id;
        } else {
            throw new JsonSyntaxException("Failed to parse String value '" + stringId + "' of field '" + name + "' to identifier");
        }
    }

    public static Identifier getRegistryEntryIdentifier(JsonObject object, String name, Registry<?> registry) {
        return getRegistryEntryIdentifier(object, name, registry, true);
    }

    public static Identifier getRegistryEntryIdentifier(JsonObject object, String name, Registry<?> registry, boolean isOptional) {
        if (object.has(name)) {
            Identifier id = asIdentifier(object.get(name), name);
            if (registry.containsId(id)) {
                return id;
            } else {
                throw createNonExistentIdException(id, registry.toString(), name);
            }
        } else if (!isOptional) {
            throw createMissingRequiredFieldException(name);
        }
        return null;
    }

    public static Identifier getRegistryEntryIdentifier(JsonElement element, String name, Registry<?> registry) {
        Identifier id = asIdentifier(element, name);
        if (registry.containsId(id)) {
            return id;
        } else {
            throw createNonExistentIdException(id, registry.toString(), name);
        }
    }

    private static JsonSyntaxException createNonExistentIdException(Identifier id, String registryType, String fieldName) {
        return new JsonSyntaxException("Non existent id '" + id + "' in " + registryType + " for field " + fieldName);
    }

    private static JsonSyntaxException createMissingRequiredFieldException(String fieldName) {
        return new JsonSyntaxException("Missing required field '" + fieldName + "'");
    }

    private static JsonSyntaxException createWrongTypeException(String fieldName, String expectedType, String foundType) {
        return new JsonSyntaxException("Field '" + fieldName + "' should be of type '" + expectedType + "' but instead was "+ foundType);
    }

    private static String getType(JsonElement element) {
        return net.minecraft.util.JsonHelper.getType(element);
    }


}
