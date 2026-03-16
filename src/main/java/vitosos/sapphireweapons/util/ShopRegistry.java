package vitosos.sapphireweapons.util;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import vitosos.sapphireweapons.registry.ModItems;
import vitosos.sapphireweapons.util.ShopCategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopRegistry {

    // Maps each ShopCategory (tab) to a list of its available entries
    public static final Map<ShopCategory, List<ShopEntry>> SHOP_ITEMS = new HashMap<>();

    public static void registerShopEntries() {

        // --- 1. CONSUMABLES TAB ---
        List<ShopEntry> consumables = new ArrayList<>();

        // Potions
        consumables.add(new ShopEntry("buy_hunter_potion", new ItemStack(ModItems.HUNTER_POTION), "Hunter Potion", 5));
        consumables.add(new ShopEntry("buy_mega_potion", new ItemStack(ModItems.MEGA_POTION), "Mega Potion", 20));
        consumables.add(new ShopEntry("buy_max_potion", new ItemStack(ModItems.MAX_POTION), "Max Potion", 80));
        consumables.add(new ShopEntry("buy_ancient_potion", new ItemStack(ModItems.ANCIENT_POTION), "Ancient Potion", 150));

        // Powders
        consumables.add(new ShopEntry("buy_lifepowder", new ItemStack(ModItems.LIFEPOWDER), "Lifepowder", 25));
        consumables.add(new ShopEntry("buy_dust_of_life", new ItemStack(ModItems.DUST_OF_LIFE), "Dust of Life", 60));
        consumables.add(new ShopEntry("buy_demon_powder", new ItemStack(ModItems.DEMON_POWDER), "Demon Powder", 120));
        consumables.add(new ShopEntry("buy_hardshell_powder", new ItemStack(ModItems.HARDSHELL_POWDER), "Hardshell Powder", 120));

        SHOP_ITEMS.put(ShopCategory.CONSUMABLES, consumables);

        // --- 2. EXPERIENCE TAB (Example) ---
        List<ShopEntry> experience = new ArrayList<>();

        // Instead of a ModItem, we use a Vanilla Experience Bottle as the icon!
        experience.add(new ShopEntry("buy_xp_small", new ItemStack(Items.EXPERIENCE_BOTTLE), "Small EXP Bundle (5 Levels)", 800));
        experience.add(new ShopEntry("buy_xp_large", new ItemStack(Items.EXPERIENCE_BOTTLE), "Large EXP Bundle (15 Levels)", 2000));

        SHOP_ITEMS.put(ShopCategory.EXPERIENCE, experience);

        // --- 3. MAINTENANCE TAB (Example) ---
        List<ShopEntry> maintenance = new ArrayList<>();
        maintenance.add(new ShopEntry("repair_main_hand", new ItemStack(Items.ANVIL), "Repair Held Weapon", 150));

        SHOP_ITEMS.put(ShopCategory.MAINTENANCE, maintenance);
    }

    // Helper method to safely get the list for the UI
    public static List<ShopEntry> getEntriesForCategory(ShopCategory category) {
        return SHOP_ITEMS.getOrDefault(category, new ArrayList<>());
    }
}