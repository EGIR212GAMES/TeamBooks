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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    private static final Map<UUID, Integer> MINE_CART_LEVELS = new ConcurrentHashMap<>();

    public static void setMinecartLevel(UUID uuid, int level) {
        if (level > 0) {
            MINE_CART_LEVELS.put(uuid, level);
        } else {
            MINE_CART_LEVELS.remove(uuid);
        }
    }

    public static int getMinecartLevel(UUID uuid) {
        return MINE_CART_LEVELS.getOrDefault(uuid, 0);
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
