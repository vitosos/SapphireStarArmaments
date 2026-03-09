package vitosos.sapphireweapons.block;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import vitosos.sapphireweapons.registry.ModSounds;

// 1. Extend BlockWithEntity instead of Block
public class ItemBoxBlock extends BlockWithEntity {

    // 1. Define the facing property (Horizontal only, so it can't point up or down)
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public ItemBoxBlock(Settings settings) {
        super(settings);
        // 2. Set the default state to North so the game doesn't crash on load
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    // 3. Register the property to the block
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    // 4. Determine the direction when the player places the block
    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        // .getOpposite() makes the "front" of the block face the player!
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    // 2. We MUST tell Minecraft to render this as a standard block, otherwise it becomes invisible!
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    // 3. Link the Block to the BlockEntity
    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ItemBoxBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            world.playSound(null, pos, ModSounds.ITEM_BOX_OPEN, SoundCategory.BLOCKS, 1.0f, 1.0f);

            // Cast the player to your interface to access their personal storage!
            vitosos.sapphireweapons.util.ISapphirePlayerData playerData = (vitosos.sapphireweapons.util.ISapphirePlayerData) player;

            // Open the UI using the player's personal inventory
            player.openHandledScreen(new net.minecraft.screen.SimpleNamedScreenHandlerFactory((syncId, inv, p) ->
                    new vitosos.sapphireweapons.screen.ItemBoxScreenHandler(syncId, inv, playerData.getBoxInventory()),
                    net.minecraft.text.Text.translatable("container.sapphire-star-armaments.item_box")));
        }
        return ActionResult.SUCCESS;
    }
}