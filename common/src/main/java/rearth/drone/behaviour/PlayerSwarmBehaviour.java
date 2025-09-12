package rearth.drone.behaviour;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import rearth.drone.DroneData;
import rearth.util.Helpers;

import static rearth.drone.DroneController.SIMPLEX;

public class PlayerSwarmBehaviour implements DroneBehaviour {
    
    private final DroneData drone;
    private final PlayerEntity owner;
    
    public PlayerSwarmBehaviour(DroneData drone, PlayerEntity owner) {
        this.drone = drone;
        this.owner = owner;
    }
    
    @Override
    public void tick() {
        drone.targetPosition = getIdlePositionTarget(owner);
    }
    
    @Override
    public float getCurrentYaw() {
        var playerDist = drone.currentPosition.distanceTo(owner.getEyePos());
        if (playerDist > 5) {
            return Helpers.calculateYaw(drone.currentPosition, owner.getEyePos());
        }
        return owner.headYaw;
    }
    
    @Override
    public int getPriority() {
        return 1;
    }
    
    // circles overhead in a random manner, with slight Y variations
    public static Vec3d getIdlePositionTarget(PlayerEntity player) {
        var playerHead = player.getEyePos();
        var overheadCenter = playerHead.add(0, 0.5f, 0);
        
        var playerYaw = Math.toRadians(player.bodyYaw - 90);
        var playerBackDir = new Vec3d(Math.cos(playerYaw), 0, Math.sin(playerYaw)).normalize();
        
        
        var time = player.getWorld().getTime();
        var sampledX = time / 100f;
        
        var x = SIMPLEX.sample(sampledX, 0);
        var y = SIMPLEX.sample(sampledX, 5000);
        var z = SIMPLEX.sample(sampledX + 5000, 5000);
        
        var offset = new Vec3d(x, y / 3, z);
        
        return overheadCenter.add(offset.multiply(1)).add(playerBackDir.multiply(0.9f));
        
    }
}
