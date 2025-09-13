package rearth.fabric;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.util.ActionResult;
import rearth.Drones;
import rearth.drone.DroneController;

public final class DronesModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        
        Drones.init();
        
        AttackBlockCallback.EVENT.register(((playerEntity, world, hand, blockPos, direction) -> {
            if (!world.isClient)
                DroneController.onPlayerBlockBreakStart(playerEntity, blockPos);
            return ActionResult.PASS;
        }));
    }
}
