package net.wondiws98.ultimatecomposterdataconfigurator.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComposterBlock;
import net.minecraft.state.StateManager;
import net.wondiws98.ultimatecomposterdataconfigurator.block.UCDCBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ComposterBlock.class)
public class ComposterBlockMixin extends Block {
	public ComposterBlockMixin(Settings settings) {
		super(settings);
	}

	/**
	 * @author Wondiws98
	 * @reason Funny way of replacing a registered block's instance!
	 */
	@Inject(method = "<init>", at = @At("TAIL"))
	private void constructorInject(CallbackInfo ignoredInfo) {
		this.setDefaultState(UCDCBlocks.CONFIGURABLE_COMPOSTER.getDefaultState());
	}

	/**
	 * @author Wondiws98
	 * @reason Funny way of replacing a registered block's instance!
	 */
	@Override
	public StateManager<Block, BlockState> getStateManager() {
		return UCDCBlocks.CONFIGURABLE_COMPOSTER.getStateManager();
	}
}