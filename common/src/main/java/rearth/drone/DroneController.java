package rearth.drone;

import dev.architectury.event.EventResult;
import dev.architectury.networking.NetworkManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import rearth.drone.behaviour.*;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class DroneController {
    
    // tp the drone to player if it's too far away
    public static final int SNAP_RANGE = 30;
    
    public static final HashMap<Text, DroneData> PLAYER_DRONES = new HashMap<>();
    
    public static final SimplexNoiseSampler SIMPLEX = new SimplexNoiseSampler(Random.create());
    
    public static void tickPlayer(ServerPlayerEntity playerEntity) {
        
        if (PLAYER_DRONES.containsKey(playerEntity.getName())) {
            var playerDrone = PLAYER_DRONES.get(playerEntity.getName());
            updateDrone(playerEntity, playerDrone);
        }
        
    }
    
    public static void updateDrone(PlayerEntity player, DroneData droneData) {
        
        if (droneData.getCurrentTask() == null) return; // this should never happen
        
        if (droneData.isGlowing()) {
            DroneLight.updateDroneLight(droneData, player.getWorld());
        }
        
        updateDroneSensors(player, droneData);
        droneData.getCurrentTask().tick();
        updateDroneMovement(player, droneData);
        
        NetworkManager.sendToPlayer((ServerPlayerEntity) player, droneData);
    }
    
    private static void updateDroneSensors(PlayerEntity player, DroneData droneData) {
        var currentPriority = droneData.getCurrentTask().getPriority();
        
        // this should be sorted by priority
        var sensors = List.of(
          new ArrowAttackBehaviour.ArrowAttackSensor(),
          // new MeleeAttackBehaviour.MeleeAttackSensor(),
          new PickupBehaviour.PickupSensor()
        );
        
        // if a sensor matches, stop the search
        for (var sensor : sensors) {
            if (currentPriority >= sensor.getPriority()) break;
            
            if (sensor.sense(droneData, player)) {
                break;
            }
            
        }
        
    }
    
    private static void updateDroneMovement(PlayerEntity player, DroneData droneData) {
        
        var powerMultiplier = 1f;
        
        var accelerationPower = 0.2f;
        var bankingFactor = 30 * Math.sqrt(powerMultiplier);
        
        var currentVelocity = droneData.currentVelocity;
        var targetOffset = droneData.targetPosition.subtract(droneData.currentPosition);
        var desiredVelocity = targetOffset;
        var velocityDelta = desiredVelocity.subtract(currentVelocity);
        
        // 2 movement modes:
        // horizontal thrusters only
        // thrusters for forward and up
        // currently only mode 1 is implemented and available
        
        // mode 1:
        // angle the model on all axis. Model forward points to the player forward, and acceleration is achieved by tilting the body in the right direction
        // this is similar to a quadcopter
        var rotationAngle = droneData.currentRotation.y;
        var bankX = Math.clamp(velocityDelta.z * -bankingFactor, -45, 45);
        var bankZ = Math.clamp(velocityDelta.x * bankingFactor, -45, 45);
        
        if (droneData.getCurrentTask() != null) {
            rotationAngle = droneData.getCurrentTask().getCurrentYaw();
            bankX += droneData.getCurrentTask().getExtraRoll();
        }
        
        
        droneData.currentRotation = new Vec3d(bankX, rotationAngle, bankZ);
        
        droneData.currentVelocity = currentVelocity.add(velocityDelta.multiply(accelerationPower));
        
        var nextPosition = droneData.currentPosition.add(droneData.currentVelocity.multiply(powerMultiplier / 20f));
        
        var positionBlocked = !MiningSupportBehaviour.isPositionAvailableFull(player.getWorld(), droneData.currentPosition, nextPosition);
        
        //ghost through blocks
        if (droneData.ghostTicks > 0) {
            droneData.ghostTicks--;
            droneData.currentPosition = nextPosition;
        } else if (droneData.ghostWaitTime > 0) {   // wait for ghosting
            droneData.ghostWaitTime--;
            if (droneData.ghostWaitTime == 0) {
                droneData.ghostTicks = 20;
            }
        } else if (positionBlocked) {  // just hit an obstacle, start ghosting CD
            droneData.currentVelocity = Vec3d.ZERO;
            droneData.ghostWaitTime = 14;
            
            if (player.getWorld() instanceof ServerWorld serverWorld) {
                var middle = droneData.currentPosition;
                serverWorld.spawnParticles(ParticleTypes.PORTAL, middle.x, middle.y, middle.z, 15, 0, 0, 0, 0.2f);
            }
        } else {    // normal movement
            droneData.currentPosition = nextPosition;
            droneData.ghostTicks = 0;
            droneData.ghostWaitTime = 0;
        }
        
        // tp to player if too far away
        var playerDist = droneData.currentPosition.distanceTo(player.getEyePos());
        if (playerDist > SNAP_RANGE) {
            droneData.currentPosition = player.getEyePos();
        }
        
    }
    
    public static Optional<DroneData> getDroneOfPlayer(PlayerEntity player) {
        if (PLAYER_DRONES.containsKey(player.getName()))
            return Optional.of(PLAYER_DRONES.get(player.getName()));
        
        return Optional.empty();
    }
    
    private static void issueAttackCommend(PlayerEntity player, DroneData droneData, LivingEntity livingEntity) {
        droneData.setCurrentTask(new MeleeAttackBehaviour(livingEntity, player, droneData));
    }
    
    public static EventResult onPlayerAttackEntityEvent(PlayerEntity player, World world, Entity entity, Hand hand, @Nullable EntityHitResult entityHitResult) {
        
        var droneCandidate = getDroneOfPlayer(player);
        if (droneCandidate.isPresent() && entity instanceof LivingEntity livingEntity)
            DroneController.issueAttackCommend(player, droneCandidate.get(), livingEntity);
        
        return EventResult.pass();
    }
    
    public static void onPlayerBlockBreakStart(PlayerEntity player, BlockPos blockPos) {
        
        var droneCandidate = getDroneOfPlayer(player);
        if (droneCandidate.isPresent() && MiningSupportBehaviour.isValidMiningTarget(player.getWorld(), blockPos)) {
            droneCandidate.get().setCurrentTask(new MiningSupportBehaviour(blockPos, player, droneCandidate.get()));
        }
        
    }
}
