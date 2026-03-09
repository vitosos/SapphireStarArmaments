package vitosos.sapphireweapons.registry;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import vitosos.sapphireweapons.SapphireStarArmaments;
import vitosos.sapphireweapons.block.ItemBoxBlockEntity;

public class ModBlockEntities {

    public static final BlockEntityType<ItemBoxBlockEntity> ITEM_BOX_BLOCK_ENTITY =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(SapphireStarArmaments.MOD_ID, "item_box_be"),
                    FabricBlockEntityTypeBuilder.create(ItemBoxBlockEntity::new, ModBlocks.ITEM_BOX).build());

    public static void register() {
        // Forces the class to load
    }
}