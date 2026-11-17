package com.teambooks.mixin;

import com.teambooks.EnchantUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class EnchantableItemMixin {
    @Inject(method = "isEnchantable", at = @At("HEAD"), cancellable = true)
    private void teambooks_isEnchantable(CallbackInfoReturnable<Boolean> ci) {
        ItemStack self = (ItemStack) (Object) this;
        if (EnchantUtil.isEnchantableItem(self)) {
            ItemEnchantments enchantments = self.get(DataComponents.ENCHANTMENTS);
            ci.setReturnValue(enchantments == null || enchantments.isEmpty());
        }
    }
}
