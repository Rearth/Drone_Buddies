package rearth.blocks.rotors;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class DrillBlock extends Block {
    public DrillBlock(Settings settings) {
        super(settings);
    }
    
    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return makeShape();
    }
    
    public VoxelShape makeShape() {	
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0.125, 0, 1, 0.875, 0.1875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.1875, 0.1875, 0.1875, 0.8125, 0.8125, 0.375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.25, 0.375, 0.75, 0.75, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.3125, 0.5625, 0.6875, 0.6875, 0.75));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0.375, 0.75, 0.625, 0.625, 0.875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0.4375, 0.875, 0.5625, 0.5625, 1));
        
        return shape;
    }
}
