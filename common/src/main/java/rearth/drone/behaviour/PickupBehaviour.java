package rearth.drone.behaviour;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import rearth.drone.DroneData;
import rearth.util.Helpers;

import java.util.Optional;

// enabled with a lodestone and observer
public class PickupBehaviour implements DroneBehaviour{
    
    private static final int MAX_RANGE = 25;
    private static final float PICKUP_RANGE = 0.75f;
    
    private final PlayerEntity owner;
    private final DroneData drone;
    private final ItemEntity target;
    
    private PickupPhase phase;
    private boolean collected = false;
    
    public PickupBehaviour(PlayerEntity owner, DroneData drone, ItemEntity target) {
        this.owner = owner;
        this.drone = drone;
        this.target = target;
        this.phase = PickupPhase.MOVING_IN;
    }
    
    @Override
    public void tick() {
        
        switch (phase) {
            case MOVING_IN -> {
                drone.targetPosition = target.getEyePos();
                
                var playerDist = drone.currentPosition.distanceTo(owner.getEyePos());
                var targetDist = drone.currentPosition.distanceTo(target.getEyePos());
                
                if (playerDist > MAX_RANGE || target.isRemoved())
                    phase = PickupPhase.MOVING_HOME;
                
                if (targetDist < PICKUP_RANGE) {
                    // do pickup
                    phase = PickupPhase.MOVING_HOME;
                    collected = true;
                    target.setNoGravity(true);
                }
                
            }
            case MOVING_HOME -> {
                drone.targetPosition = owner.getEyePos();
                
                if (collected && !target.isRemoved()) {
                    target.setPosition(drone.currentPosition);
                }
                
                var playerDist = drone.currentPosition.distanceTo(owner.getEyePos());
                if (playerDist < 1) {
                    finishTask();
                }
                
            }
        }
        
    }
    
    private void finishTask() {
        if (collected && !target.isRemoved()) {
            target.setNoGravity(true);
        }
        
        drone.setCurrentTask(new PlayerSwarmBehaviour(drone, owner));
    }
    
    @Override
    public float getCurrentYaw() {
        
        if (phase == PickupPhase.MOVING_HOME) {
            return Helpers.calculateYaw(drone.currentPosition, owner.getEyePos());
        }
        
        return Helpers.calculateYaw(drone.currentPosition, target.getEyePos());
    }
    
    @Override
    public int getPriority() {
        return 10;
    }
    
    private enum PickupPhase {
        MOVING_IN, MOVING_HOME
    }
    
    public static Optional<ItemEntity> GetPickupTarget(PlayerEntity player) {
        var world = player.getWorld();
        var target = player.getEyePos();
        
        var range = MAX_RANGE / 2;
        
        var box = new Box(target.x - range, target.y - range, target.z - range, target.x + range, target.y + range, target.z + range);
        var items = world.getEntitiesByClass(ItemEntity.class, box, itemEntity -> !itemEntity.cannotPickup());
        if (!items.isEmpty())
            return Optional.of(items.getFirst());
        
        return Optional.empty();
    }
    
    public static class PickupSensor implements DroneSensor {
        
        @Override
        public int getPriority() {
            return 10;
        }
        
        @Override
        public boolean sense(DroneData drone, PlayerEntity player) {
            var candidate = PickupBehaviour.GetPickupTarget(player);
            if (candidate.isPresent()) {
                drone.setCurrentTask(new PickupBehaviour(player, drone, candidate.get()));
                return true;
            }
            
            return false;
        }
    }
}
