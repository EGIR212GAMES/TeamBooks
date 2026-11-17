package com.teambooks.mixin;

import com.teambooks.EnchantUtil;
import com.teambooks.KakParovozMinecart;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VehicleEntity.class)
public abstract class VehicleDestroyMixin {
    @Inject(method = "destroy", at = @At("HEAD"), cancellable = true)
    private void teambooks_destroyKeepEnchantment(ServerLevel level, Item item, CallbackInfo ci) {
        VehicleEntity self = (VehicleEntity) (Object) this;
        if (!(self instanceof AbstractMinecart)) {
            return;
        }
        int enchLevel = ((KakParovozMinecart) (Object) self).teambooksGetKakParovozLevel();
        if (enchLevel <= 0) {
            return;
        }
        self.kill(level);
        if (!level.getGameRules().get(GameRules.ENTITY_DROPS).booleanValue()) {
            ci.cancel();
            return;
        }
        ItemStack stack = new ItemStack(item);
        Component customName = self.getCustomName();
        if (customName != null) {
            stack.set(DataComponents.CUSTOM_NAME, customName);
        }
        EnchantUtil.applyKakParovoz(stack, enchLevel, level.registryAccess());
        self.spawnAtLocation(level, stack);
        ci.cancel();
    }
}
