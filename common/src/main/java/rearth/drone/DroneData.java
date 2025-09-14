package rearth.drone;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockState;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rearth.Drones;
import rearth.drone.behaviour.*;
import rearth.init.TagContent;
import rearth.util.Helpers;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import static rearth.blocks.controller.ControllerBlockEntity.*;

public class DroneData implements CustomPayload {
    
    
    // synced to client
    private final List<RecordedBlock> blocks;
    public @NotNull Vec3d currentPosition;
    public @NotNull Vec3d currentRotation;  // y is vertical, z is forward, x is right
    
    // not synced
    public @NotNull Vec3d targetPosition = Vec3d.ZERO;
    public @NotNull Vec3d currentVelocity = Vec3d.ZERO;
    private @Nullable DroneBehaviour currentTask = null;   // this may only be null on the client
    public int ghostTicks = 0;
    public int ghostWaitTime = 0;
    
    // calculated on both sides
    public final EnumSet<DroneBehaviour.BlockFunctions> installed;
    public final boolean glowing;
    public final float power;
    public final List<DroneSensor> enabledSensors;
    
    public DroneData(
      @NotNull List<RecordedBlock> blocks,
      @NotNull Vec3d currentPosition,
      @NotNull Vec3d currentRotation) {
        
        this.blocks = blocks;
        this.currentPosition = currentPosition;
        this.currentRotation = currentRotation;
        
        // calculate drone data
        var weight = 0f;
        var thrust = 0f;
        var abilities = new ArrayList<DroneBehaviour.BlockFunctions>();
        abilities.add(DroneBehaviour.BlockFunctions.FLIGHT);
        var light = false;
        
        var droneFrame = new HashMap<Vec3i, BlockState>();
        blocks.forEach(block -> droneFrame.put(block.localPos(), block.state()));
        
        for (var recordedBlock : blocks) {
            
            var state = recordedBlock.state();
            weight += state.getBlock().getHardness();
            
            thrust += getThrust(recordedBlock, droneFrame);
            
            if (ArrowAttackBehaviour.isValid(recordedBlock, droneFrame)) {
                abilities.add(DroneBehaviour.BlockFunctions.ARROW_LAUNCHER);
            }
            if (MeleeAttackBehaviour.isValid(recordedBlock, droneFrame)) {
                abilities.add(DroneBehaviour.BlockFunctions.MELEE_ATTACK);
            }
            if (MiningSupportBehaviour.isValid(recordedBlock, droneFrame)) {
                abilities.add(DroneBehaviour.BlockFunctions.MINING_SUPPORT);
            }
            if (PickupBehaviour.isValid(recordedBlock, droneFrame)) {
                abilities.add(DroneBehaviour.BlockFunctions.PICKUP);
            }
//            if (state.isIn(TagContent.AXE_TOOLS)) {
//                abilities.add(DroneBehaviour.BlockFunctions.SAW);
//            }
            
            if (state.getLuminance() > 0) {
                light = true;
            }
            
        }
        
        this.power = thrust / weight;
        this.installed = EnumSet.copyOf(abilities);
        this.glowing = light;
        this.enabledSensors = getInstalledSensors(installed);
        
//        System.out.println(thrust + " / " + weight);
//        System.out.println(power);
//        System.out.println(Iterables.toString(enabledSensors));
//        System.out.println(Iterables.toString(installed));
    }
    
    public List<RecordedBlock> getBlocks() {
        return blocks;
    }
    
    public @NotNull Vec3d getCurrentPosition() {
        return currentPosition;
    }
    
    public @NotNull Vec3d getCurrentRotation() {
        return currentRotation;
    }
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return DATA_PAYLOAD_ID;
    }
    
    public @Nullable DroneBehaviour getCurrentTask() {
        return currentTask;
    }
    
    public boolean isGlowing() {
        return glowing;
    }
    
    public void setCurrentTask(@Nullable DroneBehaviour currentTask) {
        if (this.currentTask != null) {
            this.currentTask.onStopped();
        }
        this.currentTask = currentTask;
    }
    
    private static List<DroneSensor> getInstalledSensors(EnumSet<DroneBehaviour.BlockFunctions> functions) {
        var sensors = new ArrayList<DroneSensor>();
        
        if (functions.contains(DroneBehaviour.BlockFunctions.ARROW_LAUNCHER))
            sensors.add(new ArrowAttackBehaviour.ArrowAttackSensor());
        if (functions.contains(DroneBehaviour.BlockFunctions.MELEE_ATTACK))
            sensors.add(new MeleeAttackBehaviour.MeleeAttackSensor());
        if (functions.contains(DroneBehaviour.BlockFunctions.PICKUP))
            sensors.add(new PickupBehaviour.PickupSensor());
        
        return sensors;
    }
    
    private static float getThrust(RecordedBlock block, HashMap<Vec3i, BlockState> frame) {
        
        // thrusters need to have 2 empty blocks below
        
        var isThruster = block.state().isIn(TagContent.THRUSTER_BLOCKS);
        if (!isThruster) return 0;
        
        var blocked = frame.containsKey(block.localPos().down()) || frame.containsKey(block.localPos().down(2));
        if (blocked) return 0;
        
        var state = block.state();
        if (state.isIn(TagContent.LOW_THRUSTER)) {
            return LOW_THRUSTER_POWER;
        }
        if (state.isIn(TagContent.MEDIUM_THRUSTER)) {
            return MEDIUM_THRUSTER_POWER;
        }
        if (state.isIn(TagContent.HIGH_THRUSTER)) {
            return HIGH_THRUSTER_POWER;
        }
        if (state.isIn(TagContent.ULTRA_THRUSTER)) {
            return ULTRA_THRUSTER_POWER;
        }
        
        Drones.LOGGER.warn("Found thruster with thruster tag, but without a thrust strength tag: " + state);
        
        return 0;
        
    }
    
    public static final CustomPayload.Id<DroneData> DATA_PAYLOAD_ID = new CustomPayload.Id<>(Drones.id("drone_data"));
    
    public static PacketCodec<ByteBuf, DroneData> PACKET_CODEC = PacketCodec.tuple(
      RecordedBlock.PACKET_CODEC.collect(PacketCodecs.toList()),
      DroneData::getBlocks,
      Helpers.VEC3D_PACKET_CODEC,
      DroneData::getCurrentPosition,
      Helpers.VEC3D_PACKET_CODEC,
      DroneData::getCurrentRotation,
      DroneData::new
    );
}
