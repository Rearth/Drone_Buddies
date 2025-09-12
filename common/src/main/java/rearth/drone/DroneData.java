package rearth.drone;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rearth.Drones;
import rearth.drone.behaviour.DroneBehaviour;
import rearth.drone.behaviour.PlayerSwarmBehaviour;

import java.util.List;

public class DroneData implements CustomPayload {
    
    
    // synced to client
    private final List<RecordedBlock> blocks;
    public @NotNull Vec3d currentPosition;
    public @NotNull Vec3d currentRotation;  // y is vertical, z is forward, x is right
    public final boolean glowing;
    
    // not synced
    public @NotNull Vec3d targetPosition = Vec3d.ZERO;
    public @NotNull Vec3d currentVelocity = Vec3d.ZERO;
    private @Nullable DroneBehaviour currentTask;   // this may only be null on the client
    
    public DroneData(List<RecordedBlock> blocks, @NotNull Vec3d currentPosition, @NotNull Vec3d currentRotation, boolean glowing) {
        this(blocks, currentPosition, currentRotation, null, glowing);
    }
    
    public DroneData(List<RecordedBlock> blocks, @NotNull Vec3d currentPosition, @NotNull Vec3d currentRotation, @Nullable PlayerEntity player, boolean glowing) {
        this.blocks = blocks;
        this.currentPosition = currentPosition;
        this.currentRotation = currentRotation;
        this.currentTask = new PlayerSwarmBehaviour(this, player);
        this.glowing = glowing;
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
        this.currentTask = currentTask;
    }
    
    public static final CustomPayload.Id<DroneData> DATA_PAYLOAD_ID = new CustomPayload.Id<>(Drones.id("drone_data"));
    
    public static PacketCodec<ByteBuf, DroneData> PACKET_CODEC = PacketCodec.tuple(
      RecordedBlock.PACKET_CODEC.collect(PacketCodecs.toList()),
      DroneData::getBlocks,
      RecordedBlock.VEC3D_PACKET_CODEC,
      DroneData::getCurrentPosition,
      RecordedBlock.VEC3D_PACKET_CODEC,
      DroneData::getCurrentRotation,
      PacketCodecs.BOOL,
      DroneData::isGlowing,
      DroneData::new
    );
}
