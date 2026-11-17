package com.teambooks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.CreativeTabHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TeamBooksMod implements ModInitializer {
    public static final String MOD_ID = "teambooks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Identifier SKOROLYOT = Identifier.fromNamespaceAndPath("teambooks", "skorolyot");
    private static final Identifier KAK_PAROVOZ = Identifier.fromNamespaceAndPath("teambooks", "kak_parovoz");

    @Override
    public void onInitialize() {
        UseBlockCallback.EVENT.register(BookCopyHandler::handle);
        ServerTickEvents.END_SERVER_TICK.register(AdvancementAwarder::onServerTick);
        CreativeTabHandler.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            EnchantabilityPatcher.apply(server);
            var lookup = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            checkObtainability(SKOROLYOT, lookup);
            checkObtainability(KAK_PAROVOZ, lookup);
            checkEnchantingTable(server, lookup);
        });

        LOGGER.info("TeamBooks initialized (server-side).");
    }

    private static void checkObtainability(Identifier enchantmentId,
                                           net.minecraft.core.Registry<Enchantment> lookup) {
        ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, enchantmentId);
        if (!lookup.containsKey(key)) {
            LOGGER.warn("TeamBooks: {} is NOT registered", enchantmentId);
            return;
        }
        Holder<Enchantment> holder = lookup.getOrThrow(key);
        StringBuilder sb = new StringBuilder("TeamBooks obtainability [").append(enchantmentId).append("]: ")
                .append("in_enchanting_table=").append(holder.is(EnchantmentTags.IN_ENCHANTING_TABLE))
                .append(", tradeable=").append(holder.is(EnchantmentTags.TRADEABLE))
                .append(", trades_plains=").append(holder.is(EnchantmentTags.TRADES_PLAINS_COMMON))
                .append(", trades_desert=").append(holder.is(EnchantmentTags.TRADES_DESERT_COMMON))
                .append(", trades_jungle=").append(holder.is(EnchantmentTags.TRADES_JUNGLE_COMMON))
                .append(", trades_savanna=").append(holder.is(EnchantmentTags.TRADES_SAVANNA_COMMON))
                .append(", trades_snow=").append(holder.is(EnchantmentTags.TRADES_SNOW_COMMON))
                .append(", trades_swamp=").append(holder.is(EnchantmentTags.TRADES_SWAMP_COMMON))
                .append(", trades_taiga=").append(holder.is(EnchantmentTags.TRADES_TAIGA_COMMON));
        LOGGER.info(sb.toString());
    }

    private static void checkEnchantingTable(net.minecraft.server.MinecraftServer server,
                                             net.minecraft.core.Registry<Enchantment> lookup) {
        var tagOpt = lookup.get(EnchantmentTags.IN_ENCHANTING_TABLE);
        if (tagOpt.isEmpty()) {
            LOGGER.warn("TeamBooks: in_enchanting_table tag missing");
            return;
        }
        var itemRegistry = server.registryAccess().lookupOrThrow(Registries.ITEM);
        ItemStack harness = new ItemStack(itemRegistry.getOrThrow(
                ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("white_harness"))));
        ItemStack minecart = new ItemStack(itemRegistry.getOrThrow(
                ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("minecart"))));
        List<ItemStack> candidates = List.of(harness, minecart);
        for (ItemStack stack : candidates) {
            Stream<Holder<Enchantment>> stream = tagOpt.get().stream();
            List<EnchantmentInstance> results =
                    EnchantmentHelper.selectEnchantment(server.overworld().getRandom(), stack, 15, stream);
            StringBuilder sb = new StringBuilder("TeamBooks table-test [").append(stack.getItem()).append("]: ")
                    .append(results.size()).append(" options");
            for (EnchantmentInstance r : results) {
                sb.append("; ").append(r.enchantment().unwrapKey()
                        .map(k -> k.identifier().toString())
                        .orElse("?"));
            }
            LOGGER.info(sb.toString());
        }
        if (EnchantUtil.isMinecart(minecart)) {
            var access = server.registryAccess();
            for (int slot = 0; slot < 3; slot++) {
                List<EnchantmentInstance> predefined = EnchantUtil.predefinedKakParovoz(access, slot);
                if (predefined.isEmpty()) {
                    LOGGER.info("TeamBooks predefined slot {} empty", slot);
                    continue;
                }
                EnchantmentInstance r = predefined.get(0);
                LOGGER.info("TeamBooks predefined slot {} -> {} level {}",
                        slot,
                        r.enchantment().unwrapKey().map(k -> k.identifier().toString()).orElse("?"),
                        r.level());
            }
        }
    }
}
