package net.wondiws98.ultimatecomposterdataconfigurator.handler;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.wondiws98.ultimatecomposterdataconfigurator.data.server.Entry;
import net.wondiws98.ultimatecomposterdataconfigurator.data.server.Modifier;
import net.wondiws98.ultimatecomposterdataconfigurator.data.server.Result;
import net.wondiws98.ultimatecomposterdataconfigurator.data.server.UCDCDataLoader;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class ComposterHandler {
    public static final IntProperty LEVEL = Properties.LEVEL_8;
    private static final java.util.Random RAN = new java.util.Random();

    public static java.util.Random getRan() {
        return RAN;
    }

    public static Modifier getIndexedModifier(int modifierIndex) {
        return UCDCDataLoader.getModifier(modifierIndex);
    }

    public static @NotNull ArrayList<ItemStack> getModifierItemStacks(int modifierIndex) {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        Modifier modifier = getIndexedModifier(modifierIndex);
        for (Result result : modifier.getResults()) {
            int yield = result.getRandomYield();
            if (yield > 0) {
                stacks.add(new ItemStack(Registries.ITEM.get(result.getItemId()), yield));
            }
        }
        return stacks;
    }

    public static Entry getStackEntry(ItemStack stack) {
        return UCDCDataLoader.getEntryFor(stack.getItem());
    }

    public static Modifier getPrioritizedModifier(int oldModifierIndex, int newModifierIndex) {
        Modifier oldModifier = getIndexedModifier(oldModifierIndex);
        Modifier newModifier = getIndexedModifier(newModifierIndex);
        if (newModifier != null && oldModifierIndex != newModifierIndex && newModifier.getPriority() >= oldModifier.getPriority()) {
            return newModifier;
        }
        return oldModifier;
    }

    public static int ranFloatToInt(float ranFloat) {
        int intPart = (int)ranFloat;
        double decPart = ranFloat-intPart;
        intPart += (decPart > 0.0f && RAN.nextDouble() < decPart) ? 1 : 0;
        return intPart;
    }

    public static @NotNull Pair<Integer, Integer> calculateConsumption(@NotNull BlockState state, @NotNull Entry entry, ItemStack stack) {
        int itemConsumedCount = 0;
        int composterLevelIncrease = 0;
        int currentLevel = state.get(LEVEL);
        if (UCDCDataLoader.isGlobalAutoUse() && entry.isAutoUse()) {
            for (int i = 1; i <= stack.getCount() && (currentLevel + composterLevelIncrease) < 7; i++) {
                itemConsumedCount = i;
                composterLevelIncrease += entry.getRandomIncreaseChance();
            }
        } else {
            itemConsumedCount = 1;
            composterLevelIncrease = entry.getRandomIncreaseChance();
        }
        return new Pair<>(composterLevelIncrease, itemConsumedCount);
    }
}
