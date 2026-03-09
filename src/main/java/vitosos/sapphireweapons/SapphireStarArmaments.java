package vitosos.sapphireweapons;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.GeckoLib;
import vitosos.sapphireweapons.network.ServerNetworking;
import vitosos.sapphireweapons.recipe.ForgeRecipeRegistry;
import vitosos.sapphireweapons.registry.*;
import vitosos.sapphireweapons.util.ModLootTableModifiers;

public class SapphireStarArmaments implements ModInitializer {

	public static final String MOD_ID = "sapphire-star-armaments";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Forging the Sapphire Star Armaments...");

		GeckoLib.initialize();

		// Load Registries
		ModItems.register();
		ModItemGroups.register();
		ModEntities.register();
		ModBlocks.register();
		ModSounds.register();
		ModBlockEntities.register();
		ModScreenHandlers.register();
		ModCommands.register();
		ForgeRecipeRegistry.registerRecipes();

		// Mob Loot Tables
		ModLootTableModifiers.modifyLootTables();

		// Load Networking
		ServerNetworking.registerReceivers();
	}
}