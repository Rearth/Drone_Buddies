package rearth.util;

import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class Helpers {
    
    // in degrees
    public static float calculateYaw(Vec3d self, Vec3d target) {
        var direction = target.subtract(self).normalize();
        return (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90;
    }
    
    public static Vec3d lerp(Vec3d a, Vec3d b, float f) {
        return new Vec3d(lerp(a.x, b.x, f), lerp(a.y, b.y, f / 2f), lerp(a.z, b.z, f));
    }
    
    public static double lerp(double a, double b, double f) {
        return a + f * (b - a);
    }
    
    public static boolean isLineAvailable(World world, Vec3d to, Vec3d from) {
        var context = new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, ShapeContext.absent());
        var result = world.raycast(context);
        return result == null || !result.isInsideBlock();
    }
    
    public static boolean isPositionAvailable(World world, Vec3d pos, Vec3d from) {
        var backDir = from.subtract(pos).normalize();
        var start = pos.add(backDir.multiply(0.5f));
        
        var context = new RaycastContext(start, pos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, ShapeContext.absent());
        var result = world.raycast(context);
        return result == null || !result.isInsideBlock();
    }
}
