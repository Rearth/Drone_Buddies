package rearth.drone.behaviour;

import net.minecraft.entity.player.PlayerEntity;
import rearth.drone.DroneData;

public interface DroneSensor {
    
    int getPriority();
    boolean sense(DroneData drone, PlayerEntity player);    // returns true if something has been found
    
}
