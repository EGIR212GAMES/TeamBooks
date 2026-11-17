package com.teambooks.mixin;

import com.teambooks.EnchantUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantableHelperMixin {
    private static final int FALLBACK_ENCHANTABILITY = 15;

    @ModifyVariable(method = "selectEnchantment", at = @At("HEAD"), argsOnly = true)
    private static ItemStack teambooks_selectEnchantmentStack(ItemStack stack) {
        if (EnchantUtil.isEnchantableItem(stack) && !stack.has(DataComponents.ENCHANTABLE)) {
            ItemStack copy = stack.copy();
            copy.set(DataComponents.ENCHANTABLE, new Enchantable(FALLBACK_ENCHANTABILITY));
            return copy;
        }
        return stack;
    }
}
