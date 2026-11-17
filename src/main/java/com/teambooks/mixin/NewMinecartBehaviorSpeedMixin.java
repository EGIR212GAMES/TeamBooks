package com.teambooks.mixin;

import com.teambooks.EnchantUtil;
import com.teambooks.KakParovozMinecart;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NewMinecartBehavior reads max_minecart_speed from the world gamerules.
 * For enchanted carts, return the enchantment tier directly before that lookup
 * so the enchantment also works in worlds without the Minecart Improvements experiment.
 */
@Mixin(NewMinecartBehavior.class)
public abstract class NewMinecartBehaviorSpeedMixin {
    @Inject(method = "getMaxSpeed", at = @At("HEAD"), cancellable = true)
    private void teambooks$overrideEnchantedMaxSpeed(ServerLevel level,
                                                     CallbackInfoReturnable<Double> ci) {
        AbstractMinecart minecart = ((MinecartBehaviorAccessor) (Object) this).teambooksGetMinecart();
        if (!(minecart instanceof KakParovozMinecart enchantedCart)) {
            return;
        }

        int enchantmentLevel = enchantedCart.teambooksGetKakParovozLevel();
        if (enchantmentLevel > 0) {
            ci.setReturnValue(EnchantUtil.kakParovozSpeedPerTick(enchantmentLevel));
        }
    }
}
