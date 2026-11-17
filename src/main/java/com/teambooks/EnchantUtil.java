package com.teambooks;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.List;
import java.util.Set;

public final class EnchantUtil {
    private static final Set<Identifier> ENCHANTABLE_ITEMS = Set.of(
            Identifier.withDefaultNamespace("white_harness"),
            Identifier.withDefaultNamespace("orange_harness"),
            Identifier.withDefaultNamespace("magenta_harness"),
            Identifier.withDefaultNamespace("light_blue_harness"),
            Identifier.withDefaultNamespace("yellow_harness"),
            Identifier.withDefaultNamespace("lime_harness"),
            Identifier.withDefaultNamespace("pink_harness"),
            Identifier.withDefaultNamespace("gray_harness"),
            Identifier.withDefaultNamespace("light_gray_harness"),
            Identifier.withDefaultNamespace("cyan_harness"),
            Identifier.withDefaultNamespace("purple_harness"),
            Identifier.withDefaultNamespace("blue_harness"),
            Identifier.withDefaultNamespace("brown_harness"),
            Identifier.withDefaultNamespace("green_harness"),
            Identifier.withDefaultNamespace("red_harness"),
            Identifier.withDefaultNamespace("black_harness"),
            Identifier.withDefaultNamespace("minecart"),
            Identifier.withDefaultNamespace("chest_minecart"),
            Identifier.withDefaultNamespace("furnace_minecart"),
            Identifier.withDefaultNamespace("tnt_minecart"),
            Identifier.withDefaultNamespace("hopper_minecart"),
            Identifier.withDefaultNamespace("command_block_minecart"));

    private EnchantUtil() {
    }

    public static boolean isEnchantableItem(ItemStack stack) {
        return stack.typeHolder().unwrapKey()
                .map(key -> ENCHANTABLE_ITEMS.contains(key.identifier()))
                .orElse(false);
    }

    private static final Identifier KAK_PAROVOZ = Identifier.fromNamespaceAndPath("teambooks", "kak_parovoz");
    public static final String NBT_KEY_KAK_PAROVOZ = "teambooks:kak_parovoz";

    /**
     * Target movement speed in blocks per tick (Minecraft runs at 20 ticks/second).
     * These values intentionally preserve the enchantment's 20/56/116 blocks-per-second tiers.
     */
    public static double kakParovozSpeedPerTick(int level) {
        return switch (level) {
            case 1 -> 20.0 / 20.0;
            case 2 -> 56.0 / 20.0;
            default -> 116.0 / 20.0;
        };
    }

    public static Holder<Enchantment> kakParovozHolder(net.minecraft.core.RegistryAccess access) {
        var registry = access.lookupOrThrow(Registries.ENCHANTMENT);
        return registry.getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, KAK_PAROVOZ));
    }

    public static void applyKakParovoz(ItemStack stack, int level, net.minecraft.core.RegistryAccess access) {
        if (level <= 0) {
            return;
        }
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(
                stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY));
        mutable.set(kakParovozHolder(access), level);
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
    }

    public static List<EnchantmentInstance> predefinedKakParovoz(net.minecraft.core.RegistryAccess access, int slotIndex) {
        return List.of(new EnchantmentInstance(kakParovozHolder(access), slotIndex + 1));
    }

    public static boolean isMinecart(ItemStack stack) {
        return stack.typeHolder().unwrapKey()
                .map(key -> {
                    Identifier id = key.identifier();
                    return id.equals(Identifier.withDefaultNamespace("minecart"))
                            || id.equals(Identifier.withDefaultNamespace("chest_minecart"))
                            || id.equals(Identifier.withDefaultNamespace("furnace_minecart"))
                            || id.equals(Identifier.withDefaultNamespace("tnt_minecart"))
                            || id.equals(Identifier.withDefaultNamespace("hopper_minecart"))
                            || id.equals(Identifier.withDefaultNamespace("command_block_minecart"));
                })
                .orElse(false);
    }
}
