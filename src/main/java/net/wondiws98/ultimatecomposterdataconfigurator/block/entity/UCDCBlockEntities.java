package net.wondiws98.ultimatecomposterdataconfigurator.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.wondiws98.ultimatecomposterdataconfigurator.UltimateComposterDataConfigurator;
import net.wondiws98.ultimatecomposterdataconfigurator.block.UCDCBlocks;

public class UCDCBlockEntities<T extends BlockEntity> {
    public static final BlockEntityType<ConfigurableComposterBlockEntity> COMPOSTER_BLOCK_ENTITY_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(UltimateComposterDataConfigurator.MOD_ID, "composter_block_entity_block_entity"),
            FabricBlockEntityTypeBuilder.create(ConfigurableComposterBlockEntity::new, UCDCBlocks.CONFIGURABLE_COMPOSTER).build()
    );
    public static void registerBlockEntities() {}
}