package rearth.fabric.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.data.client.*;
import rearth.init.BlockContent;
import rearth.init.ItemContent;

public class ModelGenerator extends FabricModelProvider {
    
    public ModelGenerator(FabricDataOutput output) {
        super(output);
    }
    
    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerSimpleState(BlockContent.WOOD_ROTOR.get());
        blockStateModelGenerator.registerSimpleState(BlockContent.IRON_ROTOR.get());
        blockStateModelGenerator.registerSimpleState(BlockContent.ION_THRUSTER.get());
        blockStateModelGenerator.registerSimpleState(BlockContent.DRILL.get());
        blockStateModelGenerator.registerGeneric(BlockContent.ASSEMBLER_CONTROLLER.get());
        registerFrame(BlockContent.ASSEMBLER_FRAME.get(), blockStateModelGenerator);
    }
    
    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(ItemContent.POCKET_DRONE.get(), Models.GENERATED);
    }
    
    public void registerFrame(Block block, BlockStateModelGenerator blockStateModelGenerator) {
        var textureMap = (new TextureMap())
                           .put(TextureKey.PARTICLE, TextureMap.getSubId(block, "_side"))
                           .put(TextureKey.DOWN, TextureMap.getSubId(block, "_down"))
                           .put(TextureKey.UP, TextureMap.getSubId(block, "_up"))
                           .put(TextureKey.NORTH, TextureMap.getSubId(block, "_side"))
                           .put(TextureKey.SOUTH, TextureMap.getSubId(block, "_side"))
                           .put(TextureKey.EAST, TextureMap.getSubId(block, "_side"))
                           .put(TextureKey.WEST, TextureMap.getSubId(block, "_side"));
        
        blockStateModelGenerator.blockStateCollector.accept(
          BlockStateModelGenerator.createSingletonBlockState(block, Models.CUBE.upload(block, textureMap,blockStateModelGenerator.modelCollector)));
    }
}
