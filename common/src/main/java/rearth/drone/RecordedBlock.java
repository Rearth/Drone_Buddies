package rearth.drone;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public record RecordedBlock(BlockState state, Vec3i localPos) {
    
    public static PacketCodec<ByteBuf, Vec3i> VEC3I_PACKET_CODEC = new PacketCodec<>() {
        @Override
        public Vec3i decode(ByteBuf buf) {
            var x = buf.readInt();
            var y = buf.readInt();
            var z = buf.readInt();
            return new Vec3i(x, y, z);
        }
        
        @Override
        public void encode(ByteBuf buf, Vec3i value) {
            buf.writeInt(value.getX());
            buf.writeInt(value.getY());
            buf.writeInt(value.getZ());
        }
    };
    
    public static final PacketCodec<ByteBuf, RecordedBlock> PACKET_CODEC = PacketCodec.tuple(
      PacketCodecs.entryOf(Block.STATE_IDS),
      RecordedBlock::state,
      VEC3I_PACKET_CODEC,
      RecordedBlock::localPos,
      RecordedBlock::new
    );
    
    public static PacketCodec<ByteBuf, Vec3d> VEC3D_PACKET_CODEC = new PacketCodec<>() {
        @Override
        public Vec3d decode(ByteBuf buf) {
            var x = buf.readDouble();
            var y = buf.readDouble();
            var z = buf.readDouble();
            return new Vec3d(x, y, z);
        }
        
        @Override
        public void encode(ByteBuf buf, Vec3d value) {
            buf.writeDouble(value.x);
            buf.writeDouble(value.y);
            buf.writeDouble(value.z);
        }
    };
    
}
