package net.minecraft.world.item;

import eu.pb4.polymer.core.api.item.PolymerCreativeModeTabUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class CreativeTabHandler {
    public static final Identifier TAB_ID = Identifier.fromNamespaceAndPath("teambooks", "main");
    private static final Identifier SKOROLYOT = Identifier.fromNamespaceAndPath("teambooks", "skorolyot");
    private static final Identifier KAK_PAROVOZ = Identifier.fromNamespaceAndPath("teambooks", "kak_parovoz");

    private CreativeTabHandler() {
    }

    public static void register() {
        PolymerResourcePackUtils.addModAssets("teambooks");

        CreativeModeTab tab = PolymerCreativeModeTabUtils.builder()
                .title(Component.literal("TeamBooks"))
                .icon(() -> new ItemStack(Items.ENCHANTED_BOOK))
                .displayItems((params, output) -> {
                    addAllLevels(output, params.holders(), SKOROLYOT, 5);
                    addAllLevels(output, params.holders(), KAK_PAROVOZ, 3);
                })
                .build();
        PolymerCreativeModeTabUtils.registerPolymerCreativeModeTab(TAB_ID, tab);
    }

    private static void addAllLevels(CreativeModeTab.Output output,
                                     net.minecraft.core.HolderLookup.Provider holders,
                                     Identifier enchantmentId, int maxLevel) {
        var holderOpt = holders.get(ResourceKey.create(Registries.ENCHANTMENT, enchantmentId));
        holderOpt.ifPresent(h -> {
            for (int level = 1; level <= maxLevel; level++) {
                output.accept(book(h, level));
            }
        });
    }

    private static ItemStack book(Holder<Enchantment> holder, int level) {
        ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mutable.set(holder, level);
        stack.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
        return stack;
    }
}
