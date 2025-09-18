package rearth.fabric.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import rearth.init.BlockContent;

public class ModelGenerator extends FabricModelProvider {
    
    public ModelGenerator(FabricDataOutput output) {
        super(output);
    }
    
    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerSimpleState(BlockContent.WOOD_ROTOR.get());
        blockStateModelGenerator.registerSimpleState(BlockContent.IRON_ROTOR.get());
    }
    
    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
    
    }
}
