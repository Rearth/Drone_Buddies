package rearth.blocks.controller;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import rearth.init.BlockEntitiesContent;

public class ControllerBlock extends BlockWithEntity {
    
    public ControllerBlock(Settings settings) {
        super(settings);
    }
    
    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }
    
    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
    
    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ControllerBlockEntity(pos, state);
    }
    
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return (world1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof ControllerBlockEntity ticker)
                ticker.tick(world1, pos, state1, ticker);
        };
    }
    
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        
        if (!world.isClient) {
            var candidate = world.getBlockEntity(pos, BlockEntitiesContent.ASSEMBLER_CONTROLLER.get());
            candidate.ifPresent(controllerBlockEntity -> controllerBlockEntity.onUse(state, player));
        }
        
        return super.onUse(state, world, pos, player, hit);
    }
}
