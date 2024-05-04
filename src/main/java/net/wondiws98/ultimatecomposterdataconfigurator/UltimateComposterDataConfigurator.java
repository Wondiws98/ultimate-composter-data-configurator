package net.wondiws98.ultimatecomposterdataconfigurator;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.wondiws98.ultimatecomposterdataconfigurator.data.server.UCDCDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UltimateComposterDataConfigurator implements ModInitializer {
	public static final String MOD_ID = "ultimate_composter_data_configurator";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(UCDCDataLoader.INSTANCE);
		LOGGER.info("Ultimate Composter Data Configurator in!");
	}
}