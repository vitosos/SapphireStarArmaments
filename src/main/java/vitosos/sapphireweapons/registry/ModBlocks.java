package vitosos.sapphireweapons.registry;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import vitosos.sapphireweapons.SapphireStarArmaments;
import vitosos.sapphireweapons.block.GuildStockBoxBlock;
import vitosos.sapphireweapons.block.ItemBoxBlock;

public class ModBlocks {

    // --- BLOCKS ---
    public static final Block ITEM_BOX = registerBlock("item_box",
            new ItemBoxBlock(FabricBlockSettings.create()
                    .strength(0.5f, 3600000.0f)
                    .sounds(BlockSoundGroup.WOOD))); // Standard chest wood sounds for breaking/walking

    public static final Block GUILD_STOCK_BOX = registerBlock("guild_stock_box",
            new GuildStockBoxBlock(net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings.create()
                    .nonOpaque()
                    .strength(0.5f, 3600000.0f)));

    public static final Block FORGE = registerBlock("forge",
            new vitosos.sapphireweapons.block.ForgeBlock(net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings.create()
                    .mapColor(net.minecraft.block.MapColor.STONE_GRAY)
                    .requiresTool() // Requires a Pickaxe!
                    .strength(3.5f, 17.0f)
                    // Dynamically set light! 13 for Foundry, 0 for Anvil
                    .luminance(state -> state.get(vitosos.sapphireweapons.block.ForgeBlock.IS_FOUNDRY) ? 13 : 0)
                    .nonOpaque()));

    // --- HELPER METHODS ---
    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(SapphireStarArmaments.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    public static void register() {
        // Forces the class to load
    }
}