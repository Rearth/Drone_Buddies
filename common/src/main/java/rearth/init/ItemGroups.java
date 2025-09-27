package rearth.init;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import rearth.Drones;

public class ItemGroups {
    
    public static final DeferredRegister<ItemGroup> TABS = DeferredRegister.create(Drones.MOD_ID, RegistryKeys.ITEM_GROUP);
    
    public static final RegistrySupplier<ItemGroup> DRONES_TAB = TABS.register(
      Drones.id("main_group"),
      () -> CreativeTabRegistry.create(Text.translatable("category.drones.main_group"),
        () -> new ItemStack(ItemContent.POCKET_DRONE.get()))
    );
    
}
