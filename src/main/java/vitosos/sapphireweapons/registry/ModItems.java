package vitosos.sapphireweapons.registry;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import vitosos.sapphireweapons.SapphireStarArmaments;
import vitosos.sapphireweapons.item.*;
import net.minecraft.util.Rarity;

import static software.bernie.example.registry.ItemRegistry.registerItem;

public class ModItems {

    // --- POTIONS ---
    public static final Item HUNTER_POTION = new PercentagePotionItem(new FabricItemSettings().maxCount(10), 0.15f, 40, false, "hunter_drink");
    public static final Item MEGA_POTION = new PercentagePotionItem(new FabricItemSettings().maxCount(10), 0.30f, 40, false, "hunter_drink");

    public static final Item MAX_POTION = new PercentagePotionItem(new FabricItemSettings().maxCount(2), 1.0f, 20, false, "hunter_consume");
    public static final Item ANCIENT_POTION = new PercentagePotionItem(new FabricItemSettings().maxCount(1), 1.0f, 20, true, "hunter_consume");

    // --- POWDERS ---
    public static final Item DEMON_POWDER = new AreaPowderItem(new FabricItemSettings().maxCount(3), 0.0f, "demon");
    public static final Item HARDSHELL_POWDER = new AreaPowderItem(new FabricItemSettings().maxCount(3), 0.0f, "hardshell");
    public static final Item LIFEPOWDER = new AreaPowderItem(new FabricItemSettings().maxCount(3), 0.10f, "life");
    public static final Item DUST_OF_LIFE = new AreaPowderItem(new FabricItemSettings().maxCount(2), 0.20f, "dust_of_life");

    // --- GLAIVES ---
    public static final InsectGlaiveItem IRON_GLAIVE = new InsectGlaiveItem(ToolMaterials.IRON, 1, -2.4f, new Item.Settings());
    public static final InsectGlaiveItem DIAMOND_GLAIVE = new InsectGlaiveItem(ToolMaterials.DIAMOND, 3, -2.0f, new Item.Settings());
    public static final InsectGlaiveItem NETHERITE_GLAIVE = new InsectGlaiveItem(ToolMaterials.NETHERITE, 4, -2.0f, new Item.Settings().fireproof());
    public static final InsectGlaiveItem PHANTOM_GLAIVE = new InsectGlaiveItem(ToolMaterials.NETHERITE, 2, -1.8f, new Item.Settings().fireproof());
    public static final InsectGlaiveItem SPIDER_GLAIVE = new SpiderGlaiveItem(ToolMaterials.NETHERITE, 2, -2.0f, new Item.Settings().fireproof());
    public static final InsectGlaiveItem BLAZE_GLAIVE = new BlazeGlaiveItem(ToolMaterials.NETHERITE, 4, -2.0f, new Item.Settings().fireproof());

    // --- KINSECT ---
    public static final KinsectItem KINSECT_ITEM = new KinsectItem(new Item.Settings().maxCount(1), 4.0f);

    // Mats

    // --- SPIDER MATERIALS ---
    public static final Item PRIMAL_GLAND = new MonsterMaterialItem(new Item.Settings(), 10);
    public static final Item ARACHNID_HIDE = new MonsterMaterialItem(new Item.Settings(), 15);
    public static final Item PRESERVED_EYE = new MonsterMaterialItem(new Item.Settings().rarity(Rarity.RARE), 80);

    // --- PHANTOM MATERIALS ---
    public static final Item SPECTRAL_SPINE = new MonsterMaterialItem(new Item.Settings(), 15);
    public static final Item GHOSTLY_WINGARM = new MonsterMaterialItem(new Item.Settings().rarity(Rarity.UNCOMMON), 25);
    public static final Item DREAM_CORE = new MonsterMaterialItem(new Item.Settings().rarity(Rarity.RARE), 100);

    // --- BLAZE MATERIALS ---
    public static final Item WARM_ASH = new MonsterMaterialItem(new Item.Settings(), 20);
    public static final Item IGNITION_ROD = new MonsterMaterialItem(new Item.Settings().rarity(Rarity.UNCOMMON), 30);
    public static final Item FLAME_SOUL = new MonsterMaterialItem(new Item.Settings().rarity(Rarity.RARE), 120);

    public static void register() {
        // Register Items

        // Potions
        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "hunter_potion"), HUNTER_POTION);
        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "mega_potion"), MEGA_POTION);
        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "max_potion"), MAX_POTION);
        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "ancient_potion"), ANCIENT_POTION);

        // Powders
        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "demon_powder"), DEMON_POWDER);
        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "hardshell_powder"), HARDSHELL_POWDER);
        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "lifepowder"), LIFEPOWDER);
        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "dust_of_life"), DUST_OF_LIFE);

        // Glaive
        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "iron_glaive"), IRON_GLAIVE);
        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "diamond_glaive"), DIAMOND_GLAIVE);
        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "netherite_glaive"), NETHERITE_GLAIVE);
        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "phantom_glaive"), PHANTOM_GLAIVE);
        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "spider_glaive"), SPIDER_GLAIVE);
        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "blaze_glaive"), BLAZE_GLAIVE);

        // Kinsect
        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "kinsect"), KINSECT_ITEM);

        // Mats
        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "primal_gland"), PRIMAL_GLAND);
        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "arachnid_hide"), ARACHNID_HIDE);
        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "preserved_eye"), PRESERVED_EYE);

        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "spectral_spine"), SPECTRAL_SPINE);
        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "ghostly_wingarm"), GHOSTLY_WINGARM);
        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "dream_core"), DREAM_CORE);

        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "warm_ash"), WARM_ASH);
        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "ignition_rod"), IGNITION_ROD);
        Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "flame_soul"), FLAME_SOUL);
    }
}