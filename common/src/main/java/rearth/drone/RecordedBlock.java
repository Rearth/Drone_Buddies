package rearth.drone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.Vec3i;
import rearth.util.Helpers;

public record RecordedBlock(BlockState state, Vec3i localPos) {
    
    public static final Codec<RecordedBlock> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      BlockState.CODEC.fieldOf("b").forGetter(RecordedBlock::state),
      Vec3i.CODEC.fieldOf("p").forGetter(RecordedBlock::localPos)
    ).apply(instance, RecordedBlock::new));
    
    public static final PacketCodec<ByteBuf, RecordedBlock> PACKET_CODEC = PacketCodec.tuple(
      PacketCodecs.entryOf(Block.STATE_IDS),
      RecordedBlock::state,
      Helpers.VEC3I_PACKET_CODEC,
      RecordedBlock::localPos,
      RecordedBlock::new
    );
    
}
