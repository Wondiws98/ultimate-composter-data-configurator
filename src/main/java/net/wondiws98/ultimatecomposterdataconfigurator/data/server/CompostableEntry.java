package net.wondiws98.ultimatecomposterdataconfigurator.data.server;

import com.google.gson.JsonObject;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.wondiws98.ultimatecomposterdataconfigurator.helper.JsonHelper;
import org.jetbrains.annotations.NotNull;

public class CompostableEntry {
    private final Identifier itemId;
    private float value;
    private boolean autoUse = true;

    private static final String ITEM_STRING = "item";
    private static final String VALUE_STRING = "value";
    private static final String AUTO_USE_STRING = "autoUse";

    public CompostableEntry(@NotNull Identifier itemId, Float value) {
        this.itemId = itemId;
        this.value = value;
    }

    public static CompostableEntry fromJsonObject(JsonObject object, Identifier resourceId) {

        // LOAD REQUIRED FIELDS
        CompostableEntry compostableEntry = new CompostableEntry(
                JsonHelper.getRegistryEntryIdentifier(object, ITEM_STRING, Registries.ITEM, false),
                JsonHelper.getFloat(object, VALUE_STRING, false)
        );

        // LOAD OPTIONAL FIELDS
        try {
            Boolean autoUse = JsonHelper.getBoolean(object, AUTO_USE_STRING);
            if (autoUse != null) {
                compostableEntry.withAutoUse(autoUse);
            }
        } catch (Exception e) {
            logError(resourceId, e);
        }

        return compostableEntry;
    }

    public CompostableEntry withAutoUse(boolean autoUse) {
        this.autoUse = autoUse;
        return this;
    }

    public CompostableEntry copyValueOf(CompostableEntry compostableEntry) {
        this.setValue(compostableEntry.getValue());
        this.setAutoUse(compostableEntry.isAutoUse());
        return this;
    }

    public Identifier getItemId() {
        return itemId;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public boolean isAutoUse() {
        return autoUse;
    }

    public void setAutoUse(boolean autoUse) {
        this.autoUse = autoUse;
    }

    private static void logError(Identifier resourceId, Exception e) {
        UCDCDataLoader.logError(UCDCDataLoader.getEntryFieldErrorMsg(resourceId), e);
    }
}
