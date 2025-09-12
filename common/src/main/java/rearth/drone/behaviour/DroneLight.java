package rearth.drone.behaviour;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import rearth.drone.DroneData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DroneLight {
    
    private static final Map<GlobalPos, Long> activeLights = new HashMap<>();
    private static final Map<DroneData, BlockPos> droneLights = new HashMap<>();
    
    // to ensure lights are always cleaned up
    public static void removeOldLights(World world) {
        
        var removed = new HashSet<GlobalPos>();
        
        for (var pair : activeLights.entrySet()) {
            if (pair.getValue() < world.getTime() && pair.getKey().dimension().equals(world.getRegistryKey())) {
                removeDroneLight(pair.getKey().pos(), world);
                removed.add(pair.getKey());
            }
        }
        
        removed.forEach(activeLights::remove);
    }
    
    public static void updateDroneLight(DroneData drone, World world) {
        
        var targetPos = BlockPos.ofFloored(drone.currentPosition);
        var lastPos = droneLights.get(drone);
        if (lastPos != null) {
            
            if (lastPos.equals(targetPos)) {    // update timestamp
                activeLights.put(GlobalPos.create(world.getRegistryKey(), targetPos), world.getTime() + 20);
            } else {    // or remove the light if we moved
                activeLights.remove(GlobalPos.create(world.getRegistryKey(), lastPos));
                removeDroneLight(lastPos, world);
                createDroneLight(drone, targetPos, world);
            }
            
        } else {
            createDroneLight(drone, targetPos, world);
        }
        
    }
    
    private static void removeDroneLight(BlockPos pos, World world) {
        var existingState = world.getBlockState(pos);
        if (!existingState.isOf(Blocks.LIGHT)) return;
        
        world.setBlockState(pos, Blocks.AIR.getDefaultState());
        System.out.println("removing light: " + pos.toShortString());
    }
    
    private static void createDroneLight(DroneData data, BlockPos pos, World world) {
        
        var existingState = world.getBlockState(pos);
        if (!existingState.isAir()) return;
        
        System.out.println("creating light: " + pos.toShortString());
        
        world.setBlockState(pos, Blocks.LIGHT.getDefaultState());
        
        droneLights.put(data, pos);
        activeLights.put(GlobalPos.create(world.getRegistryKey(), pos), world.getTime() + 20);
        
    }
    
}
