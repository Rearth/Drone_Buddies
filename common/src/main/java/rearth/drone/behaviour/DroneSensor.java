package rearth.drone.behaviour;

import net.minecraft.entity.player.PlayerEntity;
import rearth.drone.DroneServerData;

public interface DroneSensor {
    
    int getPriority();
    boolean sense(DroneServerData drone, PlayerEntity player);    // returns true if something has been found
    
}
