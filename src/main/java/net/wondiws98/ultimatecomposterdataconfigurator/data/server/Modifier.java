package net.wondiws98.ultimatecomposterdataconfigurator.data.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.Identifier;
import net.wondiws98.ultimatecomposterdataconfigurator.handler.ComposterHandler;
import net.wondiws98.ultimatecomposterdataconfigurator.helper.JsonHelper;

import java.util.ArrayList;
import java.util.List;

public class Modifier {
    private final int index;
    private final ArrayList<Result> results = new ArrayList<>();
    private int minTime = 20;
    private int maxTime = 20;
    private int priority = 0;

    private static final String INDEX_STRING = "index";
    private static final String RESULT_STRING = "result";
    private static final String RESULTS_STRING = "results";
    private static final String MIN_TIME_STRING = "minTime";
    private static final String MAX_TIME_STRING = "maxTime";
    private static final String PRIORITY_STRING = "priority";

    public Modifier(Integer index) {
        this.index = index;
    }

    public static Modifier fromJsonObject(JsonObject object, Identifier resourceId) {
        Modifier modifier = new Modifier(JsonHelper.getInt(object, INDEX_STRING, false));

        if (modifier.getIndex() < 0 || modifier.getIndex() > 255) {
            throw new JsonSyntaxException("Modifier's index MUST be between 0 and 255");
        }

        try {
            if (object.has(RESULT_STRING)) {
                modifier.addResult(Result.fromJsonObject(object.getAsJsonObject(RESULT_STRING), resourceId));
            }
        } catch (Exception e) {
            logError(resourceId, e);
        }

        try {
            List<JsonElement> elements = JsonHelper.getList(object, RESULTS_STRING);
            if (elements != null) {
                if (modifier.getResults().size() + elements.size() > 12) {
                    Modifier.logError(resourceId, new JsonSyntaxException("Modifiers only supports up to 12 Results!"));
                }
                for (JsonElement element : elements) {
                    try {
                        if (modifier.addResult(Result.fromJsonObject(element.getAsJsonObject(), resourceId))) break;
                    } catch (Exception e) {
                        Result.logError(resourceId, e);
                    }
                }
            }
        } catch (Exception e) {
            logError(resourceId, e);
        }

        try {
            Integer minTime = JsonHelper.getInt(object, MIN_TIME_STRING);
            if (minTime != null) {
                boolean hasMax = false;
                try {
                    Integer maxTime = JsonHelper.getInt(object, MAX_TIME_STRING);
                    if (maxTime != null) {
                        hasMax = true;
                        modifier.setTime(minTime, maxTime);
                    }
                } catch (Exception e) {
                    logError(resourceId, e);
                }
                if (!hasMax) {
                    modifier.setTime(minTime, minTime);
                }
            }
        } catch (Exception e) {
            logError(resourceId, e);
        }

        try {
            Integer priority = JsonHelper.getInt(object, PRIORITY_STRING);
            if (priority != null) {
                modifier.setPriority(priority);
            }
        } catch (Exception e) {
            logError(resourceId, e);
        }

        if (modifier.getResults().isEmpty()) {
            modifier.getResults().add(new Result());
        }

        return modifier;
    }

    public int getIndex() {
        return index;
    }

    public ArrayList<Result> getResults() {
        return results;
    }

    public int getPriority() {
        return priority;
    }

    public int getRandomTime() {
        int min = minTime, max = maxTime;
        if (max > min) {
            return ComposterHandler.getRan().nextInt(min, max);
        }
        return min;
    }

    public boolean addResult(Result result) {
        if (getResults().size() < 12) {
            getResults().add(result);
            return false;
        }
        return true;
    }

    public Modifier withResult(Result result) {
        addResult(result);
        return this;
    }

    public void setTime(int minTime, int maxTime) {
        this.minTime = minTime;
        this.maxTime = maxTime;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public static void logError(Identifier resourceId, Exception e) {
        UCDCDataLoader.logError("Minor error occurred while reading Modifier field in resource json" + resourceId.toString(), e);
    }

    @Override
    public String toString() {
        return "Modifier{" +
                "index=" + index +
                ", results=" + results +
                ", minTime=" + minTime +
                ", maxTime=" + maxTime +
                ", priority=" + priority +
                '}';
    }
}
