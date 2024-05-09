package net.wondiws98.ultimatecomposterdataconfigurator.data.server;

import com.google.gson.JsonObject;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.wondiws98.ultimatecomposterdataconfigurator.handler.ComposterHandler;
import net.wondiws98.ultimatecomposterdataconfigurator.helper.JsonObjectItemReader;
import net.wondiws98.ultimatecomposterdataconfigurator.helper.JsonHelper;

public class Result {
    private Identifier itemId = Registries.ITEM.getId(Items.BONE_MEAL);
    private float minYield = 1;
    private float maxYield = 1;

    private static final String MIN_YIELD_STRING = "minYield";
    private static final String MAX_YIELD_STRING = "maxYield";

    public static Result fromJsonObject(JsonObject object, Identifier resourceId) {
        Result result = new Result();

        try {
            Identifier itemId = JsonObjectItemReader.loadItem(object, resourceId, "Result", false);
            if (itemId != null) {
                result.setItemId(itemId);
            }
        } catch (Exception e) {
            logError(resourceId, e);
        }

        try {
            Float minYield = JsonHelper.getFloat(object, MIN_YIELD_STRING);
            if (minYield != null) {
                boolean hasMax = false;
                try {
                    Float maxYield = JsonHelper.getFloat(object, MAX_YIELD_STRING);
                    if (maxYield != null) {
                        hasMax = true;
                        result.setYield(minYield, maxYield);
                    }
                } catch (Exception e) {
                    logError(resourceId, e);
                }
                if (!hasMax) {
                    result.setYield(minYield, minYield);
                }
            }
        } catch (Exception e) {
            logError(resourceId, e);
        }

        return result;
    }

    public Identifier getItemId() {
        return itemId;
    }

    public int getRandomYield() {
        float min = minYield, max = maxYield;
        if (max > min) {
            return ComposterHandler.ranFloatToInt(ComposterHandler.getRan().nextFloat(min, max));
        }
        return ComposterHandler.ranFloatToInt(min);
    }

    public void setItemId(Identifier itemId) {
        this.itemId = itemId;
    }

    public void setYield(float minYield, float maxYield) {
        int maxCount = Registries.ITEM.get(itemId).getMaxCount();
        this.minYield = Math.min(minYield, maxCount);
        this.maxYield = Math.min(maxYield, maxCount);
    }

    public static void logError(Identifier resourceId, Exception e) {
        UCDCDataLoader.logError("Minor error occurred while reading Result field in resource json" + resourceId.toString(), e);
    }

    @Override
    public String toString() {
        return "Result{" +
                "itemId=" + itemId +
                ", minYield=" + minYield +
                ", maxYield=" + maxYield +
                '}';
    }
}
