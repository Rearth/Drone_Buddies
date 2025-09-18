package rearth.blocks.controller;

import com.mojang.serialization.MapCodec;
import dev.architectury.networking.NetworkManager;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import rearth.init.BlockEntitiesContent;
import rearth.init.ComponentContent;
import rearth.init.ItemContent;
import rearth.init.NetworkContent;

public class ControllerBlock extends BlockWithEntity {
    
    public ControllerBlock(Settings settings) {
        super(settings);
    }
    
    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }
    
    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
    
    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ControllerBlockEntity(pos, state);
    }
    
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        
        System.out.println("onUse client=" + world.isClient);
        
        if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
            var candidate = world.getBlockEntity(pos, BlockEntitiesContent.ASSEMBLER_CONTROLLER.get());
            candidate.ifPresent(controllerBlockEntity ->
                                  NetworkManager.sendToPlayer(serverPlayer, new NetworkContent.OpenDroneScreenPacket(pos))
            );
        }

        
        return ActionResult.SUCCESS;
    }
    
    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        
        System.out.println("onUseWithItem client=" + world.isClient);
        
        if (stack.isOf(ItemContent.POCKET_DRONE.get()) && stack.contains(ComponentContent.DRONE_DATA_TYPE.get())) {
            System.out.println("Loading pocket drone");
            
            var stackData = stack.get(ComponentContent.DRONE_DATA_TYPE.get());
            
            var candidate = world.getBlockEntity(pos, BlockEntitiesContent.ASSEMBLER_CONTROLLER.get());
            if (candidate.isPresent() && !world.isClient()) {
                var imported = candidate.get().loadDroneToWorld(stackData);
                if (imported) {
                    stack.decrement(1);
                    return ItemActionResult.CONSUME;
                }
            }
        }
        
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }
}
