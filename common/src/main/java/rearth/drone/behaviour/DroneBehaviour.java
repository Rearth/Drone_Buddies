package rearth.drone.behaviour;

public interface DroneBehaviour {
    
    void tick();
    float getCurrentYaw();
    int getPriority();  // higher = better, scale 0 - 100
    
}
