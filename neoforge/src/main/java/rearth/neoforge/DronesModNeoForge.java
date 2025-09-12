package rearth.neoforge;

import net.neoforged.fml.common.Mod;

import rearth.Drones;

@Mod(Drones.MOD_ID)
public final class DronesModNeoForge {
    public DronesModNeoForge() {
        // Run our common setup.
        Drones.init();
    }
}
