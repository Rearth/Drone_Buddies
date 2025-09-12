package rearth;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rearth.drone.DroneController;
import rearth.drone.behaviour.DroneLight;
import rearth.init.BlockContent;
import rearth.init.BlockEntitiesContent;
import rearth.init.ItemContent;
import rearth.init.NetworkContent;

public final class Drones {
    public static final String MOD_ID = "drones";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static void init() {
        
        LOGGER.info("May your drones assemble without issue!");
        
        BlockContent.BLOCKS.register();
        BlockEntitiesContent.BLOCK_ENTITIES.register();
        ItemContent.ITEMS.register();
        
        TickEvent.SERVER_PRE.register(event -> event.getWorlds().forEach(world -> world.getPlayers().forEach(DroneController::tickPlayer)));
        TickEvent.SERVER_PRE.register(event -> event.getWorlds().forEach(DroneLight::removeOldLights));
        
        PlayerEvent.ATTACK_ENTITY.register(DroneController::onPlayerAttackEntityEvent);
        
        NetworkContent.init();
        
        BlockContent.registerItems();
        
    }
}
