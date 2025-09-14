package rearth.blocks.controller;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import rearth.Drones;
import rearth.drone.DroneController;
import rearth.drone.DroneData;
import rearth.drone.RecordedBlock;
import rearth.init.BlockContent;
import rearth.init.BlockEntitiesContent;
import rearth.util.FloodFill;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ControllerBlockEntity extends BlockEntity {
    
    public static final float LOW_THRUSTER_POWER = 10f;
    public static final float MEDIUM_THRUSTER_POWER = 15f;
    public static final float HIGH_THRUSTER_POWER = 20f;
    public static final float ULTRA_THRUSTER_POWER = 30f;
    
    public ControllerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesContent.ASSEMBLER_CONTROLLER.get(), pos, state);
    }
    
    public void onUse(BlockState state, PlayerEntity player) {
        System.out.println("assembling drone");
        
        var frameStart = getFrameStart();
        
        System.out.println(frameStart);
        
        if (frameStart.isEmpty()) return;
        
        var frameBlocks = FloodFill.Run(world, frameStart.get(), candidate -> candidate.isOf(BlockContent.ASSEMBLER_FRAME), 500, false);
        System.out.println("frame: " + frameBlocks);
        
        if (frameBlocks.isEmpty()) return;
        
        BlockPos droneStart = null;
        for (var frameBlock : frameBlocks) {
            var frameAbove = frameBlock.up();
            var candidateState = world.getBlockState(frameAbove);
            if (isValidDroneBlock(candidateState)) {
                droneStart = frameAbove;
                break;
            }
        }
        
        if (droneStart == null) return;
        
        var droneBlocks = FloodFill.Run(world, droneStart, ControllerBlockEntity::isValidDroneBlock, 500, true);
        var droneCenter = findCenterOfMass(droneBlocks);
        System.out.println("drone: " + droneBlocks);
        
        var blockData = new ArrayList<RecordedBlock>();
        for (var blockPos : droneBlocks) {
            var blockState = world.getBlockState(blockPos);
            var localPos = blockPos.subtract(droneCenter);
            var data = new RecordedBlock(blockState, localPos);
            blockData.add(data);
        }
        
        
        var droneData = new DroneData(blockData, droneCenter.toCenterPos(), Vec3d.ZERO);
        DroneController.PLAYER_DRONES.put(player.getName(), droneData);
        
    }
    
    private static BlockPos findCenterOfMass(List<BlockPos> positions) {
        if (positions.isEmpty()) {
            Drones.LOGGER.warn("tried to find COM for empty drone");
            return BlockPos.ORIGIN;
        }
        
        var dataX = 0d;
        var dataY = 0d;
        var dataZ = 0d;
        
        for (var pos : positions) {
            var center = pos.toCenterPos();
            dataX += center.x;
            dataY += center.y;
            dataZ += center.z;
        }
        
        var realCOM = new Vec3d(dataX / positions.size(), dataY / positions.size(), dataZ / positions.size());
        return BlockPos.ofFloored(realCOM);
    }
    
    private Optional<BlockPos> getFrameStart() {
        for (var neighbor : FloodFill.GetHorizontalNeighbors(pos)) {
            if (world.getBlockState(neighbor).isOf(BlockContent.ASSEMBLER_FRAME)) return Optional.of(neighbor);
        }
        
        return Optional.empty();
    }
    
    private static boolean isValidDroneBlock(BlockState state) {
        return !state.isAir() && !state.isLiquid() && !state.isOf(BlockContent.ASSEMBLER_FRAME) && !state.isOf(BlockContent.ASSEMBLER_CONTROLLER);
    }
}
