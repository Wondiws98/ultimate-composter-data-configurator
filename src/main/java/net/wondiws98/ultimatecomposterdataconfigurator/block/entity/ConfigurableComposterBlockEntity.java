package net.wondiws98.ultimatecomposterdataconfigurator.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.wondiws98.ultimatecomposterdataconfigurator.block.ConfigurableComposterBlock;
import net.wondiws98.ultimatecomposterdataconfigurator.data.server.Entry;
import net.wondiws98.ultimatecomposterdataconfigurator.handler.ComposterHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

public class ConfigurableComposterBlockEntity extends LockableContainerBlockEntity implements SidedInventory {
    private static final int COMPOSTING_SLOT = 0;
    private static final int MODIFIER_SLOT = 1;
    private static final int[] TEMPORARY_SLOTS = new int[]{COMPOSTING_SLOT, MODIFIER_SLOT};
    private static final int[] INVENTORY_SLOTS = IntStream.rangeClosed(2, 28).toArray();
    private static final int[] RESULT_SLOTS = IntStream.rangeClosed(29, 40).toArray();
    protected DefaultedList<ItemStack> inventory = DefaultedList.ofSize(TEMPORARY_SLOTS.length + INVENTORY_SLOTS.length + RESULT_SLOTS.length, ItemStack.EMPTY);
    int waitTime;
    int waitTimeTotal = -1;

    public ConfigurableComposterBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(UCDCBlockEntities.COMPOSTER_BLOCK_ENTITY_BLOCK_ENTITY, blockPos, blockState);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        this.waitTime = nbt.getShort("WaitTime");
        this.waitTimeTotal = nbt.getShort("WaitTimeTotal");
        Inventories.readNbt(nbt, this.inventory);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putShort("WaitTime", (short)this.waitTime);
        nbt.putShort("WaitTimeTotal", (short)this.waitTimeTotal);
        Inventories.writeNbt(nbt, this.inventory);
    }

    public static void tick(World world, BlockPos pos, BlockState state, ConfigurableComposterBlockEntity entity) {
        if (state.get(ConfigurableComposterBlock.LEVEL) == 7) {
            if (entity.waitTime < entity.waitTimeTotal) {
                entity.waitTime++;
                if (entity.waitTime >= entity.waitTimeTotal) {
                    entity.waitTimeTotal = -1;
                    entity.waitTime = 0;
                    ConfigurableComposterBlock.setComposterDone(state, world, pos);
                    entity.clear();
                    entity.insertResults(ComposterHandler.getModifierItemStacks(state.get(ConfigurableComposterBlock.MODIFIER)));
                }
            }
            if (entity.waitTimeTotal == -1) {
                entity.waitTimeTotal = ComposterHandler.getIndexedModifier(state.get(ConfigurableComposterBlock.MODIFIER)).getRandomTime();
            }
        }
    }

    public void insertResults(ArrayList<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            for (int i : RESULT_SLOTS) {
                if (inventory.get(i).isEmpty() || ItemStack.canCombine(inventory.get(i), stack)) {
                    setStack(i, stack);
                    break;
                }
            }
        }
        markDirty();
    }

    public void transferToInventory(int slot, BlockState state, World world, BlockPos pos) {
        ItemStack stack = removeStack(slot);
        insertInventory(stack, stack.getCount());
        Entry entry = ComposterHandler.getStackEntry(stack);
        if (entry != null) {
            BlockState state1 = ConfigurableComposterBlock.setComposterState(null, state, world, pos, entry.getRandomIncreaseChance(), entry.getModifierIndex());
            world.syncWorldEvent(WorldEvents.COMPOSTER_USED, pos, state1 != state ? 1 : 0);
        }
    }

    public boolean insertInventory(ItemStack stack, int amount) {
        for (int i : INVENTORY_SLOTS) {
            if (inventory.get(i).isEmpty() || ItemStack.canCombine(inventory.get(i), stack)) {
                setStack(i, new ItemStack(stack.getItem(), amount+inventory.get(i).getCount()));
                markDirty();
                return true;
            }
        }
        return false;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.world != null && !this.world.isClient) {
            BlockState composterBlockState = world.getBlockState(pos);
            for (int slot : TEMPORARY_SLOTS) {
                if (!inventory.get(slot).isEmpty()) {
                    transferToInventory(slot, composterBlockState, this.world, this.pos);
                }
            }
            if (composterBlockState.get(ConfigurableComposterBlock.LEVEL) == 8 && isEmpty()) {
                ConfigurableComposterBlock.emptyComposter(null, composterBlockState, this.world, this.pos);
            }
        }
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side != Direction.DOWN) {
            if (side == Direction.UP) {
                return new int[]{COMPOSTING_SLOT};
            }
            return new int[]{MODIFIER_SLOT};
        } else {
            return RESULT_SLOTS;
        }
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (this.world != null && !this.world.isClient) {
            Entry entry = ComposterHandler.getStackEntry(stack);
            if (entry != null) {
                BlockState composterBlockState = world.getBlockState(pos);
                if (composterBlockState.getBlock() instanceof ConfigurableComposterBlock && composterBlockState.get(ConfigurableComposterBlock.LEVEL) < 7) {
                    if (dir == Direction.UP) {
                        return inventory.get(slot).isEmpty();
                    } else if (dir != Direction.DOWN) {
                        int modifierIndex = composterBlockState.get(ConfigurableComposterBlock.MODIFIER);
                        int entryIndex = entry.getModifierIndex();
                        return inventory.get(slot).isEmpty() && entry.hasModifierIndex() && modifierIndex != entryIndex && ComposterHandler.getPrioritizedModifier(modifierIndex, entryIndex).getIndex() == entryIndex;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        if (this.world != null && !this.world.isClient) {
            BlockState composterBlockState = world.getBlockState(pos);
            if (composterBlockState.getBlock() instanceof ConfigurableComposterBlock && composterBlockState.get(ConfigurableComposterBlock.LEVEL) == 8) {
                return Arrays.stream(RESULT_SLOTS).anyMatch(i -> i == slot) && (dir == Direction.DOWN);
            }
        }
        return false;
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(this.inventory, slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
    }

    @Override
    public void clear() {
        this.inventory.clear();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false;
    }

    @Override
    protected Text getContainerName() {
        return null;
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return null;
    }
}
