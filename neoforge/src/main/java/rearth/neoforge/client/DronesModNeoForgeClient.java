package rearth.neoforge.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import rearth.Drones;
import rearth.DronesClient;
import rearth.client.renderers.DroneRenderer;

@Mod(value = Drones.MOD_ID, dist = Dist.CLIENT)
public class DronesModNeoForgeClient {
    
    public DronesModNeoForgeClient(IEventBus eventBus) {
        
        DronesClient.init();
        eventBus.register(new EventHandler());
    }
    
    @EventBusSubscriber(modid = Drones.MOD_ID, value = Dist.CLIENT)
    public static class CustomEvents {
        
        @SubscribeEvent
        public static void onWorldRender(RenderLevelStageEvent event) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
                DroneRenderer.doRender(event.getPoseStack(), event.getCamera(), MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers());
            }
        }
    }
    
    
    static class EventHandler {
        
        @SubscribeEvent
        public void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            
            for (var entry : DronesClient.RENDER_LAYERS.entrySet()) {
                RenderLayers.setRenderLayer(entry.getKey().get(), entry.getValue());
            }
        }
    }
}
