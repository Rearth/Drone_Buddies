package rearth.drone;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.Vec3i;
import rearth.util.Helpers;

public record RecordedBlock(BlockState state, Vec3i localPos) {
    
    public static final PacketCodec<ByteBuf, RecordedBlock> PACKET_CODEC = PacketCodec.tuple(
      PacketCodecs.entryOf(Block.STATE_IDS),
      RecordedBlock::state,
      Helpers.VEC3I_PACKET_CODEC,
      RecordedBlock::localPos,
      RecordedBlock::new
    );
    
}
