package com.teambooks;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class AdvancementAwarder {
    private static final Identifier SKOROLYOT_ENCH = Identifier.fromNamespaceAndPath("teambooks", "skorolyot");
    private static final Identifier KAK_PAROVOZ_ENCH = Identifier.fromNamespaceAndPath("teambooks", "kak_parovoz");
    private static final Identifier SKOROLYOT_ADV = Identifier.fromNamespaceAndPath("teambooks", "skorolyot");
    private static final Identifier KAK_PAROVOZ_ADV = Identifier.fromNamespaceAndPath("teambooks", "kak_parovoz");

    private static final Set<UUID> SKOROLYOT_AWARDED = new HashSet<>();
    private static final Set<UUID> KAK_PAROVOZ_AWARDED = new HashSet<>();

    private AdvancementAwarder() {
    }

    public static void onServerTick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            checkAndAward(player, SKOROLYOT_ENCH, SKOROLYOT_ADV, SKOROLYOT_AWARDED);
            checkAndAward(player, KAK_PAROVOZ_ENCH, KAK_PAROVOZ_ADV, KAK_PAROVOZ_AWARDED);
        }
    }

    private static void checkAndAward(ServerPlayer player, Identifier enchId, Identifier advId, Set<UUID> awarded) {
        if (awarded.contains(player.getUUID())) {
            return;
        }
        if (!ownsEnchantment(player, enchId)) {
            return;
        }
        AdvancementHolder holder = player.level().getServer().getAdvancements().get(advId);
        if (holder == null) {
            return;
        }
        if (player.getAdvancements().award(holder, "got_enchantment")) {
            awarded.add(player.getUUID());
        }
    }

    private static boolean ownsEnchantment(ServerPlayer player, Identifier enchId) {
        var enchantmentRegistry = player.level().getServer().registryAccess().lookup(Registries.ENCHANTMENT);
        if (enchantmentRegistry.isEmpty()) {
            return false;
        }
        var holderOpt = enchantmentRegistry.get().get(enchId);
        if (holderOpt.isEmpty()) {
            return false;
        }
        Holder<Enchantment> holder = holderOpt.get();
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (enchantmentLevel(stack, holder) > 0) {
                return true;
            }
        }
        return false;
    }

    private static int enchantmentLevel(ItemStack stack, Holder<Enchantment> holder) {
        int level = 0;
        ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
        if (enchantments != null) {
            level = Math.max(level, enchantments.getLevel(holder));
        }
        ItemEnchantments stored = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (stored != null) {
            level = Math.max(level, stored.getLevel(holder));
        }
        return level;
    }
}
