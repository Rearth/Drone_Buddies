package rearth.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import rearth.Drones;
import rearth.items.PocketDrone;

public class ItemContent {
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Drones.MOD_ID, RegistryKeys.ITEM);
    
    public static final RegistrySupplier<Item> POCKET_DRONE = ITEMS.register("pocket_drone", () ->
                                                                                               new PocketDrone(new Item.Settings()
                                                                                                                 .maxCount(1)
                                                                                               ));
    
}
