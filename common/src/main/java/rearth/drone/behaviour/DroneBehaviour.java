package rearth.drone.behaviour;

public interface DroneBehaviour {
    
    void tick();
    float getCurrentYaw();
    int getPriority();  // higher = better, scale 0 - 100
    
    default void onStopped() {};
    default float getExtraRoll() {return 0f;}
    
    enum BlockFunctions {
        ARROW_LAUNCHER, LIGHT, MELEE_ATTACK, MINING_SUPPORT, PICKUP, SAW, FLIGHT
    }
    
}
