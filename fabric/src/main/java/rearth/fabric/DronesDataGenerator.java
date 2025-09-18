package rearth.fabric;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import rearth.Drones;
import rearth.fabric.datagen.BlockTagGenerator;
import rearth.fabric.datagen.ModelGenerator;

public class DronesDataGenerator implements DataGeneratorEntrypoint {
    
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        
        System.out.println("Running drones datagen");
        
        var pack = fabricDataGenerator.createPack();
        pack.addProvider(BlockTagGenerator::new);
        pack.addProvider(ModelGenerator::new);
        
    }
}
