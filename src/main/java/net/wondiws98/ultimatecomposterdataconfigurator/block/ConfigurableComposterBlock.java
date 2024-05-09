package net.wondiws98.ultimatecomposterdataconfigurator.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;
import net.minecraft.world.event.GameEvent;
import net.wondiws98.ultimatecomposterdataconfigurator.block.entity.ConfigurableComposterBlockEntity;
import net.wondiws98.ultimatecomposterdataconfigurator.block.entity.UCDCBlockEntities;
import net.wondiws98.ultimatecomposterdataconfigurator.data.server.Entry;
import net.wondiws98.ultimatecomposterdataconfigurator.data.server.Modifier;
import net.wondiws98.ultimatecomposterdataconfigurator.handler.ComposterHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigurableComposterBlock extends BlockWithEntity implements BlockEntityProvider {
    public static final MapCodec<ConfigurableComposterBlock> CODEC = ConfigurableComposterBlock.createCodec(ConfigurableComposterBlock::new);
    public static final IntProperty LEVEL = Properties.LEVEL_8;
    public static final IntProperty MODIFIER = IntProperty.of("modifier", 0, 255);
    private static final VoxelShape RAYCAST_SHAPE = VoxelShapes.fullCube();
    private static final VoxelShape[] LEVEL_TO_COLLISION_SHAPE = Util.make(new VoxelShape[9], shapes -> {
        for (int i = 0; i < 8; ++i) {
            shapes[i] = VoxelShapes.combineAndSimplify(RAYCAST_SHAPE, Block.createCuboidShape(2.0, Math.max(2, 1 + i * 2), 2.0, 14.0, 16.0, 14.0), BooleanBiFunction.ONLY_FIRST);
        }
        shapes[8] = shapes[7];
    });

    public MapCodec<ConfigurableComposterBlock> getCodec() {
        return CODEC;
    }

    public ConfigurableComposterBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(LEVEL, 0).with(MODIFIER, 0));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ConfigurableComposterBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return LEVEL_TO_COLLISION_SHAPE[state.get(LEVEL)];
    }

    @Override
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return RAYCAST_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return LEVEL_TO_COLLISION_SHAPE[0];
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.isOf(newState.getBlock())) {
            return;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ConfigurableComposterBlockEntity) {
            if (world instanceof ServerWorld) {
                ItemScatterer.spawn(world, pos, (Inventory)((ConfigurableComposterBlockEntity)blockEntity));
            }
            super.onStateReplaced(state, world, pos, newState, moved);
            world.updateComparators(pos, this);
        } else {
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        int currentLevel = state.get(LEVEL);
        ItemStack itemStack = player.getStackInHand(hand);
        Entry entry = ComposterHandler.getStackEntry(itemStack);
        if (currentLevel < 8 && entry != null) {
            if (currentLevel < 7 && !world.isClient) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof ConfigurableComposterBlockEntity entity) {
                    Pair<Integer, Integer> calculatedConsumption = ComposterHandler.calculateConsumption(state, entry, itemStack);
                    int levelIncreased = calculatedConsumption.getLeft();
                    int itemConsumed = calculatedConsumption.getRight();
                    if (entity.insertInventory(itemStack, itemConsumed)) {
                        ConfigurableComposterBlock.setComposterState(player, state, world, pos, levelIncreased, entry.getModifierIndex());
                        world.syncWorldEvent(WorldEvents.COMPOSTER_USED, pos, levelIncreased > 0 ? 1 : 0);
                        player.increaseStat(Stats.USED.getOrCreateStat(itemStack.getItem()), itemConsumed);
                        if (!player.getAbilities().creativeMode) {
                            itemStack.decrement(itemConsumed);
                        }
                    }
                }
            }
            return ActionResult.success(world.isClient);
        }
        if (currentLevel == 8) {
            ConfigurableComposterBlock.emptyFullComposter(player, state, world, pos);
            return ActionResult.success(world.isClient);
        }
        return ActionResult.PASS;
    }

    public static BlockState setComposterState(@Nullable Entity user, @NotNull BlockState state, @NotNull WorldAccess world, BlockPos pos, int levelIncrease, int modifierIndex) {
        int newLevel = Math.min(state.get(LEVEL)+levelIncrease, 7);
        Modifier prioritizedModifier = ComposterHandler.getPrioritizedModifier(state.get(MODIFIER), modifierIndex);
        BlockState blockState = state.with(LEVEL, newLevel).with(MODIFIER, prioritizedModifier.getIndex());
        world.setBlockState(pos, blockState, Block.NOTIFY_ALL);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(user, blockState));
        return blockState;
    }

    public static void setComposterDone(BlockState state, WorldAccess world, BlockPos pos) {
        BlockState blockState = state.with(LEVEL, 8);
        world.setBlockState(pos, blockState, Block.NOTIFY_ALL);
        world.playSound(null, pos, SoundEvents.BLOCK_COMPOSTER_READY, SoundCategory.BLOCKS, 1.0f, 1.0f);
    }

    public static void emptyFullComposter(Entity user, BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null && blockEntity instanceof ConfigurableComposterBlockEntity entity) {
            if (!world.isClient) {
                ItemScatterer.spawn(world, pos, entity);
                entity.clear();
            }
            ConfigurableComposterBlock.emptyComposter(user, state, world, pos);
            world.playSound(null, pos, SoundEvents.BLOCK_COMPOSTER_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
    }

    public static void emptyComposter(@Nullable Entity user, BlockState state, WorldAccess world, BlockPos pos) {
        BlockState blockState = state.with(LEVEL, 0).with(MODIFIER, 0);
        world.setBlockState(pos, blockState, Block.NOTIFY_ALL);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(user, blockState));
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return state.get(LEVEL);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(MODIFIER);
        builder.add(LEVEL);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }


    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return new ItemStack(Items.COMPOSTER);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient() ? null: validateTicker(type, UCDCBlockEntities.COMPOSTER_BLOCK_ENTITY_BLOCK_ENTITY, (ConfigurableComposterBlockEntity::tick));
    }
}
