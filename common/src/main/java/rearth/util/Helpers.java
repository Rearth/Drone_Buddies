package rearth.util;

import net.minecraft.util.math.Vec3d;

public class Helpers {
    
    // in degrees
    public static float calculateYaw(Vec3d self, Vec3d target) {
        var direction = target.subtract(self).normalize();
        return (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90;
    }
    
}
