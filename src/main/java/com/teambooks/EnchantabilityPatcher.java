package com.teambooks;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantable;

import java.util.Set;

public final class EnchantabilityPatcher {
    private static final int ENCHANTABILITY = 15;

    private static final Set<Identifier> TARGETS = Set.of(
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

    private EnchantabilityPatcher() {
    }

    public static void apply(MinecraftServer server) {
        var itemRegistry = server.registryAccess().lookupOrThrow(Registries.ITEM);
        int patched = 0;
        for (Identifier id : TARGETS) {
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
            if (!itemRegistry.containsKey(key)) {
                TeamBooksMod.LOGGER.warn("TeamBooks patcher: {} not found", id);
                continue;
            }
            Holder<Item> holder = itemRegistry.getOrThrow(key);
            DataComponentMap original = holder.components();
            if (original.get(DataComponents.ENCHANTABLE) != null) {
                continue;
            }
            DataComponentMap updated = DataComponentMap.builder()
                    .addAll(original)
                    .set(DataComponents.ENCHANTABLE, new Enchantable(ENCHANTABILITY))
                    .build();
            ((Holder.Reference<Item>) holder).bindComponents(updated);
            patched++;
        }
        TeamBooksMod.LOGGER.info("TeamBooks patcher: added enchantable to {} items", patched);
    }
}
