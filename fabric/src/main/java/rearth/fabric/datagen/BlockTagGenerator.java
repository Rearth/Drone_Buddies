package rearth.fabric.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import rearth.init.TagContent;

import java.util.concurrent.CompletableFuture;

public class BlockTagGenerator extends FabricTagProvider<Block> {
    
    public BlockTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.BLOCK, registriesFuture);
    }
    
    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        
        getOrCreateTagBuilder(TagContent.THRUSTER_BLOCKS)
          .add(Blocks.IRON_TRAPDOOR);
        
        getOrCreateTagBuilder(TagContent.LOW_THRUSTER)
          .addOptionalTag(BlockTags.TRAPDOORS);
        
        getOrCreateTagBuilder(TagContent.MEDIUM_THRUSTER)
          .add(Blocks.IRON_TRAPDOOR);
        
        getOrCreateTagBuilder(TagContent.ARROW_LAUNCHER)
          .add(Blocks.DISPENSER);
        
        getOrCreateTagBuilder(TagContent.MELEE_DAMAGE)
          .add(Blocks.CACTUS);
        
        getOrCreateTagBuilder(TagContent.PICKUP_TOOLS)
          .add(Blocks.LODESTONE);
        
        getOrCreateTagBuilder(TagContent.MINING_TOOLS)
          .add(Blocks.IRON_BLOCK);
        
    }
}
