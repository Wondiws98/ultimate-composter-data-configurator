package net.wondiws98.ultimatecomposterdataconfigurator.handler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComposterBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import net.wondiws98.ultimatecomposterdataconfigurator.UltimateComposterDataConfigurator;
import net.wondiws98.ultimatecomposterdataconfigurator.data.server.ModifierEntry;
import net.wondiws98.ultimatecomposterdataconfigurator.data.server.UCDCDataLoader;
import net.wondiws98.ultimatecomposterdataconfigurator.data.server.CompostableEntry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.random.RandomGenerator;


public abstract class ComposterHandler {
    public static final IntProperty LEVEL = Properties.LEVEL_8;
    public static final IntProperty MODIFIER = IntProperty.of("modifier", 0, 255);

    public static ItemStack getModifiersItemStack(int modifierIndex) {
        ModifierEntry modifierEntry = UCDCDataLoader.getModifier(modifierIndex);
        return new ItemStack(Registries.ITEM.get(modifierEntry.getResult()), modifierEntry.getRandomYield());
    }

    public static int getLevelIncrease(Random random, float itemValue) {
        int levelIncrease = (int)Math.floor(itemValue);
        float decimal = itemValue-(float)levelIncrease;
        levelIncrease += (decimal > 0.0f && random.nextDouble() < (double)decimal) ? 1 : 0;
        return levelIncrease;
    }

    @Contract("_, _, _, _ -> new")
    @SuppressWarnings("StatementWithEmptyBody")
    public static @NotNull Pair<Integer, Integer> calculateConsumption(@NotNull BlockState state, Random random, @NotNull CompostableEntry entry, ItemStack stack) {
        int itemConsumedCount = 0;
        int composterLevelIncrease = 0;
        int currentLevel = state.get(LEVEL);
        float itemValue = entry.getValue();
        if (UCDCDataLoader.isGlobalAutoUse() && entry.isAutoUse()) {
            for (int i = 0; i <= stack.getCount() && (currentLevel + composterLevelIncrease) < 7; i++, itemConsumedCount = i, composterLevelIncrease += getLevelIncrease(random, itemValue));
        } else {
            itemConsumedCount = 1;
            composterLevelIncrease = getLevelIncrease(random, itemValue);
        }
        return new Pair<>(composterLevelIncrease, itemConsumedCount);
    }

    public static void setComposterState(@Nullable Entity user, @NotNull BlockState state, @NotNull WorldAccess world, BlockPos pos, int levelIncrease, int modifierIndex) {
        int newLevel = state.get(LEVEL)+levelIncrease;
        int currentModifierIndex = state.get(MODIFIER);
        ModifierEntry oldModifier = UCDCDataLoader.getModifier(currentModifierIndex);
        ModifierEntry incomingModifier = UCDCDataLoader.getModifier(modifierIndex);
        ModifierEntry newModifier;
        if (currentModifierIndex != modifierIndex && incomingModifier.getPriority() >= oldModifier.getPriority()) {
            newModifier = incomingModifier;
        } else {
            newModifier = oldModifier;
        }
        UltimateComposterDataConfigurator.LOGGER.info("modifier index = "+modifierIndex);
        BlockState blockState = (BlockState)state.with(LEVEL, Math.min(newLevel, 7)).with(MODIFIER, newModifier.getIndex());
        world.setBlockState(pos, blockState, Block.NOTIFY_ALL);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(user, blockState));
        if (newLevel == 7) {
            world.scheduleBlockTick(pos, state.getBlock(), newModifier.getRandomTime());
        }
    }

    public static BlockState emptyFullComposter(Entity user, BlockState state, World world, BlockPos pos) {
        if (!world.isClient) {
            Vec3d vec3d = Vec3d.add(pos, 0.5, 1.01, 0.5).addRandom(world.random, 0.7f);
            ItemEntity itemEntity = new ItemEntity(world, vec3d.getX(), vec3d.getY(), vec3d.getZ(), getModifiersItemStack(state.get(MODIFIER)));
            itemEntity.setToDefaultPickupDelay();
            world.spawnEntity(itemEntity);
        }
        BlockState blockState = ComposterHandler.emptyComposter(user, state, world, pos);
        world.playSound(null, pos, SoundEvents.BLOCK_COMPOSTER_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
        return blockState;
    }

    static BlockState emptyComposter(@Nullable Entity user, BlockState state, WorldAccess world, BlockPos pos) {
        BlockState blockState = (BlockState)state.with(LEVEL, 0).with(MODIFIER, 0);
        world.setBlockState(pos, blockState, Block.NOTIFY_ALL);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(user, blockState));
        return blockState;
    }

    public static class FullComposterInventory
            extends SimpleInventory
            implements SidedInventory {
        private final BlockState state;
        private final WorldAccess world;
        private final BlockPos pos;
        private boolean dirty;

        public FullComposterInventory(BlockState state, WorldAccess world, BlockPos pos, ItemStack outputItem) {
            super(outputItem);
            this.state = state;
            this.world = world;
            this.pos = pos;
        }

        @Override
        public int getMaxCountPerStack() {
            return 1;
        }

        @Override
        public int[] getAvailableSlots(Direction side) {
            int[] nArray;
            if (side == Direction.DOWN) {
                int[] nArray2 = new int[1];
                nArray = nArray2;
                nArray2[0] = 0;
            } else {
                nArray = new int[]{};
            }
            return nArray;
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
            return false;
        }

        @Override
        public boolean canExtract(int slot, ItemStack stack, Direction dir) {
            return !this.dirty && dir == Direction.DOWN;
        }

        @Override
        public void markDirty() {
            ComposterHandler.emptyComposter(null, this.state, this.world, this.pos);
            this.dirty = true;
        }
    }

    public static class ComposterInventory
            extends SimpleInventory
            implements SidedInventory {
        private final BlockState state;
        private final WorldAccess world;
        private final BlockPos pos;
        private boolean dirty;

        public ComposterInventory(BlockState state, WorldAccess world, BlockPos pos) {
            super(1);
            this.state = state;
            this.world = world;
            this.pos = pos;
        }

        @Override
        public int getMaxCountPerStack() {
            return 1;
        }

        @Override
        public int[] getAvailableSlots(Direction side) {
            int[] nArray;
            if (side == Direction.UP) {
                int[] nArray2 = new int[1];
                nArray = nArray2;
                nArray2[0] = 0;
            } else {
                nArray = new int[]{};
            }
            return nArray;
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
            return !this.dirty && dir == Direction.UP && UCDCDataLoader.getEntryFor(stack.getItem()) != null;
        }

        @Override
        public boolean canExtract(int slot, ItemStack stack, Direction dir) {
            return false;
        }

        @Override
        public void markDirty() {
            ItemStack itemStack = this.getStack(0);
            CompostableEntry entry = UCDCDataLoader.getEntryFor(itemStack.getItem());
            if (!itemStack.isEmpty() && entry != null) {
                this.dirty = true;
                int levelIncrease = getLevelIncrease(this.world.getRandom(), entry.getValue());
                ComposterHandler.setComposterState(null, this.state, this.world, this.pos, levelIncrease, UCDCDataLoader.getItemModifierIndex(itemStack.getItem()));
                this.world.syncWorldEvent(WorldEvents.COMPOSTER_USED, this.pos, levelIncrease > 0 ? 1 : 0);
                this.removeStack(0);
            }
        }
    }

    public static class DummyInventory
            extends SimpleInventory
            implements SidedInventory {
        public DummyInventory() {
            super(0);
        }

        @Override
        public int[] getAvailableSlots(Direction side) {
            return new int[0];
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
            return false;
        }

        @Override
        public boolean canExtract(int slot, ItemStack stack, Direction dir) {
            return false;
        }
    }
}
