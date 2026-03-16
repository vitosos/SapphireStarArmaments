package vitosos.sapphireweapons;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.GeckoLib;
import vitosos.sapphireweapons.config.SapphireConfig;
import vitosos.sapphireweapons.config.SapphireConfigManager;
import vitosos.sapphireweapons.network.ServerNetworking;
import vitosos.sapphireweapons.recipe.ForgeRecipeRegistry;
import vitosos.sapphireweapons.registry.*;
import vitosos.sapphireweapons.util.ISapphirePlayerData;
import vitosos.sapphireweapons.util.ModLootTableModifiers;
import vitosos.sapphireweapons.util.ShopRegistry;

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
		ShopRegistry.registerShopEntries();
		SapphireConfigManager.register();

		// Mob Loot Tables
		ModLootTableModifiers.modifyLootTables();

		// Load Networking
		ServerNetworking.registerReceivers();

		// --- 1. COPY DATA ACROSS DEATH & DIMENSIONS ---
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			ISapphirePlayerData oldData = (ISapphirePlayerData) oldPlayer;
			ISapphirePlayerData newData = (ISapphirePlayerData) newPlayer;

			// A. Copy the Item Box perfectly
			for (int i = 0; i < oldData.getBoxInventory().size(); i++) {
				newData.getBoxInventory().setStack(i, oldData.getBoxInventory().getStack(i).copy());
			}

			// B. Copy the Cantine Timer
			newData.setLastCantineTime(oldData.getLastCantineTime());

			// C. Handle Sapphire Points & Death Tax
			int oldPoints = oldData.getSapphirePoints();
			if (alive) {
				// "alive" is true if they are just changing dimensions (like leaving The End)
				newData.setSapphirePoints(oldPoints);
			} else {
				// They died! Deduct 10% of their points.
				double taxRate = vitosos.sapphireweapons.config.SapphireConfigManager.CONFIG.deathTaxPercentage;
				int pointsToKeep = oldPoints - (int)(oldPoints * taxRate);
				newData.setSapphirePoints(pointsToKeep);
			}
		});

		// --- 2. SYNC THE NEW DATA TO THE CLIENT AFTER RESPAWN ---
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			ISapphirePlayerData newData = (ISapphirePlayerData) newPlayer;

			// Sync Points (So the UI shows the new 10% deducted balance!)
			PacketByteBuf ptsBuf = PacketByteBufs.create();
			ptsBuf.writeInt(newData.getSapphirePoints());
			ServerPlayNetworking.send(newPlayer, ServerNetworking.SYNC_POINTS_PACKET, ptsBuf);

			// Sync Cantine Timer
			PacketByteBuf cantineBuf = PacketByteBufs.create();
			cantineBuf.writeLong(newData.getLastCantineTime());
			ServerPlayNetworking.send(newPlayer, ServerNetworking.SYNC_CANTINE_PACKET, cantineBuf);
		});
	}
}