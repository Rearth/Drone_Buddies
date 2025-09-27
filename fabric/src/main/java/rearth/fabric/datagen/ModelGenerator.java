package rearth.fabric.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
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
    }
    
    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(ItemContent.POCKET_DRONE.get(), Models.GENERATED);
    }
}
