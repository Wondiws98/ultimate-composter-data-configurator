package net.wondiws98.ultimatecomposterdataconfigurator.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.Instrument;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.wondiws98.ultimatecomposterdataconfigurator.UltimateComposterDataConfigurator;

public class UCDCBlocks {
    public static final Block CONFIGURABLE_COMPOSTER = Registry.register(Registries.BLOCK, new Identifier(UltimateComposterDataConfigurator.MOD_ID, "composter"), new ConfigurableComposterBlock(AbstractBlock.Settings.create().mapColor(MapColor.OAK_TAN).instrument(Instrument.BASS).strength(0.6f).sounds(BlockSoundGroup.WOOD).burnable()));

    public static void registerBlocks() {
    }
}

