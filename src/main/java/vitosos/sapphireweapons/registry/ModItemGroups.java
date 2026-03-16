package vitosos.sapphireweapons.registry;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import vitosos.sapphireweapons.SapphireStarArmaments;

public class ModItemGroups {

    // --- 1. WORKSHOP WEAPONS TAB ---
    public static final ItemGroup SAPPHIRE_WORKSHOP = Registry.register(Registries.ITEM_GROUP,
            new Identifier(SapphireStarArmaments.MOD_ID, "sapphire_workshop"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.sapphire-star-armaments.sapphire_workshop"))
                    .icon(() -> new ItemStack(ModItems.BLAZE_GLAIVE)) // <-- Swap this for a dummy item if you make one!
                    .entries((displayContext, entries) -> {
                        // The order you add them here is the exact order they appear in the tab!
                        entries.add(ModItems.IRON_GLAIVE);
                        entries.add(ModItems.DIAMOND_GLAIVE);
                        entries.add(ModItems.NETHERITE_GLAIVE);
                        entries.add(ModItems.PHANTOM_GLAIVE);
                        entries.add(ModItems.SPIDER_GLAIVE);
                        entries.add(ModItems.BLAZE_GLAIVE);
                        entries.add(ModItems.KINSECT_ITEM);
                    }).build());

    // --- 2. MOB MATERIALS TAB ---
    public static final ItemGroup MOB_MATERIALS = Registry.register(Registries.ITEM_GROUP,
            new Identifier(SapphireStarArmaments.MOD_ID, "mob_materials"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.sapphire-star-armaments.mob_materials"))
                    .icon(() -> new ItemStack(ModItems.DREAM_CORE))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.PRIMAL_GLAND);
                        entries.add(ModItems.ARACHNID_HIDE);
                        entries.add(ModItems.PRESERVED_EYE);

                        entries.add(ModItems.SPECTRAL_SPINE);
                        entries.add(ModItems.GHOSTLY_WINGARM);
                        entries.add(ModItems.DREAM_CORE);

                        entries.add(ModItems.WARM_ASH);
                        entries.add(ModItems.IGNITION_ROD);
                        entries.add(ModItems.FLAME_SOUL);
                    }).build());

    // --- 3. TOOLS AND CONSUMABLES TAB ---
    public static final ItemGroup TOOLS_AND_CONSUMABLES = Registry.register(Registries.ITEM_GROUP,
            new Identifier(SapphireStarArmaments.MOD_ID, "tools_and_consumables"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.sapphire-star-armaments.tools_and_consumables"))
                    .icon(() -> new ItemStack(ModBlocks.FORGE)) // Using the Forge as the tab's icon!
                    .entries((displayContext, entries) -> {
                        entries.add(ModBlocks.FORGE);
                        entries.add(ModBlocks.ITEM_BOX);
                        entries.add(ModBlocks.GUILD_STOCK_BOX);
                        entries.add(ModItems.HUNTER_POTION);
                        entries.add(ModItems.MEGA_POTION);
                        entries.add(ModItems.MAX_POTION);
                        entries.add(ModItems.ANCIENT_POTION);
                        entries.add(ModItems.LIFEPOWDER);
                        entries.add(ModItems.DUST_OF_LIFE);
                        entries.add(ModItems.DEMON_POWDER);
                        entries.add(ModItems.HARDSHELL_POWDER);


                    }).build());

    public static void register() {
        // Just calling this method forces the class to load and register everything!
    }
}