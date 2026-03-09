package vitosos.sapphireweapons.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import vitosos.sapphireweapons.registry.ModBlockEntities;

public class ItemBoxBlockEntity extends BlockEntity {

    public ItemBoxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ITEM_BOX_BLOCK_ENTITY, pos, state);
    }
}