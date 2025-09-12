package rearth.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.RegistryKeys;
import rearth.Drones;
import rearth.blocks.controller.ControllerBlockEntity;

public class BlockEntitiesContent {
    
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Drones.MOD_ID, RegistryKeys.BLOCK_ENTITY_TYPE);
    
    public static final RegistrySupplier<BlockEntityType<ControllerBlockEntity>> ASSEMBLER_CONTROLLER = BLOCK_ENTITIES.register("controller", () -> BlockEntityType.Builder.create(ControllerBlockEntity::new, BlockContent.ASSEMBLER_CONTROLLER.get()).build(null));
    
}
