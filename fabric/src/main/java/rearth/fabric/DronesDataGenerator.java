package rearth.fabric;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import rearth.fabric.datagen.BlockTagGenerator;

public class DronesDataGenerator implements DataGeneratorEntrypoint {
    
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        
        System.out.println("Running drones datagen");
        
        var pack = fabricDataGenerator.createPack();
        pack.addProvider(BlockTagGenerator::new);
        
    }
}
