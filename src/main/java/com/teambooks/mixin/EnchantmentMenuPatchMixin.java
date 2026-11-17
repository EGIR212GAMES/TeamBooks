package com.teambooks.mixin;

import com.teambooks.EnchantUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuPatchMixin {
    private boolean teambooksPatching = false;

    @Inject(method = "slotsChanged", at = @At("HEAD"))
    private void teambooks_patchSlot(Container container, CallbackInfo ci) {
        if (teambooksPatching) {
            return;
        }
        ItemStack stack = container.getItem(0);
        if (stack.isEmpty() || !EnchantUtil.isEnchantableItem(stack)) {
            return;
        }
        if (stack.has(DataComponents.ENCHANTABLE)) {
            return;
        }
        teambooksPatching = true;
        try {
            ItemStack copy = stack.copy();
            copy.set(DataComponents.ENCHANTABLE, new Enchantable(15));
            container.setItem(0, copy);
        } finally {
            teambooksPatching = false;
        }
    }
}
