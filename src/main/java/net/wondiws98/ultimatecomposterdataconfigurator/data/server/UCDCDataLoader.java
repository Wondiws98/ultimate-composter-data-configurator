package net.wondiws98.ultimatecomposterdataconfigurator.data.server;

import com.google.gson.*;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.wondiws98.ultimatecomposterdataconfigurator.UltimateComposterDataConfigurator;
import net.wondiws98.ultimatecomposterdataconfigurator.helper.JsonHelper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public enum UCDCDataLoader implements SimpleSynchronousResourceReloadListener {
    INSTANCE;

    private Identifier defaultResult = Registries.ITEM.getId(Items.BONE_MEAL);
    private boolean globalAutoUse = true;
    private final HashMap<Identifier, CompostableEntry> entryHashMap = new HashMap<>();
    private final HashMap<Integer, ModifierEntry> modifierHashMap = new HashMap<>();

    private static final String DATA_FOLDER = "compostable";
    private static final String REPLACE_ENTRIES_STRING = "replaceEntries";
    private static final String ENTRIES_STRING = "entries";
    private static final String REPLACE_MODIFIERS_STRING = "replaceModifiers";
    private static final String MODIFIERS_STRING = "modifiers";
    private static final String GLOBAL_AUTO_USE_STRING = "globalAutoUse";

    @Override
    public Identifier getFabricId() {
        return new Identifier(UltimateComposterDataConfigurator.MOD_ID, DATA_FOLDER);
    }

    @Override
    public void reload(ResourceManager manager) {
        resetEntries();
        resetModifiers();
        setGlobalAutoUse(true);
        Map<Identifier, Resource> resources = manager.findResources(DATA_FOLDER, (identifier) -> identifier.getPath().endsWith(".json"));
        for (Identifier id : resources.keySet()) {
            try (InputStream stream = manager.getResource(id).get().getInputStream()) {
                JsonObject object = (JsonObject) JsonParser.parseReader(new InputStreamReader(stream));

                // RESET ENTRIES
                try {
                    Boolean replaceEntries = JsonHelper.getBoolean(object, REPLACE_ENTRIES_STRING);
                    if (replaceEntries!=null && replaceEntries) {
                        resetEntries();
                    }
                } catch (Exception e) {
                    logError(getOptionalFieldErrorMsg(id), e);;
                }

                // RESET MODIFIERS
                try {
                    Boolean replaceModifiers = JsonHelper.getBoolean(object, REPLACE_MODIFIERS_STRING);
                    if (replaceModifiers !=null && replaceModifiers) {
                        resetModifiers();
                    }
                } catch (Exception e) {
                    logError(getOptionalFieldErrorMsg(id), e);;
                }

                // SET GLOBAL AUTO USE
                try {
                    Boolean globalAutoUSe = JsonHelper.getBoolean(object, GLOBAL_AUTO_USE_STRING);
                    if (globalAutoUSe !=null) {
                        setGlobalAutoUse(globalAutoUSe);
                    }
                } catch (Exception e) {
                    logError(getOptionalFieldErrorMsg(id), e);
                }

                // LOAD ENTRIES
                try {
                    List<JsonElement> elements = JsonHelper.getList(object, ENTRIES_STRING);
                    if (elements != null) {
                        for (JsonElement element : elements) {
                            try {
                                addEntry(CompostableEntry.fromJsonObject(element.getAsJsonObject(), id));
                            } catch (Exception e) {
                                logError(getEntryFieldErrorMsg(id), e);
                            }
                        }
                    }
                } catch (Exception e) {
                    logError(getEntryFieldErrorMsg(id), e);
                }

                // LOAD MODIFIERS
                try {
                    List<JsonElement> elements = JsonHelper.getList(object, MODIFIERS_STRING);
                    if (elements != null) {
                        for (JsonElement element : elements) {
                            try {
                                addModifier(ModifierEntry.fromJsonObject(element.getAsJsonObject(), id));
                            } catch (Exception e) {
                                logError(getModifierFieldErrorMsg(id), e);
                            }
                        }
                    }
                } catch (Exception e) {
                    logError(getEntryFieldErrorMsg(id), e);
                }

            } catch (Exception e) {
                logError(getCriticalErrorMsg(id), e);
            }
        }
    }

    public void resetEntries() {
        INSTANCE.entryHashMap.clear();
    }

    public void resetModifiers() {
        INSTANCE.modifierHashMap.clear();
        addModifier(new ModifierEntry(0));
    }

    public static HashMap<Identifier, CompostableEntry> getEntries() {
        return INSTANCE.entryHashMap;
    }

    public void addEntry(CompostableEntry entry) {
        HashMap<Identifier, CompostableEntry> entries = getEntries();
        CompostableEntry existingEntry = entries.get(entry.getItemId());
        if (existingEntry != null) {
            existingEntry.copyValueOf(entry);
            return;
        }
        entries.put(entry.getItemId(), entry);
    }

    public static HashMap<Integer, ModifierEntry> getModifiers() {
        return INSTANCE.modifierHashMap;
    }

    public void addModifier(ModifierEntry entry) {
        HashMap<Integer, ModifierEntry> modifiers = getModifiers();
        ModifierEntry existingModifier = modifiers.get(entry.getIndex());
        if (existingModifier != null) {
            existingModifier.copyValueOf(entry);
            return;
        }
        modifiers.put(entry.getIndex(), entry);
    }

    public static Identifier getDefaultResult() {
        return INSTANCE.defaultResult;
    }

    public void setDefaultResult(Identifier result) {
        INSTANCE.defaultResult = result;
    }

    public void setGlobalAutoUse(boolean globalAutoUse) {
        INSTANCE.globalAutoUse = globalAutoUse;
    }

    public static boolean isGlobalAutoUse() {
        return INSTANCE.globalAutoUse;
    }

    public static CompostableEntry getEntryFor(Item item) {
        return getEntries().get(Registries.ITEM.getId(item));
    }

    public static ModifierEntry getModifier(int index) {
        HashMap<Integer, ModifierEntry> modifiers = getModifiers();
        ModifierEntry modifier = modifiers.get(index);
        if (modifier != null) {
            return modifier;
        }
        return modifiers.get(0);
    }

    public static int getItemModifierIndex(Item item) {
        Identifier itemId = Registries.ITEM.getId(item);
        int foundIndex = 0;
        for (ModifierEntry entry : getModifiers().values()) {
            if (entry.hasItemId(itemId)) {
                return entry.getIndex();
            }
        }
        return 0;
    }

    public static boolean hasModifiers() {
        return getModifiers().size()>1;
    }

    public static void logError(String msg, Exception e) {
        UltimateComposterDataConfigurator.LOGGER.error(msg, e);
    }

    public static String getOptionalFieldErrorMsg(Identifier resourceId) {
        return "Minor error occurred while reading field in resource json" + resourceId.toString();
    }

    public static String getEntryFieldErrorMsg(Identifier resourceId) {
        return "Minor error occurred while reading Entry field in resource json" + resourceId.toString();
    }

    public static String getModifierFieldErrorMsg(Identifier resourceId) {
        return "Minor error occurred while reading Modifier field in resource json" + resourceId.toString();
    }

    public static String getCriticalErrorMsg(Identifier resourceId) {
        return "Critical error occurred while loading resource json " + resourceId.toString();
    }
}
