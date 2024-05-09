package net.wondiws98.ultimatecomposterdataconfigurator.data.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.Identifier;
import net.wondiws98.ultimatecomposterdataconfigurator.handler.ComposterHandler;
import net.wondiws98.ultimatecomposterdataconfigurator.helper.JsonObjectItemReader;
import net.wondiws98.ultimatecomposterdataconfigurator.helper.JsonHelper;

import java.util.ArrayList;


public class Entry {
    private final Identifier itemId;
    private float minIncreaseChance = 0.0f;
    private float maxIncreaseChance = 0.0f;
    private int modifierIndex = 0;
    private boolean autoUse = true;

    private static final String MIN_INCREASE_CHANCE_STRING = "minIncreaseChance";
    private static final String MAX_INCREASE_CHANCE_STRING = "maxIncreaseChance";
    private static final String MODIFIER_INDEX_STRING = "modifierIndex";
    private static final String AUTO_USE_STRING = "autoUse";

    public Entry(Identifier itemId) {
        this.itemId = itemId;
    }

    public static ArrayList<Entry> fromJsonObject(JsonObject object, Identifier resourceId) {
        ArrayList<Entry> entries = new ArrayList<>();

        JsonObjectItemReader.loadItems(object, resourceId, "Entry").forEach(identifier -> entries.add(new Entry(identifier)));

        try {
            Float minIncreaseChance = JsonHelper.getFloat(object, MIN_INCREASE_CHANCE_STRING);
            if (minIncreaseChance != null) {
                boolean hasMax = false;
                try {
                    Float maxIncreaseChance = JsonHelper.getFloat(object, MAX_INCREASE_CHANCE_STRING);
                    if (maxIncreaseChance != null) {
                        hasMax = true;
                        entries.forEach(entry -> entry.setIncreaseChance(minIncreaseChance, maxIncreaseChance));
                    }
                } catch (Exception e) {
                    logError(resourceId, e);
                }
                if (!hasMax) {
                    entries.forEach(entry -> entry.setIncreaseChance(minIncreaseChance, minIncreaseChance));
                }
            }
        } catch (Exception e) {
            logError(resourceId, e);
        }

        try {
            Integer modifierIndex = JsonHelper.getInt(object, MODIFIER_INDEX_STRING);
            if (modifierIndex != null) {
                if (modifierIndex < 0 || modifierIndex > 255) {
                    throw new JsonSyntaxException("Entry's modifierIndex MUST be between 0 and 255.");
                }
                entries.forEach(entry -> entry.setModifierIndex(modifierIndex));
            }
        } catch (Exception e) {
            logError(resourceId, e);
        }

        try {
            Boolean autoUse = JsonHelper.getBoolean(object, AUTO_USE_STRING);
            if (autoUse != null) {
                entries.forEach(entry -> entry.setAutoUse(autoUse));
            }
        } catch (Exception e) {
            logError(resourceId, e);
        }

        return entries;
    }

    public Identifier getItemId() {
        return itemId;
    }

    public int getModifierIndex() {
        if (!hasModifierIndex()) {
            return 0;
        }
        return modifierIndex;
    }

    public boolean hasModifierIndex() {
        return modifierIndex > 0 && modifierIndex <= 255;
    }

    public boolean isAutoUse() {
        return autoUse && minIncreaseChance+maxIncreaseChance > 0.0f;
    }

    public int getRandomIncreaseChance() {
        float min = minIncreaseChance, max = maxIncreaseChance;
        if (max > min) {
            return ComposterHandler.ranFloatToInt(ComposterHandler.getRan().nextFloat(min, max));
        }
        return ComposterHandler.ranFloatToInt(min);
    }

    public void setIncreaseChance(float minIncreaseChance, float maxIncreaseChance) {
        this.minIncreaseChance = minIncreaseChance;
        this.maxIncreaseChance = maxIncreaseChance;
    }

    public void setModifierIndex(int modifierIndex) {
        this.modifierIndex = modifierIndex;
    }

    public void setAutoUse(boolean autoUse) {
        this.autoUse = autoUse;
    }

    public static void logError(Identifier resourceId, Exception e) {
        UCDCDataLoader.logError("Minor error occurred while reading Entry field in resource json" + resourceId.toString(), e);
    }

    @Override
    public String toString() {
        return "Entry{" +
                "itemId=" + itemId +
                ", minIncreaseChance=" + minIncreaseChance +
                ", maxIncreaseChance=" + maxIncreaseChance +
                ", modifierIndex=" + modifierIndex +
                ", autoUse=" + autoUse +
                '}';
    }
}
