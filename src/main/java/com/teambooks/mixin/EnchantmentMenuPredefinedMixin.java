package com.teambooks.mixin;

import com.teambooks.EnchantUtil;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuPredefinedMixin {
    @Inject(method = "getEnchantmentList", at = @At("HEAD"), cancellable = true)
    private void teambooks_predefinedKakParovoz(net.minecraft.core.RegistryAccess access,
                                                ItemStack stack, int slotIndex, int cost,
                                                CallbackInfoReturnable<List<EnchantmentInstance>> ci) {
        if (!EnchantUtil.isMinecart(stack)) {
            return;
        }
        ci.setReturnValue(EnchantUtil.predefinedKakParovoz(access, slotIndex));
    }
}
