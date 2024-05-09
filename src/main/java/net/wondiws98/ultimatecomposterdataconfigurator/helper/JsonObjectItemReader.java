package net.wondiws98.ultimatecomposterdataconfigurator.helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.Identifier;
import net.wondiws98.ultimatecomposterdataconfigurator.data.server.UCDCDataLoader;

import java.util.ArrayList;
import java.util.List;

public class JsonObjectItemReader {
    private static final String ITEM_STRING = "item";
    private static final String ITEMS_STRING = "items";

    public static ArrayList<Identifier> loadItems(JsonObject object, Identifier resourceId, String objectType) {
        ArrayList<Identifier> itemIds = new ArrayList<>();

        try {
            itemIds.add(loadItem(object, resourceId, objectType, false));
        } catch (Exception e) {
            logItemLoadingError(resourceId, objectType, e);
        }

        try {
            List<JsonElement> elements = JsonHelper.getList(object, ITEMS_STRING);
            if (elements != null) {
                for (JsonElement element : elements) {
                    try {
                        itemIds.add(JsonHelper.getItemIdentifier(element, ITEM_STRING));
                    } catch (Exception e) {
                        logItemLoadingError(resourceId, objectType, e);
                    }
                }
            }
        } catch (Exception e) {
            logItemLoadingError(resourceId, objectType, e);
        }

        return itemIds;
    }

    public static Identifier loadItem(JsonObject object, Identifier resourceId, String objectType, boolean required) {
        try {
            Identifier itemId = JsonHelper.getItemIdentifier(object, ITEM_STRING, !required);
            if (itemId != null) {
                return itemId;
            } else if (required) {
                throw new JsonSyntaxException("There was an error loading required Item Field of " + objectType);
            }
        } catch (Exception e) {
            logItemLoadingError(resourceId, objectType, e);
        }
        return null;
    }

    private static void logItemLoadingError(Identifier resourceId, String objectType, Exception e) {
        UCDCDataLoader.logError("Failed to load error for Resource json " + resourceId + " for " + objectType, e);
    }
}
