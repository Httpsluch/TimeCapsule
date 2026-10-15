package io.github.httpsluch.timecapsule.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The capsule block, a chest-sized box facing the player who placed it. Logic lives in
 * {@link CapsuleBlockEntity}; the block only routes interactions to it. SEALED is a block state
 * so the model can show the padlock and glowing clock.
 * <p>
 * The facing property is used through {@link BlockStateProperties#HORIZONTAL_FACING} directly
 * because its Java type changed in 26.1.
 */
public class CapsuleBlock extends BaseEntityBlock {
    public static final BooleanProperty SEALED = BooleanProperty.create("sealed");
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);

    //? if >=1.21 && <26.3 {
    /*public static final com.mojang.serialization.MapCodec<CapsuleBlock> CODEC = simpleCodec(CapsuleBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    *///?}

    public CapsuleBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, net.minecraft.core.Direction.NORTH)
            .setValue(SEALED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, SEALED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CapsuleBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && placer instanceof ServerPlayer player
            && level.getBlockEntity(pos) instanceof CapsuleBlockEntity capsule) {
            capsule.onPlaced(player);
        }
    }

    /**
     * Anyone can break a capsule, but it drops as an item that keeps its items, owner and timer, so a
     * sealed capsule stays sealed. Going through getDrops covers players and modded block breakers alike.
     */
    @Override
    public java.util.List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder params) {
        if (params.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY) instanceof CapsuleBlockEntity capsule) {
            return java.util.List.of(capsule.toItem());
        }
        return super.getDrops(state, params);
    }

    // Creative mode gets no drops, so keep a capsule's data the way shulker boxes do.
    //? if >=1.21 {
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        dropForCreative(level, pos, player);
        return super.playerWillDestroy(level, pos, state, player);
    }
    //?} else {
    /*@Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        dropForCreative(level, pos, player);
        super.playerWillDestroy(level, pos, state, player);
    }
    *///?}

    private static void dropForCreative(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && player.isCreative() && level.getBlockEntity(pos) instanceof CapsuleBlockEntity capsule && capsule.hasDataToKeep()) {
            Block.popResource(level, pos, capsule.toItem());
        }
    }

    //? if >=1.21 {
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return interact(level, pos, player);
    }
    //?} else {
    /*@Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        return interact(level, pos, player);
    }
    *///?}

    private InteractionResult interact(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer
            && level.getBlockEntity(pos) instanceof CapsuleBlockEntity capsule) {
            capsule.onUse(serverPlayer);
        }
        //? if >=26.1 {
        return InteractionResult.SUCCESS;
        //?} else {
        /*return InteractionResult.sidedSuccess(level.isClientSide());
        *///?}
    }
}
