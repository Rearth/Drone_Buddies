package rearth.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import rearth.Drones;
import rearth.blocks.controller.ControllerBlock;

public class BlockContent {
    
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Drones.MOD_ID, RegistryKeys.BLOCK);
    
    public static final RegistrySupplier<Block> ASSEMBLER_FRAME = BLOCKS.register("frame", () -> new Block(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)));
    public static final RegistrySupplier<Block> ASSEMBLER_CONTROLLER = BLOCKS.register("controller", () -> new ControllerBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)));
    public static void registerItems() {
        
        registerItem(ASSEMBLER_FRAME, "frame");
        registerItem(ASSEMBLER_CONTROLLER, "controller");
    
    }
    
    private static void registerItem(RegistrySupplier<Block> block, String name) {
        ItemContent.ITEMS.register(Drones.id(name), () -> new BlockItem(block.get(), new Item.Settings()));
    }
    
}
