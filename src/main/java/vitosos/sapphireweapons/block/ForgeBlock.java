package vitosos.sapphireweapons.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

public class ForgeBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    // True = Foundry (Right side, emits particles). False = Anvil (Left side)
    public static final BooleanProperty IS_FOUNDRY = BooleanProperty.of("is_foundry");

    public ForgeBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(IS_FOUNDRY, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, IS_FOUNDRY);
    }

    // 1. Check if there is room for the 2nd block (Foundry) before placing the Anvil
    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction facing = ctx.getHorizontalPlayerFacing().getOpposite();
        BlockPos pos = ctx.getBlockPos();

        // Find the block to the "Right" of where the player is looking
        Direction rightDir = facing.rotateYCounterclockwise();

        // If the right block is empty/replaceable, allow placement!
        if (ctx.getWorld().getBlockState(pos.offset(rightDir)).canReplace(ctx)) {
            return this.getDefaultState().with(FACING, facing).with(IS_FOUNDRY, false);
        }
        return null; // Cancel placement if blocked
    }

    // 2. Automatically spawn the Foundry half when the Anvil is placed
    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient) {
            Direction facing = state.get(FACING);
            BlockPos rightPos = pos.offset(facing.rotateYCounterclockwise());
            world.setBlockState(rightPos, state.with(IS_FOUNDRY, true), 3);
        }
    }

    // 3. If you break one half, destroy the other half so it doesn't float!
    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        Direction facing = state.get(FACING);
        boolean isFoundry = state.get(IS_FOUNDRY);

        // Figure out where the "other" half is located
        Direction otherDir = isFoundry ? facing.rotateYClockwise() : facing.rotateYCounterclockwise();
        BlockPos otherPos = pos.offset(otherDir);

        BlockState otherState = world.getBlockState(otherPos);
        if (otherState.isOf(this) && otherState.get(IS_FOUNDRY) != isFoundry) {
            world.setBlockState(otherPos, Blocks.AIR.getDefaultState(), 3);
            world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, otherPos, Block.getRawIdFromState(otherState));
        }
        super.onBreak(world, pos, state, player);
    }

    // 4. Spawn Fire and Smoke purely on the Foundry half!
    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        // Only spawn particles on the Foundry half!
        if (state.get(IS_FOUNDRY)) {
            Direction facing = state.get(FACING);

            // Center of the block
            double x = pos.getX() + 0.5;
            double y = pos.getY();
            double z = pos.getZ() + 0.5;

            // --- 1. TOP PARTICLES (Coals & Smoke) ---
            // Spread them around the top surface (Y + 0.95)
            double topX = x + (random.nextDouble() - 0.5) * 0.5;
            double topY = y + 0.95;
            double topZ = z + (random.nextDouble() - 0.5) * 0.5;

            // Always spawn smoke
            world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, topX, topY, topZ, 0.0, 0.05, 0.0);
            // 33% chance to spawn a little flame popping out of the coals
            if (random.nextInt(3) == 0) {
                world.addParticle(ParticleTypes.FLAME, topX, topY, topZ, 0.0, 0.02, 0.0);
            }

            // --- 2. FRONT PARTICLES (Oven Flames) ---
            // Move to the very front face of the block
            double frontX = x + (facing.getOffsetX() * 0.55);
            double frontZ = z + (facing.getOffsetZ() * 0.55);
            double frontY = y + 0.65; // Move slightly less than halfway up

            // Calculate which way "Right" is, based on the direction the block is facing
            Direction rightDir = facing.rotateYClockwise();

            // Shift the particle to the right side of the opening
            frontX -= (rightDir.getOffsetX() * 0.2);
            frontZ -= (rightDir.getOffsetZ() * 0.2);

            // Spawn the oven flame!
            world.addParticle(ParticleTypes.FLAME, frontX, frontY, frontZ, 0.0, 0.0, 0.0);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return null; // We will wire up the BlockEntity later
    }

    @SuppressWarnings("deprecation")
    @Override
    public net.minecraft.util.ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, net.minecraft.util.Hand hand, net.minecraft.util.hit.BlockHitResult hit) {
        if (!world.isClient) {
            player.openHandledScreen(new net.minecraft.screen.SimpleNamedScreenHandlerFactory((syncId, inv, p) ->
                    new vitosos.sapphireweapons.screen.ForgeScreenHandler(syncId, inv),
                    net.minecraft.text.Text.translatable("container.sapphire-star-armaments.forge")));
        }
        return net.minecraft.util.ActionResult.SUCCESS;
    }
}