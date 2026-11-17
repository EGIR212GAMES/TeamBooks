package net.minecraft.world.entity.vehicle.minecart;

import net.minecraft.server.level.ServerLevel;

public final class MinecartTrackBridge {
    private MinecartTrackBridge() {
    }

    public static void moveAlongTrack(AbstractMinecart cart, ServerLevel level) {
        cart.moveAlongTrack(level);
    }
}
