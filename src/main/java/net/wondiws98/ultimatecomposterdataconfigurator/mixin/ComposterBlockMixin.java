package net.wondiws98.ultimatecomposterdataconfigurator.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.wondiws98.ultimatecomposterdataconfigurator.data.server.UCDCDataLoader;
import net.wondiws98.ultimatecomposterdataconfigurator.data.server.CompostableEntry;
import net.wondiws98.ultimatecomposterdataconfigurator.handler.ComposterHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ComposterBlock.class)
public class ComposterBlockMixin extends Block implements InventoryProvider {
	@Shadow
	public static final IntProperty LEVEL = Properties.LEVEL_8;
	private static final IntProperty MODIFIER = ComposterHandler.MODIFIER;
	public ComposterBlockMixin(Settings settings) {
		super(settings);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void constructorInjection(CallbackInfo info) {
		this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(MODIFIER, 0));
	}

	@Overwrite
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		if (state.get(LEVEL) == 7) {
			world.scheduleBlockTick(pos, state.getBlock(), UCDCDataLoader.getModifier(state.get(MODIFIER)).getRandomTime());
		}
	}

	/**
	 * @author Wondiws98
	 * @reason So the IDE can shut up about it!
	 */
	@Overwrite
	public ActionResult onUse(@NotNull BlockState state, World world, BlockPos pos, @NotNull PlayerEntity player, Hand hand, BlockHitResult hit) {
		int currentLevel = state.get(LEVEL);
		ItemStack itemStack = player.getStackInHand(hand);
		CompostableEntry compostableEntry = UCDCDataLoader.getEntryFor(itemStack.getItem());
		if (currentLevel < 8 && compostableEntry != null) {
			if (currentLevel < 7 && !world.isClient) {
				Pair<Integer, Integer> calculatedConsumption = ComposterHandler.calculateConsumption(state, world.getRandom(), compostableEntry, itemStack);
				int levelIncreased = calculatedConsumption.getLeft();
				int itemConsumed = calculatedConsumption.getRight();
				int modifierIndex = UCDCDataLoader.getItemModifierIndex(itemStack.getItem());
				ComposterHandler.setComposterState(player, state, world, pos, levelIncreased, modifierIndex);
				world.syncWorldEvent(WorldEvents.COMPOSTER_USED, pos, levelIncreased>0 ? 1 : 0);
				player.increaseStat(Stats.USED.getOrCreateStat(itemStack.getItem()), itemConsumed);
				if (!player.getAbilities().creativeMode) {
					itemStack.decrement(itemConsumed);
				}
			}
			return ActionResult.success(world.isClient);
		}
		if (currentLevel == 8) {
			ComposterHandler.emptyFullComposter(player, state, world, pos);
			return ActionResult.success(world.isClient);
		}
		return ActionResult.PASS;
	}

	@Inject(method = "appendProperties", at = @At("HEAD"))
	private void appendPropertiesInjection(CallbackInfo info, @Local StateManager.Builder<Block, BlockState> builder) {
		builder.add(MODIFIER);
	}

	/**
	 * @author Wondiws98
	 * @reason So the IDE can shut up about it!
	 */
    @Overwrite
	public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos) {
		int i = state.get(LEVEL);
		if (i == 8) {
			return new ComposterHandler.FullComposterInventory(state, world, pos, ComposterHandler.getModifiersItemStack(state.get(MODIFIER)));
		}
		if (i < 7) {
			return new ComposterHandler.ComposterInventory(state, world, pos);
		}
		return new ComposterHandler.DummyInventory();
	}
}