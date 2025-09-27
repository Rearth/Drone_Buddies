package rearth.items;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rearth.init.ComponentContent;

import java.util.List;
import java.util.Optional;

public class PocketDrone extends Item implements Equipment {
    
    public PocketDrone(Settings settings) {
        super(settings);
    }
    
    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.HEAD;
    }
    
    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        var base = super.getTooltipData(stack);
        
        if (!stack.contains(ComponentContent.DRONE_DATA_TYPE.get())) return base;
        
        var data = stack.get(ComponentContent.DRONE_DATA_TYPE.get());
        
        var speed = data.power;
        var blocks = data.getBlocks().size();
        var abilities = data.installed;
        var size = 1 / data.getSize();
        
        return base;
    }
    
    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        
        if (!stack.contains(ComponentContent.DRONE_DATA_TYPE.get())) {
            super.appendTooltip(stack, context, tooltip, type);
            return;
        }
        
        var data = stack.get(ComponentContent.DRONE_DATA_TYPE.get());
        
        var speed = String.format("%.1f", data.power);
        var blocks = data.getBlocks().size();
        var abilities = data.installed;
        var size = data.getSize();
        
        tooltip.add(Text.translatable("tooltip.drones.data_speed", speed));
        tooltip.add(Text.translatable("tooltip.drones.block_count", blocks));
        tooltip.add(Text.translatable("tooltip.drones.data_size", size));
        tooltip.add(Text.translatable("tooltip.drones.abilities_heading"));
        for (var ability : abilities) {
            tooltip.add(Text.literal(" - ").append(Text.translatable("drones.ability." + ability.name().toLowerCase())).formatted(Formatting.ITALIC));
        }
        
        tooltip.add(Text.literal(""));
        tooltip.add(Text.translatable("tooltip.drones.equip_hint").formatted(Formatting.ITALIC, Formatting.GRAY));
        
        
        super.appendTooltip(stack, context, tooltip, type);
    }
}
