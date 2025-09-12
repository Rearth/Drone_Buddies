package rearth.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

public class FloodFill {
    
    public static List<BlockPos> Run(World world, BlockPos start, Predicate<BlockState> filter, int maxCount, boolean allowDiagonal) {
        
        var checked = new HashSet<BlockPos>();
        var results = new ArrayList<BlockPos>();
        var open = new HashSet<BlockPos>();
        
        open.add(start);
        
        do {
            
            var nextSet = new HashSet<BlockPos>();
            
            for (var checkPos : open) {
                if (checked.contains(checkPos)) continue;
                checked.add(checkPos);
                
                var checkState = world.getBlockState(checkPos);
                if (filter.test(checkState)) {
                    results.add(checkPos);
                    
                    // add neighbors to next set
                    nextSet.addAll(List.of(GetNeighbors(checkPos, allowDiagonal)));
                    
                }
            }
            
            open = nextSet;
            
        } while (!open.isEmpty() && results.size() <= maxCount);
        
        return results;
        
    }
    
    public static BlockPos[] GetNeighbors(BlockPos from, boolean diagonal) {
        return diagonal ? GetNeighborsDiagonal(from) : GetNeighbors(from);
    }
    
    public static BlockPos[] GetNeighbors(BlockPos from) {
        return new BlockPos[] {from.up(), from.down(), from.north(), from.east(), from.south(), from.west()};
    }
    
    public static BlockPos[] GetNeighborsDiagonal(BlockPos from) {
        return new BlockPos[] {from.up(), from.down(),
          from.north(), from.east(), from.south(), from.west(),
          from.north().east(), from.east().south(), from.south().west(), from.west().north(),
          from.north().up(), from.east().up(), from.south().up(), from.west().up(),
          from.north().down(), from.east().down(), from.south().down(), from.west().down()
        };
    }
    
    public static BlockPos[] GetHorizontalNeighbors(BlockPos from) {
        return new BlockPos[] {from.north(), from.east(), from.south(), from.west()};
    }
    
}
