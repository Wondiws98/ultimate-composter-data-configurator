package net.wondiws98.ultimatecomposterdataconfigurator.data.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.wondiws98.ultimatecomposterdataconfigurator.helper.JsonHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ModifierEntry {
    private int index = 0;
    private Identifier result = Registries.ITEM.getId(Items.BONE_MEAL);
    private int minYield = 1;
    private int maxYield = 1;
    private int minTime = 20;
    private int maxTime = 20;
    private int priority = 0;
    private ArrayList<Identifier> itemIds;
    private Random rand = new Random();

    private static final String INDEX_STRING = "index";
    private static final String RESULT_STRING = "result";
    private static final String MIN_YIELD_STRING = "minYield";
    private static final String MAX_YIELD_STRING = "maxYield";
    private static final String MIN_TIME_STRING = "minTime";
    private static final String MAX_TIME_STRING = "maxTime";
    private static final String PRIORITY_STRING = "priority";
    private static final String ITEMS_STRING = "items";

    public ModifierEntry(Integer index) {
        this.index = index;
    }

    public static ModifierEntry fromJsonObject(JsonObject object, Identifier resourceId) {
        // LOAD REQUIRED FIELDS
        ModifierEntry modifierEntry = new ModifierEntry(JsonHelper.getInt(object, INDEX_STRING, false));

        // LOAD OPTIONS FIELDS
            // LOAD RESULT
        try {
            Identifier resultId = JsonHelper.getRegistryEntryIdentifier(object, RESULT_STRING, Registries.ITEM);
            if (resultId != null) {
                modifierEntry.withResult(resultId);
            }
        } catch (Exception e) {
            logError(resourceId, e);
        }
            // LOAD YIELD
        try {
            Integer minYield = JsonHelper.getInt(object, MIN_YIELD_STRING);
            if (minYield != null) {
                boolean hasMax = false;
                try {
                    Integer maxYield = JsonHelper.getInt(object, MAX_YIELD_STRING);
                    if (maxYield != null) {
                        hasMax = true;
                        modifierEntry.withYield(minYield, maxYield);
                    }
                } catch (Exception e) {
                    logError(resourceId, e);
                }
                if (!hasMax) {
                    modifierEntry.withYield(minYield);
                }
            }
        } catch (Exception e) {
            logError(resourceId, e);
        }
            // LOAD TIME
        try {
            Integer minTime = JsonHelper.getInt(object, MIN_TIME_STRING);
            if (minTime != null) {
                boolean hasMax = false;
                try {
                    Integer maxTIME = JsonHelper.getInt(object, MAX_TIME_STRING);
                    if (maxTIME != null) {
                        hasMax = true;
                        modifierEntry.withTime(minTime, maxTIME);
                    }
                } catch (Exception e) {
                    logError(resourceId, e);
                }
                if (!hasMax) {
                    modifierEntry.withTime(minTime);
                }
            }
        } catch (Exception e) {
            logError(resourceId, e);
        }
            // LOAD PRIORITY
        try {
            Integer priority = JsonHelper.getInt(object, PRIORITY_STRING);
            if (priority != null) {
                modifierEntry.withPriority(priority);
            }
        } catch (Exception e) {
            logError(resourceId, e);
        }
            // LOAD ITEMS
        try {
            List<JsonElement> elements = JsonHelper.getList(object, ITEMS_STRING);
            if (elements != null) {
                modifierEntry.setItemIds(new ArrayList<>());
                for (JsonElement element : elements) {
                    try {
                        modifierEntry.addId(JsonHelper.getRegistryEntryIdentifier(element, ITEMS_STRING, Registries.ITEM));
                    } catch (Exception e) {
                        logError(resourceId, e);
                    }
                }
            }
        } catch (Exception e) {
            logError(resourceId, e);
        }

        return modifierEntry;
    }

    public ModifierEntry withResult(Identifier result) {
        this.result = result;
        return this;
    }

    public ModifierEntry withYield(int min, int max) {
        this.minYield = min;
        this.maxYield = max;
        return this;
    }

    public ModifierEntry withYield(int yield) {
        return withYield(yield, yield);
    }

    public ModifierEntry withTime(int minTime, int maxTime) {
        this.minTime = minTime;
        this.maxTime = maxTime;
        return this;
    }

    public ModifierEntry withTime(int time) {
        return withTime(time, time);
    }

    public ModifierEntry withPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public ModifierEntry copyValueOf(ModifierEntry modifierEntry) {
        this.setResult(modifierEntry.getResult());
        this.setMinYield(modifierEntry.getMinYield());
        this.setMaxYield(modifierEntry.getMaxYield());
        this.setMinTime(modifierEntry.getMinTime());
        this.setMaxTime(modifierEntry.getMaxTime());
        this.setPriority(modifierEntry.getPriority());
        this.setItemIds(modifierEntry.getItemIds());
        return this;
    }

    public ArrayList<Identifier> getItemIds() {
        return itemIds;
    }

    public int getPriority() {
        return priority;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public int getMinTime() {
        return minTime;
    }

    public int getMaxYield() {
        return maxYield;
    }

    public int getMinYield() {
        return minYield;
    }

    public Identifier getResult() {
        return result;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setResult(Identifier result) {
        this.result = result;
    }

    public void setMinYield(int minYield) {
        this.minYield = minYield;
    }

    public void setMaxTime(int maxTime) {
        this.maxTime = maxTime;
    }

    public void setItemIds(ArrayList<Identifier> itemIds) {
        this.itemIds = itemIds;
    }

    public boolean hasItemId(Identifier identifier) {
        if (getItemIds() == null) {
            return false;
        }
        return getItemIds().contains(identifier);
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setMinTime(int minTime) {
        this.minTime = minTime;
    }

    public void setMaxYield(int maxYield) {
        this.maxYield = maxYield;
    }

    public int getRandomYield() {
        int max = getMaxYield(), min = getMinYield();
        return rand.nextInt(max - min + 1) + min;
    }

    public int getRandomTime() {
        int max = getMaxTime(), min = getMinTime();
       return rand.nextInt(max - min + 1) + min;
    }

    public void addId(Identifier itemId) {
        this.itemIds.add(itemId);
    }

    private static void logError(Identifier resourceId, Exception e) {
        UCDCDataLoader.logError(UCDCDataLoader.getModifierFieldErrorMsg(resourceId), e);
    }
}
