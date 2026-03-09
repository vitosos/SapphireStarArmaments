package vitosos.sapphireweapons.recipe;

import net.minecraft.item.Items;
import vitosos.sapphireweapons.registry.ModItems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForgeRecipeRegistry {

    // This Map holds lists of recipes, sorted by their Category!
    public static final Map<WeaponCategory, List<ForgeRecipe>> RECIPES = new HashMap<>();

    public static void registerRecipes() {
        List<ForgeRecipe> glaives = new ArrayList<>();

        // --- IRON GLAIVE ---
        glaives.add(new ForgeRecipe(
                ModItems.IRON_GLAIVE,
                "Iron Glaive",
                Items.IRON_INGOT,
                50,
                Map.of(
                        Items.IRON_INGOT, 3,
                        Items.STICK, 3,
                        Items.COPPER_INGOT, 2
                )
        ));

        // --- DIAMOND GLAIVE ---
        glaives.add(new ForgeRecipe(
                ModItems.DIAMOND_GLAIVE,
                "Diamond Glaive",
                Items.DIAMOND,
                60,
                Map.of(
                        ModItems.IRON_GLAIVE, 1,
                        Items.DIAMOND, 4,
                        Items.GOLD_INGOT, 3
                )
        ));

        // --- NETHERITE GLAIVE ---
        glaives.add(new ForgeRecipe(
                ModItems.NETHERITE_GLAIVE,
                "Netherite Glaive",
                Items.NETHERITE_INGOT,
                80,
                Map.of(
                        ModItems.DIAMOND_GLAIVE, 1,
                        Items.NETHERITE_INGOT, 2,
                        Items.BLAZE_POWDER, 2
                )
        ));

        // --- PHANTOM GLAIVE ---
        glaives.add(new ForgeRecipe(
                ModItems.PHANTOM_GLAIVE,
                "Phantom Glaive",
                Items.PHANTOM_MEMBRANE, // Display Icon
                80,
                Map.of(
                        ModItems.IRON_GLAIVE, 1,
                        ModItems.SPECTRAL_SPINE, 2,
                        ModItems.GHOSTLY_WINGARM, 3,
                        ModItems.DREAM_CORE, 1
                )
        ));

        // --- SPIDER GLAIVE ---
        glaives.add(new ForgeRecipe(
                ModItems.SPIDER_GLAIVE,
                "Spider Glaive",
                Items.SPIDER_EYE, // Display Icon
                80,
                Map.of(
                        ModItems.IRON_GLAIVE, 1,
                        ModItems.ARACHNID_HIDE, 5,
                        ModItems.PRIMAL_GLAND, 2,
                        ModItems.PRESERVED_EYE, 1
                )
        ));

        // --- BLAZE GLAIVE ---
        glaives.add(new ForgeRecipe(
                ModItems.BLAZE_GLAIVE,
                "Blaze Glaive",
                Items.BLAZE_POWDER, // Display Icon
                120,
                Map.of(
                        ModItems.NETHERITE_GLAIVE, 1,
                        ModItems.WARM_ASH, 5,
                        ModItems.IGNITION_ROD, 2,
                        ModItems.FLAME_SOUL, 1
                )
        ));

        RECIPES.put(WeaponCategory.GLAIVE, glaives);
    }
}