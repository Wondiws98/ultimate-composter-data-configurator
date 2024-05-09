package net.wondiws98.ultimatecomposterdataconfigurator.data.server;

import com.google.gson.*;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.wondiws98.ultimatecomposterdataconfigurator.UltimateComposterDataConfigurator;
import net.wondiws98.ultimatecomposterdataconfigurator.helper.JsonHelper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public final class UCDCDataLoader implements SimpleSynchronousResourceReloadListener {
    private static UCDCDataLoader INSTANCE = null;
    public static UCDCDataLoader getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UCDCDataLoader();
        }
        return INSTANCE;
    }

    private boolean globalAutoUse = true;
    private final HashMap<Identifier, Entry> entryHashMap = new HashMap<>();
    private final HashMap<Integer, Modifier> modifierHashMap = new HashMap<>();

    private static final String DATA_FOLDER = "compostable";
    private static final String GLOBAL_AUTO_USE_STRING = "globalAutoUse";
    private static final String REPLACE_ENTRIES_STRING = "replaceEntries";
    private static final String REPLACE_MODIFIERS_STRING = "replaceModifiers";
    private static final String ENTRIES_STRING = "entries";
    private static final String MODIFIERS_STRING = "modifiers";

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
            Optional<Resource> optionalResource = manager.getResource(id);
            if (optionalResource.isPresent()) {
                try (InputStream stream = optionalResource.get().getInputStream()) {
                    JsonObject object = (JsonObject) JsonParser.parseReader(new InputStreamReader(stream));
                    // RESET ENTRIES
                    try {
                        Boolean replaceEntries = JsonHelper.getBoolean(object, REPLACE_ENTRIES_STRING);
                        if (replaceEntries != null && replaceEntries) {
                            resetEntries();
                        }
                    } catch (Exception e) {
                        logError(getOptionalFieldErrorMsg(id), e);
                    }

                    // RESET MODIFIERS
                    try {
                        Boolean replaceModifiers = JsonHelper.getBoolean(object, REPLACE_MODIFIERS_STRING);
                        if (replaceModifiers != null && replaceModifiers) {
                            resetModifiers();
                        }
                    } catch (Exception e) {
                        logError(getOptionalFieldErrorMsg(id), e);
                    }

                    // SET GLOBAL AUTO USE
                    try {
                        Boolean globalAutoUse = JsonHelper.getBoolean(object, GLOBAL_AUTO_USE_STRING);
                        if (globalAutoUse != null) {
                            setGlobalAutoUse(globalAutoUse);
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
                                    Entry.fromJsonObject(element.getAsJsonObject(), id).forEach(this::addEntry);
                                } catch (Exception e) {
                                    Entry.logError(id, e);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Entry.logError(id, e);
                    }

                    // LOAD MODIFIERS
                    try {
                        List<JsonElement> elements = JsonHelper.getList(object, MODIFIERS_STRING);
                        if (elements != null) {
                            for (JsonElement element : elements) {
                                try {
                                    addModifier(Modifier.fromJsonObject(element.getAsJsonObject(), id));
                                } catch (Exception e) {
                                    Modifier.logError(id, e);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Modifier.logError(id, e);
                    }

                } catch (Exception e) {
                    logError(getCriticalErrorMsg(id), e);
                }
            }
        }
    }

    public void resetEntries() {
        INSTANCE.entryHashMap.clear();
    }

    public void resetModifiers() {
        getModifierHashMap().clear();
        addModifier(new Modifier(0).withResult(new Result()));
    }

    public static HashMap<Identifier, Entry> getEntries() {
        return INSTANCE.entryHashMap;
    }

    public void addEntry(Entry entry) {
        getEntries().put(entry.getItemId(), entry);
    }

    public static HashMap<Integer, Modifier> getModifierHashMap() {
        return INSTANCE.modifierHashMap;
    }

    public void addModifier(Modifier modifier) {
        getModifierHashMap().put(modifier.getIndex(), modifier);
    }

    public void setGlobalAutoUse(boolean globalAutoUse) {
        INSTANCE.globalAutoUse = globalAutoUse;
    }

    public static boolean isGlobalAutoUse() {
        return INSTANCE.globalAutoUse;
    }

    public static Entry getEntryFor(Item item) {
        return getEntries().get(Registries.ITEM.getId(item));
    }

    public static Modifier getModifier(int index) {
        HashMap<Integer, Modifier> modifiers = getModifierHashMap();
        Modifier modifier = modifiers.get(index);
        if (modifier != null) {
            return modifier;
        }
        return modifiers.get(0);
    }

    public static void logError(String msg, Exception e) {
        UltimateComposterDataConfigurator.LOGGER.error(msg, e);
    }

    public static String getOptionalFieldErrorMsg(Identifier resourceId) {
        return "Minor error occurred while reading field in resource json" + resourceId.toString();
    }

    public static String getCriticalErrorMsg(Identifier resourceId) {
        return "Critical error occurred while loading resource json " + resourceId.toString();
    }
}
