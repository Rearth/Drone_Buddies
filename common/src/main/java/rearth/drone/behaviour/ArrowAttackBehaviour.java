package rearth.drone.behaviour;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import rearth.drone.DroneData;
import rearth.util.Helpers;

import java.util.Comparator;

// an instance of a behaviour to attack one specific entity.
// consists of 3 phases: move in, attack, move home
public class ArrowAttackBehaviour extends PlayerSwarmBehaviour {
    
    private static final int MAX_RANGE = 25;
    private static final int ATTACK_COOLDOWN = 24;
    
    private final LivingEntity target;
    private final PlayerEntity owner;
    private final DroneData drone;
    
    private int attackCooldown = 0;
    
    public ArrowAttackBehaviour(LivingEntity target, PlayerEntity owner, DroneData drone) {
        super(drone, owner);
        this.target = target;
        this.owner = owner;
        this.drone = drone;
    }
    
    @Override
    public void tick() {
        
        super.tick();
        
        if (target.isRemoved() || !target.isAlive() || !target.isAttackable()) finishTask();
        
        
        var dist = drone.currentPosition.distanceTo(target.getEyePos());
        if (dist > MAX_RANGE) finishTask();
        
        if (attackCooldown <= 0) {
            // shoot arrow
            var world = owner.getWorld();
            var stack = new ItemStack(Items.ARROW);
            var targetPos = target.getEyePos().add(0, dist / 10f, 0);   // adjust target slightly up for longer distances to hit
            var offset = targetPos.subtract(drone.currentPosition);
            var initialVelocity = offset.normalize().multiply(2);
            
            var arrowEntity = new ArrowEntity(world, drone.currentPosition.x, drone.currentPosition.y, drone.currentPosition.z, stack, null);
            arrowEntity.setVelocity(initialVelocity);
            world.spawnEntity(arrowEntity);
            
            attackCooldown = ATTACK_COOLDOWN;
            
            // particle
            if (owner.getWorld() instanceof ServerWorld serverWorld) {
                var forward = target.getEyePos().subtract(drone.currentPosition).normalize();
                var particleStart = drone.currentPosition.add(forward.multiply(0.3f));
                serverWorld.spawnParticles(ParticleTypes.SMALL_GUST, particleStart.x, particleStart.y, particleStart.z, 1, forward.x, forward.y, forward.z, 0.2f);
            }
            
        } else {
            attackCooldown--;
        }
        
    }
    
    public void finishTask() {
        drone.setCurrentTask(new PlayerSwarmBehaviour(drone, owner));
    }
    
    @Override
    public float getCurrentYaw() {
        return Helpers.calculateYaw(drone.currentPosition, target.getEyePos());
    }
    
    @Override
    public int getPriority() {
        return 55;
    }
    
    
    public static class ArrowAttackSensor implements DroneSensor {
        
        @Override
        public int getPriority() {
            return 35;
        }
        
        @Override
        public boolean sense(DroneData drone, PlayerEntity player) {
            
            var world = player.getWorld();
            var entityRange = 16;
            var playerHead = player.getEyePos();
            
            var targets = world.getEntitiesByClass(LivingEntity.class, new Box(playerHead.x - entityRange, playerHead.y - entityRange, playerHead.z - entityRange, playerHead.x + entityRange, playerHead.y + entityRange, playerHead.z + entityRange), EntityPredicates.VALID_LIVING_ENTITY.and(EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
            targets.sort(Comparator.comparingDouble((entity) -> entity.squaredDistanceTo(playerHead)));
            targets = targets.stream().filter(target -> target.isAlive() && !target.isRemoved() && target instanceof Monster).toList();
            
            if (!targets.isEmpty()) {
                drone.setCurrentTask(new ArrowAttackBehaviour(targets.getFirst(), player, drone));
                return true;
            }
            
            return false;
        }
    }
    
}
