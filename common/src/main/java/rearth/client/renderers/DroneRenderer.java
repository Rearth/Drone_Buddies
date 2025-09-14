package rearth.client.renderers;

import net.minecraft.block.BlockEntityProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import rearth.drone.DroneData;
import rearth.util.FloodFill;
import rearth.util.Helpers;

import java.util.HashMap;

public class DroneRenderer {
    
    public static DroneData renderedDrone = null;
    
    private static final HashMap<PlayerEntity, Vec3d> lastPositions = new HashMap<>();
    private static final HashMap<PlayerEntity, Vec3d> lastRotations = new HashMap<>();
    
    public static void doRender(MatrixStack matrices, Camera camera, VertexConsumerProvider vertexConsumers) {
        var world = MinecraftClient.getInstance().world;
        if (world == null || renderedDrone == null) return;
        
        var dronePlayer = MinecraftClient.getInstance().player;
        var tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
        
        var targetScale = 0.3f;
        
        var lastPos = lastPositions.computeIfAbsent(dronePlayer, player -> renderedDrone.currentPosition);
        var lastRot = lastRotations.computeIfAbsent(dronePlayer, player -> renderedDrone.currentRotation);
        var newPos = renderedDrone.currentPosition;
        var newRot = renderedDrone.currentRotation;
        
        // adjust for switch from angle -180 to 180 on Y rotation axis
        var rotDistY = Math.abs(lastRot.y - newRot.y);
        var altRotDistY = Math.abs(-lastRot.y - newRot.y);
        if (altRotDistY < rotDistY && Math.abs(lastRot.y) > 90) {
            var positive = lastRot.y > 0;
            var adjustedY = positive ? lastRot.y - 360 : lastRot.y + 360;
            lastRot = new Vec3d(lastRot.x, adjustedY, lastRot.z);
        }
        
        var deltaDronePos = Helpers.lerp(lastPos, newPos, 0.1f);
        lastPositions.put(dronePlayer, deltaDronePos);
        var deltaDroneRot = Helpers.lerp(lastRot, newRot, 0.02f);
        lastRotations.put(dronePlayer, deltaDroneRot);
        
        matrices.push();
        matrices.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
        matrices.translate(deltaDronePos.x, deltaDronePos.y, deltaDronePos.z);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) -deltaDroneRot.x));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) -deltaDroneRot.z));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) -deltaDroneRot.y));
        
        
        for (var blockData : renderedDrone.getBlocks()) {
            var localOffset = blockData.localPos();
            var state = blockData.state();
            
            var scaledLocalOffset = Vec3d.of(localOffset).add(-0.5f, -0.5f, -0.5f).multiply(targetScale);
            
            matrices.push();
            matrices.translate(scaledLocalOffset.x, scaledLocalOffset.y, scaledLocalOffset.z);
            matrices.scale(0.3f, 0.3f, 0.3f);
            
            var light = getMaxLight(BlockPos.ofFloored(renderedDrone.currentPosition), world);
            
            // render baked / animated block
            MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(
              state,
              matrices,
              vertexConsumers,
              light, OverlayTexture.DEFAULT_UV
            );
            
            // render optional custom entity renderer
            if (state.getBlock() instanceof BlockEntityProvider blockEntityProvider) {
                var blockEntity = blockEntityProvider.createBlockEntity(new BlockPos(localOffset), state);
                var renderer = MinecraftClient.getInstance().getBlockEntityRenderDispatcher().get(blockEntity);
                if (renderer != null) {
                    renderer.render(blockEntity, 0, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
                }
            }
            
            matrices.pop();
        }
        
        matrices.pop();
        
    }
    
    private static int getMaxLight(BlockPos center, World world) {
        var bestLight = WorldRenderer.getLightmapCoordinates(world, center);
        
        for (var side : FloodFill.GetNeighbors(center)) {
            var candidate = WorldRenderer.getLightmapCoordinates(world, side);
            bestLight = Math.max(candidate, bestLight);
        }
        
        return bestLight;
    }
    
}
