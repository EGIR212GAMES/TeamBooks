package com.teambooks.mixin;

import com.mojang.datafixers.util.Pair;
import com.teambooks.EnchantUtil;
import com.teambooks.KakParovozMinecart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecart.class)
public abstract class MinecartSpeedMixin implements KakParovozMinecart {
    private static final ResourceKey<Enchantment> KAK_PAROVOZ =
            ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("teambooks", "kak_parovoz"));

    @Shadow @Final @Mutable
    private MinecartBehavior behavior;

    @Unique
    private int teambooks$kakParovozLevel;

    @Override
    public int teambooksGetKakParovozLevel() {
        return this.teambooks$kakParovozLevel;
    }

    @Override
    public void teambooksSetKakParovozLevel(int level) {
        this.teambooks$kakParovozLevel = Math.max(0, Math.min(3, level));
    }

    @Override
    public void teambooksEnsureNewMinecartBehavior() {
        if (!(this.behavior instanceof NewMinecartBehavior)) {
            AbstractMinecart self = (AbstractMinecart) (Object) this;
            this.behavior = new NewMinecartBehavior(self);
        }
    }

    @Inject(method = "createMinecart", at = @At("RETURN"))
    private static void teambooks_captureEnchantment(Level level, double x, double y, double z,
                                                     EntityType<?> type, EntitySpawnReason reason,
                                                     ItemStack stack, Player player,
                                                     CallbackInfoReturnable<?> ci) {
        if (!(ci.getReturnValue() instanceof AbstractMinecart cart)) {
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

        int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(holderOpt.get(), stack);
        if (enchantmentLevel > 0) {
            KakParovozMinecart enchantedCart = (KakParovozMinecart) (Object) cart;
            enchantedCart.teambooksSetKakParovozLevel(enchantmentLevel);
            enchantedCart.teambooksEnsureNewMinecartBehavior();
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void teambooks$saveLevel(ValueOutput out, CallbackInfo ci) {
        out.putInt(EnchantUtil.NBT_KEY_KAK_PAROVOZ, teambooksGetKakParovozLevel());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void teambooks$loadLevel(ValueInput in, CallbackInfo ci) {
        int enchantmentLevel = in.getIntOr(EnchantUtil.NBT_KEY_KAK_PAROVOZ, 0);
        teambooksSetKakParovozLevel(enchantmentLevel);
        if (enchantmentLevel > 0) {
            teambooksEnsureNewMinecartBehavior();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void teambooks$setTargetSpeedOnRail(CallbackInfo ci) {
        int enchantmentLevel = teambooksGetKakParovozLevel();
        if (enchantmentLevel <= 0) {
            return;
        }

        // OldMinecartBehavior does not iterate across successive rail blocks
        // during one tick. Replace it before vanilla movement is processed.
        teambooksEnsureNewMinecartBehavior();

        AbstractMinecart self = (AbstractMinecart) (Object) this;
        if (!(self.level() instanceof ServerLevel level)) {
            return;
        }

        BlockPos railPos = self.getCurrentBlockPosOrRailBelow();
        BlockState railState = level.getBlockState(railPos);
        if (!BaseRailBlock.isRail(railState)) {
            return;
        }

        RailShape shape = railState.getValue(((BaseRailBlock) railState.getBlock()).getShapeProperty());
        Pair<Vec3i, Vec3i> exits = AbstractMinecart.exits(shape);

        Vec3 railDirection = new Vec3(
                exits.getSecond().getX() - exits.getFirst().getX(),
                exits.getSecond().getY() - exits.getFirst().getY(),
                exits.getSecond().getZ() - exits.getFirst().getZ()
        );

        if (railDirection.lengthSqr() <= 1.0e-6) {
            return;
        }
        railDirection = railDirection.normalize();

        Vec3 currentMovement = self.getDeltaMovement();
        if (currentMovement.lengthSqr() > 1.0e-6) {
            if (currentMovement.dot(railDirection) < 0.0) {
                railDirection = railDirection.scale(-1.0);
            }
        } else {
            // A freshly placed or stopped cart needs a direction. Align its yaw
            // with the closest of the two directions of the actual rail segment.
            double yaw = Math.toRadians(self.getYRot());
            Vec3 facing = new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
            if (facing.dot(railDirection) < 0.0) {
                railDirection = railDirection.scale(-1.0);
            }
        }

        self.setDeltaMovement(railDirection.scale(EnchantUtil.kakParovozSpeedPerTick(enchantmentLevel)));
    }
}
