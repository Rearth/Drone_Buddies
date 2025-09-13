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
import rearth.drone.behaviour.DroneBehaviour;
import rearth.init.BlockContent;
import rearth.init.BlockEntitiesContent;
import rearth.init.TagContent;
import rearth.util.FloodFill;

import java.util.ArrayList;
import java.util.EnumSet;
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
        
        createDroneData(blockData, droneCenter, player);
        
    }
    
    private void createDroneData(List<RecordedBlock> blocks, BlockPos worldCenter, PlayerEntity player) {
        
        var weight = 0f;
        var thrust = 0f;
        var abilities = new ArrayList<DroneBehaviour.BlockFunctions>();
        abilities.add(DroneBehaviour.BlockFunctions.FLIGHT);
        var light = false;
        
        for (var recordedBlock : blocks) {
            
            var state = recordedBlock.state();
            weight += state.getHardness(world, worldCenter.add(recordedBlock.localPos()));
            
            if (state.isIn(TagContent.LOW_THRUSTER)) {
                thrust += LOW_THRUSTER_POWER;
            } else if (state.isIn(TagContent.MEDIUM_THRUSTER)) {
                thrust += MEDIUM_THRUSTER_POWER;
            } else if (state.isIn(TagContent.HIGH_THRUSTER)) {
                thrust += HIGH_THRUSTER_POWER;
            } else if (state.isIn(TagContent.ULTRA_THRUSTER)) {
                thrust += ULTRA_THRUSTER_POWER;
            }
            
            if (state.isIn(TagContent.ARROW_LAUNCHER)) {
                abilities.add(DroneBehaviour.BlockFunctions.ARROW_LAUNCHER);
            }
            if (state.isIn(TagContent.MELEE_DAMAGE)) {
                abilities.add(DroneBehaviour.BlockFunctions.MELEE_ATTACK);
            }
            if (state.isIn(TagContent.MINING_TOOLS)) {
                abilities.add(DroneBehaviour.BlockFunctions.MINING_SUPPORT);
            }
            if (state.isIn(TagContent.PICKUP_TOOLS)) {
                abilities.add(DroneBehaviour.BlockFunctions.PICKUP);
            }
            if (state.isIn(TagContent.AXE_TOOLS)) {
                abilities.add(DroneBehaviour.BlockFunctions.SAW);
            }
            
            if (state.getLuminance() >  0) {
                light = true;
            }
            
        }
        
        var thrustRatio = thrust / weight;
        var abilitySet = EnumSet.copyOf(abilities);
        
        System.out.println(thrustRatio);
        System.out.println(abilitySet);
        
        var droneData = new DroneData(blocks, worldCenter.toCenterPos(), Vec3d.ZERO, player, light, abilitySet, thrustRatio);
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
