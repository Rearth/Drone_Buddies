package rearth;

import dev.architectury.impl.NetworkAggregator;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.MinecraftClient;
import rearth.client.ui.DroneCreatorScreen;
import rearth.init.BlockEntitiesContent;
import rearth.init.NetworkContent;

import java.util.HashMap;
import java.util.List;

public final class DronesClient {
    
    public static final HashMap<Integer, NetworkContent.DroneMoveSyncPacket> CURRENT_DATA = new HashMap<>();
    
    public static void init() {
        Drones.LOGGER.info("Hello from drones client");
        
        NetworkAggregator.registerReceiver(NetworkManager.Side.S2C, NetworkContent.OpenDroneScreenPacket.PAYLOAD_ID, NetworkContent.OpenDroneScreenPacket.PACKET_CODEC, List.of(), DronesClient::onAssembleScreenPacket);
        
    }
    
    public static void onAssembleScreenPacket(NetworkContent.OpenDroneScreenPacket packet, NetworkManager.PacketContext context) {
        var player = context.getPlayer();
        var world = player.getWorld();
        var pos = packet.controllerPos();
        var candidate = world.getBlockEntity(pos, BlockEntitiesContent.ASSEMBLER_CONTROLLER.get());
        candidate.ifPresent(controllerBlockEntity ->
                              MinecraftClient.getInstance().setScreen(new DroneCreatorScreen(candidate.get().getCurrentDroneData(), pos))
        );
    }
}
