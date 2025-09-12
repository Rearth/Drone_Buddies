package rearth.drone;

import dev.architectury.event.EventResult;
import dev.architectury.networking.NetworkManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import rearth.Drones;
import rearth.client.renderers.DroneRenderer;
import rearth.drone.behaviour.ArrowAttackBehaviour;
import rearth.drone.behaviour.DroneLight;
import rearth.drone.behaviour.MeleeAttackBehaviour;
import rearth.drone.behaviour.PickupBehaviour;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class DroneController {
    
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
                System.out.println("sensor match: " + sensor.getClass().getName());
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
        if (droneData.getCurrentTask() != null)
            rotationAngle = DroneRenderer.lerp(rotationAngle, droneData.getCurrentTask().getCurrentYaw(), 0.2f);
        var bankX = Math.min(velocityDelta.z * -bankingFactor, 35);
        var bankZ = Math.min(velocityDelta.x * bankingFactor, 35);
        
        
        droneData.currentRotation = new Vec3d(bankX, rotationAngle, bankZ);
        
        droneData.currentVelocity = currentVelocity.add(velocityDelta.multiply(accelerationPower));
        
        droneData.currentPosition = droneData.currentPosition.add(droneData.currentVelocity.multiply(powerMultiplier / 20f));
        
    }
    
    public static Optional<DroneData> getDroneOfPlayer(PlayerEntity player) {
        if (PLAYER_DRONES.containsKey(player.getName()))
            return Optional.of(PLAYER_DRONES.get(player.getName()));
        
        return Optional.empty();
    }
    
    private static void issueAttackCommend(PlayerEntity player, DroneData droneData, LivingEntity livingEntity) {
        Drones.LOGGER.debug("Issuing attack command: {} attack {}", player.getName(), livingEntity.getDisplayName());
        droneData.setCurrentTask(new MeleeAttackBehaviour(livingEntity, player, droneData));
    }
    
    public static EventResult onPlayerAttackEntityEvent(PlayerEntity player, World world, Entity entity, Hand hand, @Nullable EntityHitResult entityHitResult) {
        
        var droneCandidate = getDroneOfPlayer(player);
        if (droneCandidate.isPresent() && entity instanceof LivingEntity livingEntity)
            DroneController.issueAttackCommend(player, droneCandidate.get(), livingEntity);
        
        return EventResult.pass();
    }
}
