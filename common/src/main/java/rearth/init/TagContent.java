package rearth.init;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import rearth.Drones;

public class TagContent {
    
    public static final TagKey<Block> THRUSTER_BLOCKS = TagKey.of(RegistryKeys.BLOCK, Drones.id("thruster"));
    public static final TagKey<Block> LOW_THRUSTER = TagKey.of(RegistryKeys.BLOCK, Drones.id("low_thruster"));
    public static final TagKey<Block> MEDIUM_THRUSTER = TagKey.of(RegistryKeys.BLOCK, Drones.id("medium_thruster"));
    public static final TagKey<Block> HIGH_THRUSTER = TagKey.of(RegistryKeys.BLOCK, Drones.id("strong_thruster"));
    public static final TagKey<Block> ULTRA_THRUSTER = TagKey.of(RegistryKeys.BLOCK, Drones.id("ultra_thruster"));
    
    public static final TagKey<Block> ARROW_LAUNCHER = TagKey.of(RegistryKeys.BLOCK, Drones.id("arrow_launcher"));
    public static final TagKey<Block> MELEE_DAMAGE = TagKey.of(RegistryKeys.BLOCK, Drones.id("melee_damage"));
    public static final TagKey<Block> MINING_TOOLS = TagKey.of(RegistryKeys.BLOCK, Drones.id("mining_tools"));
    public static final TagKey<Block> PICKUP_TOOLS = TagKey.of(RegistryKeys.BLOCK, Drones.id("pickup_tools"));
    public static final TagKey<Block> AXE_TOOLS = TagKey.of(RegistryKeys.BLOCK, Drones.id("axe_tools"));   // todo usage
    
    // todo abilities:
    // chest
    // tree chopping
    
}
