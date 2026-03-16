package vitosos.sapphireweapons.util;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;
import vitosos.sapphireweapons.registry.ModItems;

public class ModLootTableModifiers {

    // Target the specific vanilla mob loot tables
    private static final Identifier SPIDER_ID = new Identifier("minecraft", "entities/spider");
    private static final Identifier PHANTOM_ID = new Identifier("minecraft", "entities/phantom");
    private static final Identifier BLAZE_ID = new Identifier("minecraft", "entities/blaze");

    public static void modifyLootTables() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {

            // --- SPIDER LOOT ---
            if (SPIDER_ID.equals(id)) {
                tableBuilder.pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.40f))
                        .with(ItemEntry.builder(ModItems.ARACHNID_HIDE))
                        .build());

                tableBuilder.pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.15f))
                        .with(ItemEntry.builder(ModItems.PRIMAL_GLAND))
                        .build());

                tableBuilder.pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.03f))
                        .with(ItemEntry.builder(ModItems.PRESERVED_EYE))
                        .build());
            }

            // --- PHANTOM LOOT ---
            if (PHANTOM_ID.equals(id)) {
                tableBuilder.pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.40f))
                        .with(ItemEntry.builder(ModItems.SPECTRAL_SPINE))
                        .build());

                tableBuilder.pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.15f))
                        .with(ItemEntry.builder(ModItems.GHOSTLY_WINGARM))
                        .build());

                tableBuilder.pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.03f))
                        .with(ItemEntry.builder(ModItems.DREAM_CORE))
                        .build());
            }

            // --- BLAZE LOOT ---
            if (BLAZE_ID.equals(id)) {
                tableBuilder.pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.40f))
                        .with(ItemEntry.builder(ModItems.WARM_ASH))
                        .build());

                tableBuilder.pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.15f))
                        .with(ItemEntry.builder(ModItems.IGNITION_ROD))
                        .build());

                tableBuilder.pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.03f))
                        .with(ItemEntry.builder(ModItems.FLAME_SOUL))
                        .build());
            }
        });
    }
}