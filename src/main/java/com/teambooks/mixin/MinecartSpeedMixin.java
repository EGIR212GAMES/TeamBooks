package com.teambooks.mixin;

import com.teambooks.EnchantUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecart.class)
public abstract class MinecartSpeedMixin {
    private static final ResourceKey<Enchantment> KAK_PAROVOZ =
            ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("teambooks", "kak_parovoz"));

    @Inject(method = "createMinecart", at = @At("RETURN"))
    private static void teambooks_captureEnchantment(Level level, double x, double y, double z,
                                                     EntityType<?> type, EntitySpawnReason reason,
                                                     ItemStack stack, Player player,
                                                     CallbackInfoReturnable<?> ci) {
        Object result = ci.getReturnValue();
        if (!(result instanceof AbstractMinecart cart)) {
            return;
        }
        var registry = level.registryAccess().lookup(Registries.ENCHANTMENT);
        if (registry.isEmpty()) {
            return;
        }
        var holderOpt = registry.get().get(KAK_PAROVOZ.identifier());
        if (holderOpt.isEmpty()) {
            return;
        }
        int levelValue = EnchantmentHelper.getItemEnchantmentLevel(holderOpt.get(), stack);
        if (levelValue > 0) {
            EnchantUtil.setMinecartLevel(cart.getUUID(), levelValue);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void teambooks_saveLevel(ValueOutput out, CallbackInfo ci) {
        AbstractMinecart self = (AbstractMinecart) (Object) this;
        out.putInt(EnchantUtil.NBT_KEY_KAK_PAROVOZ, EnchantUtil.getMinecartLevel(self.getUUID()));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void teambooks_loadLevel(ValueInput in, CallbackInfo ci) {
        AbstractMinecart self = (AbstractMinecart) (Object) this;
        int levelValue = in.getIntOr(EnchantUtil.NBT_KEY_KAK_PAROVOZ, 0);
        if (levelValue > 0) {
            EnchantUtil.setMinecartLevel(self.getUUID(), levelValue);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void teambooks_setTargetSpeed(CallbackInfo ci) {
        AbstractMinecart self = (AbstractMinecart) (Object) this;
        int levelValue = EnchantUtil.getMinecartLevel(self.getUUID());
        if (levelValue <= 0 || !self.isOnRails()) {
            return;
        }
        double target = targetPerTick(levelValue);
        Vec3 dir = self.getDeltaMovement().horizontal();
        if (dir.lengthSqr() <= 1.0e-6) {
            float yaw = (float) Math.toRadians(self.getYRot());
            dir = new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
        } else {
            dir = dir.normalize();
        }
        self.setDeltaMovement(dir.scale(target));
    }

    @Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
    private void teambooks_maxSpeed(ServerLevel level, CallbackInfoReturnable<Double> ci) {
        AbstractMinecart self = (AbstractMinecart) (Object) this;
        int levelValue = EnchantUtil.getMinecartLevel(self.getUUID());
        if (levelValue <= 0) {
            return;
        }
        ci.setReturnValue(Math.max(ci.getReturnValue(), targetPerTick(levelValue)));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void teambooks_tickSnapToRail(CallbackInfo ci) {
        AbstractMinecart self = (AbstractMinecart) (Object) this;
        int levelValue = EnchantUtil.getMinecartLevel(self.getUUID());
        if (levelValue <= 0 || self.isOnRails()) {
            return;
        }
        if (!(self.level() instanceof ServerLevel level)) {
            return;
        }
        BlockPos rail = findRailNearby(level, BlockPos.containing(self.position()));
        if (rail == null) {
            return;
        }
        Vec3 target = Vec3.atBottomCenterOf(rail).add(0.0, 0.1, 0.0);
        self.setPos(target.x, target.y, target.z);
    }

    private static BlockPos findRailNearby(ServerLevel level, BlockPos center) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    if (BaseRailBlock.isRail(level, p)) {
                        return p;
                    }
                }
            }
        }
        return null;
    }

    private static double targetPerTick(int levelValue) {
        return switch (levelValue) {
            case 1 -> 20.0 / 20.0;
            case 2 -> 56.0 / 20.0;
            default -> 116.0 / 20.0;
        };
    }
}
