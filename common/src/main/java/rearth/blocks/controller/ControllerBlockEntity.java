package rearth.blocks.controller;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import rearth.Drones;
import rearth.drone.DroneData;
import rearth.drone.RecordedBlock;
import rearth.init.BlockContent;
import rearth.init.BlockEntitiesContent;
import rearth.init.ComponentContent;
import rearth.init.ItemContent;
import rearth.util.FloodFill;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ControllerBlockEntity extends BlockEntity {
    
    public static final float LOW_THRUSTER_POWER = 10f;
    public static final float MEDIUM_THRUSTER_POWER = 15f;
    public static final float HIGH_THRUSTER_POWER = 20f;
    public static final float ULTRA_THRUSTER_POWER = 30f;
    
    public ControllerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesContent.ASSEMBLER_CONTROLLER.get(), pos, state);
    }
    
    public List<BlockPos> getPlatformBlocks() {
        
        var frameStart = getPlatformStart();
        
        if (frameStart.isEmpty()) return List.of();
        
        var frameBlocks = FloodFill.Run(world, frameStart.get(), candidate -> candidate.isOf(BlockContent.ASSEMBLER_FRAME), checkPos -> true, 200, false);
        
        if (frameBlocks.isEmpty()) return List.of();
        
        return frameBlocks;
    }
    
    public @Nullable DroneData getCurrentDroneData() {
        var frameBlocks = getPlatformBlocks();
        
        if (frameBlocks.isEmpty()) return null;
        
        BlockPos droneStart = null;
        for (var frameBlock : frameBlocks) {
            var frameAbove = frameBlock.up();
            var candidateState = world.getBlockState(frameAbove);
            if (isValidDroneBlock(candidateState)) {
                droneStart = frameAbove;
                break;
            }
        }
        
        if (droneStart == null) return null;
        
        var droneBlocks = FloodFill.Run(world, droneStart, ControllerBlockEntity::isValidDroneBlock, this::isAboveOwnFrame, 1000, true);
        var droneCenter = findCenterOfMass(droneBlocks);
        System.out.println("drone: " + droneBlocks);
        
        var blockData = new ArrayList<RecordedBlock>();
        for (var blockPos : droneBlocks) {
            var blockState = world.getBlockState(blockPos);
            var localPos = blockPos.subtract(droneCenter);
            var data = new RecordedBlock(blockState, localPos);
            blockData.add(data);
        }
        
        var droneId = world.getRandom().nextInt(Integer.MAX_VALUE);
        var droneOffset = droneCenter.subtract(pos);
        
        return new DroneData(blockData, droneId, droneOffset);
        // DroneController.PLAYER_DRONES.put(player.getName(), droneData);
        
    }
    
    private boolean isAboveOwnFrame(BlockPos pos) {
        
        var maxRange = 20;
        for (int i = 1; i <= maxRange; i++) {
            var testPos = pos.down(i);
            if (world.getBlockState(testPos).isOf(BlockContent.ASSEMBLER_FRAME)) return true;
        }
        
        return false;
        
    }
    
    private static BlockPos findCenterOfMass(List<BlockPos> positions) {
        if (positions.isEmpty()) {
            Drones.LOGGER.warn("tried to find COM for empty drone");
            return BlockPos.ORIGIN;
        }
        
        var dataX = 0d;
        var dataY = 0d;
        var dataZ = 0d;
        
        for (var pos : positions) {
            var center = pos.toCenterPos();
            dataX += center.x;
            dataY += center.y;
            dataZ += center.z;
        }
        
        var realCOM = new Vec3d(dataX / positions.size(), dataY / positions.size(), dataZ / positions.size());
        return BlockPos.ofFloored(realCOM);
    }
    
    private Optional<BlockPos> getPlatformStart() {
        for (var neighbor : FloodFill.GetHorizontalNeighbors(pos)) {
            if (world.getBlockState(neighbor).isOf(BlockContent.ASSEMBLER_FRAME)) return Optional.of(neighbor);
        }
        
        return Optional.empty();
    }
    
    public boolean loadDroneToWorld(DroneData data) {
        
        if (getCurrentDroneData() != null) return false;
        
        // see if all blocks could be potentially placed
        // fails if any blocks are occupied
        for (var droneBlockData : data.getBlocks()) {
            var offset = droneBlockData.localPos();
            var worldPos = this.pos.add(data.getAssemblerOffset()).add(offset);
            if (!isAboveOwnFrame(worldPos)) return false;
            var worldState = world.getBlockState(worldPos);
            if (!worldState.isAir()) return false;
        }
        
        for (var droneBlockData : data.getBlocks()) {
            var offset = droneBlockData.localPos();
            var worldPos = this.pos.add(data.getAssemblerOffset()).add(offset);
            world.setBlockState(worldPos, droneBlockData.state());
        }
        
        return true;
        
    }
    
    private static boolean isValidDroneBlock(BlockState state) {
        return !state.isAir() && !state.isLiquid() && !state.isOf(BlockContent.ASSEMBLER_FRAME) && !state.isOf(BlockContent.ASSEMBLER_CONTROLLER);
    }
    
    // this is called on the server, after the player has clicked the "assemble" button
    public void assembleDrone(PlayerEntity player, String name) {
        
        Drones.LOGGER.info("Assembling drone for: {}, drone name: {}", player.getName(), name);
        
        var droneData = getCurrentDroneData();
        if (droneData == null) {
            Drones.LOGGER.warn("Player tried to create empty/invalid drone");
            return;
        }
        
        var createdStack = new ItemStack(ItemContent.POCKET_DRONE);
        createdStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name));
        createdStack.set(ComponentContent.DRONE_DATA_TYPE.get(), droneData);
        
        var itemEntity = new ItemEntity(world, player.getX(), player.getY(), player.getZ(), createdStack);
        world.spawnEntity(itemEntity);
        
        // remove blocks
        for (var droneBlock : droneData.getBlocks()) {
            var worldPos = this.pos.add(droneData.getAssemblerOffset()).add(droneBlock.localPos());
            world.setBlockState(worldPos, Blocks.AIR.getDefaultState());
        }
        
    }
    
    // C2S packet, contains the given name and controller pos
    public record AssembleDronePacket(String name, BlockPos controllerPos) implements CustomPayload {
        
        public static final CustomPayload.Id<AssembleDronePacket> PAYLOAD_ID = new CustomPayload.Id<>(Drones.id("assemble"));
        
        public static final PacketCodec<ByteBuf, AssembleDronePacket> PACKET_CODEC = PacketCodec.tuple(
          PacketCodecs.STRING,
          AssembleDronePacket::name,
          BlockPos.PACKET_CODEC,
          AssembleDronePacket::controllerPos,
          AssembleDronePacket::new
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return PAYLOAD_ID;
        }
    }
}
