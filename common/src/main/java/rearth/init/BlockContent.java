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
import rearth.blocks.rotors.IronRotor;
import rearth.blocks.rotors.WoodenRotor;

// todo drops, crafting recipes, tool assignments, random loot spawns?, creative tab

public class BlockContent {
    
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Drones.MOD_ID, RegistryKeys.BLOCK);
    
    public static final RegistrySupplier<Block> ASSEMBLER_FRAME = BLOCKS.register("frame", () -> new Block(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)));
    public static final RegistrySupplier<Block> ASSEMBLER_CONTROLLER = BLOCKS.register("controller", () -> new ControllerBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)));
    
    public static final RegistrySupplier<Block> WOOD_ROTOR = BLOCKS.register("wood_rotor", () -> new WoodenRotor(AbstractBlock.Settings.copy(Blocks.OAK_PLANKS).nonOpaque()));
    public static final RegistrySupplier<Block> IRON_ROTOR = BLOCKS.register("iron_rotor", () -> new IronRotor(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).nonOpaque()));
    
    public static void registerItems() {
        
        registerItem(ASSEMBLER_FRAME, "frame");
        registerItem(ASSEMBLER_CONTROLLER, "controller");
        registerItem(WOOD_ROTOR, "wood_rotor");
        registerItem(IRON_ROTOR, "iron_rotor");
    
    }
    
    private static void registerItem(RegistrySupplier<Block> block, String name) {
        ItemContent.ITEMS.register(Drones.id(name), () -> new BlockItem(block.get(), new Item.Settings()));
    }
    
}
