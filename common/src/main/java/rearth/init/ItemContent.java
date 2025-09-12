package rearth.init;

import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import rearth.Drones;

public class ItemContent {
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Drones.MOD_ID, RegistryKeys.ITEM);
    
}
