package rearth.init;

import dev.architectury.impl.NetworkAggregator;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import rearth.client.renderers.DroneRenderer;
import rearth.drone.DroneData;

import java.util.List;

public class NetworkContent {

    public static void init() {
        NetworkContent.registerS2C(DroneData.DATA_PAYLOAD_ID, DroneData.PACKET_CODEC, ((value, context) -> {
            DroneRenderer.renderedDrone = value;
        }));
        
    }
    
    private static <T extends CustomPayload> void registerS2C(
      CustomPayload.Id<T> dataPayloadId,
      PacketCodec<ByteBuf, T> packetCodec,
      NetworkManager.NetworkReceiver<T> receiver) {
        
        if (Platform.getEnvironment().equals(Env.SERVER)) {
            NetworkAggregator.registerS2CType(dataPayloadId, packetCodec, List.of());
        } else {
            NetworkAggregator.registerReceiver(NetworkManager.Side.S2C, dataPayloadId, packetCodec, List.of(), receiver);
        }
        
    }
    
}
